package com.lovecube.backend.services;

import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.utils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Set;

@Service
public class AdminAuthService {
    /**
     * Hidden super admin whitelist.
     * Keep this list private and do not expose it via any API/UI.
     */
    private static final Set<String> HIDDEN_SUPER_ADMIN_PHONES = Set.of(
            "15030251407"
    );
    private static final Set<Long> HIDDEN_SUPER_ADMIN_USER_IDS = Set.of();

    private static final Set<String> ADMIN_STATUSES = Set.of("ADMIN", "SUPER_ADMIN", "ROOT");
    private static final Set<String> ADMIN_ROLES = Set.of("ADMIN", "SUPER_ADMIN", "ROOT");

    private final UserRepository userRepository;

    public AdminAuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
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

    public void requireAdmin(String authHeader) {
        User user = requireUser(authHeader);
        if (!isAdmin(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无管理员权限");
        }
    }

    public void requireHiddenSuperAdmin(String authHeader) {
        User user = requireUser(authHeader);
        if (!isHiddenSuperAdmin(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "权限不足");
        }
    }

    public boolean isAdmin(User user) {
        if (user == null) {
            return false;
        }

        if (isHiddenSuperAdmin(user)) {
            return true;
        }

        String role = user.getRole();
        if (role != null && ADMIN_ROLES.contains(role.trim().toUpperCase(Locale.ROOT))) {
            return true;
        }

        String userStatus = user.getUserStatus();
        return userStatus != null && ADMIN_STATUSES.contains(userStatus.trim().toUpperCase(Locale.ROOT));
    }

    public boolean isHiddenSuperAdmin(User user) {
        if (user == null) {
            return false;
        }
        if (user.getUserid() != null && HIDDEN_SUPER_ADMIN_USER_IDS.contains(user.getUserid())) {
            return true;
        }
        String phone = user.getPhoneNumber();
        return phone != null && HIDDEN_SUPER_ADMIN_PHONES.contains(phone);
    }
}
