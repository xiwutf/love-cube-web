package com.lovecube.backend.controllers;

import com.lovecube.backend.entity.PlatCheckinComment;
import com.lovecube.backend.entity.PlatCheckinLike;
import com.lovecube.backend.entity.PlatGroupCheckin;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.PlatCheckinCommentRepository;
import com.lovecube.backend.repository.PlatCheckinLikeRepository;
import com.lovecube.backend.repository.PlatGroupCheckinRepository;
import com.lovecube.backend.repository.PlatGroupMemberRepository;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.GroupExternalEngagementService;
import com.lovecube.backend.util.CheckinCommentTextValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/platform/checkins")
public class PlatformCheckinController {

    private final PlatGroupCheckinRepository checkinRepository;
    private final PlatCheckinLikeRepository likeRepository;
    private final PlatCheckinCommentRepository commentRepository;
    private final PlatGroupMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final AdminAuthService adminAuthService;
    private final GroupExternalEngagementService groupExternalEngagementService;

    public PlatformCheckinController(
            PlatGroupCheckinRepository checkinRepository,
            PlatCheckinLikeRepository likeRepository,
            PlatCheckinCommentRepository commentRepository,
            PlatGroupMemberRepository memberRepository,
            UserRepository userRepository,
            AdminAuthService adminAuthService,
            GroupExternalEngagementService groupExternalEngagementService) {
        this.checkinRepository = checkinRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.adminAuthService = adminAuthService;
        this.groupExternalEngagementService = groupExternalEngagementService;
    }

    private void requireCheckinMembership(Long checkinId, User user) {
        PlatGroupCheckin plat = checkinRepository.findById(checkinId).orElse(null);
        if (plat != null) {
            requireApprovedMember(plat.getGroupId(), user);
            return;
        }
        groupExternalEngagementService.assertCheckinAccessForUser(checkinId, user);
    }

    private void requireApprovedMember(Long groupId, User user) {
        memberRepository.findByGroupIdAndUserId(groupId, user.getUserid())
                .filter(m -> "approved".equals(m.getStatus()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "仅团体成员可操作"));
    }

    @PostMapping("/{checkinId}/like")
    @Transactional
    public Map<String, Object> likeCheckin(
            @PathVariable Long checkinId,
            @RequestHeader("Authorization") String authHeader) {

        User user = adminAuthService.requireUser(authHeader);
        requireCheckinMembership(checkinId, user);

        if (likeRepository.existsByCheckinIdAndUserId(checkinId, user.getUserid())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "已经点过赞了");
        }
        PlatCheckinLike like = new PlatCheckinLike();
        like.setCheckinId(checkinId);
        like.setUserId(user.getUserid());
        like.setCreatedAt(LocalDateTime.now());
        likeRepository.save(like);
        long likeCount = likeRepository.countByCheckinId(checkinId);
        return Map.of("checkinId", checkinId, "liked", true, "likeCount", likeCount);
    }

    @DeleteMapping("/{checkinId}/like")
    @Transactional
    public Map<String, Object> unlikeCheckin(
            @PathVariable Long checkinId,
            @RequestHeader("Authorization") String authHeader) {

        User user = adminAuthService.requireUser(authHeader);
        requireCheckinMembership(checkinId, user);

        if (!likeRepository.existsByCheckinIdAndUserId(checkinId, user.getUserid())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "尚未点赞");
        }
        likeRepository.deleteByCheckinIdAndUserId(checkinId, user.getUserid());
        long likeCount = likeRepository.countByCheckinId(checkinId);
        return Map.of("checkinId", checkinId, "liked", false, "likeCount", likeCount);
    }

    @GetMapping("/{checkinId}/comments")
    public Map<String, Object> listComments(
            @PathVariable Long checkinId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestHeader("Authorization") String authHeader) {

        User user = adminAuthService.requireUser(authHeader);
        requireCheckinMembership(checkinId, user);

        int safePage = Math.max(1, page);
        int safeSize = Math.min(100, Math.max(1, size));
        Page<PlatCheckinComment> pageResult = commentRepository.findByCheckinIdAndDeletedAtIsNullOrderByCreatedAtAsc(
                checkinId, PageRequest.of(safePage - 1, safeSize));

        Set<Long> userIds = pageResult.getContent().stream().map(PlatCheckinComment::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userIds.isEmpty() ? Collections.emptyMap()
                : userRepository.findAllById(userIds).stream().collect(Collectors.toMap(User::getUserid, u -> u));

        List<Map<String, Object>> items = pageResult.getContent().stream().map(c -> {
            User u = userMap.get(c.getUserId());
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", c.getId());
            row.put("checkinId", checkinId);
            row.put("userId", c.getUserId());
            row.put("nickname", u != null ? u.getUsername() : "");
            row.put("avatarUrl", u != null ? u.getProfilePhoto() : "");
            row.put("content", c.getContent());
            row.put("createdAt", c.getCreatedAt());
            return row;
        }).collect(Collectors.toList());

        return Map.of("items", items, "total", pageResult.getTotalElements(), "page", safePage, "pageSize", safeSize);
    }

    @PostMapping("/{checkinId}/comments")
    @Transactional
    public Map<String, Object> addComment(
            @PathVariable Long checkinId,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> body) {

        User user = adminAuthService.requireUser(authHeader);
        requireCheckinMembership(checkinId, user);

        String raw = String.valueOf(body != null ? body.getOrDefault("content", "") : "");
        String err = CheckinCommentTextValidator.validate(raw);
        if (err != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, err);
        }
        String content = raw.trim();

        PlatCheckinComment c = new PlatCheckinComment();
        c.setCheckinId(checkinId);
        c.setUserId(user.getUserid());
        c.setContent(content);
        c.setCreatedAt(LocalDateTime.now());
        commentRepository.save(c);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", c.getId());
        row.put("checkinId", checkinId);
        row.put("userId", user.getUserid());
        row.put("nickname", user.getUsername() != null ? user.getUsername() : "");
        row.put("avatarUrl", user.getProfilePhoto() != null ? user.getProfilePhoto() : "");
        row.put("content", c.getContent());
        row.put("createdAt", c.getCreatedAt());
        return row;
    }

    @DeleteMapping("/comments/{commentId}")
    @Transactional
    public Map<String, Object> deleteComment(
            @PathVariable Long commentId,
            @RequestHeader("Authorization") String authHeader) {

        User user = adminAuthService.requireUser(authHeader);
        PlatCheckinComment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "评论不存在"));
        if (c.getDeletedAt() != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "评论不存在");
        }
        requireCheckinMembership(c.getCheckinId(), user);

        if (!c.getUserId().equals(user.getUserid())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "只能删除自己的评论");
        }
        c.setDeletedAt(LocalDateTime.now());
        commentRepository.save(c);
        return Map.of("deleted", true, "message", "已删除");
    }
}
