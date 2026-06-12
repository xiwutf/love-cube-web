package com.lovecube.backend.services;

import com.lovecube.backend.entity.PlatformEvent;

import java.time.LocalDateTime;

/**
 * 平台活动开始/结束时间推导与参与状态（P1.5）。
 */
public final class PlatformEventLifecycle {

    public static final int DEFAULT_DURATION_HOURS = 3;

    private PlatformEventLifecycle() {
    }

    public static LocalDateTime resolveStart(PlatformEvent event) {
        return event != null ? event.getEventTime() : null;
    }

    /** end_time 为空时默认 start + 3 小时 */
    public static LocalDateTime resolveEnd(PlatformEvent event) {
        if (event == null) {
            return null;
        }
        if (event.getEndTime() != null) {
            return event.getEndTime();
        }
        LocalDateTime start = event.getEventTime();
        return start != null ? start.plusHours(DEFAULT_DURATION_HOURS) : null;
    }

    public static boolean hasStarted(PlatformEvent event, LocalDateTime now) {
        LocalDateTime start = resolveStart(event);
        return start == null || !start.isAfter(now);
    }

    public static boolean isEnded(PlatformEvent event, LocalDateTime now) {
        LocalDateTime end = resolveEnd(event);
        return end != null && end.isBefore(now);
    }

    public static boolean isOngoing(PlatformEvent event, LocalDateTime now) {
        return hasStarted(event, now) && !isEnded(event, now);
    }

    /**
     * 用户参与状态码：
     * waiting_start | checkin_pending | checked_in_waiting | checked_in_ongoing |
     * review_pending | completed | missed
     */
    public static String resolveParticipationStatus(
            boolean checkedIn,
            PlatformEvent event,
            LocalDateTime now,
            boolean reviewCompleted
    ) {
        if (isEnded(event, now)) {
            if (!checkedIn) {
                return "missed";
            }
            return reviewCompleted ? "completed" : "review_pending";
        }
        if (checkedIn) {
            return hasStarted(event, now) ? "checked_in_ongoing" : "checked_in_waiting";
        }
        return hasStarted(event, now) ? "checkin_pending" : "waiting_start";
    }
}
