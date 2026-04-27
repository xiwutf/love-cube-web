package com.lovecube.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(
        name = "home_configs",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_home_config_group_key", columnNames = {"config_group", "config_key"})
        }
)
public class HomeConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_key", nullable = false, length = 100)
    private String configKey;

    @Lob
    @Column(name = "config_value", nullable = false)
    private String configValue;

    @Column(name = "config_group", nullable = false, length = 50)
    private String configGroup;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (enabled == null) enabled = true;
        if (sortOrder == null) sortOrder = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
