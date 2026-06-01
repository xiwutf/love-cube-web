package com.lovecube.backend.controllers;

import com.lovecube.backend.models.User;
import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.MatchCompatibilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/match/compatibility")
public class MatchCompatibilityController {

    private final MatchCompatibilityService compatibilityService;
    private final AdminAuthService adminAuthService;

    public MatchCompatibilityController(
            MatchCompatibilityService compatibilityService,
            AdminAuthService adminAuthService
    ) {
        this.compatibilityService = compatibilityService;
        this.adminAuthService = adminAuthService;
    }

    @GetMapping("/{peerUserId}")
    public ResponseEntity<Map<String, Object>> getSession(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long peerUserId
    ) {
        User user = adminAuthService.requireUser(authHeader);
        return ResponseEntity.ok(compatibilityService.getSession(user.getUserid(), peerUserId));
    }

    @PostMapping("/{peerUserId}")
    public ResponseEntity<Map<String, Object>> submit(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long peerUserId,
            @RequestBody Map<String, Object> body
    ) {
        User user = adminAuthService.requireUser(authHeader);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> answers = (List<Map<String, Object>>) body.get("answers");
        return ResponseEntity.ok(compatibilityService.submitAnswers(user.getUserid(), peerUserId, answers));
    }
}
