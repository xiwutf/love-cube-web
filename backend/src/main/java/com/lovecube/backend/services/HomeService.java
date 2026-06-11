package com.lovecube.backend.services;

import com.lovecube.backend.dto.UserFilterDTO;
import com.lovecube.backend.entity.Banner;
import com.lovecube.backend.entity.UserBadge;
import com.lovecube.backend.growth.entity.UserGrowth;
import com.lovecube.backend.growth.repository.GrowthUserGrowthRepository;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.BannerRepository;
import com.lovecube.backend.repository.UserBadgeRepository;
import com.lovecube.backend.repository.UserPhotoRepository;
import com.lovecube.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HomeService {
    private final BannerRepository bannerRepository;
    private final UserRepository userRepository;
    private final UnifiedProfileService unifiedProfileService;
    private final VerificationService verificationService;
    private final UserPhotoRepository userPhotoRepository;
    private final GrowthUserGrowthRepository growthUserGrowthRepository;
    private final FellowshipGrowthSummaryService fellowshipGrowthSummaryService;
    private final FellowshipUserBadgeService fellowshipUserBadgeService;
    private final UserBadgeRepository userBadgeRepository;
    private final MatchService matchService;

    public HomeService(
            BannerRepository bannerRepository,
            UserRepository userRepository,
            UnifiedProfileService unifiedProfileService,
            VerificationService verificationService,
            UserPhotoRepository userPhotoRepository,
            GrowthUserGrowthRepository growthUserGrowthRepository,
            FellowshipGrowthSummaryService fellowshipGrowthSummaryService,
            FellowshipUserBadgeService fellowshipUserBadgeService,
            UserBadgeRepository userBadgeRepository,
            MatchService matchService
    ) {
        this.bannerRepository = bannerRepository;
        this.userRepository = userRepository;
        this.unifiedProfileService = unifiedProfileService;
        this.verificationService = verificationService;
        this.userPhotoRepository = userPhotoRepository;
        this.growthUserGrowthRepository = growthUserGrowthRepository;
        this.fellowshipGrowthSummaryService = fellowshipGrowthSummaryService;
        this.fellowshipUserBadgeService = fellowshipUserBadgeService;
        this.userBadgeRepository = userBadgeRepository;
        this.matchService = matchService;
    }

    public List<Banner> getBanners() {
        return bannerRepository.findByIsActiveTrueOrderBySortAsc();
    }

    public List<Map<String, Object>> getRecommends() {
        return getRecommends(null);
    }

    public List<Map<String, Object>> getRecommends(Long viewerUserId) {
        List<User> pool = resolveHomeUserPool(viewerUserId, 30);
        HomeCardContext ctx = prepareHomeCardContext(pool);
        return pool.stream()
                .sorted(Comparator.comparingDouble((User u) -> recommendRank(u, ctx)).reversed())
                .limit(5)
                .map(user -> convertUserToCard(user, ctx))
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getNewcomers() {
        return getNewcomers(null);
    }

    public List<Map<String, Object>> getNewcomers(Long viewerUserId) {
        List<User> pool = new ArrayList<>(resolveHomeUserPool(viewerUserId, 30));
        pool.sort(Comparator
                .comparing(User::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(User::getUserid, Comparator.nullsLast(Comparator.reverseOrder())));
        List<User> top = pool.stream().limit(5).collect(Collectors.toList());
        HomeCardContext ctx = prepareHomeCardContext(top);
        return top.stream()
                .map(user -> convertUserToCard(user, ctx))
                .collect(Collectors.toList());
    }

    /**
     * 首页用户池：优先走匹配列表（异性 + 可见 + 排除自己），为空时回退到广义联谊可见用户。
     */
    private List<User> resolveHomeUserPool(Long viewerUserId, int limit) {
        int fetchSize = Math.max(limit, 20);
        if (viewerUserId != null) {
            MatchService.MatchListPageResult pageResult = matchService.getAllUsersPageWithContext(
                    viewerUserId,
                    null,
                    null,
                    null,
                    null,
                    true,
                    false,
                    0,
                    fetchSize);
            List<User> matched = pageResult.page().getContent();
            if (!matched.isEmpty()) {
                return matched;
            }
        }

        return userRepository.findVisibleFellowshipUserPool(Math.max(fetchSize, 50)).stream()
                .filter(user -> viewerUserId == null || !viewerUserId.equals(user.getUserid()))
                .limit(fetchSize)
                .collect(Collectors.toList());
    }

    /**
     * 首页一次性初始化接口：banners + recommends + newcomers + profile + completion 合并为单次 HTTP 往返。
     * profile/completion 仅在 authHeader 合法时返回，token 无效时静默忽略（不影响公开内容加载）。
     */
    public Map<String, Object> getHomeInit(String authHeader) {
        Map<String, Object> result = new LinkedHashMap<>();
        Long viewerUserId = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                User user = unifiedProfileService.requireCurrentUser(authHeader);
                viewerUserId = user.getUserid();
                Map<String, Object> profile = unifiedProfileService.buildFellowshipPayload(user);
                result.put("profile", profile);
                result.put("completion", unifiedProfileService.buildFellowshipCompletion(user));
            } catch (Exception ignored) {
                // 未登录或 token 失效 — 公开内容仍正常返回
            }
        }
        result.put("banners", getBanners());
        result.put("recommends", getRecommends(viewerUserId));
        result.put("newcomers", getNewcomers(viewerUserId));
        return result;
    }

    public List<User> searchUsers(String keyword, int page, int size) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return userRepository.findNewcomersVisibleFellowshipUsers(size);
        }
        return userRepository.searchByKeyword(keyword.trim(), page * size, size);
    }

    public List<User> filterUsers(UserFilterDTO filterDTO) {
        return userRepository.findByAgeBetweenAndGenderAndLocation(
                filterDTO.getAgeRange().get(0),
                filterDTO.getAgeRange().get(1),
                "男".equals(filterDTO.getGender()) ? 1 : 2,
                filterDTO.getRegion()
        );
    }

    private HomeCardContext prepareHomeCardContext(List<User> users) {
        List<Long> userIds = users.stream()
                .map(User::getUserid)
                .filter(id -> id != null)
                .collect(Collectors.toList());
        Map<Long, Map<String, Boolean>> verifyByUser = userIds.isEmpty()
                ? Map.of()
                : verificationService.getBatchSummary(userIds);

        Map<Long, Long> photoCountByUser = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (Object[] row : userPhotoRepository.countGroupedByUserIds(userIds)) {
                if (row == null || row.length < 2) {
                    continue;
                }
                Long uid = row[0] instanceof Number ? ((Number) row[0]).longValue() : null;
                Long count = row[1] instanceof Number ? ((Number) row[1]).longValue() : 0L;
                if (uid != null) {
                    photoCountByUser.put(uid, count);
                }
            }
        }

        Map<Long, Integer> growthLevelByUser = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (UserGrowth growth : growthUserGrowthRepository.findByUserIdIn(userIds)) {
                if (growth.getUserId() == null) {
                    continue;
                }
                int level = growth.getLevel() == null || growth.getLevel() <= 0 ? 1 : growth.getLevel();
                growthLevelByUser.put(growth.getUserId(), level);
            }
        }

        Map<Long, Map<String, UserBadge>> unlockedBadgesByUser = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (UserBadge badge : userBadgeRepository.findByUserIdInAndUnlocked(userIds, 1)) {
                if (badge.getUserId() == null || badge.getBadgeCode() == null) {
                    continue;
                }
                unlockedBadgesByUser
                        .computeIfAbsent(badge.getUserId(), id -> new LinkedHashMap<>())
                        .putIfAbsent(badge.getBadgeCode(), badge);
            }
        }

        return new HomeCardContext(verifyByUser, photoCountByUser, growthLevelByUser, unlockedBadgesByUser);
    }

    private double recommendRank(User user, HomeCardContext ctx) {
        Long userId = user.getUserid();
        Map<String, Boolean> verify = ctx.verifyByUser().getOrDefault(userId, Map.of());
        int photoCount = ctx.photoCountByUser().getOrDefault(userId, 0L).intValue();
        int growthLevel = ctx.growthLevelByUser().getOrDefault(userId, 1);
        return unifiedProfileService.computeRecommendRankLightweight(user, verify, photoCount, growthLevel);
    }

    private int completionRate(User user, HomeCardContext ctx) {
        Long userId = user.getUserid();
        Map<String, Boolean> verify = ctx.verifyByUser().getOrDefault(userId, Map.of());
        int photoCount = ctx.photoCountByUser().getOrDefault(userId, 0L).intValue();
        int growthLevel = ctx.growthLevelByUser().getOrDefault(userId, 1);
        return unifiedProfileService.computeFellowshipCompletionRateLightweight(user, verify, photoCount, growthLevel);
    }

    private Map<String, Object> convertUserToCard(User user, HomeCardContext ctx) {
        Long userId = user.getUserid();
        Map<String, Boolean> verify = ctx.verifyByUser().getOrDefault(userId, Map.of());
        int rate = completionRate(user, ctx);
        int growthLevel = ctx.growthLevelByUser().getOrDefault(userId, 1);

        Map<String, Object> card = new LinkedHashMap<>();
        card.put("userId", userId);
        card.put("userid", userId);
        card.put("nickname", user.getUsername());
        card.put("username", user.getUsername());
        card.put("profilePhoto", user.getProfilePhoto());
        card.put("avatarUrl", user.getProfilePhoto());
        card.put("age", user.getAge());
        card.put("location", user.getLocation());
        card.put("photoVerified", Boolean.TRUE.equals(verify.get("photoVerified")));
        card.put("realnameVerified", Boolean.TRUE.equals(verify.get("realnameVerified")));
        card.put("profileCompletionRate", rate);
        card.put("completionRate", rate);
        card.put("growthLevel", growthLevel);
        card.put("growthTitle", fellowshipGrowthSummaryService.resolveGrowthTitleForLevel(growthLevel));

        Map<String, UserBadge> unlocked = ctx.unlockedBadgesByUser().getOrDefault(userId, Map.of());
        Map<String, Object> badgePayload = fellowshipUserBadgeService.buildCardBadgePayloadCached(
                verify, rate, unlocked);
        card.put("verifiedBadges", badgePayload.get("verifiedBadges"));
        card.put("badges", badgePayload.get("badges"));
        return card;
    }

    private record HomeCardContext(
            Map<Long, Map<String, Boolean>> verifyByUser,
            Map<Long, Long> photoCountByUser,
            Map<Long, Integer> growthLevelByUser,
            Map<Long, Map<String, UserBadge>> unlockedBadgesByUser
    ) {
    }
}
