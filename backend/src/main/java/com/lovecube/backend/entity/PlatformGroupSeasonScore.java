package com.lovecube.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "platform_group_season_score")
public class PlatformGroupSeasonScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "season_id", nullable = false)
    private Long seasonId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(nullable = false)
    private Integer score;

    @Column(name = "checkin_count", nullable = false)
    private Integer checkinCount;

    @Column(name = "task_count", nullable = false)
    private Integer taskCount;

    @Column(name = "activity_count", nullable = false)
    private Integer activityCount;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        updatedAt = LocalDateTime.now();
        if (score == null) score = 0;
        if (checkinCount == null) checkinCount = 0;
        if (taskCount == null) taskCount = 0;
        if (activityCount == null) activityCount = 0;
    }
}
