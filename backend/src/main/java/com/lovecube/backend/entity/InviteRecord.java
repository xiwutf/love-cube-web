package com.lovecube.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "invite_record")
public class InviteRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invite_code", nullable = false, length = 32)
    private String inviteCode;

    @Column(name = "inviter_user_id", nullable = false)
    private Long inviterUserId;

    @Column(name = "invitee_user_id")
    private Long inviteeUserId;

    @Column(name = "invitee_username", length = 128)
    private String inviteeUsername;

    @Column(name = "register_ip", length = 64)
    private String registerIp;

    @Column(name = "register_user_agent", length = 500)
    private String registerUserAgent;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null || status.isBlank()) {
            status = "SUCCESS";
        }
    }
}

