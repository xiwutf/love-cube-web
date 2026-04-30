package com.lovecube.backend.repository;

import com.lovecube.backend.entity.AdminRolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminRolePermissionRepository extends JpaRepository<AdminRolePermission, Long> {
    List<AdminRolePermission> findByRoleCode(String roleCode);
}
