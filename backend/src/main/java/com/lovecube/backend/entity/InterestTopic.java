package com.lovecube.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "interest_topic")
public class InterestTopic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(name = "post_count", nullable = false)
    private Integer postCount;

    @Column(nullable = false)
    private Integer heat;

    @Column(name = "sort_no", nullable = false)
    private Integer sortNo;

    @Column(nullable = false)
    private Integer enabled;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (postCount == null) postCount = 0;
        if (heat == null) heat = 0;
        if (enabled == null) enabled = 1;
    }
}
