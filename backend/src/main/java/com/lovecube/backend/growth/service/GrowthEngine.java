package com.lovecube.backend.growth.service;

import com.lovecube.backend.growth.entity.ContributionLog;
import com.lovecube.backend.growth.entity.GrowthEvent;
import com.lovecube.backend.growth.enums.GrowthEventType;
import com.lovecube.backend.growth.enums.SettleStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GrowthEngine {
    private final GrowthRuleCatalog growthRuleCatalog;
    private final ContributionService contributionService;
    private final UserGrowthService userGrowthService;
    private final GrowthBroadcastService growthBroadcastService;

    public GrowthEngine(
            GrowthRuleCatalog growthRuleCatalog,
            ContributionService contributionService,
            UserGrowthService userGrowthService,
            GrowthBroadcastService growthBroadcastService
    ) {
        this.growthRuleCatalog = growthRuleCatalog;
        this.contributionService = contributionService;
        this.userGrowthService = userGrowthService;
        this.growthBroadcastService = growthBroadcastService;
    }

    @Transactional
    public void consume(GrowthEvent event) {
        if (!SettleStatus.SETTLED.name().equals(event.getSettleStatus())) {
            return;
        }

        GrowthRuleCatalog.GrowthRule rule = growthRuleCatalog.getRule(
                com.lovecube.backend.growth.enums.GrowthEventType.valueOf(event.getEventType())
        );
        GrowthEventType eventType = GrowthEventType.valueOf(event.getEventType());
        ContributionLog contributionLog = contributionService.createActorContribution(event, rule);
        if (contributionLog == null) {
            return;
        }
        UserGrowthService.GrowthApplyResult applyResult = userGrowthService.applyContribution(contributionLog);
        triggerMilestoneBroadcast(event, eventType, applyResult);
    }

    @Transactional
    public void consumeForTesting(GrowthEvent event) {
        consume(event);
    }

    private void triggerMilestoneBroadcast(
            GrowthEvent event,
            GrowthEventType eventType,
            UserGrowthService.GrowthApplyResult applyResult
    ) {
        try {
            if (eventType == GrowthEventType.GROUP_CREATED) {
                growthBroadcastService.broadcastGroupCreated(event);
            }
            if (applyResult.leveledUp()) {
                growthBroadcastService.broadcastLevelUp(event, applyResult.userGrowth());
            }
        } catch (Exception ignored) {
            // Broadcast failure must not affect growth bookkeeping.
        }
    }
}
