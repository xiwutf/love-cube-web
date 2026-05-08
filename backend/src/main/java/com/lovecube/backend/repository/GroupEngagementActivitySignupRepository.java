package com.lovecube.backend.repository;

import com.lovecube.backend.entity.GroupEngagementActivitySignup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupEngagementActivitySignupRepository extends JpaRepository<GroupEngagementActivitySignup, Long> {

    Optional<GroupEngagementActivitySignup> findByActivityIdAndUserId(Long activityId, Long userId);

    List<GroupEngagementActivitySignup> findByActivityId(Long activityId);

    List<GroupEngagementActivitySignup> findByActivityIdInAndUserIdAndStatus(
            List<Long> activityIds, Long userId, String status);
}
