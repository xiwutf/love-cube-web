package com.lovecube.backend.services;

import com.lovecube.backend.entity.*;
import com.lovecube.backend.models.User;
import com.lovecube.backend.repository.*;
import org.springframework.context.annotation.Lazy;
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
            Map.entry("ALL_TASKS_COMPLETE", 20),
            // 团体模块任务奖励
            Map.entry("GROUP_CHECKIN", 2),
            Map.entry("GROUP_POST_TASK", 5),
            Map.entry("GROUP_COMMENT_TASK", 3),
            Map.entry("GROUP_LIKE_TASK", 1)
    );

    private static final int[] LEVEL_EXP_RULES = {0, 100, 300, 600, 1000};

    /**
     * 平台成长称号（按等级段，与 EXP 档位独立，便于后续扩展等级上限）。
     */
    public static String getUserTitle(int level) {
        int lv = level <= 0 ? 1 : level;
        if (lv <= 4) return "新手成员";
        if (lv <= 9) return "积极参与者";
        if (lv <= 19) return "坚持行动者";
        if (lv <= 29) return "团体活跃者";
        if (lv <= 49) return "自律达人";
        return "团体领航者";
    }

    private final UserGrowthRepository userGrowthRepository;
    private final UserGrowthLogRepository userGrowthLogRepository;
    private final DailyTaskRepository dailyTaskRepository;
    private final UserDailyTaskProgressRepository userDailyTaskProgressRepository;
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final AccountTaskRepository accountTaskRepository;
    private final UserAccountTaskProgressRepository userAccountTaskProgressRepository;
    private final UserRepository userRepository;
    private final UserPhotoRepository userPhotoRepository;
    private final PlatGroupMemberRepository platGroupMemberRepository;
    private final ExtendedTaskService extendedTaskService;
    private final VerificationService verificationService;

    public GrowthService(
            UserGrowthRepository userGrowthRepository,
            UserGrowthLogRepository userGrowthLogRepository,
            DailyTaskRepository dailyTaskRepository,
            UserDailyTaskProgressRepository userDailyTaskProgressRepository,
            BadgeRepository badgeRepository,
            UserBadgeRepository userBadgeRepository,
            AccountTaskRepository accountTaskRepository,
            UserAccountTaskProgressRepository userAccountTaskProgressRepository,
            UserRepository userRepository,
            UserPhotoRepository userPhotoRepository,
            PlatGroupMemberRepository platGroupMemberRepository,
            @Lazy ExtendedTaskService extendedTaskService,
            VerificationService verificationService
    ) {
        this.userGrowthRepository = userGrowthRepository;
        this.userGrowthLogRepository = userGrowthLogRepository;
        this.dailyTaskRepository = dailyTaskRepository;
        this.userDailyTaskProgressRepository = userDailyTaskProgressRepository;
        this.badgeRepository = badgeRepository;
        this.userBadgeRepository = userBadgeRepository;
        this.accountTaskRepository = accountTaskRepository;
        this.userAccountTaskProgressRepository = userAccountTaskProgressRepository;
        this.userRepository = userRepository;
        this.userPhotoRepository = userPhotoRepository;
        this.platGroupMemberRepository = platGroupMemberRepository;
        this.extendedTaskService = extendedTaskService;
        this.verificationService = verificationService;
    }

    /** 联谊资料里程碑后刷新徽章（含平台 + 联谊徽章）。返回本次新解锁数量。 */
    public int refreshUserBadges(Long userId) {
        return refreshBadges(userId);
    }

    /** 发放一次性奖励经验（bizId 去重，用于连续签到等里程碑）。 */
    @Transactional
    public Map<String, Object> grantBonusExp(Long userId, int exp, String actionType, String bizId) {
        if (exp <= 0 || actionType == null || bizId == null) {
            return Map.of("granted", false, "message", "参数不合法");
        }
        String normalizedAction = actionType.trim().toUpperCase(Locale.ROOT);
        String normalizedBizId = bizId.trim();
        if (userGrowthLogRepository.existsByUserIdAndActionTypeAndBizId(userId, normalizedAction, normalizedBizId)) {
            return Map.of("granted", false, "duplicate", true);
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
        refreshBadges(userId);
        return Map.of("granted", true, "exp", exp, "level", growth.getLevel());
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
        extendedTaskService.onActionRecorded(userId, normalizedAction);

        return Map.of(
                "recorded", true,
                "exp", exp,
                "level", growth.getLevel(),
                "title", getUserTitle(safeInt(growth.getLevel())),
                "totalExp", growth.getExp()
        );
    }

    @Transactional
    public Map<String, Object> getGrowthOverview(Long userId) {
        syncAccountTaskCompletion(userId);
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

        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("level", growth.getLevel());
        overview.put("exp", growth.getExp());
        overview.put("title", getUserTitle(safeInt(growth.getLevel())));
        overview.put("nextLevelExp", nextLevelExp);
        overview.put("neededExpToNextLevel", neededExp);
        overview.put("today", today.toString());
        overview.put("dailyTasks", taskRows);
        overview.put("accountTasks", buildAccountTaskRows(userId));
        overview.put("badges", badgeRows);
        return overview;
    }

    /**
     * 一次性账号任务：根据资料与行为自动标记完成，奖励需主动领取。
     */
    public Map<String, Object> claimAccountTask(Long userId, String taskCode) {
        AccountTask task = accountTaskRepository.findByCode(taskCode)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在"));
        if (safeInt(task.getEnabled()) != 1) {
            throw new IllegalArgumentException("任务已停用");
        }
        UserAccountTaskProgress progress = userAccountTaskProgressRepository
                .findByUserIdAndTaskCode(userId, taskCode)
                .orElseThrow(() -> new IllegalArgumentException("任务尚未完成"));
        if (safeInt(progress.getCompleted()) != 1) {
            throw new IllegalArgumentException("任务尚未完成");
        }
        if (safeInt(progress.getClaimed()) == 1) {
            return Map.of("claimed", false, "message", "奖励已领取");
        }

        progress.setClaimed(1);
        userAccountTaskProgressRepository.save(progress);

        UserGrowth growth = getOrCreateGrowth(userId);
        int reward = Math.max(0, safeInt(task.getRewardExp()));
        growth.setExp(safeInt(growth.getExp()) + reward);
        refreshLevelAndTitle(growth);
        userGrowthRepository.save(growth);
        refreshBadges(userId);

        return Map.of(
                "claimed", true,
                "taskCode", taskCode,
                "rewardExp", reward,
                "level", growth.getLevel(),
                "title", getUserTitle(safeInt(growth.getLevel())),
                "totalExp", growth.getExp()
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
                "title", getUserTitle(safeInt(growth.getLevel())),
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
            growth.setTitle(getUserTitle(1));
            return userGrowthRepository.save(growth);
        });
    }

    private void syncAccountTaskCompletion(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return;
        }
        List<AccountTask> accountTasks = accountTaskRepository.findByEnabledOrderBySortNoAsc(1);
        for (AccountTask task : accountTasks) {
            UserAccountTaskProgress existing = userAccountTaskProgressRepository
                    .findByUserIdAndTaskCode(userId, task.getCode())
                    .orElse(null);
            if (existing != null && safeInt(existing.getClaimed()) == 1) {
                continue;
            }
            if (!evaluateAccountTaskCheck(task.getCheckType(), user, userId)) {
                continue;
            }
            UserAccountTaskProgress row = existing != null ? existing : new UserAccountTaskProgress();
            if (existing == null) {
                row.setUserId(userId);
                row.setTaskCode(task.getCode());
            }
            row.setCompleted(1);
            userAccountTaskProgressRepository.save(row);
        }
    }

    private boolean evaluateAccountTaskCheck(String checkType, User user, Long userId) {
        if (checkType == null) {
            return false;
        }
        return switch (checkType.trim()) {
            case "HAS_PROFILE_PHOTO" -> {
                String p = user.getProfilePhoto();
                yield p != null && !p.isBlank();
            }
            case "HAS_GALLERY_PHOTO" -> userPhotoRepository.countByUserIdAndStatus(userId, "ACTIVE") > 0;
            case "IN_APPROVED_PLATFORM_GROUP" -> platGroupMemberRepository.existsByUserIdAndStatus(userId, "approved");
            case "HAS_BIO_MIN_LEN" -> {
                String bio = user.getBio();
                yield bio != null && bio.trim().length() >= 8;
            }
            case "HAS_POSTED_CONTENT" -> userGrowthLogRepository.countByUserIdAndActionType(userId, "POST_CONTENT") >= 1;
            case "HAS_PHONE_BOUND" -> {
                String phone = user.getPhoneNumber();
                yield phone != null && !phone.isBlank();
            }
            default -> false;
        };
    }

    private List<Map<String, Object>> buildAccountTaskRows(Long userId) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (AccountTask task : accountTaskRepository.findByEnabledOrderBySortNoAsc(1)) {
            UserAccountTaskProgress p = userAccountTaskProgressRepository
                    .findByUserIdAndTaskCode(userId, task.getCode())
                    .orElse(null);
            if (p != null && safeInt(p.getClaimed()) == 1) {
                continue;
            }
            boolean completed = p != null && safeInt(p.getCompleted()) == 1;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("code", task.getCode());
            item.put("name", task.getName());
            item.put("checkType", task.getCheckType());
            item.put("targetCount", 1);
            item.put("progress", completed ? 1 : 0);
            item.put("completed", completed);
            item.put("rewardExp", safeInt(task.getRewardExp()));
            rows.add(item);
        }
        return rows;
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

    private int refreshBadges(Long userId) {
        List<Badge> badges = badgeRepository.findAll();
        long loginCount = userGrowthLogRepository.countByUserIdAndActionType(userId, "LOGIN");
        long postCount = userGrowthLogRepository.countByUserIdAndActionType(userId, "POST_CONTENT");
        long joinGroupCount = userGrowthLogRepository.countByUserIdAndActionType(userId, "JOIN_GROUP");
        long completedTaskCount = userDailyTaskProgressRepository.countByUserIdAndCompleted(userId, 1);
        Map<String, Integer> fellowshipProgress = computeFellowshipBadgeProgress(userId);
        int newlyUnlocked = 0;

        for (Badge badge : badges) {
            int progressValue = switch (badge.getCode()) {
                case "FIRST_LOGIN" -> (int) loginCount;
                case "FIRST_POST" -> (int) postCount;
                case "JOIN_GROUP" -> (int) joinGroupCount;
                case "DAILY_CHECK_3" -> (int) completedTaskCount;
                case "FELLOW_NEWCOMER", "FELLOW_PROFILE_MASTER", "FELLOW_REALNAME",
                        "FELLOW_PHOTO_VERIFY", "FELLOW_PHOTO_MASTER", "FELLOW_CITY",
                        "FELLOW_TRUST", "FELLOW_JOIN" -> fellowshipProgress.getOrDefault(badge.getCode(), 0);
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
            boolean wasLocked = safeInt(userBadge.getUnlocked()) == 0;
            if (wasLocked && progressValue >= target) {
                userBadge.setUnlocked(1);
                userBadge.setUnlockedAt(LocalDateTime.now());
                newlyUnlocked++;
            }
            userBadgeRepository.save(userBadge);
        }
        return newlyUnlocked;
    }

    private Map<String, Integer> computeFellowshipBadgeProgress(Long userId) {
        Map<String, Integer> progress = new HashMap<>();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return progress;
        }
        progress.put("FELLOW_NEWCOMER", 1);

        Map<String, Boolean> verify = verificationService.getBatchSummary(List.of(userId))
                .getOrDefault(userId, Map.of());
        boolean photoVerified = Boolean.TRUE.equals(verify.get("photoVerified"));
        boolean realnameVerified = Boolean.TRUE.equals(verify.get("realnameVerified"));
        long photoCount = userPhotoRepository.countByUserId(userId);

        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("avatarUrl", user.getProfilePhoto());
        profile.put("city", user.getLocation());
        profile.put("bio", user.getBio());
        profile.put("photoVerified", photoVerified);
        profile.put("realnameVerified", realnameVerified);
        if (user.getBirthDate() != null) {
            profile.put("birthYear", user.getBirthDate().getYear());
        } else {
            Integer age = user.getAge();
            if (age != null && age > 0) {
                profile.put("age", age);
            }
        }

        Map<String, Object> completion = FellowshipProfileCompletion.build(
                user, profile, (int) photoCount, 1);
        int completionRate = completion.get("completionRate") instanceof Number n ? n.intValue() : 0;

        progress.put("FELLOW_PROFILE_MASTER", completionRate >= 100 ? 1 : 0);
        progress.put("FELLOW_REALNAME", realnameVerified ? 1 : 0);
        progress.put("FELLOW_PHOTO_VERIFY", photoVerified ? 1 : 0);
        progress.put("FELLOW_PHOTO_MASTER", photoCount >= 3 ? 1 : 0);
        progress.put("FELLOW_CITY", hasText(user.getLocation()) ? 1 : 0);
        progress.put("FELLOW_TRUST", photoVerified && realnameVerified ? 1 : 0);
        progress.put("FELLOW_JOIN", Boolean.TRUE.equals(user.getFellowshipEnabled()) ? 1 : 0);
        return progress;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
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
        growth.setTitle(getUserTitle(level));
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
