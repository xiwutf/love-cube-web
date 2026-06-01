package com.lovecube.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "events")
public class PlatformEvent {
    @Id
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String location;

    @Column(name = "event_time")
    private LocalDateTime eventTime;

    @Column(name = "signup_count")
    private Integer signupCount;

    @Column(nullable = false)
    private String status;

    private String category;

    @Column(name = "cover_url", length = 512)
    private String coverUrl;

    private Boolean pinned;

    private Boolean recommended;

    @Column(name = "view_count")
    private Integer viewCount;

    @Column(name = "checkin_code", length = 16)
    private String checkinCode;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
