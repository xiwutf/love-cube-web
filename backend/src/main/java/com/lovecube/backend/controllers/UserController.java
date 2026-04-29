package com.lovecube.backend.controllers;

import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.DynamicLikeRepository;
import com.lovecube.backend.repository.DynamicRepository;
import com.lovecube.backend.repository.EventSignupRepository;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.UnifiedProfileService;
import com.lovecube.backend.services.UserService;
import com.lovecube.backend.utils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final AdminAuthService adminAuthService;
    private final UnifiedProfileService unifiedProfileService;
    private final DynamicRepository dynamicRepository;
    private final EventSignupRepository eventSignupRepository;
    private final DynamicLikeRepository dynamicLikeRepository;

    public UserController(
            UserService userService,
            UserRepository userRepository,
            AdminAuthService adminAuthService,
            UnifiedProfileService unifiedProfileService,
            DynamicRepository dynamicRepository,
            EventSignupRepository eventSignupRepository,
            DynamicLikeRepository dynamicLikeRepository
    ) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.adminAuthService = adminAuthService;
        this.unifiedProfileService = unifiedProfileService;
        this.dynamicRepository = dynamicRepository;
        this.eventSignupRepository = eventSignupRepository;
        this.dynamicLikeRepository = dynamicLikeRepository;
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    @PutMapping("/users/profile")
    public ResponseEntity<?> updateUserProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> profileData
    ) {
        try {
            User currentUser = unifiedProfileService.requireCurrentUser(authHeader);
            unifiedProfileService.updateLegacyProfile(currentUser, profileData);
            return ResponseEntity.ok(Map.of("message", "资料更新成功"));
        } catch (IllegalArgumentException e) {
            HttpStatus status = isAuthError(e.getMessage()) ? HttpStatus.UNAUTHORIZED : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "更新失败: " + e.getMessage()));
        }
    }

    @GetMapping("/users/current")
    public ResponseEntity<User> getCurrentUser(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(userService.getCurrentUser(token));
    }

    @PostMapping("/users/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (user.getOpenid() == null || user.getOpenid().isEmpty()) {
            return ResponseEntity.badRequest().body("缺少 openid");
        }
        User existingUser = userRepository.findByOpenid(user.getOpenid());
        if (existingUser != null) {
            return ResponseEntity.badRequest().body("用户已注册，直接进入");
        }
        User savedUser = userRepository.save(user);
        String token = JwtUtil.generateToken(savedUser.getOpenid());
        Map<String, Object> response = new HashMap<>();
        response.put("userId", savedUser.getUserid());
        response.put("token", token);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/checkUserStatus")
    public ResponseEntity<Map<String, Object>> checkUserStatus(@RequestHeader("Authorization") String authHeader) {
        Map<String, Object> response = new HashMap<>();
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("registered", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        String token = authHeader.substring(7);
        String openid = JwtUtil.getOpenIdFromToken(token);
        if (openid == null) {
            response.put("registered", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        response.put("registered", userRepository.existsByOpenid(openid));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/me/stats")
    public ResponseEntity<?> getCurrentUserStats(@RequestHeader("Authorization") String authHeader) {
        try {
            User currentUser = unifiedProfileService.requireCurrentUser(authHeader);
            long userId = currentUser.getUserid();
            long contentCount = dynamicRepository.countByUserIdAndIsDeletedFalse(userId);
            long eventCount = eventSignupRepository.countByUserId(userId);
            long favoriteCount = dynamicLikeRepository.countByUserId(userId);
            return ResponseEntity.ok(Map.of(
                    "contentCount", contentCount,
                    "eventCount", eventCount,
                    "favoriteCount", favoriteCount
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "获取统计数据失败: " + e.getMessage()));
        }
    }

    @GetMapping("/users/me")
    public ResponseEntity<?> getCurrentUserInfo(@RequestHeader("Authorization") String authHeader) {
        try {
            User currentUser = unifiedProfileService.requireCurrentUser(authHeader);
            Map<String, Object> result = unifiedProfileService.buildLegacyUserPayload(currentUser);
            result.put("role", adminAuthService.isAdmin(currentUser) ? "admin" : "user");
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "获取用户信息失败: " + e.getMessage()));
        }
    }

    @PostMapping("/users/me/fellowship/activate")
    public ResponseEntity<?> activateFellowship(@RequestHeader("Authorization") String authHeader) {
        try {
            User currentUser = unifiedProfileService.requireCurrentUser(authHeader);
            currentUser.setFellowshipEnabled(true);
            currentUser.setFellowshipMatchVisible(true);
            userRepository.save(currentUser);
            Map<String, Object> result = unifiedProfileService.buildLegacyUserPayload(currentUser);
            result.put("role", adminAuthService.isAdmin(currentUser) ? "admin" : "user");
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "开通联谊模块失败: " + e.getMessage()));
        }
    }

    @PostMapping("/users/me/fellowship/deactivate")
    public ResponseEntity<?> deactivateFellowship(@RequestHeader("Authorization") String authHeader) {
        try {
            User currentUser = unifiedProfileService.requireCurrentUser(authHeader);
            currentUser.setFellowshipEnabled(false);
            currentUser.setFellowshipMatchVisible(false);
            userRepository.save(currentUser);
            Map<String, Object> result = unifiedProfileService.buildLegacyUserPayload(currentUser);
            result.put("role", adminAuthService.isAdmin(currentUser) ? "admin" : "user");
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "关闭联谊模块失败: " + e.getMessage()));
        }
    }

    @PostMapping("/users/me/fellowship/match-visibility")
    public ResponseEntity<?> updateMyFellowshipMatchVisibility(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> payload
    ) {
        try {
            User currentUser = unifiedProfileService.requireCurrentUser(authHeader);
            boolean visible = Boolean.parseBoolean(String.valueOf(payload.getOrDefault("visible", false)));
            if (!Boolean.TRUE.equals(currentUser.getFellowshipEnabled()) && visible) {
                return ResponseEntity.badRequest().body(Map.of("message", "请先开通联谊模块"));
            }
            currentUser.setFellowshipMatchVisible(visible);
            userRepository.save(currentUser);
            Map<String, Object> result = unifiedProfileService.buildLegacyUserPayload(currentUser);
            result.put("role", adminAuthService.isAdmin(currentUser) ? "admin" : "user");
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "更新匹配列表可见性失败: " + e.getMessage()));
        }
    }

    private boolean isAuthError(String message) {
        if (message == null) {
            return false;
        }
        return "未登录".equals(message) || "登录凭证无效".equals(message) || "用户不存在".equals(message);
    }
}
