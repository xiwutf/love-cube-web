package com.lovecube.backend.services;

import com.lovecube.backend.entity.PlatGroup;
import com.lovecube.backend.repository.PlatGroupRepository;

import java.util.Locale;
import java.util.Random;

public final class PlatformGroupInviteSupport {

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 8;

    private PlatformGroupInviteSupport() {
    }

    public static String normalizeInviteCode(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim().toUpperCase(Locale.ROOT);
    }

    public static String generateUniqueCode(PlatGroupRepository repository) {
        Random random = new Random();
        for (int attempt = 0; attempt < 30; attempt++) {
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
            }
            String code = sb.toString();
            if (!repository.existsByInviteCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("无法生成唯一邀请码，请稍后重试");
    }

    public static String ensureInviteCode(PlatGroup group, PlatGroupRepository repository) {
        if (group == null || !"invite".equals(group.getJoinMode())) {
            return null;
        }
        String existing = normalizeInviteCode(group.getInviteCode());
        if (!existing.isEmpty()) {
            return existing;
        }
        String code = generateUniqueCode(repository);
        group.setInviteCode(code);
        return code;
    }

    public static boolean matchesInviteCode(PlatGroup group, String submitted) {
        if (group == null || !"invite".equals(group.getJoinMode())) {
            return false;
        }
        String expected = normalizeInviteCode(group.getInviteCode());
        String actual = normalizeInviteCode(submitted);
        return !expected.isEmpty() && expected.equals(actual);
    }
}
