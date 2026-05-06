package com.lovecube.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_invite_relation")
public class UserInviteRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invite_code", nullable = false, length = 32)
    private String inviteCode;

    @Column(name = "inviter_user_id", nullable = false)
    private Long inviterUserId;

    @Column(name = "invited_user_id", nullable = false, unique = true)
    private Long invitedUserId;

    @Column(name = "register_ip", length = 64)
    private String registerIp;

    @Column(name = "register_user_agent", length = 500)
    private String registerUserAgent;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null || status.isBlank()) {
            status = "SUCCESS";
        }
    }
}
