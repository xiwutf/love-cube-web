package com.lovecube.backend.repository;

import com.lovecube.backend.entity.DatingMutualMatchJobLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatingMutualMatchJobLogRepository extends JpaRepository<DatingMutualMatchJobLog, Long> {

    boolean existsByEventIdAndJobType(String eventId, String jobType);
}
