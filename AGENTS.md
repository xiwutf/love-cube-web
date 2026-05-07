# AI 开发入口手册

> **任何 AI 接到任务后，必须先读完本文件，再读对应规范，最后才能动代码。**
> 本文件是所有开发规范的分发枢纽。

---

## 第一步：任务分类（必须做，不可跳过）

拿到任务后，先用一句话回答：**"这次改的是什么、在哪一层、是新建还是修改？"**

然后按以下决策树找到自己的位置：

```
任务进来
  │
  ├─ 涉及数据库（新增表、字段、迁移）？
  │    └─ 是 → 读 DEVGUIDE.md §6（数据库规范）
  │
  ├─ 涉及后端（Controller / Service / Repository）？
  │    └─ 是 → 读 DEVGUIDE.md §4 §5（后端规范 + API 规范）
  │         若涉及用户消息 / 通知 / 微信占位推送 → 额外读 DEVGUIDE.md §10（通知系统接入规范）
  │
  └─ 涉及前端 UI？
       │
       ├─ 路由在 /pc/* 或 /m/*（新分离架构）？
       │    └─ 是 → 读 docs/ui-architecture.md（UI 分离规范）⬅ 最重要
       │
       ├─ 路由在 /fellowship/*（联谊 H5）？
       │    └─ 是 → 读 DEVGUIDE.md §3（前端规范）+ 联谊层原则（见本文第三节）
       │
       ├─ 路由在 /admin/*（管理后台）？
       │    └─ 是 → 读 DEVGUIDE.md §3（前端规范）+ 管理层原则（见本文第三节）
       │
       └─ 路由在 / 或 /platform/*（平台网站）？
            └─ 是 → 读 DEVGUIDE.md §3（前端规范）+ 平台层原则（见本文第三节）
```

分类后，再判断任务性质：

| 任务性质 | 额外必读 |
|---------|---------|
| 全新功能（从无到有） | `DEVGUIDE.md §9`（新功能开发 Checklist） |
| 修改现有功能 | `DEVGUIDE.md §8`（已知陷阱 / 避坑清单） |
| UI 迁移（旧路由 → 新 /pc/ /m/ 路由） | `docs/ui-architecture.md` 迁移流程章节 |
| Bug 修复 | 最小改动原则，不触碰无关文件 |

---

## 第二步：了解三层架构（必读前提）

本项目有三套体系，**严禁混用**：

| 层级 | 路由前缀 | 布局组件 | UI 规范 | 页面目录 |
|------|---------|---------|---------|---------|
| 平台层（网站） | `/`、`/platform/*` | `PlatformLayout.vue` | 纯 CSS，禁止 Vant | `src/pages/platform/` |
| 联谊层（H5） | `/fellowship/*` | `FellowshipLayout.vue` | Vant 4，max-width 480px | `src/pages/{功能}/` |
| 管理后台 | `/admin/*` | `AdminLayout.vue` | 纯 CSS + Vant toast | `src/pages/admin/` |

**UI 分离新架构**（叠加在上方，独立路由前缀）：

| 层级 | 路由前缀 | 布局组件 | 页面目录 | 规范文档 |
|------|---------|---------|---------|---------|
| PC 专属 | `/pc/*` | `PcLayout.vue` | `src/pages/pc/` | `docs/ui-architecture.md` |
| 移动端专属 | `/m/*` | `MobileLayout.vue` | `src/pages/mobile/` | `docs/ui-architecture.md` |

旧路由（`/`、`/fellowship/*`、`/me` 等）永远保留，不删除，不重定向。

---

## 第三步：各层专项原则

### 平台层

- 页面放 `src/pages/platform/{Name}Page.vue`
- 只用纯 CSS，禁止引入 Vant 任何组件
- 样式写 `<style scoped>`，CSS 变量来自 `assets/styles/tokens.css`
- 全局样式加到 `assets/styles/platform.css`，不加到组件里

### 联谊层

- 页面放 `src/pages/{功能}/index.vue`（`modules/fellowship/pages/` 已于 2026-05-01 删除）
- 使用 Vant 4；跳转必须带 `/fellowship/` 前缀，否则会跳到平台层
- 需要登录的路由必须加 `meta: { requiresAuth: true }`
- 联谊层 store ID 必须加 `fellowship-` 前缀

### 管理后台

- 新增管理页面后，必须同时注册到 `AdminLayout.vue` 的 `navItems` 和 `sectionMap`
- Controller 用 `adminAuthService.requireAdmin(authHeader)` 做权限校验
- 路由加 `meta: { requiresAdmin: true, permission: '权限码' }`

### PC / Mobile 新架构（`/pc/*`、`/m/*`）

**在动代码前，必须读 `docs/ui-architecture.md`。以下是最关键的禁止项：**

- ❌ 禁止 PC 页面写 `@media (max-width: 560px)` 以下断点
- ❌ 禁止 Mobile 页面写 `@media (min-width: 1024px)` 以上断点
- ❌ 禁止 PC 页面引用 `van-*` 组件或 `AppTabBar`
- ❌ 禁止 Mobile 页面引用 `DesktopDashboard`
- ❌ 禁止在同一页面同时写 PC + mobile 双端 UI
- ❌ 禁止修改旧路由文件（`platform.routes.js`、`fellowship.routes.js`）
- 新路由只加到 `pc.routes.js` 或 `mobile.routes.js`

---

## 第四步：开始前必须输出的内容（强制）

**在写任何代码之前**，必须先输出：

```
【任务分类】
- 层级：平台层 / 联谊层 / 管理后台 / PC新架构 / Mobile新架构 / 后端 / 数据库
- 类型：新功能 / 修改现有 / UI迁移 / Bug修复
- 涉及文件清单：（列出将要创建或修改的每个文件）
- 是否影响旧路由：是 / 否
- 对应规范：（列出将要遵循的规范章节）
```

如果影响旧路由，必须说明如何保证旧路由不受影响。

---

## 第五步：开发规范速查表

| 需求 | 对应文档 | 章节 |
|------|---------|------|
| 项目全貌、命令、环境变量 | `CLAUDE.md` | 全文 |
| 前端文件放置、路由注册、样式规范 | `DEVGUIDE.md` | §3 |
| 后端 Controller / Service 写法 | `DEVGUIDE.md` | §4 |
| API 规范、响应格式、前后端对接 | `DEVGUIDE.md` | §5 |
| 数据库、Flyway 迁移文件 | `DEVGUIDE.md` | §6 |
| 标准代码模式（Vue 组件、API、Store） | `DEVGUIDE.md` | §7 |
| 已知陷阱、高频错误 | `DEVGUIDE.md` | §8 |
| 新功能完整开发流程 Checklist | `DEVGUIDE.md` | §9 |
| 用户消息中心 / 业务通知 / 微信占位推送接入 | `DEVGUIDE.md` | §10 |
| PC / Mobile UI 分离规范 | `docs/ui-architecture.md` | 全文 |

---

## 第六步：完成后必须输出的内容（强制）

**在交付结果时**，必须输出：

```
【验收报告】
- 修改了哪些文件（按创建/修改/未动分类）
- 新页面访问路径（如有）
- 是否影响旧路由（用 git diff 验证旧文件无变动）
- 是否存在样式污染（PC/mobile 是否串了）
- 构建结果：npm run build 是否零错误
- 遗留风险（如有）
```

---

## 第七步：给任何 AI 的启动话术（可直接复制）

把下面整段发给任意 AI，作为每次开发前的第一条消息：

```text
你现在是本项目开发代理。开始编码前必须严格执行以下流程，未完成前禁止修改代码：

1) 先阅读文档（按顺序）：
- AGENTS.md（入口与决策树）
- CLAUDE.md（项目架构与命令）
- DEVGUIDE.md（具体开发规范）
- 若任务涉及 /pc/* 或 /m/*，必须额外阅读 docs/ui-architecture.md

2) 阅读后先输出【任务分类】，格式必须完全一致：
【任务分类】
- 层级：平台层 / 联谊层 / 管理后台 / PC新架构 / Mobile新架构 / 后端 / 数据库
- 类型：新功能 / 修改现有 / UI迁移 / Bug修复
- 涉及文件清单：（列出将要创建或修改的每个文件）
- 是否影响旧路由：是 / 否
- 对应规范：（列出将要遵循的规范章节）

3) 若未输出【任务分类】或内容不完整，停止执行并继续补全，不允许开始改代码。

4) 开发时执行“最小改动原则”：
- 只改任务清单中的文件，不顺手改无关文件
- 不改已执行的 Flyway 迁移文件
- 不混用平台/联谊/管理/PC/Mobile 的 UI 规范

5) 完成后必须输出【验收报告】，格式必须完全一致：
【验收报告】
- 修改了哪些文件（按创建/修改/未动分类）
- 新页面访问路径（如有）
- 是否影响旧路由（用 git diff 验证旧文件无变动）
- 是否存在样式污染（PC/mobile 是否串了）
- 构建结果：npm run build 是否零错误
- 遗留风险（如有）

如果以上任一步做不到，先报告阻塞点，再等待我确认，不要自行跳过流程。
```

---

## 第八步：AI 自检执行循环（防止“说了但没做”）

每次任务都按这个循环：

1. **先判定再开发**：先出 `【任务分类】`，再动代码。  
2. **边做边对照**：每次新增改动前，检查是否仍在“涉及文件清单”内。  
3. **超范围先停下**：如果发现要改清单外文件，先更新分类并说明原因，再继续。  
4. **交付前复核**：对照 `【验收报告】` 六项逐条确认后再提交结果。  
5. **无法验证就标风险**：例如无法本地验证时，必须写入“遗留风险”。  

---

## 高频错误速查（禁止）

| ❌ 错误 | ✅ 正确做法 |
|--------|-----------|
| 拿到任务直接动代码 | 先完成第四步输出分类和文件清单 |
| 联谊层跳转不带 `/fellowship/` 前缀 | `router.push('/fellowship/chat/' + id)` |
| 新联谊页面放已删除的 `modules/fellowship/pages/` | 放 `src/pages/{功能}/index.vue` |
| 平台层用 Vant 组件 | 平台层只用纯 CSS |
| 后端 Controller 路径加 `/admin` 前缀 | 只写 `/api/{resource}`，context-path 已处理 |
| 修改已执行的 Flyway 迁移文件 | 新建 `V{N+1}__description.sql` |
| PC 页面写移动端断点 | PC 只写 `max-width: 980px` 平板适配 |
| Mobile 页面引 DesktopDashboard | Mobile 页面只用 mobile 组件 |
| 顺手修改无关文件 | 只改任务清单里的文件 |
| 旧路由页面直接修改做 UI 分离 | 新建文件放 `pc/` 或 `mobile/`，旧文件不动 |
| 业务里直接写 `user_notifications` 或 `push_status`、或 Controller 造通知 | 只通过 `NotificationService` 创建；微信与 `push_status` 只由 `NotificationDispatchService` 处理（见 `DEVGUIDE.md` §10） |

---

## 文档体系说明

```
AGENTS.md          ← 你现在在这里。所有 AI 的入口，先读这个
CLAUDE.md          ← 项目全貌（架构、命令、环境）
DEVGUIDE.md        ← 详细开发规范（前端/后端/DB/代码模式；§10 通知系统接入）
docs/
  ui-architecture.md  ← PC/Mobile UI 分离规范（新架构专用）
CHANGELOG.md       ← 版本记录
README.md          ← 项目介绍
```


<claude-mem-context>
# Memory Context

# claude-mem status

This project has no memory yet. The current session will seed it; subsequent sessions will receive auto-injected context for relevant past work.

Memory injection starts on your second session in a project.

`/learn-codebase` is available if the user wants to front-load the entire repo into memory in a single pass (~5 minutes on a typical repo, optional). Otherwise memory builds passively as work happens.

Live activity: http://localhost:37777
How it works: `/how-it-works`

This message disappears once the first observation lands.
</claude-mem-context>