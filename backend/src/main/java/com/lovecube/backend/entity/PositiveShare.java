package com.lovecube.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "positive_share")
public class PositiveShare {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 32)
    private String category;

    @Column(name = "is_anonymous", nullable = false)
    private Boolean anonymous;

    @Column(name = "public_visible", nullable = false)
    private Boolean publicVisible;

    @Column(nullable = false, length = 16)
    private String status;

    @Column(name = "encourage_count", nullable = false)
    private Integer encourageCount;

    @Column(name = "comment_count", nullable = false)
    private Integer commentCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null || status.isBlank()) {
            status = "PUBLISHED";
        }
        if (anonymous == null) {
            anonymous = Boolean.FALSE;
        }
        if (publicVisible == null) {
            publicVisible = Boolean.TRUE;
        }
        if (encourageCount == null) {
            encourageCount = 0;
        }
        if (commentCount == null) {
            commentCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
