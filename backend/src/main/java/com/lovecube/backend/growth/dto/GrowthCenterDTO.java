package com.lovecube.backend.growth.dto;

import lombok.Data;

@Data
public class GrowthCenterDTO {
    private Long userId;
    private Integer level;
    private String stage;
    private Integer totalContribution;
    private Integer contribContent;
    private Integer contribOrg;
    private Integer contribSpread;
    private Integer contribCity;
    private Integer nextLevel;
    private Integer nextLevelThreshold;
    private Integer contributionToNextLevel;
    // P1-1 reward fields
    private String currentTitle;
    private String currentBadge;
    private String nextLevelRewardName;
    private String nextLevelRewardType;
    private String nextLevelRewardDisplayText;
    private Integer inviteMilestoneProgress;
    private String nextInviteRewardName;
    private String nextInviteRewardType;
    private String nextInviteRewardDisplayText;
    private Integer nextInviteRequiredCount;
}
