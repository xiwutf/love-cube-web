package com.lovecube.backend.repository;

import com.lovecube.backend.entity.LocalResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocalResourceRepository extends JpaRepository<LocalResource, Long> {
    List<LocalResource> findByStatusOrderByHeatDescUpdatedAtDesc(String status);
}
