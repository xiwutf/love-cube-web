# 更新日志

本文件记录项目的重要功能更新与问题修复，供开发与运维查阅。

## 维护说明

| 渠道 | 位置 | 受众 |
|------|------|------|
| 开发者日志 | 本文件 `CHANGELOG.md` | 开发、运维、AI 协作 |
| 用户可见日志 | `/#/platform/changelog` | 平台用户 |
| 后台更新记录 | 管理后台「首页配置」→ 后台更新记录 | 管理员（**不对用户 API 暴露**） |
| 运营配置 | 管理后台「首页配置」→ 用户更新日志 | 运营维护用户向文案 |

**发版时请同步三处（按内容归类）：**

1. 在本文件追加版本章节（含影响范围、技术细节）。
2. **用户能感知的功能/体验**：管理后台「用户更新日志」，或 Flyway 写入 `home_configs`（`config_group=changelog`）；字段：`version`、`title`、`date`、`detail`（用户可读要点，换行分隔）。
3. **仅后台/技术变更**（表结构、投递、定时任务等）：管理后台「后台更新记录」，或 Flyway 写入 `config_group=changelog_admin`；公开接口 `GET /home/config` **不会**返回该组数据。

---

## v1.6.0（2026-06-12）

### 新功能

- **我的活动中心**：新增 `/#/platform/my-activities`，聚合平台活动报名、Space 团体活动投稿与待办（签到/互评），账户中心与 Help Hub 风格入口。
- **活动详情全生命周期**：`EventDetailPage` 支持报名、签到码签到、结束后互评提交与查看。
- **活动提醒**：`ActivityReminderService` + 定时任务，活动开始前 24h/2h 及结束后互评窗口提醒；`activity_reminder_log` 去重（V87）。
- **活动状态统一展示**：`activityStatus.js` + 活动列表状态标签。

### 修复

- 个人工作台（`DesktopDashboard`）恢复邀请二维码（`InviteQrCode`），与邀请码文字、复制并存。

### 影响范围

- 后端：`ActivityHubController`、`UserActivityHubService`、`ActivityReminderService`、`ActivityReminderScheduler`、`NotificationCatalog`（提醒类型）、`V87__activity_reminder_log.sql`
- 前端：`MyActivitiesPage.vue`、`EventDetailPage.vue`、`activityStatus.js`、`myActivities.js`、`DesktopDashboard.vue`、`platform.routes.js`、`AccountCenterPage.vue`

---

## v1.5.0（2026-06-11）

### 新功能

- **通知渠道偏好**：用户可按业务类型配置站内、邮件、PushPlus 渠道开关；`user_notification_channel_prefs` 表（V86）。
- **通知投递记录**：`notification_dispatch_records` 记录外发状态，便于排查。
- **通知设置页**：`MyNotificationChannelSettingsPage` 及多处入口链接。

### 影响范围

- 后端：`NotificationChannelPrefController`、`UserNotificationChannelPrefService`、`NotificationDispatchService`、`NotificationService`、`V86__notification_channel_dispatch.sql`
- 前端：`MyNotificationChannelSettingsPage.vue`、`notificationChannelPrefs.js`、`notification-settings/index.vue` 等入口

---

## v1.4.0（2026-04-29）

### 修复与优化

- 优化平台首页移动端展示，重排首页模块顺序并调整 Hero、公告、官方入口、联谊横幅、精选内容等区块的间距、字号与卡片布局，提升小屏可读性与操作体验。
- 修复团体申请入群流程：审核制团体现在强制填写申请验证信息，新增长度校验，避免空申请或超长内容进入审核队列。
- 补全团体成员数据返回字段，在平台端与管理端展示申请说明与申请时间，便于管理员审核与用户查看申请状态。
- 修复交友实名认证输入校验：证件后四位改为更严格格式校验（支持末位 `X`），并统一输入过滤逻辑与提交提示文案。
- 修复交友资料编辑页提交流程：提交按钮在保存中禁用，保存失败可提示具体原因，保存成功后主动刷新资料与完成度，避免页面状态不同步。
- 修复退出登录后的交友资料残留问题，登出时同步清空 `fellowshipProfile` 状态，避免旧数据在下次会话中短暂闪现。

### 影响范围

- 前端（平台层）：`frontend/src/pages/WebsiteHome.vue` 与首页相关组件、`PlatformLayout`。
- 前端（交友层）：`frontend/src/pages/fellowship/VerifyPage.vue`、资料页与 `fellowshipProfile`/`user` 状态管理。
- 前端（管理端）：`frontend/src/pages/admin/GroupEditAdminPage.vue`。
- 后端：`backend/src/main/java/com/lovecube/backend/controllers/PlatformGroupController.java`。
