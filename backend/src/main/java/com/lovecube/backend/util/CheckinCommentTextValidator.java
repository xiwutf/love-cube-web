package com.lovecube.backend.util;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 打卡评论基础校验（长度、控制字符、常见注入片段）。无独立敏感词库时做最小防护。
 */
public final class CheckinCommentTextValidator {

    private static final Pattern CTRL = Pattern.compile("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]");
    private static final Pattern HTML_SCRIPT = Pattern.compile("(?i)<\\s*script|</\\s*script\\s*>");
    private static final Pattern JS_URL = Pattern.compile("(?i)javascript\\s*:");
    private static final Pattern ON_ATTR = Pattern.compile("(?i)\\son\\w+\\s*=");

    private CheckinCommentTextValidator() {
    }

    /**
     * @return 错误文案，null 表示通过
     */
    public static String validate(String raw) {
        if (raw == null) {
            return "评论内容不能为空";
        }
        String content = raw.trim();
        if (content.isEmpty()) {
            return "评论内容不能为空";
        }
        if (content.length() > 200) {
            return "评论最多 200 字";
        }
        if (CTRL.matcher(content).find()) {
            return "评论包含非法字符";
        }
        if (HTML_SCRIPT.matcher(content).find() || JS_URL.matcher(content).find() || ON_ATTR.matcher(content).find()) {
            return "评论包含不允许的内容";
        }
        String lower = content.toLowerCase(Locale.ROOT);
        if (lower.contains("http://") || lower.contains("https://") || lower.contains("www.")) {
            return "评论暂不支持链接";
        }
        return null;
    }
}
