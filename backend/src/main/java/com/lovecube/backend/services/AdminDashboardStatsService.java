package com.lovecube.backend.services;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.lovecube.backend.entity.UserInteraction;
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
import com.lovecube.backend.repository.VerificationRequestRepository;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.Optional;

/**
 * 管理端工作台聚合统计。带短时内存缓存，减轻反复打开仪表盘时的数据库压力。
 */
@Service
public class AdminDashboardStatsService {

    private static final String HIDDEN_SUPER_ADMIN_PHONE = "15030251407";

    private final AnnouncementRepository announcementRepository;
    private final ArticleRepository articleRepository;
    private final PlatformEventRepository platformEventRepository;
    private final UserRepository userRepository;
    private final VerificationRequestRepository verificationRequestRepository;
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

    public AdminDashboardStatsService(
            AnnouncementRepository announcementRepository,
            ArticleRepository articleRepository,
            PlatformEventRepository platformEventRepository,
            UserRepository userRepository,
            VerificationRequestRepository verificationRequestRepository,
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
            UserDailyTaskProgressRepository userDailyTaskProgressRepository,
            UserBadgeRepository userBadgeRepository,
            UserBlacklistRepository userBlacklistRepository,
            InviteRecordRepository inviteRecordRepository,
            EventSignupRepository eventSignupRepository,
            UserPhotoRepository userPhotoRepository,
            SiteVisitLogRepository siteVisitLogRepository,
            NotificationService notificationService,
            @Value("${lovecube.admin.dashboard-stats-cache-seconds:45}") long dashboardStatsCacheSeconds
    ) {
        this.announcementRepository = announcementRepository;
        this.articleRepository = articleRepository;
        this.platformEventRepository = platformEventRepository;
        this.userRepository = userRepository;
        this.verificationRequestRepository = verificationRequestRepository;
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
        this.userDailyTaskProgressRepository = userDailyTaskProgressRepository;
        this.userBadgeRepository = userBadgeRepository;
        this.userBlacklistRepository = userBlacklistRepository;
        this.inviteRecordRepository = inviteRecordRepository;
        this.eventSignupRepository = eventSignupRepository;
        this.userPhotoRepository = userPhotoRepository;
        this.siteVisitLogRepository = siteVisitLogRepository;
        this.notificationService = notificationService;
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
        long pendingVerifications = verificationRequestRepository.countByStatusIgnoreCase("pending")
                + userVerificationRepository.countByStatus("pending");
        long pendingReports = reportRecordRepository.countByStatusIgnoreCase("pending");
        long pendingFeedbacks = userFeedbackRepository.countByStatusNot("resolved");
        long todayReports = reportRecordRepository.countByCreatedAtGreaterThanEqual(todayStart);
        long handledReports = reportRecordRepository.countHandledReports();
        long pendingTasks = pendingVerifications + pendingReports + pendingFeedbacks;

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

        long fellowshipProfilesTotal = fellowshipProfileRepository.count();
        long fellowshipProfilesBasicFilled = fellowshipProfileRepository.countWithNicknameAndAvatar();

        long matchRecordsTotal = matchRecordRepository.count();
        long matchRecordsToday = matchRecordRepository.countByCreatedAtGreaterThanEqual(matchSince);
        long matchRecordsSevenDays = matchRecordRepository.countByCreatedAtGreaterThanEqual(matchSevenSince);

        long dynamicsTotal = dynamicRepository.countByIsDeletedFalse();
        long dynamicsToday = dynamicRepository.countByIsDeletedFalseAndCreatedAtGreaterThanEqual(todayStart);
        long dynamicsSevenDays = dynamicRepository.countByIsDeletedFalseAndCreatedAtGreaterThanEqual(sevenDaysStart);
        long dynamicCommentsSum = dynamicRepository.sumCommentCountVisible();
        long dynamicLikesSum = dynamicRepository.sumLikeCountVisible();

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
                "contentPublishedLast30d", contentPublishedLast30d
        ));
        stats.put("fellowshipData", Map.of(
                "todayLikes", todayLikes,
                "todayMessages", todayMessages,
                "pendingVerifications", pendingVerifications,
                "pendingReports", pendingReports
        ));
        stats.put("governanceData", Map.of(
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

        Map<String, Object> engagementData = new LinkedHashMap<>();
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

        stats.put("notificationData", notificationService.getAdminDashboardNotificationStats(todayStart));
        return stats;
    }
}
