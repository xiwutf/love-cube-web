package com.lovecube.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "fellowship_profiles")
public class FellowshipProfileMain {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "nickname", length = 64)
    private String nickname;

    /**
     * fellowship_profiles.avatar 列；与 fellowship_profile.avatar_url 应对齐。
     * 请通过 UnifiedProfileService 合并读写；对外 API 主字段名为 avatarUrl。
     */
    @Deprecated
    @Column(name = "avatar", length = 255)
    private String avatar;

    @Column(name = "gender", length = 16)
    private String gender;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "city", length = 64)
    private String city;

    @Column(name = "occupation", length = 64)
    private String occupation;

    @Column(name = "height")
    private Integer height;

    @Column(name = "bio", length = 500)
    private String bio;

    @Column(name = "photos_json", columnDefinition = "TEXT")
    private String photosJson;

    @Column(name = "completion_rate")
    private Integer completionRate;

    @Column(name = "verification_status", length = 32)
    private String verificationStatus;

    @Column(name = "verification_note", length = 500)
    private String verificationNote;

    @Column(name = "review_status", length = 32)
    private String reviewStatus;

    @Column(name = "review_note", length = 500)
    private String reviewNote;

    @Column(name = "reported_count")
    private Integer reportedCount;

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.completionRate == null) {
            this.completionRate = 0;
        }
        if (this.verificationStatus == null || this.verificationStatus.isBlank()) {
            this.verificationStatus = "none";
        }
        if (this.reviewStatus == null || this.reviewStatus.isBlank()) {
            this.reviewStatus = "pending";
        }
        if (this.reportedCount == null) {
            this.reportedCount = 0;
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
