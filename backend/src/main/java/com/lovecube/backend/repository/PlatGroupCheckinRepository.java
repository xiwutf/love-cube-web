package com.lovecube.backend.repository;

import com.lovecube.backend.entity.PlatGroupCheckin;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PlatGroupCheckinRepository extends JpaRepository<PlatGroupCheckin, Long> {

    Optional<PlatGroupCheckin> findByGroupIdAndUserIdAndCheckinDate(Long groupId, Long userId, LocalDate checkinDate);

    int countByGroupIdAndCheckinDate(Long groupId, LocalDate checkinDate);

    Optional<PlatGroupCheckin> findTopByGroupIdAndUserIdOrderByCheckinDateDesc(Long groupId, Long userId);

    List<PlatGroupCheckin> findByGroupIdOrderByCreatedAtDesc(Long groupId, Pageable pageable);

    List<PlatGroupCheckin> findByGroupIdAndCheckinDateOrderByCreatedAtAsc(Long groupId, LocalDate checkinDate);

    /**
     * 每个用户在团体下「最近一次打卡」的 streak_days（用于连续榜）。
     */
    @Query(value = """
            SELECT c.user_id, c.streak_days
            FROM platform_group_checkin c
            INNER JOIN (
                SELECT user_id, MAX(checkin_date) AS md
                FROM platform_group_checkin
                WHERE group_id = :groupId
                GROUP BY user_id
            ) t ON c.user_id = t.user_id AND c.checkin_date = t.md AND c.group_id = :groupId
            """, nativeQuery = true)
    List<Object[]> findLatestStreakByUserForGroup(@Param("groupId") Long groupId);

    @Query("""
            SELECT COUNT(DISTINCT c.userId) FROM PlatGroupCheckin c
            WHERE c.groupId = :groupId AND c.checkinDate >= :since
            """)
    long countDistinctUsersSince(@Param("groupId") Long groupId, @Param("since") LocalDate since);

    @Query("""
            SELECT DISTINCT c.userId FROM PlatGroupCheckin c
            WHERE c.groupId = :groupId AND c.checkinDate >= :since
            """)
    List<Long> findDistinctUserIdsSinceDate(
            @Param("groupId") Long groupId,
            @Param("since") LocalDate since);

    @Query("""
            SELECT DISTINCT c.userId FROM PlatGroupCheckin c
            WHERE c.groupId = :groupId AND c.checkinDate = :day
            """)
    List<Long> findDistinctUserIdsOnDate(
            @Param("groupId") Long groupId,
            @Param("day") LocalDate day);

    @Query("SELECT DISTINCT c.userId FROM PlatGroupCheckin c WHERE c.groupId = :groupId")
    List<Long> findDistinctUserIdsAllTime(@Param("groupId") Long groupId);
}
