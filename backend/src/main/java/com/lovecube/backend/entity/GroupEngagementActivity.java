package com.lovecube.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "group_engagement_activity")
public class GroupEngagementActivity {

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

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(length = 200)
    private String location;

    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants = 0;

    @Column(nullable = false, length = 20)
    private String status = "published";

    @Column(name = "participant_count", nullable = false)
    private Integer participantCount = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
