package com.lovecube.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(
        name = "user_badges",
        uniqueConstraints = {@UniqueConstraint(name = "uk_user_badge", columnNames = {"user_id", "badge_code"})}
)
public class UserBadge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "badge_code", nullable = false, length = 50)
    private String badgeCode;

    @Column(nullable = false)
    private Integer progress;

    @Column(nullable = false)
    private Integer unlocked;

    @Column(name = "unlocked_at")
    private LocalDateTime unlockedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (progress == null) {
            progress = 0;
        }
        if (unlocked == null) {
            unlocked = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
