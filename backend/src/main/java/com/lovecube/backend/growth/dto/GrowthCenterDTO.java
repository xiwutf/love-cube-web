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
}
