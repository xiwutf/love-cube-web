package com.lovecube.backend.growth.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity(name = "GrowthUserGrowth")
@Table(name = "user_growth")
public class UserGrowth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "level", nullable = false)
    private Integer level;

    @Column(name = "stage", nullable = false, length = 32)
    private String stage;

    @Column(name = "total_contribution", nullable = false)
    private Integer totalContribution;

    @Column(name = "contrib_content", nullable = false)
    private Integer contribContent;

    @Column(name = "contrib_org", nullable = false)
    private Integer contribOrg;

    @Column(name = "contrib_spread", nullable = false)
    private Integer contribSpread;

    @Column(name = "contrib_city", nullable = false)
    private Integer contribCity;

    @Column(name = "rule_version", nullable = false, length = 32)
    private String ruleVersion;

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
        if (level == null) {
            level = 1;
        }
        if (stage == null || stage.isBlank()) {
            stage = "normal";
        }
        if (totalContribution == null) {
            totalContribution = 0;
        }
        if (contribContent == null) {
            contribContent = 0;
        }
        if (contribOrg == null) {
            contribOrg = 0;
        }
        if (contribSpread == null) {
            contribSpread = 0;
        }
        if (contribCity == null) {
            contribCity = 0;
        }
        if (ruleVersion == null || ruleVersion.isBlank()) {
            ruleVersion = "v1";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
