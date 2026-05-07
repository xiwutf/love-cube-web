package com.lovecube.backend.growth.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GrowthEventDTO {
    private Long id;
    private String eventType;
    private Long actorUserId;
    private Long targetUserId;
    private String settleStatus;
    private String sourcePlatform;
    private String bizRefType;
    private String bizRefId;
    private LocalDateTime occurredAt;
}
