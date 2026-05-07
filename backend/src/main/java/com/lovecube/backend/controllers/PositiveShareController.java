package com.lovecube.backend.controllers;

import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.GrowthService;
import com.lovecube.backend.services.PositiveShareService;
import com.lovecube.backend.utils.JwtUtil;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/positive-shares")
public class PositiveShareController {
    private final PositiveShareService positiveShareService;
    private final AdminAuthService adminAuthService;
    private final UserRepository userRepository;
    private final GrowthService growthService;

    public PositiveShareController(
            PositiveShareService positiveShareService,
            AdminAuthService adminAuthService,
            UserRepository userRepository,
            GrowthService growthService
    ) {
        this.positiveShareService = positiveShareService;
        this.adminAuthService = adminAuthService;
        this.userRepository = userRepository;
        this.growthService = growthService;
    }

    @PostMapping
    public Map<String, Object> createShare(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> payload
    ) {
        User user = adminAuthService.requireUser(authHeader);
        Map<String, Object> result = positiveShareService.createShare(user, payload);
        Object id = result.get("id");
        if (id != null) {
            growthService.recordAction(user.getUserid(), "POST_CONTENT", "POSITIVE_SHARE_" + id);
            // 首次发布额外奖励（bizId 固定，GrowthLog 唯一约束保证只触发一次）
            growthService.recordAction(user.getUserid(), "FIRST_POST_BONUS", "FIRST_POST_ONCE");
        }
        return result;
    }

    @GetMapping
    public Map<String, Object> listShares(
            @RequestParam(defaultValue = "latest") String tab,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long currentUserId = resolveCurrentUserId(authHeader);
        return positiveShareService.listShares(tab, pageNum, pageSize, currentUserId);
    }

    @GetMapping("/my")
    public Map<String, Object> listMyShares(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        User user = adminAuthService.requireUser(authHeader);
        return positiveShareService.listShares("my", pageNum, pageSize, user.getUserid());
    }

    @GetMapping("/my/favorites")
    public Map<String, Object> listMyFavorites(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        User user = adminAuthService.requireUser(authHeader);
        return positiveShareService.listMyFavorites(user.getUserid(), pageNum, pageSize);
    }

    @GetMapping("/my/likes")
    public Map<String, Object> listMyLikes(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        User user = adminAuthService.requireUser(authHeader);
        return positiveShareService.listMyLikes(user.getUserid(), pageNum, pageSize);
    }

    @GetMapping("/my/drafts")
    public Map<String, Object> listMyDrafts(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        User user = adminAuthService.requireUser(authHeader);
        return positiveShareService.listMyDrafts(user.getUserid(), pageNum, pageSize);
    }

    @PostMapping("/{id}/like")
    public Map<String, Object> likeShare(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        User user = adminAuthService.requireUser(authHeader);
        Map<String, Object> result = positiveShareService.likeShare(id, user.getUserid());
        boolean alreadyLiked = Boolean.TRUE.equals(result.get("alreadyLiked"));
        if (!alreadyLiked) {
            growthService.recordAction(user.getUserid(), "LIKE_CONTENT", "POSITIVE_SHARE_" + id);
            // 作者被点赞经验（排除自赞，bizId 含 likerId 保证每个点赞人只触发一次）
            Long authorId = positiveShareService.getShareAuthorId(id);
            if (authorId != null && !authorId.equals(user.getUserid())) {
                growthService.recordAction(authorId, "LIKED_BY_OTHERS",
                        "LIKED_SHARE_" + id + "_BY_" + user.getUserid());
            }
        }
        return result;
    }

    @DeleteMapping("/{id}/like")
    public Map<String, Object> unlikeShare(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        User user = adminAuthService.requireUser(authHeader);
        return positiveShareService.unlikeShare(id, user.getUserid());
    }

    @PostMapping("/{id}/bookmark")
    public Map<String, Object> bookmarkShare(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        User user = adminAuthService.requireUser(authHeader);
        return positiveShareService.bookmarkShare(id, user.getUserid());
    }

    @DeleteMapping("/{id}/bookmark")
    public Map<String, Object> unbookmarkShare(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        User user = adminAuthService.requireUser(authHeader);
        return positiveShareService.unbookmarkShare(id, user.getUserid());
    }

    @PostMapping("/{id}/comments")
    public Map<String, Object> commentShare(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> payload
    ) {
        User user = adminAuthService.requireUser(authHeader);
        String content = String.valueOf(payload.getOrDefault("content", ""));
        Map<String, Object> result = positiveShareService.commentShare(id, user.getUserid(), content);
        // 评论成功后给评论者经验（同一用户对同一内容只奖励一次）
        growthService.recordAction(user.getUserid(), "COMMENT_CONTENT", "COMMENT_SHARE_" + id);
        return result;
    }

    @GetMapping("/{id}/comments")
    public Map<String, Object> listComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long currentUserId = resolveCurrentUserId(authHeader);
        return positiveShareService.listShareComments(id, pageNum, pageSize, currentUserId);
    }

    private Long resolveCurrentUserId(String authHeader) {
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
        } catch (Exception ignored) {
            return null;
        }
    }
}
