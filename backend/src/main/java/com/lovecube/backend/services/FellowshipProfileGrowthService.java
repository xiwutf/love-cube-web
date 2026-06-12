package com.lovecube.backend.services;

import com.lovecube.backend.growth.dto.GrowthEventCreateRequest;
import com.lovecube.backend.growth.enums.GrowthEventType;
import com.lovecube.backend.growth.enums.SourcePlatform;
import com.lovecube.backend.growth.service.GrowthEventService;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 联谊资料里程碑 → 成长事件（幂等 dedupeKey）+ 徽章刷新。
 */
@Service
public class FellowshipProfileGrowthService {
    private final UnifiedProfileService unifiedProfileService;
    private final GrowthEventService growthEventService;
    private final GrowthService growthService;
    private final VerificationService verificationService;
    private final UserRepository userRepository;

    public FellowshipProfileGrowthService(
            UnifiedProfileService unifiedProfileService,
            GrowthEventService growthEventService,
            GrowthService growthService,
            VerificationService verificationService,
            UserRepository userRepository
    ) {
        this.unifiedProfileService = unifiedProfileService;
        this.growthEventService = growthEventService;
        this.growthService = growthService;
        this.verificationService = verificationService;
        this.userRepository = userRepository;
    }

    /** 资料保存、照片变更、认证通过、开通联谊后调用。 */
    public void syncProfileMilestones(User user) {
        syncProfileMilestonesWithStats(user);
    }

    /** 同步资料里程碑并返回本次新增统计（dedupe 命中不计入 created）。 */
    public ProfileSyncStats syncProfileMilestonesWithStats(User user) {
        if (user == null || user.getUserid() == null) {
            return ProfileSyncStats.empty();
        }
        User activeUser = userRepository.findById(user.getUserid()).orElse(user);
        Long userId = activeUser.getUserid();
        Map<String, Object> profile = unifiedProfileService.buildFellowshipPayload(activeUser);
        int photoCount = unifiedProfileService.getUserPhotos(userId).size();
        Map<String, Object> completion = unifiedProfileService.buildFellowshipCompletion(activeUser);
        int completionRate = completion.get("completionRate") instanceof Number n ? n.intValue() : 0;

        Map<String, Boolean> verify = verificationService.getBatchSummary(java.util.List.of(userId))
                .getOrDefault(userId, Map.of());
        boolean photoVerified = Boolean.TRUE.equals(verify.get("photoVerified"));
        boolean realnameVerified = Boolean.TRUE.equals(verify.get("realnameVerified"));
        boolean anyVerified = photoVerified || realnameVerified;

        int growthEventsCreated = 0;

        if (isNotBlank(str(profile.get("avatarUrl")))) {
            growthEventsCreated += publish(userId, GrowthEventType.PROFILE_AVATAR_SET, "growth:profile_avatar_set:user:" + userId);
        }
        Integer userAge = activeUser.getAge();
        if (profile.get("birthYear") != null
                || (profile.get("age") instanceof Number ageN && ageN.intValue() > 0)
                || (userAge != null && userAge > 0)) {
            growthEventsCreated += publish(userId, GrowthEventType.PROFILE_AGE_SET, "growth:profile_age_set:user:" + userId);
        }
        if (isNotBlank(str(profile.get("city"))) || isNotBlank(activeUser.getLocation())) {
            growthEventsCreated += publish(userId, GrowthEventType.PROFILE_CITY_SET, "growth:profile_city_set:user:" + userId);
        }
        if (isNotBlank(str(profile.get("bio"))) || isNotBlank(activeUser.getBio())) {
            growthEventsCreated += publish(userId, GrowthEventType.PROFILE_BIO_SET, "growth:profile_bio_set:user:" + userId);
        }
        if (photoCount >= 3) {
            growthEventsCreated += publish(userId, GrowthEventType.PROFILE_PHOTOS_UPLOADED, "growth:profile_photos_uploaded:user:" + userId);
        }
        if (anyVerified) {
            growthEventsCreated += publish(userId, GrowthEventType.PROFILE_VERIFIED, "growth:profile_verified:user:" + userId);
        }
        if (Boolean.TRUE.equals(activeUser.getFellowshipEnabled())) {
            growthEventsCreated += publish(userId, GrowthEventType.FELLOWSHIP_ENABLED, "growth:fellowship_enabled:user:" + userId);
        }
        if (completionRate >= 100) {
            growthEventsCreated += publish(userId, GrowthEventType.USER_PROFILE_COMPLETED, "growth:user_profile_completed:user:" + userId);
        }

        int badgesUnlocked = growthService.refreshUserBadges(userId);
        return new ProfileSyncStats(growthEventsCreated, badgesUnlocked);
    }

    public void syncAfterRegistration(Long userId) {
        if (userId == null) {
            return;
        }
        growthService.refreshUserBadges(userId);
    }

    private int publish(Long userId, GrowthEventType eventType, String dedupeKey) {
        try {
            GrowthEventCreateRequest request = new GrowthEventCreateRequest();
            request.setEventType(eventType);
            request.setActorUserId(userId);
            request.setTargetUserId(userId);
            request.setBizRefType("fellowship_profile");
            request.setBizRefId(String.valueOf(userId));
            request.setDedupeKey(dedupeKey);
            request.setRuleVersion("v1");
            request.setSourcePlatform(SourcePlatform.API);
            request.setOccurredAt(LocalDateTime.now());
            Map<String, Object> result = growthEventService.publish(request);
            return Boolean.TRUE.equals(result.get("duplicated")) ? 0 : 1;
        } catch (Exception ignored) {
            // Growth event must not block business update.
            return 0;
        }
    }

    public record ProfileSyncStats(int growthEventsCreated, int badgesUnlocked) {
        public static ProfileSyncStats empty() {
            return new ProfileSyncStats(0, 0);
        }

        public boolean isSkipped() {
            return growthEventsCreated == 0 && badgesUnlocked == 0;
        }
    }

    private static String str(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
