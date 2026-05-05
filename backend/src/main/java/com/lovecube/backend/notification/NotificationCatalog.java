package com.lovecube.backend.notification;

import java.util.List;
import java.util.Locale;

/**
 * 通知类型、分级、默认微信开关、Tab 分类。
 * <p><strong>类型字符串契约：</strong>所有写入 {@code user_notifications.type} / 设置行的业务类型必须为本类中定义的
 * {@code TYPE_*} 常量（或本类 {@code resolveLevel} / Tab 规则中显式列出的历史兼容字面量），<strong>禁止</strong>在业务代码中
 * 随意硬编码新的类型字符串而不先扩展本类。</p>
 */
public final class NotificationCatalog {

    private NotificationCatalog() {}

    public static final String LEVEL_IMPORTANT = "IMPORTANT";
    public static final String LEVEL_INTERACTION = "INTERACTION";
    public static final String LEVEL_ANNOUNCEMENT = "ANNOUNCEMENT";
    public static final String LEVEL_SYSTEM = "SYSTEM";

    public static final String PUSH_SKIPPED = "SKIPPED";
    public static final String PUSH_PENDING = "PENDING";
    public static final String PUSH_MOCK_SENT = "MOCK_SENT";
    public static final String PUSH_FAILED = "FAILED";

    public static final String CHANNEL_SITE = "SITE";

    /** 业务类型（与 user_notification_settings.type 一致） */
    public static final String TYPE_GROUP_APPLICATION_APPROVED = "GROUP_APPLICATION_APPROVED";
    public static final String TYPE_GROUP_APPLICATION_REJECTED = "GROUP_APPLICATION_REJECTED";
    public static final String TYPE_GROUP_JOIN_REQUEST = "GROUP_JOIN_REQUEST";
    public static final String TYPE_CONTENT_MODERATION_PASSED = "CONTENT_MODERATION_PASSED";
    public static final String TYPE_CONTENT_MODERATION_REJECTED = "CONTENT_MODERATION_REJECTED";
    public static final String TYPE_CONTENT_COMMENTED = "CONTENT_COMMENTED";
    public static final String TYPE_PLATFORM_ANNOUNCEMENT = "PLATFORM_ANNOUNCEMENT";
    public static final String TYPE_MATCH_PROFILE_REVIEW_PASSED = "MATCH_PROFILE_REVIEW_PASSED";
    public static final String TYPE_MATCH_PROFILE_REVIEW_REJECTED = "MATCH_PROFILE_REVIEW_REJECTED";
    public static final String TYPE_PROFILE_LIKED = "PROFILE_LIKED";
    public static final String TYPE_PROFILE_VIEWED = "PROFILE_VIEWED";
    public static final String TYPE_CONTENT_LIKED = "CONTENT_LIKED";
    public static final String TYPE_USER_FOLLOWED = "USER_FOLLOWED";
    public static final String TYPE_MUTUAL_MATCH = "MUTUAL_MATCH";

    /** 设置面板与默认行：覆盖所有可配置类型 */
    public static List<String> allConfigurableTypes() {
        return List.of(
            TYPE_GROUP_APPLICATION_APPROVED,
            TYPE_GROUP_APPLICATION_REJECTED,
            TYPE_GROUP_JOIN_REQUEST,
            TYPE_CONTENT_MODERATION_PASSED,
            TYPE_CONTENT_MODERATION_REJECTED,
            TYPE_CONTENT_COMMENTED,
            TYPE_PLATFORM_ANNOUNCEMENT,
            TYPE_MATCH_PROFILE_REVIEW_PASSED,
            TYPE_MATCH_PROFILE_REVIEW_REJECTED,
            TYPE_PROFILE_LIKED,
            TYPE_PROFILE_VIEWED,
            TYPE_CONTENT_LIKED,
            TYPE_USER_FOLLOWED
        );
    }

    public static String resolveLevel(String type) {
        if (type == null) return LEVEL_SYSTEM;
        String t = type.toUpperCase(Locale.ROOT);
        return switch (t) {
            case TYPE_GROUP_APPLICATION_APPROVED,
                 TYPE_GROUP_APPLICATION_REJECTED,
                 TYPE_GROUP_JOIN_REQUEST,
                 TYPE_CONTENT_MODERATION_PASSED,
                 TYPE_CONTENT_MODERATION_REJECTED,
                 TYPE_MATCH_PROFILE_REVIEW_PASSED,
                 TYPE_MATCH_PROFILE_REVIEW_REJECTED -> LEVEL_IMPORTANT;
            case TYPE_PLATFORM_ANNOUNCEMENT -> LEVEL_ANNOUNCEMENT;
            case TYPE_PROFILE_LIKED,
                 TYPE_PROFILE_VIEWED,
                 TYPE_CONTENT_LIKED,
                 TYPE_CONTENT_COMMENTED,
                 TYPE_USER_FOLLOWED,
                 TYPE_MUTUAL_MATCH,
                 "LIKE",
                 "GROUP_POST_LIKED",
                 "GROUP_POST_COMMENTED",
                 "GROUP_POST_CREATED",
                 "GROUP_CHECKIN_CREATED",
                 "GROUP_TASK_REWARD_CLAIMED",
                 "GROUP_ACTIVITY_SIGNED_UP",
                 "GROUP_ACTIVITY_CANCELLED" -> LEVEL_INTERACTION;
            default -> LEVEL_SYSTEM;
        };
    }

    /**
     * 产品默认：重要/公告类默认开微信；互动类默认关；浏览类永远不进微信（在 Dispatch 里硬处理）。
     */
    public static boolean defaultWechatEnabled(String type) {
        String lvl = resolveLevel(type);
        if (LEVEL_ANNOUNCEMENT.equals(lvl) || LEVEL_IMPORTANT.equals(lvl)) {
            return true;
        }
        if (LEVEL_INTERACTION.equals(lvl)) {
            return false;
        }
        return false;
    }

    public static boolean defaultSiteEnabled(String type) {
        return true;
    }

    /**
     * 消息中心 Tab：GROUP / REVIEW / INTERACTION / ANNOUNCEMENT / SYSTEM（不含 ALL/UNREAD）。
     */
    public static boolean matchesTab(String type, String tab) {
        if (tab == null || tab.isBlank() || "ALL".equalsIgnoreCase(tab) || "UNREAD".equalsIgnoreCase(tab)) {
            return true;
        }
        String t = type == null ? "" : type.toUpperCase(Locale.ROOT);
        return switch (tab.toUpperCase(Locale.ROOT)) {
            case "GROUP" -> TYPE_GROUP_JOIN_REQUEST.equals(t)
                || t.startsWith("GROUP_APPLICATION")
                || "GROUP_POST_CREATED".equals(t)
                || t.startsWith("GROUP_CHECKIN")
                || t.startsWith("GROUP_TASK")
                || t.startsWith("GROUP_ACTIVITY");
            case "REVIEW" -> t.contains("MODERATION")
                || t.contains("MATCH_PROFILE_REVIEW")
                || TYPE_GROUP_APPLICATION_REJECTED.equals(t);
            case "INTERACTION" -> TYPE_PROFILE_LIKED.equals(t)
                || TYPE_PROFILE_VIEWED.equals(t)
                || TYPE_CONTENT_LIKED.equals(t)
                || TYPE_CONTENT_COMMENTED.equals(t)
                || TYPE_USER_FOLLOWED.equals(t)
                || "GROUP_POST_LIKED".equals(t)
                || "GROUP_POST_COMMENTED".equals(t)
                || "LIKE".equals(t)
                || TYPE_MUTUAL_MATCH.equals(t);
            case "ANNOUNCEMENT" -> TYPE_PLATFORM_ANNOUNCEMENT.equals(t);
            case "SYSTEM" -> LEVEL_SYSTEM.equals(resolveLevel(type))
                || "REPORT_HANDLED".equals(t)
                || "BANNED".equals(t)
                || "SYSTEM".equals(t)
                || "MESSAGE".equals(t);
            default -> true;
        };
    }

    public static boolean forceSkipWechat(String type) {
        return TYPE_PROFILE_VIEWED.equalsIgnoreCase(type == null ? "" : type);
    }
}
