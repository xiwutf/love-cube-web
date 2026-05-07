package com.lovecube.backend.growth.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_achievement")
public class UserAchievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "achievement_code", nullable = false, length = 64)
    private String achievementCode;

    @Column(name = "level", nullable = false)
    private Integer level;

    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "rule_version", nullable = false, length = 32)
    private String ruleVersion;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "granted_at", nullable = false)
    private LocalDateTime grantedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
