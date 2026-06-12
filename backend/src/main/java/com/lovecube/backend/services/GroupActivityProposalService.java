package com.lovecube.backend.services;

import com.lovecube.backend.entity.PlatGroup;
import com.lovecube.backend.entity.PlatGroupActivity;
import com.lovecube.backend.models.User;
import com.lovecube.backend.notification.NotificationCatalog;
import com.lovecube.backend.repository.PlatGroupActivityRepository;
import com.lovecube.backend.repository.PlatGroupMemberRepository;
import com.lovecube.backend.repository.PlatGroupRepository;
import com.lovecube.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GroupActivityProposalService {

    private static final List<String> MY_PROPOSAL_STATUSES = List.of("pending", "rejected", "published");

    private final PlatGroupRepository groupRepository;
    private final PlatGroupMemberRepository memberRepository;
    private final PlatGroupActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final AdminAuthService adminAuthService;
    private final NotificationService notificationService;

    public GroupActivityProposalService(
            PlatGroupRepository groupRepository,
            PlatGroupMemberRepository memberRepository,
            PlatGroupActivityRepository activityRepository,
            UserRepository userRepository,
            AdminAuthService adminAuthService,
            NotificationService notificationService
    ) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.adminAuthService = adminAuthService;
        this.notificationService = notificationService;
    }

    @Transactional
    public Map<String, Object> submitProposal(Long groupId, User user, Map<String, Object> payload) {
        requireApprovedMember(groupId, user);
        rejectManagerDirectProposal(groupId, user);

        PlatGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "团体不存在"));

        String title = String.valueOf(payload.getOrDefault("title", "")).trim();
        if (title.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "活动标题不能为空");
        }
        if (title.length() > 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "标题最多 200 字");
        }

        LocalDateTime startTime = parseRequiredTime(payload.get("startTime"), "开始时间");
        LocalDateTime endTime = parseRequiredTime(payload.get("endTime"), "结束时间");
        if (!endTime.isAfter(startTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "结束时间必须晚于开始时间");
        }

        String location = String.valueOf(payload.getOrDefault("location", "")).trim();

        LocalDateTime now = LocalDateTime.now();
        PlatGroupActivity activity = new PlatGroupActivity();
        activity.setGroupId(groupId);
        activity.setCreatorUserId(user.getUserid());
        activity.setTitle(title);
        activity.setDescription(String.valueOf(payload.getOrDefault("description", "")).trim());
        activity.setStartTime(startTime);
        activity.setEndTime(endTime);
        activity.setLocation(location.isBlank() ? null : location);
        activity.setMaxParticipants(0);
        activity.setStatus("pending");
        activity.setParticipantCount(0);
        activity.setCreatedAt(now);
        activity.setUpdatedAt(now);
        activityRepository.save(activity);

        notifyManagersProposalSubmitted(groupId, group, user, title);

        return Map.of(
                "id", activity.getId(),
                "status", activity.getStatus(),
                "message", "活动投稿已提交，等待审核"
        );
    }

    public Map<String, Object> listMyProposals(Long groupId, User user) {
        requireApprovedMember(groupId, user);
        groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "团体不存在"));

        List<PlatGroupActivity> all = activityRepository
                .findByGroupIdAndCreatorUserIdAndStatusInOrderByCreatedAtDesc(
                        groupId, user.getUserid(), MY_PROPOSAL_STATUSES);

        List<PlatGroupActivity> proposals = all.stream()
                .filter(a -> isMemberProposal(a))
                .toList();

        List<Map<String, Object>> items = proposals.stream()
                .map(this::buildProposalItem)
                .toList();

        return Map.of("items", items, "total", items.size());
    }

    public Map<String, Object> listPending(Long groupId, User user) {
        requireManagerOrSiteAdmin(groupId, user);
        groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "团体不存在"));

        List<PlatGroupActivity> pending = activityRepository
                .findByGroupIdAndStatusOrderByCreatedAtDesc(groupId, "pending");

        Set<Long> creatorIds = pending.stream()
                .map(PlatGroupActivity::getCreatorUserId)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = creatorIds.isEmpty()
                ? Map.of()
                : userRepository.findAllById(creatorIds).stream()
                .collect(Collectors.toMap(User::getUserid, u -> u, (a, b) -> a));

        List<Map<String, Object>> items = new ArrayList<>();
        for (PlatGroupActivity activity : pending) {
            Map<String, Object> row = buildProposalItem(activity);
            User creator = userMap.get(activity.getCreatorUserId());
            row.put("creatorUserId", activity.getCreatorUserId());
            row.put("creatorName", creator != null && creator.getUsername() != null
                    ? creator.getUsername() : "成员" + activity.getCreatorUserId());
            items.add(row);
        }

        return Map.of(
                "items", items,
                "total", items.size(),
                "pendingCount", items.size()
        );
    }

    @Transactional
    public Map<String, Object> reviewProposal(
            Long groupId,
            Long activityId,
            User reviewer,
            Map<String, Object> payload
    ) {
        requireManagerOrSiteAdmin(groupId, reviewer);
        PlatGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "团体不存在"));

        PlatGroupActivity activity = activityRepository.findByIdAndGroupId(activityId, groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "活动投稿不存在"));

        if (!"pending".equals(activity.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "该投稿已处理，无法重复审核");
        }

        boolean approved = Boolean.TRUE.equals(payload.get("approved"))
                || "true".equalsIgnoreCase(String.valueOf(payload.getOrDefault("approved", "")));
        String comment = String.valueOf(payload.getOrDefault("comment", "")).trim();
        LocalDateTime now = LocalDateTime.now();

        activity.setReviewedBy(reviewer.getUserid());
        activity.setReviewedAt(now);
        activity.setReviewComment(comment.isBlank()
                ? (approved ? "审核通过" : "未通过审核")
                : comment);
        activity.setUpdatedAt(now);

        if (approved) {
            activity.setStatus("published");
            activityRepository.save(activity);

            notificationService.createNotification(
                    activity.getCreatorUserId(),
                    NotificationCatalog.TYPE_ACTIVITY_PROPOSAL_APPROVED,
                    "你的活动投稿已通过：" + activity.getTitle(),
                    "你在「" + group.getName() + "」投稿的活动已通过审核并发布，成员现在可以报名参与。",
                    "/platform/groups/" + groupId + "/activities",
                    "platform_group",
                    String.valueOf(groupId));

            broadcastActivityPublished(groupId, group, activity, reviewer.getUserid());
            return Map.of(
                    "id", activity.getId(),
                    "status", activity.getStatus(),
                    "message", "审核通过，活动已发布"
            );
        }

        activity.setStatus("rejected");
        activityRepository.save(activity);

        notificationService.createNotification(
                activity.getCreatorUserId(),
                NotificationCatalog.TYPE_ACTIVITY_PROPOSAL_REJECTED,
                "你的活动投稿未通过：" + activity.getTitle(),
                "审核意见：" + activity.getReviewComment(),
                "/platform/groups/" + groupId + "/my-activity-proposals",
                "platform_group",
                String.valueOf(groupId));

        return Map.of(
                "id", activity.getId(),
                "status", activity.getStatus(),
                "message", "已驳回活动投稿"
        );
    }

    private void broadcastActivityPublished(Long groupId, PlatGroup group, PlatGroupActivity activity, Long reviewerId) {
        final String activityTitle = activity.getTitle();
        final String groupName = group.getName() != null ? group.getName() : "团体";
        memberRepository.findByGroupIdAndStatusOrderByJoinedAtAsc(groupId, "approved").stream()
                .filter(m -> !m.getUserId().equals(reviewerId))
                .forEach(m -> notificationService.send(
                        m.getUserId(),
                        "GROUP_ACTIVITY_PUBLISHED",
                        "团体发布了新活动：" + activityTitle,
                        "团体「" + groupName + "」发布了新活动，快去报名吧！",
                        "platform_group",
                        String.valueOf(groupId)));
    }

    private void notifyManagersProposalSubmitted(Long groupId, PlatGroup group, User submitter, String title) {
        String groupName = group.getName() != null ? group.getName() : "团体";
        String submitterName = submitter.getUsername() != null ? submitter.getUsername() : "成员";
        String manageLink = "/platform/spaces/" + groupId + "/manage?tab=activity-review";

        memberRepository.findByGroupIdAndStatusOrderByJoinedAtAsc(groupId, "approved").stream()
                .filter(m -> {
                    String role = m.getRole() == null ? "" : m.getRole().toLowerCase(Locale.ROOT);
                    return "owner".equals(role) || "admin".equals(role);
                })
                .filter(m -> !m.getUserId().equals(submitter.getUserid()))
                .forEach(m -> {
                    try {
                        notificationService.createNotification(
                                m.getUserId(),
                                NotificationCatalog.TYPE_ACTIVITY_PROPOSAL_SUBMITTED,
                                submitterName + " 投稿了新活动「" + title + "」",
                                submitterName + " 在「" + groupName + "」提交了活动投稿，请及时审核。",
                                manageLink,
                                "platform_group",
                                String.valueOf(groupId));
                    } catch (Exception ignored) {
                    }
                });
    }

    private void rejectManagerDirectProposal(Long groupId, User user) {
        if (adminAuthService.isAdmin(user)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "你是运营者，可前往运营台直接发布活动");
        }
        memberRepository.findByGroupIdAndUserId(groupId, user.getUserid())
                .filter(m -> "approved".equals(m.getStatus()))
                .ifPresent(m -> {
                    String role = m.getRole() == null ? "" : m.getRole().toLowerCase(Locale.ROOT);
                    if ("owner".equals(role) || "admin".equals(role)) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "你是运营者，可前往运营台直接发布活动");
                    }
                });
    }

    private void requireApprovedMember(Long groupId, User user) {
        memberRepository.findByGroupIdAndUserId(groupId, user.getUserid())
                .filter(m -> "approved".equals(m.getStatus()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "仅已通过成员可投稿"));
    }

    private void requireManagerOrSiteAdmin(Long groupId, User user) {
        if (adminAuthService.isAdmin(user)) {
            return;
        }
        memberRepository.findByGroupIdAndUserId(groupId, user.getUserid())
                .filter(m -> "approved".equals(m.getStatus())
                        && ("owner".equals(m.getRole()) || "admin".equals(m.getRole())))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "需要团体管理权限"));
    }

    private boolean isMemberProposal(PlatGroupActivity activity) {
        if ("pending".equals(activity.getStatus()) || "rejected".equals(activity.getStatus())) {
            return true;
        }
        return "published".equals(activity.getStatus()) && activity.getReviewedBy() != null;
    }

    private Map<String, Object> buildProposalItem(PlatGroupActivity activity) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", activity.getId());
        item.put("title", activity.getTitle());
        item.put("description", activity.getDescription() != null ? activity.getDescription() : "");
        item.put("startTime", activity.getStartTime());
        item.put("endTime", activity.getEndTime());
        item.put("location", activity.getLocation() != null ? activity.getLocation() : "");
        item.put("status", activity.getStatus());
        item.put("statusLabel", resolveStatusLabel(activity));
        item.put("reviewComment", activity.getReviewComment());
        item.put("reviewedAt", activity.getReviewedAt());
        item.put("reviewedBy", activity.getReviewedBy());
        item.put("createdAt", activity.getCreatedAt());
        return item;
    }

    private String resolveStatusLabel(PlatGroupActivity activity) {
        return switch (activity.getStatus()) {
            case "pending" -> "待审核";
            case "rejected" -> "已拒绝";
            case "published" -> activity.getReviewedBy() != null ? "已发布" : "已发布";
            default -> activity.getStatus();
        };
    }

    private LocalDateTime parseRequiredTime(Object raw, String label) {
        String value = String.valueOf(raw != null ? raw : "").trim();
        if (value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, label + "不能为空");
        }
        try {
            return LocalDateTime.parse(value);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, label + "格式不正确，请使用 ISO 8601 格式");
        }
    }
}
