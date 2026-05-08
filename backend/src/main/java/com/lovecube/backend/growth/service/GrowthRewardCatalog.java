package com.lovecube.backend.growth.service;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class GrowthRewardCatalog {

    public enum RewardType { BADGE, TITLE, CANDIDATE }

    public record LevelReward(
            String rewardCode, String rewardName, RewardType rewardType,
            String description, String displayText, int unlockLevel
    ) {}

    public record InviteMilestoneReward(
            String rewardCode, String rewardName, RewardType rewardType,
            String description, String displayText, int requiredCount
    ) {}

    public record RewardInfo(String rewardName, String rewardType, String description, String displayText) {}

    private static final List<LevelReward> LEVEL_REWARDS = List.of(
            new LevelReward("LEVEL_2_TITLE", "成长新星", RewardType.TITLE,
                    "完成初步成长，解锁新星称号", "解锁称号「成长新星」", 2),
            new LevelReward("LEVEL_3_BADGE", "社区活跃者", RewardType.BADGE,
                    "持续参与社区互动，解锁活跃者徽章", "解锁徽章「社区活跃者」", 3),
            new LevelReward("LEVEL_5_TITLE", "内容探索者", RewardType.TITLE,
                    "大量创作内容，解锁探索者称号", "解锁称号「内容探索者」", 5),
            new LevelReward("LEVEL_8_CANDIDATE", "团体候选者", RewardType.CANDIDATE,
                    "深度参与团体建设，成为团体候选人", "解锁身份「团体候选者」", 8),
            new LevelReward("LEVEL_10_CANDIDATE", "新星推广官候选", RewardType.CANDIDATE,
                    "成为平台核心推广力量", "解锁身份「新星推广官候选」", 10)
    );

    private static final List<InviteMilestoneReward> INVITE_REWARDS = List.of(
            new InviteMilestoneReward("INVITE_1_BADGE", "新人连接者", RewardType.BADGE,
                    "邀请首位新用户加入平台", "解锁徽章「新人连接者」", 1),
            new InviteMilestoneReward("INVITE_3_BADGE", "关系搭建者", RewardType.BADGE,
                    "帮助3位新用户加入平台", "解锁徽章「关系搭建者」", 3),
            new InviteMilestoneReward("INVITE_5_BADGE", "人气推荐者", RewardType.BADGE,
                    "帮助5位新用户加入平台", "解锁徽章「人气推荐者」", 5),
            new InviteMilestoneReward("INVITE_10_CANDIDATE", "推广官候选", RewardType.CANDIDATE,
                    "帮助10位新用户加入平台", "解锁身份「推广官候选」", 10)
    );

    public List<LevelReward> getLevelRewards() { return LEVEL_REWARDS; }
    public List<InviteMilestoneReward> getInviteRewards() { return INVITE_REWARDS; }

    public Optional<LevelReward> getRewardForLevel(int level) {
        return LEVEL_REWARDS.stream().filter(r -> r.unlockLevel() == level).findFirst();
    }

    public List<InviteMilestoneReward> getInviteRewardsUpTo(int count) {
        return INVITE_REWARDS.stream().filter(r -> r.requiredCount() <= count).toList();
    }

    public Optional<InviteMilestoneReward> getNextInviteReward(int currentCount) {
        return INVITE_REWARDS.stream().filter(r -> r.requiredCount() > currentCount).findFirst();
    }

    public Optional<RewardInfo> getByCode(String code) {
        Optional<LevelReward> lr = LEVEL_REWARDS.stream()
                .filter(r -> r.rewardCode().equals(code)).findFirst();
        if (lr.isPresent()) {
            LevelReward r = lr.get();
            return Optional.of(new RewardInfo(r.rewardName(), r.rewardType().name(), r.description(), r.displayText()));
        }
        return INVITE_REWARDS.stream()
                .filter(r -> r.rewardCode().equals(code))
                .map(r -> new RewardInfo(r.rewardName(), r.rewardType().name(), r.description(), r.displayText()))
                .findFirst();
    }

    /** Highest-level TITLE the user has earned, or empty. */
    public Optional<String> resolveCurrentTitle(Set<String> earnedCodes) {
        return LEVEL_REWARDS.stream()
                .filter(r -> r.rewardType() == RewardType.TITLE && earnedCodes.contains(r.rewardCode()))
                .max(Comparator.comparingInt(LevelReward::unlockLevel))
                .map(LevelReward::rewardName);
    }

    /** Highest-ranking BADGE the user has earned (invite badges take priority). */
    public Optional<String> resolveCurrentBadge(Set<String> earnedCodes) {
        Optional<String> inviteBadge = INVITE_REWARDS.stream()
                .filter(r -> r.rewardType() == RewardType.BADGE && earnedCodes.contains(r.rewardCode()))
                .max(Comparator.comparingInt(InviteMilestoneReward::requiredCount))
                .map(InviteMilestoneReward::rewardName);
        if (inviteBadge.isPresent()) return inviteBadge;
        return LEVEL_REWARDS.stream()
                .filter(r -> r.rewardType() == RewardType.BADGE && earnedCodes.contains(r.rewardCode()))
                .max(Comparator.comparingInt(LevelReward::unlockLevel))
                .map(LevelReward::rewardName);
    }
}
