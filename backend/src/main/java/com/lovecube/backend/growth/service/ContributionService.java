package com.lovecube.backend.growth.service;

import com.lovecube.backend.growth.entity.ContributionLog;
import com.lovecube.backend.growth.entity.GrowthEvent;
import com.lovecube.backend.growth.repository.ContributionLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ContributionService {
    private final ContributionLogRepository contributionLogRepository;

    public ContributionService(ContributionLogRepository contributionLogRepository) {
        this.contributionLogRepository = contributionLogRepository;
    }

    @Transactional
    public ContributionLog createActorContribution(
            GrowthEvent event,
            GrowthRuleCatalog.GrowthRule rule
    ) {
        if (!withinDailyLimit(event, rule)) {
            return null;
        }

        String dedupeKey = "event:" + event.getId() + ":actor:" + event.getActorUserId();
        Optional<ContributionLog> existing = contributionLogRepository.findByDedupeKey(dedupeKey);
        if (existing.isPresent()) {
            return existing.get();
        }

        ContributionLog log = new ContributionLog();
        log.setUserId(event.getActorUserId());
        log.setEventId(event.getId());
        log.setEventType(event.getEventType());
        log.setDimension(rule.dimension().name());
        log.setDeltaBase(rule.deltaBase());
        log.setDeltaFinal(rule.deltaBase());
        log.setWeightJson("{\"quality\":1.0,\"role\":1.0,\"health\":1.0}");
        log.setRuleVersion(event.getRuleVersion());
        log.setSourcePlatform(event.getSourcePlatform());
        log.setSettleStatus(event.getSettleStatus());
        log.setDedupeKey(dedupeKey);
        log.setReason("p0_base_rule");
        log.setOccurredAt(event.getOccurredAt());
        log.setCreatedAt(event.getCreatedAt());
        return contributionLogRepository.save(log);
    }

    private boolean withinDailyLimit(GrowthEvent event, GrowthRuleCatalog.GrowthRule rule) {
        int dailyLimit = Math.max(0, rule.dailyLimit());
        if (dailyLimit == 0) {
            return true;
        }
        LocalDate date = event.getOccurredAt().toLocalDate();
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        long count = contributionLogRepository.countByUserIdAndEventTypeAndOccurredAtBetween(
                event.getActorUserId(),
                event.getEventType(),
                start,
                end
        );
        return count < dailyLimit;
    }
}
