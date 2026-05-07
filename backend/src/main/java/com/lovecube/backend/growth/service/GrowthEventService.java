package com.lovecube.backend.growth.service;

import com.lovecube.backend.growth.dto.GrowthEventCreateRequest;
import com.lovecube.backend.growth.entity.GrowthEvent;
import com.lovecube.backend.growth.repository.GrowthEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class GrowthEventService {
    private final GrowthEventRepository growthEventRepository;
    private final GrowthRuleCatalog growthRuleCatalog;
    private final GrowthEngine growthEngine;

    public GrowthEventService(
            GrowthEventRepository growthEventRepository,
            GrowthRuleCatalog growthRuleCatalog,
            GrowthEngine growthEngine
    ) {
        this.growthEventRepository = growthEventRepository;
        this.growthRuleCatalog = growthRuleCatalog;
        this.growthEngine = growthEngine;
    }

    @Transactional
    public Map<String, Object> publish(GrowthEventCreateRequest request) {
        String dedupeKey = request.getDedupeKey() == null ? "" : request.getDedupeKey().trim();
        if (dedupeKey.isBlank()) {
            throw new IllegalArgumentException("dedupeKey is required");
        }

        GrowthEvent existing = growthEventRepository.findByDedupeKey(dedupeKey).orElse(null);
        if (existing != null) {
            return toResult(existing, true);
        }

        GrowthRuleCatalog.GrowthRule rule = growthRuleCatalog.getRule(request.getEventType());
        GrowthEvent event = new GrowthEvent();
        event.setEventType(request.getEventType().name());
        event.setActorUserId(request.getActorUserId());
        event.setTargetUserId(request.getTargetUserId());
        event.setBizRefType(trimToNull(request.getBizRefType()));
        event.setBizRefId(trimToNull(request.getBizRefId()));
        event.setRuleVersion(resolveRuleVersion(request.getRuleVersion()));
        event.setSourcePlatform(request.getSourcePlatform().name());
        event.setSettleStatus(rule.defaultSettleStatus().name());
        event.setDedupeKey(dedupeKey);
        event.setPayloadJson(trimToNull(request.getPayloadJson()));
        event.setOccurredAt(request.getOccurredAt() == null ? LocalDateTime.now() : request.getOccurredAt());

        GrowthEvent saved = growthEventRepository.save(event);
        growthEngine.consume(saved);
        return toResult(saved, false);
    }

    private Map<String, Object> toResult(GrowthEvent event, boolean duplicated) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("eventId", event.getId());
        result.put("eventType", event.getEventType());
        result.put("dedupeKey", event.getDedupeKey());
        result.put("settleStatus", event.getSettleStatus());
        result.put("duplicated", duplicated);
        return result;
    }

    private String resolveRuleVersion(String ruleVersion) {
        String value = trimToNull(ruleVersion);
        return value == null ? "v1" : value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
