package com.lovecube.backend.repository;

import com.lovecube.backend.entity.InterestTopicPost;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterestTopicPostRepository extends JpaRepository<InterestTopicPost, Long> {
    List<InterestTopicPost> findByTopicIdAndStatusOrderByCreatedAtDesc(Long topicId, String status, Pageable pageable);

    long countByTopicIdAndStatus(Long topicId, String status);
}
