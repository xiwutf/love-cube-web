package com.lovecube.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "platform_groups")
public class PlatformGroup {
    @Id
    private String id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_url", length = 512)
    private String coverUrl;

    private String category;

    @Column(nullable = false)
    private String status;

    @Column(name = "join_type", nullable = false)
    private String joinType;

    @Column(name = "member_count")
    private Integer memberCount;

    private Boolean pinned;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
