package com.lovecube.backend.repository;

import com.lovecube.backend.entity.DynamicComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DynamicCommentRepository extends JpaRepository<DynamicComment, Long> {

    Page<DynamicComment> findByDynamicIdAndStatusOrderByCreatedAtAsc(
            Long dynamicId, String status, Pageable pageable);
}
