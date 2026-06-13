package com.lovecube.backend.controllers;

import com.lovecube.backend.entity.GroupJoinRequest;
import com.lovecube.backend.entity.GroupMember;
import com.lovecube.backend.entity.PlatGroup;
import com.lovecube.backend.entity.PlatGroupMember;
import com.lovecube.backend.entity.PlatformGroup;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.GroupJoinRequestRepository;
import com.lovecube.backend.repository.GroupMemberRepository;
import com.lovecube.backend.repository.PlatGroupMemberRepository;
import com.lovecube.backend.repository.PlatGroupRepository;
import com.lovecube.backend.repository.PlatformGroupRepository;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.services.AdminAuthService;
import com.lovecube.backend.services.PlatformGroupSupport;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/platform/me")
public class PlatformMeController {

    private final PlatGroupRepository groupRepository;
    private final PlatGroupMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final AdminAuthService adminAuthService;
    private final PlatformGroupRepository platformGroupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupJoinRequestRepository groupJoinRequestRepository;

    public PlatformMeController(
            PlatGroupRepository groupRepository,
            PlatGroupMemberRepository memberRepository,
            UserRepository userRepository,
            AdminAuthService adminAuthService,
            PlatformGroupRepository platformGroupRepository,
            GroupMemberRepository groupMemberRepository,
            GroupJoinRequestRepository groupJoinRequestRepository) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.adminAuthService = adminAuthService;
        this.platformGroupRepository = platformGroupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupJoinRequestRepository = groupJoinRequestRepository;
    }

    @GetMapping("/groups")
    public Map<String, Object> myGroupsBucketed(@RequestHeader("Authorization") String authHeader) {
        User user = adminAuthService.requireUser(authHeader);
        List<PlatGroupMember> memberships = memberRepository.findByUserId(user.getUserid());

        List<Map<String, Object>> createdGroups = new ArrayList<>();
        List<Map<String, Object>> managedGroups = new ArrayList<>();
        List<Map<String, Object>> joinedGroups = new ArrayList<>();
        List<Map<String, Object>> legacyPlatPending = new ArrayList<>();

        Set<Long> groupIds = memberships.stream()
                .filter(m -> "approved".equals(m.getStatus()) || "pending".equals(m.getStatus()))
                .map(PlatGroupMember::getGroupId)
                .collect(Collectors.toSet());

        if (!groupIds.isEmpty()) {
            Map<Long, PlatGroup> groupMap = groupRepository.findAllById(groupIds).stream()
                    .filter(g -> "published".equals(g.getStatus()))
                    .collect(Collectors.toMap(PlatGroup::getId, g -> g));

            Set<Long> ownerIds = groupMap.values().stream()
                    .map(PlatGroup::getOwnerUserId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            Map<Long, User> ownerUsers = userRepository.findAllById(ownerIds).stream()
                    .collect(Collectors.toMap(User::getUserid, u -> u));

            for (PlatGroupMember m : memberships) {
                PlatGroup g = groupMap.get(m.getGroupId());
                if (g == null) continue;
                if (!("approved".equals(m.getStatus()) || "pending".equals(m.getStatus()))) continue;

                Map<Long, PlatGroupMember> single = Collections.singletonMap(g.getId(), m);
                Map<String, Object> summary = PlatformGroupSupport.buildGroupSummary(g, single, ownerUsers);

                if ("owner".equals(m.getRole())) {
                    createdGroups.add(summary);
                } else if ("admin".equals(m.getRole()) && "approved".equals(m.getStatus())) {
                    managedGroups.add(summary);
                } else if ("admin".equals(m.getRole()) && "pending".equals(m.getStatus())) {
                    legacyPlatPending.add(enhanceLegacyPlatPendingSummary(summary, m));
                } else if ("member".equals(m.getRole())) {
                    if ("pending".equals(m.getStatus())) {
                        legacyPlatPending.add(enhanceLegacyPlatPendingSummary(summary, m));
                    } else {
                        joinedGroups.add(summary);
                    }
                }
            }
        }

        appendModernPlatformGroupBuckets(user, createdGroups, managedGroups, joinedGroups);
        dedupeCrossTableGroups(createdGroups);
        dedupeCrossTableGroups(managedGroups);
        dedupeCrossTableGroups(joinedGroups);
        List<Map<String, Object>> pendingJoinGroups =
                buildPendingJoinGroups(user, createdGroups, managedGroups, joinedGroups);
        pendingJoinGroups.addAll(legacyPlatPending);
        sortPendingJoinGroupsByRequestedAtDesc(pendingJoinGroups);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("createdGroups", createdGroups);
        out.put("managedGroups", managedGroups);
        out.put("joinedGroups", joinedGroups);
        out.put("pendingJoinGroups", pendingJoinGroups);
        return out;
    }

    /**
     * 已提交入团申请、尚未通过审核：platform_groups / group_join_requests（新表），
     * 与 legacy platform_group_member.status = pending 合并前由调用方去重。
     */
    private List<Map<String, Object>> buildPendingJoinGroups(
            User user,
            List<Map<String, Object>> createdGroups,
            List<Map<String, Object>> managedGroups,
            List<Map<String, Object>> joinedGroups) {
        Long uid = user.getUserid();
        Set<String> alreadyInBuckets = new HashSet<>();
        for (List<Map<String, Object>> bucket : List.of(createdGroups, managedGroups, joinedGroups)) {
            for (Map<String, Object> row : bucket) {
                if (row.get("id") != null) {
                    alreadyInBuckets.add(String.valueOf(row.get("id")));
                }
            }
        }

        List<Map<String, Object>> out = new ArrayList<>();
        List<GroupJoinRequest> pending =
                groupJoinRequestRepository.findByUserIdAndStatusOrderByRequestedAtDesc(uid, "pending");
        Set<String> seenGroupIds = new HashSet<>();
        for (GroupJoinRequest req : pending) {
            String gid = req.getGroupId();
            if (gid == null || gid.isBlank()) {
                continue;
            }
            if (alreadyInBuckets.contains(gid) || groupMemberRepository.existsByGroupIdAndUserId(gid, uid)) {
                continue;
            }
            Optional<PlatformGroup> opt = platformGroupRepository.findById(gid);
            if (opt.isEmpty() || !"active".equals(opt.get().getStatus())) {
                continue;
            }
            if (!seenGroupIds.add(gid)) {
                continue;
            }
            out.add(buildPendingJoinSummary(req, opt.get()));
        }
        return out;
    }

    private Map<String, Object> buildPendingJoinSummary(GroupJoinRequest req, PlatformGroup g) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", g.getId());
        item.put("name", g.getName());
        item.put("description", g.getDescription());
        item.put("category", g.getCategory() != null ? g.getCategory() : "");
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
        item.put("ownerUserId", ownerUid);
        item.put("ownerName", ownerName);
        item.put("createdBy", g.getCreatedBy());
        item.put("tags", "");
        item.put("region", g.getCategory() != null && !g.getCategory().isBlank() ? g.getCategory() : "");
        item.put("location", item.get("region"));
        item.put("isMember", false);
        item.put("hasPendingRequest", true);
        item.put("managed", false);
        item.put("canReviewJoins", false);
        item.put("isOwner", false);
        item.put("applicationPending", true);
        item.put("joinRequestId", req.getId());
        if (req.getMemberRealName() != null) {
            item.put("myMemberRealName", req.getMemberRealName());
        }
        if (req.getRequestedAt() != null) {
            item.put("requestedAt", req.getRequestedAt());
        }
        return item;
    }

    /** legacy 团体：待审成员/管理员，与「我加入的」分离，统一进 pendingJoinGroups */
    private Map<String, Object> enhanceLegacyPlatPendingSummary(Map<String, Object> summary, PlatGroupMember m) {
        Map<String, Object> item = new LinkedHashMap<>(summary);
        item.put("applicationPending", true);
        item.put("joinRequestId", m.getId());
        item.put("legacyPlatPending", true);
        LocalDateTime ra = m.getCreatedAt() != null ? m.getCreatedAt() : m.getJoinedAt();
        if (ra != null) {
            item.put("requestedAt", ra);
        }
        item.put("hasPendingRequest", true);
        item.put("isMember", false);
        item.put("managed", false);
        item.put("canReviewJoins", false);
        return item;
    }

    private void sortPendingJoinGroupsByRequestedAtDesc(List<Map<String, Object>> rows) {
        rows.sort(Comparator.comparing(
                (Map<String, Object> row) -> {
                    Object r = row.get("requestedAt");
                    if (r instanceof LocalDateTime ldt) {
                        return ldt;
                    }
                    Object c = row.get("createdAt");
                    if (c instanceof LocalDateTime ldt2) {
                        return ldt2;
                    }
                    return LocalDateTime.MIN;
                },
                Comparator.nullsFirst(Comparator.naturalOrder())
        ).reversed());
    }

    /**
     * platform_groups + group_members：与团体大厅新表一致，合并进「我的团体」三个分桶。
     * 若与 legacy 列表 id 重复则跳过（迁移期偶发）。
     */
    private void appendModernPlatformGroupBuckets(
            User user,
            List<Map<String, Object>> createdGroups,
            List<Map<String, Object>> managedGroups,
            List<Map<String, Object>> joinedGroups) {
        Long uid = user.getUserid();
        Set<String> seen = new HashSet<>();
        Set<String> seenCrossTableKeys = new HashSet<>();
        for (List<Map<String, Object>> bucket : List.of(createdGroups, managedGroups, joinedGroups)) {
            for (Map<String, Object> row : bucket) {
                if (row.get("id") != null) {
                    seen.add(String.valueOf(row.get("id")));
                }
                String crossKey = groupCrossTableDedupeKey(row);
                if (!crossKey.equals("|")) {
                    seenCrossTableKeys.add(crossKey);
                }
            }
        }

        List<GroupMember> modernRows = groupMemberRepository.findByUserId(uid);
        for (GroupMember gm : modernRows) {
            String gid = gm.getGroupId();
            if (gid == null || gid.isBlank()) {
                continue;
            }
            Optional<PlatformGroup> opt = platformGroupRepository.findById(gid);
            if (opt.isEmpty() || !"active".equals(opt.get().getStatus())) {
                continue;
            }
            Map<String, Object> summary = buildModernGroupSummaryForMe(opt.get(), gm, uid);
            String idStr = String.valueOf(summary.get("id"));
            if (seen.contains(idStr)) {
                continue;
            }
            String crossKey = groupCrossTableDedupeKey(summary);
            if (!crossKey.equals("|") && seenCrossTableKeys.contains(crossKey)) {
                continue;
            }
            seen.add(idStr);
            if (!crossKey.equals("|")) {
                seenCrossTableKeys.add(crossKey);
            }
            String role = gm.getRole() == null ? "" : gm.getRole().trim().toLowerCase();
            if ("owner".equals(role)) {
                createdGroups.add(summary);
            } else if ("admin".equals(role)) {
                managedGroups.add(summary);
            } else {
                joinedGroups.add(summary);
            }
        }
    }

    /** 字段与 {@link GroupController} 列表摘要对齐，供前台「我的团体」normalize 使用 */
    private Map<String, Object> buildModernGroupSummaryForMe(PlatformGroup g, GroupMember gm, Long currentUserId) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", g.getId());
        item.put("name", g.getName());
        item.put("description", g.getDescription());
        item.put("category", g.getCategory() != null ? g.getCategory() : "");
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
        item.put("ownerUserId", ownerUid);
        item.put("ownerName", ownerName);
        item.put("createdBy", g.getCreatedBy());
        item.put("tags", "");
        item.put("region", g.getCategory() != null && !g.getCategory().isBlank() ? g.getCategory() : "");
        item.put("location", item.get("region"));

        item.put("isMember", true);
        item.put("hasPendingRequest", groupJoinRequestRepository.existsByGroupIdAndUserIdAndStatus(
                g.getId(), currentUserId, "pending"));
        User cu = userRepository.findById(currentUserId).orElse(null);
        boolean memberMgr = gm != null && ("admin".equalsIgnoreCase(gm.getRole()) || "owner".equalsIgnoreCase(gm.getRole()));
        boolean managed = (cu != null && adminAuthService.hasPlatformGroupManageAccess(cu, g.getId())) || memberMgr;
        item.put("managed", managed);
        item.put("canReviewJoins", (cu != null && adminAuthService.hasPlatformGroupJoinReviewAccess(cu, g.getId())) || memberMgr);
        boolean isOwnerMember = gm != null && "owner".equalsIgnoreCase(gm.getRole());
        boolean isOwnerUid = ownerUid != null && ownerUid.equals(currentUserId);
        item.put("isOwner", isOwnerMember || isOwnerUid || managed);
        item.put("myRole", gm != null ? gm.getRole() : null);
        if (gm != null && gm.getMemberRealName() != null) {
            item.put("myMemberRealName", gm.getMemberRealName());
        }
        return item;
    }

    /**
     * 迁移期：legacy platform_group 与 platform_groups 可能同名同团长各一条，按「名称+团长」去重，优先保留数字 ID（Space 运营台）。
     */
    private void dedupeCrossTableGroups(List<Map<String, Object>> bucket) {
        Map<String, Map<String, Object>> byKey = new LinkedHashMap<>();
        for (Map<String, Object> row : bucket) {
            String key = groupCrossTableDedupeKey(row);
            if ("|".equals(key)) {
                key = "id:" + row.get("id");
            }
            byKey.merge(key, row, PlatformMeController::pickPreferredCrossTableDuplicate);
        }
        bucket.clear();
        bucket.addAll(byKey.values());
    }

    private static String groupCrossTableDedupeKey(Map<String, Object> row) {
        String name = row.get("name") != null ? String.valueOf(row.get("name")).trim().toLowerCase(Locale.ROOT) : "";
        Object owner = row.get("ownerUserId") != null ? row.get("ownerUserId") : row.get("createdBy");
        return name + "|" + (owner != null ? String.valueOf(owner) : "");
    }

    private static boolean isNumericGroupId(Object id) {
        if (id == null) {
            return false;
        }
        return id.toString().matches("\\d+");
    }

    private static Map<String, Object> pickPreferredCrossTableDuplicate(
            Map<String, Object> existing,
            Map<String, Object> incoming) {
        boolean existingNumeric = isNumericGroupId(existing.get("id"));
        boolean incomingNumeric = isNumericGroupId(incoming.get("id"));
        if (existingNumeric && !incomingNumeric) {
            return existing;
        }
        if (incomingNumeric && !existingNumeric) {
            return incoming;
        }
        int existingCount = memberCountFromSummary(existing);
        int incomingCount = memberCountFromSummary(incoming);
        return incomingCount > existingCount ? incoming : existing;
    }

    private static int memberCountFromSummary(Map<String, Object> row) {
        Object mc = row.get("memberCount");
        if (mc instanceof Number n) {
            return n.intValue();
        }
        return 0;
    }
}
