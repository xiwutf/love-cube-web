package com.lovecube.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "growth_campaign_delivery")
public class GrowthCampaignDelivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "campaign_id", nullable = false)
    private Long campaignId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 32)
    private String channel;

    @Column(name = "template_code", nullable = false, length = 64)
    private String templateCode;

    @Column(name = "action_url", length = 512)
    private String actionUrl;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "opened_at")
    private LocalDateTime openedAt;

    @Column(name = "clicked_at")
    private LocalDateTime clickedAt;

    @Column(name = "clicked_count", nullable = false)
    private Integer clickedCount = 0;

    @Column(name = "last_clicked_at")
    private LocalDateTime lastClickedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "completion_snapshot_before", columnDefinition = "TEXT")
    private String completionSnapshotBefore;

    @Column(name = "completion_snapshot_after", columnDefinition = "TEXT")
    private String completionSnapshotAfter;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "notification_id")
    private Long notificationId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
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
