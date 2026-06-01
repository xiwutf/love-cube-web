package com.lovecube.backend.repository;

import com.lovecube.backend.entity.WeeklyTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WeeklyTaskRepository extends JpaRepository<WeeklyTask, Long> {
    List<WeeklyTask> findByEnabledOrderBySortNoAsc(Integer enabled);

    List<WeeklyTask> findByEnabledAndActionType(Integer enabled, String actionType);

    Optional<WeeklyTask> findByCode(String code);
}
