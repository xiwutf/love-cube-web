# Love Cube 玩法全量开发路线图

> **For agentic workers:** 按 Phase 顺序逐批交付，每批完成后可独立验收。Steps 使用 checkbox 跟踪。

**Goal:** 按优先级顺序实现平台/联谊/团体全套玩法增强，形成「注册→签到→成长→团体→联谊→邀请」闭环。

**Architecture:** 优先复用现有 GrowthEngine、团体 engagement、联谊 interaction 基础设施；新能力通过 Flyway 增量迁移 + Service 层扩展，平台层纯 CSS、联谊层 Vant。

**Tech Stack:** Spring Boot 3 / JPA / Flyway (next: V70+); Vue 3 `<script setup>`

---

## Phase 1 — 核心闭环（P0）

| # | 功能 | 状态 |
|---|------|------|
| 1.1 | 有效邀请结算 (`USER_INVITED_EFFECTIVE`) | ✅ 已完成 |
| 1.2 | 连续签到 streak 奖励 | ✅ 已完成 |
| 1.3 | 团体赛季排行榜 | ✅ 已完成 |
| 1.4 | 联谊破冰话题（匹配后） | ✅ 已完成 |

## Phase 2 — 轻量玩法（P1）

| # | 功能 | 状态 |
|---|------|------|
| 2.1 | 每日/每周挑战任务 | ✅ 已完成 |
| 2.2 | 新人 7 日任务包 | ✅ 已完成 |
| 2.3 | 心声 streak + 每日话题 | ✅ 已完成 |
| 2.4 | 互助信用分 / 达人榜 | ✅ 已完成 |
| 2.5 | 内容共创周榜 | ✅ 已完成 |

## Phase 3 — 联谊深化（P1）

| # | 功能 | 状态 |
|---|------|------|
| 3.1 | 默契测试（价值观匹配度） | ✅ 已完成 |
| 3.2 | VIP 权益深化（谁看过我、无限滑卡） | ✅ 已完成 |
| 3.3 | 活动签到码 + 活动后互评 | ✅ 已完成 |

## Phase 4 — 平台扩展（P2）

| # | 功能 | 状态 |
|---|------|------|
| 4.1 | 本地服务模块（招聘/二手/租房） | ✅ 已完成 |
| 4.2 | AI 工具（资料润色、破冰话术） | ✅ 已完成 |
| 4.3 | 兴趣话题广场 | ✅ 已完成 |
| 4.4 | 统一消息中心补全 | ✅ 已完成 |
| 4.5 | 平台会员 / 订单体系 | ✅ 已完成（演示开通） |

---

## Phase 1.1 — 有效邀请结算

**有效邀请定义:** 被邀请人累计登录 ≥3 天（每次登录/签到自动检测结算）。

**Files:**
- Create: `V70__invite_effective_login_streak.sql`
- Create: `InviteEffectiveSettleService.java`
- Create: `LoginStreakService.java` + `UserLoginStreak.java`
- Modify: `FellowshipInviteService`, `GrowthEventRepository`, `AuthController`, `GrowthController`, `FellowshipProfileController`
- Modify: `InvitePage.vue`, `CheckinPage.vue`

---

## Phase 1.2 — 连续签到 Streak

**规则:** 连续登录 3/7/14/30 天发放一次性 EXP 奖励（bizId 去重）。

---

## Phase 1.3 — 团体赛季

**规则:** 季度赛季，积分 = 打卡×2 + 任务×5 + 活动报名×10；团体详情页展示赛季榜。

---

## Phase 1.4 — 破冰话题

**规则:** 双向匹配成功后展示 3 道趣味题，双方回答后可查看对方答案。
