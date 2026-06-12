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
        name = "dating_mutual_match_job_log",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_dating_mutual_match_job",
                        columnNames = {"event_id", "job_type"}
                )
        }
)
public class DatingMutualMatchJobLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, length = 64)
    private String eventId;

    @Column(name = "job_type", nullable = false, length = 64)
    private String jobType;

    @Column(name = "executed_at", nullable = false)
    private LocalDateTime executedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (executedAt == null) {
            executedAt = now;
        }
        if (createdAt == null) {
            createdAt = now;
        }
    }
}
