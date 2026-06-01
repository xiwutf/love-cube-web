package com.lovecube.backend.services;

import com.lovecube.backend.entity.UserLoginStreak;
import com.lovecube.backend.repository.UserLoginStreakRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class LoginStreakService {

    private static final int[] STREAK_MILESTONES = {3, 7, 14, 30};
    private static final Map<Integer, Integer> STREAK_BONUS_EXP = Map.of(
            3, 10,
            7, 25,
            14, 50,
            30, 100
    );

    private final UserLoginStreakRepository streakRepository;
    private final GrowthService growthService;

    public LoginStreakService(UserLoginStreakRepository streakRepository, GrowthService growthService) {
        this.streakRepository = streakRepository;
        this.growthService = growthService;
    }

    @Transactional
    public Map<String, Object> recordLogin(Long userId, LocalDate today) {
        UserLoginStreak streak = streakRepository.findById(userId).orElseGet(() -> {
            UserLoginStreak s = new UserLoginStreak();
            s.setUserId(userId);
            s.setCurrentStreak(0);
            s.setLongestStreak(0);
            return s;
        });

        boolean isNewDay = streak.getLastLoginDate() == null || !today.equals(streak.getLastLoginDate());
        if (!isNewDay) {
            return toResult(streak, false, null);
        }

        if (streak.getLastLoginDate() != null && today.minusDays(1).equals(streak.getLastLoginDate())) {
            streak.setCurrentStreak(streak.getCurrentStreak() + 1);
        } else {
            streak.setCurrentStreak(1);
        }
        streak.setLastLoginDate(today);
        if (streak.getCurrentStreak() > streak.getLongestStreak()) {
            streak.setLongestStreak(streak.getCurrentStreak());
        }
        streakRepository.save(streak);

        Integer bonusExp = grantStreakBonusIfNeeded(userId, streak.getCurrentStreak());
        return toResult(streak, true, bonusExp);
    }

    public Map<String, Object> getStreakOverview(Long userId) {
        UserLoginStreak streak = streakRepository.findById(userId).orElse(null);
        if (streak == null) {
            return Map.of(
                    "currentStreak", 0,
                    "longestStreak", 0,
                    "nextMilestone", 3,
                    "daysToNextMilestone", 3
            );
        }
        int current = safe(streak.getCurrentStreak());
        int next = nextMilestone(current);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("currentStreak", current);
        result.put("longestStreak", safe(streak.getLongestStreak()));
        result.put("nextMilestone", next);
        result.put("daysToNextMilestone", next > current ? next - current : 0);
        result.put("nextMilestoneBonus", STREAK_BONUS_EXP.getOrDefault(next, 0));
        return result;
    }

    private Integer grantStreakBonusIfNeeded(Long userId, int currentStreak) {
        for (int milestone : STREAK_MILESTONES) {
            if (currentStreak != milestone) continue;
            Integer bonus = STREAK_BONUS_EXP.get(milestone);
            if (bonus == null || bonus <= 0) continue;
            String bizId = "STREAK_BONUS_" + milestone;
            Map<String, Object> res = growthService.grantBonusExp(userId, bonus, "STREAK", bizId);
            if (Boolean.TRUE.equals(res.get("granted"))) {
                return bonus;
            }
        }
        return null;
    }

    private int nextMilestone(int current) {
        for (int m : STREAK_MILESTONES) {
            if (current < m) return m;
        }
        return STREAK_MILESTONES[STREAK_MILESTONES.length - 1];
    }

    private Map<String, Object> toResult(UserLoginStreak streak, boolean updated, Integer bonusExp) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("currentStreak", safe(streak.getCurrentStreak()));
        result.put("longestStreak", safe(streak.getLongestStreak()));
        result.put("updated", updated);
        if (bonusExp != null) {
            result.put("streakBonusExp", bonusExp);
        }
        return result;
    }

    private int safe(Integer v) {
        return v == null ? 0 : v;
    }
}
