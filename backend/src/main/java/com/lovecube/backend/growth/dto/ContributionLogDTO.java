package com.lovecube.backend.growth.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ContributionLogDTO {
    private Long id;
    private Long userId;
    private String eventType;
    private String dimension;
    private Integer deltaFinal;
    private LocalDateTime occurredAt;
}
