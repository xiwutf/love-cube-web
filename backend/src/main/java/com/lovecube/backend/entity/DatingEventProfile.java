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
        name = "dating_event_profile",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_dating_profile_event_user", columnNames = {"event_id", "user_id"}),
                @UniqueConstraint(name = "uk_dating_profile_event_guest", columnNames = {"event_id", "guest_participant_id"})
        }
)
public class DatingEventProfile {

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

    private Integer age;

    @Column(name = "height_cm")
    private Integer heightCm;

    @Column(length = 64)
    private String city;

    @Column(length = 128)
    private String occupation;

    @Column(length = 64)
    private String education;

    @Column(name = "interest_tags", columnDefinition = "TEXT")
    private String interestTags;

    @Column(name = "self_intro", columnDefinition = "TEXT")
    private String selfIntro;

    @Column(name = "partner_requirements", columnDefinition = "TEXT")
    private String partnerRequirements;

    @Column(name = "ideal_partner_desc", columnDefinition = "TEXT")
    private String idealPartnerDesc;

    @Column(nullable = false)
    private Boolean completed = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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
