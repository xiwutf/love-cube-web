package com.lovecube.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(
        name = "event_guest_participant",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_event_guest_token",
                columnNames = {"event_id", "guest_token"}
        )
)
public class EventGuestParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, length = 64)
    private String eventId;

    @Column(name = "guest_token", nullable = false, length = 64)
    private String guestToken;

    @Column(nullable = false, length = 64)
    private String nickname;

    @Column(name = "gender_side", nullable = false, length = 16)
    private String genderSide;

    @Column(name = "mobile_hash", length = 64)
    private String mobileHash;

    @Column(name = "mobile_last4", length = 4)
    private String mobileLast4;

    @Column(name = "registered_user_id")
    private Long registeredUserId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

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
