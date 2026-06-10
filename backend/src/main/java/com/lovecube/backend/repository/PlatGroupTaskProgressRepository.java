package com.lovecube.backend.repository;

import com.lovecube.backend.entity.PlatGroupTaskProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PlatGroupTaskProgressRepository extends JpaRepository<PlatGroupTaskProgress, Long> {

    Optional<PlatGroupTaskProgress> findByGroupIdAndUserIdAndTaskCodeAndTaskDate(
            Long groupId, Long userId, String taskCode, LocalDate taskDate);

    List<PlatGroupTaskProgress> findByGroupIdAndUserIdAndTaskDate(
            Long groupId, Long userId, LocalDate taskDate);

    @org.springframework.data.jpa.repository.Query("""
            SELECT COUNT(p) FROM PlatGroupTaskProgress p
            WHERE p.groupId = :groupId AND p.completed = 1 AND p.claimed = 0
            AND p.taskDate >= :since
            """)
    long countUnclaimedCompletedSince(
            @org.springframework.data.repository.query.Param("groupId") Long groupId,
            @org.springframework.data.repository.query.Param("since") LocalDate since);
}
