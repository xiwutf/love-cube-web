package com.lovecube.backend.growth.dto;

import com.lovecube.backend.growth.enums.GrowthEventType;
import com.lovecube.backend.growth.enums.SourcePlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GrowthEventCreateRequest {
    @NotNull
    private GrowthEventType eventType;

    @NotNull
    private Long actorUserId;

    private Long targetUserId;

    private String bizRefType;

    private String bizRefId;

    @NotBlank
    private String dedupeKey;

    private String ruleVersion;

    @NotNull
    private SourcePlatform sourcePlatform;

    private LocalDateTime occurredAt;

    private String payloadJson;
}
