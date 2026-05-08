package com.lovecube.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "group_join_requests")
public class GroupJoinRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private String groupId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String status;

    @Column(length = 255)
    private String message;

    /** 提交申请时登记的团体内展示姓名（通过后写入 group_members.member_real_name） */
    @Column(name = "member_real_name", length = 64)
    private String memberRealName;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "handled_at")
    private LocalDateTime handledAt;
}
