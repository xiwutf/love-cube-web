package com.lovecube.backend.growth;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum GrowthCampaignSegment {
    LOW_COMPLETION,
    MEDIUM_COMPLETION,
    NEARLY_COMPLETE,
    UNVERIFIED,
    MISSING_CITY,
    MISSING_AVATAR,
    MISSING_PHOTOS,
    MISSING_BIO,
    NOT_ENABLE_FELLOWSHIP,
    LOW_ACTIVITY;

    public static Optional<GrowthCampaignSegment> fromCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(s -> s.name().equals(normalized))
                .findFirst();
    }

    public String suggestedTemplateCode() {
        return switch (this) {
            case UNVERIFIED -> GrowthCampaignTemplate.VERIFY_NOW.getCode();
            case LOW_COMPLETION, MEDIUM_COMPLETION -> GrowthCampaignTemplate.COMPLETE_PROFILE.getCode();
            case NEARLY_COMPLETE -> GrowthCampaignTemplate.NEARLY_COMPLETE.getCode();
            case NOT_ENABLE_FELLOWSHIP -> GrowthCampaignTemplate.ENABLE_FELLOWSHIP.getCode();
            case MISSING_PHOTOS -> GrowthCampaignTemplate.UPLOAD_PHOTOS.getCode();
            case MISSING_CITY -> GrowthCampaignTemplate.CITY_REMINDER.getCode();
            case MISSING_AVATAR, MISSING_BIO, LOW_ACTIVITY -> GrowthCampaignTemplate.COMPLETE_PROFILE.getCode();
        };
    }
}
