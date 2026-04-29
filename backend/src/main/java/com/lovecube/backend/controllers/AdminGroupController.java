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
@RequestMapping("/api/admin/groups")
public class AdminGroupController {

    private final PlatformGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final GroupPostRepository postRepository;
    private final GroupJoinRequestRepository joinRequestRepository;
    private final UserRepository userRepository;
    private final AdminAuthService adminAuthService;

    public AdminGroupController(
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
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        adminAuthService.requireAdmin(authHeader);
        List<PlatformGroup> groups = groupRepository.findAllOrderByPinnedDescCreatedAtDesc();
        return groups.stream().map(g -> {
            Map<String, Object> item = buildGroupDetail(g);
            item.put("pendingRequestCount",
                    joinRequestRepository.findByGroupIdAndStatusOrderByRequestedAtDesc(g.getId(), "pending").size());
            return item;
        }).collect(Collectors.toList());
    }

    @PostMapping
    public PlatformGroup createGroup(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> payload
    ) {
        User admin = adminAuthService.requireUser(authHeader);
        if (!adminAuthService.isAdmin(admin)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无管理员权限");
        }
        String name = String.valueOf(payload.getOrDefault("name", "")).trim();
        if (name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "团体名称不能为空");
        }
        PlatformGroup group = new PlatformGroup();
        group.setId("group-" + UUID.randomUUID());
        group.setName(name);
        group.setDescription(String.valueOf(payload.getOrDefault("description", "")));
        group.setCategory(String.valueOf(payload.getOrDefault("category", "")));
        group.setCoverUrl(String.valueOf(payload.getOrDefault("coverUrl", "")));
        group.setStatus(String.valueOf(payload.getOrDefault("status", "active")));
        group.setJoinType(String.valueOf(payload.getOrDefault("joinType", "approval")));
        group.setMemberCount(0);
        group.setPinned(Boolean.parseBoolean(String.valueOf(payload.getOrDefault("pinned", "false"))));
        group.setCreatedBy(admin.getUserid());
        group.setCreatedAt(LocalDateTime.now());
        group.setUpdatedAt(LocalDateTime.now());
        return groupRepository.save(group);
    }

    @PutMapping("/{id}")
    public PlatformGroup updateGroup(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id,
            @RequestBody Map<String, Object> payload
    ) {
        adminAuthService.requireAdmin(authHeader);
        PlatformGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "团体不存在"));
        if (payload.containsKey("name")) {
            String name = String.valueOf(payload.get("name")).trim();
            if (!name.isBlank()) group.setName(name);
        }
        if (payload.containsKey("description")) group.setDescription(String.valueOf(payload.get("description")));
        if (payload.containsKey("category")) group.setCategory(String.valueOf(payload.get("category")));
        if (payload.containsKey("coverUrl")) group.setCoverUrl(String.valueOf(payload.get("coverUrl")));
        if (payload.containsKey("status")) group.setStatus(String.valueOf(payload.get("status")));
        if (payload.containsKey("joinType")) group.setJoinType(String.valueOf(payload.get("joinType")));
        if (payload.containsKey("pinned")) group.setPinned(Boolean.parseBoolean(String.valueOf(payload.get("pinned"))));
        group.setUpdatedAt(LocalDateTime.now());
        return groupRepository.save(group);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public Map<String, Object> deleteGroup(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id
    ) {
        adminAuthService.requireAdmin(authHeader);
        groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "团体不存在"));
        memberRepository.findByGroupIdOrderByJoinedAtAsc(id).forEach(m -> memberRepository.delete(m));
        postRepository.findByGroupIdOrderByCreatedAtDesc(id).forEach(p -> postRepository.delete(p));
        joinRequestRepository.findByGroupIdOrderByRequestedAtDesc(id).forEach(r -> joinRequestRepository.delete(r));
        groupRepository.deleteById(id);
        return Map.of("id", id, "message", "团体已删除");
    }

    @GetMapping("/{id}/join-requests")
    public List<Map<String, Object>> listJoinRequests(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id,
            @RequestParam(defaultValue = "pending") String status
    ) {
        adminAuthService.requireAdmin(authHeader);
        groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "团体不存在"));
        List<GroupJoinRequest> requests = "all".equals(status)
                ? joinRequestRepository.findByGroupIdOrderByRequestedAtDesc(id)
                : joinRequestRepository.findByGroupIdAndStatusOrderByRequestedAtDesc(id, status);
        Set<Long> userIds = requests.stream().map(GroupJoinRequest::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserid, u -> u));
        return requests.stream().map(r -> {
            User u = userMap.get(r.getUserId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", r.getId());
            item.put("userId", r.getUserId());
            item.put("status", r.getStatus());
            item.put("message", r.getMessage());
            item.put("requestedAt", r.getRequestedAt());
            item.put("handledAt", r.getHandledAt());
            item.put("username", u != null ? u.getUsername() : "");
            item.put("avatarUrl", u != null ? u.getProfilePhoto() : "");
            return item;
        }).collect(Collectors.toList());
    }

    @PatchMapping("/{id}/join-requests/{requestId}/approve")
    @Transactional
    public Map<String, Object> approveRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id,
            @PathVariable Long requestId
    ) {
        adminAuthService.requireAdmin(authHeader);
        GroupJoinRequest req = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "申请不存在"));
        if (!id.equals(req.getGroupId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "申请与团体不匹配");
        }
        if (!"pending".equals(req.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "该申请已处理");
        }
        PlatformGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "团体不存在"));

        if (!memberRepository.existsByGroupIdAndUserId(id, req.getUserId())) {
            GroupMember member = new GroupMember();
            member.setGroupId(id);
            member.setUserId(req.getUserId());
            member.setRole("member");
            member.setJoinedAt(LocalDateTime.now());
            memberRepository.save(member);
            group.setMemberCount((group.getMemberCount() == null ? 0 : group.getMemberCount()) + 1);
            groupRepository.save(group);
        }
        req.setStatus("approved");
        req.setHandledAt(LocalDateTime.now());
        joinRequestRepository.save(req);
        return Map.of("id", requestId, "status", "approved", "message", "已通过申请");
    }

    @PatchMapping("/{id}/join-requests/{requestId}/reject")
    @Transactional
    public Map<String, Object> rejectRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id,
            @PathVariable Long requestId
    ) {
        adminAuthService.requireAdmin(authHeader);
        GroupJoinRequest req = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "申请不存在"));
        if (!id.equals(req.getGroupId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "申请与团体不匹配");
        }
        if (!"pending".equals(req.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "该申请已处理");
        }
        req.setStatus("rejected");
        req.setHandledAt(LocalDateTime.now());
        joinRequestRepository.save(req);
        return Map.of("id", requestId, "status", "rejected", "message", "已拒绝申请");
    }

    @PostMapping("/{id}/posts")
    public GroupPost createPost(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id,
            @RequestBody Map<String, Object> payload
    ) {
        User admin = adminAuthService.requireUser(authHeader);
        if (!adminAuthService.isAdmin(admin)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无管理员权限");
        }
        groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "团体不存在"));
        String content = String.valueOf(payload.getOrDefault("content", "")).trim();
        if (content.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "内容不能为空");
        }
        String type = String.valueOf(payload.getOrDefault("type", "post"));
        GroupPost post = new GroupPost();
        post.setId("gpost-" + UUID.randomUUID());
        post.setGroupId(id);
        post.setUserId(admin.getUserid());
        post.setType(type);
        post.setContent(content);
        post.setLikeCount(0);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        return postRepository.save(post);
    }

    @DeleteMapping("/{id}/posts/{postId}")
    public Map<String, Object> deletePost(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id,
            @PathVariable String postId
    ) {
        adminAuthService.requireAdmin(authHeader);
        postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "动态不存在"));
        postRepository.deleteById(postId);
        return Map.of("id", postId, "message", "动态已删除");
    }

    @DeleteMapping("/{id}/members/{userId}")
    @Transactional
    public Map<String, Object> removeMember(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id,
            @PathVariable Long userId
    ) {
        adminAuthService.requireAdmin(authHeader);
        PlatformGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "团体不存在"));
        if (!memberRepository.existsByGroupIdAndUserId(id, userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "该用户不是团体成员");
        }
        memberRepository.deleteByGroupIdAndUserId(id, userId);
        int count = group.getMemberCount() == null ? 0 : group.getMemberCount();
        group.setMemberCount(Math.max(0, count - 1));
        groupRepository.save(group);
        return Map.of("userId", userId, "message", "成员已移除");
    }

    private Map<String, Object> buildGroupDetail(PlatformGroup g) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", g.getId());
        item.put("name", g.getName());
        item.put("description", g.getDescription());
        item.put("category", g.getCategory());
        item.put("coverUrl", g.getCoverUrl());
        item.put("status", g.getStatus());
        item.put("joinType", g.getJoinType());
        item.put("memberCount", g.getMemberCount() == null ? 0 : g.getMemberCount());
        item.put("pinned", Boolean.TRUE.equals(g.getPinned()));
        item.put("createdBy", g.getCreatedBy());
        item.put("createdAt", g.getCreatedAt());
        item.put("updatedAt", g.getUpdatedAt());
        return item;
    }
}
