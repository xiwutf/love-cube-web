package com.lovecube.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_notifications")
public class UserNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "type", length = 64, nullable = false)
    private String type;

    @Column(name = "level", length = 32, nullable = false)
    private String level;

    @Column(name = "title", length = 256, nullable = false)
    private String title;

    @Column(name = "content", length = 1000, nullable = false)
    private String content;

    @Column(name = "link_url", length = 512)
    private String linkUrl;

    @Column(name = "related_id", length = 128)
    private String relatedId;

    @Column(name = "related_type", length = 64)
    private String relatedType;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "push_channel", length = 32, nullable = false)
    private String pushChannel = "SITE";

    @Column(name = "push_status", length = 32, nullable = false)
    private String pushStatus = "SKIPPED";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isRead == null) isRead = false;
        if (pushChannel == null) pushChannel = "SITE";
        if (pushStatus == null) pushStatus = "SKIPPED";
    }
}
