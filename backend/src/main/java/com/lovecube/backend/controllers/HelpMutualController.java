package com.lovecube.backend.controllers;

import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.HelpMutualService;
import com.lovecube.backend.utils.JwtUtil;
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
@RequestMapping("/api/help")
public class HelpMutualController {

    private final HelpMutualService helpMutualService;
    private final AdminAuthService adminAuthService;
    private final UserRepository userRepository;

    public HelpMutualController(
            HelpMutualService helpMutualService,
            AdminAuthService adminAuthService,
            UserRepository userRepository
    ) {
        this.helpMutualService = helpMutualService;
        this.adminAuthService = adminAuthService;
        this.userRepository = userRepository;
    }

    @GetMapping("/requests")
    public Map<String, Object> listRequests(
            @RequestParam(required = false) String helpType,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return helpMutualService.listPublic(helpType, pageNum, pageSize);
    }

    @GetMapping("/requests/mine")
    public Map<String, Object> listMineAuthored(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        User user = adminAuthService.requireUser(authHeader);
        return helpMutualService.listMineAuthored(user, pageNum, pageSize);
    }

    @GetMapping("/replies/mine")
    public Map<String, Object> listMineReplied(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        User user = adminAuthService.requireUser(authHeader);
        return helpMutualService.listMineReplied(user, pageNum, pageSize);
    }

    @GetMapping("/requests/{id}")
    public Map<String, Object> getDetail(
            @PathVariable long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Long viewerId = resolveCurrentUserId(authHeader);
        return helpMutualService.getRequestDetail(id, viewerId);
    }

    @PostMapping("/requests")
    public Map<String, Object> createRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> payload
    ) {
        User user = adminAuthService.requireUser(authHeader);
        return helpMutualService.createRequest(user, payload);
    }

    @PostMapping("/requests/{id}/reply")
    public Map<String, Object> createReply(
            @PathVariable long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> payload
    ) {
        User user = adminAuthService.requireUser(authHeader);
        return helpMutualService.createReply(id, user, payload);
    }

    @PostMapping("/replies/{replyId}/accept")
    public Map<String, Object> acceptReply(
            @PathVariable long replyId,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        User user = adminAuthService.requireUser(authHeader);
        return helpMutualService.acceptReply(replyId, user);
    }

    /**
     * 发布者拒绝待确认的互助意向；响应体为统一结构：success、message、data。
     */
    @PostMapping("/replies/{replyId}/reject")
    public Map<String, Object> rejectReply(
            @PathVariable long replyId,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        User user = adminAuthService.requireUser(authHeader);
        return helpMutualService.rejectReply(replyId, user);
    }

    @PostMapping("/requests/{id}/resolve")
    public Map<String, Object> resolve(
            @PathVariable long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> payload
    ) {
        User user = adminAuthService.requireUser(authHeader);
        return helpMutualService.resolveRequest(id, user, payload);
    }

    @PostMapping("/requests/{id}/close")
    public Map<String, Object> close(
            @PathVariable long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        User user = adminAuthService.requireUser(authHeader);
        return helpMutualService.closeRequest(id, user);
    }

    @GetMapping("/leaderboard")
    public Map<String, Object> leaderboard(@RequestParam(defaultValue = "10") int limit) {
        return helpMutualService.getLeaderboard(limit);
    }

    @GetMapping("/user/stats/{userId}")
    public Map<String, Object> userStats(@PathVariable long userId) {
        return helpMutualService.getUserStats(userId);
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
