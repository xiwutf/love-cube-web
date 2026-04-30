package com.lovecube.backend.controllers;

import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.FellowshipInviteService;
import com.lovecube.backend.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final int USERNAME_MAX_LENGTH = 20;
    private static final Set<String> BOOTSTRAP_ADMIN_PHONES = Set.of(
            "15030251407"
    );

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final FellowshipInviteService fellowshipInviteService;
    private final AdminAuthService adminAuthService;

    public AuthController(
            UserRepository userRepository,
            BCryptPasswordEncoder passwordEncoder,
            FellowshipInviteService fellowshipInviteService,
            AdminAuthService adminAuthService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.fellowshipInviteService = fellowshipInviteService;
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String email = body.get("email");
        String password = body.get("password");

        if (password == null || password.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "密码不能为空"));
        }

        User user = null;
        if (phone != null && !phone.isEmpty()) {
            user = userRepository.findByPhoneNumber(phone);
        } else if (email != null && !email.isEmpty()) {
            user = userRepository.findByEmail(email);
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "请填写手机号或邮箱"));
        }

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户不存在，请先注册"));
        }

        if (user.getPasswordHash() == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "密码错误"));
        }

        if (user.getOpenid() == null || user.getOpenid().isEmpty()) {
            user.setOpenid("h5_" + user.getUserid());
            userRepository.save(user);
        }

        String token = JwtUtil.generateToken(user.getOpenid());
        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getUserid());
        result.put("token", token);
        return ResponseEntity.ok(result);
    }

    @Transactional
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String phone = body.get("phone");
        String password = body.get("password");
        String username = body.get("username");
        String inviteCodeRaw = body.get("inviteCode");
        String inviteCode = inviteCodeRaw == null ? "" : inviteCodeRaw.trim().toUpperCase();
        String normalizedUsername;

        if (phone == null || phone.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "手机号不能为空"));
        }
        if (password == null || password.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("message", "密码至少 6 位"));
        }
        try {
            normalizedUsername = normalizeUsername(username);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
        if (userRepository.findByPhoneNumber(phone) != null) {
            return ResponseEntity.badRequest().body(Map.of("message", "该手机号已注册"));
        }

        boolean allowRegisterWithoutInvite = isBootstrapAdmin(phone) || userRepository.count() == 0;
        User inviter = null;
        if (!allowRegisterWithoutInvite) {
            if (inviteCode.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "邀请码不能为空"));
            }
            try {
                inviter = fellowshipInviteService.validateInviteCodeForRegistration(inviteCode);
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest().body(Map.of("message", "邀请码无效"));
            }
        }

        try {
            User user = new User();
            user.setPhoneNumber(phone);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setUsername(normalizedUsername != null ? normalizedUsername : "用户" + phone.substring(phone.length() - 4));
            user.setOpenid("h5_tmp_" + UUID.randomUUID().toString().replace("-", ""));
            user.setInvitedByUserId(inviter == null ? null : inviter.getUserid());
            user.setRegisterIp(resolveClientIp(request));
            user.setRegisterUserAgent(trim(request.getHeader("User-Agent"), 500));
            user.setUserStatus("NORMAL");
            user.setInviteCodeStatus("ENABLED");
            user.setFellowshipEnabled(false);
            user.setFellowshipMatchVisible(false);

            User saved = userRepository.save(user);
            saved.setOpenid("h5_" + saved.getUserid());
            saved.setInviteCode(fellowshipInviteService.generateUniqueInviteCode(saved.getUserid()));
            userRepository.save(saved);

            if (inviter != null) {
                fellowshipInviteService.createSuccessRecord(
                        inviteCode,
                        inviter,
                        saved,
                        saved.getRegisterIp(),
                        saved.getRegisterUserAgent()
                );
            }

            String token = JwtUtil.generateToken(saved.getOpenid());
            Map<String, Object> result = new HashMap<>();
            result.put("userId", saved.getUserid());
            result.put("token", token);
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "注册失败，请稍后重试"));
        }
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> body
    ) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        String confirmPassword = body.get("confirmPassword");

        if (oldPassword == null || oldPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "旧密码不能为空"));
        }
        if (newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "新密码不能为空"));
        }
        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("message", "新密码至少 6 位"));
        }
        if (confirmPassword == null || !newPassword.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body(Map.of("message", "两次输入的新密码不一致"));
        }
        if (newPassword.equals(oldPassword)) {
            return ResponseEntity.badRequest().body(Map.of("message", "新密码不能与旧密码相同"));
        }

        User user = adminAuthService.requireUser(authHeader);
        if (user.getPasswordHash() == null || !passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "旧密码不正确"));
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "密码修改成功"));
    }

    private String normalizeUsername(String rawUsername) {
        if (rawUsername == null) {
            return null;
        }
        String username = rawUsername.trim();
        if (username.isEmpty()) {
            return null;
        }
        if (username.length() > USERNAME_MAX_LENGTH) {
            throw new IllegalArgumentException("昵称最多 20 个字符");
        }
        return username;
    }

    private boolean isBootstrapAdmin(String phone) {
        return BOOTSTRAP_ADMIN_PHONES.contains(phone);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String[] headerCandidates = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP"
        };
        for (String header : headerCandidates) {
            String value = request.getHeader(header);
            if (value != null && !value.isBlank() && !"unknown".equalsIgnoreCase(value)) {
                if (value.contains(",")) {
                    return value.split(",")[0].trim();
                }
                return trim(value, 64);
            }
        }
        return trim(request.getRemoteAddr(), 64);
    }

    private String trim(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String text = value.trim();
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }
}
