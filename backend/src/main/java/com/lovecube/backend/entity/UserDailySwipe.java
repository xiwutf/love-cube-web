package com.lovecube.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_daily_swipe")
public class UserDailySwipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "swipe_date", nullable = false)
    private LocalDate swipeDate;

    @Column(name = "swipe_count", nullable = false)
    private Integer swipeCount;

    @Column(name = "rewind_count", nullable = false)
    private Integer rewindCount = 0;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void touch() {
        updatedAt = LocalDateTime.now();
        if (swipeCount == null) {
            swipeCount = 0;
        }
        if (rewindCount == null) {
            rewindCount = 0;
        }
    }
}
