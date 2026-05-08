package com.lovecube.backend.repository;

import com.lovecube.backend.entity.GroupPostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupPostCommentRepository extends JpaRepository<GroupPostComment, Long> {

    void deleteByPostId(String postId);

    Page<GroupPostComment> findByPostIdAndStatusOrderByCreatedAtAsc(
            String postId, String status, Pageable pageable);

    long countByPostIdAndStatus(String postId, String status);
}
