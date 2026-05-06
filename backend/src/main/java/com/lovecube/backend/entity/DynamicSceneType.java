package com.lovecube.backend.entity;

public enum DynamicSceneType {
    PLATFORM,
    FELLOWSHIP;

    public static DynamicSceneType fromNullable(String raw) {
        if (raw == null || raw.isBlank()) {
            return FELLOWSHIP;
        }
        try {
            return DynamicSceneType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return FELLOWSHIP;
        }
    }
}
