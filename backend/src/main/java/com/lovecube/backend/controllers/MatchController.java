package com.lovecube.backend.controllers;

import com.lovecube.backend.models.User;
import com.lovecube.backend.services.MatchService;
import com.lovecube.backend.services.UnifiedProfileService;
import com.lovecube.backend.services.VerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/matches")
public class MatchController {
    private static final Logger logger = LoggerFactory.getLogger(MatchController.class);

    @Autowired
    private MatchService matchService;

    @Autowired
    private UnifiedProfileService unifiedProfileService;

    @Autowired
    private VerificationService verificationService;

    @GetMapping("/list")
    public ResponseEntity<?> getUserList(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) Integer gender,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Boolean verifiedOnly,
            @RequestParam(required = false, defaultValue = "false") Boolean includeActed,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        try {
            Long currentUserId = matchService.getCurrentUserId(token);
            if (currentUserId == null) {
                return ResponseEntity.badRequest().body("无效的用户Token");
            }

            int safePage = page == null || page < 1 ? 1 : page;
            int safeSize = size == null ? 20 : Math.min(Math.max(size, 1), 50);

            List<User> users = matchService.getAllUsers(currentUserId, gender, minAge, maxAge, location, includeActed);
            if (Boolean.TRUE.equals(verifiedOnly) && !users.isEmpty()) {
                List<Long> allUserIds = users.stream().map(User::getUserid).collect(Collectors.toList());
                Map<Long, Map<String, Boolean>> allVerifyMap = verificationService.getBatchSummary(allUserIds);
                users = users.stream().filter(u -> {
                    Map<String, Boolean> badges = allVerifyMap.getOrDefault(u.getUserid(), Map.of());
                    boolean photoVerified = badges.getOrDefault("photoVerified", false);
                    boolean realnameVerified = badges.getOrDefault("realnameVerified", false);
                    return photoVerified || realnameVerified;
                }).collect(Collectors.toList());
            }
            int total = users.size();
            int fromIndex = Math.min((safePage - 1) * safeSize, total);
            int toIndex = Math.min(fromIndex + safeSize, total);
            List<User> pageUsers = users.subList(fromIndex, toIndex);
            List<Long> userIds = pageUsers.stream().map(User::getUserid).collect(Collectors.toList());
            Map<Long, Map<String, Boolean>> verifyMap = verificationService.getBatchSummary(userIds);

            List<Map<String, Object>> enriched = pageUsers.stream().map(u -> {
                Map<String, Object> payload = unifiedProfileService.buildLegacyUserPayload(u);
                Map<String, Boolean> badges = verifyMap.getOrDefault(u.getUserid(), Map.of());
                payload.put("photoVerified",    badges.getOrDefault("photoVerified",    false));
                payload.put("realnameVerified", badges.getOrDefault("realnameVerified", false));
                return payload;
            }).collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", enriched);
            response.put("page", safePage);
            response.put("size", safeSize);
            response.put("total", total);
            response.put("hasMore", toIndex < total);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("获取用户列表失败", e);
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "获取用户列表失败: " + e.getMessage()));
        }
    }

    @PostMapping("/filter")
    public ResponseEntity<?> filterMatches(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> filters) {
        try {
            Long currentUserId = matchService.getCurrentUserId(token);
            if (currentUserId == null) {
                return ResponseEntity.badRequest().body("无效的用户Token");
            }

            Integer minAge = null;
            Integer maxAge = null;
            if (filters.containsKey("ageRange") && filters.get("ageRange") instanceof List<?>) {
                List<?> ageRange = (List<?>) filters.get("ageRange");
                if (ageRange.size() == 2 && ageRange.get(0) instanceof Number && ageRange.get(1) instanceof Number) {
                    minAge = ((Number) ageRange.get(0)).intValue();
                    maxAge = ((Number) ageRange.get(1)).intValue();
                }
            }
            Integer gender = filters.get("gender") != null
                ? Integer.parseInt(filters.get("gender").toString()) : null;
            String location = (String) filters.get("region");

            List<User> filteredUsers = matchService.findMatches(currentUserId, minAge, maxAge, gender, location);
            List<Long> userIds = filteredUsers.stream().map(User::getUserid).collect(Collectors.toList());
            Map<Long, Map<String, Boolean>> verifyMap = verificationService.getBatchSummary(userIds);

            List<Map<String, Object>> enriched = filteredUsers.stream().map(u -> {
                Map<String, Object> payload = unifiedProfileService.buildLegacyUserPayload(u);
                Map<String, Boolean> badges = verifyMap.getOrDefault(u.getUserid(), Map.of());
                payload.put("photoVerified",    badges.getOrDefault("photoVerified",    false));
                payload.put("realnameVerified", badges.getOrDefault("realnameVerified", false));
                return payload;
            }).collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", enriched);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("筛选用户失败", e);
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "筛选用户失败: " + e.getMessage()));
        }
    }
}
