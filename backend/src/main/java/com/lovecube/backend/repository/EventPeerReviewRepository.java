package com.lovecube.backend.repository;

import com.lovecube.backend.entity.EventPeerReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventPeerReviewRepository extends JpaRepository<EventPeerReview, Long> {
    Optional<EventPeerReview> findByEventIdAndReviewerUserIdAndTargetUserId(
            String eventId, Long reviewerUserId, Long targetUserId);

    List<EventPeerReview> findByEventIdAndReviewerUserId(String eventId, Long reviewerUserId);
}
