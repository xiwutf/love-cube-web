package com.lovecube.backend.growth.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserAchievementDTO {
    private Long id;
    private String achievementCode;
    private Integer level;
    private String status;
    private LocalDateTime grantedAt;
    // P1-1 enriched fields (resolved from catalog)
    private String achievementName;
    private String achievementType;
    private String description;
    private String displayText;
}
