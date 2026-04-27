package com.lovecube.backend.repository;

import com.lovecube.backend.entity.ReportRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportRecordRepository extends JpaRepository<ReportRecord, String> {

    List<ReportRecord> findByReporterIdOrderByCreatedAtDesc(Long reporterId);

    long countByStatusIgnoreCase(String status);

    long countByCreatedAtGreaterThanEqual(LocalDateTime createdAt);

    @Query("SELECT COUNT(r) FROM ReportRecord r WHERE LOWER(COALESCE(r.status, '')) <> 'pending'")
    long countHandledReports();
}
