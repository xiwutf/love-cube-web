package com.lovecube.backend.repository;

import com.lovecube.backend.entity.PlatformEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlatformEventRepository extends JpaRepository<PlatformEvent, String> {
    List<PlatformEvent> findByStatusOrderByEventTimeDesc(String status);

    long countByPinnedTrue();

    long countByRecommendedTrue();

    @Query("SELECT e FROM PlatformEvent e WHERE e.status = :status ORDER BY CASE WHEN e.pinned = true THEN 0 ELSE 1 END, e.eventTime DESC")
    List<PlatformEvent> findByStatusPinnedFirst(@Param("status") String status);
}
