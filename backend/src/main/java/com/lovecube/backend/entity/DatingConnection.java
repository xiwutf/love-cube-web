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
        name = "dating_connection",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_dating_connection_pair",
                        columnNames = {
                                "event_id",
                                "participant_type",
                                "participant_id",
                                "target_participant_type",
                                "target_participant_id"
                        }
                )
        }
)
public class DatingConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, length = 64)
    private String eventId;

    @Column(name = "participant_type", nullable = false, length = 16)
    private String participantType;

    @Column(name = "participant_id", nullable = false)
    private Long participantId;

    @Column(name = "target_participant_type", nullable = false, length = 16)
    private String targetParticipantType;

    @Column(name = "target_participant_id", nullable = false)
    private Long targetParticipantId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
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
