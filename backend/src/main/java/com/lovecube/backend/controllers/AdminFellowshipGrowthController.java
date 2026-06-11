package com.lovecube.backend.controllers;

import com.lovecube.backend.models.User;
import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.AdminFellowshipGrowthDashboardService;
import com.lovecube.backend.services.FellowshipGrowthBackfillService;
import com.lovecube.backend.services.PermissionConstants;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/fellowship/growth")
public class AdminFellowshipGrowthController {
    private final AdminAuthService adminAuthService;
    private final FellowshipGrowthBackfillService fellowshipGrowthBackfillService;
    private final AdminFellowshipGrowthDashboardService adminFellowshipGrowthDashboardService;

    public AdminFellowshipGrowthController(
            AdminAuthService adminAuthService,
            FellowshipGrowthBackfillService fellowshipGrowthBackfillService,
            AdminFellowshipGrowthDashboardService adminFellowshipGrowthDashboardService
    ) {
        this.adminAuthService = adminAuthService;
        this.fellowshipGrowthBackfillService = fellowshipGrowthBackfillService;
        this.adminFellowshipGrowthDashboardService = adminFellowshipGrowthDashboardService;
    }

    /**
     * 对历史用户执行一次资料里程碑 growth 补偿与徽章刷新（dedupe 幂等）。
     */
    @PostMapping("/sync-existing-users")
    public ResponseEntity<Map<String, Object>> syncExistingUsers(
            @RequestHeader("Authorization") String authHeader
    ) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.USER_MANAGE);
        return ResponseEntity.ok(fellowshipGrowthBackfillService.syncExistingUsers());
    }

    /**
     * 联谊成长运营看板：资料完成度分布、认证与缺失项统计。
     */
    @GetMapping("/dashboard")
    public Map<String, Object> growthDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        User operator = adminAuthService.requirePermission(authHeader, PermissionConstants.USER_MANAGE);
        return adminFellowshipGrowthDashboardService.buildDashboard(operator);
    }
}
