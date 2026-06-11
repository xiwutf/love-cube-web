package com.lovecube.backend.repository;

import com.lovecube.backend.entity.PlatGroupPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PlatGroupPostRepository extends JpaRepository<PlatGroupPost, Long> {

    List<PlatGroupPost> findByGroupIdAndStatusOrderByCreatedAtDesc(Long groupId, String status);

    Page<PlatGroupPost> findByGroupIdAndStatusOrderByCreatedAtDesc(Long groupId, String status, Pageable pageable);

    List<PlatGroupPost> findTop20ByStatusOrderByCreatedAtDesc(String status);

    long countByCreatedAtGreaterThanEqual(LocalDateTime since);

    long countByGroupIdAndStatusAndCreatedAtGreaterThanEqual(
            Long groupId, String status, LocalDateTime since);

    @Query("""
            SELECT DISTINCT p.userId FROM PlatGroupPost p
            WHERE p.groupId = :groupId AND p.status = 'published' AND p.createdAt >= :since
            """)
    List<Long> findDistinctAuthorIdsSince(
            @Param("groupId") Long groupId,
            @Param("since") LocalDateTime since);

    @Query("""
            SELECT DISTINCT p.userId FROM PlatGroupPost p
            WHERE p.groupId = :groupId AND p.status = 'published'
            """)
    List<Long> findDistinctAuthorIdsAllTime(@Param("groupId") Long groupId);

    @Query("""
            SELECT DISTINCT p.userId FROM PlatGroupPost p
            WHERE p.groupId = :groupId AND p.status = 'published'
            AND p.createdAt >= :start AND p.createdAt < :end
            """)
    List<Long> findDistinctAuthorIdsBetween(
            @Param("groupId") Long groupId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
