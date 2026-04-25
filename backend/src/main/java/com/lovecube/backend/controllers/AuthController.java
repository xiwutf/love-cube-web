package com.lovecube.backend.controllers;

import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.services.FellowshipInviteService;
import com.lovecube.backend.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Set<String> BOOTSTRAP_ADMIN_PHONES = Set.of(
            "13800000000",
            "15030251407"
    );

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final FellowshipInviteService fellowshipInviteService;

    public AuthController(
            UserRepository userRepository,
            BCryptPasswordEncoder passwordEncoder,
            FellowshipInviteService fellowshipInviteService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.fellowshipInviteService = fellowshipInviteService;
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

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String phone = body.get("phone");
        String password = body.get("password");
        String username = body.get("username");
        String inviteCodeRaw = body.get("inviteCode");
        String inviteCode = inviteCodeRaw == null ? "" : inviteCodeRaw.trim().toUpperCase();

        if (phone == null || phone.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "手机号不能为空"));
        }
        if (password == null || password.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("message", "密码至少 6 位"));
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
            user.setUsername(username != null && !username.isEmpty() ? username : "用户" + phone.substring(phone.length() - 4));
            user.setOpenid("h5_tmp_" + UUID.randomUUID().toString().replace("-", ""));
            user.setInvitedByUserId(inviter == null ? null : inviter.getUserid());
            user.setRegisterIp(resolveClientIp(request));
            user.setRegisterUserAgent(trim(request.getHeader("User-Agent"), 500));
            user.setUserStatus("NORMAL");
            user.setInviteCodeStatus("ENABLED");

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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "注册失败，请稍后重试"));
        }
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
