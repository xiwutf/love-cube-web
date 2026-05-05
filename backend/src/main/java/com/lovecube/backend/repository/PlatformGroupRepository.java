package com.lovecube.backend.repository;

import com.lovecube.backend.entity.PlatformGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface PlatformGroupRepository extends JpaRepository<PlatformGroup, String> {

    long countByStatus(String status);

    long countByCreatedAtGreaterThanEqual(LocalDateTime since);

    @Query("SELECT g FROM PlatformGroup g WHERE g.status = :status ORDER BY g.pinned DESC, g.createdAt DESC")
    List<PlatformGroup> findByStatusOrderByPinnedDescCreatedAtDesc(String status);

    @Query("SELECT g FROM PlatformGroup g ORDER BY g.pinned DESC, g.createdAt DESC")
    List<PlatformGroup> findAllOrderByPinnedDescCreatedAtDesc();
}
