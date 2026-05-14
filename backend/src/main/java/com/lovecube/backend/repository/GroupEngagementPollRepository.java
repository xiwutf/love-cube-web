package com.lovecube.backend.repository;

import com.lovecube.backend.entity.GroupEngagementPoll;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupEngagementPollRepository extends JpaRepository<GroupEngagementPoll, Long> {

    List<GroupEngagementPoll> findByGroupExternalIdOrderByCreatedAtDesc(String groupExternalId);

    Optional<GroupEngagementPoll> findByIdAndGroupExternalId(Long id, String groupExternalId);
}
