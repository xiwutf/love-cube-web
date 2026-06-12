package com.lovecube.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(
        name = "dating_event_identity",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_dating_event_badge", columnNames = {"event_id", "badge_label"}),
                @UniqueConstraint(name = "uk_dating_identity_event_user", columnNames = {"event_id", "user_id"}),
                @UniqueConstraint(name = "uk_dating_identity_event_guest", columnNames = {"event_id", "guest_participant_id"})
        }
)
public class DatingEventIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "row_id")
    private Long id;

    @Column(name = "event_id", nullable = false, length = 64)
    private String eventId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "guest_participant_id")
    private Long guestParticipantId;

    @Column(name = "gender_side", nullable = false, length = 16)
    private String genderSide;

    @Column(name = "badge_seq", nullable = false)
    private Integer badgeSeq;

    @Column(name = "badge_label", nullable = false, length = 16)
    private String badgeLabel;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;
}
