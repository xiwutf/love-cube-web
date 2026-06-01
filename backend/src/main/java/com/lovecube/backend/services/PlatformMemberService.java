package com.lovecube.backend.services;

import com.lovecube.backend.models.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class PlatformMemberService {

    public static final int MONTHLY_PRICE = 19;

    public boolean isActiveMember(User user) {
        return user != null
                && user.getPlatformMemberExpiresAt() != null
                && user.getPlatformMemberExpiresAt().isAfter(LocalDateTime.now());
    }

    public Map<String, Object> buildStatus(User user) {
        Map<String, Object> body = new LinkedHashMap<>();
        boolean active = isActiveMember(user);
        body.put("memberActive", active);
        body.put("expiresAt", user != null && user.getPlatformMemberExpiresAt() != null
                ? user.getPlatformMemberExpiresAt().toString() : null);
        body.put("monthlyPrice", MONTHLY_PRICE);
        body.put("perks", java.util.List.of(
                "平台内容优先推荐",
                "本地服务发布优先审核",
                "话题广场专属标识",
                "消息中心高级筛选"
        ));
        return body;
    }

    public void activateMonthly(User user) {
        LocalDateTime base = (user.getPlatformMemberExpiresAt() != null
                && user.getPlatformMemberExpiresAt().isAfter(LocalDateTime.now()))
                ? user.getPlatformMemberExpiresAt()
                : LocalDateTime.now();
        user.setPlatformMemberExpiresAt(base.plusMonths(1));
    }
}
