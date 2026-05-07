package com.lovecube.backend.repository;

import com.lovecube.backend.entity.PositiveShareLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PositiveShareLikeRepository extends JpaRepository<PositiveShareLike, Long> {
    boolean existsByShareIdAndUserId(Long shareId, Long userId);

    long countByShareId(Long shareId);

    long deleteByShareIdAndUserId(Long shareId, Long userId);

    List<PositiveShareLike> findByShareIdInAndUserId(List<Long> shareIds, Long userId);

    Page<PositiveShareLike> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByUserId(Long userId);
}
