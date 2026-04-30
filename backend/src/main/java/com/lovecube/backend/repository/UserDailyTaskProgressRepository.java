package com.lovecube.backend.repository;

import com.lovecube.backend.entity.UserDailyTaskProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserDailyTaskProgressRepository extends JpaRepository<UserDailyTaskProgress, Long> {
    List<UserDailyTaskProgress> findByUserIdAndTaskDate(Long userId, LocalDate taskDate);

    Optional<UserDailyTaskProgress> findByUserIdAndTaskCodeAndTaskDate(Long userId, String taskCode, LocalDate taskDate);

    long countByUserIdAndCompleted(Long userId, Integer completed);
}
