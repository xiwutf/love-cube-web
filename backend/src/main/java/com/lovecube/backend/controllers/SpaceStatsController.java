package com.lovecube.backend.controllers;

import com.lovecube.backend.models.User;
import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.SpaceStatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/platform/groups/{groupId}/space-stats")
public class SpaceStatsController {

    private final AdminAuthService adminAuthService;
    private final SpaceStatsService spaceStatsService;

    public SpaceStatsController(
            AdminAuthService adminAuthService,
            SpaceStatsService spaceStatsService
    ) {
        this.adminAuthService = adminAuthService;
        this.spaceStatsService = spaceStatsService;
    }

    @GetMapping
    public Map<String, Object> getSpaceStats(
            @PathVariable Long groupId,
            @RequestHeader("Authorization") String authHeader
    ) {
        User user = adminAuthService.requireUser(authHeader);
        return spaceStatsService.getSpaceStats(groupId, user);
    }
}
