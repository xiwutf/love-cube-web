package com.lovecube.backend.repository;

import com.lovecube.backend.entity.UserGrowthLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserGrowthLogRepository extends JpaRepository<UserGrowthLog, Long> {
    boolean existsByUserIdAndActionTypeAndBizId(Long userId, String actionType, String bizId);

    long countByUserIdAndActionType(Long userId, String actionType);
}
