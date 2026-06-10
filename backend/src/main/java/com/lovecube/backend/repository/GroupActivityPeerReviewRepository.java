package com.lovecube.backend.repository;

import com.lovecube.backend.entity.GroupActivityPeerReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupActivityPeerReviewRepository extends JpaRepository<GroupActivityPeerReview, Long> {

    Optional<GroupActivityPeerReview> findByActivityIdAndReviewerUserIdAndTargetUserId(
            Long activityId, Long reviewerUserId, Long targetUserId);

    List<GroupActivityPeerReview> findByActivityIdAndReviewerUserId(Long activityId, Long reviewerUserId);
}
