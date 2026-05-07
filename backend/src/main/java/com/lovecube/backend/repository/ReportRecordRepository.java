package com.lovecube.backend.repository;

import com.lovecube.backend.entity.ReportRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportRecordRepository extends JpaRepository<ReportRecord, String> {

    List<ReportRecord> findByReporterIdOrderByCreatedAtDesc(Long reporterId);

    long countByStatusIgnoreCase(String status);

    long countByCreatedAtGreaterThanEqual(LocalDateTime createdAt);

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime start, LocalDateTime end);

    List<ReportRecord> findByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT COUNT(r) FROM ReportRecord r WHERE LOWER(COALESCE(r.status, '')) <> 'pending'")
    long countHandledReports();

    @Query("SELECT r.reasonType, COUNT(r) FROM ReportRecord r WHERE r.createdAt >= :since GROUP BY r.reasonType")
    List<Object[]> countGroupedByReasonSince(@Param("since") LocalDateTime since);
}
