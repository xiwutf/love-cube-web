package com.lovecube.backend.repository;

import com.lovecube.backend.entity.ContentRiskLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentRiskLogRepository extends JpaRepository<ContentRiskLog, Long> {
}
