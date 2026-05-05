package com.lovecube.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "local_resource")
public class LocalResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 30)
    private String type;

    @Column(length = 100)
    private String location;

    @Column(name = "event_time")
    private LocalDateTime eventTime;

    @Column(length = 500)
    private String summary;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(nullable = false)
    private Integer heat;

    @Column(name = "interest_count", nullable = false)
    private Integer interestCount;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
        if (heat == null) heat = 0;
        if (interestCount == null) interestCount = 0;
        if (status == null || status.isBlank()) status = "draft";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
