package com.lovecube.backend.controllers;

import com.lovecube.backend.models.User;
import com.lovecube.backend.services.SocialBindingService;
import com.lovecube.backend.services.UnifiedProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/social-bindings")
public class SocialBindingController {

    private final SocialBindingService socialBindingService;
    private final UnifiedProfileService unifiedProfileService;

    public SocialBindingController(SocialBindingService socialBindingService,
                                   UnifiedProfileService unifiedProfileService) {
        this.socialBindingService = socialBindingService;
        this.unifiedProfileService = unifiedProfileService;
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            User user = unifiedProfileService.requireCurrentUser(authHeader);
            return ResponseEntity.ok(socialBindingService.listBindings(user.getUserid()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/wechat/mock-bind")
    public ResponseEntity<?> mockBindWechat(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            User user = unifiedProfileService.requireCurrentUser(authHeader);
            return ResponseEntity.ok(socialBindingService.mockBindWechatOfficial(user.getUserid()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> unbind(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        try {
            User user = unifiedProfileService.requireCurrentUser(authHeader);
            boolean ok = socialBindingService.unbind(user.getUserid(), id);
            if (!ok) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "绑定不存在"));
            }
            return ResponseEntity.ok(Map.of("message", "已解绑"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }
}
