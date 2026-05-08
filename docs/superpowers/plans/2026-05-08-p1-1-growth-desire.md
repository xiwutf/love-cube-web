# P1-1 User Growth Desire Enhancement — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Give every user a vivid sense of "I'm growing, I'm about to level up, I earned a reward" — by adding level/invite reward rules, granting achievements on milestones, enriching the growth center API, and redesigning `GrowthCenterPage.vue` with five focused sections.

**Architecture:** A new `GrowthRewardCatalog` (code-only, no DB config) and `GrowthRewardService` (grant logic) slot into the existing GrowthEngine pipeline without replacing it. A new Flyway V66 migration adds a `marker` column to `dynamics` to separate dedup keys from human-readable text, and adds a unique constraint on `user_achievement(user_id, achievement_code)`. `/api/growth/me` gains six new fields; `/api/growth/achievements/me` gains four enriched fields. The frontend GrowthCenterPage.vue is redesigned with identity card, next-goal card, invite milestone card, achievement wall, and readable contribution log.

**Tech Stack:** Spring Boot 3 / JPA / Flyway (next version: V66); Vue 3 `<script setup>` / plain CSS (platform layer — no Vant).

---

## File Map

### New files
| Path | Purpose |
|------|---------|
| `backend/src/main/resources/db/migration/V66__growth_p1_marker_achievement_unique.sql` | Add `dynamics.marker` column + `user_achievement` unique constraint |
| `backend/src/main/java/com/lovecube/backend/growth/service/GrowthRewardCatalog.java` | Code-defined reward rules (level + invite milestones) |
| `backend/src/main/java/com/lovecube/backend/growth/service/GrowthRewardService.java` | Achievement grant logic, broadcast trigger |

### Modified files (backend)
| File | What changes |
|------|-------------|
| `backend/src/main/java/com/lovecube/backend/entity/Dynamic.java` | +`marker` field |
| `backend/src/main/java/com/lovecube/backend/repository/DynamicRepository.java` | +`existsByUserIdAndMarkerAndSceneTypeAndIsDeletedFalse` |
| `backend/src/main/java/com/lovecube/backend/growth/repository/UserAchievementRepository.java` | +`existsByUserIdAndAchievementCode`, +`findByUserId` |
| `backend/src/main/java/com/lovecube/backend/growth/service/UserGrowthService.java` | Extend `resolveLevel()` to Lv10 |
| `backend/src/main/java/com/lovecube/backend/growth/service/GrowthBroadcastService.java` | +`broadcastReward()`, update existing to write readable content + marker column |
| `backend/src/main/java/com/lovecube/backend/growth/service/GrowthEngine.java` | Inject + call `GrowthRewardService` after level-up |
| `backend/src/main/java/com/lovecube/backend/services/FellowshipInviteService.java` | +`countEffectiveInvites()` |
| `backend/src/main/java/com/lovecube/backend/controllers/AuthController.java` | Inject `GrowthRewardService`, call invite milestone check after registration |
| `backend/src/main/java/com/lovecube/backend/growth/dto/GrowthCenterDTO.java` | +6 new fields |
| `backend/src/main/java/com/lovecube/backend/growth/dto/UserAchievementDTO.java` | +4 new fields |
| `backend/src/main/java/com/lovecube/backend/growth/controller/GrowthController.java` | Inject `GrowthRewardCatalog` + `FellowshipInviteService`, populate all new DTO fields |

### Modified files (frontend)
| File | What changes |
|------|-------------|
| `frontend/src/pages/platform/GrowthCenterPage.vue` | Full 5-section redesign |

---

## Level Thresholds (Lv1–Lv10)
| Level | Min Contribution |
|-------|-----------------|
| Lv1 | 0 |
| Lv2 | 50 |
| Lv3 | 150 |
| Lv4 | 300 |
| Lv5 | 500 |
| Lv6 | 750 |
| Lv7 | 1000 |
| Lv8 | 1500 |
| Lv9 | 2000 |
| Lv10 | 3000 |

---

## Tasks

---

### Task 1: Flyway migration V66

**Files:**
- Create: `backend/src/main/resources/db/migration/V66__growth_p1_marker_achievement_unique.sql`

- [ ] **Step 1: Write the migration**

```sql
-- Add marker column to dynamics for separating dedup key from display text
ALTER TABLE dynamics ADD COLUMN marker VARCHAR(255) DEFAULT NULL;
ALTER TABLE dynamics ADD INDEX idx_dynamics_marker_scene (user_id, marker, scene_type);

-- Unique constraint on user_achievement(user_id, achievement_code)
-- Remove duplicate rows first (keep lowest id per pair)
DELETE ua1 FROM user_achievement ua1
INNER JOIN user_achievement ua2
  ON ua1.user_id = ua2.user_id
 AND ua1.achievement_code = ua2.achievement_code
 AND ua1.id > ua2.id;

ALTER TABLE user_achievement
  ADD UNIQUE KEY uk_user_achievement_code (user_id, achievement_code);
```

- [ ] **Step 2: Verify file exists**

```powershell
ls backend\src\main\resources\db\migration\V66*
```

Expected: one file listed.

---

### Task 2: Dynamic entity + DynamicRepository

**Files:**
- Modify: `backend/src/main/java/com/lovecube/backend/entity/Dynamic.java`
- Modify: `backend/src/main/java/com/lovecube/backend/repository/DynamicRepository.java`

- [ ] **Step 1: Add `marker` field to Dynamic.java**

After the `sceneType` field (line 38 area), insert:

```java
@Column(name = "marker", length = 255)
private String marker;
```

- [ ] **Step 2: Add dedup query to DynamicRepository.java**

Add this method alongside the existing `existsByUserIdAndContentAndSceneTypeAndIsDeletedFalse`:

```java
boolean existsByUserIdAndMarkerAndSceneTypeAndIsDeletedFalse(Long userId, String marker, String sceneType);
```

- [ ] **Step 3: Compile**

```powershell
cd backend; .\mvnw.cmd compile -q
```

Expected: `BUILD SUCCESS`

---

### Task 3: GrowthRewardCatalog

**Files:**
- Create: `backend/src/main/java/com/lovecube/backend/growth/service/GrowthRewardCatalog.java`

- [ ] **Step 1: Write the catalog**

```java
package com.lovecube.backend.growth.service;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class GrowthRewardCatalog {

    public enum RewardType { BADGE, TITLE, CANDIDATE }

    public record LevelReward(
            String rewardCode, String rewardName, RewardType rewardType,
            String description, String displayText, int unlockLevel
    ) {}

    public record InviteMilestoneReward(
            String rewardCode, String rewardName, RewardType rewardType,
            String description, String displayText, int requiredCount
    ) {}

    public record RewardInfo(String rewardName, String rewardType, String description, String displayText) {}

    private static final List<LevelReward> LEVEL_REWARDS = List.of(
            new LevelReward("LEVEL_2_TITLE", "成长新星", RewardType.TITLE,
                    "完成初步成长，解锁新星称号", "解锁称号「成长新星」", 2),
            new LevelReward("LEVEL_3_BADGE", "社区活跃者", RewardType.BADGE,
                    "持续参与社区互动，解锁活跃者徽章", "解锁徽章「社区活跃者」", 3),
            new LevelReward("LEVEL_5_TITLE", "内容探索者", RewardType.TITLE,
                    "大量创作内容，解锁探索者称号", "解锁称号「内容探索者」", 5),
            new LevelReward("LEVEL_8_CANDIDATE", "团体候选者", RewardType.CANDIDATE,
                    "深度参与团体建设，成为团体候选人", "解锁身份「团体候选者」", 8),
            new LevelReward("LEVEL_10_CANDIDATE", "新星推广官候选", RewardType.CANDIDATE,
                    "成为平台核心推广力量", "解锁身份「新星推广官候选」", 10)
    );

    private static final List<InviteMilestoneReward> INVITE_REWARDS = List.of(
            new InviteMilestoneReward("INVITE_1_BADGE", "新人连接者", RewardType.BADGE,
                    "邀请首位新用户加入平台", "解锁徽章「新人连接者」", 1),
            new InviteMilestoneReward("INVITE_3_BADGE", "关系搭建者", RewardType.BADGE,
                    "帮助3位新用户加入平台", "解锁徽章「关系搭建者」", 3),
            new InviteMilestoneReward("INVITE_5_BADGE", "人气推荐者", RewardType.BADGE,
                    "帮助5位新用户加入平台", "解锁徽章「人气推荐者」", 5),
            new InviteMilestoneReward("INVITE_10_CANDIDATE", "推广官候选", RewardType.CANDIDATE,
                    "帮助10位新用户加入平台", "解锁身份「推广官候选」", 10)
    );

    public List<LevelReward> getLevelRewards() { return LEVEL_REWARDS; }
    public List<InviteMilestoneReward> getInviteRewards() { return INVITE_REWARDS; }

    public Optional<LevelReward> getRewardForLevel(int level) {
        return LEVEL_REWARDS.stream().filter(r -> r.unlockLevel() == level).findFirst();
    }

    public List<InviteMilestoneReward> getInviteRewardsUpTo(int count) {
        return INVITE_REWARDS.stream().filter(r -> r.requiredCount() <= count).toList();
    }

    public Optional<InviteMilestoneReward> getNextInviteReward(int currentCount) {
        return INVITE_REWARDS.stream().filter(r -> r.requiredCount() > currentCount).findFirst();
    }

    public Optional<RewardInfo> getByCode(String code) {
        Optional<LevelReward> lr = LEVEL_REWARDS.stream()
                .filter(r -> r.rewardCode().equals(code)).findFirst();
        if (lr.isPresent()) {
            LevelReward r = lr.get();
            return Optional.of(new RewardInfo(r.rewardName(), r.rewardType().name(), r.description(), r.displayText()));
        }
        return INVITE_REWARDS.stream()
                .filter(r -> r.rewardCode().equals(code))
                .map(r -> new RewardInfo(r.rewardName(), r.rewardType().name(), r.description(), r.displayText()))
                .findFirst();
    }

    /** Highest-level TITLE the user has earned, or empty. */
    public Optional<String> resolveCurrentTitle(Set<String> earnedCodes) {
        return LEVEL_REWARDS.stream()
                .filter(r -> r.rewardType() == RewardType.TITLE && earnedCodes.contains(r.rewardCode()))
                .max(Comparator.comparingInt(LevelReward::unlockLevel))
                .map(LevelReward::rewardName);
    }

    /** Highest-ranking BADGE the user has earned (invite badges take priority). */
    public Optional<String> resolveCurrentBadge(Set<String> earnedCodes) {
        Optional<String> inviteBadge = INVITE_REWARDS.stream()
                .filter(r -> r.rewardType() == RewardType.BADGE && earnedCodes.contains(r.rewardCode()))
                .max(Comparator.comparingInt(InviteMilestoneReward::requiredCount))
                .map(InviteMilestoneReward::rewardName);
        if (inviteBadge.isPresent()) return inviteBadge;
        return LEVEL_REWARDS.stream()
                .filter(r -> r.rewardType() == RewardType.BADGE && earnedCodes.contains(r.rewardCode()))
                .max(Comparator.comparingInt(LevelReward::unlockLevel))
                .map(LevelReward::rewardName);
    }
}
```

- [ ] **Step 2: Compile**

```powershell
cd backend; .\mvnw.cmd compile -q
```

Expected: `BUILD SUCCESS`

---

### Task 4: UserAchievementRepository — add two methods

**Files:**
- Modify: `backend/src/main/java/com/lovecube/backend/growth/repository/UserAchievementRepository.java`

- [ ] **Step 1: Replace file content**

```java
package com.lovecube.backend.growth.repository;

import com.lovecube.backend.growth.entity.UserAchievement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    Page<UserAchievement> findByUserIdOrderByGrantedAtDesc(Long userId, Pageable pageable);
    boolean existsByUserIdAndAchievementCode(Long userId, String achievementCode);
    List<UserAchievement> findByUserId(Long userId);
}
```

- [ ] **Step 2: Compile**

```powershell
cd backend; .\mvnw.cmd compile -q
```

Expected: `BUILD SUCCESS`

---

### Task 5: Extend level system to Lv10

**Files:**
- Modify: `backend/src/main/java/com/lovecube/backend/growth/service/UserGrowthService.java`
- Modify: `backend/src/main/java/com/lovecube/backend/growth/controller/GrowthController.java`

- [ ] **Step 1: Replace `resolveLevel()` in UserGrowthService.java**

Find the existing `resolveLevel` method (around line 59) and replace it:

```java
private int resolveLevel(int totalContribution) {
    if (totalContribution >= 3000) return 10;
    if (totalContribution >= 2000) return 9;
    if (totalContribution >= 1500) return 8;
    if (totalContribution >= 1000) return 7;
    if (totalContribution >= 750) return 6;
    if (totalContribution >= 500) return 5;
    if (totalContribution >= 300) return 4;
    if (totalContribution >= 150) return 3;
    if (totalContribution >= 50) return 2;
    return 1;
}
```

- [ ] **Step 2: Replace `resolveLevel()` and `resolveLevelThreshold()` in GrowthController.java**

Find `resolveLevel` (around line 210) and replace with:

```java
private int resolveLevel(int totalContribution) {
    if (totalContribution >= 3000) return 10;
    if (totalContribution >= 2000) return 9;
    if (totalContribution >= 1500) return 8;
    if (totalContribution >= 1000) return 7;
    if (totalContribution >= 750) return 6;
    if (totalContribution >= 500) return 5;
    if (totalContribution >= 300) return 4;
    if (totalContribution >= 150) return 3;
    if (totalContribution >= 50) return 2;
    return 1;
}

private int resolveLevelThreshold(int level) {
    return switch (level) {
        case 2 -> 50;
        case 3 -> 150;
        case 4 -> 300;
        case 5 -> 500;
        case 6 -> 750;
        case 7 -> 1000;
        case 8 -> 1500;
        case 9 -> 2000;
        case 10 -> 3000;
        default -> 3000;
    };
}
```

Also find `int nextLevel = Math.min(5, level + 1);` in `toGrowthCenterDto` and change `5` to `10`:

```java
int nextLevel = Math.min(10, level + 1);
```

- [ ] **Step 3: Compile**

```powershell
cd backend; .\mvnw.cmd compile -q
```

Expected: `BUILD SUCCESS`

---

### Task 6: GrowthRewardService

**Files:**
- Create: `backend/src/main/java/com/lovecube/backend/growth/service/GrowthRewardService.java`

- [ ] **Step 1: Write the service**

```java
package com.lovecube.backend.growth.service;

import com.lovecube.backend.growth.entity.GrowthEvent;
import com.lovecube.backend.growth.entity.UserAchievement;
import com.lovecube.backend.growth.repository.UserAchievementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GrowthRewardService {

    private final GrowthRewardCatalog rewardCatalog;
    private final UserAchievementRepository userAchievementRepository;
    private final GrowthBroadcastService growthBroadcastService;

    public GrowthRewardService(
            GrowthRewardCatalog rewardCatalog,
            UserAchievementRepository userAchievementRepository,
            GrowthBroadcastService growthBroadcastService
    ) {
        this.rewardCatalog = rewardCatalog;
        this.userAchievementRepository = userAchievementRepository;
        this.growthBroadcastService = growthBroadcastService;
    }

    @Transactional
    public void checkAndGrantLevelReward(Long userId, int newLevel, GrowthEvent triggerEvent) {
        rewardCatalog.getRewardForLevel(newLevel).ifPresent(reward -> {
            if (userAchievementRepository.existsByUserIdAndAchievementCode(userId, reward.rewardCode())) {
                return;
            }
            userAchievementRepository.save(newAchievement(userId, reward.rewardCode(), newLevel,
                    triggerEvent != null ? triggerEvent.getId() : null));
            String typeLabel = rewardTypeLabel(reward.rewardType());
            String displayText = "🎉 恭喜你达到 Lv" + newLevel + "，获得「" + reward.rewardName() + "」" + typeLabel;
            growthBroadcastService.broadcastReward(userId, "LEVEL_REWARD_" + newLevel, displayText, triggerEvent);
        });
    }

    @Transactional
    public void checkAndGrantInviteMilestoneRewards(Long userId, int effectiveInviteCount) {
        List<GrowthRewardCatalog.InviteMilestoneReward> earned =
                rewardCatalog.getInviteRewardsUpTo(effectiveInviteCount);
        for (GrowthRewardCatalog.InviteMilestoneReward reward : earned) {
            if (userAchievementRepository.existsByUserIdAndAchievementCode(userId, reward.rewardCode())) {
                continue;
            }
            userAchievementRepository.save(newAchievement(userId, reward.rewardCode(), 0, null));
            String displayText = "🎉 你已成功帮助 " + reward.requiredCount()
                    + " 位新用户加入平台，解锁「" + reward.rewardName() + "」徽章";
            growthBroadcastService.broadcastReward(userId, reward.rewardCode(), displayText, null);
        }
    }

    private UserAchievement newAchievement(Long userId, String code, int level, Long eventId) {
        UserAchievement a = new UserAchievement();
        a.setUserId(userId);
        a.setAchievementCode(code);
        a.setLevel(level);
        a.setEventId(eventId);
        a.setRuleVersion("v1");
        a.setStatus("GRANTED");
        LocalDateTime now = LocalDateTime.now();
        a.setGrantedAt(now);
        a.setCreatedAt(now);
        return a;
    }

    private String rewardTypeLabel(GrowthRewardCatalog.RewardType type) {
        return switch (type) {
            case BADGE -> "徽章";
            case TITLE -> "称号";
            case CANDIDATE -> "候选身份";
        };
    }
}
```

- [ ] **Step 2: Compile**

```powershell
cd backend; .\mvnw.cmd compile -q
```

Expected: `BUILD SUCCESS`

---

### Task 7: GrowthBroadcastService — add broadcastReward + use marker column

**Files:**
- Modify: `backend/src/main/java/com/lovecube/backend/growth/service/GrowthBroadcastService.java`

Changes: `broadcastGroupCreated` and `broadcastLevelUp` now store human-readable text in `content` and the machine marker in `marker`. New `broadcastReward` method for P1-1 rewards. Dedup checks both `marker` column (new records) and `content` column (legacy fallback).

- [ ] **Step 1: Replace GrowthBroadcastService.java**

```java
package com.lovecube.backend.growth.service;

import com.lovecube.backend.entity.Dynamic;
import com.lovecube.backend.growth.entity.GrowthEvent;
import com.lovecube.backend.growth.entity.UserGrowth;
import com.lovecube.backend.repository.DynamicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class GrowthBroadcastService {
    private static final String SCENE_TYPE_GROWTH = "GROWTH";

    private final DynamicRepository dynamicRepository;

    public GrowthBroadcastService(DynamicRepository dynamicRepository) {
        this.dynamicRepository = dynamicRepository;
    }

    @Transactional
    public void broadcastGroupCreated(GrowthEvent event) {
        String marker = marker("GROUP_CREATED", event.getId());
        publishIfAbsent(event.getActorUserId(), marker, "创建了新的团体，成长里程碑已达成");
    }

    @Transactional
    public void broadcastLevelUp(GrowthEvent event, UserGrowth userGrowth) {
        String marker = marker("LEVEL_UP", event.getId());
        publishIfAbsent(event.getActorUserId(), marker,
                "成长等级提升至 Lv." + userGrowth.getLevel() + "，继续保持！");
    }

    @Transactional
    public void broadcastReward(Long userId, String markerKey, String displayText, GrowthEvent event) {
        String eventSuffix = event != null ? "|event=" + event.getId() : "";
        String marker = "[GROWTH|REWARD|" + markerKey + eventSuffix + "]";
        publishIfAbsent(userId, marker, displayText);
    }

    private String marker(String type, Long eventId) {
        return "[GROWTH|" + type + "|event=" + eventId + "]";
    }

    private void publishIfAbsent(Long userId, String marker, String displayText) {
        // Dedup by marker column (records created after V66 migration)
        if (dynamicRepository.existsByUserIdAndMarkerAndSceneTypeAndIsDeletedFalse(
                userId, marker, SCENE_TYPE_GROWTH)) {
            return;
        }
        // Legacy fallback: pre-V66 records stored marker string in content column
        if (dynamicRepository.existsByUserIdAndContentAndSceneTypeAndIsDeletedFalse(
                userId, marker, SCENE_TYPE_GROWTH)) {
            return;
        }
        Dynamic dynamic = new Dynamic();
        dynamic.setUserId(userId);
        dynamic.setContent(displayText);
        dynamic.setMarker(marker);
        dynamic.setImageUrls(null);
        dynamic.setLikeCount(0);
        dynamic.setCommentCount(0);
        dynamic.setShareCount(0);
        dynamic.setIsDeleted(false);
        dynamic.setSceneType(SCENE_TYPE_GROWTH);
        dynamic.setCreatedAt(LocalDateTime.now());
        dynamic.setUpdatedAt(LocalDateTime.now());
        dynamicRepository.save(dynamic);
    }
}
```

- [ ] **Step 2: Compile**

```powershell
cd backend; .\mvnw.cmd compile -q
```

Expected: `BUILD SUCCESS`

---

### Task 8: GrowthEngine — wire GrowthRewardService after level-up

**Files:**
- Modify: `backend/src/main/java/com/lovecube/backend/growth/service/GrowthEngine.java`

- [ ] **Step 1: Replace GrowthEngine.java**

```java
package com.lovecube.backend.growth.service;

import com.lovecube.backend.growth.entity.ContributionLog;
import com.lovecube.backend.growth.entity.GrowthEvent;
import com.lovecube.backend.growth.enums.GrowthEventType;
import com.lovecube.backend.growth.enums.SettleStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GrowthEngine {
    private final GrowthRuleCatalog growthRuleCatalog;
    private final ContributionService contributionService;
    private final UserGrowthService userGrowthService;
    private final GrowthBroadcastService growthBroadcastService;
    private final GrowthRewardService growthRewardService;

    public GrowthEngine(
            GrowthRuleCatalog growthRuleCatalog,
            ContributionService contributionService,
            UserGrowthService userGrowthService,
            GrowthBroadcastService growthBroadcastService,
            GrowthRewardService growthRewardService
    ) {
        this.growthRuleCatalog = growthRuleCatalog;
        this.contributionService = contributionService;
        this.userGrowthService = userGrowthService;
        this.growthBroadcastService = growthBroadcastService;
        this.growthRewardService = growthRewardService;
    }

    @Transactional
    public void consume(GrowthEvent event) {
        if (!SettleStatus.SETTLED.name().equals(event.getSettleStatus())) {
            return;
        }
        GrowthRuleCatalog.GrowthRule rule = growthRuleCatalog.getRule(
                GrowthEventType.valueOf(event.getEventType()));
        GrowthEventType eventType = GrowthEventType.valueOf(event.getEventType());
        ContributionLog contributionLog = contributionService.createActorContribution(event, rule);
        if (contributionLog == null) {
            return;
        }
        UserGrowthService.GrowthApplyResult applyResult = userGrowthService.applyContribution(contributionLog);
        triggerMilestoneBroadcast(event, eventType, applyResult);
    }

    @Transactional
    public void consumeForTesting(GrowthEvent event) {
        consume(event);
    }

    private void triggerMilestoneBroadcast(
            GrowthEvent event,
            GrowthEventType eventType,
            UserGrowthService.GrowthApplyResult applyResult
    ) {
        try {
            if (eventType == GrowthEventType.GROUP_CREATED) {
                growthBroadcastService.broadcastGroupCreated(event);
            }
            if (applyResult.leveledUp()) {
                growthBroadcastService.broadcastLevelUp(event, applyResult.userGrowth());
                growthRewardService.checkAndGrantLevelReward(
                        event.getActorUserId(), applyResult.userGrowth().getLevel(), event);
            }
        } catch (Exception ignored) {
            // Broadcast/reward failure must not affect growth bookkeeping.
        }
    }
}
```

- [ ] **Step 2: Compile**

```powershell
cd backend; .\mvnw.cmd compile -q
```

Expected: `BUILD SUCCESS`

---

### Task 9: FellowshipInviteService + AuthController — invite milestone trigger

**Files:**
- Modify: `backend/src/main/java/com/lovecube/backend/services/FellowshipInviteService.java`
- Modify: `backend/src/main/java/com/lovecube/backend/controllers/AuthController.java`

- [ ] **Step 1: Add `countEffectiveInvites()` to FellowshipInviteService.java**

Insert this public method anywhere after `getMyCodeSummary`:

```java
public long countEffectiveInvites(Long userId) {
    return userInviteRelationRepository.countByInviterUserIdAndStatus(userId, "SUCCESS");
}
```

- [ ] **Step 2: Add GrowthRewardService field to AuthController**

In AuthController, add to the field declarations:
```java
private final com.lovecube.backend.growth.service.GrowthRewardService growthRewardService;
```

Add `com.lovecube.backend.growth.service.GrowthRewardService growthRewardService` as the last parameter of the constructor, and assign `this.growthRewardService = growthRewardService;` in the body.

The updated constructor signature becomes:
```java
public AuthController(
        UserRepository userRepository,
        BCryptPasswordEncoder passwordEncoder,
        FellowshipInviteService fellowshipInviteService,
        AdminAuthService adminAuthService,
        GrowthService growthService,
        GrowthEventService growthEventService,
        com.lovecube.backend.growth.service.GrowthRewardService growthRewardService
) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.fellowshipInviteService = fellowshipInviteService;
    this.adminAuthService = adminAuthService;
    this.growthService = growthService;
    this.growthEventService = growthEventService;
    this.growthRewardService = growthRewardService;
}
```

- [ ] **Step 3: Call milestone check after createSuccessRecord in AuthController**

Find the block that calls `fellowshipInviteService.createSuccessRecord(inviteCode, inviter, saved, ...)`. Immediately after that call, add:

```java
try {
    long effectiveCount = fellowshipInviteService.countEffectiveInvites(inviter.getUserid());
    growthRewardService.checkAndGrantInviteMilestoneRewards(inviter.getUserid(), (int) effectiveCount);
} catch (Exception ignored) {
    // milestone reward must not break registration
}
```

- [ ] **Step 4: Compile**

```powershell
cd backend; .\mvnw.cmd compile -q
```

Expected: `BUILD SUCCESS`

---

### Task 10: GrowthCenterDTO + UserAchievementDTO — add new fields

**Files:**
- Modify: `backend/src/main/java/com/lovecube/backend/growth/dto/GrowthCenterDTO.java`
- Modify: `backend/src/main/java/com/lovecube/backend/growth/dto/UserAchievementDTO.java`

- [ ] **Step 1: Replace GrowthCenterDTO.java**

```java
package com.lovecube.backend.growth.dto;

import lombok.Data;

@Data
public class GrowthCenterDTO {
    private Long userId;
    private Integer level;
    private String stage;
    private Integer totalContribution;
    private Integer contribContent;
    private Integer contribOrg;
    private Integer contribSpread;
    private Integer contribCity;
    private Integer nextLevel;
    private Integer nextLevelThreshold;
    private Integer contributionToNextLevel;
    // P1-1 reward fields
    private String currentTitle;
    private String currentBadge;
    private String nextLevelRewardName;
    private String nextLevelRewardType;
    private String nextLevelRewardDisplayText;
    private Integer inviteMilestoneProgress;
    private String nextInviteRewardName;
    private String nextInviteRewardType;
    private String nextInviteRewardDisplayText;
    private Integer nextInviteRequiredCount;
}
```

- [ ] **Step 2: Replace UserAchievementDTO.java**

```java
package com.lovecube.backend.growth.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserAchievementDTO {
    private Long id;
    private String achievementCode;
    private Integer level;
    private String status;
    private LocalDateTime grantedAt;
    // P1-1 enriched fields (resolved from catalog)
    private String achievementName;
    private String achievementType;
    private String description;
    private String displayText;
}
```

- [ ] **Step 3: Compile**

```powershell
cd backend; .\mvnw.cmd compile -q
```

Expected: `BUILD SUCCESS`

---

### Task 11: GrowthController — populate all new fields

**Files:**
- Modify: `backend/src/main/java/com/lovecube/backend/growth/controller/GrowthController.java`

Changes: inject `GrowthRewardCatalog` and `FellowshipInviteService`; rewrite `toGrowthCenterDto`; update `toAchievementDto`.

- [ ] **Step 1: Add two new field declarations and constructor parameters**

Add fields:
```java
private final GrowthRewardCatalog growthRewardCatalog;
private final com.lovecube.backend.services.FellowshipInviteService fellowshipInviteService;
```

Update the constructor — add both as parameters and assign:
```java
public GrowthController(
        GrowthEventService growthEventService,
        AdminAuthService adminAuthService,
        GrowthUserGrowthRepository growthUserGrowthRepository,
        ContributionLogRepository contributionLogRepository,
        UserAchievementRepository userAchievementRepository,
        GrowthEventRepository growthEventRepository,
        GrowthRewardCatalog growthRewardCatalog,
        com.lovecube.backend.services.FellowshipInviteService fellowshipInviteService
) {
    this.growthEventService = growthEventService;
    this.adminAuthService = adminAuthService;
    this.growthUserGrowthRepository = growthUserGrowthRepository;
    this.contributionLogRepository = contributionLogRepository;
    this.userAchievementRepository = userAchievementRepository;
    this.growthEventRepository = growthEventRepository;
    this.growthRewardCatalog = growthRewardCatalog;
    this.fellowshipInviteService = fellowshipInviteService;
}
```

- [ ] **Step 2: Add required imports at the top of GrowthController.java**

```java
import com.lovecube.backend.growth.service.GrowthRewardCatalog;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
```

- [ ] **Step 3: Replace `toGrowthCenterDto` method**

```java
private GrowthCenterDTO toGrowthCenterDto(Long userId, UserGrowth growth) {
    GrowthCenterDTO dto = new GrowthCenterDTO();
    dto.setUserId(userId);

    int total = growth == null ? 0 : safe(growth.getTotalContribution());
    int level = growth == null ? 1
            : (safe(growth.getLevel()) == 0 ? resolveLevel(total) : safe(growth.getLevel()));
    int nextLevel = Math.min(10, level + 1);
    int nextThreshold = resolveLevelThreshold(nextLevel);
    int toNext = Math.max(0, nextThreshold - total);

    dto.setLevel(level);
    dto.setStage(growth == null || growth.getStage() == null ? "normal" : growth.getStage());
    dto.setTotalContribution(total);
    dto.setContribContent(growth == null ? 0 : safe(growth.getContribContent()));
    dto.setContribOrg(growth == null ? 0 : safe(growth.getContribOrg()));
    dto.setContribSpread(growth == null ? 0 : safe(growth.getContribSpread()));
    dto.setContribCity(growth == null ? 0 : safe(growth.getContribCity()));
    dto.setNextLevel(nextLevel);
    dto.setNextLevelThreshold(nextThreshold);
    dto.setContributionToNextLevel(toNext);

    // Resolve current title and badge from earned achievements
    List<com.lovecube.backend.growth.entity.UserAchievement> earned =
            userAchievementRepository.findByUserId(userId);
    Set<String> earnedCodes = earned.stream()
            .map(com.lovecube.backend.growth.entity.UserAchievement::getAchievementCode)
            .collect(Collectors.toSet());
    dto.setCurrentTitle(growthRewardCatalog.resolveCurrentTitle(earnedCodes).orElse(null));
    dto.setCurrentBadge(growthRewardCatalog.resolveCurrentBadge(earnedCodes).orElse(null));

    // Next level reward preview
    growthRewardCatalog.getRewardForLevel(nextLevel).ifPresent(r -> {
        dto.setNextLevelRewardName(r.rewardName());
        dto.setNextLevelRewardType(r.rewardType().name());
        dto.setNextLevelRewardDisplayText(r.displayText());
    });

    // Invite milestone progress
    int inviteCount = (int) fellowshipInviteService.countEffectiveInvites(userId);
    dto.setInviteMilestoneProgress(inviteCount);
    growthRewardCatalog.getNextInviteReward(inviteCount).ifPresent(r -> {
        dto.setNextInviteRewardName(r.rewardName());
        dto.setNextInviteRewardType(r.rewardType().name());
        dto.setNextInviteRewardDisplayText(r.displayText());
        dto.setNextInviteRequiredCount(r.requiredCount());
    });

    return dto;
}
```

- [ ] **Step 4: Replace `toAchievementDto` method**

```java
private UserAchievementDTO toAchievementDto(UserAchievement entity) {
    UserAchievementDTO dto = new UserAchievementDTO();
    dto.setId(entity.getId());
    dto.setAchievementCode(entity.getAchievementCode());
    dto.setLevel(entity.getLevel());
    dto.setStatus(entity.getStatus());
    dto.setGrantedAt(entity.getGrantedAt());
    growthRewardCatalog.getByCode(entity.getAchievementCode()).ifPresent(info -> {
        dto.setAchievementName(info.rewardName());
        dto.setAchievementType(info.rewardType());
        dto.setDescription(info.description());
        dto.setDisplayText(info.displayText());
    });
    return dto;
}
```

- [ ] **Step 5: Compile**

```powershell
cd backend; .\mvnw.cmd compile -q
```

Expected: `BUILD SUCCESS`

---

### Task 12: GrowthCenterPage.vue — five-section redesign

**Files:**
- Modify: `frontend/src/pages/platform/GrowthCenterPage.vue`

- [ ] **Step 1: Replace entire file**

```vue
<template>
  <section class="growth-page">
    <header class="growth-head">
      <button type="button" class="growth-back" aria-label="返回" @click="$router.back()">‹</button>
      <h1 class="growth-title">成长中心</h1>
    </header>

    <div class="growth-body">
      <p v-if="loading" class="growth-status">加载中...</p>
      <template v-else>

        <!-- 1. Identity Card -->
        <article class="growth-card identity-card">
          <div class="identity-level">
            <span class="lv-badge">Lv.{{ growth.level }}</span>
            <div class="identity-tags">
              <span v-if="growth.currentTitle" class="tag tag-title">{{ growth.currentTitle }}</span>
              <span v-if="growth.currentBadge" class="tag tag-badge">{{ growth.currentBadge }}</span>
            </div>
          </div>
          <p class="identity-contrib">总贡献值 <strong>{{ growth.totalContribution }}</strong></p>
          <div class="contrib-grid">
            <div class="contrib-item">
              <label>内容</label>
              <strong>{{ growth.contribContent }}</strong>
            </div>
            <div class="contrib-item">
              <label>组织</label>
              <strong>{{ growth.contribOrg }}</strong>
            </div>
            <div class="contrib-item">
              <label>传播</label>
              <strong>{{ growth.contribSpread }}</strong>
            </div>
            <div class="contrib-item">
              <label>城市</label>
              <strong>{{ growth.contribCity }}</strong>
            </div>
          </div>
        </article>

        <!-- 2. Next Goal Card -->
        <article class="growth-card next-goal-card">
          <h2 class="card-title">下一目标</h2>
          <div class="goal-header">
            <span>Lv.{{ growth.level }}</span>
            <span class="goal-label">
              距 Lv.{{ growth.nextLevel }} 还差
              <strong>{{ growth.contributionToNextLevel }}</strong> 贡献值
            </span>
            <span>Lv.{{ growth.nextLevel }}</span>
          </div>
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: levelProgressPct + '%' }"></div>
          </div>
          <p v-if="growth.nextLevelRewardDisplayText" class="goal-reward">
            升至 Lv.{{ growth.nextLevel }} 可获得：<strong>{{ growth.nextLevelRewardDisplayText }}</strong>
          </p>
          <p v-else-if="growth.level >= 10" class="goal-reward">已达到最高等级，感谢你的持续贡献！</p>
          <p v-else class="goal-reward">升至 Lv.{{ growth.nextLevel }} 继续前进！</p>
        </article>

        <!-- 3. Invite Milestone Card -->
        <article class="growth-card invite-card">
          <h2 class="card-title">邀请里程碑</h2>
          <div class="invite-progress">
            <span class="invite-count">有效邀请 <strong>{{ growth.inviteMilestoneProgress ?? 0 }}</strong> 人</span>
            <span v-if="growth.nextInviteRequiredCount" class="invite-next">
              再邀请
              <strong>{{ growth.nextInviteRequiredCount - (growth.inviteMilestoneProgress ?? 0) }}</strong>
              人，解锁「{{ growth.nextInviteRewardName }}」
            </span>
            <span v-else class="invite-next invite-max">已解锁全部邀请奖励！</span>
          </div>
          <div v-if="growth.nextInviteRequiredCount" class="progress-bar">
            <div class="progress-fill" :style="{ width: inviteProgressPct + '%' }"></div>
          </div>
        </article>

        <!-- 4. Achievement Wall -->
        <article class="growth-card achievement-card">
          <h2 class="card-title">我的成就</h2>
          <div v-if="achievements.length" class="achievement-grid">
            <div v-for="item in achievements" :key="item.id" class="achievement-item">
              <div class="achievement-icon">{{ achievementIcon(item.achievementType) }}</div>
              <p class="achievement-name">{{ item.achievementName || item.achievementCode }}</p>
              <p class="achievement-time">{{ formatDate(item.grantedAt) }}</p>
            </div>
          </div>
          <p v-else class="empty-state">继续发动态、加入团体、邀请朋友，即可解锁成长奖励。</p>
        </article>

        <!-- 5. Recent Contributions -->
        <article class="growth-card contrib-log-card">
          <h2 class="card-title">最近成长记录</h2>
          <ul v-if="contributions.length" class="contrib-list">
            <li v-for="log in contributions" :key="log.id" class="contrib-row">
              <div>
                <strong>{{ eventLabel(log.eventType) }}</strong>
                <p>{{ dimensionLabel(log.dimension) }}贡献 · {{ formatTime(log.occurredAt) }}</p>
              </div>
              <span class="contrib-delta">+{{ log.deltaFinal }}</span>
            </li>
          </ul>
          <p v-else class="empty-state">暂无贡献记录</p>
        </article>

      </template>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getGrowthAchievements, getGrowthCenter, getGrowthContributions } from '@/api/growth.js'

const loading = ref(false)
const growth = ref({
  level: 1,
  stage: 'normal',
  totalContribution: 0,
  contribContent: 0,
  contribOrg: 0,
  contribSpread: 0,
  contribCity: 0,
  nextLevel: 2,
  nextLevelThreshold: 50,
  contributionToNextLevel: 50,
  currentTitle: null,
  currentBadge: null,
  nextLevelRewardName: null,
  nextLevelRewardDisplayText: null,
  inviteMilestoneProgress: 0,
  nextInviteRewardName: null,
  nextInviteRequiredCount: null,
})
const contributions = ref([])
const achievements = ref([])

const levelProgressPct = computed(() => {
  const threshold = growth.value.nextLevelThreshold
  const toNext = growth.value.contributionToNextLevel
  if (threshold <= 0) return 100
  const earned = threshold - toNext
  return Math.min(100, Math.max(0, Math.round((earned / threshold) * 100)))
})

const inviteProgressPct = computed(() => {
  const required = growth.value.nextInviteRequiredCount
  const current = growth.value.inviteMilestoneProgress ?? 0
  if (!required) return 100
  const MILESTONES = [0, 1, 3, 5, 10]
  const prevMilestone = MILESTONES.filter(m => m < required).pop() ?? 0
  const span = required - prevMilestone
  const progress = current - prevMilestone
  return Math.min(100, Math.max(0, Math.round((progress / span) * 100)))
})

const EVENT_LABELS = {
  POST_CREATED: '发布动态',
  POST_LIKED: '收到点赞',
  POST_COMMENTED: '收到评论',
  GROUP_CREATED: '创建团体',
  GROUP_JOINED: '加入团体',
  USER_REGISTERED: '完成注册',
  USER_PROFILE_COMPLETED: '完善个人资料',
  USER_DAILY_ACTIVE: '每日活跃',
  USER_INVITED_REGISTERED: '邀请好友注册',
  USER_INVITED_EFFECTIVE: '有效邀请',
}

const DIMENSION_LABELS = {
  CONTENT: '内容',
  ORG: '组织',
  SPREAD: '传播',
  CITY: '城市',
}

const ACHIEVEMENT_ICONS = { BADGE: '🏅', TITLE: '🎖', CANDIDATE: '⭐' }

function eventLabel(type) { return EVENT_LABELS[type] || type }
function dimensionLabel(dim) { return DIMENSION_LABELS[dim] || dim }
function achievementIcon(type) { return ACHIEVEMENT_ICONS[type] || '🏆' }

function formatTime(value) {
  if (!value) return ''
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function formatDate(value) {
  if (!value) return ''
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

onMounted(async () => {
  loading.value = true
  try {
    const [centerRes, contribRes, achieveRes] = await Promise.allSettled([
      getGrowthCenter(),
      getGrowthContributions(20),
      getGrowthAchievements(20),
    ])
    if (centerRes.status === 'fulfilled' && centerRes.value) {
      growth.value = { ...growth.value, ...centerRes.value }
    }
    contributions.value = contribRes.status === 'fulfilled' && Array.isArray(contribRes.value)
      ? contribRes.value : []
    achievements.value = achieveRes.status === 'fulfilled' && Array.isArray(achieveRes.value)
      ? achieveRes.value : []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.growth-page {
  min-height: 100vh;
  background: var(--lc-bg);
  color: var(--lc-text);
}

.growth-head {
  display: flex;
  align-items: center;
  background: var(--lc-surface);
  border-bottom: 1px solid var(--lc-soft-alt);
  position: sticky;
  top: 0;
  z-index: 10;
}

.growth-back {
  width: 48px;
  height: 48px;
  border: 0;
  background: transparent;
  font-size: 22px;
  color: var(--lc-indigo);
  cursor: pointer;
}

.growth-title {
  margin: 0;
  font-size: 17px;
  font-weight: 800;
}

.growth-body {
  max-width: 720px;
  margin: 0 auto;
  padding: 14px 12px calc(90px + env(safe-area-inset-bottom));
}

.growth-status {
  text-align: center;
  padding: 36px 0;
  color: var(--lc-subtle);
}

.growth-card {
  background: var(--lc-surface);
  border: 1px solid var(--lc-soft-alt);
  border-radius: 14px;
  padding: 16px;
  margin-bottom: 12px;
}

.card-title {
  margin: 0 0 12px;
  font-size: 15px;
  font-weight: 700;
}

/* Identity Card */
.identity-level {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.lv-badge {
  background: linear-gradient(135deg, var(--lc-indigo), #2563eb);
  color: #fff;
  font-size: 15px;
  font-weight: 900;
  padding: 4px 14px;
  border-radius: 999px;
  white-space: nowrap;
}

.identity-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.tag {
  font-size: 12px;
  font-weight: 700;
  padding: 2px 10px;
  border-radius: 999px;
}

.tag-title {
  background: rgba(236, 72, 153, 0.1);
  color: var(--lc-pink);
  border: 1px solid rgba(236, 72, 153, 0.2);
}

.tag-badge {
  background: rgba(99, 102, 241, 0.1);
  color: var(--lc-indigo);
  border: 1px solid rgba(99, 102, 241, 0.2);
}

.identity-contrib {
  margin: 0 0 12px;
  font-size: 13px;
  color: var(--lc-subtle);
}

.identity-contrib strong {
  color: var(--lc-text);
  font-size: 20px;
  font-weight: 900;
}

.contrib-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
}

.contrib-item {
  background: var(--lc-bg);
  border-radius: 10px;
  padding: 8px;
  text-align: center;
}

.contrib-item label {
  display: block;
  color: var(--lc-subtle);
  font-size: 11px;
  margin-bottom: 4px;
}

.contrib-item strong {
  font-size: 16px;
  font-weight: 800;
}

/* Goal + Invite shared */
.progress-bar {
  height: 8px;
  background: var(--lc-bg);
  border-radius: 999px;
  overflow: hidden;
  margin-bottom: 10px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--lc-indigo), var(--lc-pink));
  border-radius: 999px;
  transition: width 0.4s ease;
}

/* Next Goal Card */
.goal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 13px;
  color: var(--lc-subtle);
}

.goal-label {
  text-align: center;
  color: var(--lc-slate);
}

.goal-label strong {
  color: var(--lc-text);
}

.goal-reward {
  margin: 0;
  font-size: 13px;
  color: var(--lc-subtle);
}

.goal-reward strong {
  color: var(--lc-violet);
}

/* Invite Milestone Card */
.invite-progress {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 10px;
}

.invite-count {
  font-size: 14px;
}

.invite-count strong {
  font-size: 22px;
  font-weight: 900;
  color: var(--lc-indigo);
}

.invite-next {
  font-size: 13px;
  color: var(--lc-subtle);
}

.invite-next strong {
  color: var(--lc-text);
}

.invite-max {
  color: var(--lc-emerald);
  font-weight: 700;
}

/* Achievement Wall */
.achievement-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.achievement-item {
  background: var(--lc-bg);
  border-radius: 10px;
  padding: 12px 8px;
  text-align: center;
}

.achievement-icon {
  font-size: 26px;
  margin-bottom: 6px;
}

.achievement-name {
  margin: 0 0 4px;
  font-size: 12px;
  font-weight: 700;
}

.achievement-time {
  margin: 0;
  font-size: 11px;
  color: var(--lc-subtle);
}

/* Contribution Log */
.contrib-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.contrib-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid var(--lc-soft);
}

.contrib-row:last-child {
  border-bottom: 0;
}

.contrib-row strong {
  font-size: 13px;
}

.contrib-row p {
  margin: 3px 0 0;
  font-size: 12px;
  color: var(--lc-subtle);
}

.contrib-delta {
  font-weight: 800;
  font-size: 15px;
  color: var(--lc-violet);
  white-space: nowrap;
}

/* Shared */
.empty-state {
  margin: 0;
  font-size: 13px;
  color: var(--lc-subtle);
  text-align: center;
  padding: 16px 0;
}
</style>
```

- [ ] **Step 2: Frontend build check**

```powershell
cd frontend; npm run build 2>&1 | Select-Object -Last 20
```

Expected: no errors, build succeeds.

---

### Task 13: Final verification

- [ ] **Step 1: Backend compile**

```powershell
cd backend; .\mvnw.cmd compile -q
```

Expected: `BUILD SUCCESS`

- [ ] **Step 2: Frontend build**

```powershell
cd frontend; npm run build 2>&1 | Select-Object -Last 30
```

Expected: `BUILD SUCCESS`

- [ ] **Step 3: Semantic guard scan**

```bash
bash scripts/semantic-guard.sh --git-hook
```

Expected: no violations.

---

## Acceptance Report Checklist

After implementation, verify:

1. **Modified files**: V66 migration, Dynamic entity, DynamicRepository, UserAchievementRepository, GrowthRewardCatalog (new), GrowthRewardService (new), UserGrowthService, GrowthBroadcastService, GrowthEngine, FellowshipInviteService, AuthController, GrowthCenterDTO, UserAchievementDTO, GrowthController, GrowthCenterPage.vue
2. **Reward rules defined**: 5 level rewards (Lv2/3/5/8/10), 4 invite milestones (1/3/5/10 people)
3. **Level reward trigger**: GrowthEngine.triggerMilestoneBroadcast → GrowthRewardService.checkAndGrantLevelReward after each level-up
4. **Invite reward trigger**: AuthController.register → growthRewardService.checkAndGrantInviteMilestoneRewards after createSuccessRecord
5. **Achievement dedup**: `existsByUserIdAndAchievementCode` + DB unique constraint on (user_id, achievement_code)
6. **Broadcast text**: human-readable text in `content`; machine marker in `marker` column; legacy fallback checks `content` column for pre-V66 records
7. **/api/growth/me new fields**: currentTitle, currentBadge, nextLevelRewardName/Type/DisplayText, inviteMilestoneProgress, nextInviteRewardName/Type/DisplayText/RequiredCount
8. **Growth center sections**: identity card (level + tags + contrib grid), next goal (progress bar + reward preview), invite milestone (count + progress), achievement wall (grid with icons), contribution log (readable labels)
9. **Empty-state user**: all sections render with sensible defaults (Lv1, 0 invites, empty achievement wall shows guidance text)
10. **npm run build**: passes
11. **mvnw compile**: passes
12. **Old flow impact**: GrowthEngine pipeline unchanged for non-level-up events; broadcast dedup backward-compatible with legacy marker-in-content records
13. **Deferred risk**: USER_INVITED_EFFECTIVE events are still PENDING (no settle mechanism exists); effective invite count uses SUCCESS relations as proxy — acceptable for P1-1

---

## Notes

- **Effective invite counting**: Uses `user_invite_relation.status = 'SUCCESS'` as proxy for "effective invite". The formal `USER_INVITED_EFFECTIVE → SETTLED` settling mechanism does not exist yet; that is a separate future task.
- **Level thresholds extended to Lv10**: Both `UserGrowthService.resolveLevel()` and `GrowthController.resolveLevel()` must be updated — they are intentionally duplicated in the current codebase.
- **Next Flyway migration**: Must be V67 after this plan (V66 is consumed here).
