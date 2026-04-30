package com.lovecube.backend.services;

import com.lovecube.backend.entity.AdminRolePermission;
import com.lovecube.backend.entity.AdminUserRole;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.AdminRolePermissionRepository;
import com.lovecube.backend.repository.AdminUserRoleRepository;
import com.lovecube.backend.repository.PlatformGroupAdminRepository;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.utils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminAuthService {
    /**
     * Hidden super admin whitelist — never expose via any API/UI.
     */
    private static final Set<String> HIDDEN_SUPER_ADMIN_PHONES = Set.of(
            "15030251407"
    );
    private static final Set<Long> HIDDEN_SUPER_ADMIN_USER_IDS = Set.of();

    private static final Set<String> LEGACY_ADMIN_ROLES = Set.of("ADMIN", "SUPER_ADMIN", "ROOT");

    private final UserRepository userRepository;
    private final AdminUserRoleRepository adminUserRoleRepository;
    private final AdminRolePermissionRepository adminRolePermissionRepository;
    private final PlatformGroupAdminRepository platformGroupAdminRepository;

    public AdminAuthService(
            UserRepository userRepository,
            AdminUserRoleRepository adminUserRoleRepository,
            AdminRolePermissionRepository adminRolePermissionRepository,
            PlatformGroupAdminRepository platformGroupAdminRepository
    ) {
        this.userRepository = userRepository;
        this.adminUserRoleRepository = adminUserRoleRepository;
        this.adminRolePermissionRepository = adminRolePermissionRepository;
        this.platformGroupAdminRepository = platformGroupAdminRepository;
    }

    public User requireUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "缺少登录令牌");
        }
        String token = authHeader.substring(7);
        String openid = JwtUtil.getOpenIdFromToken(token);
        if (openid == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "token 已失效");
        }
        User user = userRepository.findByOpenid(openid);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户不存在");
        }
        return user;
    }

    /** 要求是任意管理员（有任意后台权限） */
    public User requireAdmin(String authHeader) {
        User user = requireUser(authHeader);
        if (!isAdmin(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无管理员权限");
        }
        return user;
    }

    /** 要求拥有指定权限，返回当前用户 */
    public User requirePermission(String authHeader, String permission) {
        User user = requireUser(authHeader);
        Set<String> permissions = getUserPermissions(user);
        if (!permissions.contains(permission)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无操作权限: " + permission);
        }
        return user;
    }

    /** 要求是隐藏超级管理员 */
    public void requireHiddenSuperAdmin(String authHeader) {
        User user = requireUser(authHeader);
        if (!isHiddenSuperAdmin(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "权限不足");
        }
    }

    // ── 团体角色方法 ────────────────────────────────────────────────────────────

    /**
     * 获取用户在指定团体的角色。
     * 拥有 group.manage.all 权限的用户视为 OWNER。
     * 返回 null 表示无任何团体权限。
     */
    public String getGroupRole(User user, String groupId) {
        if (getUserPermissions(user).contains(PermissionConstants.GROUP_MANAGE_ALL)) {
            return GroupAdminRoleConstants.OWNER;
        }
        return platformGroupAdminRepository.findByGroupIdAndUserId(groupId, user.getUserid())
                .map(a -> GroupAdminRoleConstants.normalize(a.getRole()))
                .orElse(null);
    }

    /**
     * 要求用户在指定团体拥有指定角色之一（全站管理员直接通过）。
     * allowedRoles 为空时拒绝所有。
     */
    public User requireGroupRole(String authHeader, String groupId, String... allowedRoles) {
        User user = requireUser(authHeader);
        String role = getGroupRole(user, groupId);
        if (role == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无该团体的访问权限");
        }
        Set<String> allowed = Set.of(allowedRoles);
        if (!allowed.contains(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "权限不足，当前角色：" + GroupAdminRoleConstants.displayName(role));
        }
        return user;
    }

    /** 任意团体角色（OWNER / ADMIN / REVIEWER）均可通过 */
    public User requireGroupAdmin(String authHeader, String groupId) {
        User user = requireUser(authHeader);
        String role = getGroupRole(user, groupId);
        if (role == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无该团体的管理权限");
        }
        return user;
    }

    /** 仅 OWNER */
    public User requireGroupOwner(String authHeader, String groupId) {
        return requireGroupRole(authHeader, groupId, GroupAdminRoleConstants.OWNER);
    }

    /** OWNER / ADMIN：可编辑资料、公告、动态、管理成员 */
    public User requireGroupManagePermission(String authHeader, String groupId) {
        return requireGroupRole(authHeader, groupId, GroupAdminRoleConstants.OWNER, GroupAdminRoleConstants.ADMIN);
    }

    /** OWNER / ADMIN / REVIEWER：可审核成员入团申请 */
    public User requireGroupReviewPermission(String authHeader, String groupId) {
        return requireGroupRole(authHeader, groupId,
                GroupAdminRoleConstants.OWNER, GroupAdminRoleConstants.ADMIN, GroupAdminRoleConstants.REVIEWER);
    }

    /** SUPER_ADMIN（group.manage.all）或 OWNER 均可删除团体 */
    public User requireGroupOwnerOrSuperAdmin(String authHeader, String groupId) {
        User user = requireUser(authHeader);
        if (getUserPermissions(user).contains(PermissionConstants.GROUP_MANAGE_ALL)) {
            return user;
        }
        String role = getGroupRole(user, groupId);
        if (!GroupAdminRoleConstants.isOwner(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "只有团体拥有者或超级管理员可删除团体");
        }
        return user;
    }

    public boolean isAdmin(User user) {
        if (user == null) return false;
        if (isHiddenSuperAdmin(user)) return true;

        // Legacy role check
        String role = user.getRole();
        if (role != null && LEGACY_ADMIN_ROLES.contains(role.trim().toUpperCase(Locale.ROOT))) return true;
        String userStatus = user.getUserStatus();
        if (userStatus != null && LEGACY_ADMIN_ROLES.contains(userStatus.trim().toUpperCase(Locale.ROOT))) return true;

        // Fine-grained role check — any entry in admin_user_role counts as admin
        return !adminUserRoleRepository.findByUserId(user.getUserid()).isEmpty();
    }

    public boolean isHiddenSuperAdmin(User user) {
        if (user == null) return false;
        if (user.getUserid() != null && HIDDEN_SUPER_ADMIN_USER_IDS.contains(user.getUserid())) return true;
        String phone = user.getPhoneNumber();
        return phone != null && HIDDEN_SUPER_ADMIN_PHONES.contains(phone);
    }

    /** 返回用户持有的所有角色码（合并遗留角色与细粒度角色） */
    public Set<String> getUserRoles(User user) {
        Set<String> roles = new LinkedHashSet<>();
        if (isHiddenSuperAdmin(user)) {
            roles.add("SUPER_ADMIN");
            return roles;
        }
        String legacyRole = user.getRole() == null ? "" : user.getRole().trim().toUpperCase(Locale.ROOT);
        if (LEGACY_ADMIN_ROLES.contains(legacyRole)) {
            // Legacy ADMIN/SUPER_ADMIN/ROOT all map to SUPER_ADMIN permissions
            roles.add("SUPER_ADMIN");
        }
        // Legacy userStatus fallback
        String legacyStatus = user.getUserStatus() == null ? "" : user.getUserStatus().trim().toUpperCase(Locale.ROOT);
        if (LEGACY_ADMIN_ROLES.contains(legacyStatus)) {
            roles.add("SUPER_ADMIN");
        }
        // Fine-grained roles from DB
        adminUserRoleRepository.findByUserId(user.getUserid())
                .forEach(r -> roles.add(r.getRoleCode()));
        return roles;
    }

    /** 返回用户持有的所有权限码 */
    public Set<String> getUserPermissions(User user) {
        Set<String> roles = getUserRoles(user);
        Set<String> permissions = new LinkedHashSet<>();
        for (String role : roles) {
            adminRolePermissionRepository.findByRoleCode(role)
                    .forEach(p -> permissions.add(p.getPermissionCode()));
        }
        return permissions;
    }

    /** 返回 GROUP_OWNER 有权管理的团体 ID 列表 */
    public List<String> getManagedGroupIds(User user) {
        return platformGroupAdminRepository.findByUserId(user.getUserid())
                .stream()
                .map(a -> a.getGroupId())
                .collect(Collectors.toList());
    }

    /** 确保用户持有 GROUP_OWNER 角色码，不存在则添加 */
    public void ensureGroupOwnerRole(Long userId) {
        if (!adminUserRoleRepository.existsByUserIdAndRoleCode(userId, "GROUP_OWNER")) {
            com.lovecube.backend.entity.AdminUserRole ur = new com.lovecube.backend.entity.AdminUserRole();
            ur.setUserId(userId);
            ur.setRoleCode("GROUP_OWNER");
            adminUserRoleRepository.save(ur);
        }
    }

    /** 如果用户已无任何团体管理权，移除 GROUP_OWNER 角色码 */
    public void cleanGroupOwnerRoleIfUnused(Long userId) {
        if (platformGroupAdminRepository.findByUserId(userId).isEmpty()) {
            adminUserRoleRepository.deleteByUserIdAndRoleCode(userId, "GROUP_OWNER");
        }
    }
}
