package com.lovecube.backend.services;

import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.utils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminAuthService {
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
        return user != null && "13800000000".equals(user.getPhoneNumber());
    }
}
