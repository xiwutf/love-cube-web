package com.lovecube.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(
        name = "user_growth_logs",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_action_biz", columnNames = {"user_id", "action_type", "biz_id"})
        }
)
public class UserGrowthLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType;

    @Column(name = "biz_id", nullable = false, length = 100)
    private String bizId;

    @Column(nullable = false)
    private Integer exp;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
