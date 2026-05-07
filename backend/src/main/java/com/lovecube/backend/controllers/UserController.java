package com.lovecube.backend.controllers;

import com.lovecube.backend.entity.UserInteraction;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.DynamicLikeRepository;
import com.lovecube.backend.repository.DynamicRepository;
import com.lovecube.backend.repository.EventSignupRepository;
import com.lovecube.backend.repository.PositiveShareBookmarkRepository;
import com.lovecube.backend.repository.PositiveShareLikeRepository;
import com.lovecube.backend.repository.UserInteractionRepository;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.FellowshipInviteService;
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
    private final PositiveShareBookmarkRepository positiveShareBookmarkRepository;
    private final PositiveShareLikeRepository positiveShareLikeRepository;
    private final UserInteractionRepository userInteractionRepository;
    private final FellowshipInviteService fellowshipInviteService;

    public UserController(
            UserService userService,
            UserRepository userRepository,
            AdminAuthService adminAuthService,
            UnifiedProfileService unifiedProfileService,
            DynamicRepository dynamicRepository,
            EventSignupRepository eventSignupRepository,
            DynamicLikeRepository dynamicLikeRepository,
            PositiveShareBookmarkRepository positiveShareBookmarkRepository,
            PositiveShareLikeRepository positiveShareLikeRepository,
            UserInteractionRepository userInteractionRepository,
            FellowshipInviteService fellowshipInviteService
    ) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.adminAuthService = adminAuthService;
        this.unifiedProfileService = unifiedProfileService;
        this.dynamicRepository = dynamicRepository;
        this.eventSignupRepository = eventSignupRepository;
        this.dynamicLikeRepository = dynamicLikeRepository;
        this.positiveShareBookmarkRepository = positiveShareBookmarkRepository;
        this.positiveShareLikeRepository = positiveShareLikeRepository;
        this.userInteractionRepository = userInteractionRepository;
        this.fellowshipInviteService = fellowshipInviteService;
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

    @GetMapping("/user/invite-info")
    public ResponseEntity<?> getInviteInfo(
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader(value = "Origin", required = false) String origin
    ) {
        try {
            User currentUser = unifiedProfileService.requireCurrentUser(authHeader);
            return ResponseEntity.ok(fellowshipInviteService.getInviteInfo(currentUser, origin));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Failed to load invite info"));
        }
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
            long positiveShareBookmarkCount = positiveShareBookmarkRepository.countByUserId(userId);
            long positiveShareLikeCount = positiveShareLikeRepository.countByUserId(userId);
            long followingCount = userInteractionRepository.countByFromUserIdAndInteractionType(userId, UserInteraction.InteractionType.FOLLOW);
            long fansCount = userInteractionRepository.countByToUserIdAndInteractionType(userId, UserInteraction.InteractionType.FOLLOW);
            long likesReceived = userInteractionRepository.countByToUserIdAndInteractionType(userId, UserInteraction.InteractionType.LIKE);
            return ResponseEntity.ok(Map.of(
                    "contentCount", contentCount,
                    "eventCount", eventCount,
                    "favoriteCount", favoriteCount,
                    "positiveShareBookmarkCount", positiveShareBookmarkCount,
                    "positiveShareLikeCount", positiveShareLikeCount,
                    "followingCount", followingCount,
                    "fansCount", fansCount,
                    "likesReceived", likesReceived
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
            Map<String, Object> profile = unifiedProfileService.buildFellowshipPayload(currentUser);
            Integer age = profile.get("age") instanceof Number n ? n.intValue() : null;
            String maritalStatus = profile.get("maritalStatus") == null ? "" : String.valueOf(profile.get("maritalStatus")).trim();
            String avatarUrl = profile.get("avatarUrl") == null ? "" : String.valueOf(profile.get("avatarUrl")).trim();

            if (age == null || age <= 0) {
                Map<String, Object> body = new HashMap<>();
                body.put("message", "请先完善年龄后再开通联谊功能");
                body.put("code", "FELLOWSHIP_ACTIVATION_REQUIRES_AGE");
                return ResponseEntity.badRequest().body(body);
            }
            if (!"单身".equals(maritalStatus) && !"已婚".equals(maritalStatus) && !"离异".equals(maritalStatus)) {
                Map<String, Object> body = new HashMap<>();
                body.put("message", "请先选择婚姻状况（单身/已婚/离异）后再开通联谊功能");
                body.put("code", "FELLOWSHIP_ACTIVATION_REQUIRES_MARITAL_STATUS");
                return ResponseEntity.badRequest().body(body);
            }
            if (avatarUrl.isBlank()) {
                Map<String, Object> body = new HashMap<>();
                body.put("message", "请先上传头像后再开通联谊功能");
                body.put("code", "FELLOWSHIP_ACTIVATION_REQUIRES_AVATAR");
                return ResponseEntity.badRequest().body(body);
            }
            if (!unifiedProfileService.hasFellowshipLifePhotos(currentUser.getUserid())) {
                Map<String, Object> body = new HashMap<>();
                body.put("message", "请先至少上传一张生活照后再开通联谊功能");
                body.put("code", "FELLOWSHIP_ACTIVATION_REQUIRES_PHOTO");
                return ResponseEntity.badRequest().body(body);
            }
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
