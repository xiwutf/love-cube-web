package com.lovecube.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "user_weekly_task_progress")
public class UserWeeklyTaskProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "task_code", nullable = false, length = 50)
    private String taskCode;

    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    @Column(nullable = false)
    private Integer progress;

    @Column(nullable = false)
    private Integer completed;

    @Column(nullable = false)
    private Integer claimed;
}
