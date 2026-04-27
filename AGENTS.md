# AI 任务入口手册

AI 在本项目执行任务时，先看这一页，再按类型读取对应手册的关键章节。

## 10 秒快速决策

1. 用一句话定义任务目标（改什么、影响哪里）
2. 按下表匹配任务类型，只读最少必要章节
3. 确认代码落点（文件、层级、模块）后再动代码
4. 命中以下关键词时，必须叠加专项检查：
   - **层级混用**：`fellowship`、`platform`、布局、路由前缀
   - **数据库**：新建表、新增字段、Schema 变更
   - **全局资源**：全局样式、入口文件、路由文件
5. 改完做最小验证（能跑通本次改动路径即可）

---

## 本项目两层架构（必读前提）

本项目有两套体系，**严禁混用**：

| 维度 | 平台层（网站） | 交友层（H5） |
|------|--------------|------------|
| 路由 | `/` | `/fellowship/*` |
| 布局 | 全宽，无约束 | max-width 480px，居中 |
| UI 库 | 纯 CSS | Vant 4 |
| 样式文件 | `assets/styles/platform.css` | `assets/styles/fellowship.css` |
| 组件目录 | `src/components/` | `src/components/fellowship/` |
| 页面目录 | `src/pages/platform/` | `src/pages/{功能}/` |

判断任何任务前，先确认它属于哪一层。

---

## 任务类型与必读内容

| 任务类型 | 必读位置 |
|---------|---------|
| 项目总览 / 快速定位 | `CLAUDE.md` 全文 |
| 前端页面 / 组件 / 逻辑 | `DEVGUIDE.md` §3（开发规范）、§7（代码模式）、§8（陷阱清单） |
| 样式 / 主题 / CSS 变量 | `DEVGUIDE.md` §3.6（样式规范） |
| 路由 / 导航 | `DEVGUIDE.md` §3.2（路由注册）、§3.7（路由跳转） |
| 后端 Controller / Service | `DEVGUIDE.md` §4（后端规范）、§7.3–7.5（后端模式） |
| API 联调 / 前后端对接 | `DEVGUIDE.md` §5（API 规范）、§5.3（现有 API 速查） |
| 数据库 / Schema 变更 | `DEVGUIDE.md` §6（数据库规范） |
| 本地启动 / 环境配置 | `DEVGUIDE.md` §2（本地启动）+ `CLAUDE.md` 环境文件表 |
| 新功能完整开发 | `DEVGUIDE.md` §9（Checklist 全流程） |

---

## 执行流程（必须按顺序）

1. **定义目标**：一句话说清楚改什么、为什么、影响哪层（平台层 / 交友层 / 后端 / 全局）
2. **读手册**：按上表找到对应章节，读完再落代码
3. **定位落点**：在代码中找到真实的文件、函数、模块
4. **实施修改**：遵守对应层的约定（见下方专项原则）
5. **最小验证**：至少跑通本次改动路径
6. **收尾**：只有开发流程、目录职责或关键约定发生变化时才更新文档

---

## 前端专项原则

- **两层不混**：交友层组件不进 `src/components/`（根目录），平台层不用 Vant
- **页面目录**：交友层页面放 `src/pages/{功能}/index.vue`；`src/modules/fellowship/pages/` 是旧目录，**禁止再往里加新页面**
- **路由前缀**：交友层跳转必须带 `/fellowship/` 前缀，漏掉会跳到平台层
- **认证路由**：需要登录的交友页面必须加 `meta: { requiresAuth: true }`
- **API 调用**：用 `src/api/request.js` 封装，`baseURL` 已处理环境差异，不硬编码 URL
- **响应解包**：后端有两种格式（直接返回 / 包装格式），用 `unwrapList()` 兼容
- **样式**：`<style scoped>` 无例外；颜色用 CSS 变量，不硬编码；禁止内联大段样式；禁止 Tailwind

## 后端专项原则

- **路径前缀**：Controller `@RequestMapping` 以 `/api/` 开头，**不加 `/admin`**（context-path 已处理）
- **认证**：每个需要登录的接口都必须调 `getCurrentUser(authHeader)`，返回 null 时响应 401
- **幂等写入**：写操作前先 `existsBy...` 检查，避免重复记录
- **推荐列表**：必须过滤已操作用户（调 `findActedUserIdsByFromUserId`）
- **分层**：业务逻辑在 Service，Controller 只做参数解析和响应封装
- **枚举**：实体类枚举字段用 `@Enumerated(EnumType.STRING)`，不用 ORDINAL

## 数据库专项原则

- `ddl-auto: none`，Hibernate **不自动建表改表**
- `schema-platform.sql` **不会自动执行**（`sql.init.mode: never`），仅作历史参考
- 所有 Schema 变更通过 **Flyway 迁移文件**（`db/migration/V{N}__{描述}.sql`），启动时自动执行
- Flyway 文件只跑一次，**无需幂等写法**；禁止修改已执行的文件（会破坏 checksum 校验）
- 两张 fellowship 资料表：`fellowship_profile`（用这个）、`fellowship_profiles`（旧，废弃）

---

## 高频错误（禁止）

| 错误 | 正确做法 |
|------|---------|
| 不读手册直接开改 | 先按类型读对应章节 |
| 交友层跳转少写 `/fellowship/` | `router.push('/fellowship/chat/' + id)` |
| 新页面放 `modules/fellowship/pages/` | 放 `src/pages/{功能}/index.vue` |
| Controller 路径加 `/admin` | 只写 `/api/{resource}` |
| 写操作不检查是否已存在 | 先 `existsBy...` 再 save |
| 修改已执行的 Flyway 文件 | 新建下一个版本号的迁移文件 |
| 平台层使用 Vant 组件 | 平台层用纯 CSS |
| 全局样式文件里加页面专属样式 | 放在组件 `<style scoped>` 里 |

---

## 任务完成标准（DoD）

- 代码改动与任务目标一致，未越界修改其他层
- 本次改动路径可运行（前端可访问对应页面 / 后端接口可调通）
- 涉及 DB 变更时，SQL 已确认幂等
- 涉及路由时，已确认层级前缀正确
- 交付说明包含：改了什么、为什么、如何验证、剩余风险（如有）
