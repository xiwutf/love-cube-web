package com.lovecube.backend.controllers;

import com.lovecube.backend.entity.AdminUserRole;
import com.lovecube.backend.entity.PlatformGroupAdmin;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.AdminUserRoleRepository;
import com.lovecube.backend.repository.PlatformGroupAdminRepository;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.GroupAdminRoleConstants;
import com.lovecube.backend.services.PermissionConstants;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色分配管理 —— 仅 SUPER_ADMIN 可操作
 */
@RestController
@RequestMapping("/api/admin/roles")
public class AdminRoleController {

    private final AdminUserRoleRepository adminUserRoleRepository;
    private final PlatformGroupAdminRepository platformGroupAdminRepository;
    private final UserRepository userRepository;
    private final AdminAuthService adminAuthService;

    public AdminRoleController(
            AdminUserRoleRepository adminUserRoleRepository,
            PlatformGroupAdminRepository platformGroupAdminRepository,
            UserRepository userRepository,
            AdminAuthService adminAuthService
    ) {
        this.adminUserRoleRepository = adminUserRoleRepository;
        this.platformGroupAdminRepository = platformGroupAdminRepository;
        this.userRepository = userRepository;
        this.adminAuthService = adminAuthService;
    }

    /** 查询某用户的角色列表 */
    @GetMapping("/users/{userId}")
    public Map<String, Object> getUserRoles(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long userId
    ) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.SYSTEM_MANAGE);
        List<AdminUserRole> roles = adminUserRoleRepository.findByUserId(userId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", userId);
        result.put("roles", roles.stream().map(AdminUserRole::getRoleCode).collect(Collectors.toList()));
        return result;
    }

    /** 为用户分配角色 */
    @PostMapping("/users/{userId}")
    public Map<String, Object> assignRole(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long userId,
            @RequestBody Map<String, Object> payload
    ) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.SYSTEM_MANAGE);
        String roleCode = String.valueOf(payload.getOrDefault("roleCode", "")).trim().toUpperCase();
        if (roleCode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "roleCode 不能为空");
        }
        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
        if (!adminUserRoleRepository.existsByUserIdAndRoleCode(userId, roleCode)) {
            AdminUserRole role = new AdminUserRole();
            role.setUserId(userId);
            role.setRoleCode(roleCode);
            adminUserRoleRepository.save(role);
        }
        return Map.of("userId", userId, "roleCode", roleCode, "message", "角色已分配");
    }

    /** 撤销用户角色 */
    @DeleteMapping("/users/{userId}/{roleCode}")
    public Map<String, Object> revokeRole(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long userId,
            @PathVariable String roleCode
    ) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.SYSTEM_MANAGE);
        adminUserRoleRepository.deleteByUserIdAndRoleCode(userId, roleCode.toUpperCase());
        return Map.of("userId", userId, "roleCode", roleCode, "message", "角色已撤销");
    }

    /** 为用户绑定团体管理权（GROUP_OWNER） */
    @PostMapping("/group-admins")
    public Map<String, Object> assignGroupAdmin(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> payload
    ) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.SYSTEM_MANAGE);
        Long userId = Long.valueOf(String.valueOf(payload.get("userId")));
        String groupId = String.valueOf(payload.get("groupId"));
        String role = String.valueOf(payload.getOrDefault("role", "OWNER")).toUpperCase();

        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));

        PlatformGroupAdmin ga = platformGroupAdminRepository
                .findByGroupIdAndUserId(groupId, userId)
                .orElseGet(PlatformGroupAdmin::new);
        ga.setGroupId(groupId);
        ga.setUserId(userId);
        ga.setRole(GroupAdminRoleConstants.normalize(role));
        platformGroupAdminRepository.save(ga);
        adminAuthService.ensureGroupOwnerRole(userId);
        return Map.of("userId", userId, "groupId", groupId, "message", "团体管理员已绑定");
    }

    /** 解除用户的团体管理权 */
    @DeleteMapping("/group-admins/{groupId}/{userId}")
    public Map<String, Object> revokeGroupAdmin(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String groupId,
            @PathVariable Long userId
    ) {
        adminAuthService.requirePermission(authHeader, PermissionConstants.SYSTEM_MANAGE);
        platformGroupAdminRepository.deleteByGroupIdAndUserId(groupId, userId);
        adminAuthService.cleanGroupOwnerRoleIfUnused(userId);
        return Map.of("userId", userId, "groupId", groupId, "message", "团体管理员已解除");
    }
}
