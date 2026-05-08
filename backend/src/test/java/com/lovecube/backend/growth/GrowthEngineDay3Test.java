package com.lovecube.backend.growth;

import com.lovecube.backend.growth.entity.ContributionLog;
import com.lovecube.backend.growth.entity.GrowthEvent;
import com.lovecube.backend.growth.entity.UserGrowth;
import com.lovecube.backend.growth.enums.GrowthEventType;
import com.lovecube.backend.growth.enums.SettleStatus;
import com.lovecube.backend.repository.DynamicRepository;
import com.lovecube.backend.growth.repository.ContributionLogRepository;
import com.lovecube.backend.growth.repository.GrowthUserGrowthRepository;
import com.lovecube.backend.growth.service.ContributionService;
import com.lovecube.backend.growth.service.GrowthEngine;
import com.lovecube.backend.growth.service.GrowthBroadcastService;
import com.lovecube.backend.growth.service.GrowthRewardService;
import com.lovecube.backend.growth.service.GrowthRuleCatalog;
import com.lovecube.backend.growth.service.UserGrowthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class GrowthEngineDay3Test {
    private final Map<String, ContributionLog> logByDedupe = new HashMap<>();
    private final List<ContributionLog> logs = new ArrayList<>();
    private final Map<Long, UserGrowth> growthByUser = new HashMap<>();
    private final AtomicLong logId = new AtomicLong(1);

    private GrowthEngine growthEngine;

    @BeforeEach
    void setUp() {
        ContributionLogRepository contributionLogRepository = Mockito.mock(ContributionLogRepository.class);
        GrowthUserGrowthRepository growthUserGrowthRepository = Mockito.mock(GrowthUserGrowthRepository.class);
        DynamicRepository dynamicRepository = Mockito.mock(DynamicRepository.class);
        Mockito.when(dynamicRepository.existsByUserIdAndContentAndSceneTypeAndIsDeletedFalse(
                Mockito.anyLong(), Mockito.anyString(), Mockito.anyString()
        )).thenReturn(false);
        Mockito.when(dynamicRepository.existsByUserIdAndMarkerAndSceneTypeAndIsDeletedFalse(
                Mockito.anyLong(), Mockito.anyString(), Mockito.anyString()
        )).thenReturn(false);

        Mockito.when(contributionLogRepository.findByDedupeKey(Mockito.anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(logByDedupe.get(invocation.getArgument(0))));
        Mockito.when(contributionLogRepository.save(Mockito.any(ContributionLog.class)))
                .thenAnswer(invocation -> {
                    ContributionLog log = invocation.getArgument(0);
                    if (log.getId() == null) {
                        log.setId(logId.getAndIncrement());
                    }
                    logByDedupe.put(log.getDedupeKey(), log);
                    logs.removeIf(existing -> Objects.equals(existing.getId(), log.getId()));
                    logs.add(log);
                    return log;
                });
        Mockito.when(contributionLogRepository.countByUserIdAndEventTypeAndOccurredAtBetween(
                        Mockito.anyLong(), Mockito.anyString(), Mockito.any(), Mockito.any()))
                .thenAnswer(invocation -> {
                    Long userId = invocation.getArgument(0);
                    String eventType = invocation.getArgument(1);
                    LocalDateTime start = invocation.getArgument(2);
                    LocalDateTime end = invocation.getArgument(3);
                    return logs.stream()
                            .filter(log -> Objects.equals(log.getUserId(), userId))
                            .filter(log -> eventType.equals(log.getEventType()))
                            .filter(log -> !log.getOccurredAt().isBefore(start) && log.getOccurredAt().isBefore(end))
                            .count();
                });

        Mockito.when(growthUserGrowthRepository.findByUserId(Mockito.anyLong()))
                .thenAnswer(invocation -> Optional.ofNullable(growthByUser.get(invocation.getArgument(0))));
        Mockito.when(growthUserGrowthRepository.save(Mockito.any(UserGrowth.class)))
                .thenAnswer(invocation -> {
                    UserGrowth growth = invocation.getArgument(0);
                    if (growth.getId() == null) {
                        growth.setId((long) (growthByUser.size() + 1));
                    }
                    growthByUser.put(growth.getUserId(), growth);
                    return growth;
                });

        GrowthRuleCatalog catalog = new GrowthRuleCatalog();
        ContributionService contributionService = new ContributionService(contributionLogRepository);
        UserGrowthService userGrowthService = new UserGrowthService(growthUserGrowthRepository);
        GrowthBroadcastService growthBroadcastService = new GrowthBroadcastService(dynamicRepository);
        GrowthRewardService growthRewardService = Mockito.mock(GrowthRewardService.class);
        growthEngine = new GrowthEngine(catalog, contributionService, userGrowthService, growthBroadcastService, growthRewardService);
    }

    @Test
    void shouldProcessTenSamplesAndKeepSnapshotConsistent() {
        Long actor = 1001L;
        LocalDateTime now = LocalDateTime.now();
        List<GrowthEvent> events = List.of(
                event(1L, GrowthEventType.USER_REGISTERED, SettleStatus.SETTLED, actor, now.plusSeconds(1)),
                event(2L, GrowthEventType.USER_PROFILE_COMPLETED, SettleStatus.SETTLED, actor, now.plusSeconds(2)),
                event(3L, GrowthEventType.USER_DAILY_ACTIVE, SettleStatus.SETTLED, actor, now.plusSeconds(3)),
                event(4L, GrowthEventType.POST_CREATED, SettleStatus.SETTLED, actor, now.plusSeconds(4)),
                event(5L, GrowthEventType.POST_LIKED, SettleStatus.SETTLED, actor, now.plusSeconds(5)),
                event(6L, GrowthEventType.POST_COMMENTED, SettleStatus.SETTLED, actor, now.plusSeconds(6)),
                event(7L, GrowthEventType.GROUP_CREATED, SettleStatus.SETTLED, actor, now.plusSeconds(7)),
                event(8L, GrowthEventType.GROUP_JOINED, SettleStatus.SETTLED, actor, now.plusSeconds(8)),
                event(9L, GrowthEventType.USER_INVITED_REGISTERED, SettleStatus.SETTLED, actor, now.plusSeconds(9)),
                event(10L, GrowthEventType.USER_INVITED_EFFECTIVE, SettleStatus.PENDING, actor, now.plusSeconds(10))
        );

        events.forEach(growthEngine::consumeForTesting);

        UserGrowth snapshot = growthByUser.get(actor);
        assertNotNull(snapshot);
        assertEquals(9, logs.size(), "pending 事件不应记账");
        assertEquals(60, snapshot.getTotalContribution());
        assertEquals(18, snapshot.getContribContent());
        assertEquals(27, snapshot.getContribOrg());
        assertEquals(15, snapshot.getContribSpread());
        assertEquals(0, snapshot.getContribCity());
        assertEquals(2, snapshot.getLevel(), "总贡献 60 应落入 level=2");

        // 同一事件再次消费，dedupe_key 应保证不重复记账
        growthEngine.consumeForTesting(events.get(6));
        assertEquals(9, logs.size());
        assertEquals(60, snapshot.getTotalContribution());
    }

    private GrowthEvent event(
            Long id,
            GrowthEventType type,
            SettleStatus status,
            Long actorUserId,
            LocalDateTime occurredAt
    ) {
        GrowthEvent event = new GrowthEvent();
        event.setId(id);
        event.setEventType(type.name());
        event.setActorUserId(actorUserId);
        event.setRuleVersion("v1");
        event.setSourcePlatform("API");
        event.setSettleStatus(status.name());
        event.setDedupeKey("event:" + id);
        event.setOccurredAt(occurredAt);
        event.setCreatedAt(occurredAt);
        return event;
    }
}
