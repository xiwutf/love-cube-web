package com.lovecube.backend.controllers;

import com.lovecube.backend.entity.GroupJoinRequest;
import com.lovecube.backend.entity.GroupMember;
import com.lovecube.backend.entity.GroupPost;
import com.lovecube.backend.entity.PlatformGroup;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.GroupJoinRequestRepository;
import com.lovecube.backend.repository.GroupMemberRepository;
import com.lovecube.backend.repository.GroupPostRepository;
import com.lovecube.backend.repository.PlatformGroupRepository;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.services.AdminAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final PlatformGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final GroupPostRepository postRepository;
    private final GroupJoinRequestRepository joinRequestRepository;
    private final UserRepository userRepository;
    private final AdminAuthService adminAuthService;

    public GroupController(
            PlatformGroupRepository groupRepository,
            GroupMemberRepository memberRepository,
            GroupPostRepository postRepository,
            GroupJoinRequestRepository joinRequestRepository,
            UserRepository userRepository,
            AdminAuthService adminAuthService
    ) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.postRepository = postRepository;
        this.joinRequestRepository = joinRequestRepository;
        this.userRepository = userRepository;
        this.adminAuthService = adminAuthService;
    }

    @GetMapping
    public List<Map<String, Object>> listGroups(
            @RequestParam(defaultValue = "active") String status,
            @RequestParam(required = false) String category,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        List<PlatformGroup> groups = groupRepository.findByStatusOrderByPinnedDescCreatedAtDesc(status);
        if (category != null && !category.isBlank()) {
            groups = groups.stream().filter(g -> category.equals(g.getCategory())).collect(Collectors.toList());
        }
        Long currentUserId = resolveOptionalUserId(authHeader);
        return groups.stream().map(g -> buildGroupSummary(g, currentUserId)).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public Map<String, Object> getGroup(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        PlatformGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "团体不存在"));
        Long currentUserId = resolveOptionalUserId(authHeader);
        Map<String, Object> result = buildGroupSummary(group, currentUserId);
        result.put("description", group.getDescription());
        return result;
    }

    @GetMapping("/{id}/members")
    public List<Map<String, Object>> getMembers(@PathVariable String id) {
        groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "团体不存在"));
        List<GroupMember> members = memberRepository.findByGroupIdOrderByJoinedAtAsc(id);
        Set<Long> userIds = members.stream().map(GroupMember::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserid, u -> u));
        return members.stream().map(m -> {
            User u = userMap.get(m.getUserId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("userId", m.getUserId());
            item.put("role", m.getRole());
            item.put("joinedAt", m.getJoinedAt());
            item.put("username", u != null ? u.getUsername() : "");
            item.put("avatarUrl", u != null ? u.getProfilePhoto() : "");
            return item;
        }).collect(Collectors.toList());
    }

    @GetMapping("/{id}/posts")
    public List<Map<String, Object>> getPosts(@PathVariable String id) {
        groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "团体不存在"));
        List<GroupPost> posts = postRepository.findByGroupIdOrderByCreatedAtDesc(id);
        Set<Long> userIds = posts.stream().map(GroupPost::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserid, u -> u));
        return posts.stream().map(p -> {
            User u = userMap.get(p.getUserId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", p.getId());
            item.put("type", p.getType());
            item.put("content", p.getContent());
            item.put("likeCount", p.getLikeCount());
            item.put("createdAt", p.getCreatedAt());
            item.put("authorName", u != null ? u.getUsername() : "");
            item.put("authorAvatar", u != null ? u.getProfilePhoto() : "");
            return item;
        }).collect(Collectors.toList());
    }

    @PostMapping("/{id}/join")
    @Transactional
    public Map<String, Object> joinGroup(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) Map<String, Object> payload
    ) {
        User user = adminAuthService.requireUser(authHeader);
        PlatformGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "团体不存在"));
        if (!"active".equals(group.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "该团体当前不可加入");
        }
        if (memberRepository.existsByGroupIdAndUserId(id, user.getUserid())) {
            return Map.of("joined", true, "message", "你已是该团体成员");
        }

        String joinType = group.getJoinType() == null ? "approval" : group.getJoinType();

        if ("open".equals(joinType)) {
            GroupMember member = new GroupMember();
            member.setGroupId(id);
            member.setUserId(user.getUserid());
            member.setRole("member");
            member.setJoinedAt(LocalDateTime.now());
            memberRepository.save(member);
            group.setMemberCount((group.getMemberCount() == null ? 0 : group.getMemberCount()) + 1);
            groupRepository.save(group);
            return Map.of("joined", true, "message", "加入成功");
        }

        boolean alreadyPending = joinRequestRepository.existsByGroupIdAndUserIdAndStatus(id, user.getUserid(), "pending");
        if (alreadyPending) {
            return Map.of("joined", false, "pending", true, "message", "申请已提交，等待管理员审核");
        }

        GroupJoinRequest req = new GroupJoinRequest();
        req.setGroupId(id);
        req.setUserId(user.getUserid());
        req.setStatus("pending");
        req.setMessage(payload != null ? String.valueOf(payload.getOrDefault("message", "")) : "");
        req.setRequestedAt(LocalDateTime.now());
        joinRequestRepository.save(req);
        return Map.of("joined", false, "pending", true, "message", "申请已提交，等待管理员审核");
    }

    @DeleteMapping("/{id}/leave")
    @Transactional
    public Map<String, Object> leaveGroup(
            @PathVariable String id,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        User user = adminAuthService.requireUser(authHeader);
        PlatformGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "团体不存在"));
        if (!memberRepository.existsByGroupIdAndUserId(id, user.getUserid())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "你不是该团体成员");
        }
        memberRepository.deleteByGroupIdAndUserId(id, user.getUserid());
        int count = group.getMemberCount() == null ? 0 : group.getMemberCount();
        group.setMemberCount(Math.max(0, count - 1));
        groupRepository.save(group);
        return Map.of("left", true, "message", "已退出团体");
    }

    private Map<String, Object> buildGroupSummary(PlatformGroup g, Long currentUserId) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", g.getId());
        item.put("name", g.getName());
        item.put("category", g.getCategory());
        item.put("coverUrl", g.getCoverUrl());
        item.put("status", g.getStatus());
        item.put("joinType", g.getJoinType());
        item.put("memberCount", g.getMemberCount() == null ? 0 : g.getMemberCount());
        item.put("pinned", Boolean.TRUE.equals(g.getPinned()));
        item.put("createdAt", g.getCreatedAt());
        if (currentUserId != null) {
            item.put("isMember", memberRepository.existsByGroupIdAndUserId(g.getId(), currentUserId));
            item.put("hasPendingRequest", joinRequestRepository.existsByGroupIdAndUserIdAndStatus(
                    g.getId(), currentUserId, "pending"));
        } else {
            item.put("isMember", false);
            item.put("hasPendingRequest", false);
        }
        return item;
    }

    private Long resolveOptionalUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        try {
            return adminAuthService.requireUser(authHeader).getUserid();
        } catch (Exception e) {
            return null;
        }
    }
}
