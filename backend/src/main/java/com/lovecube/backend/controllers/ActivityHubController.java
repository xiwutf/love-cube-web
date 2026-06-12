package com.lovecube.backend.controllers;

import com.lovecube.backend.models.User;
import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.UserActivityHubService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/platform/my-activities")
public class ActivityHubController {

    private final AdminAuthService adminAuthService;
    private final UserActivityHubService hubService;

    public ActivityHubController(AdminAuthService adminAuthService, UserActivityHubService hubService) {
        this.adminAuthService = adminAuthService;
        this.hubService = hubService;
    }

    @GetMapping("/group-signups")
    public List<Map<String, Object>> myGroupSignups(
            @RequestHeader("Authorization") String authHeader
    ) {
        User user = adminAuthService.requireUser(authHeader);
        return hubService.listMyGroupSignups(user);
    }

    @GetMapping("/proposals")
    public List<Map<String, Object>> myProposals(
            @RequestHeader("Authorization") String authHeader
    ) {
        User user = adminAuthService.requireUser(authHeader);
        return hubService.listMyProposals(user);
    }

    @GetMapping("/hosted-count")
    public Map<String, Object> hostedCount(
            @RequestHeader("Authorization") String authHeader
    ) {
        User user = adminAuthService.requireUser(authHeader);
        return Map.of("count", hubService.countHostedActivities(user));
    }
}
