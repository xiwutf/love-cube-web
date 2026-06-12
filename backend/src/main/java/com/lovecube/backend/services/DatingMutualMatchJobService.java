package com.lovecube.backend.services;

import com.lovecube.backend.dating.DatingEventTemplate;
import com.lovecube.backend.entity.DatingMutualMatchJobLog;
import com.lovecube.backend.entity.PlatformEvent;
import com.lovecube.backend.repository.DatingMutualMatchJobLogRepository;
import com.lovecube.backend.repository.PlatformEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class DatingMutualMatchJobService {

    public static final String JOB_TYPE_AUTO_RECOMPUTE_AFTER_END = "AUTO_RECOMPUTE_AFTER_END";

    private static final Logger log = LoggerFactory.getLogger(DatingMutualMatchJobService.class);

    private static final int DELAY_AFTER_END_MINUTES = 5;
    private static final int WINDOW_AFTER_END_MINUTES = 30;

    private final PlatformEventRepository eventRepository;
    private final DatingMutualMatchJobLogRepository jobLogRepository;
    private final DatingEventService datingEventService;
    private final DatingMutualMatchNotifyService notifyService;
    private final TransactionTemplate transactionTemplate;

    public DatingMutualMatchJobService(
            PlatformEventRepository eventRepository,
            DatingMutualMatchJobLogRepository jobLogRepository,
            DatingEventService datingEventService,
            DatingMutualMatchNotifyService notifyService,
            TransactionTemplate transactionTemplate
    ) {
        this.eventRepository = eventRepository;
        this.jobLogRepository = jobLogRepository;
        this.datingEventService = datingEventService;
        this.notifyService = notifyService;
        this.transactionTemplate = transactionTemplate;
    }

    public int runAutoRecomputeAfterEnd() {
        LocalDateTime now = LocalDateTime.now();
        List<PlatformEvent> datingEvents = eventRepository.findByStatusAndTemplateType(
                "published",
                DatingEventTemplate.TYPE_DATING
        );
        int processed = 0;
        for (PlatformEvent event : datingEvents) {
            try {
                Boolean done = transactionTemplate.execute(status -> processEventIfEligible(event, now));
                if (Boolean.TRUE.equals(done)) {
                    processed++;
                }
            } catch (Exception e) {
                log.warn("联谊自动互选计算失败 eventId={}: {}", event.getId(), e.getMessage(), e);
            }
        }
        return processed;
    }

    private boolean processEventIfEligible(PlatformEvent event, LocalDateTime now) {
        if (event == null || event.getId() == null) {
            return false;
        }
        if (!DatingEventTemplate.isDating(event.getTemplateType())) {
            return false;
        }
        LocalDateTime end = PlatformEventLifecycle.resolveEnd(event);
        if (end == null || !end.isBefore(now)) {
            return false;
        }
        if (now.isBefore(end.plusMinutes(DELAY_AFTER_END_MINUTES))) {
            return false;
        }
        if (now.isAfter(end.plusMinutes(WINDOW_AFTER_END_MINUTES))) {
            return false;
        }
        if (jobLogRepository.existsByEventIdAndJobType(event.getId(), JOB_TYPE_AUTO_RECOMPUTE_AFTER_END)) {
            return false;
        }

        Map<String, Object> result = datingEventService.recomputeMutualMatchesInternal(event.getId());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> createdMatches = (List<Map<String, Object>>) result.get("createdMatches");
        int createdCount = ((Number) result.getOrDefault("createdMatchCount", 0)).intValue();
        notifyService.notifyNewMatches(event.getId(), createdMatches);

        DatingMutualMatchJobLog jobLog = new DatingMutualMatchJobLog();
        jobLog.setEventId(event.getId());
        jobLog.setJobType(JOB_TYPE_AUTO_RECOMPUTE_AFTER_END);
        jobLog.setExecutedAt(now);
        try {
            jobLogRepository.save(jobLog);
        } catch (DataIntegrityViolationException e) {
            log.debug("联谊自动互选任务已存在 eventId={}", event.getId());
            return false;
        }

        log.info("联谊自动互选计算完成 eventId={} title={} createdMatchCount={}",
                event.getId(), event.getTitle(), createdCount);
        return true;
    }
}
