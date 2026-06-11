package com.lovecube.backend.services;

import com.lovecube.backend.growth.entity.UserGrowth;
import com.lovecube.backend.growth.repository.GrowthUserGrowthRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 联谊 Me 页成长摘要：复用 P0 贡献成长引擎。 */
@Service
public class FellowshipGrowthSummaryService {
    private final GrowthUserGrowthRepository growthUserGrowthRepository;
    private final GrowthService growthService;
    private final FellowshipUserBadgeService fellowshipUserBadgeService;

    public FellowshipGrowthSummaryService(
            GrowthUserGrowthRepository growthUserGrowthRepository,
            GrowthService growthService,
            FellowshipUserBadgeService fellowshipUserBadgeService
    ) {
        this.growthUserGrowthRepository = growthUserGrowthRepository;
        this.growthService = growthService;
        this.fellowshipUserBadgeService = fellowshipUserBadgeService;
    }

    public Map<String, Object> buildMeGrowthSummary(Long userId) {
        UserGrowth growth = growthUserGrowthRepository.findByUserId(userId).orElse(null);
        int total = growth == null ? 0 : safe(growth.getTotalContribution());
        int level = growth == null ? 1 : (safe(growth.getLevel()) == 0 ? resolveLevel(total) : safe(growth.getLevel()));
        int nextLevel = Math.min(10, level + 1);
        int nextThreshold = resolveLevelThreshold(nextLevel);
        int toNext = Math.max(0, nextThreshold - total);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("growthLevel", level);
        summary.put("growthTitle", resolveGrowthTitle(level));
        summary.put("currentExp", total);
        summary.put("nextLevelExp", nextThreshold);
        summary.put("expToNextLevel", toNext);
        summary.put("badges", growthService.getMyBadges(userId));
        summary.put("recentlyUnlockedBadges", fellowshipUserBadgeService.buildRecentlyUnlockedBadges(userId, 3));
        return summary;
    }

    public Map<String, Object> buildCardGrowthFields(Long userId) {
        UserGrowth growth = growthUserGrowthRepository.findByUserId(userId).orElse(null);
        int total = growth == null ? 0 : safe(growth.getTotalContribution());
        int level = growth == null ? 1 : (safe(growth.getLevel()) == 0 ? resolveLevel(total) : safe(growth.getLevel()));
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("growthLevel", level);
        row.put("growthTitle", resolveGrowthTitle(level));
        return row;
    }

    private static int safe(Integer value) {
        return value == null ? 0 : value;
    }

    private static int resolveLevel(int totalContribution) {
        if (totalContribution >= 3000) return 10;
        if (totalContribution >= 2000) return 9;
        if (totalContribution >= 1500) return 8;
        if (totalContribution >= 1000) return 7;
        if (totalContribution >= 750) return 6;
        if (totalContribution >= 500) return 5;
        if (totalContribution >= 300) return 4;
        if (totalContribution >= 150) return 3;
        if (totalContribution >= 50) return 2;
        return 1;
    }

    private static int resolveLevelThreshold(int level) {
        return switch (level) {
            case 2 -> 50;
            case 3 -> 150;
            case 4 -> 300;
            case 5 -> 500;
            case 6 -> 750;
            case 7 -> 1000;
            case 8 -> 1500;
            case 9 -> 2000;
            case 10 -> 3000;
            default -> 50;
        };
    }

    private static String resolveGrowthTitle(int level) {
        return switch (level) {
            case 1 -> "新手用户";
            case 2 -> "成长新星";
            case 3 -> "社区活跃者";
            case 4 -> "内容探索者";
            case 5 -> "内容探索者";
            case 6 -> "联谊达人";
            case 7 -> "推荐之星";
            case 8 -> "团体候选者";
            case 9 -> "人气伙伴";
            case 10 -> "新星推广官";
            default -> "新手用户";
        };
    }
}
