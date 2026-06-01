package com.lovecube.backend.services;

import com.lovecube.backend.entity.NewcomerTask;
import com.lovecube.backend.entity.UserNewcomerTaskProgress;
import com.lovecube.backend.entity.UserWeeklyTaskProgress;
import com.lovecube.backend.entity.WeeklyTask;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.NewcomerTaskRepository;
import com.lovecube.backend.repository.PlatGroupMemberRepository;
import com.lovecube.backend.repository.UserGrowthLogRepository;
import com.lovecube.backend.repository.UserNewcomerTaskProgressRepository;
import com.lovecube.backend.repository.UserRepository;
import com.lovecube.backend.repository.UserWeeklyTaskProgressRepository;
import com.lovecube.backend.repository.WeeklyTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExtendedTaskService {

    private final WeeklyTaskRepository weeklyTaskRepository;
    private final UserWeeklyTaskProgressRepository weeklyProgressRepository;
    private final NewcomerTaskRepository newcomerTaskRepository;
    private final UserNewcomerTaskProgressRepository newcomerProgressRepository;
    private final UserRepository userRepository;
    private final UserGrowthLogRepository growthLogRepository;
    private final PlatGroupMemberRepository platGroupMemberRepository;
    private final GrowthService growthService;

    public ExtendedTaskService(
            WeeklyTaskRepository weeklyTaskRepository,
            UserWeeklyTaskProgressRepository weeklyProgressRepository,
            NewcomerTaskRepository newcomerTaskRepository,
            UserNewcomerTaskProgressRepository newcomerProgressRepository,
            UserRepository userRepository,
            UserGrowthLogRepository growthLogRepository,
            PlatGroupMemberRepository platGroupMemberRepository,
            GrowthService growthService
    ) {
        this.weeklyTaskRepository = weeklyTaskRepository;
        this.weeklyProgressRepository = weeklyProgressRepository;
        this.newcomerTaskRepository = newcomerTaskRepository;
        this.newcomerProgressRepository = newcomerProgressRepository;
        this.userRepository = userRepository;
        this.growthLogRepository = growthLogRepository;
        this.platGroupMemberRepository = platGroupMemberRepository;
        this.growthService = growthService;
    }

    public void onActionRecorded(Long userId, String actionType) {
        updateWeeklyProgress(userId, actionType);
        syncNewcomerCompletion(userId);
    }

    public List<Map<String, Object>> buildWeeklyTaskRows(Long userId) {
        LocalDate weekStart = resolveWeekStart(LocalDate.now());
        Map<String, UserWeeklyTaskProgress> progressMap = weeklyProgressRepository
                .findByUserIdAndWeekStart(userId, weekStart)
                .stream()
                .collect(java.util.stream.Collectors.toMap(UserWeeklyTaskProgress::getTaskCode, p -> p));

        List<Map<String, Object>> rows = new ArrayList<>();
        for (WeeklyTask task : weeklyTaskRepository.findByEnabledOrderBySortNoAsc(1)) {
            UserWeeklyTaskProgress p = progressMap.get(task.getCode());
            int current = p == null ? 0 : safe(p.getProgress());
            int target = Math.max(1, safe(task.getTargetCount()));
            boolean completed = p != null && safe(p.getCompleted()) == 1;
            boolean claimed = p != null && safe(p.getClaimed()) == 1;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("code", task.getCode());
            row.put("name", task.getName());
            row.put("actionType", task.getActionType());
            row.put("targetCount", target);
            row.put("progress", Math.min(current, target));
            row.put("completed", completed);
            row.put("claimed", claimed);
            row.put("rewardExp", safe(task.getRewardExp()));
            row.put("weekStart", weekStart.toString());
            rows.add(row);
        }
        return rows;
    }

    @Transactional
    public Map<String, Object> claimWeeklyTask(Long userId, String taskCode) {
        WeeklyTask task = weeklyTaskRepository.findByCode(taskCode)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在"));
        LocalDate weekStart = resolveWeekStart(LocalDate.now());
        UserWeeklyTaskProgress progress = weeklyProgressRepository
                .findByUserIdAndTaskCodeAndWeekStart(userId, taskCode, weekStart)
                .orElseThrow(() -> new IllegalArgumentException("任务尚未完成"));
        if (safe(progress.getCompleted()) != 1) {
            throw new IllegalArgumentException("任务尚未完成");
        }
        if (safe(progress.getClaimed()) == 1) {
            return Map.of("claimed", false, "message", "奖励已领取");
        }
        progress.setClaimed(1);
        weeklyProgressRepository.save(progress);
        int reward = Math.max(0, safe(task.getRewardExp()));
        Map<String, Object> bonus = growthService.grantBonusExp(userId, reward, "WEEKLY_TASK", taskCode + "_" + weekStart);
        return Map.of(
                "claimed", true,
                "taskCode", taskCode,
                "rewardExp", reward,
                "granted", bonus.get("granted")
        );
    }

    public Map<String, Object> buildNewcomerPack(Long userId) {
        syncNewcomerCompletion(userId);
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getCreatedAt() == null) {
            return Map.of("eligible", false, "tasks", List.of());
        }
        long daysSinceReg = ChronoUnit.DAYS.between(user.getCreatedAt().toLocalDate(), LocalDate.now()) + 1;
        if (daysSinceReg > 7) {
            return Map.of("eligible", false, "expired", true, "tasks", List.of());
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (NewcomerTask task : newcomerTaskRepository.findByEnabledOrderBySortNoAsc(1)) {
            UserNewcomerTaskProgress p = newcomerProgressRepository
                    .findByUserIdAndTaskCode(userId, task.getCode()).orElse(null);
            boolean unlocked = daysSinceReg >= safe(task.getUnlockDay());
            boolean completed = p != null && safe(p.getCompleted()) == 1;
            boolean claimed = p != null && safe(p.getClaimed()) == 1;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("code", task.getCode());
            row.put("name", task.getName());
            row.put("unlockDay", task.getUnlockDay());
            row.put("unlocked", unlocked);
            row.put("completed", completed);
            row.put("claimed", claimed);
            row.put("rewardExp", safe(task.getRewardExp()));
            row.put("currentDay", (int) daysSinceReg);
            rows.add(row);
        }
        Map<String, Object> pack = new LinkedHashMap<>();
        pack.put("eligible", true);
        pack.put("currentDay", (int) daysSinceReg);
        pack.put("tasks", rows);
        return pack;
    }

    @Transactional
    public Map<String, Object> claimNewcomerTask(Long userId, String taskCode) {
        NewcomerTask task = newcomerTaskRepository.findByCode(taskCode)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在"));
        UserNewcomerTaskProgress progress = newcomerProgressRepository
                .findByUserIdAndTaskCode(userId, taskCode)
                .orElseThrow(() -> new IllegalArgumentException("任务尚未完成"));
        if (safe(progress.getCompleted()) != 1) {
            throw new IllegalArgumentException("任务尚未完成");
        }
        if (safe(progress.getClaimed()) == 1) {
            return Map.of("claimed", false, "message", "奖励已领取");
        }
        progress.setClaimed(1);
        newcomerProgressRepository.save(progress);
        int reward = Math.max(0, safe(task.getRewardExp()));
        growthService.grantBonusExp(userId, reward, "NEWCOMER", taskCode);
        return Map.of("claimed", true, "taskCode", taskCode, "rewardExp", reward);
    }

    @Transactional
    public void syncNewcomerCompletion(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getCreatedAt() == null) return;
        long daysSinceReg = ChronoUnit.DAYS.between(user.getCreatedAt().toLocalDate(), LocalDate.now()) + 1;
        if (daysSinceReg > 7) return;

        for (NewcomerTask task : newcomerTaskRepository.findByEnabledOrderBySortNoAsc(1)) {
            if (daysSinceReg < safe(task.getUnlockDay())) continue;
            UserNewcomerTaskProgress existing = newcomerProgressRepository
                    .findByUserIdAndTaskCode(userId, task.getCode()).orElse(null);
            if (existing != null && safe(existing.getClaimed()) == 1) continue;
            if (!evaluateNewcomerCheck(task, userId)) continue;
            UserNewcomerTaskProgress row = existing != null ? existing : new UserNewcomerTaskProgress();
            if (existing == null) {
                row.setUserId(userId);
                row.setTaskCode(task.getCode());
                row.setClaimed(0);
            }
            row.setCompleted(1);
            newcomerProgressRepository.save(row);
        }
    }

    private void updateWeeklyProgress(Long userId, String actionType) {
        LocalDate weekStart = resolveWeekStart(LocalDate.now());
        List<WeeklyTask> tasks = weeklyTaskRepository.findByEnabledAndActionType(1, actionType);
        for (WeeklyTask task : tasks) {
            UserWeeklyTaskProgress progress = weeklyProgressRepository
                    .findByUserIdAndTaskCodeAndWeekStart(userId, task.getCode(), weekStart)
                    .orElseGet(() -> {
                        UserWeeklyTaskProgress init = new UserWeeklyTaskProgress();
                        init.setUserId(userId);
                        init.setTaskCode(task.getCode());
                        init.setWeekStart(weekStart);
                        init.setProgress(0);
                        init.setCompleted(0);
                        init.setClaimed(0);
                        return init;
                    });
            if (safe(progress.getClaimed()) == 1) continue;
            int current = safe(progress.getProgress());
            int target = Math.max(1, safe(task.getTargetCount()));
            if (current < target) {
                progress.setProgress(current + 1);
            }
            if (safe(progress.getProgress()) >= target) {
                progress.setCompleted(1);
            }
            weeklyProgressRepository.save(progress);
        }
    }

    private boolean evaluateNewcomerCheck(NewcomerTask task, Long userId) {
        int need = Math.max(1, safe(task.getCheckValue()));
        return switch (String.valueOf(task.getCheckType())) {
            case "LOGIN_COUNT" -> growthLogRepository.countByUserIdAndActionType(userId, "LOGIN") >= need;
            case "POST_COUNT" -> growthLogRepository.countByUserIdAndActionType(userId, "POST_CONTENT") >= need;
            case "LIKE_COUNT" -> growthLogRepository.countByUserIdAndActionType(userId, "LIKE_CONTENT") >= need;
            case "COMMENT_COUNT" -> growthLogRepository.countByUserIdAndActionType(userId, "COMMENT_CONTENT") >= need;
            case "VIEW_COUNT" -> growthLogRepository.countByUserIdAndActionType(userId, "VIEW_CONTENT") >= need;
            case "IN_APPROVED_PLATFORM_GROUP" -> platGroupMemberRepository.existsByUserIdAndStatus(userId, "approved");
            default -> false;
        };
    }

    static LocalDate resolveWeekStart(LocalDate date) {
        return date.with(java.time.DayOfWeek.MONDAY);
    }

    private int safe(Integer v) {
        return v == null ? 0 : v;
    }
}
