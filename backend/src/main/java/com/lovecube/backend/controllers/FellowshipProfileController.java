package com.lovecube.backend.controllers;

import com.lovecube.backend.growth.dto.GrowthEventCreateRequest;
import com.lovecube.backend.growth.enums.GrowthEventType;
import com.lovecube.backend.growth.enums.SourcePlatform;
import com.lovecube.backend.growth.service.GrowthEventService;
import com.lovecube.backend.models.User;
import com.lovecube.backend.services.FellowshipProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/fellowship/profile")
public class FellowshipProfileController {
    private final FellowshipProfileService fellowshipProfileService;
    private final GrowthEventService growthEventService;

    public FellowshipProfileController(
            FellowshipProfileService fellowshipProfileService,
            GrowthEventService growthEventService
    ) {
        this.fellowshipProfileService = fellowshipProfileService;
        this.growthEventService = growthEventService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            User currentUser = fellowshipProfileService.requireCurrentUser(authHeader);
            Map<String, Object> profile = fellowshipProfileService.getMyProfile(currentUser);
            return ResponseEntity.ok(fellowshipProfileService.toResponse(profile));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "获取资料失败: " + e.getMessage()));
        }
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateMyProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> payload
    ) {
        try {
            User currentUser = fellowshipProfileService.requireCurrentUser(authHeader);
            Map<String, Object> profile = fellowshipProfileService.updateMyProfile(currentUser, payload);
            publishGrowthEventSafely(currentUser.getUserid());
            return ResponseEntity.ok(fellowshipProfileService.toResponse(profile));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "更新资料失败: " + e.getMessage()));
        }
    }

    @GetMapping("/completion")
    public ResponseEntity<?> getCompletion(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            User currentUser = fellowshipProfileService.requireCurrentUser(authHeader);
            return ResponseEntity.ok(fellowshipProfileService.getCompletion(currentUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "获取完整度失败: " + e.getMessage()));
        }
    }

    private void publishGrowthEventSafely(Long userId) {
        try {
            GrowthEventCreateRequest request = new GrowthEventCreateRequest();
            request.setEventType(GrowthEventType.USER_PROFILE_COMPLETED);
            request.setActorUserId(userId);
            request.setTargetUserId(userId);
            request.setBizRefType("fellowship_profile");
            request.setBizRefId(String.valueOf(userId));
            request.setDedupeKey("growth:user_profile_completed:user:" + userId);
            request.setRuleVersion("v1");
            request.setSourcePlatform(SourcePlatform.API);
            request.setOccurredAt(LocalDateTime.now());
            growthEventService.publish(request);
        } catch (Exception ignored) {
            // Growth event must not block business update.
        }
    }
}
