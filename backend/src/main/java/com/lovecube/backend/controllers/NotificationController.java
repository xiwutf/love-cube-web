package com.lovecube.backend.controllers;

import com.lovecube.backend.entity.Notification;
import com.lovecube.backend.models.User;
import com.lovecube.backend.services.NotificationService;
import com.lovecube.backend.services.UnifiedProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UnifiedProfileService unifiedProfileService;

    public NotificationController(NotificationService notificationService,
                                  UnifiedProfileService unifiedProfileService) {
        this.notificationService = notificationService;
        this.unifiedProfileService = unifiedProfileService;
    }

    @GetMapping
    public ResponseEntity<?> list(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "50") int limit) {
        try {
            User user = unifiedProfileService.requireCurrentUser(authHeader);
            List<Notification> list = notificationService.getMyNotifications(user.getUserid(), limit);
            return ResponseEntity.ok(list);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> unreadCount(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            User user = unifiedProfileService.requireCurrentUser(authHeader);
            long count = notificationService.getUnreadCount(user.getUserid());
            return ResponseEntity.ok(Map.of("count", count));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markRead(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        try {
            User user = unifiedProfileService.requireCurrentUser(authHeader);
            notificationService.markRead(user.getUserid(), id);
            return ResponseEntity.ok(Map.of("message", "已标记已读"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/read-all")
    public ResponseEntity<?> readAll(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            User user = unifiedProfileService.requireCurrentUser(authHeader);
            notificationService.markAllRead(user.getUserid());
            return ResponseEntity.ok(Map.of("message", "全部已读"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }
}
