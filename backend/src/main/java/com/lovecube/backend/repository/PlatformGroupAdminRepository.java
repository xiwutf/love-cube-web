package com.lovecube.backend.repository;

import com.lovecube.backend.entity.PlatformGroupAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlatformGroupAdminRepository extends JpaRepository<PlatformGroupAdmin, Long> {
    List<PlatformGroupAdmin> findByUserId(Long userId);
    List<PlatformGroupAdmin> findByGroupId(String groupId);
    Optional<PlatformGroupAdmin> findByGroupIdAndUserId(String groupId, Long userId);
    boolean existsByGroupIdAndUserId(String groupId, Long userId);
    void deleteByGroupIdAndUserId(String groupId, Long userId);
}
