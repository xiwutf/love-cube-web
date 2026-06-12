package com.lovecube.backend.services;

import com.lovecube.backend.entity.ActivityReminderLog;
import com.lovecube.backend.entity.EventSignup;
import com.lovecube.backend.entity.PlatGroupActivity;
import com.lovecube.backend.entity.PlatGroupActivitySignup;
import com.lovecube.backend.entity.PlatformEvent;
import com.lovecube.backend.notification.NotificationCatalog;
import com.lovecube.backend.repository.ActivityReminderLogRepository;
import com.lovecube.backend.repository.EventSignupRepository;
import com.lovecube.backend.repository.PlatGroupActivityRepository;
import com.lovecube.backend.repository.PlatGroupActivitySignupRepository;
import com.lovecube.backend.repository.PlatformEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ActivityReminderService {

    private static final Logger log = LoggerFactory.getLogger(ActivityReminderService.class);

    static final String SOURCE_EVENT = "EVENT";
    static final String SOURCE_GROUP_ACTIVITY = "GROUP_ACTIVITY";

    static final String TYPE_24H = "REMINDER_24H";
    static final String TYPE_2H = "REMINDER_2H";
    static final String TYPE_REVIEW = "REVIEW_REMINDER";

    private static final int CRON_WINDOW_MINUTES = 15;
    private static final int REVIEW_LOOKBACK_DAYS = 14;

    private final PlatformEventRepository eventRepository;
    private final EventSignupRepository eventSignupRepository;
    private final PlatGroupActivityRepository groupActivityRepository;
    private final PlatGroupActivitySignupRepository groupSignupRepository;
    private final ActivityReminderLogRepository reminderLogRepository;
    private final NotificationService notificationService;
    private final EventEngagementService eventEngagementService;
    private final GroupActivityEngagementService groupActivityEngagementService;

    public ActivityReminderService(
            PlatformEventRepository eventRepository,
            EventSignupRepository eventSignupRepository,
            PlatGroupActivityRepository groupActivityRepository,
            PlatGroupActivitySignupRepository groupSignupRepository,
            ActivityReminderLogRepository reminderLogRepository,
            NotificationService notificationService,
            EventEngagementService eventEngagementService,
            GroupActivityEngagementService groupActivityEngagementService
    ) {
        this.eventRepository = eventRepository;
        this.eventSignupRepository = eventSignupRepository;
        this.groupActivityRepository = groupActivityRepository;
        this.groupSignupRepository = groupSignupRepository;
        this.reminderLogRepository = reminderLogRepository;
        this.notificationService = notificationService;
        this.eventEngagementService = eventEngagementService;
        this.groupActivityEngagementService = groupActivityEngagementService;
    }

    public int runAllReminders() {
        int sent = 0;
        try {
            send24HourReminders();
        } catch (Exception e) {
            log.warn("活动 24h 提醒失败: {}", e.getMessage(), e);
        }
        try {
            send2HourReminders();
        } catch (Exception e) {
            log.warn("活动 2h 提醒失败: {}", e.getMessage(), e);
        }
        try {
            sendReviewReminders();
        } catch (Exception e) {
            log.warn("活动互评提醒失败: {}", e.getMessage(), e);
        }
        return sent;
    }

    void send24HourReminders() {
        LocalDateTime windowStart = LocalDateTime.now().plusHours(24);
        LocalDateTime windowEnd = windowStart.plusMinutes(CRON_WINDOW_MINUTES);

        List<PlatformEvent> events = eventRepository.findByStatusAndEventTimeGreaterThanEqualAndEventTimeLessThan(
                "published", windowStart, windowEnd);
        for (PlatformEvent event : events) {
            remindPlatformEventSignups(event, TYPE_24H, NotificationCatalog.TYPE_ACTIVITY_REMINDER_24H,
                    "活动即将开始（24 小时后）",
                    "你报名的活动「" + event.getTitle() + "」将在 24 小时后开始，请提前安排时间。");
        }

        for (PlatGroupActivity activity : groupActivityRepository.findPublishedStartingBetween(windowStart, windowEnd)) {
            remindGroupActivitySignups(activity, TYPE_24H, NotificationCatalog.TYPE_ACTIVITY_REMINDER_24H,
                    "团体活动即将开始（24 小时后）",
                    "你报名的活动「" + activity.getTitle() + "」将在 24 小时后开始，请提前安排时间。");
        }
    }

    void send2HourReminders() {
        LocalDateTime windowStart = LocalDateTime.now().plusHours(2);
        LocalDateTime windowEnd = windowStart.plusMinutes(CRON_WINDOW_MINUTES);

        List<PlatformEvent> events = eventRepository.findByStatusAndEventTimeGreaterThanEqualAndEventTimeLessThan(
                "published", windowStart, windowEnd);
        for (PlatformEvent event : events) {
            remindPlatformEventSignups(event, TYPE_2H, NotificationCatalog.TYPE_ACTIVITY_REMINDER_2H,
                    "活动即将开始（2 小时后）",
                    "你报名的活动「" + event.getTitle() + "」将在 2 小时后开始，请留意签到安排。");
        }

        for (PlatGroupActivity activity : groupActivityRepository.findPublishedStartingBetween(windowStart, windowEnd)) {
            remindGroupActivitySignups(activity, TYPE_2H, NotificationCatalog.TYPE_ACTIVITY_REMINDER_2H,
                    "团体活动即将开始（2 小时后）",
                    "你报名的活动「" + activity.getTitle() + "」将在 2 小时后开始，请留意现场签到码。");
        }
    }

    void sendReviewReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime since = now.minusDays(REVIEW_LOOKBACK_DAYS);

        List<PlatformEvent> endedEvents = eventRepository.findByStatusAndEventTimeLessThanAndEventTimeGreaterThanEqual(
                "published", now, since);
        for (PlatformEvent event : endedEvents) {
            if (!PlatformEventLifecycle.isEnded(event, now)) {
                continue;
            }
            for (EventSignup signup : eventSignupRepository.findByEventIdAndCheckedInTrue(event.getId())) {
                int pending = eventEngagementService.countPendingReviews(signup.getUserId(), event.getId());
                if (pending <= 0) {
                    continue;
                }
                trySendReminder(
                        SOURCE_EVENT,
                        event.getId(),
                        signup.getUserId(),
                        TYPE_REVIEW,
                        NotificationCatalog.TYPE_ACTIVITY_REVIEW_REMINDER,
                        "活动互评提醒",
                        "活动「" + event.getTitle() + "」已结束，还有 " + pending + " 位伙伴待你互评。",
                        "/events/" + event.getId(),
                        SOURCE_EVENT,
                        event.getId()
                );
            }
        }

        for (PlatGroupActivity activity : groupActivityRepository.findPublishedEndedBetween(since, now)) {
            for (PlatGroupActivitySignup signup : groupSignupRepository
                    .findByActivityIdAndStatusAndCheckedInTrue(activity.getId(), "signed_up")) {
                Map<String, Object> reviewSummary = groupActivityEngagementService.buildReviewSummary(
                        signup.getUserId(), activity.getId(), true, true);
                if (!Boolean.TRUE.equals(reviewSummary.get("canReview"))) {
                    continue;
                }
                int pending = reviewSummary.get("pendingReviewCount") instanceof Number n ? n.intValue() : 0;
                if (pending <= 0) {
                    continue;
                }
                trySendReminder(
                        SOURCE_GROUP_ACTIVITY,
                        String.valueOf(activity.getId()),
                        signup.getUserId(),
                        TYPE_REVIEW,
                        NotificationCatalog.TYPE_ACTIVITY_REVIEW_REMINDER,
                        "团体活动互评提醒",
                        "活动「" + activity.getTitle() + "」已结束，还有 " + pending + " 位伙伴待你互评。",
                        "/platform/groups/" + activity.getGroupId() + "/activities",
                        "platform_group",
                        String.valueOf(activity.getGroupId())
                );
            }
        }
    }

    private void remindPlatformEventSignups(PlatformEvent event, String reminderType, String notificationType,
                                            String title, String content) {
        for (EventSignup signup : eventSignupRepository.findByEventId(event.getId())) {
            trySendReminder(
                    SOURCE_EVENT,
                    event.getId(),
                    signup.getUserId(),
                    reminderType,
                    notificationType,
                    title,
                    content,
                    "/events/" + event.getId(),
                    SOURCE_EVENT,
                    event.getId()
            );
        }
    }

    private void remindGroupActivitySignups(PlatGroupActivity activity, String reminderType, String notificationType,
                                            String title, String content) {
        for (PlatGroupActivitySignup signup : groupSignupRepository.findByActivityId(activity.getId())) {
            if (!"signed_up".equals(signup.getStatus())) {
                continue;
            }
            trySendReminder(
                    SOURCE_GROUP_ACTIVITY,
                    String.valueOf(activity.getId()),
                    signup.getUserId(),
                    reminderType,
                    notificationType,
                    title,
                    content,
                    "/platform/groups/" + activity.getGroupId() + "/activities",
                    "platform_group",
                    String.valueOf(activity.getGroupId())
            );
        }
    }

    @Transactional
    protected void trySendReminder(String source, String activityId, Long userId, String reminderType,
                                   String notificationType, String title, String content,
                                   String linkUrl, String relatedType, String relatedId) {
        if (userId == null || activityId == null || activityId.isBlank()) {
            return;
        }
        if (reminderLogRepository.existsByActivitySourceAndActivityIdAndUserIdAndReminderType(
                source, activityId, userId, reminderType)) {
            return;
        }
        try {
            notificationService.createNotification(
                    userId, notificationType, title, content, linkUrl, relatedType, relatedId);
            ActivityReminderLog row = new ActivityReminderLog();
            row.setActivitySource(source);
            row.setActivityId(activityId);
            row.setUserId(userId);
            row.setReminderType(reminderType);
            LocalDateTime now = LocalDateTime.now();
            row.setSentAt(now);
            row.setCreatedAt(now);
            reminderLogRepository.save(row);
            log.info("活动提醒已发送 source={} activityId={} userId={} type={}",
                    source, activityId, userId, reminderType);
        } catch (Exception e) {
            log.warn("活动提醒发送失败 source={} activityId={} userId={} type={}: {}",
                    source, activityId, userId, reminderType, e.getMessage());
        }
    }
}
