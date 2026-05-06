package com.lovecube.backend.controllers;

import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.FellowshipInviteService;
import com.lovecube.backend.services.GrowthService;
import com.lovecube.backend.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
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
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private static final int USERNAME_MAX_LENGTH = 20;

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final FellowshipInviteService fellowshipInviteService;
    private final AdminAuthService adminAuthService;
    private final GrowthService growthService;

    public AuthController(
            UserRepository userRepository,
            BCryptPasswordEncoder passwordEncoder,
            FellowshipInviteService fellowshipInviteService,
            AdminAuthService adminAuthService,
            GrowthService growthService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.fellowshipInviteService = fellowshipInviteService;
        this.adminAuthService = adminAuthService;
        this.growthService = growthService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String email = body.get("email");
        String password = body.get("password");

        if (password == null || password.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Password cannot be empty"));
        }

        User user;
        if (phone != null && !phone.isEmpty()) {
            user = userRepository.findByPhoneNumber(phone);
        } else if (email != null && !email.isEmpty()) {
            user = userRepository.findByEmail(email);
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "Phone or email is required"));
        }

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "User does not exist"));
        }

        if (user.getPasswordHash() == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Incorrect password"));
        }

        if (user.getOpenid() == null || user.getOpenid().isEmpty()) {
            user.setOpenid("h5_" + user.getUserid());
            userRepository.save(user);
        }

        String token = JwtUtil.generateToken(user.getOpenid());
        growthService.recordAction(user.getUserid(), "LOGIN", "LOGIN_" + java.time.LocalDate.now());
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
        String gender = body.get("gender");
        String inviteCodeRaw = body.get("inviteCode");
        String inviteCode = inviteCodeRaw == null ? "" : inviteCodeRaw.trim().toUpperCase();
        String normalizedUsername;
        Integer genderCode;

        if (phone == null || phone.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Phone cannot be empty"));
        }
        if (password == null || password.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("message", "Password must be at least 6 characters"));
        }
        try {
            normalizedUsername = normalizeUsername(username);
            genderCode = parseRegisterGender(gender);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
        if (userRepository.findByPhoneNumber(phone) != null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Phone number is already registered"));
        }

        User inviter = null;
        if (!inviteCode.isEmpty()) {
            try {
                inviter = fellowshipInviteService.validateInviteCodeForRegistration(inviteCode);
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid invite code"));
            }
        }

        try {
            User user = new User();
            user.setPhoneNumber(phone);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setUsername(normalizedUsername != null ? normalizedUsername : "User" + phone.substring(phone.length() - 4));
            user.setOpenid("h5_tmp_" + UUID.randomUUID().toString().replace("-", ""));
            user.setInvitedByUserId(inviter == null ? null : inviter.getUserid());
            user.setRegisterIp(resolveClientIp(request));
            user.setRegisterUserAgent(trim(request.getHeader("User-Agent"), 500));
            user.setUserStatus("NORMAL");
            user.setInviteCodeStatus("ENABLED");
            user.setGender(genderCode);
            user.setFellowshipEnabled(false);
            user.setFellowshipMatchVisible(false);

            User saved = userRepository.save(user);
            saved.setOpenid("h5_" + saved.getUserid());
            fellowshipInviteService.ensureUserInviteCode(saved);
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
        } catch (DataIntegrityViolationException ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            Throwable root = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause() : ex;
            log.warn("Register failed: data integrity — phone={}, cause={}", phone, root.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message",
                    "Registration failed: data conflict. If this phone is new, try again later or contact support."
            ));
        } catch (IllegalStateException ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.warn("Register failed: illegal state — phone={}, error={}", phone, ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message",
                    "Invite code could not be allocated. Please try again in a moment."
            ));
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("Register failed: unexpected — phone={}", phone, ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Registration failed, please try again"));
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
            return ResponseEntity.badRequest().body(Map.of("message", "Old password cannot be empty"));
        }
        if (newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "New password cannot be empty"));
        }
        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("message", "New password must be at least 6 characters"));
        }
        if (confirmPassword == null || !newPassword.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Passwords do not match"));
        }
        if (newPassword.equals(oldPassword)) {
            return ResponseEntity.badRequest().body(Map.of("message", "New password cannot match old password"));
        }

        User user = adminAuthService.requireUser(authHeader);
        if (user.getPasswordHash() == null || !passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Old password is incorrect"));
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Password updated"));
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
            throw new IllegalArgumentException("Username cannot exceed 20 characters");
        }
        return username;
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

    private Integer parseRegisterGender(String rawGender) {
        String value = rawGender == null ? "" : rawGender.trim().toLowerCase();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Gender cannot be empty");
        }
        if (Set.of("male", "man", "m", "1").contains(value)) {
            return 1;
        }
        if (Set.of("female", "woman", "f", "2").contains(value)) {
            return 2;
        }
        throw new IllegalArgumentException("Invalid gender");
    }
}
