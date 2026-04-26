package com.lovecube.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_verifications")
public class UserVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "real_name", length = 64)
    private String realName;

    @Column(name = "id_number", length = 64)
    private String idNumber;

    @Column(name = "id_front_url", length = 512)
    private String idFrontUrl;

    @Column(name = "id_back_url", length = 512)
    private String idBackUrl;

    @Column(name = "selfie_url", length = 512)
    private String selfieUrl;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "verify_type", length = 32)
    private String verifyType;

    @Column(name = "submit_data", columnDefinition = "TEXT")
    private String submitData;

    @Column(name = "reviewer_id")
    private Long reviewerId;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.submittedAt == null) {
            this.submittedAt = now;
        }
        if (this.status == null || this.status.isBlank()) {
            this.status = "pending";
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
