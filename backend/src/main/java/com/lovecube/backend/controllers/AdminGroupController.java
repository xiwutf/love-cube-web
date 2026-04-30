package com.lovecube.backend.controllers;

import com.lovecube.backend.entity.GroupJoinRequest;
import com.lovecube.backend.entity.GroupMember;
import com.lovecube.backend.entity.GroupPost;
import com.lovecube.backend.entity.PlatformGroup;
import com.lovecube.backend.entity.PlatformGroupAdmin;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.GroupJoinRequestRepository;
import com.lovecube.backend.repository.GroupMemberRepository;
import com.lovecube.backend.repository.GroupPostRepository;
import com.lovecube.backend.repository.PlatformGroupAdminRepository;
import com.lovecube.backend.repository.PlatformGroupRepository;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.GroupAdminRoleConstants;
import com.lovecube.backend.services.PermissionConstants;
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
    private final PlatformGroupAdminRepository groupAdminRepository;
    private final UserRepository userRepository;
    private final AdminAuthService adminAuthService;

    public AdminGroupController(
            PlatformGroupRepository groupRepository,
            GroupMemberRepository memberRepository,
            GroupPostRepository postRepository,
            GroupJoinRequestRepository joinRequestRepository,
            PlatformGroupAdminRepository groupAdminRepository,
            UserRepository userRepository,
            AdminAuthService adminAuthService
    ) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.postRepository = postRepository;
        this.joinRequestRepository = joinRequestRepository;
        this.groupAdminRepository = groupAdminRepository;
        this.userRepository = userRepository;
        this.adminAuthService = adminAuthService;
    }

    // ── 团体列表 ───────────────────────────────────────────────────────────────

    /** SUPER_ADMIN 看全部；GROUP_OWNER/ADMIN/REVIEWER 只看自己管理的，并携带角色信息 */
    @GetMapping
    public List<Map<String, Object>> listGroups(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        User user = adminAuthService.requireUser(authHeader);
        Set<String> perms = adminAuthService.getUserPermissions(user);
        boolean manageAll = perms.contains(PermissionConstants.GROUP_MANAGE_ALL);
        boolean manageOwn = perms.contains(PermissionConstants.GROUP_MANAGE_OWN);

        if (!manageAll && !manageOwn) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无团体管理权限");
        }

        List<PlatformGroup> groups;
        if (manageAll) {
            groups = groupRepository.findAllOrderByPinnedDescCreatedAtDesc();
        } else {
            List<String> ids = adminAuthService.getManagedGroupIds(user);
            groups = ids.isEmpty() ? Collections.emptyList() : groupRepository.findAllById(ids);
        }

        return groups.stream().map(g -> {
            Map<String, Object> item = buildGroupDetail(g);
            item.put("pendingRequestCount",
                    joinRequestRepository.findByGroupIdAndStatusOrderByRequestedAtDesc(g.getId(), "pending").size());
            String tableRole = adminAuthService.getGroupRole(user, g.getId());
            if (tableRole == null && manageAll) {
                item.put("userRole", null);
                item.put("userRoleName", "平台监管");
                item.put("userPermissions", buildGroupPermissions(GroupAdminRoleConstants.OWNER));
                item.put("regulatingAsPlatformAdmin", true);
            } else {
                String norm = tableRole != null ? tableRole : GroupAdminRoleConstants.OWNER;
                item.put("userRole", norm);
                item.put("userRoleName", GroupAdminRoleConstants.displayName(norm));
                item.put("userPermissions", buildGroupPermissions(norm));
                item.put("regulatingAsPlatformAdmin", false);
            }
            return item;
        }).collect(Collectors.toList());
    }

    /** 团体详情（后台）：含当前用户在该团体内的角色与权限，供编辑页与 tab 控制 */
    @GetMapping("/{id}")
    public Map<String, Object> getGroupForAdmin(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id
    ) {
        User user = adminAuthService.requireUser(authHeader);
        PlatformGroup g = groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "团体不存在"));
        adminAuthService.requireGroupAdmin(authHeader, id);
        Map<String, Object> item = buildGroupDetail(g);
        String tableRole = adminAuthService.getGroupRole(user, id);
        if (tableRole == null && adminAuthService.hasGroupManageAll(user)) {
            item.put("userRole", null);
            item.put("userRoleName", "平台监管");
            item.put("userPermissions", buildGroupPermissions(GroupAdminRoleConstants.OWNER));
            item.put("regulatingAsPlatformAdmin", true);
        } else {
            String norm = tableRole != null ? tableRole : GroupAdminRoleConstants.OWNER;
            item.put("userRole", norm);
            item.put("userRoleName", GroupAdminRoleConstants.displayName(norm));
            item.put("userPermissions", buildGroupPermissions(norm));
            item.put("regulatingAsPlatformAdmin", false);
        }
        return item;
    }

    // ── 团体 CRUD ──────────────────────────────────────────────────────────────

    @PostMapping
    @Transactional
    public PlatformGroup createGroup(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> payload
    ) {
        User admin = adminAuthService.requirePermission(authHeader, PermissionConstants.GROUP_MANAGE_ALL);
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
        group.setMemberCount(1);
        group.setPinned(Boolean.parseBoolean(String.valueOf(payload.getOrDefault("pinned", "false"))));
        group.setCreatedBy(admin.getUserid());
        group.setOwnerUserId(admin.getUserid());
        group.setCreatedAt(LocalDateTime.now());
        group.setUpdatedAt(LocalDateTime.now());
        PlatformGroup saved = groupRepository.save(group);

        adminAuthService.upsertPlatformGroupAdmin(saved.getId(), admin.getUserid(), GroupAdminRoleConstants.OWNER);

        GroupMember creatorMember = new GroupMember();
        creatorMember.setGroupId(saved.getId());
        creatorMember.setUserId(admin.getUserid());
        creatorMember.setRole("owner");
        creatorMember.setJoinedAt(LocalDateTime.now());
        memberRepository.save(creatorMember);

        return saved;
    }

    /** OWNER / ADMIN 可编辑团体资料 */
    @PutMapping("/{id}")
    public PlatformGroup updateGroup(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id,
            @RequestBody Map<String, Object> payload
    ) {
        adminAuthService.requireGroupManagePermission(authHeader, id);
        PlatformGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "团体不存在"));
        if (payload.containsKey("name")) {
            String name = String.valueOf(payload.get("name")).trim();
            if (!name.isBlank()) group.setName(name);
        }
        if (payload.containsKey("description")) group.setDescription(String.valueOf(payload.get("description")));
        if (payload.containsKey("category"))    group.setCategory(String.valueOf(payload.get("category")));
        if (payload.containsKey("coverUrl"))    group.setCoverUrl(String.valueOf(payload.get("coverUrl")));
        if (payload.containsKey("status"))      group.setStatus(String.valueOf(payload.get("status")));
        if (payload.containsKey("joinType"))    group.setJoinType(String.valueOf(payload.get("joinType")));
        if (payload.containsKey("pinned"))      group.setPinned(Boolean.parseBoolean(String.valueOf(payload.get("pinned"))));
        group.setUpdatedAt(LocalDateTime.now());
        return groupRepository.save(group);
    }

    /** SUPER_ADMIN 或 OWNER 可删除 */
    @DeleteMapping("/{id}")
    @Transactional
    public Map<String, Object> deleteGroup(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id
    ) {
        adminAuthService.requireGroupOwnerOrSuperAdmin(authHeader, id);
        groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "团体不存在"));
        memberRepository.findByGroupIdOrderByJoinedAtAsc(id).forEach(memberRepository::delete);
        postRepository.findByGroupIdOrderByCreatedAtDesc(id).forEach(postRepository::delete);
        joinRequestRepository.findByGroupIdOrderByRequestedAtDesc(id).forEach(joinRequestRepository::delete);
        groupAdminRepository.findByGroupId(id).forEach(groupAdminRepository::delete);
        groupRepository.deleteById(id);
        return Map.of("id", id, "message", "团体已删除");
    }

    // ── 入团申请（OWNER / ADMIN / REVIEWER） ──────────────────────────────────

    @GetMapping("/{id}/join-requests")
    public List<Map<String, Object>> listJoinRequests(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id,
            @RequestParam(defaultValue = "pending") String status
    ) {
        adminAuthService.requireGroupReviewPermission(authHeader, id);
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
        adminAuthService.requireGroupReviewPermission(authHeader, id);
        GroupJoinRequest req = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "申请不存在"));
        if (!id.equals(req.getGroupId()))     throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "申请与团体不匹配");
        if (!"pending".equals(req.getStatus())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "该申请已处理");
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
        adminAuthService.requireGroupReviewPermission(authHeader, id);
        GroupJoinRequest req = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "申请不存在"));
        if (!id.equals(req.getGroupId()))      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "申请与团体不匹配");
        if (!"pending".equals(req.getStatus())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "该申请已处理");
        req.setStatus("rejected");
        req.setHandledAt(LocalDateTime.now());
        joinRequestRepository.save(req);
        return Map.of("id", requestId, "status", "rejected", "message", "已拒绝申请");
    }

    // ── 成员管理（OWNER / ADMIN） ──────────────────────────────────────────────

    @GetMapping("/{id}/members")
    public List<Map<String, Object>> listMembers(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id
    ) {
        adminAuthService.requireGroupManagePermission(authHeader, id);
        groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "团体不存在"));
        List<GroupMember> members = memberRepository.findByGroupIdOrderByJoinedAtAsc(id);
        Set<Long> uids = members.stream().map(GroupMember::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userRepository.findAllById(uids).stream()
                .collect(Collectors.toMap(User::getUserid, u -> u));
        return members.stream().map(m -> {
            User u = userMap.get(m.getUserId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", m.getId());
            item.put("userId", m.getUserId());
            item.put("role", m.getRole());
            item.put("joinedAt", m.getJoinedAt());
            item.put("username", u != null ? u.getUsername() : "");
            item.put("avatarUrl", u != null ? u.getProfilePhoto() : "");
            return item;
        }).collect(Collectors.toList());
    }

    @DeleteMapping("/{id}/members/{userId}")
    @Transactional
    public Map<String, Object> removeMember(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id,
            @PathVariable Long userId
    ) {
        adminAuthService.requireGroupManagePermission(authHeader, id);
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

    // ── 团体动态（OWNER / ADMIN） ──────────────────────────────────────────────

    @PostMapping("/{id}/posts")
    public GroupPost createPost(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id,
            @RequestBody Map<String, Object> payload
    ) {
        User admin = adminAuthService.requireGroupManagePermission(authHeader, id);
        groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "团体不存在"));
        String content = String.valueOf(payload.getOrDefault("content", "")).trim();
        if (content.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "内容不能为空");
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
        adminAuthService.requireGroupManagePermission(authHeader, id);
        postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "动态不存在"));
        postRepository.deleteById(postId);
        return Map.of("id", postId, "message", "动态已删除");
    }

    // ── 团体管理员管理（仅 OWNER） ────────────────────────────────────────────

    @GetMapping("/{id}/admins")
    public List<Map<String, Object>> listGroupAdmins(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id
    ) {
        adminAuthService.requireGroupOwner(authHeader, id);
        List<PlatformGroupAdmin> admins = groupAdminRepository.findByGroupId(id);
        Set<Long> uids = admins.stream().map(PlatformGroupAdmin::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userRepository.findAllById(uids).stream()
                .collect(Collectors.toMap(User::getUserid, u -> u));
        return admins.stream().map(a -> {
            User u = userMap.get(a.getUserId());
            String normRole = GroupAdminRoleConstants.normalize(a.getRole());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", a.getId());
            item.put("userId", a.getUserId());
            item.put("role", normRole);
            item.put("roleName", GroupAdminRoleConstants.displayName(normRole));
            item.put("createdAt", a.getCreatedAt());
            item.put("username", u != null ? u.getUsername() : "");
            item.put("avatarUrl", u != null ? u.getProfilePhoto() : "");
            return item;
        }).collect(Collectors.toList());
    }

    @PostMapping("/{id}/admins")
    public Map<String, Object> addGroupAdmin(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id,
            @RequestBody Map<String, Object> payload
    ) {
        User operator = adminAuthService.requireGroupOwner(authHeader, id);
        Long targetUserId = Long.valueOf(String.valueOf(payload.get("userId")));
        String role = GroupAdminRoleConstants.normalize(String.valueOf(payload.getOrDefault("role", GroupAdminRoleConstants.ADMIN)));
        if (GroupAdminRoleConstants.OWNER.equals(role)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不能直接添加 OWNER，请使用转让功能");
        }
        if (operator.getUserid().equals(targetUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不能修改自己的角色");
        }
        userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));
        groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "团体不存在"));

        Optional<PlatformGroupAdmin> existing = groupAdminRepository.findByGroupIdAndUserId(id, targetUserId);
        PlatformGroupAdmin ga;
        if (existing.isPresent()) {
            ga = existing.get();
            ga.setRole(role);
        } else {
            ga = new PlatformGroupAdmin();
            ga.setGroupId(id);
            ga.setUserId(targetUserId);
            ga.setRole(role);
        }
        groupAdminRepository.save(ga);

        // 确保用户持有 GROUP_OWNER 角色码（用于后台访问权限）
        adminAuthService.ensureGroupOwnerRole(targetUserId);
        return Map.of("userId", targetUserId, "role", role,
                "roleName", GroupAdminRoleConstants.displayName(role), "message", "管理员已添加");
    }

    @DeleteMapping("/{id}/admins/{userId}")
    @Transactional
    public Map<String, Object> removeGroupAdmin(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String id,
            @PathVariable Long userId
    ) {
        User operator = adminAuthService.requireGroupOwner(authHeader, id);
        if (operator.getUserid().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "不能移除自己（团体拥有者）");
        }
        // 不能移除其他 OWNER
        groupAdminRepository.findByGroupIdAndUserId(id, userId).ifPresent(ga -> {
            if (GroupAdminRoleConstants.isOwner(ga.getRole())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "不能移除团体拥有者");
            }
        });
        groupAdminRepository.deleteByGroupIdAndUserId(id, userId);
        adminAuthService.cleanGroupOwnerRoleIfUnused(userId);
        return Map.of("userId", userId, "message", "管理员已移除");
    }

    // ── 私有工具 ──────────────────────────────────────────────────────────────

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
        item.put("ownerUserId", g.getOwnerUserId());
        item.put("createdAt", g.getCreatedAt());
        item.put("updatedAt", g.getUpdatedAt());
        return item;
    }

    private List<String> buildGroupPermissions(String role) {
        if (role == null) return Collections.emptyList();
        List<String> perms = new ArrayList<>();
        perms.add("group.review.member");
        if (GroupAdminRoleConstants.canManage(role)) {
            perms.add("group.edit.info");
            perms.add("group.manage.notice");
            perms.add("group.manage.post");
            perms.add("group.remove.member");
        }
        if (GroupAdminRoleConstants.isOwner(role)) {
            perms.add("group.manage.admins");
            perms.add("group.delete");
        }
        return perms;
    }
}
