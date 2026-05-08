package com.lovecube.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "group_engagement_task_progress")
public class GroupEngagementTaskProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_external_id", nullable = false, length = 128)
    private String groupExternalId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "task_code", nullable = false, length = 32)
    private String taskCode;

    @Column(name = "task_date", nullable = false)
    private LocalDate taskDate;

    @Column(nullable = false)
    private Integer completed = 0;

    @Column(nullable = false)
    private Integer claimed = 0;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "claimed_at")
    private LocalDateTime claimedAt;
}
