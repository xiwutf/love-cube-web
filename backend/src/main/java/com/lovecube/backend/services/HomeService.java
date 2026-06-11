package com.lovecube.backend.services;

import com.lovecube.backend.dto.UserFilterDTO;
import com.lovecube.backend.entity.Banner;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.BannerRepository;
import com.lovecube.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
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
    private final FellowshipCardEnrichmentService fellowshipCardEnrichmentService;

    public HomeService(
            BannerRepository bannerRepository,
            UserRepository userRepository,
            UnifiedProfileService unifiedProfileService,
            VerificationService verificationService,
            FellowshipCardEnrichmentService fellowshipCardEnrichmentService
    ) {
        this.bannerRepository = bannerRepository;
        this.userRepository = userRepository;
        this.unifiedProfileService = unifiedProfileService;
        this.verificationService = verificationService;
        this.fellowshipCardEnrichmentService = fellowshipCardEnrichmentService;
    }

    public List<Banner> getBanners() {
        return bannerRepository.findByIsActiveTrueOrderBySortAsc();
    }

    public List<Map<String, Object>> getRecommends() {
        List<User> pool = new ArrayList<>(userRepository.findVisibleFellowshipUserPool(50));
        List<Long> userIds = pool.stream().map(User::getUserid).collect(Collectors.toList());
        Map<Long, Map<String, Boolean>> verifyMap = verificationService.getBatchSummary(userIds);
        return pool.stream()
                .sorted(Comparator.comparingDouble((User u) -> unifiedProfileService.computeRecommendRankForUser(
                        u, verifyMap.getOrDefault(u.getUserid(), Map.of()))).reversed())
                .limit(5)
                .map(this::convertUserToCard)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getNewcomers() {
        return userRepository.findNewcomersVisibleFellowshipUsers(5).stream()
                .map(this::convertUserToCard)
                .collect(Collectors.toList());
    }

    /**
     * 首页一次性初始化接口：banners + recommends + newcomers + profile + completion 合并为单次 HTTP 往返。
     * profile/completion 仅在 authHeader 合法时返回，token 无效时静默忽略（不影响公开内容加载）。
     */
    public Map<String, Object> getHomeInit(String authHeader) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("banners", getBanners());
        result.put("recommends", getRecommends());
        result.put("newcomers", getNewcomers());

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                User user = unifiedProfileService.requireCurrentUser(authHeader);
                Map<String, Object> profile = unifiedProfileService.buildFellowshipPayload(user);
                result.put("profile", profile);
                result.put("completion", unifiedProfileService.buildFellowshipCompletion(user));
            } catch (Exception ignored) {
                // 未登录或 token 失效 — 公开内容仍正常返回
            }
        }
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

    /**
     * 首页列表卡片：仅读取 users 表已有字段，不触发任何额外查询。
     * 首页卡片只展示头像/昵称/年龄/地区，无需调用 buildUnifiedProfile。
     */
    private Map<String, Object> convertUserToCard(User user) {
        Map<String, Object> card = new LinkedHashMap<>();
        card.put("userId", user.getUserid());
        card.put("userid", user.getUserid());
        card.put("nickname", user.getUsername());
        card.put("username", user.getUsername());
        card.put("profilePhoto", user.getProfilePhoto());
        card.put("avatarUrl", user.getProfilePhoto());
        card.put("age", user.getAge());
        card.put("location", user.getLocation());
        Map<String, Boolean> verify = verificationService.getBatchSummary(List.of(user.getUserid()))
                .getOrDefault(user.getUserid(), Map.of());
        card.put("photoVerified", Boolean.TRUE.equals(verify.get("photoVerified")));
        card.put("realnameVerified", Boolean.TRUE.equals(verify.get("realnameVerified")));
        int completionRate = unifiedProfileService.computeFellowshipCompletionRateForUser(user);
        fellowshipCardEnrichmentService.enrichUserCard(card, user, verify, completionRate);
        return card;
    }
}
