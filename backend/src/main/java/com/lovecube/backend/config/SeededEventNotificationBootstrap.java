package com.lovecube.backend.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovecube.backend.entity.HomeConfig;
import com.lovecube.backend.entity.PlatformEvent;
import com.lovecube.backend.repository.HomeConfigRepository;
import com.lovecube.backend.repository.PlatformEventRepository;
import com.lovecube.backend.services.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Flyway 种子活动发布后，在应用启动后异步补发一次站内活动通知（仅当 notify_bootstrap 行仍启用时执行）。
 */
@Component
public class SeededEventNotificationBootstrap {

    private static final Logger log = LoggerFactory.getLogger(SeededEventNotificationBootstrap.class);
    private static final String GROUP_NOTIFY_BOOTSTRAP = "notify_bootstrap";

    private final HomeConfigRepository homeConfigRepository;
    private final PlatformEventRepository platformEventRepository;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final TaskExecutor notificationDispatchExecutor;

    public SeededEventNotificationBootstrap(
            HomeConfigRepository homeConfigRepository,
            PlatformEventRepository platformEventRepository,
            NotificationService notificationService,
            ObjectMapper objectMapper,
            @Qualifier("notificationDispatchExecutor") TaskExecutor notificationDispatchExecutor
    ) {
        this.homeConfigRepository = homeConfigRepository;
        this.platformEventRepository = platformEventRepository;
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
        this.notificationDispatchExecutor = notificationDispatchExecutor;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        List<HomeConfig> pending = homeConfigRepository
                .findByConfigGroupInAndEnabledTrueOrderByConfigGroupAscSortOrderAscIdAsc(List.of(GROUP_NOTIFY_BOOTSTRAP));
        if (pending.isEmpty()) {
            log.debug("无待发送的种子活动通知（notify_bootstrap 已处理或未配置）");
            return;
        }
        log.info("检测到 {} 条种子活动通知待发送，将在后台异步执行", pending.size());
        for (HomeConfig row : pending) {
            notificationDispatchExecutor.execute(() -> dispatchSafely(row.getId()));
        }
    }

    private void dispatchSafely(Long homeConfigId) {
        try {
            dispatchById(homeConfigId);
        } catch (Exception ex) {
            log.warn("种子活动通知引导失败 id={}: {}", homeConfigId, ex.getMessage());
        }
    }

    private void dispatchById(Long homeConfigId) {
        HomeConfig row = homeConfigRepository.findById(homeConfigId).orElse(null);
        if (row == null || !Boolean.TRUE.equals(row.getEnabled())) {
            return;
        }
        if (!GROUP_NOTIFY_BOOTSTRAP.equals(row.getConfigGroup())) {
            return;
        }
        dispatchIfNeeded(row);
    }

    private void dispatchIfNeeded(HomeConfig row) {
        Map<String, Object> meta = parseJson(row.getConfigValue());
        String eventId = String.valueOf(meta.getOrDefault("eventId", row.getConfigKey())).trim();
        if (eventId.isBlank()) {
            log.warn("种子活动通知配置缺少 eventId，已跳过 key={}", row.getConfigKey());
            disable(row);
            return;
        }
        PlatformEvent event = platformEventRepository.findById(eventId).orElse(null);
        if (event == null) {
            log.warn("种子活动不存在，暂不发送通知 eventId={}（请确认 Flyway V90 已执行）", eventId);
            return;
        }
        if (!"published".equalsIgnoreCase(String.valueOf(event.getStatus()))) {
            log.warn("种子活动未发布，跳过通知 eventId={} status={}", eventId, event.getStatus());
            disable(row);
            return;
        }
        log.info("开始广播种子活动站内通知 eventId={} title={}", eventId, event.getTitle());
        String summary = event.getSummary() != null && !event.getSummary().isBlank()
                ? event.getSummary()
                : "新活动已发布，欢迎报名参与。";
        int sent = notificationService.broadcastPlatformEventPublished(
                "【活动通知】" + event.getTitle(),
                summary,
                event.getId(),
                "/events/" + event.getId()
        );
        disable(row);
        log.info("种子活动站内通知已完成 eventId={} recipients={}", eventId, sent);
    }

    private void disable(HomeConfig row) {
        row.setEnabled(false);
        homeConfigRepository.save(row);
    }

    private Map<String, Object> parseJson(String raw) {
        if (raw == null || raw.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(raw, new TypeReference<>() {});
        } catch (Exception ignored) {
            return Map.of();
        }
    }
}
