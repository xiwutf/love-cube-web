package com.lovecube.backend.growth.repository;

import com.lovecube.backend.growth.entity.ContributionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ContributionLogRepository extends JpaRepository<ContributionLog, Long> {
    Optional<ContributionLog> findByDedupeKey(String dedupeKey);

    long countByUserIdAndEventTypeAndOccurredAtBetween(
            Long userId,
            String eventType,
            LocalDateTime start,
            LocalDateTime end
    );

    Page<ContributionLog> findByUserIdOrderByOccurredAtDesc(Long userId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(c.deltaFinal), 0) FROM ContributionLog c")
    long sumTotalContribution();
}
