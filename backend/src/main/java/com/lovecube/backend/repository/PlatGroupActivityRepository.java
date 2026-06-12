package com.lovecube.backend.repository;

import com.lovecube.backend.entity.PlatGroupActivity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlatGroupActivityRepository extends JpaRepository<PlatGroupActivity, Long> {

    List<PlatGroupActivity> findByGroupIdOrderByStartTimeDesc(Long groupId);

    List<PlatGroupActivity> findByGroupIdAndStatusOrderByStartTimeDesc(Long groupId, String status);

    Optional<PlatGroupActivity> findByIdAndGroupId(Long id, Long groupId);

    List<PlatGroupActivity> findByGroupIdAndStatusInOrderByStartTimeAsc(Long groupId, List<String> statuses, Pageable pageable);

    @Modifying
    @Query(value = "UPDATE platform_group_activity SET participant_count = participant_count + 1 WHERE id = :id", nativeQuery = true)
    void incrementParticipantCount(@Param("id") Long id);

    @Modifying
    @Query(value = "UPDATE platform_group_activity SET participant_count = GREATEST(0, participant_count - 1) WHERE id = :id", nativeQuery = true)
    void decrementParticipantCount(@Param("id") Long id);

    long countByGroupIdAndStatus(Long groupId, String status);

    List<PlatGroupActivity> findByGroupIdAndCreatorUserIdAndStatusInOrderByCreatedAtDesc(
            Long groupId, Long creatorUserId, List<String> statuses);

    List<PlatGroupActivity> findByGroupIdAndStatusOrderByCreatedAtDesc(Long groupId, String status);

    @Query(value = """
            SELECT COUNT(*) FROM platform_group_activity
            WHERE group_id = :groupId
              AND (status IN ('pending', 'rejected')
                   OR (status = 'published' AND reviewed_by IS NOT NULL))
            """, nativeQuery = true)
    long countMemberActivityProposalsSubmitted(@Param("groupId") Long groupId);

    @Query(value = """
            SELECT COUNT(*) FROM platform_group_activity
            WHERE group_id = :groupId
              AND status = 'published'
              AND reviewed_by IS NOT NULL
            """, nativeQuery = true)
    long countMemberActivityProposalsApproved(@Param("groupId") Long groupId);

    List<PlatGroupActivity> findByCreatorUserIdOrderByCreatedAtDesc(Long creatorUserId);

    @Query("""
            SELECT a FROM PlatGroupActivity a
            WHERE a.creatorUserId = :userId
            AND (a.status IN ('pending', 'rejected')
                 OR (a.status = 'published' AND a.reviewedBy IS NOT NULL))
            ORDER BY a.createdAt DESC
            """)
    List<PlatGroupActivity> findMemberProposalsByCreatorUserId(@Param("userId") Long userId);

    @Query("""
            SELECT a FROM PlatGroupActivity a
            WHERE a.status = 'published'
            AND a.startTime >= :start AND a.startTime < :end
            """)
    List<PlatGroupActivity> findPublishedStartingBetween(
            @Param("start") java.time.LocalDateTime start,
            @Param("end") java.time.LocalDateTime end);

    @Query("""
            SELECT a FROM PlatGroupActivity a
            WHERE a.status = 'published'
            AND a.endTime < :now AND a.endTime >= :since
            """)
    List<PlatGroupActivity> findPublishedEndedBetween(
            @Param("since") java.time.LocalDateTime since,
            @Param("now") java.time.LocalDateTime now);
}
