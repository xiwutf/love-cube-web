package com.lovecube.backend.controllers;

import com.lovecube.backend.services.BlacklistService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/blacklist")
public class BlacklistController {

    private final BlacklistService blacklistService;

    public BlacklistController(BlacklistService blacklistService) {
        this.blacklistService = blacklistService;
    }

    @GetMapping
    public ResponseEntity<?> getMyBlacklist(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = blacklistService.getUserIdFromToken(authHeader);
            return ResponseEntity.ok(blacklistService.getMyBlacklist(userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{targetId}")
    public ResponseEntity<?> blockUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long targetId,
            @RequestBody(required = false) Map<String, Object> body) {
        try {
            Long userId = blacklistService.getUserIdFromToken(authHeader);
            String reason = body != null ? (String) body.get("reason") : null;
            blacklistService.blockUser(userId, targetId, reason);
            return ResponseEntity.ok(Map.of("message", "已拉黑"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{targetId}")
    public ResponseEntity<?> unblockUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long targetId) {
        try {
            Long userId = blacklistService.getUserIdFromToken(authHeader);
            blacklistService.unblockUser(userId, targetId);
            return ResponseEntity.ok(Map.of("message", "已解除拉黑"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/status/{targetId}")
    public ResponseEntity<?> getBlockStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long targetId) {
        try {
            Long userId = blacklistService.getUserIdFromToken(authHeader);
            return ResponseEntity.ok(blacklistService.getBlockStatus(userId, targetId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }
}
