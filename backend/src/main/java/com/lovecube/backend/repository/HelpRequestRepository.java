package com.lovecube.backend.repository;

import com.lovecube.backend.entity.HelpRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface HelpRequestRepository extends JpaRepository<HelpRequest, Long> {

    @Query("""
            SELECT r FROM HelpRequest r
            WHERE r.status = 'active'
            AND (:helpType IS NULL OR :helpType = '' OR r.helpType = :helpType)
            ORDER BY r.createdAt DESC
            """)
    Page<HelpRequest> pageActivePublic(@Param("helpType") String helpType, Pageable pageable);

    Page<HelpRequest> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("""
            SELECT r FROM HelpRequest r
            WHERE (:status IS NULL OR :status = '' OR r.status = :status)
            ORDER BY r.createdAt DESC
            """)
    Page<HelpRequest> pageAdmin(@Param("status") String status, Pageable pageable);

    long countByStatus(String status);

    long countByCreatedAtGreaterThanEqual(LocalDateTime since);
}
