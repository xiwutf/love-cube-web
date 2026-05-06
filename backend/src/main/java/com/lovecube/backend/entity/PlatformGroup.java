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

    /**
     * 创建者用户 ID（历史字段，与 {@link #ownerUserId} 在业务上常一致）。
     * 对外「拥有者」语义请以 {@link #ownerUserId} 为准；若为空可兼容回退本字段。
     */
    @Deprecated
    @Column(name = "created_by")
    private Long createdBy;

    /** 团体拥有者（API 主字段 ownerUserId 的数据来源） */
    @Column(name = "owner_user_id")
    private Long ownerUserId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
