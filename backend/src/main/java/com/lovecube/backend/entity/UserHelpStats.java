package com.lovecube.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_help_stats")
public class UserHelpStats {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "help_count", nullable = false)
    private Integer helpCount;

    @Column(name = "success_count", nullable = false)
    private Integer successCount;

    @Column(name = "accepted_count", nullable = false)
    private Integer acceptedCount;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        updatedAt = now;
        if (helpCount == null) {
            helpCount = 0;
        }
        if (successCount == null) {
            successCount = 0;
        }
        if (acceptedCount == null) {
            acceptedCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
