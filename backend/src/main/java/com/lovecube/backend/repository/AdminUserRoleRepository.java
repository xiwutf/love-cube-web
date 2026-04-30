package com.lovecube.backend.repository;

import com.lovecube.backend.entity.AdminUserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminUserRoleRepository extends JpaRepository<AdminUserRole, Long> {
    List<AdminUserRole> findByUserId(Long userId);
    boolean existsByUserIdAndRoleCode(Long userId, String roleCode);
    void deleteByUserIdAndRoleCode(Long userId, String roleCode);
}
