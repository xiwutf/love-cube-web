package com.lovecube.backend.repository;

import com.lovecube.backend.entity.PlatformEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PlatformEventRepository extends JpaRepository<PlatformEvent, String> {
    List<PlatformEvent> findByStatusOrderByEventTimeDesc(String status);

    long countByPinnedTrue();

    long countByRecommendedTrue();

    long countByStatus(String status);

    @Query("SELECT COUNT(DISTINCT e.location) FROM PlatformEvent e WHERE e.status = 'published' AND e.location IS NOT NULL AND e.location <> ''")
    long countDistinctPublishedLocations();

    @Query("SELECT e FROM PlatformEvent e WHERE e.status = :status ORDER BY CASE WHEN e.pinned = true THEN 0 ELSE 1 END, e.eventTime DESC")
    List<PlatformEvent> findByStatusPinnedFirst(@Param("status") String status);

    long countByCreatedAtGreaterThanEqual(LocalDateTime since);

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime start, LocalDateTime end);

    List<PlatformEvent> findByStatusAndEventTimeGreaterThanEqualAndEventTimeLessThan(
            String status, LocalDateTime start, LocalDateTime end);

    List<PlatformEvent> findByStatusAndEventTimeLessThanAndEventTimeGreaterThanEqual(
            String status, LocalDateTime before, LocalDateTime since);

    List<PlatformEvent> findByStatusAndTemplateType(String status, String templateType);
}
