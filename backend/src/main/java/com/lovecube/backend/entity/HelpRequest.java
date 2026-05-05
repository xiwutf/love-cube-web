package com.lovecube.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "help_request")
public class HelpRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "help_type", nullable = false, length = 32)
    private String helpType;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 100)
    private String region;

    @Column(name = "contact_type", length = 32)
    private String contactType;

    @Column(name = "contact_value", length = 200)
    private String contactValue;

    @Column(name = "contact_public", nullable = false)
    private Boolean contactPublic;

    private LocalDate deadline;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "helper_user_id")
    private Long helperUserId;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolved_note", length = 500)
    private String resolvedNote;

    @Column(name = "reply_count", nullable = false)
    private Integer replyCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null || status.isBlank()) {
            status = "pending";
        }
        if (contactPublic == null) {
            contactPublic = Boolean.FALSE;
        }
        if (replyCount == null) {
            replyCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
