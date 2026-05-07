package com.lovecube.backend.growth.service;

import com.lovecube.backend.growth.enums.ContributionDimension;
import com.lovecube.backend.growth.enums.GrowthEventType;
import com.lovecube.backend.growth.enums.SettleStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class GrowthRuleCatalog {
    private final Map<GrowthEventType, GrowthRule> rules = new EnumMap<>(GrowthEventType.class);

    public GrowthRuleCatalog() {
        register(GrowthEventType.USER_REGISTERED, 5, ContributionDimension.SPREAD, 1, false, false, SettleStatus.SETTLED, "immediate");
        register(GrowthEventType.USER_PROFILE_COMPLETED, 10, ContributionDimension.CONTENT, 1, true, true, SettleStatus.SETTLED, "immediate");
        register(GrowthEventType.USER_DAILY_ACTIVE, 2, ContributionDimension.ORG, 1, false, false, SettleStatus.SETTLED, "immediate");
        register(GrowthEventType.POST_CREATED, 5, ContributionDimension.CONTENT, 3, false, false, SettleStatus.SETTLED, "immediate");
        register(GrowthEventType.POST_LIKED, 1, ContributionDimension.CONTENT, 3, false, false, SettleStatus.SETTLED, "immediate");
        register(GrowthEventType.POST_COMMENTED, 2, ContributionDimension.CONTENT, 10, false, false, SettleStatus.SETTLED, "immediate");
        register(GrowthEventType.GROUP_CREATED, 20, ContributionDimension.ORG, 1, true, true, SettleStatus.SETTLED, "immediate");
        register(GrowthEventType.GROUP_JOINED, 5, ContributionDimension.ORG, 2, false, false, SettleStatus.SETTLED, "immediate");
        register(GrowthEventType.USER_INVITED_REGISTERED, 10, ContributionDimension.SPREAD, 5, false, false, SettleStatus.SETTLED, "immediate");
        register(GrowthEventType.USER_INVITED_EFFECTIVE, 30, ContributionDimension.SPREAD, 3, true, true, SettleStatus.PENDING, "deferred");
    }

    public GrowthRule getRule(GrowthEventType eventType) {
        GrowthRule rule = rules.get(eventType);
        if (rule == null) {
            throw new IllegalArgumentException("Unsupported growth event type: " + eventType);
        }
        return rule;
    }

    private void register(
            GrowthEventType eventType,
            int deltaBase,
            ContributionDimension dimension,
            int dailyLimit,
            boolean broadcast,
            boolean achievementEnabled,
            SettleStatus defaultSettleStatus,
            String settleMode
    ) {
        rules.put(eventType, new GrowthRule(
                eventType,
                deltaBase,
                dimension,
                dailyLimit,
                broadcast,
                achievementEnabled,
                defaultSettleStatus,
                settleMode
        ));
    }

    public record GrowthRule(
            GrowthEventType eventType,
            int deltaBase,
            ContributionDimension dimension,
            int dailyLimit,
            boolean broadcast,
            boolean achievementEnabled,
            SettleStatus defaultSettleStatus,
            String settleMode
    ) {
    }
}
