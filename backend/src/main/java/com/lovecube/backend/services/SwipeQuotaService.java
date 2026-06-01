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
                    return created;
                });
        if (row.getSwipeCount() >= VipService.FREE_DAILY_SWIPE_LIMIT) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "今日滑卡次数已用完，开通 VIP 可无限滑卡");
        }
        row.setSwipeCount(row.getSwipeCount() + 1);
        swipeRepository.save(row);
    }

    private int getTodayCount(Long userId) {
        return swipeRepository.findByUserIdAndSwipeDate(userId, LocalDate.now())
                .map(UserDailySwipe::getSwipeCount)
                .orElse(0);
    }
}
