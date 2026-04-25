package com.lovecube.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "reports")
public class ReportRecord {
    @Id
    private String id;

    @Column(name = "reporter_id")
    private Long reporterId;

    @Column(name = "target_user_id")
    private Long targetUserId;

    @Column(name = "report_type")
    private String reportType;

    private String content;

    private String status;

    private String note;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
