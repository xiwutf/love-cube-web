package com.lovecube.backend.services;

import com.lovecube.backend.models.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;

public final class GroupMemberRealNameSupport {

    public static final int MAX_LEN = 64;

    private GroupMemberRealNameSupport() {
    }

    public static String parseFromPayload(Map<String, Object> payload) {
        if (payload == null) {
            return "";
        }
        Object v = payload.containsKey("memberRealName") ? payload.get("memberRealName") : payload.get("realName");
        if (v == null) {
            return "";
        }
        return String.valueOf(v).trim();
    }

    /** 校验并返回必填团体内姓名（加入团体、创建团体时使用）。 */
    public static String requireMemberRealName(Map<String, Object> payload) {
        String s = parseFromPayload(payload);
        if (s.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "请填写真实姓名（仅在你所在的团体内向其他成员展示，不会写入公开个人主页）");
        }
        return validateLen(s);
    }

    /** 创建团体等场景：可空；若填写则校验长度。 */
    public static Optional<String> optionalMemberRealName(Map<String, Object> payload) {
        String s = parseFromPayload(payload);
        if (s.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(validateLen(s));
    }

    private static String validateLen(String s) {
        if (s.length() > MAX_LEN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "真实姓名不能超过 " + MAX_LEN + " 个字");
        }
        return s;
    }

    public static String authorDisplay(boolean revealInnerNames, User u, String memberRealName) {
        if (revealInnerNames && memberRealName != null && !memberRealName.isBlank()) {
            return memberRealName.trim();
        }
        return u != null && u.getUsername() != null ? u.getUsername() : "";
    }
}
