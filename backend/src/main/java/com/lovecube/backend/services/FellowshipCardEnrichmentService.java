package com.lovecube.backend.services;

import com.lovecube.backend.models.User;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/** 联谊用户卡片/资料：成长等级 + 徽章展示字段。 */
@Service
public class FellowshipCardEnrichmentService {
    private final FellowshipGrowthSummaryService fellowshipGrowthSummaryService;
    private final FellowshipUserBadgeService fellowshipUserBadgeService;

    public FellowshipCardEnrichmentService(
            FellowshipGrowthSummaryService fellowshipGrowthSummaryService,
            FellowshipUserBadgeService fellowshipUserBadgeService
    ) {
        this.fellowshipGrowthSummaryService = fellowshipGrowthSummaryService;
        this.fellowshipUserBadgeService = fellowshipUserBadgeService;
    }

    public void enrichUserCard(
            Map<String, Object> card,
            User user,
            Map<String, Boolean> verifyBadges,
            int completionRate
    ) {
        if (card == null || user == null) {
            return;
        }
        card.put("profileCompletionRate", completionRate);
        card.put("completionRate", completionRate);
        card.putAll(fellowshipGrowthSummaryService.buildCardGrowthFields(user.getUserid()));

        Map<String, Boolean> badges = verifyBadges != null ? verifyBadges : Map.of();
        Map<String, Object> badgePayload = fellowshipUserBadgeService.buildCardBadgePayload(user, badges, completionRate);
        card.put("verifiedBadges", badgePayload.get("verifiedBadges"));
        card.put("badges", badgePayload.get("badges"));
    }
}
