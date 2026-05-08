package com.lovecube.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "group_members")
public class GroupMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private String groupId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String role;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    /** 仅在本团体内向其他成员展示，非全站昵称 / 用户名 */
    @Column(name = "member_real_name", length = 64)
    private String memberRealName;
}
