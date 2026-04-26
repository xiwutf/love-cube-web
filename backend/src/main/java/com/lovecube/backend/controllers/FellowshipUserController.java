package com.lovecube.backend.controllers;

import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.services.BlacklistService;
import com.lovecube.backend.services.UnifiedProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fellowship/users")
public class FellowshipUserController {
    private final UnifiedProfileService unifiedProfileService;
    private final UserRepository userRepository;
    private final BlacklistService blacklistService;

    public FellowshipUserController(UnifiedProfileService unifiedProfileService,
                                    UserRepository userRepository,
                                    BlacklistService blacklistService) {
        this.unifiedProfileService = unifiedProfileService;
        this.userRepository = userRepository;
        this.blacklistService = blacklistService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserDetail(@PathVariable Long userId) {
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
                .limit(Math.max(1, Math.min(limit, 100)))
                .map(unifiedProfileService::buildLegacyUserPayload)
                .collect(Collectors.toList());
        return ResponseEntity.ok(rows);
    }
}
