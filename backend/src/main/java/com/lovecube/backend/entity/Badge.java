package com.lovecube.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "badges")
public class Badge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(length = 255)
    private String icon;

    @Column(name = "badge_type", length = 30)
    private String badgeType;

    @Column(name = "condition_value")
    private Integer conditionValue;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
