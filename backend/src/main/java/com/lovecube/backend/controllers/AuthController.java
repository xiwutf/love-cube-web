package com.lovecube.backend.controllers;

import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * H5 端账号密码登录/注册接口
 * 微信小程序仍走 /api/wechat/login，两套登录互不干扰。
 *
 * JWT 复用同一套密钥和 subject=openid 的机制：
 * H5 用户若无微信 openid，则在首次登录时写入虚拟 openid（h5_<userId>），
 * 后续所有依赖 openid 的接口无需任何改动。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * H5 登录
     * Body: { "phone": "138xxxx", "password": "xxx" }
     *    或 { "email": "xx@xx.com", "password": "xxx" }
     * 返回: { "userId": 1, "token": "xxx" }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String phone    = body.get("phone");
        String email    = body.get("email");
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

        // 确保 H5 用户有 openid（复用已有 JWT 机制，无需改其他接口）
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

    /**
     * H5 注册
     * Body: { "phone": "138xxxx", "password": "xxx", "username": "可选昵称" }
     * 返回: { "userId": 1, "token": "xxx" }
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String phone    = body.get("phone");
        String password = body.get("password");
        String username = body.get("username");

        if (phone == null || phone.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "手机号不能为空"));
        }
        if (password == null || password.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("message", "密码至少 6 位"));
        }
        if (userRepository.findByPhoneNumber(phone) != null) {
            return ResponseEntity.badRequest().body(Map.of("message", "该手机号已注册"));
        }

        User user = new User();
        user.setPhoneNumber(phone);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setUsername(username != null && !username.isEmpty()
                ? username
                : "用户" + phone.substring(phone.length() - 4));
        // openid NOT NULL 约束：先用 UUID 占位，save 后拿到 userId 再更新为稳定值
        user.setOpenid("h5_tmp_" + UUID.randomUUID().toString().replace("-", ""));
        User saved = userRepository.save(user);

        saved.setOpenid("h5_" + saved.getUserid());
        userRepository.save(saved);

        String token = JwtUtil.generateToken(saved.getOpenid());
        Map<String, Object> result = new HashMap<>();
        result.put("userId", saved.getUserid());
        result.put("token", token);
        return ResponseEntity.ok(result);
    }
}
