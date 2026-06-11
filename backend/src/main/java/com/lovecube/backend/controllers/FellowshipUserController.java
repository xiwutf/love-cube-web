package com.lovecube.backend.controllers;

import com.lovecube.backend.entity.UserInteraction;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.EventSignupRepository;
import com.lovecube.backend.repository.MatchRecordRepository;
import com.lovecube.backend.repository.UserBlacklistRepository;
import com.lovecube.backend.repository.UserInteractionRepository;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.repository.UserVisitorRepository;
import com.lovecube.backend.services.BlacklistService;
import com.lovecube.backend.services.FellowshipGrowthSummaryService;
import com.lovecube.backend.services.SwipeQuotaService;
import com.lovecube.backend.services.UnifiedProfileService;
import com.lovecube.backend.services.UserVisitorService;
import com.lovecube.backend.services.VipService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fellowship/users")
public class FellowshipUserController {
    private final UnifiedProfileService unifiedProfileService;
    private final UserRepository userRepository;
    private final BlacklistService blacklistService;
    private final UserVisitorRepository userVisitorRepository;
    private final UserVisitorService userVisitorService;
    private final UserInteractionRepository userInteractionRepository;
    private final MatchRecordRepository matchRecordRepository;
    private final UserBlacklistRepository userBlacklistRepository;
    private final EventSignupRepository eventSignupRepository;
    private final VipService vipService;
    private final SwipeQuotaService swipeQuotaService;
    private final FellowshipGrowthSummaryService fellowshipGrowthSummaryService;

    public FellowshipUserController(UnifiedProfileService unifiedProfileService,
                                    UserRepository userRepository,
                                    BlacklistService blacklistService,
                                    UserVisitorRepository userVisitorRepository,
                                    UserVisitorService userVisitorService,
                                    UserInteractionRepository userInteractionRepository,
                                    MatchRecordRepository matchRecordRepository,
                                    UserBlacklistRepository userBlacklistRepository,
                                    EventSignupRepository eventSignupRepository,
                                    VipService vipService,
                                    SwipeQuotaService swipeQuotaService,
                                    FellowshipGrowthSummaryService fellowshipGrowthSummaryService) {
        this.unifiedProfileService = unifiedProfileService;
        this.userRepository = userRepository;
        this.blacklistService = blacklistService;
        this.userVisitorRepository = userVisitorRepository;
        this.userVisitorService = userVisitorService;
        this.userInteractionRepository = userInteractionRepository;
        this.matchRecordRepository = matchRecordRepository;
        this.userBlacklistRepository = userBlacklistRepository;
        this.eventSignupRepository = eventSignupRepository;
        this.vipService = vipService;
        this.swipeQuotaService = swipeQuotaService;
        this.fellowshipGrowthSummaryService = fellowshipGrowthSummaryService;
    }

    @GetMapping("/me/stats")
    public ResponseEntity<?> getMyStats(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            User currentUser = unifiedProfileService.requireCurrentUser(authHeader);
            long userId = currentUser.getUserid();

            LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
            long todayVisitorCount = userVisitorRepository.countTodayVisitors(userId, startOfToday);
            long totalVisitorCount = userVisitorRepository.countByVisitedUserId(userId);
            long likesReceived = userInteractionRepository.countByToUserIdAndInteractionType(
                    userId, UserInteraction.InteractionType.LIKE);
            long mutualMatchCount = matchRecordRepository.findByUserId(userId).size();
            long followingCount = userInteractionRepository.countByFromUserIdAndInteractionType(
                    userId, UserInteraction.InteractionType.FOLLOW);
            long blacklistCount = userBlacklistRepository.findByUserId(userId).size();
            long eventSignupCount = eventSignupRepository.countByUserId(userId);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("todayVisitorCount", todayVisitorCount);
            body.put("totalVisitorCount", totalVisitorCount);
            body.put("likesReceived", likesReceived);
            body.put("mutualMatchCount", mutualMatchCount);
            body.put("followingCount", followingCount);
            body.put("blacklistCount", blacklistCount);
            body.put("eventSignupCount", eventSignupCount);
            body.putAll(vipService.buildVipStatus(currentUser));
            body.put("swipeQuota", swipeQuotaService.getStatus(currentUser));
            body.putAll(unifiedProfileService.buildFellowshipCompletion(currentUser));
            body.putAll(fellowshipGrowthSummaryService.buildMeGrowthSummary(userId));
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "获取统计数据失败: " + e.getMessage()));
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserDetail(
            @PathVariable Long userId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            User currentUser = unifiedProfileService.requireCurrentUser(authHeader);
            userVisitorService.recordVisit(
                    currentUser.getUserid(),
                    userId,
                    com.lovecube.backend.entity.UserVisitor.VisitType.PROFILE,
                    com.lovecube.backend.entity.UserVisitor.VisitSource.PROFILE_PAGE,
                    null,
                    null
            );
        } catch (Exception ignored) {
            // Keep profile reading compatible when auth is absent/invalid; just skip visitor recording.
        }
        return ResponseEntity.ok(unifiedProfileService.buildPublicProfile(userId));
    }

    @GetMapping
    public ResponseEntity<?> listUsers(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "limit", defaultValue = "20") int limit
    ) {
        Long currentId = null;
        try {
            User currentUser = unifiedProfileService.requireCurrentUser(authHeader);
            currentId = currentUser.getUserid();
        } catch (Exception ignored) {
        }

        final Long currentUserId = currentId;
        List<Long> blacklistIds = Collections.emptyList();
        if (currentUserId != null) {
            try {
                blacklistIds = blacklistService.getBlockedAndBlockerIds(currentUserId);
            } catch (Exception ignored) {
            }
        }
        final List<Long> finalBlacklistIds = blacklistIds;

        List<Map<String, Object>> rows = userRepository.findAll().stream()
                .filter(u -> currentUserId == null || !u.getUserid().equals(currentUserId))
                .filter(u -> !finalBlacklistIds.contains(u.getUserid()))
                .filter(u -> !"DISABLED".equalsIgnoreCase(u.getUserStatus()))
                .filter(u -> Boolean.TRUE.equals(u.getFellowshipEnabled()))
                .filter(u -> Boolean.TRUE.equals(u.getFellowshipMatchVisible()))
                .limit(Math.max(1, Math.min(limit, 100)))
                .map(unifiedProfileService::buildLegacyUserPayload)
                .collect(Collectors.toList());
        return ResponseEntity.ok(rows);
    }
}
