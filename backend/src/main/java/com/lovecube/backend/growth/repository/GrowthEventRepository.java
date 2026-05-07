package com.lovecube.backend.growth.repository;

import com.lovecube.backend.growth.entity.GrowthEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GrowthEventRepository extends JpaRepository<GrowthEvent, Long> {
    Optional<GrowthEvent> findByDedupeKey(String dedupeKey);

    Page<GrowthEvent> findByActorUserIdOrderByOccurredAtDesc(Long actorUserId, Pageable pageable);

    long countBySettleStatus(String settleStatus);

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime start, LocalDateTime end);

    @Query("SELECT g.eventType, COUNT(g) FROM GrowthEvent g GROUP BY g.eventType")
    List<Object[]> countByEventType();
}
