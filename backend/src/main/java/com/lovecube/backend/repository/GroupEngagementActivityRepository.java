package com.lovecube.backend.repository;

import com.lovecube.backend.entity.GroupEngagementActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupEngagementActivityRepository extends JpaRepository<GroupEngagementActivity, Long> {

    List<GroupEngagementActivity> findByGroupExternalIdOrderByStartTimeDesc(String groupExternalId);

    Optional<GroupEngagementActivity> findByIdAndGroupExternalId(Long id, String groupExternalId);

    @Modifying
    @Query(value = "UPDATE group_engagement_activity SET participant_count = participant_count + 1 WHERE id = :id", nativeQuery = true)
    void incrementParticipantCount(@Param("id") Long id);

    @Modifying
    @Query(value = "UPDATE group_engagement_activity SET participant_count = GREATEST(0, participant_count - 1) WHERE id = :id", nativeQuery = true)
    void decrementParticipantCount(@Param("id") Long id);
}
