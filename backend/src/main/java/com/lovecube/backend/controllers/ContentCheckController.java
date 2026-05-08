package com.lovecube.backend.controllers;

import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.ContentRiskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/content")
public class ContentCheckController {

    @Autowired
    private ContentRiskService contentRiskService;

    @Autowired
    private AdminAuthService adminAuthService;

    @PostMapping("/check")
    public ResponseEntity<?> check(
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String text = (String) request.get("text");
        if (text == null || text.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "text is required"));
        }

        Long userId = null;
        try {
            if (authHeader != null && !authHeader.isBlank()) {
                userId = adminAuthService.requireUser(authHeader).getUserid();
            }
        } catch (Exception ignored) {
            // 未登录或 token 失效，仍允许检测，userId 记为 null
        }

        String context = (String) request.getOrDefault("context", "content-check");
        Map<String, Object> result = contentRiskService.check(text, userId, context);
        return ResponseEntity.ok(result);
    }
}
