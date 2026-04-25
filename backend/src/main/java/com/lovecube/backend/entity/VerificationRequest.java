package com.lovecube.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "verification_requests")
public class VerificationRequest {
    @Id
    private String id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "real_name")
    private String realName;

    @Column(name = "id_number")
    private String idNumber;

    private String note;

    private String status;

    @Column(name = "reject_reason")
    private String rejectReason;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
}
