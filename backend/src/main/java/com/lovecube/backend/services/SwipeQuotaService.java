package com.lovecube.backend.services;

import com.lovecube.backend.entity.UserDailySwipe;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.UserDailySwipeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class SwipeQuotaService {

    private final UserDailySwipeRepository swipeRepository;
    private final VipService vipService;

    public SwipeQuotaService(UserDailySwipeRepository swipeRepository, VipService vipService) {
        this.swipeRepository = swipeRepository;
        this.vipService = vipService;
    }

    public Map<String, Object> getStatus(User user) {
        Map<String, Object> body = new LinkedHashMap<>();
        boolean vipActive = vipService.isActiveVip(user);
        body.put("vipActive", vipActive);
        int rewindLimit = vipActive ? VipService.VIP_DAILY_REWIND_LIMIT : VipService.FREE_DAILY_REWIND_LIMIT;
        int rewindUsed = getTodayRewindCount(user.getUserid());
        body.put("rewindLimit", rewindLimit);
        body.put("rewindUsed", rewindUsed);
        body.put("rewindRemaining", Math.max(0, rewindLimit - rewindUsed));
        if (vipActive) {
            body.put("unlimited", true);
            body.put("used", 0);
            body.put("limit", -1);
            body.put("remaining", -1);
            return body;
        }
        int used = getTodayCount(user.getUserid());
        int limit = VipService.FREE_DAILY_SWIPE_LIMIT;
        body.put("unlimited", false);
        body.put("used", used);
        body.put("limit", limit);
        body.put("remaining", Math.max(0, limit - used));
        return body;
    }

    @Transactional
    public void consumeSwipe(User user) {
        if (vipService.isActiveVip(user)) {
            return;
        }
        Long userId = user.getUserid();
        LocalDate today = LocalDate.now();
        UserDailySwipe row = swipeRepository.findByUserIdAndSwipeDate(userId, today)
                .orElseGet(() -> {
                    UserDailySwipe created = new UserDailySwipe();
                    created.setUserId(userId);
                    created.setSwipeDate(today);
                    created.setSwipeCount(0);
                    created.setRewindCount(0);
                    return created;
                });
        if (row.getSwipeCount() >= VipService.FREE_DAILY_SWIPE_LIMIT) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "今日滑卡次数已用完，开通 VIP 可无限滑卡");
        }
        row.setSwipeCount(row.getSwipeCount() + 1);
        swipeRepository.save(row);
    }

    @Transactional
    public void consumeRewind(User user) {
        if (user == null || user.getUserid() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户认证失败");
        }
        boolean vipActive = vipService.isActiveVip(user);
        int limit = vipActive ? VipService.VIP_DAILY_REWIND_LIMIT : VipService.FREE_DAILY_REWIND_LIMIT;
        UserDailySwipe row = getOrCreateTodayRow(user.getUserid());
        int used = row.getRewindCount() != null ? row.getRewindCount() : 0;
        if (used >= limit) {
            String hint = vipActive
                    ? "今日撤回次数已用完"
                    : "今日撤回次数已用完，开通 VIP 可每日撤回 " + VipService.VIP_DAILY_REWIND_LIMIT + " 次";
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, hint);
        }
        row.setRewindCount(used + 1);
        swipeRepository.save(row);
    }

    private UserDailySwipe getOrCreateTodayRow(Long userId) {
        LocalDate today = LocalDate.now();
        return swipeRepository.findByUserIdAndSwipeDate(userId, today)
                .orElseGet(() -> {
                    UserDailySwipe created = new UserDailySwipe();
                    created.setUserId(userId);
                    created.setSwipeDate(today);
                    created.setSwipeCount(0);
                    created.setRewindCount(0);
                    return created;
                });
    }

    private int getTodayRewindCount(Long userId) {
        return swipeRepository.findByUserIdAndSwipeDate(userId, LocalDate.now())
                .map(r -> r.getRewindCount() != null ? r.getRewindCount() : 0)
                .orElse(0);
    }

    private int getTodayCount(Long userId) {
        return swipeRepository.findByUserIdAndSwipeDate(userId, LocalDate.now())
                .map(UserDailySwipe::getSwipeCount)
                .orElse(0);
    }
}
