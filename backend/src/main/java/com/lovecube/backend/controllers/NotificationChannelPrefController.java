package com.lovecube.backend.controllers;

import com.lovecube.backend.entity.UserNotification;
import com.lovecube.backend.models.User;
import com.lovecube.backend.services.NotificationService;
import com.lovecube.backend.services.UnifiedProfileService;
import com.lovecube.backend.services.UserNotificationChannelPrefService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notification-channel-prefs")
public class NotificationChannelPrefController {

    private final UserNotificationChannelPrefService channelPrefService;
    private final NotificationService notificationService;
    private final UnifiedProfileService unifiedProfileService;

    public NotificationChannelPrefController(
            UserNotificationChannelPrefService channelPrefService,
            NotificationService notificationService,
            UnifiedProfileService unifiedProfileService
    ) {
        this.channelPrefService = channelPrefService;
        this.notificationService = notificationService;
        this.unifiedProfileService = unifiedProfileService;
    }

    @GetMapping
    public ResponseEntity<?> get(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            User user = unifiedProfileService.requireCurrentUser(authHeader);
            return ResponseEntity.ok(channelPrefService.toApiMap(user.getUserid()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping
    public ResponseEntity<?> update(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body
    ) {
        try {
            User user = unifiedProfileService.requireCurrentUser(authHeader);
            channelPrefService.update(user.getUserid(), body != null ? body : Map.of());
            return ResponseEntity.ok(Map.of("message", "已保存"));
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("Token")) {
                return ResponseEntity.badRequest().body(Map.of("message", msg));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", msg));
        }
    }

    @PostMapping("/test")
    public ResponseEntity<?> sendTest(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            User user = unifiedProfileService.requireCurrentUser(authHeader);
            UserNotification n = notificationService.sendChannelTestNotification(user.getUserid());
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("message", n != null ? "测试通知已发送，请查收站内消息及已开启的外部渠道" : "站内消息未创建（可能被关闭），外部渠道不会投递");
            body.put("notificationId", n != null ? n.getId() : null);
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }
}
