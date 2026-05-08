package com.lovecube.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "group_engagement_checkin")
public class GroupEngagementCheckin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_external_id", nullable = false, length = 128)
    private String groupExternalId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "checkin_date", nullable = false)
    private LocalDate checkinDate;

    @Column(name = "checkin_type", nullable = false, length = 32)
    private String checkinType;

    @Column(length = 500)
    private String content;

    @Column(name = "streak_days", nullable = false)
    private Integer streakDays = 1;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
