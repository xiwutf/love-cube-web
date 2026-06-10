package com.lovecube.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "group_activity_peer_review")
public class GroupActivityPeerReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "reviewer_user_id", nullable = false)
    private Long reviewerUserId;

    @Column(name = "target_user_id", nullable = false)
    private Long targetUserId;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 300)
    private String comment;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
