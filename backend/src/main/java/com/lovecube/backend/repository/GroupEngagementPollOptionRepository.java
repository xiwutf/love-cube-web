package com.lovecube.backend.repository;

import com.lovecube.backend.entity.GroupEngagementPollOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupEngagementPollOptionRepository extends JpaRepository<GroupEngagementPollOption, Long> {

    List<GroupEngagementPollOption> findByPollIdOrderBySortOrderAscIdAsc(Long pollId);
}
