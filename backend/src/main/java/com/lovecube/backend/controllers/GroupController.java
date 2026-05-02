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
import com.lovecube.backend.services.GroupAdminRoleConstants;
import com.lovecube.backend.services.GrowthService;
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
    private final GrowthService growthService;

    public GroupController(
            PlatformGroupRepository groupRepository,
            GroupMemberRepository memberRepository,
            GroupPostRepository postRepository,
            GroupJoinRequestRepository joinRequestRepository,
            UserRepository userRepository,
            AdminAuthService adminAuthService,
            GrowthService growthService
    ) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.postRepository = postRepository;
        this.joinRequestRepository = joinRequestRepository;
        this.userRepository = userRepository;
        this.adminAuthService = adminAuthService;
        this.growthService = growthService;
    }

    @GetMapping
    public Map<String, Object> listGroups(
            @RequestParam(defaultValue = "active") String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String joinMode,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "20") int pageSize,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        String categoryFilter = (category != null && !category.isBlank()) ? category : type;
        List<PlatformGroup> groups = groupRepository.findByStatusOrderByPinnedDescCreatedAtDesc(status);
        if (categoryFilter != null && !categoryFilter.isBlank()) {
            groups = groups.stream().filter(g -> categoryFilter.equals(g.getCategory())).collect(Collectors.toList());
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim().toLowerCase();
            groups = groups.stream()
                    .filter(g -> g.getName().toLowerCase().contains(kw)
                            || (g.getDescription() != null && g.getDescription().toLowerCase().contains(kw)))
                    .collect(Collectors.toList());
        }
        if (joinMode != null && !joinMode.isBlank()) {
            groups = groups.stream()
                    .filter(g -> matchesJoinModeForList(g.getJoinType(), joinMode))
                    .collect(Collectors.toList());
        }
        if ("newest".equals(sort)) {
            groups = groups.stream()
                    .sorted(Comparator.comparing(PlatformGroup::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.toList());
        } else {
            groups = groups.stream()
                    .sorted(Comparator.comparing(PlatformGroup::getMemberCount, Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.toList());
        }
        Long currentUserId = resolveOptionalUserId(authHeader);
        int safePage = Math.max(1, page);
        int safeSize = Math.min(100, Math.max(1, pageSize));
        int total = groups.size();
        int from = Math.min((safePage - 1) * safeSize, total);
        int to = Math.min(from + safeSize, total);
        List<PlatformGroup> pageGroups = from < total ? groups.subList(from, to) : Collections.emptyList();
        List<Map<String, Object>> items = buildGroupSummariesBatch(pageGroups, currentUserId);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("items", items);
        body.put("total", total);
        body.put("page", safePage);
        body.put("pageSize", safeSize);
        return body;
    }

    /** 登录用户创建团体：写入 platform_groups + platform_group_admin(OWNER) + 创建者为成员 */
    @PostMapping
    @Transactional
    public Map<String, Object> createGroupForUser(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> payload
    ) {
        User user = adminAuthService.requireUser(authHeader);
        String name = String.valueOf(payload.getOrDefault("name", "")).trim();
        if (name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "团体名称不能为空");
        }
        PlatformGroup group = new PlatformGroup();
        group.setId("group-" + UUID.randomUUID());
        group.setName(name);
        group.setDescription(String.valueOf(payload.getOrDefault("description", "")));
        Object typeObj = payload.containsKey("type") ? payload.get("type") : payload.get("category");
        group.setCategory(typeObj != null ? String.valueOf(typeObj) : "");
        group.setCoverUrl(payload.get("coverUrl") != null ? String.valueOf(payload.get("coverUrl")) : "");
        group.setStatus("active");
        String jm = String.valueOf(payload.getOrDefault("joinMode", "audit"));
        group.setJoinType("open".equals(jm) ? "open" : "approval");
        group.setMemberCount(1);
        group.setPinned(false);
        group.setCreatedBy(user.getUserid());
        group.setOwnerUserId(user.getUserid());
        group.setCreatedAt(LocalDateTime.now());
        group.setUpdatedAt(LocalDateTime.now());
        PlatformGroup saved = groupRepository.save(group);

        adminAuthService.upsertPlatformGroupAdmin(saved.getId(), user.getUserid(), GroupAdminRoleConstants.OWNER);

        GroupMember member = new GroupMember();
        member.setGroupId(saved.getId());
        member.setUserId(user.getUserid());
        member.setRole("owner");
        member.setJoinedAt(LocalDateTime.now());
        memberRepository.save(member);
        growthService.recordAction(user.getUserid(), "JOIN_GROUP", "CREATE_GROUP_" + saved.getId());

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", saved.getId());
        out.put("message", "创建成功");
        return out;
    }

    private static boolean matchesJoinModeForList(String joinType, String filter) {
        if (joinType == null) return false;
        if ("open".equals(filter)) return "open".equals(joinType);
        if ("audit".equals(filter) || "invite".equals(filter)) {
            return "approval".equals(joinType) || "audit".equals(joinType);
        }
        return true;
    }

    @GetMapping("/hot")
    public List<Map<String, Object>> hotGroups(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long currentUserId = resolveOptionalUserId(authHeader);
        List<PlatformGroup> all = groupRepository.findByStatusOrderByPinnedDescCreatedAtDesc("active");
        List<PlatformGroup> top = all.stream()
                .sorted(Comparator.comparing(PlatformGroup::getMemberCount, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .collect(Collectors.toList());
        return buildGroupSummariesBatch(top, currentUserId);
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
        Long ou = group.getOwnerUserId() != null ? group.getOwnerUserId() : group.getCreatedBy();
        List<Map<String, Object>> adminList = new ArrayList<>();
        if (ou != null) {
            userRepository.findById(ou).ifPresent(u -> {
                Map<String, Object> a = new LinkedHashMap<>();
                a.put("userId", u.getUserid());
                a.put("name", u.getUsername());
                a.put("role", "owner");
                a.put("avatar", u.getProfilePhoto());
                adminList.add(a);
            });
        }
        result.put("admins", adminList);
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
            item.put("id", m.getId());
            item.put("userId", m.getUserId());
            item.put("role", m.getRole());
            item.put("status", "approved");
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

    @PostMapping("/{id}/posts")
    @Transactional
    public GroupPost createMemberPost(
            @PathVariable String id,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> payload
    ) {
        User user = adminAuthService.requireUser(authHeader);
        groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "团体不存在"));
        if (!memberRepository.existsByGroupIdAndUserId(id, user.getUserid())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅限团体成员发布动态");
        }
        String content = String.valueOf(payload.getOrDefault("content", "")).trim();
        if (content.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "内容不能为空");
        }
        GroupPost post = new GroupPost();
        post.setId("gpost-" + UUID.randomUUID());
        post.setGroupId(id);
        post.setUserId(user.getUserid());
        post.setType(String.valueOf(payload.getOrDefault("type", "post")));
        post.setContent(content);
        post.setLikeCount(0);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        return postRepository.save(post);
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
            growthService.recordAction(user.getUserid(), "JOIN_GROUP", "JOIN_GROUP_" + id);
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

    /**
     * 列表页专用：对当前页批量查库，避免对每个团体重复查询（原实现为明显的 N+1）。
     */
    private List<Map<String, Object>> buildGroupSummariesBatch(List<PlatformGroup> groups, Long currentUserId) {
        if (groups.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> ownerUids = groups.stream()
                .map(g -> g.getOwnerUserId() != null ? g.getOwnerUserId() : g.getCreatedBy())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> ownerNames = ownerUids.isEmpty()
                ? Collections.emptyMap()
                : userRepository.findAllById(ownerUids).stream()
                .collect(Collectors.toMap(User::getUserid, u -> u.getUsername() != null ? u.getUsername() : ""));

        Set<String> pageGroupIds = groups.stream().map(PlatformGroup::getId).collect(Collectors.toSet());

        Map<String, GroupMember> memberByGroupId = Collections.emptyMap();
        Set<String> pendingGroupIds = Collections.emptySet();
        User batchUser = null;

        if (currentUserId != null) {
            memberByGroupId = memberRepository.findByUserId(currentUserId).stream()
                    .filter(m -> pageGroupIds.contains(m.getGroupId()))
                    .collect(Collectors.toMap(GroupMember::getGroupId, m -> m, (a, b) -> a));

            if (!pageGroupIds.isEmpty()) {
                pendingGroupIds = joinRequestRepository
                        .findByUserIdAndGroupIdInAndStatus(currentUserId, pageGroupIds, "pending")
                        .stream()
                        .map(GroupJoinRequest::getGroupId)
                        .collect(Collectors.toSet());
            }

            batchUser = userRepository.findById(currentUserId).orElse(null);
        }

        List<Map<String, Object>> out = new ArrayList<>(groups.size());
        for (PlatformGroup g : groups) {
            out.add(buildGroupSummaryFromBatch(
                    g, currentUserId, batchUser, ownerNames, memberByGroupId, pendingGroupIds));
        }
        return out;
    }

    private Map<String, Object> buildGroupSummaryFromBatch(
            PlatformGroup g,
            Long currentUserId,
            User batchUser,
            Map<Long, String> ownerNames,
            Map<String, GroupMember> memberByGroupId,
            Set<String> pendingGroupIds) {

        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", g.getId());
        item.put("name", g.getName());
        item.put("description", g.getDescription());
        item.put("category", g.getCategory());
        item.put("coverUrl", g.getCoverUrl());
        item.put("status", g.getStatus());
        String jt = g.getJoinType();
        item.put("joinType", jt);
        boolean isOpen = "open".equals(jt);
        item.put("joinMode", isOpen ? "free" : "audit");
        item.put("joinModeKey", isOpen ? "open" : "audit");
        item.put("memberCount", g.getMemberCount() == null ? 0 : g.getMemberCount());
        item.put("pinned", Boolean.TRUE.equals(g.getPinned()));
        item.put("createdAt", g.getCreatedAt());
        Long ownerUid = g.getOwnerUserId() != null ? g.getOwnerUserId() : g.getCreatedBy();
        String ownerName = ownerUid != null ? ownerNames.getOrDefault(ownerUid, "") : "";
        item.put("ownerName", ownerName);
        item.put("tags", "");
        if (currentUserId != null) {
            GroupMember gm = memberByGroupId.get(g.getId());
            item.put("isMember", gm != null);
            item.put("hasPendingRequest", pendingGroupIds.contains(g.getId()));
            boolean memberMgr = gm != null && ("owner".equalsIgnoreCase(gm.getRole()) || "admin".equalsIgnoreCase(gm.getRole()));
            boolean managed = (batchUser != null && adminAuthService.hasPlatformGroupManageAccess(batchUser, g.getId()))
                    || memberMgr;
            item.put("managed", managed);
            item.put("canReviewJoins", batchUser != null && adminAuthService.hasPlatformGroupJoinReviewAccess(batchUser, g.getId()));
            boolean isOwnerMember = gm != null && "owner".equalsIgnoreCase(gm.getRole());
            boolean ownerUidMatch = ownerUid != null && ownerUid.equals(currentUserId);
            item.put("isOwner", isOwnerMember || ownerUidMatch || managed);
        } else {
            item.put("isMember", false);
            item.put("hasPendingRequest", false);
            item.put("managed", false);
            item.put("canReviewJoins", false);
            item.put("isOwner", false);
        }
        return item;
    }

    private Map<String, Object> buildGroupSummary(PlatformGroup g, Long currentUserId) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", g.getId());
        item.put("name", g.getName());
        item.put("description", g.getDescription());
        item.put("category", g.getCategory());
        item.put("coverUrl", g.getCoverUrl());
        item.put("status", g.getStatus());
        String jt = g.getJoinType();
        item.put("joinType", jt);
        boolean isOpen = "open".equals(jt);
        item.put("joinMode", isOpen ? "free" : "audit");
        item.put("joinModeKey", isOpen ? "open" : "audit");
        item.put("memberCount", g.getMemberCount() == null ? 0 : g.getMemberCount());
        item.put("pinned", Boolean.TRUE.equals(g.getPinned()));
        item.put("createdAt", g.getCreatedAt());
        Long ownerUid = g.getOwnerUserId() != null ? g.getOwnerUserId() : g.getCreatedBy();
        String ownerName = "";
        if (ownerUid != null) {
            ownerName = userRepository.findById(ownerUid).map(User::getUsername).orElse("");
        }
        item.put("ownerName", ownerName);
        item.put("tags", "");
        if (currentUserId != null) {
            item.put("isMember", memberRepository.existsByGroupIdAndUserId(g.getId(), currentUserId));
            item.put("hasPendingRequest", joinRequestRepository.existsByGroupIdAndUserIdAndStatus(
                    g.getId(), currentUserId, "pending"));
            User cu = userRepository.findById(currentUserId).orElse(null);
            Optional<GroupMember> gm = memberRepository.findByGroupIdAndUserId(g.getId(), currentUserId);
            boolean memberMgr = gm.map(x -> "admin".equalsIgnoreCase(x.getRole()) || "owner".equalsIgnoreCase(x.getRole())).orElse(false);
            boolean managed = (cu != null && adminAuthService.hasPlatformGroupManageAccess(cu, g.getId())) || memberMgr;
            item.put("managed", managed);
            item.put("canReviewJoins", (cu != null && adminAuthService.hasPlatformGroupJoinReviewAccess(cu, g.getId())) || memberMgr);
            boolean isOwnerMember = gm.map(x -> "owner".equalsIgnoreCase(x.getRole())).orElse(false);
            boolean isOwnerUid = ownerUid != null && ownerUid.equals(currentUserId);
            item.put("isOwner", isOwnerMember || isOwnerUid || managed);
        } else {
            item.put("isMember", false);
            item.put("hasPendingRequest", false);
            item.put("managed", false);
            item.put("canReviewJoins", false);
            item.put("isOwner", false);
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
