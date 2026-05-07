package com.lovecube.backend.growth.service;

import com.lovecube.backend.growth.entity.ContributionLog;
import com.lovecube.backend.growth.entity.UserGrowth;
import com.lovecube.backend.growth.enums.ContributionDimension;
import com.lovecube.backend.growth.repository.GrowthUserGrowthRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserGrowthService {
    private final GrowthUserGrowthRepository userGrowthRepository;

    public UserGrowthService(GrowthUserGrowthRepository userGrowthRepository) {
        this.userGrowthRepository = userGrowthRepository;
    }

    @Transactional
    public GrowthApplyResult applyContribution(ContributionLog log) {
        UserGrowth growth = userGrowthRepository.findByUserId(log.getUserId())
                .orElseGet(() -> initialize(log.getUserId(), log.getRuleVersion()));
        int oldLevel = safe(growth.getLevel());

        int delta = log.getDeltaFinal() == null ? 0 : log.getDeltaFinal();
        growth.setTotalContribution(safe(growth.getTotalContribution()) + delta);

        ContributionDimension dimension = ContributionDimension.valueOf(log.getDimension());
        switch (dimension) {
            case CONTENT -> growth.setContribContent(safe(growth.getContribContent()) + delta);
            case ORG -> growth.setContribOrg(safe(growth.getContribOrg()) + delta);
            case SPREAD -> growth.setContribSpread(safe(growth.getContribSpread()) + delta);
            case CITY -> growth.setContribCity(safe(growth.getContribCity()) + delta);
        }

        growth.setLevel(resolveLevel(safe(growth.getTotalContribution())));
        growth.setRuleVersion(log.getRuleVersion());
        UserGrowth saved = userGrowthRepository.save(growth);
        return new GrowthApplyResult(saved, saved.getLevel() > oldLevel);
    }

    private UserGrowth initialize(Long userId, String ruleVersion) {
        UserGrowth growth = new UserGrowth();
        growth.setUserId(userId);
        growth.setLevel(1);
        growth.setStage("normal");
        growth.setTotalContribution(0);
        growth.setContribContent(0);
        growth.setContribOrg(0);
        growth.setContribSpread(0);
        growth.setContribCity(0);
        growth.setRuleVersion(ruleVersion == null ? "v1" : ruleVersion);
        return userGrowthRepository.save(growth);
    }

    private int safe(Integer value) {
        return value == null ? 0 : value;
    }

    private int resolveLevel(int totalContribution) {
        if (totalContribution >= 500) {
            return 5;
        }
        if (totalContribution >= 300) {
            return 4;
        }
        if (totalContribution >= 150) {
            return 3;
        }
        if (totalContribution >= 50) {
            return 2;
        }
        return 1;
    }

    public record GrowthApplyResult(UserGrowth userGrowth, boolean leveledUp) {
    }
}
