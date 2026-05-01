package com.lovecube.backend.controllers;

import com.lovecube.backend.entity.UserFeedback;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.UserFeedbackRepository;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.services.GrowthService;
import com.lovecube.backend.utils.JwtUtil;
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
    private final GrowthService growthService;

    public FeedbackController(
            UserRepository userRepository,
            UserFeedbackRepository userFeedbackRepository,
            GrowthService growthService
    ) {
        this.userRepository = userRepository;
        this.userFeedbackRepository = userFeedbackRepository;
        this.growthService = growthService;
    }

    @PostMapping({"", "/submit"})
    public ResponseEntity<?> submitFeedback(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> payload
    ) {
        User user = resolveCurrentUser(authHeader);
        String module = safe(payload.get("module"), 128);
        String goals = safe(payload.get("goals"), 256);
        String missing = safe(payload.get("missing"), 128);
        String content = safe(payload.get("content"), 1000);
        if (content.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "反馈内容不能为空"));
        }
        String contact = safe(payload.get("contact"), 128);

        UserFeedback record = new UserFeedback();
        record.setId("feedback-" + UUID.randomUUID());
        record.setUserId(user == null ? 0L : user.getUserid());
        record.setUsername(user == null ? "共创访客" : resolveUsername(user));
        record.setContact(contact);
        record.setContent(buildQuestionnaireContent(module, goals, missing, content));
        record.setStatus("pending");
        userFeedbackRepository.save(record);

        int awardedExp = 0;
        if (user != null) {
            Map<String, Object> growthResult = growthService.recordAction(user.getUserid(), "FEEDBACK_REPORT", record.getId());
            Object expObj = growthResult.get("exp");
            if (Boolean.TRUE.equals(growthResult.get("recorded")) && expObj instanceof Number num) {
                awardedExp = num.intValue();
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", record.getId());
        result.put("message", "感谢参与平台共建");
        result.put("awardedExp", awardedExp);
        return ResponseEntity.ok(result);
    }

    private User resolveCurrentUser(String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return null;
            }
            String token = authHeader.substring(7);
            String openid = JwtUtil.getOpenIdFromToken(token);
            if (openid == null) {
                return null;
            }
            return userRepository.findByOpenid(openid);
        } catch (Exception e) {
            return null;
        }
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

    private String buildQuestionnaireContent(String module, String goals, String missing, String content) {
        StringBuilder builder = new StringBuilder();
        if (!module.isBlank()) {
            builder.append("Q1-最关注模块：").append(module).append('\n');
        }
        if (!goals.isBlank()) {
            builder.append("Q2-来站目标：").append(goals).append('\n');
        }
        if (!missing.isBlank()) {
            builder.append("Q3-当前最缺：").append(missing).append('\n');
        }
        builder.append(content);
        return safe(builder.toString(), 1000);
    }
}
