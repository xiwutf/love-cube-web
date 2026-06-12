package com.lovecube.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "activity_reminder_log")
public class ActivityReminderLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "activity_source", nullable = false, length = 32)
    private String activitySource;

    @Column(name = "activity_id", nullable = false, length = 64)
    private String activityId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "reminder_type", nullable = false, length = 32)
    private String reminderType;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
