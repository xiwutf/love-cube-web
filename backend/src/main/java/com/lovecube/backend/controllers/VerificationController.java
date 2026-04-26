package com.lovecube.backend.controllers;

import com.lovecube.backend.entity.UserVerification;
import com.lovecube.backend.models.User;
import com.lovecube.backend.services.UnifiedProfileService;
import com.lovecube.backend.services.VerificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/verify")
public class VerificationController {

    private final VerificationService verificationService;
    private final UnifiedProfileService unifiedProfileService;

    public VerificationController(VerificationService verificationService,
                                  UnifiedProfileService unifiedProfileService) {
        this.verificationService = verificationService;
        this.unifiedProfileService = unifiedProfileService;
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submit(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body) {
        try {
            User user = unifiedProfileService.requireCurrentUser(authHeader);
            String verifyType = String.valueOf(body.getOrDefault("verifyType", "")).trim().toUpperCase();
            if (!List.of("REALNAME", "PHOTO", "IDCARD").contains(verifyType)) {
                return ResponseEntity.badRequest().body(Map.of("message", "verifyType 无效"));
            }
            String submitData = body.containsKey("submitData")
                ? String.valueOf(body.get("submitData")) : "{}";
            UserVerification saved = verificationService.submitVerification(user.getUserid(), verifyType, submitData);
            return ResponseEntity.ok(saved);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> myVerifications(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            User user = unifiedProfileService.requireCurrentUser(authHeader);
            List<UserVerification> list = verificationService.getMyVerifications(user.getUserid());
            return ResponseEntity.ok(list);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/status/{userId}")
    public ResponseEntity<?> statusFor(@PathVariable Long userId) {
        return ResponseEntity.ok(verificationService.getStatusSummary(userId));
    }
}
