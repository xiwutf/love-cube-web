package com.lovecube.backend.repository;

import com.lovecube.backend.entity.PlatGroupActivitySignup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PlatGroupActivitySignupRepository extends JpaRepository<PlatGroupActivitySignup, Long> {

    Optional<PlatGroupActivitySignup> findByActivityIdAndUserId(Long activityId, Long userId);

    int countByActivityIdAndStatus(Long activityId, String status);

    List<PlatGroupActivitySignup> findByActivityId(Long activityId);

    List<PlatGroupActivitySignup> findByActivityIdInAndUserIdAndStatus(
            List<Long> activityIds, Long userId, String status);

    List<PlatGroupActivitySignup> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);

    long countByCreatedAtGreaterThanEqual(LocalDateTime since);

    List<PlatGroupActivitySignup> findByActivityIdAndStatusAndCheckedInTrue(Long activityId, String status);

    @org.springframework.data.jpa.repository.Query("""
            SELECT COUNT(DISTINCT s.userId) FROM PlatGroupActivitySignup s
            WHERE s.groupId = :groupId AND s.status = 'signed_up'
            AND s.updatedAt >= :since
            """)
    long countDistinctActiveSignupsSince(
            @org.springframework.data.repository.query.Param("groupId") Long groupId,
            @org.springframework.data.repository.query.Param("since") LocalDateTime since);

    @org.springframework.data.jpa.repository.Query("""
            SELECT DISTINCT s.userId FROM PlatGroupActivitySignup s
            WHERE s.groupId = :groupId
            AND (s.createdAt >= :since OR (s.checkedInAt IS NOT NULL AND s.checkedInAt >= :since))
            """)
    List<Long> findDistinctUserIdsSince(
            @org.springframework.data.repository.query.Param("groupId") Long groupId,
            @org.springframework.data.repository.query.Param("since") LocalDateTime since);

    @org.springframework.data.jpa.repository.Query("""
            SELECT DISTINCT s.userId FROM PlatGroupActivitySignup s WHERE s.groupId = :groupId
            """)
    List<Long> findDistinctUserIdsAllTime(
            @org.springframework.data.repository.query.Param("groupId") Long groupId);

    @org.springframework.data.jpa.repository.Query("""
            SELECT DISTINCT s.userId FROM PlatGroupActivitySignup s
            WHERE s.groupId = :groupId
            AND ((s.createdAt >= :start AND s.createdAt < :end)
                OR (s.checkedInAt IS NOT NULL AND s.checkedInAt >= :start AND s.checkedInAt < :end))
            """)
    List<Long> findDistinctUserIdsActiveBetween(
            @org.springframework.data.repository.query.Param("groupId") Long groupId,
            @org.springframework.data.repository.query.Param("start") LocalDateTime start,
            @org.springframework.data.repository.query.Param("end") LocalDateTime end);
}
