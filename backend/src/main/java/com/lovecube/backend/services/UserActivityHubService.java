package com.lovecube.backend.services;

import com.lovecube.backend.entity.PlatGroup;
import com.lovecube.backend.entity.PlatGroupActivity;
import com.lovecube.backend.entity.PlatGroupActivitySignup;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.PlatGroupActivityRepository;
import com.lovecube.backend.repository.PlatGroupActivitySignupRepository;
import com.lovecube.backend.repository.PlatGroupRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserActivityHubService {

    private final PlatGroupActivitySignupRepository signupRepository;
    private final PlatGroupActivityRepository activityRepository;
    private final PlatGroupRepository groupRepository;
    private final GroupActivityEngagementService engagementService;

    public UserActivityHubService(
            PlatGroupActivitySignupRepository signupRepository,
            PlatGroupActivityRepository activityRepository,
            PlatGroupRepository groupRepository,
            GroupActivityEngagementService engagementService
    ) {
        this.signupRepository = signupRepository;
        this.activityRepository = activityRepository;
        this.groupRepository = groupRepository;
        this.engagementService = engagementService;
    }

    public List<Map<String, Object>> listMyGroupSignups(User user) {
        List<PlatGroupActivitySignup> signups = signupRepository
                .findByUserIdAndStatusOrderByCreatedAtDesc(user.getUserid(), "signed_up");
        if (signups.isEmpty()) {
            return List.of();
        }

        Set<Long> activityIds = signups.stream().map(PlatGroupActivitySignup::getActivityId).collect(Collectors.toSet());
        Map<Long, PlatGroupActivity> activityMap = activityRepository.findAllById(activityIds).stream()
                .collect(Collectors.toMap(PlatGroupActivity::getId, a -> a, (a, b) -> a));

        Set<Long> groupIds = activityMap.values().stream().map(PlatGroupActivity::getGroupId).collect(Collectors.toSet());
        Map<Long, PlatGroup> groupMap = groupRepository.findAllById(groupIds).stream()
                .collect(Collectors.toMap(PlatGroup::getId, g -> g, (a, b) -> a));

        LocalDateTime now = LocalDateTime.now();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (PlatGroupActivitySignup signup : signups) {
            PlatGroupActivity activity = activityMap.get(signup.getActivityId());
            if (activity == null || "cancelled".equals(activity.getStatus())) {
                continue;
            }
            PlatGroup group = groupMap.get(activity.getGroupId());
            rows.add(buildGroupSignupRow(signup, activity, group, user.getUserid(), now));
        }
        return rows;
    }

    public List<Map<String, Object>> listMyProposals(User user) {
        List<PlatGroupActivity> proposals = activityRepository.findMemberProposalsByCreatorUserId(user.getUserid());
        if (proposals.isEmpty()) {
            return List.of();
        }
        Set<Long> groupIds = proposals.stream().map(PlatGroupActivity::getGroupId).collect(Collectors.toSet());
        Map<Long, PlatGroup> groupMap = groupRepository.findAllById(groupIds).stream()
                .collect(Collectors.toMap(PlatGroup::getId, g -> g, (a, b) -> a));

        List<Map<String, Object>> items = new ArrayList<>();
        for (PlatGroupActivity activity : proposals) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", activity.getId());
            row.put("groupId", activity.getGroupId());
            PlatGroup group = groupMap.get(activity.getGroupId());
            row.put("groupName", group != null && group.getName() != null ? group.getName() : "团体");
            row.put("title", activity.getTitle());
            row.put("description", activity.getDescription() != null ? activity.getDescription() : "");
            row.put("startTime", activity.getStartTime());
            row.put("endTime", activity.getEndTime());
            row.put("location", activity.getLocation() != null ? activity.getLocation() : "");
            row.put("status", activity.getStatus());
            row.put("statusLabel", resolveProposalStatusLabel(activity));
            row.put("reviewComment", activity.getReviewComment());
            row.put("createdAt", activity.getCreatedAt());
            items.add(row);
        }
        return items;
    }

    public int countHostedActivities(User user) {
        return (int) activityRepository.findByCreatorUserIdOrderByCreatedAtDesc(user.getUserid()).stream()
                .filter(a -> "published".equals(a.getStatus()))
                .count();
    }

    private Map<String, Object> buildGroupSignupRow(
            PlatGroupActivitySignup signup,
            PlatGroupActivity activity,
            PlatGroup group,
            Long userId,
            LocalDateTime now
    ) {
        boolean checkedIn = Boolean.TRUE.equals(signup.getCheckedIn());
        boolean eventEnded = activity.getEndTime() != null && !activity.getEndTime().isAfter(now);
        Map<String, Object> reviewSummary = engagementService.buildReviewSummary(
                userId, activity.getId(), checkedIn, eventEnded);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("source", "GROUP_ACTIVITY");
        row.put("activityId", activity.getId());
        row.put("groupId", activity.getGroupId());
        row.put("groupName", group != null && group.getName() != null ? group.getName() : "团体");
        row.put("title", activity.getTitle());
        row.put("location", activity.getLocation() != null ? activity.getLocation() : "");
        row.put("startTime", activity.getStartTime());
        row.put("endTime", activity.getEndTime());
        row.put("signupAt", signup.getCreatedAt());
        row.put("checkedIn", checkedIn);
        row.put("checkedInAt", signup.getCheckedInAt());
        row.put("checkinEnabled", activity.getCheckinCode() != null && !activity.getCheckinCode().isBlank());
        row.put("canReview", Boolean.TRUE.equals(reviewSummary.get("canReview")));
        row.put("pendingReviewCount", reviewSummary.get("pendingReviewCount"));
        row.put("reviewCompleted", reviewSummary.get("reviewCompleted"));
        row.put("eventEnded", eventEnded);
        row.put("activityStatus", activity.getStatus());
        row.put("status", resolveSignupStatus(checkedIn, eventEnded, reviewSummary));
        row.put("detailPath", "/platform/groups/" + activity.getGroupId() + "/activities");
        if (activity.getPlatformEventId() != null && !activity.getPlatformEventId().isBlank()) {
            row.put("platformEventId", activity.getPlatformEventId());
            row.put("platformEventPath", "/events/" + activity.getPlatformEventId());
        }
        return row;
    }

    private String resolveSignupStatus(boolean checkedIn, boolean eventEnded, Map<String, Object> reviewSummary) {
        if (!eventEnded) {
            return checkedIn ? "checked_in" : "upcoming";
        }
        if (!checkedIn) {
            return "missed";
        }
        if (Boolean.TRUE.equals(reviewSummary.get("reviewCompleted"))) {
            return "completed";
        }
        return "review_pending";
    }

    private String resolveProposalStatusLabel(PlatGroupActivity activity) {
        return switch (activity.getStatus()) {
            case "pending" -> "待审核";
            case "rejected" -> "已拒绝";
            case "published" -> "已发布";
            default -> activity.getStatus();
        };
    }
}
