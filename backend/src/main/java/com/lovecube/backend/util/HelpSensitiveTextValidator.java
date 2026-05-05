package com.lovecube.backend.util;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 互助内容基础校验 + 本地敏感词片段扫描（无外部审核服务时的最小接入）。
 */
public final class HelpSensitiveTextValidator {

    private static final Pattern CTRL = Pattern.compile("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]");
    private static final Pattern HTML_SCRIPT = Pattern.compile("(?i)<\\s*script|</\\s*script\\s*>");
    private static final Pattern JS_URL = Pattern.compile("(?i)javascript\\s*:");
    private static final Pattern ON_ATTR = Pattern.compile("(?i)\\son\\w+\\s*=");

    /** 可后续改为配置表；此处为站内 UGC 常见违规营销/欺诈类词根 */
    private static final List<String> BANNED_SUBSTRINGS = List.of(
            "色情", "裸聊", "赌博", "博彩", "时时彩", "发票", "代开发票",
            "办证", "刻章", "枪支", "炸药", "刷单", "套现", "网贷催收"
    );

    private HelpSensitiveTextValidator() {
    }

    public static void validateBody(String label, String raw, int maxLen) {
        String err = validatePlain(label, raw, maxLen, true);
        if (err != null) {
            throw new IllegalArgumentException(err);
        }
        if (containsBanned(raw)) {
            throw new IllegalArgumentException("内容未通过敏感词校验，请修改后重试");
        }
    }

    public static void validateOptional(String label, String raw, int maxLen) {
        if (raw == null || raw.isBlank()) {
            return;
        }
        validateBody(label, raw, maxLen);
    }

    /** 上架前复核（标题+正文合并扫描） */
    public static void assertNoBannedCombined(String... segments) {
        StringBuilder sb = new StringBuilder();
        for (String s : segments) {
            if (s != null) {
                sb.append(s).append('\n');
            }
        }
        if (containsBanned(sb.toString())) {
            throw new IllegalArgumentException("内容未通过敏感词校验，无法上架");
        }
    }

    private static String validatePlain(String label, String raw, int maxLen, boolean required) {
        if (raw == null) {
            return required ? label + "不能为空" : null;
        }
        String content = raw.trim();
        if (required && content.isEmpty()) {
            return label + "不能为空";
        }
        if (!required && content.isEmpty()) {
            return null;
        }
        if (content.length() > maxLen) {
            return label + "不能超过 " + maxLen + " 字";
        }
        if (CTRL.matcher(content).find()) {
            return label + "包含非法字符";
        }
        if (HTML_SCRIPT.matcher(content).find() || JS_URL.matcher(content).find() || ON_ATTR.matcher(content).find()) {
            return label + "包含不允许的内容";
        }
        return null;
    }

    private static boolean containsBanned(String raw) {
        if (raw == null || raw.isBlank()) {
            return false;
        }
        String normalized = raw.toLowerCase(Locale.ROOT);
        for (String w : BANNED_SUBSTRINGS) {
            if (normalized.contains(w.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}
