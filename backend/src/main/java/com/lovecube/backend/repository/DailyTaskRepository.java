package com.lovecube.backend.repository;

import com.lovecube.backend.entity.DailyTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DailyTaskRepository extends JpaRepository<DailyTask, Long> {
    List<DailyTask> findByEnabledOrderBySortNoAsc(Integer enabled);

    List<DailyTask> findByEnabledAndActionType(Integer enabled, String actionType);

    Optional<DailyTask> findByCode(String code);
}
