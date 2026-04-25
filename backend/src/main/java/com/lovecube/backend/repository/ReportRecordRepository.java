package com.lovecube.backend.repository;

import com.lovecube.backend.entity.ReportRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRecordRepository extends JpaRepository<ReportRecord, String> {
}
