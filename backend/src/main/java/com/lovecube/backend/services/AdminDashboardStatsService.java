package com.lovecube.backend.services;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.lovecube.backend.entity.Dynamic;
import com.lovecube.backend.entity.LocalResource;
import com.lovecube.backend.entity.ReportRecord;
import com.lovecube.backend.entity.UserFeedback;
import com.lovecube.backend.entity.UserInteraction;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.AnnouncementRepository;
import com.lovecube.backend.repository.ArticleRepository;
import com.lovecube.backend.repository.ChatMessageRepository;
import com.lovecube.backend.repository.DynamicRepository;
import com.lovecube.backend.repository.EventSignupRepository;
import com.lovecube.backend.repository.FellowshipProfileRepository;
import com.lovecube.backend.repository.GroupJoinRequestRepository;
import com.lovecube.backend.repository.GroupMemberRepository;
import com.lovecube.backend.repository.GroupPostRepository;
import com.lovecube.backend.repository.HelpReplyRepository;
import com.lovecube.backend.repository.HelpRequestRepository;
import com.lovecube.backend.repository.InviteRecordRepository;
import com.lovecube.backend.repository.LocalResourceRepository;
import com.lovecube.backend.repository.MatchRecordRepository;
import com.lovecube.backend.repository.PlatGroupActivitySignupRepository;
import com.lovecube.backend.repository.PlatformEventRepository;
import com.lovecube.backend.repository.PlatformGroupRepository;
import com.lovecube.backend.repository.PositiveShareCommentRepository;
import com.lovecube.backend.repository.PositiveShareRepository;
import com.lovecube.backend.repository.ReportRecordRepository;
import com.lovecube.backend.repository.SiteVisitLogRepository;
import com.lovecube.backend.repository.UserBadgeRepository;
import com.lovecube.backend.repository.UserBlacklistRepository;
import com.lovecube.backend.repository.UserDailyTaskProgressRepository;
import com.lovecube.backend.repository.UserFeedbackRepository;
import com.lovecube.backend.repository.UserInteractionRepository;
import com.lovecube.backend.repository.UserPhotoRepository;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.repository.UserVerificationRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * 管理端工作台聚合统计。带短时内存缓存，减轻反复打开仪表盘时的数据库压力。
 */
@Service
public class AdminDashboardStatsService {
    private static final String SCENE_TYPE_FELLOWSHIP = "FELLOWSHIP";

    private static final String HIDDEN_SUPER_ADMIN_PHONE = "15030251407";

    private final AnnouncementRepository announcementRepository;
    private final ArticleRepository articleRepository;
    private final PlatformEventRepository platformEventRepository;
    private final UserRepository userRepository;
    private final UserVerificationRepository userVerificationRepository;
    private final ReportRecordRepository reportRecordRepository;
    private final UserFeedbackRepository userFeedbackRepository;
    private final UserInteractionRepository userInteractionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final PlatformGroupRepository platformGroupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupPostRepository groupPostRepository;
    private final GroupJoinRequestRepository groupJoinRequestRepository;
    private final PlatGroupActivitySignupRepository platGroupActivitySignupRepository;
    private final PositiveShareRepository positiveShareRepository;
    private final PositiveShareCommentRepository positiveShareCommentRepository;
    private final HelpRequestRepository helpRequestRepository;
    private final HelpReplyRepository helpReplyRepository;
    private final FellowshipProfileRepository fellowshipProfileRepository;
    private final MatchRecordRepository matchRecordRepository;
    private final DynamicRepository dynamicRepository;
    private final LocalResourceRepository localResourceRepository;
    private final UserDailyTaskProgressRepository userDailyTaskProgressRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserBlacklistRepository userBlacklistRepository;
    private final InviteRecordRepository inviteRecordRepository;
    private final EventSignupRepository eventSignupRepository;
    private final UserPhotoRepository userPhotoRepository;
    private final SiteVisitLogRepository siteVisitLogRepository;
    private final NotificationService notificationService;

    private final long cacheTtlSeconds;
    private final Optional<LoadingCache<Boolean, Map<String, Object>>> cache;
    private final Executor trendsExecutor;

    public AdminDashboardStatsService(
            AnnouncementRepository announcementRepository,
            ArticleRepository articleRepository,
            PlatformEventRepository platformEventRepository,
            UserRepository userRepository,
            UserVerificationRepository userVerificationRepository,
            ReportRecordRepository reportRecordRepository,
            UserFeedbackRepository userFeedbackRepository,
            UserInteractionRepository userInteractionRepository,
            ChatMessageRepository chatMessageRepository,
            PlatformGroupRepository platformGroupRepository,
            GroupMemberRepository groupMemberRepository,
            GroupPostRepository groupPostRepository,
            GroupJoinRequestRepository groupJoinRequestRepository,
            PlatGroupActivitySignupRepository platGroupActivitySignupRepository,
            PositiveShareRepository positiveShareRepository,
            PositiveShareCommentRepository positiveShareCommentRepository,
            HelpRequestRepository helpRequestRepository,
            HelpReplyRepository helpReplyRepository,
            FellowshipProfileRepository fellowshipProfileRepository,
            MatchRecordRepository matchRecordRepository,
            DynamicRepository dynamicRepository,
            LocalResourceRepository localResourceRepository,
            UserDailyTaskProgressRepository userDailyTaskProgressRepository,
            UserBadgeRepository userBadgeRepository,
            UserBlacklistRepository userBlacklistRepository,
            InviteRecordRepository inviteRecordRepository,
            EventSignupRepository eventSignupRepository,
            UserPhotoRepository userPhotoRepository,
            SiteVisitLogRepository siteVisitLogRepository,
            NotificationService notificationService,
            @Qualifier("adminDashboardTrendsExecutor") Executor trendsExecutor,
            @Value("${lovecube.admin.dashboard-stats-cache-seconds:45}") long dashboardStatsCacheSeconds
    ) {
        this.announcementRepository = announcementRepository;
        this.articleRepository = articleRepository;
        this.platformEventRepository = platformEventRepository;
        this.userRepository = userRepository;
        this.userVerificationRepository = userVerificationRepository;
        this.reportRecordRepository = reportRecordRepository;
        this.userFeedbackRepository = userFeedbackRepository;
        this.userInteractionRepository = userInteractionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.platformGroupRepository = platformGroupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupPostRepository = groupPostRepository;
        this.groupJoinRequestRepository = groupJoinRequestRepository;
        this.platGroupActivitySignupRepository = platGroupActivitySignupRepository;
        this.positiveShareRepository = positiveShareRepository;
        this.positiveShareCommentRepository = positiveShareCommentRepository;
        this.helpRequestRepository = helpRequestRepository;
        this.helpReplyRepository = helpReplyRepository;
        this.fellowshipProfileRepository = fellowshipProfileRepository;
        this.matchRecordRepository = matchRecordRepository;
        this.dynamicRepository = dynamicRepository;
        this.localResourceRepository = localResourceRepository;
        this.userDailyTaskProgressRepository = userDailyTaskProgressRepository;
        this.userBadgeRepository = userBadgeRepository;
        this.userBlacklistRepository = userBlacklistRepository;
        this.inviteRecordRepository = inviteRecordRepository;
        this.eventSignupRepository = eventSignupRepository;
        this.userPhotoRepository = userPhotoRepository;
        this.siteVisitLogRepository = siteVisitLogRepository;
        this.notificationService = notificationService;
        this.trendsExecutor = trendsExecutor;
        this.cacheTtlSeconds = Math.max(0, dashboardStatsCacheSeconds);
        if (this.cacheTtlSeconds > 0) {
            this.cache = Optional.of(Caffeine.newBuilder()
                    .maximumSize(4)
                    .expireAfterWrite(Duration.ofSeconds(this.cacheTtlSeconds))
                    .build(this::buildDashboardStats));
        } else {
            this.cache = Optional.empty();
        }
    }

    /**
     * @param hiddenSuperAdminOperator 与 {@link AdminAuthService#isHiddenSuperAdmin} 一致，决定用户统计是否包含隐藏账号
     * @param refresh                  为 true 时清空缓存后重新计算
     */
    public Map<String, Object> getDashboardStats(boolean hiddenSuperAdminOperator, boolean refresh) {
        Map<String, Object> body;
        if (cache.isEmpty()) {
            body = buildDashboardStats(hiddenSuperAdminOperator);
        } else {
            if (refresh) {
                cache.get().invalidateAll();
            }
            body = cache.get().get(hiddenSuperAdminOperator);
        }
        Map<String, Object> out = new HashMap<>(body);
        out.put("statsCacheTtlSeconds", cacheTtlSeconds);
        return out;
    }

    private Map<String, Object> buildDashboardStats(boolean hiddenSuperAdminOperator) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime sevenDaysStart = LocalDate.now().minusDays(6).atStartOfDay();
        LocalDateTime thirtyDaysStart = LocalDate.now().minusDays(29).atStartOfDay();
        long todayStartMillis = java.sql.Timestamp.valueOf(todayStart).getTime();
        LocalDate today = LocalDate.now();
        Date matchSince = Date.from(todayStart.atZone(ZoneId.systemDefault()).toInstant());
        Date matchSevenSince = Date.from(sevenDaysStart.atZone(ZoneId.systemDefault()).toInstant());

        long totalUsers = hiddenSuperAdminOperator
                ? userRepository.count()
                : userRepository.countVisibleUsers(HIDDEN_SUPER_ADMIN_PHONE);
        long todayNewUsers = hiddenSuperAdminOperator
                ? userRepository.countByCreatedAtGreaterThanEqual(todayStart)
                : userRepository.countVisibleUsersCreatedSince(todayStart, HIDDEN_SUPER_ADMIN_PHONE);
        long sevenDayNewUsers = hiddenSuperAdminOperator
                ? userRepository.countByCreatedAtGreaterThanEqual(sevenDaysStart)
                : userRepository.countVisibleUsersCreatedSince(sevenDaysStart, HIDDEN_SUPER_ADMIN_PHONE);
        long bannedUsers = hiddenSuperAdminOperator
                ? userRepository.countByUserStatusIgnoreCase("DISABLED")
                : userRepository.countVisibleUsersByStatus("DISABLED", HIDDEN_SUPER_ADMIN_PHONE);

        long totalAnnouncements = announcementRepository.count();
        long totalArticles = articleRepository.count();
        long totalEvents = platformEventRepository.count();
        long pinnedContent = announcementRepository.countByPinnedTrue()
                + articleRepository.countByPinnedTrue()
                + platformEventRepository.countByPinnedTrue();
        long recommendedContent = announcementRepository.countByRecommendedTrue()
                + articleRepository.countByRecommendedTrue()
                + platformEventRepository.countByRecommendedTrue();

        long todayLikes = userInteractionRepository.countByInteractionTypeAndCreatedAtGreaterThanEqual(
                UserInteraction.InteractionType.LIKE, todayStart)
                + userInteractionRepository.countByInteractionTypeAndCreatedAtGreaterThanEqual(
                UserInteraction.InteractionType.SUPER_LIKE, todayStart);
        long todayMessages = chatMessageRepository.countByTimestampGreaterThanEqual(todayStartMillis);
        // 与后台「认证审核」列表一致：仅统计 user_verifications，避免 verification_requests 遗留 pending
        // 导致首页有数、审核页无待办。
        long pendingVerifications = userVerificationRepository.countByStatusIgnoreCase("pending");
        long pendingReports = reportRecordRepository.countByStatusIgnoreCase("pending");
        long pendingFeedbacks = userFeedbackRepository.countByStatusNot("resolved");
        long todayReports = reportRecordRepository.countByCreatedAtGreaterThanEqual(todayStart);
        long handledReports = reportRecordRepository.countHandledReports();
        long pendingTasks = pendingVerifications + pendingReports;

        long activeGroups = platformGroupRepository.countByStatus("active");
        long totalGroups = platformGroupRepository.count();
        long groupMembersTotal = groupMemberRepository.count();
        long groupPostsTotal = groupPostRepository.count();
        long groupPostsToday = groupPostRepository.countByCreatedAtGreaterThanEqual(todayStart);
        long pendingGroupJoinRequests = groupJoinRequestRepository.countByStatus("pending");
        long groupJoinRequestsToday = groupJoinRequestRepository.countByRequestedAtGreaterThanEqual(todayStart);
        long platActivitySignupsTotal = platGroupActivitySignupRepository.count();
        long platActivitySignupsToday = platGroupActivitySignupRepository.countByCreatedAtGreaterThanEqual(todayStart);

        long positiveSharesPublished = positiveShareRepository.countByStatus("PUBLISHED");
        long positiveSharesPending = positiveShareRepository.countByStatus("PENDING");
        long positiveSharesToday = positiveShareRepository.countByCreatedAtGreaterThanEqual(todayStart);
        long positiveShareCommentsTotal = positiveShareCommentRepository.count();
        long positiveShareCommentsToday = positiveShareCommentRepository.countByCreatedAtGreaterThanEqual(todayStart);

        long helpRequestsPending = helpRequestRepository.countByStatus("pending");
        long helpRequestsActive = helpRequestRepository.countByStatus("active");
        long helpRequestsResolved = helpRequestRepository.countByStatus("resolved");
        long helpRequestsClosed = helpRequestRepository.countByStatus("closed");
        long helpRequestsToday = helpRequestRepository.countByCreatedAtGreaterThanEqual(todayStart);
        long helpRepliesToday = helpReplyRepository.countByCreatedAtGreaterThanEqual(todayStart);
        long pendingLocalResources = localResourceRepository.countPendingForAdmin();

        long fellowshipProfilesTotal = fellowshipProfileRepository.count();
        long fellowshipProfilesBasicFilled = fellowshipProfileRepository.countWithNicknameAndAvatar();

        long matchRecordsTotal = matchRecordRepository.count();
        long matchRecordsToday = matchRecordRepository.countByCreatedAtGreaterThanEqual(matchSince);
        long matchRecordsSevenDays = matchRecordRepository.countByCreatedAtGreaterThanEqual(matchSevenSince);

        long dynamicsTotal = dynamicRepository.countByIsDeletedFalseAndSceneType(SCENE_TYPE_FELLOWSHIP);
        long dynamicsToday = dynamicRepository.countByIsDeletedFalseAndSceneTypeAndCreatedAtGreaterThanEqual(SCENE_TYPE_FELLOWSHIP, todayStart);
        long dynamicsSevenDays = dynamicRepository.countByIsDeletedFalseAndSceneTypeAndCreatedAtGreaterThanEqual(SCENE_TYPE_FELLOWSHIP, sevenDaysStart);
        long dynamicCommentsSum = dynamicRepository.sumCommentCountVisible(SCENE_TYPE_FELLOWSHIP);
        long dynamicLikesSum = dynamicRepository.sumLikeCountVisible(SCENE_TYPE_FELLOWSHIP);

        long dailyTasksCompletedToday = userDailyTaskProgressRepository.countByTaskDateAndCompleted(today, 1);
        long userBadgesGranted = userBadgeRepository.count();
        long blacklistEntries = userBlacklistRepository.count();
        long inviteRecordsTotal = inviteRecordRepository.count();
        long inviteSuccessTotal = inviteRecordRepository.countByStatus("SUCCESS");
        long inviteRecordsToday = inviteRecordRepository.countByCreatedAtGreaterThanEqual(todayStart);
        long eventSignupsTotal = eventSignupRepository.count();
        long eventSignupsToday = eventSignupRepository.countByCreatedAtGreaterThanEqual(todayStart);
        long userPhotosTotal = userPhotoRepository.count();
        long userPhotosToday = userPhotoRepository.countByCreatedAtGreaterThanEqual(todayStart);
        long contentPublishedLast30d = announcementRepository.countPublishedSince(thirtyDaysStart)
                + articleRepository.countPublishedSince(thirtyDaysStart)
                + platformEventRepository.countByCreatedAtGreaterThanEqual(thirtyDaysStart);
        long groupsCreatedLast30d = platformGroupRepository.countByCreatedAtGreaterThanEqual(thirtyDaysStart);
        long repeatVisitors7d = siteVisitLogRepository.countRepeatVisitorsSince(sevenDaysStart);
        long visitorsUv7d = siteVisitLogRepository.countDistinctVisitorIdSince(sevenDaysStart);
        long reportsLast7d = reportRecordRepository.countByCreatedAtGreaterThanEqual(sevenDaysStart);
        long contentPublishedLast7d = announcementRepository.countPublishedSince(sevenDaysStart)
                + articleRepository.countPublishedSince(sevenDaysStart)
                + platformEventRepository.countByCreatedAtGreaterThanEqual(sevenDaysStart);
        // 待处理内容：仅含确有审核/处置状态的类型；近7天动态量为运营参考，不应计入待办总数。
        long pendingContent = positiveSharesPending + helpRequestsPending + pendingLocalResources;

        Map<String, Object> trends = buildSevenDayTrends(hiddenSuperAdminOperator, sevenDaysStart);
        Map<String, Object> activityRatio = buildActivityRatio(
                contentPublishedLast7d + positiveSharesToday + pendingLocalResources,
                dynamicsSevenDays + matchRecordsSevenDays + todayLikes + todayMessages,
                groupPostsToday + pendingGroupJoinRequests + groupJoinRequestsToday
        );
        Map<String, Object> recent = buildRecent(
                hiddenSuperAdminOperator, pendingReports, pendingFeedbacks, pendingLocalResources);

        List<Object[]> reasonRows = new ArrayList<>(reportRecordRepository.countGroupedByReasonSince(sevenDaysStart));
        reasonRows.sort(Comparator.comparingLong(r -> -(((Number) r[1]).longValue())));
        List<Map<String, Object>> reportReasonTop = new ArrayList<>();
        for (Object[] row : reasonRows) {
            if (reportReasonTop.size() >= 8) {
                break;
            }
            String reason = row[0] != null && !String.valueOf(row[0]).isBlank() ? String.valueOf(row[0]) : "(未填)";
            long cnt = ((Number) row[1]).longValue();
            reportReasonTop.add(Map.of("reason", reason, "count", cnt));
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("todayNewUsers", todayNewUsers);
        stats.put("sevenDayNewUsers", sevenDayNewUsers);
        stats.put("bannedUsers", bannedUsers);
        stats.put("totalAnnouncements", totalAnnouncements);
        stats.put("totalArticles", totalArticles);
        stats.put("totalEvents", totalEvents);
        stats.put("pinnedContent", pinnedContent);
        stats.put("recommendedContent", recommendedContent);
        stats.put("todayLikes", todayLikes);
        stats.put("todayMessages", todayMessages);
        stats.put("pendingVerifications", pendingVerifications);
        stats.put("pendingReports", pendingReports);
        stats.put("pendingFeedbacks", pendingFeedbacks);
        stats.put("todayReports", todayReports);
        stats.put("handledReports", handledReports);
        stats.put("pendingTasks", pendingTasks);
        stats.put("userData", Map.of(
                "totalUsers", totalUsers,
                "todayNewUsers", todayNewUsers,
                "sevenDayNewUsers", sevenDayNewUsers,
                "bannedUsers", bannedUsers
        ));
        stats.put("contentData", Map.of(
                "totalAnnouncements", totalAnnouncements,
                "totalArticles", totalArticles,
                "totalEvents", totalEvents,
                "pinnedContent", pinnedContent,
                "recommendedContent", recommendedContent,
                "todayNewDynamics", dynamicsToday,
                "pendingContent", pendingContent,
                "contentPublishedLast7d", contentPublishedLast7d,
                "contentPublishedLast30d", contentPublishedLast30d
        ));
        stats.put("fellowshipData", Map.of(
                "todayLikes", todayLikes,
                "todayMessages", todayMessages,
                "pendingDynamics", dynamicsToday,
                "pendingVerifications", pendingVerifications,
                "pendingReports", pendingReports
        ));
        stats.put("governanceData", Map.of(
                "pendingReports", pendingReports,
                "todayReports", todayReports,
                "handledReports", handledReports,
                "bannedUsers", bannedUsers,
                "pendingTasks", pendingTasks,
                "reportsLast7d", reportsLast7d
        ));

        Map<String, Object> communityData = new LinkedHashMap<>();
        communityData.put("activeGroups", activeGroups);
        communityData.put("totalGroups", totalGroups);
        communityData.put("groupMembersTotal", groupMembersTotal);
        communityData.put("groupPostsTotal", groupPostsTotal);
        communityData.put("groupPostsToday", groupPostsToday);
        communityData.put("pendingGroupJoinRequests", pendingGroupJoinRequests);
        communityData.put("groupJoinRequestsToday", groupJoinRequestsToday);
        communityData.put("platActivitySignupsTotal", platActivitySignupsTotal);
        communityData.put("platActivitySignupsToday", platActivitySignupsToday);
        communityData.put("eventSignupsTotal", eventSignupsTotal);
        communityData.put("eventSignupsToday", eventSignupsToday);
        communityData.put("groupsCreatedLast30d", groupsCreatedLast30d);
        stats.put("communityData", communityData);

        Map<String, Object> helpAndShareData = new LinkedHashMap<>();
        helpAndShareData.put("helpRequestsPending", helpRequestsPending);
        helpAndShareData.put("helpRequestsActive", helpRequestsActive);
        helpAndShareData.put("helpRequestsResolved", helpRequestsResolved);
        helpAndShareData.put("helpRequestsClosed", helpRequestsClosed);
        helpAndShareData.put("helpRequestsToday", helpRequestsToday);
        helpAndShareData.put("helpRepliesToday", helpRepliesToday);
        helpAndShareData.put("positiveSharesPublished", positiveSharesPublished);
        helpAndShareData.put("positiveSharesPending", positiveSharesPending);
        helpAndShareData.put("positiveSharesToday", positiveSharesToday);
        helpAndShareData.put("positiveShareCommentsTotal", positiveShareCommentsTotal);
        helpAndShareData.put("positiveShareCommentsToday", positiveShareCommentsToday);
        stats.put("helpAndShareData", helpAndShareData);

        Map<String, Object> localResourceData = new LinkedHashMap<>();
        localResourceData.put("pendingLocalResources", pendingLocalResources);
        stats.put("localResourceData", localResourceData);

        Map<String, Object> feedbackData = new LinkedHashMap<>();
        feedbackData.put("pendingFeedbacks", pendingFeedbacks);
        stats.put("feedbackData", feedbackData);

        Map<String, Object> engagementData = new LinkedHashMap<>();
        // 新口径：显式标注联谊动态；同时保留旧字段，避免前端存量页面受影响。
        engagementData.put("fellowshipDynamicsTotal", dynamicsTotal);
        engagementData.put("fellowshipDynamicsToday", dynamicsToday);
        engagementData.put("fellowshipDynamicsSevenDays", dynamicsSevenDays);
        engagementData.put("fellowshipDynamicCommentsSum", dynamicCommentsSum);
        engagementData.put("fellowshipDynamicLikesSum", dynamicLikesSum);
        engagementData.put("dynamicsTotal", dynamicsTotal);
        engagementData.put("dynamicsToday", dynamicsToday);
        engagementData.put("dynamicsSevenDays", dynamicsSevenDays);
        engagementData.put("dynamicCommentsSum", dynamicCommentsSum);
        engagementData.put("dynamicLikesSum", dynamicLikesSum);
        engagementData.put("matchRecordsTotal", matchRecordsTotal);
        engagementData.put("matchRecordsToday", matchRecordsToday);
        engagementData.put("matchRecordsSevenDays", matchRecordsSevenDays);
        stats.put("engagementData", engagementData);

        Map<String, Object> growthData = new LinkedHashMap<>();
        growthData.put("fellowshipProfilesTotal", fellowshipProfilesTotal);
        growthData.put("fellowshipProfilesBasicFilled", fellowshipProfilesBasicFilled);
        growthData.put("dailyTasksCompletedToday", dailyTasksCompletedToday);
        growthData.put("userBadgesGranted", userBadgesGranted);
        growthData.put("blacklistEntries", blacklistEntries);
        growthData.put("inviteRecordsTotal", inviteRecordsTotal);
        growthData.put("inviteSuccessTotal", inviteSuccessTotal);
        growthData.put("inviteRecordsToday", inviteRecordsToday);
        growthData.put("userPhotosTotal", userPhotosTotal);
        growthData.put("userPhotosToday", userPhotosToday);
        stats.put("growthData", growthData);

        Map<String, Object> visitorQualityData = new LinkedHashMap<>();
        visitorQualityData.put("repeatVisitors7d", repeatVisitors7d);
        visitorQualityData.put("visitorsUv7d", visitorsUv7d);
        stats.put("visitorQualityData", visitorQualityData);

        Map<String, Object> reportInsightData = new LinkedHashMap<>();
        reportInsightData.put("reportReasonTop", reportReasonTop);
        stats.put("reportInsightData", reportInsightData);

        Map<String, Long> notificationData = notificationService.getAdminDashboardNotificationStats(todayStart);
        stats.put("notificationData", notificationData);
        stats.put("trends", trends);
        stats.put("activityRatio", activityRatio);
        stats.put("recent", recent);
        return stats;
    }

    private Map<String, Object> buildSevenDayTrends(boolean hiddenSuperAdminOperator, LocalDateTime sevenDaysStart) {
        List<CompletableFuture<TrendDayBucket>> futures = new ArrayList<>(7);
        for (int i = 0; i < 7; i++) {
            LocalDateTime start = sevenDaysStart.plusDays(i);
            LocalDateTime end = start.plusDays(1);
            futures.add(CompletableFuture.supplyAsync(
                    () -> buildOneDayTrendBucket(hiddenSuperAdminOperator, start, end),
                    trendsExecutor));
        }
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

        List<String> labels = new ArrayList<>(7);
        List<Long> newUsers = new ArrayList<>(7);
        List<Long> activeUsers = new ArrayList<>(7);
        List<Long> dynamics = new ArrayList<>(7);
        List<Long> comments = new ArrayList<>(7);
        List<Long> contentPublish = new ArrayList<>(7);
        for (CompletableFuture<TrendDayBucket> f : futures) {
            TrendDayBucket b = f.join();
            labels.add(b.label());
            newUsers.add(b.newUsers());
            activeUsers.add(b.activeUsers());
            dynamics.add(b.dynamics());
            comments.add(b.comments());
            contentPublish.add(b.contentPublish());
        }

        Map<String, Object> trends = new LinkedHashMap<>();
        trends.put("labels", labels);
        trends.put("newUsers", newUsers);
        trends.put("activeUsers", activeUsers);
        trends.put("dynamics", dynamics);
        trends.put("comments", comments);
        trends.put("contentPublish", contentPublish);
        return trends;
    }

    private TrendDayBucket buildOneDayTrendBucket(
            boolean hiddenSuperAdminOperator, LocalDateTime start, LocalDateTime end) {
        long startMillis = java.sql.Timestamp.valueOf(start).getTime();
        long endMillis = java.sql.Timestamp.valueOf(end).getTime();
        String label = String.format("%02d-%02d", start.getMonthValue(), start.getDayOfMonth());
        long newUsers = hiddenSuperAdminOperator
                ? userRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(start, end)
                : userRepository.countVisibleUsersCreatedBetween(start, end, HIDDEN_SUPER_ADMIN_PHONE);
        long activeFromDynamics = dynamicRepository.countDistinctActiveUsersBetween(SCENE_TYPE_FELLOWSHIP, start, end);
        long activeFromInteractions = userInteractionRepository.countDistinctActiveUsersBetween(start, end);
        long activeFromMessages = chatMessageRepository.countDistinctActiveSendersBetween(startMillis, endMillis);
        long activeUsers = Math.max(activeFromDynamics, Math.max(activeFromInteractions, activeFromMessages));
        long dynamics = dynamicRepository.countByIsDeletedFalseAndSceneTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(SCENE_TYPE_FELLOWSHIP, start, end);
        long comments = dynamicRepository.sumCommentCountVisibleBetween(SCENE_TYPE_FELLOWSHIP, start, end)
                + userInteractionRepository.countByInteractionTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                UserInteraction.InteractionType.COMMENT, start, end);
        long contentPublish = announcementRepository.countPublishedBetween(start, end)
                + articleRepository.countPublishedBetween(start, end)
                + platformEventRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(start, end)
                + positiveShareRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(start, end);
        return new TrendDayBucket(label, newUsers, activeUsers, dynamics, comments, contentPublish);
    }

    private record TrendDayBucket(
            String label,
            long newUsers,
            long activeUsers,
            long dynamics,
            long comments,
            long contentPublish
    ) {}

    private Map<String, Object> buildActivityRatio(long platformCount, long datingCount, long groupCount) {
        long total = platformCount + datingCount + groupCount;
        Map<String, Object> ratio = new LinkedHashMap<>();
        ratio.put("platform", total <= 0 ? 0 : roundPercent(platformCount, total));
        ratio.put("dating", total <= 0 ? 0 : roundPercent(datingCount, total));
        ratio.put("group", total <= 0 ? 0 : roundPercent(groupCount, total));
        return ratio;
    }

    private double roundPercent(long value, long total) {
        return Math.round(value * 1000.0 / total) / 10.0;
    }

    private Map<String, Object> buildRecent(
            boolean hiddenSuperAdminOperator,
            long pendingReports,
            long pendingFeedbacks,
            long pendingLocalResources
    ) {
        Map<String, Object> recent = new LinkedHashMap<>();
        recent.put("users", buildRecentUsers(hiddenSuperAdminOperator));
        recent.put("contents", buildRecentContents());
        recent.put("reports", buildRecentReports());
        recent.put("feedbacks", buildRecentFeedbacks());
        recent.put("notices", buildSystemNotices(pendingReports, pendingFeedbacks, pendingLocalResources));
        return recent;
    }

    private List<Map<String, Object>> buildRecentUsers(boolean hiddenSuperAdminOperator) {
        return userRepository.findRecentUsersForAdmin(3, hiddenSuperAdminOperator, HIDDEN_SUPER_ADMIN_PHONE).stream()
                .map(user -> recentItem(
                        initial(user.getUsername(), "用"),
                        safeText(user.getUsername(), "用户" + user.getUserid()),
                        "手机号：" + safeText(maskPhone(user.getPhoneNumber()), "未填写"),
                        user.getCreatedAt(),
                        "/admin/users?userId=" + user.getUserid()))
                .toList();
    }

    private List<Map<String, Object>> buildRecentContents() {
        List<Dynamic> dynamics = dynamicRepository
                .findByIsDeletedFalseAndSceneTypeOrderByCreatedAtDesc(PageRequest.of(0, 3), SCENE_TYPE_FELLOWSHIP)
                .getContent();
        Set<Long> userIds = dynamics.stream()
                .map(Dynamic::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> idToUsername = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (User u : userRepository.findAllById(userIds)) {
                idToUsername.put(u.getUserid(), safeText(u.getUsername(), ""));
            }
        }
        return dynamics.stream()
                .map(dynamic -> recentItem(
                        "动",
                        truncate(safeText(dynamic.getContent(), "动态内容"), 18),
                        "用户：" + safeText(
                                idToUsername.getOrDefault(dynamic.getUserId(), ""),
                                "用户" + dynamic.getUserId()),
                        dynamic.getCreatedAt(),
                        "/admin/fellowship-dynamics?dynamicId=" + dynamic.getId()))
                .toList();
    }

    private List<Map<String, Object>> buildRecentReports() {
        return reportRecordRepository.findByOrderByCreatedAtDesc(PageRequest.of(0, 3)).stream()
                .map(report -> recentItem(
                        "举",
                        safeText(report.getReasonType(), "用户举报"),
                        "被举报用户：" + safeText(String.valueOf(report.getTargetUserId()), "-"),
                        report.getCreatedAt(),
                        "/admin/reports?status=PENDING"))
                .toList();
    }

    private List<Map<String, Object>> buildRecentFeedbacks() {
        return userFeedbackRepository.findByOrderByCreatedAtDesc(PageRequest.of(0, 3)).stream()
                .map(feedback -> recentItem(
                        "反",
                        truncate(safeText(feedback.getContent(), "用户体验问卷"), 18),
                        "用户：" + safeText(feedback.getUsername(), "匿名用户"),
                        feedback.getCreatedAt(),
                        "/admin/feedbacks"))
                .toList();
    }

    private List<Map<String, Object>> buildSystemNotices(
            long pendingReports, long pendingFeedbacks, long pendingLocalResources) {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(systemNotice("服务器统计接口正常", "green", "/admin/analytics"));
        rows.add(systemNotice("待处理举报 " + pendingReports + " 条", "red", "/admin/reports?status=PENDING"));
        rows.add(systemNotice("用户体验问卷 " + pendingFeedbacks + " 份", "green", "/admin/feedbacks"));
        rows.add(systemNotice("待审核本地资源 " + pendingLocalResources + " 条", "amber", "/admin/local-resources"));
        return rows;
    }

    private Map<String, Object> recentItem(String avatar, String title, String desc, LocalDateTime time) {
        return recentItem(avatar, title, desc, time, null);
    }

    private Map<String, Object> recentItem(String avatar, String title, String desc, LocalDateTime time, String to) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("avatar", avatar);
        item.put("title", title);
        item.put("desc", desc);
        item.put("time", relativeTime(time));
        if (to != null && !to.isBlank()) {
            item.put("to", to);
        }
        return item;
    }

    private Map<String, Object> systemNotice(String title, String level, String to) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("avatar", "●");
        item.put("title", title);
        item.put("desc", "");
        item.put("time", "刚刚");
        item.put("level", level);
        if (to != null && !to.isBlank()) {
            item.put("to", to);
        }
        return item;
    }

    private Map<String, Object> systemNotice(String title, String level) {
        return systemNotice(title, level, null);
    }

    private String relativeTime(LocalDateTime time) {
        if (time == null) {
            return "-";
        }
        long minutes = Math.max(0, Duration.between(time, LocalDateTime.now()).toMinutes());
        if (minutes < 1) {
            return "刚刚";
        }
        if (minutes < 60) {
            return minutes + "分钟前";
        }
        long hours = minutes / 60;
        if (hours < 24) {
            return hours + "小时前";
        }
        return (hours / 24) + "天前";
    }

    private String safeText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String truncate(String value, int maxLength) {
        String text = safeText(value, "");
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    private String initial(String value, String fallback) {
        String text = safeText(value, fallback);
        return text.substring(0, 1);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
