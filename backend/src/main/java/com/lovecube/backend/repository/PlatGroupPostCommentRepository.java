package com.lovecube.backend.repository;

import com.lovecube.backend.entity.PlatGroupPostComment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlatGroupPostCommentRepository extends JpaRepository<PlatGroupPostComment, Long> {

    List<PlatGroupPostComment> findByPostIdAndStatusOrderByCreatedAtAsc(Long postId, String status, Pageable pageable);

    long countByPostIdAndStatus(Long postId, String status);
}
