# 更新日志

本文件记录项目的重要功能更新与问题修复。

## 2026-04-29

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
