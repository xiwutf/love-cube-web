package com.lovecube.backend.repository;

import com.lovecube.backend.entity.SpaceCampaignProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SpaceCampaignProgressRepository extends JpaRepository<SpaceCampaignProgress, Long> {
    List<SpaceCampaignProgress> findByCampaignIdAndUserIdOrderByDayNumberAsc(Long campaignId, Long userId);

    Optional<SpaceCampaignProgress> findByCampaignIdAndUserIdAndDayNumber(
            Long campaignId, Long userId, Integer dayNumber);

    long countByCampaignId(Long campaignId);

    @Query("SELECT COUNT(DISTINCT p.userId) FROM SpaceCampaignProgress p WHERE p.campaignId = :campaignId")
    long countDistinctParticipants(@Param("campaignId") Long campaignId);

    @Query("SELECT p.dayNumber, COUNT(DISTINCT p.userId) FROM SpaceCampaignProgress p "
            + "WHERE p.campaignId = :campaignId GROUP BY p.dayNumber ORDER BY p.dayNumber")
    List<Object[]> countCompletionsByDay(@Param("campaignId") Long campaignId);

    long countByCampaignIdAndDayNumber(Long campaignId, Integer dayNumber);

    @Query("SELECT COUNT(p) FROM SpaceCampaignProgress p WHERE p.campaignId = :campaignId")
    long countTotalCompletions(@Param("campaignId") Long campaignId);

    List<SpaceCampaignProgress> findByCampaignIdAndUserIdIn(Long campaignId, Collection<Long> userIds);

    @Query("SELECT p.userId FROM SpaceCampaignProgress p WHERE p.campaignId = :campaignId AND p.dayNumber = :dayNumber")
    List<Long> findUserIdsByCampaignIdAndDayNumber(
            @Param("campaignId") Long campaignId,
            @Param("dayNumber") Integer dayNumber
    );

    @Query("""
            SELECT DISTINCT p.userId FROM SpaceCampaignProgress p
            WHERE p.campaignId IN (SELECT c.id FROM SpaceCampaign c WHERE c.groupId = :groupId)
            AND p.completedAt >= :since
            """)
    List<Long> findDistinctUserIdsForGroupSince(
            @Param("groupId") Long groupId,
            @Param("since") LocalDateTime since);

    @Query("""
            SELECT DISTINCT p.userId FROM SpaceCampaignProgress p
            WHERE p.campaignId IN (SELECT c.id FROM SpaceCampaign c WHERE c.groupId = :groupId)
            AND p.completedAt >= :start AND p.completedAt < :end
            """)
    List<Long> findDistinctUserIdsForGroupBetween(
            @Param("groupId") Long groupId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("""
            SELECT DISTINCT p.userId FROM SpaceCampaignProgress p
            WHERE p.campaignId IN (SELECT c.id FROM SpaceCampaign c WHERE c.groupId = :groupId)
            """)
    List<Long> findDistinctUserIdsForGroupAllTime(@Param("groupId") Long groupId);
}
