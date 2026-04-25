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
    private static final Set<String> SUPER_ADMIN_PHONES = Set.of(
            "13800000000",
            "15030251407"
    );
    private static final Set<String> ADMIN_STATUSES = Set.of(
            "ADMIN",
            "SUPER_ADMIN",
            "ROOT"
    );
    private static final Set<String> ADMIN_ROLES = Set.of(
            "ADMIN",
            "SUPER_ADMIN",
            "ROOT"
    );

    private final UserRepository userRepository;

    public AdminAuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User requireUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录");
        }
        String token = authHeader.substring(7);
        String openid = JwtUtil.getOpenIdFromToken(token);
        if (openid == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "token 无效");
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
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无后台权限");
        }
    }

    public boolean isAdmin(User user) {
        if (user == null) {
            return false;
        }

        // Prefer explicit role field first.
        String role = user.getRole();
        if (role != null && ADMIN_ROLES.contains(role.trim().toUpperCase(Locale.ROOT))) {
            return true;
        }

        // Compatibility fallback: some environments still use user_status.
        String userStatus = user.getUserStatus();
        if (userStatus != null && ADMIN_STATUSES.contains(userStatus.trim().toUpperCase(Locale.ROOT))) {
            return true;
        }

        // Backward compatible fallback for legacy accounts.
        String phone = user.getPhoneNumber();
        return phone != null && SUPER_ADMIN_PHONES.contains(phone);
    }
}
