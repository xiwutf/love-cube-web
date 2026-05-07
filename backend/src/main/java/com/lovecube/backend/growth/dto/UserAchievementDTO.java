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
}
