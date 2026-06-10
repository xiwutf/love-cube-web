package com.lovecube.backend.repository;

import com.lovecube.backend.entity.PlatformGroupWeeklyDigestLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlatformGroupWeeklyDigestLogRepository extends JpaRepository<PlatformGroupWeeklyDigestLog, Long> {

    Optional<PlatformGroupWeeklyDigestLog> findByGroupIdAndWeekKey(Long groupId, String weekKey);
}
