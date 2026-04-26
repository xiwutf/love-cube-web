package com.lovecube.backend.controllers;

import com.lovecube.backend.entity.UserFeedback;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.UserFeedbackRepository;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.utils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {
    private final UserRepository userRepository;
    private final UserFeedbackRepository userFeedbackRepository;

    public FeedbackController(
            UserRepository userRepository,
            UserFeedbackRepository userFeedbackRepository
    ) {
        this.userRepository = userRepository;
        this.userFeedbackRepository = userFeedbackRepository;
    }

    @PostMapping("")
    public ResponseEntity<?> submitFeedback(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> payload
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "未提供或格式错误的 token"));
        }

        String token = authHeader.substring(7);
        String openid = JwtUtil.getOpenIdFromToken(token);
        if (openid == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "token 无效"));
        }

        User user = userRepository.findByOpenid(openid);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户不存在"));
        }

        String content = safe(payload.get("content"), 1000);
        if (content.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "反馈内容不能为空"));
        }
        String contact = safe(payload.get("contact"), 128);

        UserFeedback record = new UserFeedback();
        record.setId("feedback-" + UUID.randomUUID());
        record.setUserId(user.getUserid());
        record.setUsername(resolveUsername(user));
        record.setContact(contact);
        record.setContent(content);
        record.setStatus("pending");
        userFeedbackRepository.save(record);

        Map<String, Object> result = new HashMap<>();
        result.put("id", record.getId());
        result.put("message", "反馈提交成功");
        return ResponseEntity.ok(result);
    }

    private String resolveUsername(User user) {
        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            return user.getUsername().trim();
        }
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isBlank()) {
            return "用户" + user.getPhoneNumber().substring(Math.max(0, user.getPhoneNumber().length() - 4));
        }
        return "用户" + user.getUserid();
    }

    private String safe(Object value, int maxLen) {
        if (value == null) {
            return "";
        }
        String text = String.valueOf(value).trim();
        if (text.length() <= maxLen) {
            return text;
        }
        return text.substring(0, maxLen);
    }
}
