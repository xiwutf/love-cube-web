package com.lovecube.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_login_streak")
public class UserLoginStreak {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "current_streak", nullable = false)
    private Integer currentStreak;

    @Column(name = "longest_streak", nullable = false)
    private Integer longestStreak;

    @Column(name = "last_login_date")
    private LocalDate lastLoginDate;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        updatedAt = LocalDateTime.now();
        if (currentStreak == null) currentStreak = 0;
        if (longestStreak == null) longestStreak = 0;
    }
}
