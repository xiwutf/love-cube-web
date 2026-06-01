package com.lovecube.backend.repository;

import com.lovecube.backend.entity.UserWeeklyTaskProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserWeeklyTaskProgressRepository extends JpaRepository<UserWeeklyTaskProgress, Long> {
    Optional<UserWeeklyTaskProgress> findByUserIdAndTaskCodeAndWeekStart(Long userId, String taskCode, LocalDate weekStart);

    List<UserWeeklyTaskProgress> findByUserIdAndWeekStart(Long userId, LocalDate weekStart);
}
