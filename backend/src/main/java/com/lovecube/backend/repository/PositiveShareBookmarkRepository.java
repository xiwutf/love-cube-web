package com.lovecube.backend.repository;

import com.lovecube.backend.entity.PositiveShareBookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PositiveShareBookmarkRepository extends JpaRepository<PositiveShareBookmark, Long> {
    boolean existsByShareIdAndUserId(Long shareId, Long userId);

    long deleteByShareIdAndUserId(Long shareId, Long userId);

    List<PositiveShareBookmark> findByShareIdInAndUserId(List<Long> shareIds, Long userId);

    Page<PositiveShareBookmark> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByUserId(Long userId);
}
