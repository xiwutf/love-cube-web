package com.lovecube.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "platform_group")
public class PlatGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String slug;

    @Column(name = "cover_url", length = 512)
    private String coverUrl;

    @Column(nullable = false, length = 30)
    private String type;

    @Column(length = 50)
    private String region;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String tags;

    @Column(name = "owner_user_id")
    private Long ownerUserId;

    @Column(name = "member_count", nullable = false)
    private Integer memberCount;

    @Column(name = "post_count", nullable = false)
    private Integer postCount = 0;

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "join_mode", nullable = false, length = 20)
    private String joinMode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
