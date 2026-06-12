package com.lovecube.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(
        name = "event_signups",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_event_signups_event_user", columnNames = {"event_id", "user_id"}),
                @UniqueConstraint(name = "uk_event_signups_event_guest", columnNames = {"event_id", "guest_participant_id"})
        }
)
public class EventSignup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, length = 64)
    private String eventId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "guest_participant_id")
    private Long guestParticipantId;

    @Column(name = "checked_in", nullable = false)
    private Boolean checkedIn;

    @Column(name = "checked_in_at")
    private LocalDateTime checkedInAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (checkedIn == null) {
            checkedIn = false;
        }
    }
}
