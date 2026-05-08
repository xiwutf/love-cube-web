package com.lovecube.backend.repository;

import com.lovecube.backend.entity.GroupEngagementCheckin;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GroupEngagementCheckinRepository extends JpaRepository<GroupEngagementCheckin, Long> {

    Optional<GroupEngagementCheckin> findByGroupExternalIdAndUserIdAndCheckinDate(
            String groupExternalId, Long userId, LocalDate checkinDate);

    int countByGroupExternalIdAndCheckinDate(String groupExternalId, LocalDate checkinDate);

    Optional<GroupEngagementCheckin> findTopByGroupExternalIdAndUserIdOrderByCheckinDateDesc(
            String groupExternalId, Long userId);

    List<GroupEngagementCheckin> findByGroupExternalIdOrderByCreatedAtDesc(String groupExternalId, Pageable pageable);

    List<GroupEngagementCheckin> findByGroupExternalIdAndCheckinDateOrderByCreatedAtAsc(
            String groupExternalId, LocalDate checkinDate);

    @Query(value = """
            SELECT c.user_id, c.streak_days
            FROM group_engagement_checkin c
            INNER JOIN (
                SELECT user_id, MAX(checkin_date) AS md
                FROM group_engagement_checkin
                WHERE group_external_id = :groupExternalId
                GROUP BY user_id
            ) t ON c.user_id = t.user_id AND c.checkin_date = t.md
                AND c.group_external_id = :groupExternalId
            """, nativeQuery = true)
    List<Object[]> findLatestStreakByUserForGroup(@Param("groupExternalId") String groupExternalId);
}
