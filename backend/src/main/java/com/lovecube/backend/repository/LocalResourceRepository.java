package com.lovecube.backend.repository;

import com.lovecube.backend.entity.LocalResource;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocalResourceRepository extends JpaRepository<LocalResource, Long> {
    List<LocalResource> findByStatusOrderByHeatDescUpdatedAtDesc(String status);

    @Query("SELECT COUNT(l) FROM LocalResource l WHERE LOWER(COALESCE(l.status, 'draft')) IN ('draft', 'pending', 'reviewing')")
    long countPendingForAdmin();
}
