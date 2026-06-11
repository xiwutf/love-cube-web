package com.lovecube.backend.services;

import com.lovecube.backend.entity.Badge;
import com.lovecube.backend.entity.UserBadge;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.BadgeRepository;
import com.lovecube.backend.repository.UserBadgeRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 联谊用户卡片徽章展示：认证强标签 + 普通徽章，最多 3 个。
 */
@Service
public class FellowshipUserBadgeService {
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private volatile Map<String, Badge> badgeCatalogCache;

    public FellowshipUserBadgeService(BadgeRepository badgeRepository, UserBadgeRepository userBadgeRepository) {
        this.badgeRepository = badgeRepository;
        this.userBadgeRepository = userBadgeRepository;
    }

    public Map<String, Object> buildCardBadgePayload(
            User user,
            Map<String, Boolean> verifyBadges,
            int profileCompletionRate
    ) {
        Map<String, UserBadge> unlockedMap = userBadgeRepository.findByUserIdOrderByCreatedAtAsc(user.getUserid())
                .stream()
                .filter(ub -> ub.getUnlocked() != null && ub.getUnlocked() == 1)
                .collect(Collectors.toMap(UserBadge::getBadgeCode, ub -> ub, (a, b) -> a));
        return buildCardBadgePayloadCached(verifyBadges, profileCompletionRate, unlockedMap);
    }

    public Map<String, Object> buildCardBadgePayloadCached(
            Map<String, Boolean> verifyBadges,
            int profileCompletionRate,
            Map<String, UserBadge> unlockedMap
    ) {
        boolean photoVerified = verifyBadges != null && Boolean.TRUE.equals(verifyBadges.get("photoVerified"));
        boolean realnameVerified = verifyBadges != null && Boolean.TRUE.equals(verifyBadges.get("realnameVerified"));
        Map<String, Badge> badgeCatalog = badgeCatalog();
        Map<String, UserBadge> unlocked = unlockedMap != null ? unlockedMap : Map.of();

        List<Map<String, Object>> verifiedBadges = new ArrayList<>();
        if (realnameVerified) {
            verifiedBadges.add(identityBadge("FELLOW_REALNAME", "实名认证", badgeCatalog));
        }
        if (photoVerified) {
            verifiedBadges.add(identityBadge("FELLOW_PHOTO_VERIFY", "真人认证", badgeCatalog));
        }

        List<Candidate> candidates = new ArrayList<>();
        addIfUnlocked(candidates, unlocked, badgeCatalog, "FELLOW_PROFILE_MASTER", 80);
        addIfUnlocked(candidates, unlocked, badgeCatalog, "FELLOW_PHOTO_MASTER", 70);
        addIfUnlocked(candidates, unlocked, badgeCatalog, "FELLOW_TRUST", 65);
        addIfUnlocked(candidates, unlocked, badgeCatalog, "FELLOW_CITY", 50);
        addIfUnlocked(candidates, unlocked, badgeCatalog, "FELLOW_JOIN", 45);
        addIfUnlocked(candidates, unlocked, badgeCatalog, "FELLOW_NEWCOMER", 40);

        candidates.sort(Comparator.comparingInt(Candidate::priority).reversed());

        List<Map<String, Object>> badges = new ArrayList<>();
        int remaining = Math.max(0, 3 - verifiedBadges.size());
        for (int i = 0; i < Math.min(remaining, candidates.size()); i++) {
            badges.add(candidates.get(i).row());
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("verifiedBadges", verifiedBadges);
        result.put("badges", badges);
        result.put("profileCompletionRate", profileCompletionRate);
        return result;
    }

    public List<Map<String, Object>> buildRecentlyUnlockedBadges(Long userId, int limit) {
        Map<String, Badge> badgeCatalog = badgeCatalog();
        return userBadgeRepository.findByUserIdOrderByCreatedAtAsc(userId).stream()
                .filter(ub -> ub.getUnlocked() != null && ub.getUnlocked() == 1 && ub.getUnlockedAt() != null)
                .sorted(Comparator.comparing(UserBadge::getUnlockedAt).reversed())
                .limit(Math.max(1, limit))
                .map(ub -> {
                    Badge badge = badgeCatalog.get(ub.getBadgeCode());
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("code", ub.getBadgeCode());
                    row.put("name", badge == null ? ub.getBadgeCode() : badge.getName());
                    row.put("description", badge == null ? "" : badge.getDescription());
                    row.put("icon", badge == null ? "" : badge.getIcon());
                    row.put("unlockedAt", ub.getUnlockedAt());
                    return row;
                })
                .toList();
    }

    private Map<String, Badge> badgeCatalog() {
        Map<String, Badge> cache = badgeCatalogCache;
        if (cache != null) {
            return cache;
        }
        synchronized (this) {
            cache = badgeCatalogCache;
            if (cache == null) {
                cache = badgeRepository.findAll().stream()
                        .collect(Collectors.toMap(Badge::getCode, b -> b, (a, b) -> a));
                badgeCatalogCache = cache;
            }
            return cache;
        }
    }

    private static void addIfUnlocked(
            List<Candidate> candidates,
            Map<String, UserBadge> unlockedMap,
            Map<String, Badge> catalog,
            String code,
            int priority
    ) {
        if (!unlockedMap.containsKey(code)) {
            return;
        }
        Badge badge = catalog.get(code);
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("code", code);
        row.put("name", badge == null ? code : badge.getName());
        row.put("description", badge == null ? "" : badge.getDescription());
        row.put("icon", badge == null ? "" : badge.getIcon());
        row.put("displayType", "normal");
        candidates.add(new Candidate(priority, row));
    }

    private static Map<String, Object> identityBadge(String code, String fallbackName, Map<String, Badge> catalog) {
        Badge badge = catalog.get(code);
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("code", code);
        row.put("name", badge == null ? fallbackName : badge.getName());
        row.put("description", badge == null ? "" : badge.getDescription());
        row.put("icon", badge == null ? "" : badge.getIcon());
        row.put("displayType", "identity");
        return row;
    }

    private record Candidate(int priority, Map<String, Object> row) {
    }
}
