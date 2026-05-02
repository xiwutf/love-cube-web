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
}
