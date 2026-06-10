package com.lovecube.backend.controllers;

import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.AdminPlatformGroupStatsService;
import com.lovecube.backend.services.PermissionConstants;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/platform-groups")
public class AdminPlatformGroupStatsController {

    private final AdminPlatformGroupStatsService statsService;
    private final AdminAuthService adminAuthService;

    public AdminPlatformGroupStatsController(
            AdminPlatformGroupStatsService statsService,
            AdminAuthService adminAuthService
    ) {
        this.statsService = statsService;
        this.adminAuthService = adminAuthService;
    }

    @GetMapping("/stats")
    public Map<String, Object> overview(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        var user = adminAuthService.requireUser(authHeader);
        Set<String> perms = adminAuthService.getUserPermissions(user);
        if (!perms.contains(PermissionConstants.GROUP_MANAGE_ALL)
                && !perms.contains(PermissionConstants.GROUP_MANAGE_OWN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无团体管理权限");
        }
        return statsService.overview();
    }
}
