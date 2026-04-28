package com.lovecube.backend.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userid;

    @Column(name = "username")
    private String username;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "openid")
    private String openid;

    @Column(name = "profile_photo")
    private String profilePhoto;

    @Column(name = "bio")
    private String bio;

    @Column(name = "age")
    private Integer age;

    @Column(name = "gender")
    private Integer gender;

    @Column(name = "location")
    private String location;

    @Column(name = "occupation")
    private String occupation;

    @Column(name = "height")
    private Integer height;

    @Column(name = "birth_date")
    private LocalDateTime birthDate;

    @Column(name = "photos", columnDefinition = "TEXT")
    private String photos;

    @Column(name = "vip_status")
    private String vipStatus;

    @Column(name = "vip_expires_at")
    private LocalDateTime vipExpiresAt;

    @Column(name = "invite_code", unique = true, length = 32)
    private String inviteCode;

    @Column(name = "invited_by_user_id")
    private Long invitedByUserId;

    @Column(name = "register_ip", length = 64)
    private String registerIp;

    @Column(name = "register_user_agent", length = 500)
    private String registerUserAgent;

    @Column(name = "user_status", length = 32)
    private String userStatus;

    @Column(name = "role", length = 32)
    private String role;

    @Column(name = "invite_code_status", length = 32)
    private String inviteCodeStatus;

    @Column(name = "fellowship_enabled")
    private Boolean fellowshipEnabled;

    @Column(name = "fellowship_match_visible")
    private Boolean fellowshipMatchVisible;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (userStatus == null || userStatus.isBlank()) {
            userStatus = "NORMAL";
        }
        if (role == null || role.isBlank()) {
            role = "USER";
        }
        if (inviteCodeStatus == null || inviteCodeStatus.isBlank()) {
            inviteCodeStatus = "ENABLED";
        }
        if (fellowshipEnabled == null) {
            fellowshipEnabled = false;
        }
        if (fellowshipMatchVisible == null) {
            fellowshipMatchVisible = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
