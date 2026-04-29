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

    private static final Map<String, String> TYPE_LABELS = Map.of(
            "region", "地区团体",
            "church", "教会团体",
            "study", "学习小组",
            "interest", "兴趣团体",
            "family", "生活团契",
            "service", "事奉团队"
    );

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

    // ── List ──────────────────────────────────────────────────────────────────

    @GetMapping
    public List<Map<String, Object>> listGroups(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String sort,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Long currentUserId = resolveOptionalUserId(authHeader);
        List<PlatGroup> groups = groupRepository.findByStatusOrderByMemberCountDescCreatedAtDesc("published");

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
        if (type != null && !type.isBlank()) {
            groups = groups.stream().filter(g -> type.equals(g.getType())).collect(Collectors.toList());
        }
        if ("newest".equals(sort)) {
            groups = groups.stream()
                    .sorted(Comparator.comparing(PlatGroup::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.toList());
        }

        Map<Long, PlatGroupMember> memberMap = loadUserMemberMap(currentUserId);
        return groups.stream().map(g -> buildGroupSummary(g, memberMap)).collect(Collectors.toList());
    }

    // ── Hot ──────────────────────────────────────────────────────────────────

    @GetMapping("/hot")
    public List<Map<String, Object>> hotGroups(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long currentUserId = resolveOptionalUserId(authHeader);
        List<PlatGroup> groups = groupRepository.findTop5ByStatusOrderByMemberCountDesc("published");
        Map<Long, PlatGroupMember> memberMap = loadUserMemberMap(currentUserId);
        return groups.stream().map(g -> buildGroupSummary(g, memberMap)).collect(Collectors.toList());
    }

    // ── Feed ─────────────────────────────────────────────────────────────────

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

    // ── Detail ────────────────────────────────────────────────────────────────

    @GetMapping("/{idOrSlug}")
    public Map<String, Object> getGroup(
            @PathVariable String idOrSlug,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        PlatGroup group = resolveGroup(idOrSlug);
        Long currentUserId = resolveOptionalUserId(authHeader);
        Map<Long, PlatGroupMember> memberMap = loadUserMemberMap(currentUserId);
        Map<String, Object> result = buildGroupSummary(group, memberMap);

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

    // ── Create group ─────────────────────────────────────────────────────────

    @PostMapping
    @Transactional
    public Map<String, Object> createGroup(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> payload) {

        User user = adminAuthService.requireUser(authHeader);

        PlatGroup group = new PlatGroup();
        group.setName(String.valueOf(payload.getOrDefault("name", "")));
        group.setType(String.valueOf(payload.getOrDefault("type", "region")));
        group.setRegion(String.valueOf(payload.getOrDefault("region", "")));
        group.setDescription(String.valueOf(payload.getOrDefault("description", "")));
        group.setCoverUrl((String) payload.get("coverUrl"));
        group.setJoinMode(String.valueOf(payload.getOrDefault("joinMode", "audit")));
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

        return Map.of("id", saved.getId(), "slug", saved.getSlug(), "message", "创建成功");
    }

    // ── Join ──────────────────────────────────────────────────────────────────

    @PostMapping("/{id}/join")
    @Transactional
    public Map<String, Object> joinGroup(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody(required = false) Map<String, Object> payload) {

        User user = adminAuthService.requireUser(authHeader);
        PlatGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "团体不存在"));

        if (!"published".equals(group.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "该团体当前不可加入");
        }

        Optional<PlatGroupMember> existing = memberRepository.findByGroupIdAndUserId(id, user.getUserid());
        if (existing.isPresent()) {
            PlatGroupMember m = existing.get();
            if ("approved".equals(m.getStatus())) {
                return Map.of("joined", true, "pending", false, "message", "已加入");
            }
            if ("pending".equals(m.getStatus())) {
                return Map.of("joined", false, "pending", true, "message", "申请审核中");
            }
            // left or rejected — allow re-join/re-apply
            String reason = payload != null ? String.valueOf(payload.getOrDefault("message", "")) : "";
            if ("free".equals(group.getJoinMode())) {
                m.setStatus("approved");
                m.setJoinedAt(LocalDateTime.now());
                m.setUpdatedAt(LocalDateTime.now());
                memberRepository.save(m);
                group.setMemberCount((group.getMemberCount() == null ? 0 : group.getMemberCount()) + 1);
                groupRepository.save(group);
                return Map.of("joined", true, "pending", false, "message", "加入成功");
            }
            m.setStatus("pending");
            m.setApplyReason(reason);
            m.setUpdatedAt(LocalDateTime.now());
            memberRepository.save(m);
            return Map.of("joined", false, "pending", true, "message", "申请已提交，等待审核");
        }

        String reason = payload != null ? String.valueOf(payload.getOrDefault("message", "")) : "";
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
            return Map.of("joined", true, "pending", false, "message", "加入成功");
        }

        member.setStatus("pending");
        member.setApplyReason(reason);
        memberRepository.save(member);
        return Map.of("joined", false, "pending", true, "message", "申请已提交，等待审核");
    }

    // ── Leave ─────────────────────────────────────────────────────────────────

    @PostMapping("/{id}/leave")
    @Transactional
    public Map<String, Object> leaveGroup(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        User user = adminAuthService.requireUser(authHeader);
        PlatGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "团体不存在"));

        PlatGroupMember member = memberRepository.findByGroupIdAndUserId(id, user.getUserid())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "你不是该团体成员"));

        if ("owner".equals(member.getRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "团体所有者不能直接退出，请先转让团体");
        }

        if ("approved".equals(member.getStatus())) {
            group.setMemberCount(Math.max(0, (group.getMemberCount() == null ? 0 : group.getMemberCount()) - 1));
            groupRepository.save(group);
        }

        member.setStatus("left");
        member.setUpdatedAt(LocalDateTime.now());
        memberRepository.save(member);

        return Map.of("left", true, "message", "已退出团体");
    }

    // ── Members ───────────────────────────────────────────────────────────────

    @GetMapping("/{id}/members")
    public List<Map<String, Object>> getMembers(
            @PathVariable Long id,
            @RequestParam(defaultValue = "approved") String status,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "团体不存在"));

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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "成员记录不存在"));
        if (!"pending".equals(target.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "该申请状态无法操作");
        }

        target.setStatus("approved");
        target.setJoinedAt(LocalDateTime.now());
        target.setUpdatedAt(LocalDateTime.now());
        memberRepository.save(target);

        PlatGroup group = groupRepository.findById(id).orElseThrow();
        group.setMemberCount((group.getMemberCount() == null ? 0 : group.getMemberCount()) + 1);
        groupRepository.save(group);

        return Map.of("approved", true, "message", "已通过申请");
    }

    @PostMapping("/{id}/members/{memberId}/reject")
    @Transactional
    public Map<String, Object> rejectMember(
            @PathVariable Long id,
            @PathVariable Long memberId,
            @RequestHeader("Authorization") String authHeader) {

        requireManagerRole(id, authHeader);

        PlatGroupMember target = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "成员记录不存在"));
        if (!"pending".equals(target.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "该申请状态无法操作");
        }

        target.setStatus("rejected");
        target.setUpdatedAt(LocalDateTime.now());
        memberRepository.save(target);

        return Map.of("rejected", true, "message", "已拒绝申请");
    }

    // ── Posts ─────────────────────────────────────────────────────────────────

    @GetMapping("/{id}/posts")
    public List<Map<String, Object>> getPosts(@PathVariable Long id) {
        groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "团体不存在"));

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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "需要加入团体才能发帖"));

        PlatGroupPost post = new PlatGroupPost();
        post.setGroupId(id);
        post.setUserId(user.getUserid());
        post.setContent(String.valueOf(payload.getOrDefault("content", "")));
        post.setImageUrls((String) payload.get("imageUrls"));
        post.setStatus("published");
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);

        return Map.of("id", post.getId(), "message", "发布成功");
    }

    // ── Notices ───────────────────────────────────────────────────────────────

    @GetMapping("/{id}/notices")
    public List<Map<String, Object>> getNotices(@PathVariable Long id) {
        groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "团体不存在"));

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

        return Map.of("id", notice.getId(), "message", "公告发布成功");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Map<String, Object> buildGroupSummary(PlatGroup g, Map<Long, PlatGroupMember> memberMap) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", g.getId());
        item.put("slug", g.getSlug());
        item.put("name", g.getName());
        item.put("coverUrl", g.getCoverUrl());
        item.put("type", g.getType());
        String typeName = TYPE_LABELS.getOrDefault(g.getType(), g.getType());
        item.put("typeName", typeName);
        item.put("category", typeName);
        item.put("region", g.getRegion());
        item.put("location", g.getRegion());
        item.put("memberCount", g.getMemberCount() == null ? 0 : g.getMemberCount());
        item.put("description", g.getDescription());
        item.put("joinMode", g.getJoinMode());
        item.put("joinType", "free".equals(g.getJoinMode()) ? "open" : "approval");
        item.put("status", g.getStatus());
        item.put("createdAt", g.getCreatedAt());

        PlatGroupMember m = memberMap.get(g.getId());
        if (m != null) {
            boolean isApproved = "approved".equals(m.getStatus());
            boolean isPending = "pending".equals(m.getStatus());
            boolean isManager = isApproved && ("owner".equals(m.getRole()) || "admin".equals(m.getRole()));
            item.put("isMember", isApproved);
            item.put("managed", isManager);
            item.put("hasPendingRequest", isPending);
            item.put("myStatus", isManager ? "managed" : isApproved ? "joined" : isPending ? "pending" : "none");
        } else {
            item.put("isMember", false);
            item.put("managed", false);
            item.put("hasPendingRequest", false);
            item.put("myStatus", "none");
        }
        return item;
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
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "团体不存在"));
        } catch (NumberFormatException e) {
            return groupRepository.findBySlug(idOrSlug)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "团体不存在"));
        }
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
        memberRepository.findByGroupIdAndUserId(groupId, user.getUserid())
                .filter(m -> "approved".equals(m.getStatus())
                        && ("owner".equals(m.getRole()) || "admin".equals(m.getRole())))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "无权限操作"));
    }

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("MM-dd HH:mm");

    private String formatRelativeTime(LocalDateTime dt) {
        if (dt == null) return "";
        long minutes = java.time.Duration.between(dt, LocalDateTime.now()).toMinutes();
        if (minutes < 1) return "刚刚";
        if (minutes < 60) return minutes + " 分钟前";
        if (minutes < 1440) return (minutes / 60) + " 小时前";
        return dt.format(DF);
    }
}
