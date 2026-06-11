package com.lovecube.backend.controllers;

import com.lovecube.backend.models.User;
import com.lovecube.backend.services.FellowshipProfileGrowthService;
import com.lovecube.backend.services.FellowshipProfileService;
import com.lovecube.backend.services.InviteEffectiveSettleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/fellowship/profile")
public class FellowshipProfileController {
    private final FellowshipProfileService fellowshipProfileService;
    private final FellowshipProfileGrowthService fellowshipProfileGrowthService;
    private final InviteEffectiveSettleService inviteEffectiveSettleService;

    public FellowshipProfileController(
            FellowshipProfileService fellowshipProfileService,
            FellowshipProfileGrowthService fellowshipProfileGrowthService,
            InviteEffectiveSettleService inviteEffectiveSettleService
    ) {
        this.fellowshipProfileService = fellowshipProfileService;
        this.fellowshipProfileGrowthService = fellowshipProfileGrowthService;
        this.inviteEffectiveSettleService = inviteEffectiveSettleService;
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
            fellowshipProfileGrowthService.syncProfileMilestones(currentUser);
            try {
                inviteEffectiveSettleService.trySettleForInvitee(currentUser.getUserid());
            } catch (Exception ignored) {
                // settle must not block profile update
            }
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
}
