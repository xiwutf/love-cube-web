package com.lovecube.backend.services;

import com.lovecube.backend.models.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class VipService {

    public static final int FREE_DAILY_SWIPE_LIMIT = 30;
    /** 非 VIP 每日可撤回跳过次数 */
    public static final int FREE_DAILY_REWIND_LIMIT = 1;
    /** VIP 每日可撤回跳过次数 */
    public static final int VIP_DAILY_REWIND_LIMIT = 5;

    public boolean isActiveVip(User user) {
        if (user == null || user.getVipExpiresAt() == null) {
            return false;
        }
        return user.getVipExpiresAt().isAfter(LocalDateTime.now());
    }

    public java.util.Map<String, Object> buildVipStatus(User user) {
        boolean active = isActiveVip(user);
        java.util.Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("vipActive", active);
        body.put("vipStatus", user != null ? user.getVipStatus() : null);
        body.put("expiresAt", user != null && user.getVipExpiresAt() != null
                ? user.getVipExpiresAt().toString() : null);
        body.put("dailySwipeLimit", active ? -1 : FREE_DAILY_SWIPE_LIMIT);
        body.put("dailyRewindLimit", active ? VIP_DAILY_REWIND_LIMIT : FREE_DAILY_REWIND_LIMIT);
        return body;
    }
}
