package com.lovecube.backend.controllers;

import com.lovecube.backend.models.User;
import com.lovecube.backend.services.FellowshipProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/fellowship/profile")
public class FellowshipProfileController {
    private final FellowshipProfileService fellowshipProfileService;

    public FellowshipProfileController(FellowshipProfileService fellowshipProfileService) {
        this.fellowshipProfileService = fellowshipProfileService;
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
