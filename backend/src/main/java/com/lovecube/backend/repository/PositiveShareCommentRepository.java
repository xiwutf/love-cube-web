package com.lovecube.backend.repository;

import com.lovecube.backend.entity.PositiveShareComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface PositiveShareCommentRepository extends JpaRepository<PositiveShareComment, Long> {
    long countByShareId(Long shareId);

    Page<PositiveShareComment> findByShareIdOrderByCreatedAtDesc(Long shareId, Pageable pageable);

    long countByCreatedAtGreaterThanEqual(LocalDateTime since);
}
