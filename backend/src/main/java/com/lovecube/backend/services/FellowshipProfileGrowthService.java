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
        if (user == null || user.getUserid() == null) {
            return;
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

        if (isNotBlank(str(profile.get("avatarUrl")))) {
            publish(userId, GrowthEventType.PROFILE_AVATAR_SET, "growth:profile_avatar_set:user:" + userId);
        }
        if (profile.get("birthYear") != null
                || (profile.get("age") instanceof Number ageN && ageN.intValue() > 0)
                || activeUser.getAge() > 0) {
            publish(userId, GrowthEventType.PROFILE_AGE_SET, "growth:profile_age_set:user:" + userId);
        }
        if (isNotBlank(str(profile.get("city"))) || isNotBlank(activeUser.getLocation())) {
            publish(userId, GrowthEventType.PROFILE_CITY_SET, "growth:profile_city_set:user:" + userId);
        }
        if (isNotBlank(str(profile.get("bio"))) || isNotBlank(activeUser.getBio())) {
            publish(userId, GrowthEventType.PROFILE_BIO_SET, "growth:profile_bio_set:user:" + userId);
        }
        if (photoCount >= 3) {
            publish(userId, GrowthEventType.PROFILE_PHOTOS_UPLOADED, "growth:profile_photos_uploaded:user:" + userId);
        }
        if (anyVerified) {
            publish(userId, GrowthEventType.PROFILE_VERIFIED, "growth:profile_verified:user:" + userId);
        }
        if (Boolean.TRUE.equals(activeUser.getFellowshipEnabled())) {
            publish(userId, GrowthEventType.FELLOWSHIP_ENABLED, "growth:fellowship_enabled:user:" + userId);
        }
        if (completionRate >= 100) {
            publish(userId, GrowthEventType.USER_PROFILE_COMPLETED, "growth:user_profile_completed:user:" + userId);
        }

        growthService.refreshUserBadges(userId);
    }

    public void syncAfterRegistration(Long userId) {
        if (userId == null) {
            return;
        }
        growthService.refreshUserBadges(userId);
    }

    private void publish(Long userId, GrowthEventType eventType, String dedupeKey) {
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
            growthEventService.publish(request);
        } catch (Exception ignored) {
            // Growth event must not block business update.
        }
    }

    private static String str(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
