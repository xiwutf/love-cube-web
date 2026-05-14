package com.lovecube.backend.services;

import com.lovecube.backend.entity.GroupEngagementActivity;
import com.lovecube.backend.entity.GroupEngagementActivitySignup;
import com.lovecube.backend.entity.GroupEngagementCheckin;
import com.lovecube.backend.entity.GroupEngagementPoll;
import com.lovecube.backend.entity.GroupEngagementPollOption;
import com.lovecube.backend.entity.GroupEngagementPollVote;
import com.lovecube.backend.entity.GroupEngagementTaskProgress;
import com.lovecube.backend.entity.GroupMember;
import com.lovecube.backend.entity.PlatformGroup;
import com.lovecube.backend.entity.UserGrowth;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.GroupEngagementActivityRepository;
import com.lovecube.backend.repository.GroupEngagementActivitySignupRepository;
import com.lovecube.backend.repository.GroupEngagementCheckinRepository;
import com.lovecube.backend.repository.GroupEngagementPollOptionRepository;
import com.lovecube.backend.repository.GroupEngagementPollRepository;
import com.lovecube.backend.repository.GroupEngagementPollVoteRepository;
import com.lovecube.backend.repository.GroupEngagementTaskProgressRepository;
import com.lovecube.backend.repository.GroupMemberRepository;
import com.lovecube.backend.repository.PlatformGroupRepository;
import com.lovecube.backend.repository.PlatCheckinCommentRepository;
import com.lovecube.backend.repository.PlatCheckinLikeRepository;
import com.lovecube.backend.repository.UserGrowthRepository;
import com.lovecube.backend.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * platform_groups（字符串 ID）的打卡、团体每日任务与活动；
 * 与 plat 团体（{@link com.lovecube.backend.entity.PlatGroup}，Long 数字 ID）表互不混用。
 */
@Service
public class GroupExternalEngagementService {

    private static final Map<String, String> TASK_NAMES = Map.of(
            "CHECKIN", "今日打卡",
            "POST", "发布一条团体动态",
            "COMMENT", "评论一次团体动态",
            "LIKE", "点赞一次团体动态"
    );
    private static final Map<String, Integer> TASK_REWARDS = Map.of(
            "CHECKIN", 2, "POST", 5, "COMMENT", 3, "LIKE", 1
    );
    private static final Map<String, String> TASK_ACTION_TYPES = Map.of(
            "CHECKIN", "GROUP_CHECKIN",
            "POST", "GROUP_POST_TASK",
            "COMMENT", "GROUP_COMMENT_TASK",
            "LIKE", "GROUP_LIKE_TASK"
    );
    private static final List<String> TASK_ORDER = List.of("CHECKIN", "POST", "COMMENT", "LIKE");

    private final PlatformGroupRepository platformGroupRepository;
    private final GroupMemberRepository memberRepository;
    private final AdminAuthService adminAuthService;
    private final NotificationService notificationService;
    private final GroupEngagementCheckinRepository checkinRepository;
    private final GroupEngagementTaskProgressRepository taskProgressRepository;
    private final GroupEngagementActivityRepository activityRepository;
    private final GroupEngagementActivitySignupRepository activitySignupRepository;
    private final GroupEngagementPollRepository pollRepository;
    private final GroupEngagementPollOptionRepository pollOptionRepository;
    private final GroupEngagementPollVoteRepository pollVoteRepository;
    private final GrowthService growthService;
    private final PlatCheckinLikeRepository checkinLikeRepository;
    private final PlatCheckinCommentRepository checkinCommentRepository;
    private final UserGrowthRepository userGrowthRepository;
    private final UserRepository userRepository;

    public GroupExternalEngagementService(
            PlatformGroupRepository platformGroupRepository,
            GroupMemberRepository memberRepository,
            AdminAuthService adminAuthService,
            NotificationService notificationService,
            GroupEngagementCheckinRepository checkinRepository,
            GroupEngagementTaskProgressRepository taskProgressRepository,
            GroupEngagementActivityRepository activityRepository,
            GroupEngagementActivitySignupRepository activitySignupRepository,
            GroupEngagementPollRepository pollRepository,
            GroupEngagementPollOptionRepository pollOptionRepository,
            GroupEngagementPollVoteRepository pollVoteRepository,
            GrowthService growthService,
            PlatCheckinLikeRepository checkinLikeRepository,
            PlatCheckinCommentRepository checkinCommentRepository,
            UserGrowthRepository userGrowthRepository,
            UserRepository userRepository) {
        this.platformGroupRepository = platformGroupRepository;
        this.memberRepository = memberRepository;
        this.adminAuthService = adminAuthService;
        this.notificationService = notificationService;
        this.checkinRepository = checkinRepository;
        this.taskProgressRepository = taskProgressRepository;
        this.activityRepository = activityRepository;
        this.activitySignupRepository = activitySignupRepository;
        this.pollRepository = pollRepository;
        this.pollOptionRepository = pollOptionRepository;
        this.pollVoteRepository = pollVoteRepository;
        this.growthService = growthService;
        this.checkinLikeRepository = checkinLikeRepository;
        this.checkinCommentRepository = checkinCommentRepository;
        this.userGrowthRepository = userGrowthRepository;
        this.userRepository = userRepository;
    }

    /** 校验 engagement 打卡的点赞、评论权限（先于 plat 打卡解析）。 */
    public void assertCheckinAccessForUser(Long checkinId, User user) {
        GroupEngagementCheckin c = checkinRepository.findById(checkinId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "打卡不存在"));
        if (!memberRepository.existsByGroupIdAndUserId(c.getGroupExternalId(), user.getUserid())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅团体成员可操作");
        }
    }

    @Transactional
    public void completeDailyTask(String groupExternalId, Long userId, String taskCode) {
        if (!TASK_NAMES.containsKey(taskCode)) {
            return;
        }
        if (platformGroupRepository.findById(groupExternalId).isEmpty()) {
            return;
        }
        upsertCompletedTask(groupExternalId, userId, taskCode);
    }

    public Map<String, Object> checkinSummary(String groupId, String authHeader) {
        requireActiveGroup(groupId);
        LocalDate today = LocalDate.now();
        int todayCount = checkinRepository.countByGroupExternalIdAndCheckinDate(groupId, today);

        boolean checkedInToday = false;
        int myStreakDays = 0;
        Long userId = resolveOptionalUserId(authHeader);
        if (userId != null) {
            checkedInToday = checkinRepository
                    .findByGroupExternalIdAndUserIdAndCheckinDate(groupId, userId, today).isPresent();
            myStreakDays = checkinRepository
                    .findTopByGroupExternalIdAndUserIdOrderByCheckinDateDesc(groupId, userId)
                    .map(c -> c.getStreakDays() != null ? c.getStreakDays() : 1)
                    .orElse(0);
        }

        List<GroupEngagementCheckin> recent = checkinRepository
                .findByGroupExternalIdOrderByCreatedAtDesc(groupId, PageRequest.of(0, 20));
        Set<Long> userIds = recent.stream().map(GroupEngagementCheckin::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userIds.isEmpty() ? Map.of()
                : userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserid, u -> u));

        List<Map<String, Object>> recentList = recent.stream().map(c -> {
            User u = userMap.get(c.getUserId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", c.getId());
            item.put("userId", c.getUserId());
            item.put("username", u != null ? u.getUsername() : "");
            item.put("avatarUrl", u != null ? u.getProfilePhoto() : "");
            item.put("checkinType", c.getCheckinType());
            item.put("content", c.getContent() != null ? c.getContent() : "");
            item.put("streakDays", c.getStreakDays());
            item.put("createdAt", c.getCreatedAt());
            return item;
        }).collect(Collectors.toList());

        if (!recentList.isEmpty()) {
            Set<Long> uids = recent.stream().map(GroupEngagementCheckin::getUserId).collect(Collectors.toSet());
            Map<Long, Integer> lvl = userGrowthRepository.findByUserIdIn(uids).stream()
                    .collect(Collectors.toMap(
                            UserGrowth::getUserId,
                            g -> g.getLevel() != null ? g.getLevel() : 1,
                            (a, b) -> a));
            for (Map<String, Object> item : recentList) {
                long uid = ((Number) item.get("userId")).longValue();
                item.put("title", GrowthService.getUserTitle(lvl.getOrDefault(uid, 1)));
            }
        }
        enrichCheckinFeedItems(recentList, userId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("checkedInToday", checkedInToday);
        result.put("todayCount", todayCount);
        result.put("myStreakDays", myStreakDays);
        result.put("recentCheckins", recentList);
        return result;
    }

    public Map<String, Object> checkinRankings(String groupId, String type, String authHeader) {
        User user = adminAuthService.requireUser(authHeader);
        requireActiveGroup(groupId);
        if (!memberRepository.existsByGroupIdAndUserId(groupId, user.getUserid())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅团体成员可查看排行榜");
        }

        String t = type != null ? type.trim().toLowerCase() : "daily";
        if (!"daily".equals(t) && !"streak".equals(t)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "type 仅支持 daily 或 streak");
        }

        List<GroupMember> members = memberRepository.findByGroupIdOrderByJoinedAtAsc(groupId);
        if (members.isEmpty()) {
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("type", t);
            empty.put("items", List.of());
            empty.put("currentUser", null);
            return empty;
        }

        LocalDate today = LocalDate.now();
        Map<Long, GroupEngagementCheckin> todayByUser = checkinRepository
                .findByGroupExternalIdAndCheckinDateOrderByCreatedAtAsc(groupId, today)
                .stream()
                .collect(Collectors.toMap(GroupEngagementCheckin::getUserId, c -> c, (a, b) -> a));

        Map<Long, Integer> streakByUser = new HashMap<>();
        for (Object[] row : checkinRepository.findLatestStreakByUserForGroup(groupId)) {
            if (row == null || row.length < 2 || row[0] == null || row[1] == null) {
                continue;
            }
            streakByUser.put(((Number) row[0]).longValue(), ((Number) row[1]).intValue());
        }

        List<RankScratch> scratches = new ArrayList<>();
        for (GroupMember m : members) {
            Long uid = m.getUserId();
            GroupEngagementCheckin td = todayByUser.get(uid);
            boolean checked = td != null;
            scratches.add(new RankScratch(uid, checked, checked ? td.getCreatedAt() : null,
                    checked ? 1 : 0, streakByUser.getOrDefault(uid, 0)));
        }

        Comparator<RankScratch> dailyCmp = Comparator
                .comparing((RankScratch r) -> r.checkedToday).reversed()
                .thenComparing(RankScratch::getTodayCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing((RankScratch r) -> r.streakDays).reversed()
                .thenComparing((RankScratch r) -> r.userId);
        Comparator<RankScratch> streakCmp = Comparator
                .comparing((RankScratch r) -> r.streakDays).reversed()
                .thenComparing((RankScratch r) -> r.checkedToday).reversed()
                .thenComparing(RankScratch::getTodayCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing((RankScratch r) -> r.userId);

        scratches.sort("streak".equals(t) ? streakCmp : dailyCmp);
        for (int i = 0; i < scratches.size(); i++) {
            scratches.get(i).rank = i + 1;
        }

        Set<Long> allUserIds = scratches.stream().map(s -> s.userId).collect(Collectors.toSet());
        Map<Long, User> users = userRepository.findAllById(allUserIds).stream()
                .collect(Collectors.toMap(User::getUserid, u -> u));
        Map<Long, Integer> levelByUser = userGrowthRepository.findByUserIdIn(allUserIds).stream()
                .collect(Collectors.toMap(UserGrowth::getUserId, g -> g.getLevel() != null ? g.getLevel() : 1));

        List<Map<String, Object>> fullRows = scratches.stream()
                .map(s -> toRankingRow(s, users.get(s.userId), levelByUser.getOrDefault(s.userId, 1), user.getUserid()))
                .collect(Collectors.toList());

        List<Map<String, Object>> top20 = fullRows.stream().limit(20).collect(Collectors.toList());
        Map<String, Object> current = fullRows.stream()
                .filter(r -> Boolean.TRUE.equals(r.get("isCurrentUser")))
                .findFirst()
                .orElse(Map.of());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", t);
        body.put("items", top20);
        body.put("currentUser", current);
        return body;
    }

    @Transactional
    public Map<String, Object> createCheckin(String groupId, String authHeader, Map<String, Object> payload) {
        User user = adminAuthService.requireUser(authHeader);
        requireActiveGroup(groupId);

        if (!memberRepository.existsByGroupIdAndUserId(groupId, user.getUserid())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅团体成员可以打卡");
        }

        LocalDate today = LocalDate.now();
        if (checkinRepository.findByGroupExternalIdAndUserIdAndCheckinDate(groupId, user.getUserid(), today)
                .isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "今天已经打卡过了");
        }

        String checkinType = String.valueOf(payload.getOrDefault("checkinType", "other")).trim();
        List<String> validTypes = List.of("thanks", "prayer", "study", "exercise", "share", "other");
        if (!validTypes.contains(checkinType)) {
            checkinType = "other";
        }

        String content = String.valueOf(payload.getOrDefault("content", "")).trim();
        if (content.length() > 500) {
            content = content.substring(0, 500);
        }

        int streakDays = 1;
        Optional<GroupEngagementCheckin> previous =
                checkinRepository.findTopByGroupExternalIdAndUserIdOrderByCheckinDateDesc(groupId, user.getUserid());
        if (previous.isPresent() && today.minusDays(1).equals(previous.get().getCheckinDate())) {
            streakDays = (previous.get().getStreakDays() != null ? previous.get().getStreakDays() : 1) + 1;
        }

        GroupEngagementCheckin checkin = new GroupEngagementCheckin();
        checkin.setGroupExternalId(groupId);
        checkin.setUserId(user.getUserid());
        checkin.setCheckinDate(today);
        checkin.setCheckinType(checkinType);
        checkin.setContent(content.isBlank() ? null : content);
        checkin.setStreakDays(streakDays);
        checkin.setCreatedAt(LocalDateTime.now());
        checkinRepository.save(checkin);

        upsertCompletedTask(groupId, user.getUserid(), "CHECKIN");

        notificationService.send(user.getUserid(), "GROUP_CHECKIN_CREATED",
                "打卡成功",
                "你已在团体完成今日打卡，连续打卡 " + streakDays + " 天",
                "platform_group", groupId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", checkin.getId());
        result.put("streakDays", streakDays);
        result.put("message", "打卡成功");
        return result;
    }

    public List<Map<String, Object>> todayTasks(String groupId, String authHeader) {
        User user = adminAuthService.requireUser(authHeader);
        requireActiveGroup(groupId);
        if (!memberRepository.existsByGroupIdAndUserId(groupId, user.getUserid())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅团体成员可查看任务");
        }

        LocalDate today = LocalDate.now();
        Map<String, GroupEngagementTaskProgress> progressMap = taskProgressRepository
                .findByGroupExternalIdAndUserIdAndTaskDate(groupId, user.getUserid(), today)
                .stream()
                .collect(Collectors.toMap(GroupEngagementTaskProgress::getTaskCode, p -> p));

        return TASK_ORDER.stream().map(code -> {
            GroupEngagementTaskProgress p = progressMap.get(code);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("taskCode", code);
            item.put("name", TASK_NAMES.getOrDefault(code, code));
            item.put("rewardExp", TASK_REWARDS.getOrDefault(code, 0));
            item.put("completed", p != null && p.getCompleted() != null && p.getCompleted() == 1);
            item.put("claimed", p != null && p.getClaimed() != null && p.getClaimed() == 1);
            item.put("completedAt", p != null ? p.getCompletedAt() : null);
            item.put("claimedAt", p != null ? p.getClaimedAt() : null);
            return item;
        }).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> claimTask(String groupId, String taskCode, String authHeader) {
        User user = adminAuthService.requireUser(authHeader);
        requireActiveGroup(groupId);
        if (!memberRepository.existsByGroupIdAndUserId(groupId, user.getUserid())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅团体成员可领取奖励");
        }
        if (!TASK_NAMES.containsKey(taskCode)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "无效的任务代码");
        }

        LocalDate today = LocalDate.now();
        GroupEngagementTaskProgress progress = taskProgressRepository
                .findByGroupExternalIdAndUserIdAndTaskCodeAndTaskDate(groupId, user.getUserid(), taskCode, today)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "任务尚未完成"));

        if (progress.getCompleted() == null || progress.getCompleted() != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "任务尚未完成");
        }
        if (progress.getClaimed() != null && progress.getClaimed() == 1) {
            return Map.of("claimed", false, "message", "奖励已领取");
        }

        progress.setClaimed(1);
        progress.setClaimedAt(LocalDateTime.now());
        taskProgressRepository.save(progress);

        String actionType = TASK_ACTION_TYPES.getOrDefault(taskCode, "GROUP_CHECKIN");
        String bizId = "GROUP_TASK_" + groupId + "_" + taskCode + "_" + today;
        growthService.recordAction(user.getUserid(), actionType, bizId);

        int rewardExp = TASK_REWARDS.getOrDefault(taskCode, 0);
        notificationService.send(user.getUserid(), "GROUP_TASK_REWARD_CLAIMED",
                "你领取了团体任务奖励：+" + rewardExp + " EXP",
                "任务「" + TASK_NAMES.getOrDefault(taskCode, taskCode) + "」奖励已到账",
                "platform_group", groupId);

        return Map.of("claimed", true, "taskCode", taskCode, "rewardExp", rewardExp, "message", "奖励已领取");
    }

    public Map<String, Object> listActivities(String groupId, String filter, int page, int size,
                                              String authHeader) {
        requireActiveGroup(groupId);
        Long userId = resolveOptionalUserId(authHeader);

        List<GroupEngagementActivity> all =
                activityRepository.findByGroupExternalIdOrderByStartTimeDesc(groupId);

        if ("upcoming".equals(filter)) {
            LocalDateTime now = LocalDateTime.now();
            all = all.stream()
                    .filter(a -> "published".equals(a.getStatus()) && a.getEndTime().isAfter(now))
                    .collect(Collectors.toList());
        } else if ("ended".equals(filter)) {
            LocalDateTime now = LocalDateTime.now();
            all = all.stream()
                    .filter(a -> "ended".equals(a.getStatus()) ||
                            ("published".equals(a.getStatus()) && !a.getEndTime().isAfter(now)))
                    .collect(Collectors.toList());
        }

        int total = all.size();
        int safePage = Math.max(1, page);
        int safeSize = Math.min(50, Math.max(1, size));
        int from = Math.min((safePage - 1) * safeSize, total);
        int to = Math.min(from + safeSize, total);
        List<GroupEngagementActivity> pageItems = from < total ? all.subList(from, to) : List.of();

        Set<Long> creatorIds =
                pageItems.stream().map(GroupEngagementActivity::getCreatorUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = creatorIds.isEmpty() ? Map.of()
                : userRepository.findAllById(creatorIds).stream().collect(Collectors.toMap(User::getUserid, u -> u));

        Set<Long> signedUpIds = Set.of();
        if (userId != null && !pageItems.isEmpty()) {
            List<Long> activityIds = pageItems.stream().map(GroupEngagementActivity::getId).collect(Collectors.toList());
            signedUpIds = activitySignupRepository
                    .findByActivityIdInAndUserIdAndStatus(activityIds, userId, "signed_up")
                    .stream()
                    .map(GroupEngagementActivitySignup::getActivityId)
                    .collect(Collectors.toSet());
        }
        Set<Long> signedSet = signedUpIds;

        LocalDateTime now = LocalDateTime.now();
        List<Map<String, Object>> items = pageItems.stream().map(a -> {
            User creator = userMap.get(a.getCreatorUserId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", a.getId());
            item.put("title", a.getTitle());
            item.put("description", a.getDescription() != null ? a.getDescription() : "");
            item.put("startTime", a.getStartTime());
            item.put("endTime", a.getEndTime());
            item.put("location", a.getLocation() != null ? a.getLocation() : "");
            item.put("maxParticipants", a.getMaxParticipants());
            item.put("participantCount", a.getParticipantCount());
            item.put("status", a.getStatus());
            item.put("isEnded", !a.getEndTime().isAfter(now));
            item.put("signedUpByMe", signedSet.contains(a.getId()));
            item.put("creatorName", creator != null ? creator.getUsername() : "");
            item.put("createdAt", a.getCreatedAt());
            return item;
        }).collect(Collectors.toList());

        return Map.of("items", items, "total", total, "page", safePage, "pageSize", safeSize);
    }

    @Transactional
    public Map<String, Object> createActivity(String groupId, String authHeader, Map<String, Object> payload) {
        User user = adminAuthService.requireUser(authHeader);
        requireActiveGroup(groupId);
        requireManagerRole(groupId, user);

        PlatformGroup group = platformGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "团体不存在"));

        String title = String.valueOf(payload.getOrDefault("title", "")).trim();
        if (title.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "活动标题不能为空");
        }
        if (title.length() > 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "标题最多 200 字");
        }

        String startStr = String.valueOf(payload.getOrDefault("startTime", "")).trim();
        String endStr = String.valueOf(payload.getOrDefault("endTime", "")).trim();
        if (startStr.isBlank() || endStr.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "活动时间不能为空");
        }

        LocalDateTime startTime;
        LocalDateTime endTime;
        try {
            startTime = LocalDateTime.parse(startStr);
            endTime = LocalDateTime.parse(endStr);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "时间格式不正确，请使用 ISO 8601 格式");
        }
        if (!endTime.isAfter(startTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "结束时间必须晚于开始时间");
        }

        GroupEngagementActivity activity = new GroupEngagementActivity();
        activity.setGroupExternalId(groupId);
        activity.setCreatorUserId(user.getUserid());
        activity.setTitle(title);
        activity.setDescription(String.valueOf(payload.getOrDefault("description", "")).trim());
        activity.setStartTime(startTime);
        activity.setEndTime(endTime);
        String location = String.valueOf(payload.getOrDefault("location", "")).trim();
        activity.setLocation(location.isBlank() ? null : location);
        Object maxP = payload.get("maxParticipants");
        activity.setMaxParticipants(maxP != null ? Math.max(0, Integer.parseInt(String.valueOf(maxP))) : 0);
        activity.setStatus("published");
        activity.setParticipantCount(0);
        activity.setCreatedAt(LocalDateTime.now());
        activity.setUpdatedAt(LocalDateTime.now());
        activityRepository.save(activity);

        String activityTitle = title;
        memberRepository.findByGroupIdOrderByJoinedAtAsc(groupId).stream()
                .filter(m -> !m.getUserId().equals(user.getUserid()))
                .forEach(m -> notificationService.send(
                        m.getUserId(), "GROUP_ACTIVITY_PUBLISHED",
                        "团体发布了新活动：" + activityTitle,
                        "团体「" + group.getName() + "」发布了新活动，快去报名吧！",
                        "platform_group", groupId));

        return Map.of("id", activity.getId(), "message", "活动已发布");
    }

    public Map<String, Object> getActivity(String groupId, Long activityId, String authHeader) {
        requireActiveGroup(groupId);
        GroupEngagementActivity activity = activityRepository.findByIdAndGroupExternalId(activityId, groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity not found"));

        Long userId = resolveOptionalUserId(authHeader);
        boolean signedUp = false;
        if (userId != null) {
            signedUp = activitySignupRepository.findByActivityIdAndUserId(activityId, userId)
                    .map(s -> "signed_up".equals(s.getStatus()))
                    .orElse(false);
        }

        User creator = userRepository.findById(activity.getCreatorUserId()).orElse(null);
        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", activity.getId());
        result.put("groupId", groupId);
        result.put("title", activity.getTitle());
        result.put("description", activity.getDescription() != null ? activity.getDescription() : "");
        result.put("startTime", activity.getStartTime());
        result.put("endTime", activity.getEndTime());
        result.put("location", activity.getLocation() != null ? activity.getLocation() : "");
        result.put("maxParticipants", activity.getMaxParticipants());
        result.put("participantCount", activity.getParticipantCount());
        result.put("status", activity.getStatus());
        result.put("isEnded", !activity.getEndTime().isAfter(now));
        result.put("signedUpByMe", signedUp);
        result.put("creatorName", creator != null ? creator.getUsername() : "");
        result.put("createdAt", activity.getCreatedAt());
        return result;
    }

    @Transactional
    public Map<String, Object> signUpActivity(String groupId, Long activityId, String authHeader) {
        User user = adminAuthService.requireUser(authHeader);
        requireActiveGroup(groupId);
        if (!memberRepository.existsByGroupIdAndUserId(groupId, user.getUserid())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅团体成员可报名活动");
        }

        GroupEngagementActivity activity = activityRepository.findByIdAndGroupExternalId(activityId, groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity not found"));
        if (!"published".equals(activity.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "活动不可报名");
        }
        if (activity.getEndTime().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "活动已结束");
        }
        if (activity.getMaxParticipants() != null && activity.getMaxParticipants() > 0
                && activity.getParticipantCount() != null
                && activity.getParticipantCount() >= activity.getMaxParticipants()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "报名人数已满");
        }

        Optional<GroupEngagementActivitySignup> existing =
                activitySignupRepository.findByActivityIdAndUserId(activityId, user.getUserid());
        if (existing.isPresent()) {
            GroupEngagementActivitySignup s = existing.get();
            if ("signed_up".equals(s.getStatus())) {
                return Map.of("signedUp", true, "message", "已报名");
            }
            s.setStatus("signed_up");
            s.setUpdatedAt(LocalDateTime.now());
            activitySignupRepository.save(s);
        } else {
            GroupEngagementActivitySignup signup = new GroupEngagementActivitySignup();
            signup.setActivityId(activityId);
            signup.setGroupExternalId(groupId);
            signup.setUserId(user.getUserid());
            signup.setStatus("signed_up");
            signup.setCreatedAt(LocalDateTime.now());
            signup.setUpdatedAt(LocalDateTime.now());
            activitySignupRepository.save(signup);
        }

        activityRepository.incrementParticipantCount(activityId);

        if (!user.getUserid().equals(activity.getCreatorUserId())) {
            String actorName = user.getUsername() != null ? user.getUsername() : "有人";
            notificationService.send(activity.getCreatorUserId(), "GROUP_ACTIVITY_SIGNED_UP",
                    actorName + " 报名了你发布的活动",
                    actorName + " 报名了活动「" + activity.getTitle() + "」",
                    "platform_group", groupId);
        }

        int updatedCount = activityRepository.findById(activityId)
                .map(GroupEngagementActivity::getParticipantCount).orElse(0);
        return Map.of("signedUp", true, "participantCount", updatedCount, "message", "报名成功");
    }

    @Transactional
    public Map<String, Object> cancelActivitySignup(String groupId, Long activityId, String authHeader) {
        User user = adminAuthService.requireUser(authHeader);
        requireActiveGroup(groupId);

        GroupEngagementActivity activity = activityRepository.findByIdAndGroupExternalId(activityId, groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity not found"));
        if (activity.getEndTime().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "活动已结束，无法取消报名");
        }

        GroupEngagementActivitySignup signup = activitySignupRepository
                .findByActivityIdAndUserId(activityId, user.getUserid())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "你未报名此活动"));
        if (!"signed_up".equals(signup.getStatus())) {
            return Map.of("cancelled", true, "message", "已取消报名");
        }

        signup.setStatus("cancelled");
        signup.setUpdatedAt(LocalDateTime.now());
        activitySignupRepository.save(signup);

        activityRepository.decrementParticipantCount(activityId);

        int updatedCount = activityRepository.findById(activityId)
                .map(GroupEngagementActivity::getParticipantCount).orElse(0);
        return Map.of("cancelled", true, "participantCount", updatedCount, "message", "已取消报名");
    }

    @Transactional
    public Map<String, Object> updateActivity(String groupId, Long activityId, String authHeader,
                                              Map<String, Object> payload) {
        User user = adminAuthService.requireUser(authHeader);
        requireActiveGroup(groupId);
        requireManagerRole(groupId, user);

        GroupEngagementActivity activity = activityRepository.findByIdAndGroupExternalId(activityId, groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity not found"));

        if (payload.containsKey("title")) {
            String title = String.valueOf(payload.get("title")).trim();
            if (!title.isBlank()) {
                activity.setTitle(title);
            }
        }
        if (payload.containsKey("description")) {
            activity.setDescription(String.valueOf(payload.get("description")).trim());
        }
        if (payload.containsKey("location")) {
            String loc = String.valueOf(payload.get("location")).trim();
            activity.setLocation(loc.isBlank() ? null : loc);
        }
        if (payload.containsKey("startTime")) {
            try {
                activity.setStartTime(LocalDateTime.parse(String.valueOf(payload.get("startTime"))));
            } catch (Exception ignored) {
                // skip invalid
            }
        }
        if (payload.containsKey("endTime")) {
            try {
                activity.setEndTime(LocalDateTime.parse(String.valueOf(payload.get("endTime"))));
            } catch (Exception ignored) {
                // skip invalid
            }
        }
        if (!activity.getEndTime().isAfter(activity.getStartTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "结束时间必须晚于开始时间");
        }
        if (payload.containsKey("maxParticipants")) {
            activity.setMaxParticipants(Math.max(0, Integer.parseInt(String.valueOf(payload.get("maxParticipants")))));
        }
        if (payload.containsKey("status")) {
            String newStatus = String.valueOf(payload.get("status")).trim();
            if (List.of("published", "cancelled", "ended").contains(newStatus)) {
                activity.setStatus(newStatus);
                if ("cancelled".equals(newStatus)) {
                    List<GroupEngagementActivitySignup> signups =
                            activitySignupRepository.findByActivityId(activityId);
                    String actTitle = activity.getTitle();
                    signups.stream().filter(s -> "signed_up".equals(s.getStatus())).forEach(s ->
                            notificationService.send(s.getUserId(), "GROUP_ACTIVITY_CANCELLED",
                                    "团体活动已取消：" + actTitle,
                                    "你报名的活动「" + actTitle + "」已被取消",
                                    "platform_group", groupId));
                }
            }
        }

        activity.setUpdatedAt(LocalDateTime.now());
        activityRepository.save(activity);
        return Map.of("updated", true, "message", "活动已更新");
    }

    public Map<String, Object> listPolls(String groupId, int page, int size, String authHeader) {
        requireActiveGroup(groupId);
        Long userId = resolveOptionalUserId(authHeader);
        User viewer = userId != null ? userRepository.findById(userId).orElse(null) : null;

        List<GroupEngagementPoll> all = pollRepository.findByGroupExternalIdOrderByCreatedAtDesc(groupId).stream()
                .filter(p -> "published".equals(p.getStatus()))
                .collect(Collectors.toList());

        int total = all.size();
        int safePage = Math.max(1, page);
        int safeSize = Math.min(50, Math.max(1, size));
        int from = Math.min((safePage - 1) * safeSize, total);
        int to = Math.min(from + safeSize, total);
        List<GroupEngagementPoll> pageItems = from < total ? all.subList(from, to) : List.of();

        Set<Long> votedPollIds = Set.of();
        if (userId != null && !pageItems.isEmpty()) {
            List<Long> pids = pageItems.stream().map(GroupEngagementPoll::getId).collect(Collectors.toList());
            votedPollIds = new HashSet<>(pollVoteRepository.findPollIdsVotedByUserIn(userId, pids));
        }

        LocalDateTime now = LocalDateTime.now();
        List<Map<String, Object>> items = new ArrayList<>();
        for (GroupEngagementPoll p : pageItems) {
            boolean countsVisible = shouldExposePollCounts(p, viewer);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", p.getId());
            row.put("title", p.getTitle());
            row.put("selectionMode", p.getSelectionMode());
            row.put("maxChoices", p.getMaxChoices());
            row.put("startTime", p.getStartTime());
            row.put("endTime", p.getEndTime());
            row.put("resultsPublic", Boolean.TRUE.equals(p.getResultsPublic()));
            row.put("createdAt", p.getCreatedAt());
            row.put("isEnded", now.isAfter(p.getEndTime()));
            row.put("isNotYetOpen", now.isBefore(p.getStartTime()));
            row.put("hasVoted", votedPollIds.contains(p.getId()));
            row.put("countsVisible", countsVisible);
            items.add(row);
        }
        return Map.of("items", items, "total", total, "page", safePage, "pageSize", safeSize);
    }

    public Map<String, Object> getPoll(String groupId, Long pollId, String authHeader) {
        requireActiveGroup(groupId);
        GroupEngagementPoll poll = pollRepository.findByIdAndGroupExternalId(pollId, groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "投票不存在"));
        Long userId = resolveOptionalUserId(authHeader);
        User viewer = userId != null ? userRepository.findById(userId).orElse(null) : null;
        boolean countsVisible = shouldExposePollCounts(poll, viewer);

        List<GroupEngagementPollOption> optionRows =
                pollOptionRepository.findByPollIdOrderBySortOrderAscIdAsc(pollId);
        Map<Long, Long> countByOption = Map.of();
        long voterTotal = 0;
        if (countsVisible) {
            countByOption = toLongCountMap(pollVoteRepository.countVotesByPollGrouped(pollId));
            voterTotal = pollVoteRepository.countDistinctVotersByPollId(pollId);
        }

        List<Long> mySelections = List.of();
        if (userId != null) {
            mySelections = pollVoteRepository.findByPollIdAndUserId(pollId, userId).stream()
                    .map(GroupEngagementPollVote::getOptionId)
                    .collect(Collectors.toList());
        }

        List<Map<String, Object>> options = new ArrayList<>();
        for (GroupEngagementPollOption o : optionRows) {
            Map<String, Object> om = new LinkedHashMap<>();
            om.put("id", o.getId());
            om.put("label", o.getLabel());
            om.put("sortOrder", o.getSortOrder());
            if (countsVisible) {
                om.put("voteCount", countByOption.getOrDefault(o.getId(), 0L).intValue());
            } else {
                om.put("voteCount", null);
            }
            options.add(om);
        }

        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", poll.getId());
        body.put("groupId", groupId);
        body.put("title", poll.getTitle());
        body.put("description", poll.getDescription() != null ? poll.getDescription() : "");
        body.put("selectionMode", poll.getSelectionMode());
        body.put("maxChoices", poll.getMaxChoices());
        body.put("startTime", poll.getStartTime());
        body.put("endTime", poll.getEndTime());
        body.put("resultsPublic", Boolean.TRUE.equals(poll.getResultsPublic()));
        body.put("countsVisible", countsVisible);
        body.put("isEnded", now.isAfter(poll.getEndTime()));
        body.put("isNotYetOpen", now.isBefore(poll.getStartTime()));
        body.put("status", poll.getStatus());
        body.put("createdAt", poll.getCreatedAt());
        body.put("options", options);
        body.put("mySelections", mySelections);
        if (countsVisible) {
            body.put("voterCount", voterTotal);
        } else {
            body.put("voterCount", null);
        }
        return body;
    }

    @Transactional
    public Map<String, Object> createPoll(String groupId, String authHeader, Map<String, Object> payload) {
        User user = adminAuthService.requireUser(authHeader);
        requireActiveGroup(groupId);
        requireManagerRole(groupId, user);

        PlatformGroup group = platformGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "团体不存在"));

        String title = String.valueOf(payload.getOrDefault("title", "")).trim();
        if (title.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "投票标题不能为空");
        }
        if (title.length() > 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "标题最多 200 字");
        }

        String mode = String.valueOf(payload.getOrDefault("selectionMode", "single")).trim().toLowerCase();
        if (!"single".equals(mode) && !"multiple".equals(mode)) {
            mode = "single";
        }

        List<String> optionLabels = readStringList(payload.get("options"));
        if (optionLabels.size() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "至少需要 2 个选项");
        }
        if (optionLabels.size() > 30) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "选项最多 30 个");
        }
        for (int i = 0; i < optionLabels.size(); i++) {
            if (optionLabels.get(i).length() > 500) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "单个选项最多 500 字");
            }
        }

        int maxChoices = 1;
        if ("multiple".equals(mode)) {
            Object mc = payload.get("maxChoices");
            if (mc == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "多选投票需填写每人最多可选几项");
            }
            maxChoices = Integer.parseInt(String.valueOf(mc));
            if (maxChoices < 2 || maxChoices > optionLabels.size()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "每人最多可选项数须在 2 到选项个数之间");
            }
        }

        String startStr = String.valueOf(payload.getOrDefault("startTime", "")).trim();
        String endStr = String.valueOf(payload.getOrDefault("endTime", "")).trim();
        if (startStr.isBlank() || endStr.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "投票时间不能为空");
        }
        LocalDateTime startTime;
        LocalDateTime endTime;
        try {
            startTime = LocalDateTime.parse(startStr);
            endTime = LocalDateTime.parse(endStr);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "时间格式不正确，请使用 ISO 8601 格式");
        }
        if (!endTime.isAfter(startTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "结束时间必须晚于开始时间");
        }

        LocalDateTime now = LocalDateTime.now();
        GroupEngagementPoll poll = new GroupEngagementPoll();
        poll.setGroupExternalId(groupId);
        poll.setCreatorUserId(user.getUserid());
        poll.setTitle(title);
        poll.setDescription(String.valueOf(payload.getOrDefault("description", "")).trim());
        poll.setSelectionMode(mode);
        poll.setMaxChoices(maxChoices);
        poll.setStartTime(startTime);
        poll.setEndTime(endTime);
        poll.setStatus("published");
        poll.setResultsPublic(false);
        poll.setCreatedAt(now);
        poll.setUpdatedAt(now);
        pollRepository.save(poll);

        int order = 0;
        for (String label : optionLabels) {
            GroupEngagementPollOption opt = new GroupEngagementPollOption();
            opt.setPollId(poll.getId());
            opt.setLabel(label);
            opt.setSortOrder(order++);
            opt.setCreatedAt(now);
            pollOptionRepository.save(opt);
        }

        String pollTitle = title;
        memberRepository.findByGroupIdOrderByJoinedAtAsc(groupId).stream()
                .filter(m -> !m.getUserId().equals(user.getUserid()))
                .forEach(m -> notificationService.send(
                        m.getUserId(), "GROUP_POLL_PUBLISHED",
                        "团体发起了新投票：" + pollTitle,
                        "团体「" + group.getName() + "」发起了新投票，欢迎参与。",
                        "platform_group", groupId));

        return Map.of("id", poll.getId(), "message", "投票已发布");
    }

    @Transactional
    public Map<String, Object> submitPollVotes(String groupId, Long pollId, String authHeader,
                                                Map<String, Object> payload) {
        User user = adminAuthService.requireUser(authHeader);
        requireActiveGroup(groupId);
        if (!memberRepository.existsByGroupIdAndUserId(groupId, user.getUserid())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "仅团体成员可参与投票");
        }

        GroupEngagementPoll poll = pollRepository.findByIdAndGroupExternalId(pollId, groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "投票不存在"));
        if (!"published".equals(poll.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "投票不可用");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(poll.getStartTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "投票尚未开始");
        }
        if (now.isAfter(poll.getEndTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "投票已结束");
        }

        List<Long> optionIds = readLongList(payload.get("optionIds"));
        if (optionIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请选择至少一个选项");
        }
        if (optionIds.size() != new HashSet<>(optionIds).size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "选项不能重复");
        }

        List<GroupEngagementPollOption> validOptions =
                pollOptionRepository.findByPollIdOrderBySortOrderAscIdAsc(pollId);
        Set<Long> allowed = validOptions.stream().map(GroupEngagementPollOption::getId).collect(Collectors.toSet());
        for (Long oid : optionIds) {
            if (!allowed.contains(oid)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "存在无效选项");
            }
        }

        if ("single".equalsIgnoreCase(poll.getSelectionMode())) {
            if (optionIds.size() != 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "单选投票只能选择一个选项");
            }
        } else {
            int max = poll.getMaxChoices() != null ? poll.getMaxChoices() : 1;
            if (optionIds.size() > max) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "多选投票最多选择 " + max + " 个选项");
            }
        }

        pollVoteRepository.deleteByPollIdAndUserId(pollId, user.getUserid());
        for (Long oid : optionIds) {
            GroupEngagementPollVote v = new GroupEngagementPollVote();
            v.setPollId(pollId);
            v.setOptionId(oid);
            v.setUserId(user.getUserid());
            v.setCreatedAt(LocalDateTime.now());
            pollVoteRepository.save(v);
        }
        poll.setUpdatedAt(LocalDateTime.now());
        pollRepository.save(poll);

        return Map.of("ok", true, "message", "投票已提交");
    }

    @Transactional
    public Map<String, Object> revealPollResults(String groupId, Long pollId, String authHeader) {
        User user = adminAuthService.requireUser(authHeader);
        requireActiveGroup(groupId);
        requireManagerRole(groupId, user);

        GroupEngagementPoll poll = pollRepository.findByIdAndGroupExternalId(pollId, groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "投票不存在"));
        poll.setResultsPublic(true);
        poll.setUpdatedAt(LocalDateTime.now());
        pollRepository.save(poll);
        return Map.of("resultsPublic", true, "message", "已向成员展示票数");
    }

    private boolean userCanManageGroup(String groupId, User user) {
        if (user == null) {
            return false;
        }
        boolean siteAdmin = adminAuthService.isAdmin(user);
        GroupMember self = memberRepository.findByGroupIdAndUserId(groupId, user.getUserid()).orElse(null);
        boolean ownerLike = siteAdmin || (self != null && "owner".equalsIgnoreCase(self.getRole()));
        boolean grpAdmin = self != null && "admin".equalsIgnoreCase(self.getRole());
        return ownerLike || grpAdmin;
    }

    private boolean shouldExposePollCounts(GroupEngagementPoll poll, User viewer) {
        if (Boolean.TRUE.equals(poll.getResultsPublic())) {
            return true;
        }
        return userCanManageGroup(poll.getGroupExternalId(), viewer);
    }

    private List<String> readStringList(Object raw) {
        if (!(raw instanceof List<?> list)) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (Object o : list) {
            if (o == null) {
                continue;
            }
            String s = String.valueOf(o).trim();
            if (!s.isBlank()) {
                out.add(s);
            }
        }
        return out;
    }

    private List<Long> readLongList(Object raw) {
        if (!(raw instanceof List<?> list)) {
            return List.of();
        }
        List<Long> out = new ArrayList<>();
        for (Object o : list) {
            if (o == null) {
                continue;
            }
            try {
                if (o instanceof Number n) {
                    out.add(n.longValue());
                } else {
                    out.add(Long.parseLong(String.valueOf(o).trim()));
                }
            } catch (NumberFormatException ignored) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "选项 ID 格式不正确");
            }
        }
        return out;
    }

    private void requireActiveGroup(String groupId) {
        PlatformGroup group = platformGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "团体不存在"));
        if (!"active".equals(group.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "该团体当前不可用");
        }
    }

    private void requireManagerRole(String groupId, User user) {
        boolean siteAdmin = adminAuthService.isAdmin(user);
        GroupMember self = memberRepository.findByGroupIdAndUserId(groupId, user.getUserid()).orElse(null);
        boolean ownerLike = siteAdmin || (self != null && "owner".equalsIgnoreCase(self.getRole()));
        boolean grpAdmin = self != null && "admin".equalsIgnoreCase(self.getRole());
        if (!ownerLike && !grpAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权限");
        }
    }

    private Long resolveOptionalUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        try {
            return adminAuthService.requireUser(authHeader).getUserid();
        } catch (Exception e) {
            return null;
        }
    }

    private void upsertCompletedTask(String groupExternalId, Long userId, String taskCode) {
        if (!TASK_NAMES.containsKey(taskCode)) {
            return;
        }
        LocalDate today = LocalDate.now();
        GroupEngagementTaskProgress progress = taskProgressRepository
                .findByGroupExternalIdAndUserIdAndTaskCodeAndTaskDate(groupExternalId, userId, taskCode, today)
                .orElseGet(() -> {
                    GroupEngagementTaskProgress p = new GroupEngagementTaskProgress();
                    p.setGroupExternalId(groupExternalId);
                    p.setUserId(userId);
                    p.setTaskCode(taskCode);
                    p.setTaskDate(today);
                    p.setCompleted(0);
                    p.setClaimed(0);
                    return p;
                });
        if (progress.getCompleted() == null || progress.getCompleted() == 0) {
            progress.setCompleted(1);
            progress.setCompletedAt(LocalDateTime.now());
            taskProgressRepository.save(progress);
        }
    }

    private static final class RankScratch {
        final Long userId;
        final boolean checkedToday;
        final LocalDateTime todayCreatedAt;
        final int checkinCount;
        final int streakDays;
        int rank;

        RankScratch(Long userId, boolean checkedToday, LocalDateTime todayCreatedAt, int checkinCount,
                    int streakDays) {
            this.userId = userId;
            this.checkedToday = checkedToday;
            this.todayCreatedAt = todayCreatedAt;
            this.checkinCount = checkinCount;
            this.streakDays = streakDays;
        }

        LocalDateTime getTodayCreatedAt() {
            return todayCreatedAt;
        }
    }

    private Map<String, Object> toRankingRow(RankScratch s, User u, int level, Long currentUserId) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("userId", s.userId);
        row.put("nickname", u != null ? u.getUsername() : "");
        row.put("avatarUrl", u != null ? u.getProfilePhoto() : "");
        row.put("checkinCount", s.checkinCount);
        row.put("streakDays", s.streakDays);
        row.put("rank", s.rank);
        row.put("isCurrentUser", currentUserId != null && currentUserId.equals(s.userId));
        row.put("title", GrowthService.getUserTitle(level));
        return row;
    }

    private void enrichCheckinFeedItems(List<Map<String, Object>> items, Long currentUserId) {
        if (items == null || items.isEmpty()) {
            return;
        }
        List<Long> ids = items.stream()
                .map(m -> m.get("id"))
                .filter(Objects::nonNull)
                .map(id -> ((Number) id).longValue())
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            return;
        }

        Map<Long, Long> likeCountMap = toLongCountMap(checkinLikeRepository.countByCheckinIdInGrouped(ids));
        Map<Long, Long> commentCountMap = toLongCountMap(checkinCommentRepository.countByCheckinIdInGrouped(ids));
        Set<Long> likedSet = Collections.emptySet();
        if (currentUserId != null) {
            likedSet = new HashSet<>(checkinLikeRepository.findCheckinIdsLikedByUser(currentUserId, ids));
        }
        final Set<Long> likedFinal = likedSet;
        for (Map<String, Object> item : items) {
            long cid = ((Number) item.get("id")).longValue();
            item.put("likeCount", likeCountMap.getOrDefault(cid, 0L).intValue());
            item.put("commentCount", commentCountMap.getOrDefault(cid, 0L).intValue());
            item.put("likedByCurrentUser", likedFinal.contains(cid));
        }
    }

    private static Map<Long, Long> toLongCountMap(List<Object[]> rows) {
        if (rows == null || rows.isEmpty()) {
            return Map.of();
        }
        Map<Long, Long> map = new HashMap<>();
        for (Object[] row : rows) {
            if (row == null || row.length < 2 || row[0] == null || row[1] == null) {
                continue;
            }
            map.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }
        return map;
    }
}
