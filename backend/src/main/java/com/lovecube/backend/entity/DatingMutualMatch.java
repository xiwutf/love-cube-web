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
        name = "dating_mutual_match",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_dating_mutual_match_pair",
                        columnNames = {
                                "event_id",
                                "participant_a_type",
                                "participant_a_id",
                                "participant_b_type",
                                "participant_b_id"
                        }
                )
        }
)
public class DatingMutualMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, length = 64)
    private String eventId;

    @Column(name = "participant_a_type", nullable = false, length = 16)
    private String participantAType;

    @Column(name = "participant_a_id", nullable = false)
    private Long participantAId;

    @Column(name = "participant_b_type", nullable = false, length = 16)
    private String participantBType;

    @Column(name = "participant_b_id", nullable = false)
    private Long participantBId;

    @Column(name = "matched_at", nullable = false)
    private LocalDateTime matchedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (matchedAt == null) {
            matchedAt = now;
        }
        if (createdAt == null) {
            createdAt = now;
        }
    }
}
