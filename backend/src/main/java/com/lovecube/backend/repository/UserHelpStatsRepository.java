package com.lovecube.backend.repository;

import com.lovecube.backend.entity.UserHelpStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserHelpStatsRepository extends JpaRepository<UserHelpStats, Long> {
    List<UserHelpStats> findTop20ByOrderByCreditScoreDescSuccessCountDesc();
}
