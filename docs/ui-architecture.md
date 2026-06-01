# UI 架构规范（PC / Mobile 分离版）

> 建立时间：2026-04-30 · Phase 1 已完成

---

## 一、目标

- PC 与移动端 UI 完全解耦
- 避免响应式混写导致样式污染
- 支持未来迁移小程序 / App
- 降低多人开发误改风险

---

## 二、目录规范

```
src/
├── pages/
│   ├── pc/                   # PC 专属页面
│   └── mobile/               # 移动端专属页面
├── components/
│   ├── pc/                   # PC 专属组件
│   └── mobile/               # 移动端专属组件
└── layouts/
    ├── PcLayout.vue          # PC 布局（sticky header + footer，无移动端导航）
    └── MobileLayout.vue      # 移动端布局（max-width 480px wrapper）
```

旧目录继续保留，不改动：

```
src/pages/platform/           # 旧路由页面（保留）
src/pages/fellowship/         # 旧路由页面（保留）
src/layouts/PlatformLayout.vue
src/layouts/FellowshipLayout.vue
```

---

## 三、路由规范

**PC 路由**（新，灰度入口）：

```
/pc/platform
/pc/platform/me
/pc/events
```

**Mobile 路由**（新，灰度入口）：

```
/m/platform
/m/platform/me
/m/fellowship/me
```

**旧路由（永久保留，不改，不删，不重定向）**：

```
/platform
/me
/fellowship/me
```

路由注册文件：

- `src/router/modules/pc.routes.js` — 只加 PC 新路由
- `src/router/modules/mobile.routes.js` — 只加 Mobile 新路由
- **不要修改** `platform.routes.js` / `fellowship.routes.js`

---

## 四、核心规则（强制）

### 1. UI 隔离

❌ 禁止：PC 页面引用 mobile 组件（`AppTabBar`、`van-*` 等）

❌ 禁止：mobile 页面引用 PC 组件（`DesktopDashboard` 等）

❌ 禁止：在同一个页面写 PC + mobile 双端 UI

❌ 禁止：再写 `@media` 同时适配 PC 和 mobile

### 2. 修改边界

- **页面文件**：只负责结构组装，不写业务逻辑
- **组件文件**：负责具体 UI 实现
- **样式**：只改当前页面 / 组件的 `<style scoped>`

❌ 禁止跨页面修改样式

❌ 禁止顺手重构无关页面

### 3. 代码变更规则

**修改前** 必须输出：

- 修改文件清单
- 修改目的
- 是否影响旧路由

**修改后** 必须输出：

- 新页面访问路径
- 是否影响现有功能
- 是否存在样式污染

### 4. 复用层（允许共用）

以下内容 PC 和移动端**继续共用，禁止因分离而复制**：

- `src/api/` — 所有接口
- `src/stores/` — Pinia stores
- `src/composables/` — 组合式函数
- `src/utils/` — 工具函数
- 权限逻辑（`router/index.js` beforeEach）
- 登录态

---

## 五、迁移策略

**阶段策略**：

| 阶段 | 内容 | 状态 |
|------|------|------|
| Phase 1 | 结构分离（布局、路由、核心页面） | ✅ 已完成 |
| Phase 2 | 核心页面迁移（逐页推进） | 🔄 进行中 |
| Phase 3 | 旧页面逐步废弃 | 🔲 最后再做 |

**移动端优先原则（2026-06）：** 新玩法默认先落地 `/m/*` 与 `/fellowship/*`（Vant H5）；PC 走 `/pc/*` 或平台层桌面布局。用户主路径以手机为准。

**Phase 2 已完成 / 进行中清单**：

| 新路由 | 页面文件 | 状态 |
|--------|---------|------|
| `/pc/platform` | `pages/pc/platform/HomePage.vue` | ✅ |
| `/pc/platform/me` | `pages/pc/platform/MePage.vue` | ✅ |
| `/m/fellowship/me` | `pages/mobile/fellowship/MePage.vue` | ✅ |
| `/m/fellowship/discover` … `match` … `messages` | 复用 fellowship 页面 + MobileLayout | ✅ |
| `/m/platform` | `pages/mobile/platform/HubPage.vue` | ✅ |
| `/m/platform/tasks` | `pages/mobile/platform/MyTasksPage.vue` + `MyTasksPanel` | ✅ |
| `/m/platform/checkin` | `pages/mobile/platform/CheckinPage.vue` + `CheckinPanel` | ✅ |
| `/m/platform/content` | 复用 `PlatformContentPage.vue`（任务跳转） | ✅ |
| `/m/platform/positive-share` | `pages/mobile/platform/PositiveSharePage.vue` | ✅ |
| `/m/platform/help` | `pages/mobile/platform/HelpSquarePage.vue` | ✅ |
| `/m/platform/help/:id` `create` `my` | 复用 `Help*Page.vue` + `usePlatformPath` | ✅ |
| `/m/platform/messages` | `pages/mobile/platform/MessagesPage.vue`（壳 + 复用消息中心） | ✅ |
| `/pc/platform/play` | `pages/pc/platform/PlayHubPage.vue` | ✅ |
| `/m/platform/groups` | `pages/mobile/platform/GroupsPage.vue` | ✅ |
| `/m/platform/my-groups` | `pages/mobile/platform/MyGroupsPage.vue`（壳 + 复用 MyGroupsPage） | ✅ |
| `/m/platform/groups/:id` 等子路由 | `GroupDetailMobile.vue`（含动态评论、成员审核、管理员角色、赛季榜、打卡榜） | ✅ |
| `/m/platform/local` | `pages/mobile/platform/LocalServicesPage.vue` | ✅ |
| `/m/platform/topics` | `pages/mobile/platform/TopicsPage.vue` | ✅ |
| `/m/platform/messages` | 复用 `MessagesCenterPage.vue` | ✅ |
| `/m/platform/member` | `pages/mobile/platform/MemberPage.vue` | ✅ |
| `/m/fellowship/ai-tools` | `pages/mobile/fellowship/AiToolsPage.vue` | ✅ |
| `/pc/events` `/pc/events/:id` | `EventsPage.vue` + `EventDetailPage.vue`（`eventsPath` 自适应） | ✅ |
| `/pc/platform/tasks` `checkin` `help` `groups` … | PC 玩法子路由 + `pcPaths` / `usePlatformPath` 内链统一 | ✅ |

---

## 六、样式规范

**必须**：

- 使用 `<style scoped>`，禁止全局污染
- PC 页面不写 mobile 断点（`max-width: 560px` 以下禁止）
- Mobile 页面不写 PC 断点（`min-width: 1024px` 以上禁止）

**推荐断点**：

- PC 适配平板：`@media (max-width: 980px)`
- Mobile 适配小屏：`@media (max-width: 420px)`

**宽度基准**：

- PC：`width: min(100% - 64px, 1720px)`
- Mobile：`width: min(100%, 480px)`（`MobileLayout` 约束外层）

---

## 七、开发流程（必须遵守）

1. 选择要迁移的页面（1~2 个）
2. 输出修改计划（文件清单 + 目的 + 旧路由影响）
3. 拆分 PC / mobile 页面文件
4. 挂载新路由（只改 `pc.routes.js` 或 `mobile.routes.js`）
5. 验证新旧页面（`npm run build` 零错误 + 浏览器访问）
6. 输出结果（访问路径 + 污染检查 + 旧路由回归）

---

## 八、快速检查清单

开发前 / 后必须确认：

- [ ] 是否修改了不相关文件
- [ ] 是否影响旧页面（git diff 旧文件应为空）
- [ ] 是否混入另一端 UI（PC 混 mobile 或反之）
- [ ] 是否写了双端 media（同一文件同时有 PC + mobile 断点）
- [ ] 是否破坏组件边界（PC 引 van-*，mobile 引 DesktopDashboard）
- [ ] `npm run build` 是否零错误

---

## 九、常见错误（必须避免）

❌ 在 PC 页面写 `.van-*`（移动端 Vant 组件）

❌ 在 mobile 页面写大屏 grid / dashboard

❌ 一个页面写两套 UI（`.me-mobile` + `.me-desktop` 并存）

❌ 为了方便直接改旧页面而非新建分离文件

❌ 分离 UI 时顺手改接口 / store / 权限逻辑

---

## 十、说明

本规范优先级高于个人开发习惯。

违反规范的代码必须重构。
