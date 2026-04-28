package com.lovecube.backend.repository;

import com.lovecube.backend.entity.PositiveShare;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PositiveShareRepository extends JpaRepository<PositiveShare, Long> {
    @Query("SELECT p FROM PositiveShare p WHERE p.status = 'PUBLISHED' AND p.publicVisible = true ORDER BY p.createdAt DESC")
    Page<PositiveShare> findLatestPublic(Pageable pageable);

    @Query("SELECT p FROM PositiveShare p WHERE p.status = 'PUBLISHED' AND p.publicVisible = true AND p.createdAt >= :startOfDay ORDER BY p.createdAt DESC")
    Page<PositiveShare> findTodayPublic(@Param("startOfDay") LocalDateTime startOfDay, Pageable pageable);

    @Query("SELECT p FROM PositiveShare p WHERE p.status = 'PUBLISHED' AND p.publicVisible = true ORDER BY p.encourageCount DESC, p.commentCount DESC, p.createdAt DESC")
    Page<PositiveShare> findHotPublic(Pageable pageable);

    @Query("SELECT p FROM PositiveShare p WHERE p.userId = :userId AND p.status <> 'DELETED' ORDER BY p.createdAt DESC")
    Page<PositiveShare> findMyShares(@Param("userId") Long userId, Pageable pageable);

    long countByStatusAndPublicVisible(String status, Boolean publicVisible);

    @Query("SELECT p.id FROM PositiveShare p WHERE p.status = 'PUBLISHED' AND p.publicVisible = true")
    List<Long> findAllPublishedPublicIds();

    Page<PositiveShare> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    @Query("SELECT p FROM PositiveShare p WHERE p.status <> 'DELETED' ORDER BY p.createdAt DESC")
    Page<PositiveShare> findAllNonDeleted(Pageable pageable);
}
