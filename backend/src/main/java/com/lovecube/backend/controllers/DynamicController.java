package com.lovecube.backend.controllers;

import com.lovecube.backend.entity.Dynamic;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.services.DynamicService;
import com.lovecube.backend.services.GrowthService;
import com.lovecube.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DynamicController {

    @Autowired
    private DynamicService dynamicService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GrowthService growthService;

    /**
     * 获取动态列表
     */
    @GetMapping("/dynamics")
    public ResponseEntity<?> getDynamicList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long currentUserId = getCurrentUserId(authHeader);
            if (currentUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "用户认证失败"));
            }

            Map<String, Object> result = dynamicService.getDynamicList(pageNum, pageSize, currentUserId);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "获取动态列表失败: " + e.getMessage()));
        }
    }

    /**
     * 发布动态
     */
    @PostMapping("/dynamics")
    public ResponseEntity<?> publishDynamic(
            @RequestBody Map<String, Object> request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long currentUserId = getCurrentUserId(authHeader);
            if (currentUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "用户认证失败"));
            }

            String content = (String) request.get("content");
            @SuppressWarnings("unchecked")
            List<String> imageUrls = (List<String>) request.get("imageUrls");

            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "动态内容不能为空"));
            }

            Dynamic dynamic = dynamicService.publishDynamic(currentUserId, content, imageUrls);
            growthService.recordAction(currentUserId, "POST_CONTENT", "DYNAMIC_" + dynamic.getId());
            growthService.recordAction(currentUserId, "FIRST_POST_BONUS", "FIRST_POST_ONCE");
            return ResponseEntity.ok(Map.of(
                "message", "发布成功",
                "dynamic", dynamic
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "发布动态失败: " + e.getMessage()));
        }
    }

    /**
     * 点赞/取消点赞动态
     */
    @PostMapping("/dynamics/{id}/like")
    public ResponseEntity<?> toggleLike(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long currentUserId = getCurrentUserId(authHeader);
            if (currentUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "用户认证失败"));
            }

            Map<String, Object> result = dynamicService.toggleLike(id, currentUserId);
            if (Boolean.TRUE.equals(result.get("isLiked"))) {
                growthService.recordAction(currentUserId, "LIKE_CONTENT", "DYNAMIC_LIKE_" + id);
            }
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "操作失败: " + e.getMessage()));
        }
    }

    /**
     * 取消点赞动态（DELETE方法）
     */
    @DeleteMapping("/dynamics/{id}/like")
    public ResponseEntity<?> unlikeDynamic(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        return toggleLike(id, authHeader);
    }

    /**
     * 删除动态
     */
    @DeleteMapping("/dynamics/{id}")
    public ResponseEntity<?> deleteDynamic(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long currentUserId = getCurrentUserId(authHeader);
            if (currentUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "用户认证失败"));
            }

            dynamicService.deleteDynamic(id, currentUserId);
            return ResponseEntity.ok(Map.of("message", "删除成功"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "删除失败: " + e.getMessage()));
        }
    }

    /**
     * 获取动态详情
     */
    @GetMapping("/dynamics/{id}")
    public ResponseEntity<?> getDynamicDetail(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long currentUserId = getCurrentUserId(authHeader);
            if (currentUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "用户认证失败"));
            }

            Dynamic dynamic = dynamicService.getDynamicDetail(id, currentUserId);
            return ResponseEntity.ok(dynamic);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "获取动态详情失败: " + e.getMessage()));
        }
    }

    @GetMapping("/dynamics/me/count")
    public ResponseEntity<?> getMyDynamicCount(@RequestHeader("Authorization") String authHeader) {
        Long currentUserId = getCurrentUserId(authHeader);
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户认证失败"));
        }
        return ResponseEntity.ok(Map.of("count", dynamicService.getMyDynamicCount(currentUserId)));
    }

    /**
     * 动态评论列表
     */
    @GetMapping("/dynamics/{id}/comments")
    public ResponseEntity<?> listDynamicComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestHeader("Authorization") String authHeader) {
        try {
            if (getCurrentUserId(authHeader) == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户认证失败"));
            }
            Map<String, Object> result = dynamicService.listDynamicComments(id, pageNum, pageSize);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage() != null ? e.getMessage() : "加载评论失败"));
        }
    }

    /**
     * 发表评论
     */
    @PostMapping("/dynamics/{id}/comments")
    public ResponseEntity<?> addDynamicComment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long currentUserId = getCurrentUserId(authHeader);
            if (currentUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户认证失败"));
            }
            String content = request.get("content") != null ? String.valueOf(request.get("content")) : "";
            Map<String, Object> result = dynamicService.addDynamicComment(id, currentUserId, content);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage() != null ? e.getMessage() : "评论失败"));
        }
    }

    /**
     * 删除评论（评论者本人或动态发布者可删）
     */
    @DeleteMapping("/dynamics/{id}/comments/{commentId}")
    public ResponseEntity<?> deleteDynamicComment(
            @PathVariable Long id,
            @PathVariable Long commentId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long currentUserId = getCurrentUserId(authHeader);
            if (currentUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户认证失败"));
            }
            dynamicService.deleteDynamicComment(id, commentId, currentUserId);
            return ResponseEntity.ok(Map.of("message", "已删除"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage() != null ? e.getMessage() : "删除失败"));
        }
    }

    /**
     * 从Authorization头中获取当前用户ID
     */
    private Long getCurrentUserId(String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return null;
            }

            String token = authHeader.substring(7);
            String openid = JwtUtil.getOpenIdFromToken(token);

            if (openid == null) {
                return null;
            }

            User user = userRepository.findByOpenid(openid);
            return user != null ? user.getUserid() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
