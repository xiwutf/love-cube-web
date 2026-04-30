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
@Table(name = "announcements")
public class Announcement {
    @Id
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String status;

    @Column(name = "publish_date")
    private LocalDateTime publishDate;

    private String category;

    @Column(name = "cover_url", length = 512)
    private String coverUrl;

    @Column(name = "attachment_url", length = 512)
    private String attachmentUrl;

    private Boolean pinned;

    private Boolean recommended;

    @Column(name = "popup_enabled")
    private Boolean popupEnabled;

    @Column(name = "view_count")
    private Integer viewCount;

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
