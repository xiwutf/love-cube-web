package com.lovecube.backend.controllers;

import com.lovecube.backend.models.User;
import com.lovecube.backend.services.NotificationSettingService;
import com.lovecube.backend.services.UnifiedProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notification-settings")
public class NotificationSettingController {

    private final NotificationSettingService notificationSettingService;
    private final UnifiedProfileService unifiedProfileService;

    public NotificationSettingController(
            NotificationSettingService notificationSettingService,
            UnifiedProfileService unifiedProfileService) {
        this.notificationSettingService = notificationSettingService;
        this.unifiedProfileService = unifiedProfileService;
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            User user = unifiedProfileService.requireCurrentUser(authHeader);
            return ResponseEntity.ok(notificationSettingService.getOrCreateSettings(user.getUserid()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping
    public ResponseEntity<?> update(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody List<Map<String, Object>> body) {
        try {
            User user = unifiedProfileService.requireCurrentUser(authHeader);
            notificationSettingService.updateSettings(user.getUserid(), body);
            return ResponseEntity.ok(Map.of("message", "已保存"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }
}
