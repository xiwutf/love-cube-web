package com.lovecube.backend.repository;

import com.lovecube.backend.entity.PlatGroupPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PlatGroupPostRepository extends JpaRepository<PlatGroupPost, Long> {

    List<PlatGroupPost> findByGroupIdAndStatusOrderByCreatedAtDesc(Long groupId, String status);

    Page<PlatGroupPost> findByGroupIdAndStatusOrderByCreatedAtDesc(Long groupId, String status, Pageable pageable);

    List<PlatGroupPost> findTop20ByStatusOrderByCreatedAtDesc(String status);

    long countByCreatedAtGreaterThanEqual(LocalDateTime since);
}
