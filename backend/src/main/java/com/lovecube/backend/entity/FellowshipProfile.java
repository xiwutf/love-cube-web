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

    @Column(name = "identity_role", length = 32)
    private String identityRole; // self | guardian_son | guardian_daughter

    @Column(name = "guardian_role", length = 32)
    private String guardianRole; // father | mother | family

    @Column(name = "child_gender", length = 16)
    private String childGender;

    @Column(name = "child_age")
    private Integer childAge;

    @Column(name = "child_height")
    private Integer childHeight;

    @Column(name = "child_education", length = 64)
    private String childEducation;

    @Column(name = "child_job", length = 64)
    private String childJob;

    @Column(name = "child_city", length = 64)
    private String childCity;

    @Column(name = "child_house_car_status", length = 128)
    private String childHouseCarStatus;

    @Column(name = "child_marriage_intention", length = 500)
    private String childMarriageIntention;

    @Column(name = "child_partner_requirements", columnDefinition = "TEXT")
    private String childPartnerRequirements;

    @Column(name = "guardian_contact_visible")
    private Boolean guardianContactVisible;

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
        if (this.identityRole == null || this.identityRole.isBlank()) {
            this.identityRole = "self";
        }
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
