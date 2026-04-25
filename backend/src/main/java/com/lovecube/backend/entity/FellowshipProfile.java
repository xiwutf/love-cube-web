package com.lovecube.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "fellowship_profile")
public class FellowshipProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "nickname", length = 64)
    private String nickname;

    @Column(name = "gender", length = 16)
    private String gender;

    @Column(name = "birth_year")
    private Integer birthYear;

    @Column(name = "age")
    private Integer age;

    @Column(name = "city", length = 64)
    private String city;

    @Column(name = "occupation", length = 64)
    private String occupation;

    @Column(name = "education", length = 64)
    private String education;

    @Column(name = "height")
    private Integer height;

    @Column(name = "bio", length = 500)
    private String bio;

    @Column(name = "intention", length = 500)
    private String intention;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;

    @Column(name = "profile_status", nullable = false, length = 16)
    private String profileStatus;

    @Column(name = "review_status", nullable = false, length = 16)
    private String reviewStatus;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.profileStatus == null || this.profileStatus.isBlank()) {
            this.profileStatus = "INCOMPLETE";
        }
        if (this.reviewStatus == null || this.reviewStatus.isBlank()) {
            this.reviewStatus = "PENDING";
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
