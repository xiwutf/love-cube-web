package com.lovecube.backend.growth.controller;

import com.lovecube.backend.growth.dto.GrowthEventCreateRequest;
import com.lovecube.backend.growth.dto.ContributionLogDTO;
import com.lovecube.backend.growth.dto.GrowthCenterDTO;
import com.lovecube.backend.growth.dto.GrowthEventDTO;
import com.lovecube.backend.growth.dto.UserAchievementDTO;
import com.lovecube.backend.growth.entity.ContributionLog;
import com.lovecube.backend.growth.entity.GrowthEvent;
import com.lovecube.backend.growth.entity.UserAchievement;
import com.lovecube.backend.growth.entity.UserGrowth;
import com.lovecube.backend.growth.repository.ContributionLogRepository;
import com.lovecube.backend.growth.repository.GrowthEventRepository;
import com.lovecube.backend.growth.repository.GrowthUserGrowthRepository;
import com.lovecube.backend.growth.repository.UserAchievementRepository;
import com.lovecube.backend.growth.service.GrowthEventService;
import com.lovecube.backend.models.User;
import com.lovecube.backend.services.AdminAuthService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController("growthEventController")
@RequestMapping("/api/growth")
public class GrowthController {
    private final GrowthEventService growthEventService;
    private final AdminAuthService adminAuthService;
    private final GrowthUserGrowthRepository growthUserGrowthRepository;
    private final ContributionLogRepository contributionLogRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final GrowthEventRepository growthEventRepository;

    public GrowthController(
            GrowthEventService growthEventService,
            AdminAuthService adminAuthService,
            GrowthUserGrowthRepository growthUserGrowthRepository,
            ContributionLogRepository contributionLogRepository,
            UserAchievementRepository userAchievementRepository,
            GrowthEventRepository growthEventRepository
    ) {
        this.growthEventService = growthEventService;
        this.adminAuthService = adminAuthService;
        this.growthUserGrowthRepository = growthUserGrowthRepository;
        this.contributionLogRepository = contributionLogRepository;
        this.userAchievementRepository = userAchievementRepository;
        this.growthEventRepository = growthEventRepository;
    }

    @PostMapping("/events")
    public ResponseEntity<?> publishEvent(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody GrowthEventCreateRequest request
    ) {
        User currentUser = adminAuthService.requireUser(authHeader);
        if (!currentUser.getUserid().equals(request.getActorUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "actorUserId must match current user"));
        }

        try {
            return ResponseEntity.ok(growthEventService.publish(request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<GrowthCenterDTO> getMyGrowth(@RequestHeader("Authorization") String authHeader) {
        User currentUser = adminAuthService.requireUser(authHeader);
        UserGrowth growth = growthUserGrowthRepository.findByUserId(currentUser.getUserid())
                .orElse(null);
        return ResponseEntity.ok(toGrowthCenterDto(currentUser.getUserid(), growth));
    }

    @GetMapping("/contributions/me")
    public ResponseEntity<List<ContributionLogDTO>> getMyContributions(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "20") int limit
    ) {
        User currentUser = adminAuthService.requireUser(authHeader);
        int safeLimit = Math.min(100, Math.max(1, limit));
        List<ContributionLogDTO> items = contributionLogRepository
                .findByUserIdOrderByOccurredAtDesc(
                        currentUser.getUserid(),
                        PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "occurredAt"))
                )
                .stream()
                .map(this::toContributionLogDto)
                .toList();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/achievements/me")
    public ResponseEntity<List<UserAchievementDTO>> getMyAchievements(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "20") int limit
    ) {
        User currentUser = adminAuthService.requireUser(authHeader);
        int safeLimit = Math.min(100, Math.max(1, limit));
        List<UserAchievementDTO> items = userAchievementRepository
                .findByUserIdOrderByGrantedAtDesc(
                        currentUser.getUserid(),
                        PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "grantedAt"))
                )
                .stream()
                .map(this::toAchievementDto)
                .toList();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/events/me")
    public ResponseEntity<List<GrowthEventDTO>> getMyEvents(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "20") int limit
    ) {
        User currentUser = adminAuthService.requireUser(authHeader);
        int safeLimit = Math.min(100, Math.max(1, limit));
        List<GrowthEventDTO> items = growthEventRepository
                .findByActorUserIdOrderByOccurredAtDesc(
                        currentUser.getUserid(),
                        PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "occurredAt"))
                )
                .stream()
                .map(this::toGrowthEventDto)
                .toList();
        return ResponseEntity.ok(items);
    }

    private GrowthCenterDTO toGrowthCenterDto(Long userId, UserGrowth growth) {
        GrowthCenterDTO dto = new GrowthCenterDTO();
        dto.setUserId(userId);
        if (growth == null) {
            dto.setLevel(1);
            dto.setStage("normal");
            dto.setTotalContribution(0);
            dto.setContribContent(0);
            dto.setContribOrg(0);
            dto.setContribSpread(0);
            dto.setContribCity(0);
            dto.setNextLevel(2);
            dto.setNextLevelThreshold(50);
            dto.setContributionToNextLevel(50);
            return dto;
        }

        int total = safe(growth.getTotalContribution());
        int level = safe(growth.getLevel()) == 0 ? resolveLevel(total) : safe(growth.getLevel());
        int nextLevel = Math.min(5, level + 1);
        int nextThreshold = resolveLevelThreshold(nextLevel);
        int toNext = Math.max(0, nextThreshold - total);

        dto.setLevel(level);
        dto.setStage(growth.getStage() == null ? "normal" : growth.getStage());
        dto.setTotalContribution(total);
        dto.setContribContent(safe(growth.getContribContent()));
        dto.setContribOrg(safe(growth.getContribOrg()));
        dto.setContribSpread(safe(growth.getContribSpread()));
        dto.setContribCity(safe(growth.getContribCity()));
        dto.setNextLevel(nextLevel);
        dto.setNextLevelThreshold(nextThreshold);
        dto.setContributionToNextLevel(toNext);
        return dto;
    }

    private ContributionLogDTO toContributionLogDto(ContributionLog entity) {
        ContributionLogDTO dto = new ContributionLogDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setEventType(entity.getEventType());
        dto.setDimension(entity.getDimension());
        dto.setDeltaFinal(entity.getDeltaFinal());
        dto.setOccurredAt(entity.getOccurredAt());
        return dto;
    }

    private UserAchievementDTO toAchievementDto(UserAchievement entity) {
        UserAchievementDTO dto = new UserAchievementDTO();
        dto.setId(entity.getId());
        dto.setAchievementCode(entity.getAchievementCode());
        dto.setLevel(entity.getLevel());
        dto.setStatus(entity.getStatus());
        dto.setGrantedAt(entity.getGrantedAt());
        return dto;
    }

    private GrowthEventDTO toGrowthEventDto(GrowthEvent entity) {
        GrowthEventDTO dto = new GrowthEventDTO();
        dto.setId(entity.getId());
        dto.setEventType(entity.getEventType());
        dto.setActorUserId(entity.getActorUserId());
        dto.setTargetUserId(entity.getTargetUserId());
        dto.setSettleStatus(entity.getSettleStatus());
        dto.setSourcePlatform(entity.getSourcePlatform());
        dto.setBizRefType(entity.getBizRefType());
        dto.setBizRefId(entity.getBizRefId());
        dto.setOccurredAt(entity.getOccurredAt());
        return dto;
    }

    private int safe(Integer value) {
        return value == null ? 0 : value;
    }

    private int resolveLevel(int totalContribution) {
        if (totalContribution >= 500) return 5;
        if (totalContribution >= 300) return 4;
        if (totalContribution >= 150) return 3;
        if (totalContribution >= 50) return 2;
        return 1;
    }

    private int resolveLevelThreshold(int level) {
        return switch (level) {
            case 2 -> 50;
            case 3 -> 150;
            case 4 -> 300;
            case 5 -> 500;
            default -> 500;
        };
    }
}
