package com.lovecube.backend.growth.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "growth_event")
public class GrowthEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @Column(name = "actor_user_id", nullable = false)
    private Long actorUserId;

    @Column(name = "target_user_id")
    private Long targetUserId;

    @Column(name = "biz_ref_type", length = 64)
    private String bizRefType;

    @Column(name = "biz_ref_id", length = 128)
    private String bizRefId;

    @Column(name = "rule_version", nullable = false, length = 32)
    private String ruleVersion;

    @Column(name = "source_platform", nullable = false, length = 32)
    private String sourcePlatform;

    @Column(name = "settle_status", nullable = false, length = 16)
    private String settleStatus;

    @Column(name = "dedupe_key", nullable = false, length = 128, unique = true)
    private String dedupeKey;

    @Column(name = "payload_json", columnDefinition = "json")
    private String payloadJson;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (occurredAt == null) {
            occurredAt = LocalDateTime.now();
        }
        if (ruleVersion == null || ruleVersion.isBlank()) {
            ruleVersion = "v1";
        }
    }
}
