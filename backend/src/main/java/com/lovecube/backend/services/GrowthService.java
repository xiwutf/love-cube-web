package com.lovecube.backend.services;

import com.lovecube.backend.entity.*;
import com.lovecube.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class GrowthService {
    private static final Map<String, Integer> ACTION_EXP = Map.ofEntries(
            Map.entry("LOGIN", 2),
            Map.entry("POST_CONTENT", 10),
            Map.entry("VIEW_CONTENT", 1),
            Map.entry("LIKE_CONTENT", 2),
            Map.entry("JOIN_GROUP", 10),
            Map.entry("FEEDBACK_REPORT", 5),
            Map.entry("COMMENT_CONTENT", 3),
            Map.entry("LIKED_BY_OTHERS", 2),
            Map.entry("FIRST_POST_BONUS", 20),
            Map.entry("ALL_TASKS_COMPLETE", 20)
    );

    private static final int[] LEVEL_EXP_RULES = {0, 100, 300, 600, 1000};
    private static final String[] LEVEL_TITLES = {"新手用户", "新手创作者", "活跃用户", "内容达人", "社区骨干"};

    private final UserGrowthRepository userGrowthRepository;
    private final UserGrowthLogRepository userGrowthLogRepository;
    private final DailyTaskRepository dailyTaskRepository;
    private final UserDailyTaskProgressRepository userDailyTaskProgressRepository;
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;

    public GrowthService(
            UserGrowthRepository userGrowthRepository,
            UserGrowthLogRepository userGrowthLogRepository,
            DailyTaskRepository dailyTaskRepository,
            UserDailyTaskProgressRepository userDailyTaskProgressRepository,
            BadgeRepository badgeRepository,
            UserBadgeRepository userBadgeRepository
    ) {
        this.userGrowthRepository = userGrowthRepository;
        this.userGrowthLogRepository = userGrowthLogRepository;
        this.dailyTaskRepository = dailyTaskRepository;
        this.userDailyTaskProgressRepository = userDailyTaskProgressRepository;
        this.badgeRepository = badgeRepository;
        this.userBadgeRepository = userBadgeRepository;
    }

    public Map<String, Object> recordAction(Long userId, String actionType, String bizId) {
        String normalizedAction = normalizeActionType(actionType);
        String normalizedBizId = normalizeBizId(bizId);
        if (normalizedAction == null || normalizedBizId == null) {
            return Map.of("recorded", false, "message", "参数不合法");
        }
        int exp = ACTION_EXP.getOrDefault(normalizedAction, 0);
        if (exp <= 0) {
            return Map.of("recorded", false, "message", "不支持的成长行为");
        }
        if (userGrowthLogRepository.existsByUserIdAndActionTypeAndBizId(userId, normalizedAction, normalizedBizId)) {
            return Map.of("recorded", false, "duplicate", true, "message", "重复行为，已忽略");
        }

        UserGrowth growth = getOrCreateGrowth(userId);
        UserGrowthLog log = new UserGrowthLog();
        log.setUserId(userId);
        log.setActionType(normalizedAction);
        log.setBizId(normalizedBizId);
        log.setExp(exp);
        userGrowthLogRepository.save(log);

        growth.setExp(Math.max(0, growth.getExp()) + exp);
        refreshLevelAndTitle(growth);
        userGrowthRepository.save(growth);

        updateDailyTaskProgress(userId, normalizedAction);
        refreshBadges(userId);

        return Map.of(
                "recorded", true,
                "exp", exp,
                "level", growth.getLevel(),
                "title", growth.getTitle(),
                "totalExp", growth.getExp()
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getGrowthOverview(Long userId) {
        UserGrowth growth = getOrCreateGrowth(userId);
        List<DailyTask> tasks = dailyTaskRepository.findByEnabledOrderBySortNoAsc(1);
        LocalDate today = LocalDate.now();
        Map<String, UserDailyTaskProgress> progressMap = userDailyTaskProgressRepository
                .findByUserIdAndTaskDate(userId, today)
                .stream()
                .collect(Collectors.toMap(UserDailyTaskProgress::getTaskCode, item -> item));

        List<Map<String, Object>> taskRows = new ArrayList<>();
        for (DailyTask task : tasks) {
            UserDailyTaskProgress progress = progressMap.get(task.getCode());
            int current = progress == null ? 0 : safeInt(progress.getProgress());
            int target = Math.max(1, safeInt(task.getTargetCount()));
            boolean completed = progress != null && safeInt(progress.getCompleted()) == 1;
            boolean claimed = progress != null && safeInt(progress.getClaimed()) == 1;
            taskRows.add(Map.of(
                    "code", task.getCode(),
                    "name", task.getName(),
                    "actionType", task.getActionType(),
                    "targetCount", target,
                    "progress", Math.min(current, target),
                    "completed", completed,
                    "claimed", claimed,
                    "rewardExp", safeInt(task.getRewardExp())
            ));
        }

        List<Map<String, Object>> badgeRows = buildBadgeRows(userId);
        int nextLevelExp = resolveNextLevelExp(growth.getExp());
        int neededExp = Math.max(0, nextLevelExp - growth.getExp());

        return Map.of(
                "level", growth.getLevel(),
                "exp", growth.getExp(),
                "title", growth.getTitle(),
                "nextLevelExp", nextLevelExp,
                "neededExpToNextLevel", neededExp,
                "today", today.toString(),
                "dailyTasks", taskRows,
                "badges", badgeRows
        );
    }

    public Map<String, Object> claimDailyTask(Long userId, String taskCode) {
        DailyTask task = dailyTaskRepository.findByCode(taskCode)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在"));
        LocalDate today = LocalDate.now();
        UserDailyTaskProgress progress = userDailyTaskProgressRepository
                .findByUserIdAndTaskCodeAndTaskDate(userId, taskCode, today)
                .orElseThrow(() -> new IllegalArgumentException("任务尚未完成"));

        if (safeInt(progress.getCompleted()) != 1) {
            throw new IllegalArgumentException("任务尚未完成");
        }
        if (safeInt(progress.getClaimed()) == 1) {
            return Map.of("claimed", false, "message", "奖励已领取");
        }

        progress.setClaimed(1);
        userDailyTaskProgressRepository.save(progress);

        UserGrowth growth = getOrCreateGrowth(userId);
        int reward = Math.max(0, safeInt(task.getRewardExp()));
        growth.setExp(safeInt(growth.getExp()) + reward);
        refreshLevelAndTitle(growth);
        userGrowthRepository.save(growth);
        refreshBadges(userId);

        // 检查今日所有任务是否全部领取，若是则发放全完成奖励 (+20 EXP)
        int bonusExp = 0;
        List<DailyTask> allTasks = dailyTaskRepository.findByEnabledOrderBySortNoAsc(1);
        long claimedToday = userDailyTaskProgressRepository.findByUserIdAndTaskDate(userId, today)
                .stream().filter(p -> safeInt(p.getClaimed()) == 1).count();
        if (!allTasks.isEmpty() && claimedToday >= allTasks.size()) {
            Map<String, Object> bonus = recordAction(userId, "ALL_TASKS_COMPLETE", "ALL_DAILY_" + today);
            if (Boolean.TRUE.equals(bonus.get("recorded"))) {
                bonusExp = safeInt((Integer) bonus.getOrDefault("exp", 0));
                growth = getOrCreateGrowth(userId); // 刷新以反映奖励经验
            }
        }

        return Map.of(
                "claimed", true,
                "taskCode", taskCode,
                "rewardExp", reward,
                "bonusExp", bonusExp,
                "level", growth.getLevel(),
                "title", growth.getTitle(),
                "totalExp", growth.getExp()
        );
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMyBadges(Long userId) {
        return buildBadgeRows(userId);
    }

    private UserGrowth getOrCreateGrowth(Long userId) {
        return userGrowthRepository.findByUserId(userId).orElseGet(() -> {
            UserGrowth growth = new UserGrowth();
            growth.setUserId(userId);
            growth.setLevel(1);
            growth.setExp(0);
            growth.setTitle("新手用户");
            return userGrowthRepository.save(growth);
        });
    }

    private void updateDailyTaskProgress(Long userId, String actionType) {
        LocalDate today = LocalDate.now();
        List<DailyTask> tasks = dailyTaskRepository.findByEnabledAndActionType(1, actionType);
        for (DailyTask task : tasks) {
            UserDailyTaskProgress progress = userDailyTaskProgressRepository
                    .findByUserIdAndTaskCodeAndTaskDate(userId, task.getCode(), today)
                    .orElseGet(() -> {
                        UserDailyTaskProgress init = new UserDailyTaskProgress();
                        init.setUserId(userId);
                        init.setTaskCode(task.getCode());
                        init.setTaskDate(today);
                        init.setProgress(0);
                        init.setCompleted(0);
                        init.setClaimed(0);
                        return init;
                    });
            int current = safeInt(progress.getProgress());
            int target = Math.max(1, safeInt(task.getTargetCount()));
            if (current < target) {
                progress.setProgress(current + 1);
            }
            if (safeInt(progress.getProgress()) >= target) {
                progress.setCompleted(1);
            }
            userDailyTaskProgressRepository.save(progress);
        }
    }

    private void refreshBadges(Long userId) {
        List<Badge> badges = badgeRepository.findAll();
        long loginCount = userGrowthLogRepository.countByUserIdAndActionType(userId, "LOGIN");
        long postCount = userGrowthLogRepository.countByUserIdAndActionType(userId, "POST_CONTENT");
        long joinGroupCount = userGrowthLogRepository.countByUserIdAndActionType(userId, "JOIN_GROUP");
        long completedTaskCount = userDailyTaskProgressRepository.countByUserIdAndCompleted(userId, 1);

        for (Badge badge : badges) {
            int progressValue = switch (badge.getCode()) {
                case "FIRST_LOGIN" -> (int) loginCount;
                case "FIRST_POST" -> (int) postCount;
                case "JOIN_GROUP" -> (int) joinGroupCount;
                case "DAILY_CHECK_3" -> (int) completedTaskCount;
                default -> 0;
            };
            int target = Math.max(1, safeInt(badge.getConditionValue()));
            UserBadge userBadge = userBadgeRepository.findByUserIdAndBadgeCode(userId, badge.getCode())
                    .orElseGet(() -> {
                        UserBadge init = new UserBadge();
                        init.setUserId(userId);
                        init.setBadgeCode(badge.getCode());
                        init.setProgress(0);
                        init.setUnlocked(0);
                        return init;
                    });
            userBadge.setProgress(Math.max(userBadge.getProgress(), progressValue));
            if (safeInt(userBadge.getUnlocked()) == 0 && progressValue >= target) {
                userBadge.setUnlocked(1);
                userBadge.setUnlockedAt(LocalDateTime.now());
            }
            userBadgeRepository.save(userBadge);
        }
    }

    private List<Map<String, Object>> buildBadgeRows(Long userId) {
        List<Badge> allBadges = badgeRepository.findAll();
        Map<String, UserBadge> userBadgeMap = userBadgeRepository.findByUserIdOrderByCreatedAtAsc(userId)
                .stream()
                .collect(Collectors.toMap(UserBadge::getBadgeCode, item -> item));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Badge badge : allBadges) {
            UserBadge userBadge = userBadgeMap.get(badge.getCode());
            int progress = userBadge == null ? 0 : safeInt(userBadge.getProgress());
            int target = Math.max(1, safeInt(badge.getConditionValue()));
            boolean unlocked = userBadge != null && safeInt(userBadge.getUnlocked()) == 1;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("code", badge.getCode());
            item.put("name", badge.getName());
            item.put("description", badge.getDescription() == null ? "" : badge.getDescription());
            item.put("icon", badge.getIcon() == null ? "" : badge.getIcon());
            item.put("badgeType", badge.getBadgeType() == null ? "" : badge.getBadgeType());
            item.put("conditionValue", target);
            item.put("progress", Math.min(progress, target));
            item.put("unlocked", unlocked);
            item.put("unlockedAt", userBadge == null ? null : userBadge.getUnlockedAt());
            rows.add(item);
        }
        return rows;
    }

    private void refreshLevelAndTitle(UserGrowth growth) {
        int exp = safeInt(growth.getExp());
        int level = 1;
        for (int i = LEVEL_EXP_RULES.length - 1; i >= 0; i--) {
            if (exp >= LEVEL_EXP_RULES[i]) {
                level = i + 1;
                break;
            }
        }
        growth.setLevel(level);
        growth.setTitle(LEVEL_TITLES[level - 1]);
    }

    private int resolveNextLevelExp(int exp) {
        for (int levelExpRule : LEVEL_EXP_RULES) {
            if (exp < levelExpRule) {
                return levelExpRule;
            }
        }
        return LEVEL_EXP_RULES[LEVEL_EXP_RULES.length - 1];
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String normalizeActionType(String actionType) {
        if (actionType == null) {
            return null;
        }
        String value = actionType.trim().toUpperCase(Locale.ROOT);
        return ACTION_EXP.containsKey(value) ? value : null;
    }

    private String normalizeBizId(String bizId) {
        if (bizId == null) {
            return null;
        }
        String value = bizId.trim();
        if (value.isEmpty() || value.length() > 100) {
            return null;
        }
        return value;
    }
}
