package com.lovecube.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "platform_group_weekly_digest_log")
public class PlatformGroupWeeklyDigestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "week_key", nullable = false, length = 16)
    private String weekKey;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;
}
