package com.lovecube.backend.controllers;

import com.lovecube.backend.entity.PlatGroup;
import com.lovecube.backend.entity.PlatGroupMember;
import com.lovecube.backend.entity.PlatGroupNotice;
import com.lovecube.backend.entity.PlatGroupPost;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.PlatGroupMemberRepository;
import com.lovecube.backend.repository.PlatGroupNoticeRepository;
import com.lovecube.backend.repository.PlatGroupPostRepository;
import com.lovecube.backend.repository.PlatGroupRepository;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.PlatformGroupSupport;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/platform/groups")
public class PlatformGroupController {

    private final PlatGroupRepository groupRepository;
    private final PlatGroupMemberRepository memberRepository;
    private final PlatGroupPostRepository postRepository;
    private final PlatGroupNoticeRepository noticeRepository;
    private final UserRepository userRepository;
    private final AdminAuthService adminAuthService;

    public PlatformGroupController(
            PlatGroupRepository groupRepository,
            PlatGroupMemberRepository memberRepository,
            PlatGroupPostRepository postRepository,
            PlatGroupNoticeRepository noticeRepository,
            UserRepository userRepository,
            AdminAuthService adminAuthService) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.postRepository = postRepository;
        this.noticeRepository = noticeRepository;
        this.userRepository = userRepository;
        this.adminAuthService = adminAuthService;
    }


    @GetMapping
    public Map<String, Object> listGroups(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String joinMode,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "20") int pageSize,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long currentUserId = resolveOptionalUserId(authHeader);
        List<PlatGroup> groups = "all".equals(status)
                ? groupRepository.findAllByOrderByMemberCountDescCreatedAtDesc()
                : groupRepository.findByStatusOrderByMemberCountDescCreatedAtDesc(
                        (status != null && !status.isBlank()) ? status : "published");

        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim().toLowerCase();
            groups = groups.stream()
                    .filter(g -> g.getName().toLowerCase().contains(kw)
                            || (g.getDescription() != null && g.getDescription().toLowerCase().contains(kw)))
                    .collect(Collectors.toList());
        }
        if (region != null && !region.isBlank()) {
            groups = groups.stream().filter(g -> region.equals(g.getRegion())).collect(Collectors.toList());
        }
        String typeFilter = (type != null && !type.isBlank()) ? type : category;
        if (typeFilter != null && !typeFilter.isBlank()) {
            groups = groups.stream().filter(g -> typeFilter.equals(g.getType())).collect(Collectors.toList());
        }
        if (joinMode != null && !joinMode.isBlank()) {
            groups = groups.stream()
                    .filter(g -> PlatformGroupSupport.matchesJoinModeFilter(g.getJoinMode(), joinMode))
                    .collect(Collectors.toList());
        }
        if ("newest".equals(sort)) {
            groups = groups.stream()
                    .sorted(Comparator.comparing(PlatGroup::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.toList());
        }

        Map<Long, PlatGroupMember> memberMap = loadUserMemberMap(currentUserId);
        Map<Long, User> ownerUsers = loadOwnersForGroups(groups);
        List<Map<String, Object>> summaries = groups.stream()
                .map(g -> PlatformGroupSupport.buildGroupSummary(g, memberMap, ownerUsers))
                .collect(Collectors.toList());

        int safePage = Math.max(1, page);
        int safeSize = Math.min(100, Math.max(1, pageSize));
        int total = summaries.size();
        int from = Math.min((safePage - 1) * safeSize, total);
        int to = Math.min(from + safeSize, total);
        List<Map<String, Object>> items = from < total ? summaries.subList(from, to) : Collections.emptyList();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("items", items);
        body.put("total", total);
        body.put("page", safePage);
        body.put("pageSize", safeSize);
        return body;
    }


    @GetMapping("/hot")
    public List<Map<String, Object>> hotGroups(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long currentUserId = resolveOptionalUserId(authHeader);
        List<PlatGroup> groups = groupRepository.findTop5ByStatusOrderByMemberCountDesc("published");
        Map<Long, PlatGroupMember> memberMap = loadUserMemberMap(currentUserId);
        Map<Long, User> ownerUsers = loadOwnersForGroups(groups);
        return groups.stream().map(g -> PlatformGroupSupport.buildGroupSummary(g, memberMap, ownerUsers)).collect(Collectors.toList());
    }


    @GetMapping("/feed")
    public List<Map<String, Object>> feed() {
        List<PlatGroupPost> posts = postRepository.findTop20ByStatusOrderByCreatedAtDesc("published");

        Set<Long> groupIds = posts.stream().map(PlatGroupPost::getGroupId).collect(Collectors.toSet());
        Map<Long, PlatGroup> groupMap = groupRepository.findAllById(groupIds).stream()
                .collect(Collectors.toMap(PlatGroup::getId, g -> g));

        Set<Long> userIds = posts.stream().map(PlatGroupPost::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserid, u -> u));

        return posts.stream().map(p -> {
            PlatGroup g = groupMap.get(p.getGroupId());
            User u = userMap.get(p.getUserId());
            String text = p.getContent().length() > 50
                    ? p.getContent().substring(0, 50) + "..."
                    : p.getContent();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", p.getId());
            item.put("groupId", p.getGroupId());
            item.put("groupName", g != null ? g.getName() : "");
            item.put("avatarUrl", g != null ? g.getCoverUrl() : "");
            item.put("text", text);
            item.put("time", formatRelativeTime(p.getCreatedAt()));
            item.put("authorName", u != null ? u.getUsername() : "");
            item.put("content", p.getContent());
            item.put("createdAt", p.getCreatedAt());
            return item;
        }).collect(Collectors.toList());
    }


    @GetMapping("/my")
    public List<Map<String, Object>> myGroups(
            @RequestHeader("Authorization") String authHeader) {

        User user = adminAuthService.requireUser(authHeader);
        List<PlatGroupMember> memberships = memberRepository.findByUserId(user.getUserid());

        List<PlatGroupMember> relevant = memberships.stream()
                .filter(m -> "approved".equals(m.getStatus()) || "pending".equals(m.getStatus()))
                .collect(Collectors.toList());

        if (relevant.isEmpty()) return Collections.emptyList();

        Set<Long> groupIds = relevant.stream().map(PlatGroupMember::getGroupId).collect(Collectors.toSet());
        Map<Long, PlatGroup> groupMap = groupRepository.findAllById(groupIds).stream()
                .collect(Collectors.toMap(PlatGroup::getId, g -> g));
        Map<Long, PlatGroupMember> memberMap = relevant.stream()
                .collect(Collectors.toMap(PlatGroupMember::getGroupId, m -> m, (a, b) -> a));

        Map<Long, User> ownerUsers = loadOwnersForGroups(
                groupMap.values().stream()
                        .filter(g -> "published".equals(g.getStatus()))
                        .collect(Collectors.toList()));

        return relevant.stream()
                .map(m -> {
                    PlatGroup group = groupMap.get(m.getGroupId());
                    if (group == null || !"published".equals(group.getStatus())) return null;
                    return PlatformGroupSupport.buildGroupSummary(group, memberMap, ownerUsers);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }


    @GetMapping("/{idOrSlug}")
    public Map<String, Object> getGroup(
            @PathVariable String idOrSlug,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        PlatGroup group = resolveGroup(idOrSlug);
        Long currentUserId = resolveOptionalUserId(authHeader);
        Map<Long, PlatGroupMember> memberMap = loadUserMemberMap(currentUserId);
        Map<Long, User> ownerUsers = loadOwnersForGroups(Collections.singletonList(group));
        Map<String, Object> result = PlatformGroupSupport.buildGroupSummary(group, memberMap, ownerUsers);

        // Include latest notice
        List<PlatGroupNotice> notices = noticeRepository
                .findByGroupIdAndStatusOrderByCreatedAtDesc(group.getId(), "published");
        if (!notices.isEmpty()) {
            PlatGroupNotice n = notices.get(0);
            Map<String, Object> notice = new LinkedHashMap<>();
            notice.put("id", n.getId());
            notice.put("title", n.getTitle());
            notice.put("content", n.getContent());
            notice.put("createdAt", n.getCreatedAt());
            result.put("latestNotice", notice);
        }

        // Include admins (owner + admin roles)
        List<PlatGroupMember> admins = memberRepository
                .findByGroupIdAndStatusOrderByJoinedAtAsc(group.getId(), "approved")
                .stream()
                .filter(m -> "owner".equals(m.getRole()) || "admin".equals(m.getRole()))
                .collect(Collectors.toList());
        Set<Long> adminUserIds = admins.stream().map(PlatGroupMember::getUserId).collect(Collectors.toSet());
        Map<Long, User> adminUsers = userRepository.findAllById(adminUserIds).stream()
                .collect(Collectors.toMap(User::getUserid, u -> u));
        result.put("admins", admins.stream().map(m -> {
            User u = adminUsers.get(m.getUserId());
            Map<String, Object> a = new LinkedHashMap<>();
            a.put("userId", m.getUserId());
            a.put("role", m.getRole());
            a.put("name", u != null ? u.getUsername() : "");
            a.put("avatar", u != null ? u.getProfilePhoto() : "");
            return a;
        }).collect(Collectors.toList()));

        return result;
    }



    @PostMapping
    @Transactional
    public Map<String, Object> createGroup(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> payload) {

        User user = adminAuthService.requireUser(authHeader);

        String name = String.valueOf(payload.getOrDefault("name", "")).trim();
        if (name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "团体名称不能为空");
        }

        PlatGroup group = new PlatGroup();
        group.setName(name);
        group.setType(String.valueOf(payload.getOrDefault("type", "region")));
        group.setRegion(String.valueOf(payload.getOrDefault("region", "")));
        group.setDescription(String.valueOf(payload.getOrDefault("description", "")));
        group.setCoverUrl((String) payload.get("coverUrl"));
        group.setJoinMode(PlatformGroupSupport.normalizeJoinModeForStore(payload.get("joinMode")));
        if (payload.containsKey("tags")) {
            String t = String.valueOf(payload.get("tags")).trim();
            group.setTags(t.isBlank() ? null : t);
        }
        group.setOwnerUserId(user.getUserid());
        group.setMemberCount(1);
        group.setStatus("published");
        group.setCreatedAt(LocalDateTime.now());
        group.setUpdatedAt(LocalDateTime.now());

        String slug = (String) payload.get("slug");
        group.setSlug((slug != null && !slug.isBlank()) ? slug : "group-" + System.currentTimeMillis());

        PlatGroup saved = groupRepository.save(group);

        PlatGroupMember owner = new PlatGroupMember();
        owner.setGroupId(saved.getId());
        owner.setUserId(user.getUserid());
        owner.setRole("owner");
        owner.setStatus("approved");
        owner.setJoinedAt(LocalDateTime.now());
        owner.setCreatedAt(LocalDateTime.now());
        owner.setUpdatedAt(LocalDateTime.now());
        memberRepository.save(owner);

        return Map.of("id", saved.getId(), "slug", saved.getSlug(), "message", "Created successfully");
    }


    @PutMapping("/{id}")
    @Transactional
    public Map<String, Object> updateGroup(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> payload) {

        User user = adminAuthService.requireUser(authHeader);
        PlatGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        PlatGroupMember self = memberRepository.findByGroupIdAndUserId(id, user.getUserid())
                .filter(m -> "approved".equals(m.getStatus()))
                .orElse(null);

        boolean siteAdmin = adminAuthService.isAdmin(user);
        boolean ownerLike = siteAdmin || (self != null && "owner".equals(self.getRole()));
        boolean groupAdmin = self != null && "admin".equals(self.getRole());
        boolean canEdit = siteAdmin || ownerLike || groupAdmin;

        if (!canEdit) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No permission");
        }

        if (payload.containsKey("name") && ownerLike) {
            String name = String.valueOf(payload.get("name")).trim();
            if (!name.isBlank()) group.setName(name);
        }
        String type = strOf(payload, "type");
        if (type == null) type = strOf(payload, "category");
        if (type != null && !type.isBlank() && ownerLike) group.setType(type);

        if (payload.containsKey("region") && (ownerLike || groupAdmin)) {
            group.setRegion(String.valueOf(payload.get("region")));
        }
        if (payload.containsKey("description") && (ownerLike || groupAdmin)) {
            group.setDescription(String.valueOf(payload.get("description")));
        }
        if (payload.containsKey("coverUrl") && (ownerLike || groupAdmin)) {
            group.setCoverUrl(String.valueOf(payload.get("coverUrl")));
        }
        if (payload.containsKey("status") && ownerLike) {
            group.setStatus(String.valueOf(payload.get("status")));
        }
        if (payload.containsKey("tags") && ownerLike) {
            String t = String.valueOf(payload.get("tags")).trim();
            group.setTags(t.isBlank() ? null : t);
        }

        if ((payload.containsKey("joinMode") || payload.containsKey("joinType")) && ownerLike) {
            if (payload.containsKey("joinMode")) {
                group.setJoinMode(PlatformGroupSupport.normalizeJoinModeForStore(payload.get("joinMode")));
            } else {
                group.setJoinMode("open".equals(String.valueOf(payload.get("joinType"))) ? "free" : "audit");
            }
        }

        group.setUpdatedAt(LocalDateTime.now());
        PlatGroup saved = groupRepository.save(group);

        Map<Long, PlatGroupMember> selfMap = loadUserMemberMap(user.getUserid());
        Map<Long, User> owners = loadOwnersForGroups(Collections.singletonList(saved));
        return PlatformGroupSupport.buildGroupSummary(saved, selfMap, owners);
    }


    @PostMapping("/{id}/join")
    @Transactional
    public Map<String, Object> joinGroup(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody(required = false) Map<String, Object> payload) {

        User user = adminAuthService.requireUser(authHeader);
        PlatGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        if (!"published".equals(group.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group is not joinable");
        }

        if ("invite".equals(group.getJoinMode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "该团体仅限邀请加入");
        }

        String reason = payload != null ? String.valueOf(payload.getOrDefault("message", "")).trim() : "";
        boolean needApplyNote = "audit".equals(group.getJoinMode());
        if (needApplyNote && reason.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "申请验证信息不能为空");
        }
        if (reason.length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "申请验证信息不能超过255个字符");
        }

        Optional<PlatGroupMember> existing = memberRepository.findByGroupIdAndUserId(id, user.getUserid());
        if (existing.isPresent()) {
            PlatGroupMember m = existing.get();
            if ("approved".equals(m.getStatus())) {
                return Map.of("joined", true, "pending", false, "message", "Joined successfully");
            }
            if ("pending".equals(m.getStatus())) {
                return Map.of("joined", false, "pending", true, "message", "Request pending");
            }
            if ("free".equals(group.getJoinMode())) {
                m.setStatus("approved");
                m.setJoinedAt(LocalDateTime.now());
                m.setUpdatedAt(LocalDateTime.now());
                memberRepository.save(m);
                group.setMemberCount((group.getMemberCount() == null ? 0 : group.getMemberCount()) + 1);
                groupRepository.save(group);
                return Map.of("joined", true, "pending", false, "message", "Joined successfully");
            }
            m.setStatus("pending");
            m.setApplyReason(reason);
            m.setUpdatedAt(LocalDateTime.now());
            memberRepository.save(m);
            return Map.of("joined", false, "pending", true, "message", "Request submitted, waiting for approval");
        }

        PlatGroupMember member = new PlatGroupMember();
        member.setGroupId(id);
        member.setUserId(user.getUserid());
        member.setRole("member");
        member.setCreatedAt(LocalDateTime.now());
        member.setUpdatedAt(LocalDateTime.now());

        if ("free".equals(group.getJoinMode())) {
            member.setStatus("approved");
            member.setJoinedAt(LocalDateTime.now());
            memberRepository.save(member);
            group.setMemberCount((group.getMemberCount() == null ? 0 : group.getMemberCount()) + 1);
            groupRepository.save(group);
            return Map.of("joined", true, "pending", false, "message", "Joined successfully");
        }

        member.setStatus("pending");
        member.setApplyReason(reason);
        memberRepository.save(member);
        return Map.of("joined", false, "pending", true, "message", "Request submitted, waiting for approval");
    }


    @PostMapping("/{id}/leave")
    @Transactional
    public Map<String, Object> leaveGroup(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        User user = adminAuthService.requireUser(authHeader);
        PlatGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        PlatGroupMember member = memberRepository.findByGroupIdAndUserId(id, user.getUserid())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "You are not a group member"));

        if ("owner".equals(member.getRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Owner cannot leave group directly");
        }

        if ("approved".equals(member.getStatus())) {
            group.setMemberCount(Math.max(0, (group.getMemberCount() == null ? 0 : group.getMemberCount()) - 1));
            groupRepository.save(group);
        }

        member.setStatus("left");
        member.setUpdatedAt(LocalDateTime.now());
        memberRepository.save(member);

        return Map.of("left", true, "message", "Left group successfully");
    }


    @GetMapping("/{id}/members")
    public List<Map<String, Object>> getMembers(
            @PathVariable Long id,
            @RequestParam(defaultValue = "approved") String status,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        List<PlatGroupMember> members = "all".equals(status)
                ? memberRepository.findByGroupIdOrderByJoinedAtAsc(id)
                : memberRepository.findByGroupIdAndStatusOrderByJoinedAtAsc(id, status);

        Set<Long> userIds = members.stream().map(PlatGroupMember::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserid, u -> u));

        return members.stream().map(m -> {
            User u = userMap.get(m.getUserId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", m.getId());
            item.put("userId", m.getUserId());
            item.put("role", m.getRole());
            item.put("status", m.getStatus());
            item.put("joinedAt", m.getJoinedAt());
            item.put("applyReason", m.getApplyReason());
            item.put("requestedAt", m.getCreatedAt());
            item.put("username", u != null ? u.getUsername() : "");
            item.put("avatarUrl", u != null ? u.getProfilePhoto() : "");
            return item;
        }).collect(Collectors.toList());
    }

    @PostMapping("/{id}/members/{memberId}/approve")
    @Transactional
    public Map<String, Object> approveMember(
            @PathVariable Long id,
            @PathVariable Long memberId,
            @RequestHeader("Authorization") String authHeader) {

        requireManagerRole(id, authHeader);

        PlatGroupMember target = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member record not found"));
        if (!"pending".equals(target.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request status cannot be changed");
        }

        target.setStatus("approved");
        target.setJoinedAt(LocalDateTime.now());
        target.setUpdatedAt(LocalDateTime.now());
        memberRepository.save(target);

        PlatGroup group = groupRepository.findById(id).orElseThrow();
        group.setMemberCount((group.getMemberCount() == null ? 0 : group.getMemberCount()) + 1);
        groupRepository.save(group);

        return Map.of("approved", true, "message", "Request approved");
    }

    @PostMapping("/{id}/members/{memberId}/reject")
    @Transactional
    public Map<String, Object> rejectMember(
            @PathVariable Long id,
            @PathVariable Long memberId,
            @RequestHeader("Authorization") String authHeader) {

        requireManagerRole(id, authHeader);

        PlatGroupMember target = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member record not found"));
        if (!"pending".equals(target.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request status cannot be changed");
        }

        target.setStatus("rejected");
        target.setUpdatedAt(LocalDateTime.now());
        memberRepository.save(target);

        return Map.of("rejected", true, "message", "Request rejected");
    }

    @PutMapping("/{id}/members/{memberId}/audit")
    @Transactional
    public Map<String, Object> auditMemberPut(
            @PathVariable Long id,
            @PathVariable Long memberId,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody(required = false) Map<String, Object> body) {
        String action = body != null ? String.valueOf(body.getOrDefault("action", "approve")).trim().toLowerCase() : "approve";
        if ("reject".equals(action) || "rejected".equals(action)) {
            return rejectMember(id, memberId, authHeader);
        }
        return approveMember(id, memberId, authHeader);
    }

    @DeleteMapping("/{id}/members/{memberId}")
    @Transactional
    public Map<String, Object> removeGroupMember(
            @PathVariable Long id,
            @PathVariable Long memberId,
            @RequestHeader("Authorization") String authHeader) {

        requireManagerRole(id, authHeader);

        PlatGroupMember target = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member record not found"));
        if (!id.equals(target.getGroupId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid member");
        }
        if ("owner".equals(target.getRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot remove owner");
        }

        PlatGroup group = groupRepository.findById(id).orElseThrow();
        if ("approved".equals(target.getStatus())) {
            group.setMemberCount(Math.max(0, (group.getMemberCount() == null ? 0 : group.getMemberCount()) - 1));
            groupRepository.save(group);
        }

        target.setStatus("removed");
        target.setUpdatedAt(LocalDateTime.now());
        memberRepository.save(target);

        return Map.of("removed", true, "message", "Member removed");
    }


    @GetMapping("/{id}/posts")
    public List<Map<String, Object>> getPosts(@PathVariable Long id) {
        groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        List<PlatGroupPost> posts = postRepository.findByGroupIdAndStatusOrderByCreatedAtDesc(id, "published");
        Set<Long> userIds = posts.stream().map(PlatGroupPost::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserid, u -> u));

        return posts.stream().map(p -> {
            User u = userMap.get(p.getUserId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", p.getId());
            item.put("content", p.getContent());
            item.put("imageUrls", p.getImageUrls());
            item.put("type", "post");
            item.put("likeCount", 0);
            item.put("createdAt", p.getCreatedAt());
            item.put("authorName", u != null ? u.getUsername() : "");
            item.put("authorAvatar", u != null ? u.getProfilePhoto() : "");
            return item;
        }).collect(Collectors.toList());
    }

    @PostMapping("/{id}/posts")
    @Transactional
    public Map<String, Object> createPost(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> payload) {

        User user = adminAuthService.requireUser(authHeader);
        memberRepository.findByGroupIdAndUserId(id, user.getUserid())
                .filter(m -> "approved".equals(m.getStatus()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Join the group before posting"));

        PlatGroupPost post = new PlatGroupPost();
        post.setGroupId(id);
        post.setUserId(user.getUserid());
        post.setContent(String.valueOf(payload.getOrDefault("content", "")));
        post.setImageUrls((String) payload.get("imageUrls"));
        post.setStatus("published");
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);

        return Map.of("id", post.getId(), "message", "Post published successfully");
    }


    @GetMapping("/{id}/notices")
    public List<Map<String, Object>> getNotices(@PathVariable Long id) {
        groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        return noticeRepository.findByGroupIdAndStatusOrderByCreatedAtDesc(id, "published").stream()
                .map(n -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", n.getId());
                    item.put("title", n.getTitle());
                    item.put("content", n.getContent());
                    item.put("createdAt", n.getCreatedAt());
                    return item;
                }).collect(Collectors.toList());
    }

    @PostMapping("/{id}/notices")
    @Transactional
    public Map<String, Object> createNotice(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> payload) {

        User user = adminAuthService.requireUser(authHeader);
        requireManagerRole(id, authHeader);

        PlatGroupNotice notice = new PlatGroupNotice();
        notice.setGroupId(id);
        notice.setTitle(String.valueOf(payload.getOrDefault("title", "")));
        notice.setContent(String.valueOf(payload.getOrDefault("content", "")));
        notice.setCreatedBy(user.getUserid());
        notice.setStatus("published");
        notice.setCreatedAt(LocalDateTime.now());
        notice.setUpdatedAt(LocalDateTime.now());
        noticeRepository.save(notice);

        return Map.of("id", notice.getId(), "message", "Notice published successfully");
    }


    private Map<Long, User> loadOwnersForGroups(List<PlatGroup> groups) {
        if (groups == null || groups.isEmpty()) return Collections.emptyMap();
        Set<Long> ids = groups.stream().map(PlatGroup::getOwnerUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (ids.isEmpty()) return Collections.emptyMap();
        return userRepository.findAllById(ids).stream().collect(Collectors.toMap(User::getUserid, u -> u));
    }

    private Map<Long, PlatGroupMember> loadUserMemberMap(Long userId) {
        if (userId == null) return Collections.emptyMap();
        return memberRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(PlatGroupMember::getGroupId, m -> m, (a, b) -> a));
    }

    private PlatGroup resolveGroup(String idOrSlug) {
        try {
            long id = Long.parseLong(idOrSlug);
            return groupRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));
        } catch (NumberFormatException e) {
            return groupRepository.findBySlug(idOrSlug)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));
        }
    }

    private String readString(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private String strOf(Map<String, Object> m, String key) {
        return m.containsKey(key) && m.get(key) != null ? String.valueOf(m.get(key)) : null;
    }

    private Long resolveOptionalUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        try {
            return adminAuthService.requireUser(authHeader).getUserid();
        } catch (Exception e) {
            return null;
        }
    }

    private void requireManagerRole(Long groupId, String authHeader) {
        User user = adminAuthService.requireUser(authHeader);
        if (adminAuthService.isAdmin(user)) {
            return;
        }
        memberRepository.findByGroupIdAndUserId(groupId, user.getUserid())
                .filter(m -> "approved".equals(m.getStatus())
                        && ("owner".equals(m.getRole()) || "admin".equals(m.getRole())))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No permission"));
    }

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("MM-dd HH:mm");

    private String formatRelativeTime(LocalDateTime dt) {
        if (dt == null) return "";
        long minutes = java.time.Duration.between(dt, LocalDateTime.now()).toMinutes();
        if (minutes < 1) return "just now";
        if (minutes < 60) return minutes + " minutes ago";
        if (minutes < 1440) return (minutes / 60) + " hours ago";
        return dt.format(DF);
    }
}
