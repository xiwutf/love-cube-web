package com.lovecube.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "group_engagement_poll")
public class GroupEngagementPoll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_external_id", nullable = false, length = 128)
    private String groupExternalId;

    @Column(name = "creator_user_id", nullable = false)
    private Long creatorUserId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** single | multiple */
    @Column(name = "selection_mode", nullable = false, length = 20)
    private String selectionMode = "single";

    /** 多选时每人最多可选几项；单选固定为 1 */
    @Column(name = "max_choices", nullable = false)
    private Integer maxChoices = 1;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false, length = 20)
    private String status = "published";

    @Column(name = "results_public", nullable = false)
    private Boolean resultsPublic = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
