package com.lovecube.backend.controllers;

import com.lovecube.backend.models.User;
import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.GrowthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class GrowthController {
    private final GrowthService growthService;
    private final AdminAuthService adminAuthService;

    public GrowthController(GrowthService growthService, AdminAuthService adminAuthService) {
        this.growthService = growthService;
        this.adminAuthService = adminAuthService;
    }

    @GetMapping("/users/me/growth")
    public ResponseEntity<?> getMyGrowth(@RequestHeader("Authorization") String authHeader) {
        User user = adminAuthService.requireUser(authHeader);
        return ResponseEntity.ok(growthService.getGrowthOverview(user.getUserid()));
    }

    @PostMapping("/growth/actions")
    public ResponseEntity<?> recordGrowthAction(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> body
    ) {
        User user = adminAuthService.requireUser(authHeader);
        String actionType = body.get("actionType");
        String bizId = body.get("bizId");
        Map<String, Object> result = growthService.recordAction(user.getUserid(), actionType, bizId);
        if (Boolean.FALSE.equals(result.get("recorded")) && !"true".equals(String.valueOf(result.get("duplicate")))) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/daily-tasks/{taskCode}/claim")
    public ResponseEntity<?> claimDailyTask(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String taskCode
    ) {
        User user = adminAuthService.requireUser(authHeader);
        try {
            return ResponseEntity.ok(growthService.claimDailyTask(user.getUserid(), taskCode));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("/badges/me")
    public ResponseEntity<List<Map<String, Object>>> getMyBadges(@RequestHeader("Authorization") String authHeader) {
        User user = adminAuthService.requireUser(authHeader);
        return ResponseEntity.ok(growthService.getMyBadges(user.getUserid()));
    }
}
