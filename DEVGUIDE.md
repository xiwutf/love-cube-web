# Love Cube 开发手册

> **开发任何功能前必须先读此手册。** 架构细节参见 `CLAUDE.md`，本手册聚焦实操规范、代码模式与已知陷阱。

---

## 目录

1. [项目架构速览](#1-项目架构速览)
2. [本地启动](#2-本地启动)
3. [前端开发规范](#3-前端开发规范)
4. [后端开发规范](#4-后端开发规范)
5. [API 对接规范](#5-api-对接规范)
6. [数据库规范](#6-数据库规范)
7. [标准代码模式](#7-标准代码模式)
8. [已知陷阱 / 避坑清单](#8-已知陷阱--避坑清单)
9. [新功能开发 Checklist](#9-新功能开发-checklist)
10. [通知系统接入规范](#10-通知系统接入规范)

---

## 1. 项目架构速览

```
前端 Vue 3 (Vite, port 5173)
  ├── 平台层 /           → src/pages/platform/ + src/pages/WebsiteHome.vue
  └── 交友层 /fellowship → src/pages/ (auth required, max-width 480px, Vant 4)

后端 Spring Boot (port 8090, context-path: /admin)
  └── 所有 API: /admin/api/**
```

**两套体系，不要混用：**

| 维度 | 平台层（网站） | 交友层（H5） |
|------|--------------|------------|
| 路由前缀 | `/` | `/fellowship/` |
| 布局 | 全宽 PlatformLayout | max 480px FellowshipLayout |
| UI 库 | 纯 CSS（.platform-* 工具类） | Vant 4 |
| 全局样式 | `assets/styles/platform.css` | `assets/styles/fellowship.css` |
| 共用基础 | `assets/styles/tokens.css`（变量）+ `utilities.css`（u-* 原子类） | 同左 |
| 组件目录 | `src/components/` | `src/components/fellowship/` |

---

## 2. 本地启动

```bash
# 前端
cd frontend && npm install && npm run dev   # http://localhost:5173

# 后端
cd backend && mvnw.cmd spring-boot:run     # http://localhost:8090
```

后端连接 **Aliyun RDS**，无需本地 DB。Redis 已禁用（`store-type: none`）。

### 2.1 编码与字符集规范（强制）

- 所有项目文件统一使用 **UTF-8（无 BOM）** 编码。
- 禁止使用 **ANSI / GBK** 等非 UTF-8 编码保存源码、配置和脚本文件。
- 中文注释必须可正常显示，提交前需确认无乱码。
- 前后端接口涉及中文返回时，必须确保字符集正确，避免出现乱码。
- 数据库字段字符集统一支持 **utf8mb4**，新增表/字段时必须遵循该要求。

---

## 3. 前端开发规范

### 3.1 文件放置规则

| 类型 | 放置位置 | 说明 |
|------|---------|------|
| 交友页面 | `src/pages/{功能}/index.vue` | 路由在 `src/router/index.js` 中注册 |
| 平台页面 | `src/pages/platform/{Name}Page.vue` | PascalCase + Page 后缀 |
| 交友专用组件 | `src/components/fellowship/` | 仅交友层使用 |
| 通用组件 | `src/components/` | 两层共用 |
| API 文件 | `src/api/{功能}.js` | 一个功能一个文件 |
| Pinia Store | `src/stores/{功能}.js` | 交友 store ID 前缀 `fellowship-` |
| 工具函数 | `src/utils/` | 通用；复制一份到 `src/modules/fellowship/utils/` 如需 @f 引用 |

> `src/modules/fellowship/pages/` 已于 2026-05-01 删除，不再存在。新联谊页面一律放 `src/pages/{功能}/index.vue`。

### 3.2 路由注册

```js
// src/router/index.js — fellowship 路由区域
{ path: 'your-page', component: () => import('@/pages/your-page/index.vue'), ...auth }
```

- 需要登录的页面必须加 `...auth`（即 `meta: { requiresAuth: true }`）
- 公开页面（login/welcome）不加 auth

### 3.3 Composition API 规范

**全部使用 `<script setup>`，无例外。**

```vue
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
// ...
</script>
```

### 3.4 API 调用规范

`request.js` baseURL = `${VITE_API_BASE_URL}/api`

- 开发环境：`/admin/api`（Vite proxy → `http://localhost:8090/admin/api`）
- 生产环境：`http://xifg.com.cn:8090/admin/api`

**调用写法：**

```js
// src/api/match.js
import request from './request.js'

// GET 请求
export async function getMatchList(params = {}) {
  return request.get('/matches/list', { params })
}

// POST 请求
export async function likeUser(userId) {
  return request.post(`/interactions/like/${userId}`)
}
```

**返回值处理 — 后端响应有两种格式：**

```js
// 格式1: 直接返回数组/对象（如 InteractionController）
// res 本身就是数据

// 格式2: 包装格式（如 MatchController）
// { success: true, data: [...] }

// 统一用 unwrapList 处理列表：
function unwrapList(res) {
  if (Array.isArray(res)) return res
  if (Array.isArray(res?.data)) return res.data
  return []
}
```

### 3.5 错误处理规范

```js
// 页面内标准模式
const loading = ref(false)
const data = ref([])

async function load() {
  loading.value = true
  try {
    data.value = await fetchSomething()
  } catch (e) {
    showToast({ message: e.message || '加载失败', type: 'fail' })
  } finally {
    loading.value = false
  }
}
```

- `request.js` 已统一处理 401（跳转登录），**不要在业务代码里重复处理 401**
- 失败提示用 Vant 的 `showToast`

### 3.6 样式规范

#### 样式池结构

所有全局样式集中在 `frontend/src/assets/styles/`，由 `index.css` 统一引入：

```
assets/styles/
  index.css        → 入口，按顺序 @import 以下所有文件
  tokens.css       → 所有 CSS 变量（颜色、间距、字号、阴影、圆角、z-index）
  base.css         → 全局 reset + html/body/#app
  utilities.css    → u-* 原子工具类（flex、gap、margin-top、文字、圆角等）
  vant-theme.css   → Vant 4 组件颜色覆盖
  fellowship.css   → 交友层移动端壳 (.fellowship-shell / .fellowship-layout)
  platform.css     → 平台层工具类（.platform-* / .module-* / .detail-*）
  admin.css        → 管理后台工具类（.admin-* 表格/按钮/标签/表单）
```

#### 工作流：先查池，再定义，再使用

```
需要某个样式
  ↓
1. 查 tokens.css — 有对应 CSS 变量吗？（--lc-blue / --lc-space-4 / --lc-shadow …）
  ↓ 有 → 直接 var(--lc-*)
2. 查 utilities.css — 有对应原子类吗？（u-flex / u-mt-4 / u-truncate …）
  ↓ 有 → 直接用 class
3. 查层级文件 — 有对应组合类吗？（.platform-card / .admin-btn / .module-grid …）
  ↓ 有 → 直接用 class
4. 都没有 → 在对应文件中定义后再使用（token → tokens.css，原子 → utilities.css，
            平台组合 → platform.css，管理组合 → admin.css），禁止直接写魔法值
```

#### UI 库

| 层 | 使用 |
|----|------|
| 交友层（H5） | **Vant 4**（已配置，直接用 `<van-*>` 组件） |
| 平台层（桌面） | 纯 CSS 工具类（`.platform-*`） |
| 管理后台 | 纯 CSS 工具类（`.admin-*`） |

#### 使用示例

```vue
<template>
  <!-- ✅ 正确：用池里的类 -->
  <div class="platform-card u-flex u-gap-4">
    <h2 class="platform-section-title">标题</h2>
    <button class="platform-btn platform-btn-primary">操作</button>
  </div>

  <!-- ✅ 正确：scoped 内只写无法复用的布局细节，颜色用 token -->
  <div class="banner">内容</div>
</template>

<style scoped>
.banner {
  background: linear-gradient(135deg, var(--lc-blue-mid), var(--lc-blue));
  border-radius: var(--lc-radius-lg);
  padding: var(--lc-space-8);
}
</style>
```

#### 禁止行为

```vue
<style scoped>
/* ❌ 硬编码颜色 */
color: #2563EB;          /* 改为 var(--lc-blue) */
background: #F8FAFC;     /* 改为 var(--lc-bg) */

/* ❌ 硬编码间距魔法值 */
margin-top: 20px;        /* 改为 var(--lc-space-5) 或 class="u-mt-5" */
gap: 16px;               /* 改为 var(--lc-space-4) 或 class="u-gap-4" */

/* ❌ 没有 scoped（全局污染） */
</style>

<!-- ❌ 引入 Tailwind 或其他未集成的 CSS 框架 -->
```

#### Token 速查

```css
/* ── 白 / 表面 ─────────────────────────── */
--lc-surface          /* #FFFFFF 纯白，卡片/模态底色 */
--lc-bg               /* #F8FAFC 页面底色 */
--lc-soft             /* #F1F5F9 轻灰底色 */
--lc-soft-alt         /* #EEF0F4 略深的灰蓝底 */
--lc-border           /* #E2E8F0 通用边框 */

/* ── 文字 ────────────────────────────── */
--lc-text             /* #0F172A 主文本 */
--lc-text-deep        /* #1E293B 深文本（slate-800） */
--lc-slate            /* #334155 次深文本（slate-700） */
--lc-muted            /* #475569 弱文本（slate-600） */
--lc-muted-light      /* #64748B 更弱文本（slate-500） */
--lc-subtle           /* #94A3B8 占位/禁用文本 */

/* ── 品牌蓝 ──────────────────────────── */
--lc-blue             /* #2563EB 平台主色 */
--lc-blue-mid         /* #1D4ED8 悬停/深一档 */
--lc-blue-dark        /* #1E3A8A 深蓝 */
--lc-blue-light       /* #EFF6FF 蓝色浅底 */
--lc-blue-border      /* #BFDBFE 蓝色边框 */

/* ── 靛蓝 / 紫罗兰（特色区块） ────────── */
--lc-indigo           /* #4F46E5 靛蓝 */
--lc-indigo-light     /* #EEF2FF 靛蓝浅底 */
--lc-indigo-soft      /* #F1EFFF 极浅紫底 */
--lc-violet           /* #6D5DFB 紫罗兰 */
--lc-purple           /* #7C3AED 深紫 */

/* ── 橙色 ────────────────────────────── */
--lc-orange           /* #F97316 橙色 */
--lc-orange-light     /* #FFF7ED 橙色浅底 */

/* ── 翡翠绿（次级成功色） ───────────── */
--lc-emerald          /* #059669 翡翠绿 */
--lc-emerald-light    /* #F0FDF4 翡翠浅底 */

/* ── 行动粉（平台/后台 CTA） ─────────── */
--lc-rose             /* #F54878 玫红行动色 */

/* ── 联谊粉（仅联谊层） ─────────────── */
--lc-pink             /* #EC4899 */
--lc-pink-light       /* #FCE7F3 */
--lc-pink-border      /* #FBCFE8 */

/* ── 状态色 ──────────────────────────── */
--lc-green / --lc-green-light
--lc-red   / --lc-red-light
--lc-amber / --lc-amber-light

/* ── 间距（4 的倍数） ─────────────────── */
--lc-space-1(4px) -2(8px) -3(12px) -4(16px) -5(20px)
--lc-space-6(24px) -8(32px) -10(40px) -12(48px) -16(64px) -20(80px)

/* ── 字号 ────────────────────────────── */
--lc-text-xs(11px) -sm(13px) -base(14px) -md(16px) -lg(18px) -xl(20px)

/* ── 圆角 ────────────────────────────── */
--lc-radius-xs(6px) -sm(10px) -(16px) -lg(20px)

/* ── 阴影 ────────────────────────────── */
--lc-shadow-sm  --lc-shadow  --lc-shadow-lg  --lc-shadow-blue

/* ── 其他 ────────────────────────────── */
--lc-transition                              /* all .25s ease */
--lc-z-dropdown -sticky -overlay -modal -toast
```

### 3.7 路由跳转

```js
// 交友层页面跳转必须带 /fellowship/ 前缀
router.push('/fellowship/user-profile/' + userId)
router.push('/fellowship/chat/' + userId)
router.push('/fellowship/messages')

// 错误写法（会跳到平台层）
router.push('/user-profile/' + userId)   // ❌
router.push('/chat/' + userId)           // ❌
```

### 3.8 页面 UI 改动约束（强制）

后续所有页面 UI 改动必须遵守以下规则：

1. 禁止在单个大型 Vue 页面中堆叠全部 UI。
2. 页面文件仅负责 layout 组装，不承载复杂 UI 细节。
3. 每个页面必须拆分为多个业务区块组件。
4. PC 区块组件统一放在 `components/pc/`。
5. 移动端区块组件统一放在 `components/mobile/`。
6. 样式集中放到对应页面的独立 CSS 文件，不在页面内分散维护。
7. 修改某个区块时，只允许改该区块组件和其对应样式文件。
8. 禁止顺手重构无关页面或无关区块。
9. 禁止改 API、store、路由，除非任务明确要求。
10. 每次改动前，必须先列出将修改的文件清单并确认范围。

---

## 4. 后端开发规范

### 4.1 Controller 规范

```java
@RestController
@RequestMapping("/api/{resource}")   // 不要加 /admin 前缀，context-path 已处理
public class XxxController {

    @Autowired
    private XxxService xxxService;

    @GetMapping("/list")
    public ResponseEntity<?> getList(@RequestHeader("Authorization") String authHeader) {
        try {
            User currentUser = getCurrentUser(authHeader);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "用户认证失败"));
            }
            // 业务逻辑委托给 Service
            var result = xxxService.getList(currentUser.getUserid());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "操作失败: " + e.getMessage()));
        }
    }

    // 所有需要登录的 Controller 都要有这个私有方法
    private User getCurrentUser(String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
            String token = authHeader.substring(7);
            String openid = JwtUtil.getOpenIdFromToken(token);
            if (openid == null) return null;
            return userRepository.findByOpenid(openid);
        } catch (Exception e) {
            return null;
        }
    }
}
```

### 4.2 响应格式约定

**列表接口（简单）：** 直接返回 `List<Map>` 或实体列表
```java
return ResponseEntity.ok(list);   // 前端直接用 res
```

**带状态的操作接口：** 返回包含 message 和业务字段的 Map
```java
return ResponseEntity.ok(Map.of(
    "message", "操作成功",
    "isLiked", true,
    "matched", false
));
```

**带分页的列表接口：** 用包装格式
```java
Map<String, Object> response = new HashMap<>();
response.put("success", true);
response.put("data", list);
response.put("total", total);
return ResponseEntity.ok(response);
```

**错误响应：** 统一用 `Map.of("message", "错误描述")`，HTTP 状态码要正确设置（400/401/403/404/500）。

### 4.3 Service 层规范

```java
@Service
@Transactional   // 写操作加在类上，只读方法可加 @Transactional(readOnly = true)
public class XxxService {

    // 写操作要保证幂等性 — 操作前先检查是否已存在
    public void likeUser(Long fromUserId, Long toUserId) {
        boolean exists = repo.existsByFromUserIdAndToUserId(fromUserId, toUserId);
        if (exists) return;   // 幂等返回，不报错
        // ... 执行写入
    }
}
```

### 4.4 Repository 规范

优先用 Spring Data JPA 命名查询，复杂 SQL 用 `@Query`：

```java
// 简单查询 — 命名方式
List<User> findByGenderAndUseridNot(Integer gender, Long userId);
boolean existsByFromUserIdAndToUserIdAndInteractionType(...);

// 复杂查询 — JPQL
@Query("SELECT DISTINCT ui.toUserId FROM UserInteraction ui WHERE ui.fromUserId = :userId " +
       "AND ui.interactionType IN ('LIKE', 'SUPER_LIKE', 'SKIP')")
List<Long> findActedUserIdsByFromUserId(@Param("userId") Long userId);

// 原生 SQL（只在无法用 JPQL 时使用）
@Query(value = "SELECT * FROM users ORDER BY RAND() LIMIT :limit", nativeQuery = true)
List<User> findRandomUsers(@Param("limit") int limit);
```

### 4.5 实体类规范

```java
@Data
@Entity
@Table(name = "your_table")
public class YourEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "snake_case_column", nullable = false)
    private String fieldName;

    @Enumerated(EnumType.STRING)   // 枚举存字符串，不存数字
    private YourEnum status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

---

## 5. API 对接规范

### 5.1 URL 完整路径

```
开发：http://localhost:5173 → (Vite proxy) → http://localhost:8090
生产：https://xifg.com.cn → (Nginx) → http://localhost:8090

前端 baseURL:  /admin/api   (dev)  |  http://xifg.com.cn:8090/admin/api  (prod)
后端 context:  /admin
后端 controller: /api/{resource}
完整后端 URL:  http://localhost:8090/admin/api/{resource}/{action}
```

**示例：**
```
前端调用:  request.get('/matches/list')
实际请求:  GET /admin/api/matches/list
后端匹配:  MatchController @RequestMapping("/api/matches") + @GetMapping("/list")
```

### 5.2 认证

所有需要登录的接口，后端必须验证 `Authorization: Bearer {token}` header。

```java
// Controller 内标准写法
User currentUser = getCurrentUser(authHeader);  // 见 4.1
if (currentUser == null) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "用户认证失败"));
}
```

前端 `request.js` 已自动注入 token，业务代码无需手动处理。

### 5.3 现有 API 速查

| 功能 | 方法 | 路径 |
|------|------|------|
| 获取推荐列表 | GET | `/matches/list?gender=1` |
| 筛选匹配 | POST | `/matches/filter` |
| 喜欢用户 (toggle) | POST | `/interactions/like/{userId}` |
| 超级喜欢 | POST | `/interactions/superlike/{userId}` |
| 跳过用户 | POST | `/interactions/dislike/{userId}` |
| 查询喜欢状态 | GET | `/interactions/like-status/{userId}` |
| 我发出的喜欢 | GET | `/interactions/likes/sent` |
| 互相喜欢列表 | GET | `/interactions/likes/mutual` |
| 聊天记录 | GET | `/chat/history/{userId}/{receiverId}` |
| 发送消息 | POST | `/chat/send` |
| 聊天伙伴列表 | GET | `/partners/{userId}` |
| 删除聊天 | DELETE | `/chat/delete/{userId}/{receiverId}` |
| 我的联谊资料 | GET | `/fellowship/profile/me` |
| 更新联谊资料 | PUT | `/fellowship/profile/me` |
| 资料完成度 | GET | `/fellowship/profile/completion` |
| 登录 | POST | `/auth/login` |
| 注册 | POST | `/auth/register` |

---

## 6. 数据库规范

### 6.1 核心原则

- `ddl-auto: none` — Hibernate **不自动建表/改表**
- `sql.init.mode: never` — `schema-platform.sql` **不会在启动时自动执行**，仅作历史参考
- 所有 schema 变更通过 **Flyway 迁移文件**管理，每次启动自动执行未跑过的版本
- Flyway 基线版本为 V13（已存在的生产库以此为起点）

### 6.2 新增字段 / 新建表

**在 `backend/src/main/resources/db/migration/` 下新建迁移文件，命名规则：`V{下一个版本号}__{简短描述}.sql`**

当前最高版本为 **V38**，下一个迁移文件应为 `V39__description.sql`。

> **已知空缺**：V15 从未创建，数据库从 V14 直接跳至 V16。`out-of-order` 默认关闭，**禁止补建 V15**，否则 Flyway 启动会报错。

```sql
-- 示例：V39__add_user_tags.sql

-- 新建表
CREATE TABLE user_tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    tag VARCHAR(64) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
);

-- 新增列
ALTER TABLE users ADD COLUMN tag_ids VARCHAR(255) NULL;
```

> Flyway 迁移文件只执行一次，**不需要幂等写法**（IF NOT EXISTS / 条件 ALTER）。如果迁移失败需回滚，删除 `flyway_schema_history` 中对应记录后修正文件再重启。

### 6.3 现有主要表

| 表名 | 用途 | 关键字段 |
|------|------|---------|
| `users` | 用户账号 | userid, openid, phone_number, role, invite_code |
| `user_interactions` | 所有互动 | from_user_id, to_user_id, interaction_type (LIKE/SUPER_LIKE/SKIP/FOLLOW/COMMENT/GIFT) |
| `match_records` | 推荐历史 | user_id, matched_user_id, match_score |
| `chat_messages` | 聊天消息 | sender_id, receiver_id, content, is_read |
| `fellowship_profile` | 联谊资料 | user_id, nickname, gender, age, city, avatar_url |
| `fellowship_profiles` | 联谊资料（旧） | 同上，逐步废弃 |
| `announcements` | 公告 | title, status, publish_date |
| `invite_record` | 邀请记录 | invite_code, inviter_user_id, invitee_user_id |

### 6.4 枚举值（interaction_type）

```
LIKE        - 普通喜欢（toggle，重复调用 = 取消）
SUPER_LIKE  - 超级喜欢（幂等，不重复创建）
SKIP        - 跳过/不感兴趣（幂等）
FOLLOW      - 关注（toggle）
COMMENT     - 评论
GIFT        - 礼物
```

---

## 7. 标准代码模式

### 7.1 前端：列表页标准模式

```vue
<template>
  <div class="page">
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="noMore"
                finished-text="没有更多了" @load="load">
        <div v-for="item in list" :key="item.id" class="item" @click="goDetail(item)">
          <!-- 内容 -->
        </div>
        <van-empty v-if="!loading && !list.length" description="暂无数据" />
      </van-list>
    </van-pull-refresh>
    <AppTabBar />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import AppTabBar from '@/components/AppTabBar.vue'
import { fetchList } from '@/api/xxx.js'

const router = useRouter()
const list = ref([])
const loading = ref(false)
const noMore = ref(false)
const refreshing = ref(false)
let page = 0
const PAGE_SIZE = 20

async function load() {
  if (loading.value || noMore.value) return
  loading.value = true
  try {
    const items = await fetchList({ page, size: PAGE_SIZE })
    list.value.push(...items)
    if (items.length < PAGE_SIZE) noMore.value = true
    else page++
  } catch (e) {
    showToast({ message: e.message || '加载失败', type: 'fail' })
  } finally {
    loading.value = false
  }
}

async function onRefresh() {
  page = 0
  list.value = []
  noMore.value = false
  await load()
  refreshing.value = false
}

function goDetail(item) {
  router.push('/fellowship/xxx/' + item.id)
}
</script>
```

### 7.2 前端：toggle 操作（喜欢/关注）

```js
// 乐观更新 + 失败回滚
async function toggleLike(item) {
  const prev = item.isLiked
  item.isLiked = !prev          // 乐观更新
  try {
    const res = await likeUser(item.userId)
    item.isLiked = res.isLiked  // 以服务器返回为准
    if (res.matched) {
      showMatchedDialog(item.userId)
    }
  } catch {
    item.isLiked = prev         // 失败回滚
  }
}
```

### 7.3 后端：互动操作标准模式（幂等 toggle）

```java
@PostMapping("/like/{userId}")
public ResponseEntity<?> like(@PathVariable Long userId,
                              @RequestHeader("Authorization") String authHeader) {
    User me = getCurrentUser(authHeader);
    if (me == null) return unauth();

    boolean isLiked = service.isLiked(me.getUserid(), userId, userId);
    if (isLiked) {
        service.unlikeUser(me.getUserid(), userId);
        return ok(Map.of("isLiked", false, "matched", false));
    }
    service.likeUser(me.getUserid(), userId);
    boolean matched = service.checkMutualLike(me.getUserid(), userId);
    return ok(Map.of("isLiked", true, "matched", matched, "matchedUserId", matched ? userId : null));
}
```

### 7.4 后端：推荐列表过滤模式

推荐类列表**必须**排除已操作过的用户：

```java
public List<User> getRecommendations(Long currentUserId) {
    // 1. 拿到已操作 ID 列表
    List<Long> actedIds = userInteractionRepository.findActedUserIdsByFromUserId(currentUserId);

    // 2. 过滤
    return userRepository.findAll().stream()
        .filter(u -> !u.getUserid().equals(currentUserId))
        .filter(u -> !actedIds.contains(u.getUserid()))
        .collect(Collectors.toList());
}
```

### 7.5 后端：幂等写入模式

```java
// 写入前先检查是否存在，避免重复记录
public void createRecord(Long userId, Long targetId) {
    boolean exists = repo.existsByUserIdAndTargetId(userId, targetId);
    if (exists) return;   // 幂等返回
    // ... 执行写入
}
```

---

## 8. 已知陷阱 / 避坑清单

### 前端陷阱

| 陷阱 | 错误做法 | 正确做法 |
|------|---------|---------|
| 路由前缀缺失 | `router.push('/chat/' + id)` | `router.push('/fellowship/chat/' + id)` |
| 新页面放错目录 | 放在已删除的 `modules/fellowship/pages/` | 放在 `src/pages/{功能}/index.vue` |
| superlike 复用 like | `request.post('/interactions/like/' + id)` | `request.post('/interactions/superlike/' + id)` |
| 本地模拟接口 | `return { matched: false }` | 调真实后端接口 |
| 喜欢状态刷新丢失 | 只用 `ref(false)` 本地记录 | 页面加载时调 `getLikeStatus()` |
| 响应解包不一致 | 直接用 `res.data` | 用 `unwrapList()` 兼容两种格式 |

### 后端陷阱

| 陷阱 | 错误做法 | 正确做法 |
|------|---------|---------|
| 推荐列表不过滤 | `findByUseridNot(currentUserId)` | 再过滤 `actedIds` |
| 重复写入 MatchRecord | `saveAll(allMatches)` | 先 check exists 再 save |
| 非幂等写操作 | 直接 save 不检查 | 先 `existsBy...` |
| like 不返回 matched | `Map.of("isLiked", true)` | 加 `matched` 和 `matchedUserId` 字段 |
| 多余的 URL 前缀 | `@RequestMapping("/admin/api/matches")` | `@RequestMapping("/api/matches")` |
| 消息中心绕规范 | Controller / 业务 Service 直接写 `user_notifications` 或 `push_status` | 见 [§10](#10-通知系统接入规范)：创建走 `NotificationService`，微信与 `push_status` 走 `NotificationDispatchService` |

### 数据库陷阱

| 陷阱 | 说明 |
|------|------|
| 直接改表结构 | `ddl-auto: none`，Hibernate 不自动修改，需手写 SQL |
| 修改已执行的 Flyway 文件 | Flyway 会校验 checksum，改已执行文件会报错；应新建下一个版本号的文件 |
| 两张 fellowship 资料表 | `fellowship_profile`（新，用这个）和 `fellowship_profiles`（旧，逐步废弃） |
| EnumType.ORDINAL | 枚举必须用 `@Enumerated(EnumType.STRING)`，不用 ORDINAL（数字随枚举顺序变化而错乱） |

---

## 9. 新功能开发 Checklist

### 开始前

- [ ] 确认功能属于**平台层**还是**交友层**
- [ ] 在此手册中查找是否有现成 API 可复用（见 5.3）
- [ ] 确认需要新建哪些表/字段，新建 Flyway 迁移文件 `db/migration/V{N}__{描述}.sql`

### 前端

- [ ] 页面文件放在 `src/pages/` 正确子目录
- [ ] UI 改动前先列出本次将修改的文件清单
- [ ] 路由在 `router/index.js` 中注册，需要认证的加 `...auth`
- [ ] API 文件放在 `src/api/`，用 `request.js` 封装，不硬编码 URL
- [ ] 交友层跳转用 `/fellowship/` 前缀
- [ ] 所有 style `scoped`，无 Tailwind；颜色/间距用 `var(--lc-*)` 变量，不硬编码
- [ ] 样式优先从池中选取（tokens → utilities → platform/admin 类），新增通用样式写入对应池文件
- [ ] 列表用 `van-list` + `van-pull-refresh` 实现分页和下拉刷新
- [ ] 操作失败有 `showToast` 提示
- [ ] 涉及 like/skip 等操作：调真实接口，处理 `matched` 返回值

### 后端

- [ ] Controller 路径以 `/api/` 开头（不加 `/admin`）
- [ ] 每个需要认证的接口都调 `getCurrentUser()` 验证 token
- [ ] 业务逻辑在 Service 层，Controller 只做参数解析和响应封装
- [ ] 写操作保证幂等（先 check exists）
- [ ] 推荐类接口过滤已操作用户（调 `findActedUserIdsByFromUserId`）
- [ ] 匹配成功时响应包含 `matched` 字段
- [ ] 若涉及用户消息中心 / 业务通知：遵守 [§10 通知系统接入规范](#10-通知系统接入规范)（`NotificationService` 统一持久化；`NotificationDispatchService` 仅内存计算微信占位与 `push_status`；Controller 不写 `user_notifications`）

### 联调

- [ ] Swagger UI 确认接口路径：`http://localhost:8090/admin/swagger-ui.html`
- [ ] 前端请求路径 = `VITE_API_BASE_URL(/admin)` + `/api` + Controller mapping
- [ ] 检查响应格式，根据是否包装调整 `unwrapList` 或直接使用

### 提交前

- [ ] 无遗留 `console.log`
- [ ] 无硬编码的用户 ID / token / URL
- [ ] 无本地模拟的假接口（`return { matched: false }` 类）
- [ ] 已新建 Flyway 迁移文件，文件名版本号不重复，未修改已执行文件

---

## 10. 通知系统接入规范

> 本节约束**用户消息中心**（`user_notifications` / `user_notification_settings`）与微信占位推送的接入方式。违反本节视为架构违规，Code Review 应打回。

### 10.1 创建入口（强制）

1. **所有业务通知必须通过 `NotificationService` 创建**（或该类对外暴露的专用方法，如 `notifyXxx`）。业务层禁止绕过 `NotificationService` 自行拼装通知实体并持久化。
2. **`Controller` 不允许直接写 `user_notifications` 表**（禁止在 Controller 中注入 `UserNotificationRepository` 等并 `save` 通知行；查询类接口除外时仍应委托给专门 Service，且不得承担「造通知」职责）。

### 10.2 微信与推送状态（强制）

3. **微信推送占位与 `push_status` 取值**只允许由 `NotificationDispatchService` 在内存中计算（如 `applyPushInMemory`）；**对 `user_notifications` 的写入**（含首次 `save` 与已读等更新）统一由 `NotificationService` 调用 `UserNotificationRepository` 完成。业务 Service、Controller 不得直连微信 SDK、HTTP 客户端调用微信开放平台接口，也不得在业务分支里自行更新 `push_channel` / `push_status`。
4. **`PROFILE_VIEWED`（浏览/访客类）通知禁止走实时微信推送**：仅允许站内送达；dispatch 层不得为其产生「已发微信」类语义（如占位 MOCK 发往微信）。
5. **在真实微信公众号接入完成之前，禁止调用任何外部微信 API**（含模板消息、订阅消息、客服消息等）。当前仅允许站内通知 + 库内字段预留与占位逻辑。

### 10.3 默认开关策略（与 `NotificationCatalog` 对齐）

6. **互动类通知**：默认**仅站内开启**；**微信侧默认关闭**（用户可在设置中自行打开，且需已绑定微信）。
7. **重要类通知**：默认**站内开启**；**微信侧默认开启（预留）**——表示产品默认意图为「可推送」；实际是否发出仍受绑定状态、`NotificationDispatchService` 决策与公众号接入进度约束。

### 10.4 新增类型与业务接入文档

8. **新增通知类型必须先在 `NotificationCatalog`（`com.lovecube.backend.notification.NotificationCatalog`）中补全**：分级（重要 / 互动 / 系统）、默认站内/微信开关、Tab 归类等；禁止在业务代码里散落「魔法字符串」且无目录定义。
9. **每新增一处业务接入**，在 PR / 设计说明中必须写清：
   - **触发时机**（何种事件、同步还是异步、是否幂等）；
   - **接收人**（单用户、多用户、广播范围）；
   - **`related_type` / `related_id`**（与前端跳转、去重、审计一致）；
   - **`link_url`**（站内打开路径约定，与联谊层 `/fellowship/` 等路由一致）。

### 10.5 禁止项小结

10. **禁止在业务代码中写死 `push_status`**（例如硬编码 `MOCK_SENT`、`PENDING`）。推送结果只能由消息保存后的统一 dispatch 流程写入。

| ❌ 禁止 | ✅ 正确 |
|--------|--------|
| Controller / 任意业务 Service 内 `save(userNotification)` 造数据 | 调用 `NotificationService` 的创建/通知方法 |
| 业务类里 `wechatClient.send...` 或更新 `push_status` | `NotificationDispatchService` 只做内存侧决策；落库仍只经 `NotificationService` |
| 新增类型只写枚举或常量、不更新 `NotificationCatalog` | 先补目录，再写业务触发 |
| 未接入公众号即调用外部微信 API | 仅站内 + 预留字段；接入后集中改 dispatch 层 |

---

*最后更新：2026-05-04*
