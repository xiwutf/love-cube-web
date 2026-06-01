package com.lovecube.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user_newcomer_task_progress")
public class UserNewcomerTaskProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "task_code", nullable = false, length = 50)
    private String taskCode;

    @Column(nullable = false)
    private Integer completed;

    @Column(nullable = false)
    private Integer claimed;
}
