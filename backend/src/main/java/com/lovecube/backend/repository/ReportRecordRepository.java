package com.lovecube.backend.repository;

import com.lovecube.backend.entity.ReportRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRecordRepository extends JpaRepository<ReportRecord, String> {

    List<ReportRecord> findByReporterIdOrderByCreatedAtDesc(Long reporterId);
}
