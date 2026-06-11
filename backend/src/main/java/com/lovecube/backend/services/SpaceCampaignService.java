package com.lovecube.backend.services;

import com.lovecube.backend.entity.PlatGroup;
import com.lovecube.backend.entity.PlatGroupMember;
import com.lovecube.backend.entity.PlatGroupPost;
import com.lovecube.backend.entity.SpaceCampaign;
import com.lovecube.backend.entity.SpaceCampaignDay;
import com.lovecube.backend.entity.SpaceCampaignProgress;
import com.lovecube.backend.models.User;
import com.lovecube.backend.notification.NotificationCatalog;
import com.lovecube.backend.repository.PlatGroupMemberRepository;
import com.lovecube.backend.repository.PlatGroupPostRepository;
import com.lovecube.backend.repository.PlatGroupRepository;
import com.lovecube.backend.repository.SpaceCampaignDayRepository;
import com.lovecube.backend.repository.SpaceCampaignProgressRepository;
import com.lovecube.backend.repository.SpaceCampaignRepository;
import com.lovecube.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SpaceCampaignService {

    private static final Logger log = LoggerFactory.getLogger(SpaceCampaignService.class);

    public static final String RELATED_TYPE = "SPACE_CAMPAIGN";
    private static final int DEFAULT_DURATION = 7;
    private static final int DEFAULT_MAKEUP_LIMIT = 2;

    private final SpaceCampaignRepository campaignRepository;
    private final SpaceCampaignDayRepository dayRepository;
    private final SpaceCampaignProgressRepository progressRepository;
    private final PlatGroupRepository groupRepository;
    private final PlatGroupMemberRepository memberRepository;
    private final PlatGroupPostRepository postRepository;
    private final UserRepository userRepository;
    private final AdminAuthService adminAuthService;
    private final NotificationService notificationService;

    public SpaceCampaignService(
            SpaceCampaignRepository campaignRepository,
            SpaceCampaignDayRepository dayRepository,
            SpaceCampaignProgressRepository progressRepository,
            PlatGroupRepository groupRepository,
            PlatGroupMemberRepository memberRepository,
            PlatGroupPostRepository postRepository,
            UserRepository userRepository,
            AdminAuthService adminAuthService,
            NotificationService notificationService
    ) {
        this.campaignRepository = campaignRepository;
        this.dayRepository = dayRepository;
        this.progressRepository = progressRepository;
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.adminAuthService = adminAuthService;
        this.notificationService = notificationService;
    }

    public List<Map<String, Object>> listForGroup(Long groupId, User viewer) {
        requireGroupExists(groupId);
        requireManagerOrSiteAdmin(groupId, viewer);
        refreshCampaignStatuses(groupId);
        return campaignRepository.findByGroupIdOrderByCreatedAtDesc(groupId).stream()
                .map(c -> summarizeCampaign(c, false, false))
                .collect(Collectors.toList());
    }

    public Map<String, Object> getActive(Long groupId, User viewer) {
        requireGroupExists(groupId);
        requireApprovedMemberOrManager(groupId, viewer);
        refreshCampaignStatuses(groupId);
        SpaceCampaign campaign = resolveViewableCampaign(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "当前没有进行中的打卡营"));
        boolean manager = canManage(groupId, viewer);
        return buildCampaignView(campaign, viewer.getUserid(), manager);
    }

    public Map<String, Object> getDetail(Long groupId, Long campaignId, User viewer) {
        requireGroupExists(groupId);
        SpaceCampaign campaign = requireCampaignInGroup(groupId, campaignId);
        boolean manager = canManage(groupId, viewer);
        if (!manager) {
            requireApprovedMember(groupId, viewer.getUserid());
        }
        refreshSingleCampaignStatus(campaign);
        return buildCampaignView(campaign, viewer.getUserid(), manager);
    }

    @Transactional
    public Map<String, Object> create(Long groupId, User operator, Map<String, Object> payload) {
        requireGroupExists(groupId);
        requireManagerOrSiteAdmin(groupId, operator);

        String title = String.valueOf(payload.getOrDefault("title", "")).trim();
        if (title.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "打卡营标题不能为空");
        }
        String description = String.valueOf(payload.getOrDefault("description", "")).trim();
        LocalDate startDate = parseStartDate(payload.get("startDate"));
        if (startDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "开始日期不能为空");
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> daysPayload = (List<Map<String, Object>>) payload.get("days");
        if (daysPayload == null || daysPayload.size() != DEFAULT_DURATION) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请配置 7 天每日任务");
        }

        endOtherActiveCampaigns(groupId);

        SpaceCampaign campaign = new SpaceCampaign();
        campaign.setGroupId(groupId);
        campaign.setTitle(title);
        campaign.setDescription(description.isBlank() ? null : description);
        campaign.setStartDate(startDate);
        campaign.setDurationDays(DEFAULT_DURATION);
        campaign.setStatus(resolveStatusForCampaign(campaign));
        campaign.setCreatedBy(operator.getUserid());
        campaign.setAllowMakeup(parseAllowMakeup(payload.get("allowMakeup")));
        campaign.setMakeupDaysLimit(parseMakeupDaysLimit(payload.get("makeupDaysLimit")));
        campaign = campaignRepository.save(campaign);

        for (int i = 0; i < DEFAULT_DURATION; i++) {
            Map<String, Object> dayMap = daysPayload.get(i);
            int dayNumber = i + 1;
            String taskTitle = String.valueOf(dayMap.getOrDefault("taskTitle", "")).trim();
            if (taskTitle.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "第 " + dayNumber + " 天任务标题不能为空");
            }
            String taskDescription = String.valueOf(dayMap.getOrDefault("taskDescription", "")).trim();
            SpaceCampaignDay day = new SpaceCampaignDay();
            day.setCampaignId(campaign.getId());
            day.setDayNumber(dayNumber);
            day.setTaskTitle(taskTitle);
            day.setTaskDescription(taskDescription.isBlank() ? null : taskDescription);
            dayRepository.save(day);
        }

        Map<String, Object> out = buildCampaignView(campaign, operator.getUserid(), true);
        out.put("message", "打卡营已创建");
        return out;
    }

    @Transactional
    public Map<String, Object> completeDay(Long groupId, Long campaignId, User user, Integer dayNumber) {
        requireGroupExists(groupId);
        requireApprovedMember(groupId, user.getUserid());
        SpaceCampaign campaign = requireCampaignInGroup(groupId, campaignId);
        refreshSingleCampaignStatus(campaign);
        if (!"active".equals(campaign.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "打卡营未在进行中");
        }

        int currentDay = currentDayNumber(campaign);
        if (currentDay < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "打卡营尚未开始");
        }
        if (currentDay > campaign.getDurationDays()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "打卡营已结束");
        }
        if (dayNumber == null || dayNumber < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "无效的任务天数");
        }
        if (!isDayCompletable(campaign, dayNumber)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "该天任务不可完成（仅可完成今日及最近 " + effectiveMakeupLimit(campaign) + " 天内任务）");
        }

        if (progressRepository.findByCampaignIdAndUserIdAndDayNumber(campaignId, user.getUserid(), dayNumber).isPresent()) {
            return Map.of("completed", true, "message", "该天任务已完成", "alreadyDone", true, "dayNumber", dayNumber);
        }

        SpaceCampaignProgress progress = new SpaceCampaignProgress();
        progress.setCampaignId(campaignId);
        progress.setUserId(user.getUserid());
        progress.setDayNumber(dayNumber);
        progress.setCompletedAt(LocalDateTime.now());
        progressRepository.save(progress);

        SpaceCampaignDay dayDef = dayRepository.findByCampaignIdAndDayNumber(campaignId, dayNumber).orElse(null);
        publishCampaignFeedPost(groupId, user, dayNumber, dayDef != null ? dayDef.getTaskTitle() : "打卡任务");

        boolean makeup = dayNumber < currentDay;
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("completed", true);
        out.put("dayNumber", dayNumber);
        out.put("makeup", makeup);
        out.put("message", makeup ? "补卡成功" : "今日打卡完成");
        out.put("myCompletedDays", progressRepository.findByCampaignIdAndUserIdOrderByDayNumberAsc(campaignId, user.getUserid())
                .size());
        return out;
    }

    @Transactional
    public Map<String, Object> notifyMembers(Long groupId, Long campaignId, User operator) {
        requireGroupExists(groupId);
        requireManagerOrSiteAdmin(groupId, operator);
        SpaceCampaign campaign = requireCampaignInGroup(groupId, campaignId);
        PlatGroup group = groupRepository.findById(groupId).orElseThrow();

        List<PlatGroupMember> members = memberRepository.findByGroupIdAndStatusOrderByJoinedAtAsc(groupId, "approved");
        int sent = 0;
        int skipped = 0;
        String link = "/platform/groups/" + groupId + "?camp=" + campaignId;
        String content = "「" + group.getName() + "」打卡营「" + campaign.getTitle() + "」进行中，快来完成今日任务吧。";

        for (PlatGroupMember m : members) {
            if (m.getUserId().equals(operator.getUserid())) {
                continue;
            }
            var n = notificationService.createNotification(
                    m.getUserId(),
                    NotificationCatalog.TYPE_SPACE_CAMPAIGN_REMINDER,
                    "社区打卡营提醒",
                    content,
                    link,
                    RELATED_TYPE,
                    String.valueOf(campaignId)
            );
            if (n == null) {
                skipped++;
            } else {
                sent++;
            }
        }

        return Map.of(
                "sent", sent,
                "skipped", skipped,
                "targetCount", Math.max(0, members.size() - 1),
                "message", "已通知成员"
        );
    }

    /** 定时任务：刷新全站打卡营状态 */
    @Transactional
    public int refreshAllCampaignStatusesScheduled() {
        List<SpaceCampaign> rows = campaignRepository.findByStatusIn(List.of("scheduled", "active"));
        int updated = 0;
        for (SpaceCampaign c : rows) {
            String before = c.getStatus();
            refreshSingleCampaignStatus(c);
            if (!Objects.equals(before, c.getStatus())) {
                updated++;
            }
        }
        log.info("打卡营状态刷新完成，更新 {} 条", updated);
        return updated;
    }

    /** 定时任务：提醒当日未完成成员（morning=false 早间，true 晚间） */
    @Transactional
    public int sendScheduledReminders(boolean evening) {
        refreshAllCampaignStatusesScheduled();
        List<SpaceCampaign> active = campaignRepository.findByStatusIn(List.of("active"));
        int sent = 0;
        for (SpaceCampaign campaign : active) {
            sent += sendReminderForCampaign(campaign, evening);
        }
        log.info("打卡营{}提醒已发送 {} 条", evening ? "晚间" : "早间", sent);
        return sent;
    }

    private int sendReminderForCampaign(SpaceCampaign campaign, boolean evening) {
        int currentDay = currentDayNumber(campaign);
        if (currentDay < 1 || currentDay > campaign.getDurationDays()) {
            return 0;
        }
        Long groupId = campaign.getGroupId();
        PlatGroup group = groupRepository.findById(groupId).orElse(null);
        if (group == null) {
            return 0;
        }

        Set<Long> doneToday = new HashSet<>(
                progressRepository.findUserIdsByCampaignIdAndDayNumber(campaign.getId(), currentDay));
        List<PlatGroupMember> members = memberRepository.findByGroupIdAndStatusOrderByJoinedAtAsc(groupId, "approved");
        String link = "/platform/groups/" + groupId + "?camp=" + campaign.getId();
        String title = evening ? "打卡营晚间提醒" : "打卡营早间提醒";
        String content = evening
                ? "「" + group.getName() + "」打卡营今日任务还未完成，记得打卡。"
                : "「" + group.getName() + "」打卡营第 " + currentDay + " 天任务已开启，记得完成哦。";

        int sent = 0;
        for (PlatGroupMember m : members) {
            if (doneToday.contains(m.getUserId())) {
                continue;
            }
            var n = notificationService.createNotification(
                    m.getUserId(),
                    NotificationCatalog.TYPE_SPACE_CAMPAIGN_REMINDER,
                    title,
                    content,
                    link,
                    RELATED_TYPE,
                    String.valueOf(campaign.getId())
            );
            if (n != null) {
                sent++;
            }
        }
        return sent;
    }

    private void publishCampaignFeedPost(Long groupId, User user, int dayNumber, String taskTitle) {
        String nickname = user.getUsername() != null && !user.getUsername().isBlank()
                ? user.getUsername() : "成员";
        String safeTitle = taskTitle != null && !taskTitle.isBlank() ? taskTitle : "打卡任务";
        String content = nickname + " 完成了打卡营第 " + dayNumber + " 天：" + safeTitle;

        PlatGroupPost post = new PlatGroupPost();
        post.setGroupId(groupId);
        post.setUserId(user.getUserid());
        post.setContent(content);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setStatus("published");
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);

        groupRepository.findById(groupId).ifPresent(g -> {
            g.setPostCount((g.getPostCount() == null ? 0 : g.getPostCount()) + 1);
            g.setLastActiveAt(LocalDateTime.now());
            groupRepository.save(g);
        });
    }

    private boolean isDayCompletable(SpaceCampaign campaign, int dayNumber) {
        int currentDay = currentDayNumber(campaign);
        if (dayNumber < 1 || dayNumber > currentDay || dayNumber > campaign.getDurationDays()) {
            return false;
        }
        if (!Boolean.TRUE.equals(campaign.getAllowMakeup())) {
            return dayNumber == currentDay;
        }
        int minDay = Math.max(1, currentDay - effectiveMakeupLimit(campaign));
        return dayNumber >= minDay;
    }

    private int effectiveMakeupLimit(SpaceCampaign campaign) {
        if (!Boolean.TRUE.equals(campaign.getAllowMakeup())) {
            return 0;
        }
        Integer limit = campaign.getMakeupDaysLimit();
        return limit == null ? DEFAULT_MAKEUP_LIMIT : Math.max(0, limit);
    }

    private boolean parseAllowMakeup(Object raw) {
        if (raw == null) {
            return true;
        }
        if (raw instanceof Boolean b) {
            return b;
        }
        String s = String.valueOf(raw).trim().toLowerCase();
        return !"false".equals(s) && !"0".equals(s);
    }

    private int parseMakeupDaysLimit(Object raw) {
        if (raw == null) {
            return DEFAULT_MAKEUP_LIMIT;
        }
        try {
            int v = Integer.parseInt(String.valueOf(raw).trim());
            return Math.max(0, Math.min(DEFAULT_DURATION, v));
        } catch (NumberFormatException e) {
            return DEFAULT_MAKEUP_LIMIT;
        }
    }

    private void endOtherActiveCampaigns(Long groupId) {
        List<SpaceCampaign> active = campaignRepository.findByGroupIdAndStatusOrderByStartDateDesc(groupId, "active");
        for (SpaceCampaign c : active) {
            c.setStatus("ended");
            campaignRepository.save(c);
        }
        List<SpaceCampaign> scheduled = campaignRepository.findByGroupIdAndStatusOrderByStartDateDesc(groupId, "scheduled");
        for (SpaceCampaign c : scheduled) {
            c.setStatus("ended");
            campaignRepository.save(c);
        }
    }

    private void refreshCampaignStatuses(Long groupId) {
        for (SpaceCampaign c : campaignRepository.findByGroupIdOrderByCreatedAtDesc(groupId)) {
            refreshSingleCampaignStatus(c);
        }
    }

    private void refreshSingleCampaignStatus(SpaceCampaign campaign) {
        if (campaign == null || "ended".equals(campaign.getStatus())) {
            return;
        }
        String next = resolveStatusForCampaign(campaign);
        if (!next.equals(campaign.getStatus())) {
            campaign.setStatus(next);
            campaignRepository.save(campaign);
        }
    }

    private String resolveStatusForCampaign(SpaceCampaign campaign) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = campaign.getStartDate();
        int duration = campaign.getDurationDays() != null ? campaign.getDurationDays() : DEFAULT_DURATION;
        if (today.isBefore(startDate)) {
            return "scheduled";
        }
        LocalDate end = startDate.plusDays(duration - 1L);
        if (today.isAfter(end)) {
            return "ended";
        }
        return "active";
    }

    private Optional<SpaceCampaign> resolveViewableCampaign(Long groupId) {
        refreshCampaignStatuses(groupId);
        LocalDate today = LocalDate.now();
        return campaignRepository.findByGroupIdOrderByCreatedAtDesc(groupId).stream()
                .filter(c -> !"ended".equals(c.getStatus()))
                .filter(c -> {
                    LocalDate end = c.getStartDate().plusDays(c.getDurationDays() - 1L);
                    return !today.isAfter(end);
                })
                .filter(c -> !today.isBefore(c.getStartDate()) || "scheduled".equals(c.getStatus()))
                .findFirst();
    }

    private int currentDayNumber(SpaceCampaign campaign) {
        LocalDate today = LocalDate.now();
        if (today.isBefore(campaign.getStartDate())) {
            return 0;
        }
        long days = ChronoUnit.DAYS.between(campaign.getStartDate(), today) + 1;
        if (days > campaign.getDurationDays()) {
            return campaign.getDurationDays() + 1;
        }
        return (int) days;
    }

    private Map<String, Object> buildCampaignView(SpaceCampaign campaign, Long userId, boolean includeStats) {
        Map<String, Object> out = summarizeCampaign(campaign, true, includeStats);
        List<SpaceCampaignDay> days = dayRepository.findByCampaignIdOrderByDayNumberAsc(campaign.getId());

        int currentDay = currentDayNumber(campaign);
        out.put("currentDayNumber", currentDay);

        List<SpaceCampaignProgress> myRows = progressRepository
                .findByCampaignIdAndUserIdOrderByDayNumberAsc(campaign.getId(), userId);
        Set<Integer> myDone = myRows.stream().map(SpaceCampaignProgress::getDayNumber).collect(Collectors.toSet());
        out.put("myCompletedDays", myDone.size());
        out.put("myCompletedDayNumbers", myDone.stream().sorted().collect(Collectors.toList()));

        List<Map<String, Object>> dayStates = new ArrayList<>();
        for (SpaceCampaignDay d : days) {
            Map<String, Object> state = toDayMap(d);
            int dn = d.getDayNumber();
            boolean completed = myDone.contains(dn);
            boolean canComplete = !completed && isDayCompletable(campaign, dn);
            state.put("completedByMe", completed);
            state.put("canComplete", canComplete);
            state.put("makeup", canComplete && dn < currentDay);
            dayStates.add(state);
        }
        out.put("dayStates", dayStates);
        out.put("days", dayStates);

        SpaceCampaignDay todayTask = days.stream()
                .filter(d -> d.getDayNumber().equals(currentDay))
                .findFirst()
                .orElse(null);
        if (todayTask != null) {
            Map<String, Object> today = toDayMap(todayTask);
            today.put("completedByMe", myDone.contains(currentDay));
            today.put("canComplete", isDayCompletable(campaign, currentDay) && !myDone.contains(currentDay));
            out.put("todayTask", today);
        } else {
            out.put("todayTask", null);
        }

        if (includeStats) {
            out.put("stats", buildStats(campaign));
        }
        return out;
    }

    /**
     * 供 Space 运营数据面板复用：返回当前进行中打卡营的核心指标与掉队成员。
     * 调用方需自行校验管理权限。
     */
    public Map<String, Object> buildActiveCampaignStatsSnapshot(Long groupId) {
        refreshCampaignStatuses(groupId);
        Optional<SpaceCampaign> opt = campaignRepository
                .findFirstByGroupIdAndStatusOrderByStartDateDescCreatedAtDesc(groupId, "active");
        Map<String, Object> section = new LinkedHashMap<>();
        if (opt.isEmpty()) {
            section.put("activeCampaignTitle", "");
            section.put("campaignParticipants", 0);
            section.put("campaignTodayCompleted", 0);
            section.put("campaignCompletionRate", 0.0);
            section.put("fallenBehindCount", 0);
            section.put("fallenBehindMembers", List.of());
            return section;
        }
        SpaceCampaign campaign = opt.get();
        Map<String, Object> stats = buildStats(campaign);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> fallenBehind = (List<Map<String, Object>>) stats.get("fallenBehindMembers");
        if (fallenBehind == null) {
            fallenBehind = List.of();
        }
        long memberTotal = memberRepository.countByGroupIdAndStatus(groupId, "approved");
        long totalDone = progressRepository.countTotalCompletions(campaign.getId());
        long totalSlots = memberTotal * campaign.getDurationDays();
        double completionRate = totalSlots > 0 ? (totalDone * 100.0 / totalSlots) : 0.0;

        section.put("activeCampaignTitle", campaign.getTitle());
        section.put("campaignParticipants", memberTotal);
        section.put("campaignTodayCompleted", stats.get("todayCompletedCount"));
        section.put("campaignCompletionRate", Math.round(completionRate * 10) / 10.0);
        section.put("fallenBehindCount", fallenBehind.size());
        section.put("fallenBehindMembers", fallenBehind);
        return section;
    }

    private Map<String, Object> buildStats(SpaceCampaign campaign) {
        Long groupId = campaign.getGroupId();
        long participants = progressRepository.countDistinctParticipants(campaign.getId());
        int currentDay = currentDayNumber(campaign);
        long todayDone = currentDay >= 1 && currentDay <= campaign.getDurationDays()
                ? progressRepository.countByCampaignIdAndDayNumber(campaign.getId(), currentDay)
                : 0;

        List<PlatGroupMember> approved = memberRepository.findByGroupIdAndStatusOrderByJoinedAtAsc(groupId, "approved");
        int memberTotal = approved.size();
        long todayNotCompleted = currentDay >= 1 && currentDay <= campaign.getDurationDays()
                ? Math.max(0, memberTotal - todayDone)
                : 0;

        long totalSlots = participants * campaign.getDurationDays();
        long totalDone = progressRepository.countTotalCompletions(campaign.getId());
        double completionRate = totalSlots > 0 ? (totalDone * 100.0 / totalSlots) : 0.0;

        Map<Integer, Long> dayCounts = new HashMap<>();
        for (Object[] row : progressRepository.countCompletionsByDay(campaign.getId())) {
            dayCounts.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }
        List<Map<String, Object>> daily = new ArrayList<>();
        for (int d = 1; d <= campaign.getDurationDays(); d++) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("dayNumber", d);
            item.put("completedCount", dayCounts.getOrDefault(d, 0L));
            daily.add(item);
        }

        int streakCompletedCount = 0;
        List<Map<String, Object>> fallenBehindMembers = new ArrayList<>();
        if (currentDay >= 1 && currentDay <= campaign.getDurationDays()) {
            for (PlatGroupMember m : approved) {
                Set<Integer> done = progressRepository
                        .findByCampaignIdAndUserIdOrderByDayNumberAsc(campaign.getId(), m.getUserId())
                        .stream()
                        .map(SpaceCampaignProgress::getDayNumber)
                        .collect(Collectors.toSet());
                boolean fullStreak = true;
                List<Integer> missed = new ArrayList<>();
                for (int d = 1; d <= currentDay; d++) {
                    if (!done.contains(d)) {
                        fullStreak = false;
                        missed.add(d);
                    }
                }
                if (fullStreak) {
                    streakCompletedCount++;
                } else if (!missed.isEmpty()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("userId", m.getUserId());
                    row.put("username", resolveDisplayName(m));
                    row.put("missedDays", missed);
                    row.put("missedCount", missed.size());
                    fallenBehindMembers.add(row);
                }
            }
            fallenBehindMembers.sort((a, b) -> Integer.compare(
                    ((Number) b.get("missedCount")).intValue(),
                    ((Number) a.get("missedCount")).intValue()
            ));
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("participantCount", participants);
        stats.put("memberTotal", memberTotal);
        stats.put("todayCompletedCount", todayDone);
        stats.put("todayNotCompletedCount", todayNotCompleted);
        stats.put("streakCompletedCount", streakCompletedCount);
        stats.put("totalCompletionRate", Math.round(completionRate * 10) / 10.0);
        stats.put("dailyCompletions", daily);
        stats.put("fallenBehindMembers", fallenBehindMembers);
        return stats;
    }

    private String resolveDisplayName(PlatGroupMember m) {
        if (m.getMemberRealName() != null && !m.getMemberRealName().isBlank()) {
            return m.getMemberRealName();
        }
        return userRepository.findById(m.getUserId())
                .map(User::getUsername)
                .orElse("成员" + m.getUserId());
    }

    private Map<String, Object> summarizeCampaign(SpaceCampaign c, boolean withDescription, boolean withStats) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("groupId", c.getGroupId());
        m.put("title", c.getTitle());
        if (withDescription) {
            m.put("description", c.getDescription());
        }
        m.put("startDate", c.getStartDate());
        m.put("durationDays", c.getDurationDays());
        m.put("status", c.getStatus());
        m.put("allowMakeup", Boolean.TRUE.equals(c.getAllowMakeup()));
        m.put("makeupDaysLimit", effectiveMakeupLimit(c));
        m.put("createdBy", c.getCreatedBy());
        m.put("createdAt", c.getCreatedAt());
        if (withStats) {
            m.put("stats", buildStats(c));
        }
        return m;
    }

    private Map<String, Object> toDayMap(SpaceCampaignDay d) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("dayNumber", d.getDayNumber());
        m.put("taskTitle", d.getTaskTitle());
        m.put("taskDescription", d.getTaskDescription());
        return m;
    }

    private LocalDate parseStartDate(Object raw) {
        if (raw == null) return null;
        String s = String.valueOf(raw).trim();
        if (s.isBlank()) return null;
        try {
            return LocalDate.parse(s.length() > 10 ? s.substring(0, 10) : s);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "开始日期格式不正确");
        }
    }

    private PlatGroup requireGroupExists(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "团体不存在"));
    }

    private SpaceCampaign requireCampaignInGroup(Long groupId, Long campaignId) {
        SpaceCampaign c = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "打卡营不存在"));
        if (!Objects.equals(c.getGroupId(), groupId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "打卡营不属于该团体");
        }
        return c;
    }

    private boolean canManage(Long groupId, User user) {
        if (adminAuthService.isAdmin(user)) {
            return true;
        }
        return memberRepository.findByGroupIdAndUserId(groupId, user.getUserid())
                .filter(m -> "approved".equals(m.getStatus())
                        && ("owner".equals(m.getRole()) || "admin".equals(m.getRole())))
                .isPresent();
    }

    private void requireManagerOrSiteAdmin(Long groupId, User user) {
        if (canManage(groupId, user)) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "需要团体管理权限");
    }

    private void requireApprovedMember(Long groupId, Long userId) {
        memberRepository.findByGroupIdAndUserId(groupId, userId)
                .filter(m -> "approved".equals(m.getStatus()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "仅团体成员可参与"));
    }

    private void requireApprovedMemberOrManager(Long groupId, User user) {
        if (canManage(groupId, user)) {
            return;
        }
        requireApprovedMember(groupId, user.getUserid());
    }
}
