package com.lovecube.backend.controllers;

import com.lovecube.backend.models.User;
import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.AiAssistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai-assist")
public class AiAssistController {

    private final AiAssistService aiAssistService;
    private final AdminAuthService adminAuthService;

    public AiAssistController(AiAssistService aiAssistService, AdminAuthService adminAuthService) {
        this.aiAssistService = aiAssistService;
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/profile-polish")
    public ResponseEntity<Map<String, Object>> polishProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> body
    ) {
        adminAuthService.requireUser(authHeader);
        String bio = String.valueOf(body.getOrDefault("bio", ""));
        String nickname = String.valueOf(body.getOrDefault("nickname", ""));
        return ResponseEntity.ok(aiAssistService.polishProfile(bio, nickname));
    }

    @GetMapping("/icebreaker")
    public Map<String, Object> icebreaker(@RequestParam(defaultValue = "match") String scene) {
        return aiAssistService.icebreakerHints(scene);
    }
}
