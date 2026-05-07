package com.lovecube.backend.growth.controller;

import com.lovecube.backend.growth.repository.ContributionLogRepository;
import com.lovecube.backend.growth.repository.GrowthEventRepository;
import com.lovecube.backend.growth.repository.GrowthUserGrowthRepository;
import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.PermissionConstants;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/growth")
public class AdminGrowthController {
    private final AdminAuthService adminAuthService;
    private final GrowthEventRepository growthEventRepository;
    private final ContributionLogRepository contributionLogRepository;
    private final GrowthUserGrowthRepository growthUserGrowthRepository;

    public AdminGrowthController(
            AdminAuthService adminAuthService,
            GrowthEventRepository growthEventRepository,
            ContributionLogRepository contributionLogRepository,
            GrowthUserGrowthRepository growthUserGrowthRepository
    ) {
        this.adminAuthService = adminAuthService;
        this.growthEventRepository = growthEventRepository;
        this.contributionLogRepository = contributionLogRepository;
        this.growthUserGrowthRepository = growthUserGrowthRepository;
    }

    @GetMapping("/overview")
    public Map<String, Object> getOverview(@RequestHeader("Authorization") String authHeader) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.SYSTEM_MANAGE);

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrowStart = todayStart.plusDays(1);

        long totalEvents = growthEventRepository.count();
        long settledEvents = growthEventRepository.countBySettleStatus("SETTLED");
        long pendingEvents = growthEventRepository.countBySettleStatus("PENDING");
        long totalContribution = contributionLogRepository.sumTotalContribution();
        long activeGrowthUsers = growthUserGrowthRepository.countByTotalContributionGreaterThan(0);
        long todayEvents = growthEventRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(todayStart, tomorrowStart);

        Map<String, Long> levelDistribution = new LinkedHashMap<>();
        List<Object[]> levelRows = growthUserGrowthRepository.countLevelDistribution();
        for (Object[] row : levelRows) {
            String level = String.valueOf(row[0]);
            long count = row[1] instanceof Number n ? n.longValue() : 0L;
            levelDistribution.put(level, count);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalEvents", totalEvents);
        result.put("settledEvents", settledEvents);
        result.put("pendingEvents", pendingEvents);
        result.put("totalContribution", totalContribution);
        result.put("activeGrowthUsers", activeGrowthUsers);
        result.put("todayEvents", todayEvents);
        result.put("levelDistribution", levelDistribution);
        return result;
    }
}
