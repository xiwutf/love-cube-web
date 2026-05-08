package com.lovecube.backend.growth.service;

import com.lovecube.backend.growth.entity.GrowthEvent;
import com.lovecube.backend.growth.entity.UserAchievement;
import com.lovecube.backend.growth.repository.UserAchievementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GrowthRewardService {

    private final GrowthRewardCatalog rewardCatalog;
    private final UserAchievementRepository userAchievementRepository;
    private final GrowthBroadcastService growthBroadcastService;

    public GrowthRewardService(
            GrowthRewardCatalog rewardCatalog,
            UserAchievementRepository userAchievementRepository,
            GrowthBroadcastService growthBroadcastService
    ) {
        this.rewardCatalog = rewardCatalog;
        this.userAchievementRepository = userAchievementRepository;
        this.growthBroadcastService = growthBroadcastService;
    }

    @Transactional
    public void checkAndGrantLevelReward(Long userId, int newLevel, GrowthEvent triggerEvent) {
        rewardCatalog.getRewardForLevel(newLevel).ifPresent(reward -> {
            if (userAchievementRepository.existsByUserIdAndAchievementCode(userId, reward.rewardCode())) {
                return;
            }
            userAchievementRepository.save(newAchievement(userId, reward.rewardCode(), newLevel,
                    triggerEvent != null ? triggerEvent.getId() : null));
            String typeLabel = rewardTypeLabel(reward.rewardType());
            String displayText = "🎉 恭喜你达到 Lv" + newLevel + "，获得「" + reward.rewardName() + "」" + typeLabel;
            growthBroadcastService.broadcastReward(userId, "LEVEL_REWARD_" + newLevel, displayText, triggerEvent);
        });
    }

    @Transactional
    public void checkAndGrantInviteMilestoneRewards(Long userId, int effectiveInviteCount) {
        List<GrowthRewardCatalog.InviteMilestoneReward> earned =
                rewardCatalog.getInviteRewardsUpTo(effectiveInviteCount);
        for (GrowthRewardCatalog.InviteMilestoneReward reward : earned) {
            if (userAchievementRepository.existsByUserIdAndAchievementCode(userId, reward.rewardCode())) {
                continue;
            }
            userAchievementRepository.save(newAchievement(userId, reward.rewardCode(), 0, null));
            String displayText = "🎉 你已成功帮助 " + reward.requiredCount()
                    + " 位新用户加入平台，解锁「" + reward.rewardName() + "」徽章";
            growthBroadcastService.broadcastReward(userId, reward.rewardCode(), displayText, null);
        }
    }

    private UserAchievement newAchievement(Long userId, String code, int level, Long eventId) {
        UserAchievement a = new UserAchievement();
        a.setUserId(userId);
        a.setAchievementCode(code);
        a.setLevel(level);
        a.setEventId(eventId);
        a.setRuleVersion("v1");
        a.setStatus("GRANTED");
        LocalDateTime now = LocalDateTime.now();
        a.setGrantedAt(now);
        a.setCreatedAt(now);
        return a;
    }

    private String rewardTypeLabel(GrowthRewardCatalog.RewardType type) {
        return switch (type) {
            case BADGE -> "徽章";
            case TITLE -> "称号";
            case CANDIDATE -> "候选身份";
        };
    }
}
