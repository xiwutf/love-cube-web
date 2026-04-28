package com.lovecube.backend.controllers;

import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.services.AdminAuthService;
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

    public PositiveShareController(
            PositiveShareService positiveShareService,
            AdminAuthService adminAuthService,
            UserRepository userRepository
    ) {
        this.positiveShareService = positiveShareService;
        this.adminAuthService = adminAuthService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public Map<String, Object> createShare(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> payload
    ) {
        User user = adminAuthService.requireUser(authHeader);
        return positiveShareService.createShare(user, payload);
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
        return positiveShareService.likeShare(id, user.getUserid());
    }

    @DeleteMapping("/{id}/like")
    public Map<String, Object> unlikeShare(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        User user = adminAuthService.requireUser(authHeader);
        return positiveShareService.unlikeShare(id, user.getUserid());
    }

    @PostMapping("/{id}/comments")
    public Map<String, Object> commentShare(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> payload
    ) {
        User user = adminAuthService.requireUser(authHeader);
        String content = String.valueOf(payload.getOrDefault("content", ""));
        return positiveShareService.commentShare(id, user.getUserid(), content);
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
