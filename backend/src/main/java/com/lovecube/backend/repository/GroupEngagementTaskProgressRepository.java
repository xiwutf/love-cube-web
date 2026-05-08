package com.lovecube.backend.repository;

import com.lovecube.backend.entity.GroupEngagementTaskProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GroupEngagementTaskProgressRepository extends JpaRepository<GroupEngagementTaskProgress, Long> {

    List<GroupEngagementTaskProgress> findByGroupExternalIdAndUserIdAndTaskDate(
            String groupExternalId, Long userId, LocalDate taskDate);

    Optional<GroupEngagementTaskProgress> findByGroupExternalIdAndUserIdAndTaskCodeAndTaskDate(
            String groupExternalId, Long userId, String taskCode, LocalDate taskDate);
}
