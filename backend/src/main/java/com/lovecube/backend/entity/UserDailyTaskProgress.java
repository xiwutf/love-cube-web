package com.lovecube.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(
        name = "user_daily_task_progress",
        uniqueConstraints = {@UniqueConstraint(name = "uk_user_task_date", columnNames = {"user_id", "task_code", "task_date"})}
)
public class UserDailyTaskProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "task_code", nullable = false, length = 50)
    private String taskCode;

    @Column(name = "task_date", nullable = false)
    private LocalDate taskDate;

    @Column(nullable = false)
    private Integer progress;

    @Column(nullable = false)
    private Integer completed;

    @Column(nullable = false)
    private Integer claimed;

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
        if (completed == null) {
            completed = 0;
        }
        if (claimed == null) {
            claimed = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
