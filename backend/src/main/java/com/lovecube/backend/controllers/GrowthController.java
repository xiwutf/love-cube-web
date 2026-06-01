package com.lovecube.backend.controllers;

import com.lovecube.backend.models.User;
import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.ExtendedTaskService;
import com.lovecube.backend.services.GrowthService;
import com.lovecube.backend.services.InviteEffectiveSettleService;
import com.lovecube.backend.services.LoginStreakService;
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
    private final LoginStreakService loginStreakService;
    private final InviteEffectiveSettleService inviteEffectiveSettleService;
    private final ExtendedTaskService extendedTaskService;

    public GrowthController(
            GrowthService growthService,
            AdminAuthService adminAuthService,
            LoginStreakService loginStreakService,
            InviteEffectiveSettleService inviteEffectiveSettleService,
            ExtendedTaskService extendedTaskService
    ) {
        this.growthService = growthService;
        this.adminAuthService = adminAuthService;
        this.loginStreakService = loginStreakService;
        this.inviteEffectiveSettleService = inviteEffectiveSettleService;
        this.extendedTaskService = extendedTaskService;
    }

    @GetMapping("/users/me/growth")
    public ResponseEntity<?> getMyGrowth(@RequestHeader("Authorization") String authHeader) {
        User user = adminAuthService.requireUser(authHeader);
        Map<String, Object> overview = new java.util.LinkedHashMap<>(growthService.getGrowthOverview(user.getUserid()));
        overview.put("loginStreak", loginStreakService.getStreakOverview(user.getUserid()));
        overview.put("weeklyTasks", extendedTaskService.buildWeeklyTaskRows(user.getUserid()));
        overview.put("newcomerPack", extendedTaskService.buildNewcomerPack(user.getUserid()));
        return ResponseEntity.ok(overview);
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
        if ("LOGIN".equalsIgnoreCase(String.valueOf(actionType))) {
            java.time.LocalDate today = java.time.LocalDate.now();
            Map<String, Object> streakResult = loginStreakService.recordLogin(user.getUserid(), today);
            result = new java.util.LinkedHashMap<>(result);
            result.put("loginStreak", streakResult);
            try {
                inviteEffectiveSettleService.trySettleForInvitee(user.getUserid());
            } catch (Exception ignored) {
                // settle must not break checkin
            }
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

    @PostMapping("/account-tasks/{taskCode}/claim")
    public ResponseEntity<?> claimAccountTask(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String taskCode
    ) {
        User user = adminAuthService.requireUser(authHeader);
        try {
            return ResponseEntity.ok(growthService.claimAccountTask(user.getUserid(), taskCode));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("/badges/me")
    public ResponseEntity<List<Map<String, Object>>> getMyBadges(@RequestHeader("Authorization") String authHeader) {
        User user = adminAuthService.requireUser(authHeader);
        return ResponseEntity.ok(growthService.getMyBadges(user.getUserid()));
    }

    @PostMapping("/weekly-tasks/{taskCode}/claim")
    public ResponseEntity<?> claimWeeklyTask(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String taskCode
    ) {
        User user = adminAuthService.requireUser(authHeader);
        try {
            return ResponseEntity.ok(extendedTaskService.claimWeeklyTask(user.getUserid(), taskCode));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/newcomer-tasks/{taskCode}/claim")
    public ResponseEntity<?> claimNewcomerTask(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String taskCode
    ) {
        User user = adminAuthService.requireUser(authHeader);
        try {
            return ResponseEntity.ok(extendedTaskService.claimNewcomerTask(user.getUserid(), taskCode));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
        }
    }
}
