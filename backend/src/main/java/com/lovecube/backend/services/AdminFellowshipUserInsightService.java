package com.lovecube.backend.services;

import com.lovecube.backend.entity.FellowshipProfile;
import com.lovecube.backend.entity.FellowshipProfileMain;
import com.lovecube.backend.entity.UserBadge;
import com.lovecube.backend.entity.UserProfile;
import com.lovecube.backend.growth.entity.UserGrowth;
import com.lovecube.backend.growth.repository.GrowthUserGrowthRepository;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.UserBadgeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 后台运营视角：用户联谊资料完成度、成长、徽章与权益摘要。
 */
@Service
public class AdminFellowshipUserInsightService {
    private final FellowshipGrowthSummaryService fellowshipGrowthSummaryService;
    private final FellowshipUserBadgeService fellowshipUserBadgeService;
    private final GrowthUserGrowthRepository growthUserGrowthRepository;
    private final UserBadgeRepository userBadgeRepository;

    public AdminFellowshipUserInsightService(
            FellowshipGrowthSummaryService fellowshipGrowthSummaryService,
            FellowshipUserBadgeService fellowshipUserBadgeService,
            GrowthUserGrowthRepository growthUserGrowthRepository,
            UserBadgeRepository userBadgeRepository
    ) {
        this.fellowshipGrowthSummaryService = fellowshipGrowthSummaryService;
        this.fellowshipUserBadgeService = fellowshipUserBadgeService;
        this.growthUserGrowthRepository = growthUserGrowthRepository;
        this.userBadgeRepository = userBadgeRepository;
    }

    public InsightBatchContext prepareContext(
            List<User> users,
            Map<Long, Long> photoCountByUser,
            Map<Long, Map<String, Boolean>> verifyByUser,
            Map<Long, FellowshipProfileMain> mainByUser,
            Map<Long, FellowshipProfile> legacyByUser,
            Map<Long, UserProfile> userProfileByUser
    ) {
        List<Long> userIds = users.stream().map(User::getUserid).filter(id -> id != null).toList();
        Map<Long, UserGrowth> growthByUser = userIds.isEmpty()
                ? Map.of()
                : growthUserGrowthRepository.findByUserIdIn(userIds).stream()
                .collect(Collectors.toMap(UserGrowth::getUserId, g -> g, (a, b) -> a));

        Map<Long, Integer> badgeCountByUser = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (UserBadge ub : userBadgeRepository.findByUserIdInAndUnlocked(userIds, 1)) {
                if (ub.getUserId() == null) {
                    continue;
                }
                badgeCountByUser.merge(ub.getUserId(), 1, Integer::sum);
            }
        }

        return new InsightBatchContext(
                photoCountByUser,
                verifyByUser,
                mainByUser,
                legacyByUser,
                userProfileByUser,
                growthByUser,
                badgeCountByUser
        );
    }

    public Map<String, Object> buildInsightFields(User user, InsightBatchContext ctx) {
        if (user == null || user.getUserid() == null) {
            return Map.of();
        }
        Long userId = user.getUserid();
        Map<String, Boolean> verify = ctx.verifyByUser().getOrDefault(userId, Map.of());
        boolean photoVerified = Boolean.TRUE.equals(verify.get("photoVerified"));
        boolean realnameVerified = Boolean.TRUE.equals(verify.get("realnameVerified"));

        int photoCount = ctx.photoCountByUser().getOrDefault(userId, 0L).intValue();
        Map<String, Object> profile = buildProfileSnapshot(user, ctx, verify, photoCount);
        int growthLevel = resolveGrowthLevel(userId, ctx);
        Map<String, Object> completion = FellowshipProfileCompletion.build(user, profile, photoCount, growthLevel);

        int completionRate = toInt(completion.get("completionRate"));
        Map<String, Object> growthFields = fellowshipGrowthSummaryService.buildCardGrowthFields(userId);
        Map<String, Object> badgePayload = fellowshipUserBadgeService.buildCardBadgePayload(
                user, verify, completionRate);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cardBadges = (List<Map<String, Object>>) badgePayload.getOrDefault("badges", List.of());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> verifiedBadges = (List<Map<String, Object>>) badgePayload.getOrDefault(
                "verifiedBadges", List.of());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> missingItems = (List<Map<String, Object>>) completion.getOrDefault(
                "missingItems", List.of());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> unlockedBenefits = (List<Map<String, Object>>) completion.getOrDefault(
                "unlockedBenefits", List.of());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> nextBenefits = (List<Map<String, Object>>) completion.getOrDefault(
                "nextBenefits", List.of());

        List<Map<String, Object>> unlockedBadgeRows = loadUnlockedBadgeRows(userId);

        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("profileCompletionRate", completionRate);
        fields.put("profileMissingItems", missingItems);
        fields.put("growthLevel", growthFields.get("growthLevel"));
        fields.put("growthTitle", growthFields.get("growthTitle"));
        fields.put("badgeCount", ctx.badgeCountByUser().getOrDefault(userId, 0));
        fields.put("badges", unlockedBadgeRows.isEmpty() ? cardBadges : unlockedBadgeRows);
        fields.put("verifiedBadges", verifiedBadges);
        fields.put("exposureBoostPercent", completion.get("exposureBoostPercent"));
        fields.put("fellowshipMatchVisible", Boolean.TRUE.equals(user.getFellowshipMatchVisible()));
        fields.put("unlockedBenefits", unlockedBenefits);
        fields.put("nextBenefits", nextBenefits);
        fields.put("fellowshipPoolEligible", isInRecommendPool(user, completionRate));
        fields.put("verificationTier", resolveVerificationTier(photoVerified, realnameVerified));
        fields.put("completionBucket", resolveCompletionBucket(completionRate));
        return fields;
    }

    public Map<String, Object> buildDashboard(List<User> users, InsightBatchContext ctx) {
        Map<String, Integer> completionBuckets = new LinkedHashMap<>();
        completionBuckets.put("0_39", 0);
        completionBuckets.put("40_59", 0);
        completionBuckets.put("60_79", 0);
        completionBuckets.put("80_99", 0);
        completionBuckets.put("100", 0);

        Map<String, Integer> verificationStats = new LinkedHashMap<>();
        verificationStats.put("none", 0);
        verificationStats.put("realnameOnly", 0);
        verificationStats.put("photoOnly", 0);
        verificationStats.put("both", 0);

        Map<String, Integer> fellowshipStats = new LinkedHashMap<>();
        fellowshipStats.put("notEnabled", 0);
        fellowshipStats.put("enabled", 0);
        fellowshipStats.put("matchVisible", 0);

        Map<String, Integer> missingItemStats = new LinkedHashMap<>();
        missingItemStats.put("avatar", 0);
        missingItemStats.put("age", 0);
        missingItemStats.put("city", 0);
        missingItemStats.put("verification", 0);
        missingItemStats.put("photos", 0);
        missingItemStats.put("bio", 0);
        missingItemStats.put("fellowship", 0);

        for (User user : users) {
            Map<String, Object> insight = buildInsightFields(user, ctx);
            String bucket = String.valueOf(insight.getOrDefault("completionBucket", "0_39"));
            completionBuckets.merge(bucket, 1, Integer::sum);

            String tier = String.valueOf(insight.getOrDefault("verificationTier", "none"));
            verificationStats.merge(tier, 1, Integer::sum);

            boolean enabled = Boolean.TRUE.equals(user.getFellowshipEnabled());
            boolean matchVisible = Boolean.TRUE.equals(user.getFellowshipMatchVisible());
            boolean inPool = Boolean.TRUE.equals(insight.get("fellowshipPoolEligible"));
            if (!enabled) {
                fellowshipStats.merge("notEnabled", 1, Integer::sum);
            } else if (inPool) {
                fellowshipStats.merge("matchVisible", 1, Integer::sum);
            } else {
                fellowshipStats.merge("enabled", 1, Integer::sum);
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> missing = (List<Map<String, Object>>) insight.getOrDefault(
                    "profileMissingItems", List.of());
            for (String key : missing.stream()
                    .map(m -> String.valueOf(m.getOrDefault("key", "")))
                    .filter(k -> !k.isBlank())
                    .toList()) {
                String statKey = "fellowship".equals(key) ? "fellowshipEnabled" : key;
                if (missingItemStats.containsKey(statKey)) {
                    missingItemStats.merge(statKey, 1, Integer::sum);
                }
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("completionBuckets", completionBuckets);
        result.put("verificationStats", verificationStats);
        result.put("fellowshipStats", fellowshipStats);
        result.put("missingItemStats", missingItemStats);
        result.put("totalUsers", users.size());
        return result;
    }

    public boolean matchesSegment(
            User user,
            Map<String, Object> insight,
            LocalDateTime lastLoginAt,
            String segment
    ) {
        if (segment == null || segment.isBlank() || user == null || insight == null) {
            return false;
        }
        if ("DISABLED".equalsIgnoreCase(user.getUserStatus())) {
            return false;
        }
        String normalized = segment.trim().toUpperCase();
        return switch (normalized) {
            case "LOW_COMPLETION" -> "0_39".equals(String.valueOf(insight.getOrDefault("completionBucket", "")));
            case "MEDIUM_COMPLETION" -> {
                String bucket = String.valueOf(insight.getOrDefault("completionBucket", ""));
                yield "40_59".equals(bucket) || "60_79".equals(bucket);
            }
            case "NEARLY_COMPLETE" -> "80_99".equals(String.valueOf(insight.getOrDefault("completionBucket", "")));
            case "UNVERIFIED" -> "none".equals(String.valueOf(insight.getOrDefault("verificationTier", "none")));
            case "MISSING_CITY" -> hasMissingItem(insight, "city");
            case "MISSING_AVATAR" -> hasMissingItem(insight, "avatar");
            case "MISSING_PHOTOS" -> hasMissingItem(insight, "photos");
            case "MISSING_BIO" -> hasMissingItem(insight, "bio");
            case "NOT_ENABLE_FELLOWSHIP" -> !Boolean.TRUE.equals(user.getFellowshipEnabled());
            case "LOW_ACTIVITY" -> !isRecentLogin(lastLoginAt);
            default -> false;
        };
    }

    public boolean matchesFilters(
            User user,
            Map<String, Object> insight,
            LocalDateTime lastLoginAt,
            AdminUserListFilters filters
    ) {
        if (filters == null || filters.isEmpty()) {
            return true;
        }
        if (filters.completionBucket() != null && !filters.completionBucket().isBlank()) {
            String bucket = String.valueOf(insight.getOrDefault("completionBucket", ""));
            if (!filters.completionBucket().equals(bucket)) {
                return false;
            }
        }
        if (filters.verificationFilter() != null && !filters.verificationFilter().isBlank()) {
            String tier = String.valueOf(insight.getOrDefault("verificationTier", "none"));
            if (!filters.verificationFilter().equals(tier)) {
                return false;
            }
        }
        if (filters.fellowshipFilter() != null && !filters.fellowshipFilter().isBlank()) {
            boolean enabled = Boolean.TRUE.equals(user.getFellowshipEnabled());
            boolean inPool = Boolean.TRUE.equals(insight.get("fellowshipPoolEligible"));
            switch (filters.fellowshipFilter()) {
                case "notEnabled" -> {
                    if (enabled) return false;
                }
                case "enabled" -> {
                    if (!enabled || inPool) return false;
                }
                case "matchVisible" -> {
                    if (!inPool) return false;
                }
                default -> {
                    return false;
                }
            }
        }
        if (Boolean.TRUE.equals(filters.lowActive())) {
            if (isRecentLogin(lastLoginAt)) {
                return false;
            }
        }
        if (filters.missingItem() != null && !filters.missingItem().isBlank()) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> missing = (List<Map<String, Object>>) insight.getOrDefault(
                    "profileMissingItems", List.of());
            boolean hasMissing = missing.stream()
                    .anyMatch(m -> filters.missingItem().equals(String.valueOf(m.get("key"))));
            if (!hasMissing) {
                return false;
            }
        }
        return true;
    }

    private List<Map<String, Object>> loadUnlockedBadgeRows(Long userId) {
        return userBadgeRepository.findByUserIdOrderByCreatedAtAsc(userId).stream()
                .filter(ub -> ub.getUnlocked() != null && ub.getUnlocked() == 1)
                .map(ub -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("code", ub.getBadgeCode());
                    row.put("unlockedAt", ub.getUnlockedAt());
                    return row;
                })
                .toList();
    }

    private Map<String, Object> buildProfileSnapshot(
            User user,
            InsightBatchContext ctx,
            Map<String, Boolean> verify,
            int photoCount
    ) {
        FellowshipProfileMain main = ctx.mainByUser().get(user.getUserid());
        FellowshipProfile legacy = ctx.legacyByUser().get(user.getUserid());
        UserProfile userProfile = ctx.userProfileByUser().get(user.getUserid());

        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("avatarUrl", firstNonBlank(
                user.getProfilePhoto(),
                main == null ? null : main.getAvatar(),
                legacy == null ? null : legacy.getAvatarUrl()));
        profile.put("city", firstNonBlank(
                user.getLocation(),
                main == null ? null : main.getCity(),
                legacy == null ? null : legacy.getCity(),
                userProfile == null ? null : userProfile.getCity()));
        profile.put("bio", firstNonBlank(user.getBio(), main == null ? null : main.getBio()));
        profile.put("photoVerified", verify.get("photoVerified"));
        profile.put("realnameVerified", verify.get("realnameVerified"));
        profile.put("photoCount", photoCount);

        if (main != null && main.getBirthday() != null) {
            profile.put("birthYear", main.getBirthday().getYear());
        } else if (legacy != null && legacy.getBirthYear() != null) {
            profile.put("birthYear", legacy.getBirthYear());
        } else if (user.getAge() > 0) {
            profile.put("age", user.getAge());
        }
        return profile;
    }

    private int resolveGrowthLevel(Long userId, InsightBatchContext ctx) {
        UserGrowth growth = ctx.growthByUser().get(userId);
        if (growth == null || growth.getLevel() == null || growth.getLevel() <= 0) {
            return 1;
        }
        return growth.getLevel();
    }

    private static boolean isInRecommendPool(User user, int completionRate) {
        return Boolean.TRUE.equals(user.getFellowshipEnabled())
                && Boolean.TRUE.equals(user.getFellowshipMatchVisible())
                && completionRate >= 40;
    }

    private static String resolveVerificationTier(boolean photoVerified, boolean realnameVerified) {
        if (photoVerified && realnameVerified) {
            return "both";
        }
        if (realnameVerified) {
            return "realnameOnly";
        }
        if (photoVerified) {
            return "photoOnly";
        }
        return "none";
    }

    private static String resolveCompletionBucket(int rate) {
        if (rate >= 100) {
            return "100";
        }
        if (rate >= 80) {
            return "80_99";
        }
        if (rate >= 60) {
            return "60_79";
        }
        if (rate >= 40) {
            return "40_59";
        }
        return "0_39";
    }

    private static boolean hasMissingItem(Map<String, Object> insight, String key) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> missing = (List<Map<String, Object>>) insight.getOrDefault(
                "profileMissingItems", List.of());
        return missing.stream()
                .anyMatch(m -> key.equals(String.valueOf(m.getOrDefault("key", ""))));
    }

    private static boolean isRecentLogin(LocalDateTime lastLoginAt) {
        if (lastLoginAt == null) {
            return false;
        }
        return lastLoginAt.isAfter(LocalDateTime.now().minusDays(7));
    }

    private static int toInt(Object value) {
        if (value instanceof Number n) {
            return n.intValue();
        }
        return 0;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    public record InsightBatchContext(
            Map<Long, Long> photoCountByUser,
            Map<Long, Map<String, Boolean>> verifyByUser,
            Map<Long, FellowshipProfileMain> mainByUser,
            Map<Long, FellowshipProfile> legacyByUser,
            Map<Long, UserProfile> userProfileByUser,
            Map<Long, UserGrowth> growthByUser,
            Map<Long, Integer> badgeCountByUser
    ) {
    }

    public record AdminUserListFilters(
            String completionBucket,
            String verificationFilter,
            String fellowshipFilter,
            Boolean lowActive,
            String missingItem
    ) {
        public boolean isEmpty() {
            return (completionBucket == null || completionBucket.isBlank())
                    && (verificationFilter == null || verificationFilter.isBlank())
                    && (fellowshipFilter == null || fellowshipFilter.isBlank())
                    && !Boolean.TRUE.equals(lowActive)
                    && (missingItem == null || missingItem.isBlank());
        }
    }
}
