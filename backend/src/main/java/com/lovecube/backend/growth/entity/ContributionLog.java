package com.lovecube.backend.growth.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "contribution_log")
public class ContributionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @Column(name = "dimension", nullable = false, length = 16)
    private String dimension;

    @Column(name = "delta_base", nullable = false)
    private Integer deltaBase;

    @Column(name = "delta_final", nullable = false)
    private Integer deltaFinal;

    @Column(name = "weight_json", columnDefinition = "json")
    private String weightJson;

    @Column(name = "rule_version", nullable = false, length = 32)
    private String ruleVersion;

    @Column(name = "source_platform", nullable = false, length = 32)
    private String sourcePlatform;

    @Column(name = "settle_status", nullable = false, length = 16)
    private String settleStatus;

    @Column(name = "dedupe_key", nullable = false, unique = true, length = 128)
    private String dedupeKey;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
