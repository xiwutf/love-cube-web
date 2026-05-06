# API 与数据字段治理规范

本文档记录 Love Cube 项目中**同一业务含义、多套字段名**的治理结论与防回退规则。详细变更批次见 Git 历史与 Flyway `V54__field_governance_backfill_avatar_and_owner.sql`。

## 1. 命名原则（防回退）

1. **一个业务含义只允许一个主字段名**（JSON API 与对内 DTO 对外暴露时以本文档「最终字段」为准）。
2. **新增字段前**必须在本文档与 `users` / `fellowship_*` 等核心表上检索是否已有同义列或同义 API 键。
3. **数据库旧列**在过渡期保留；Java 实体上旧属性用 JavaDoc `@deprecated` 标明「仅兼容读取 / 同步用」。
4. **删除列或删除 API 键**前必须：Flyway 数据迁移已完成、全仓库 `grep` 无引用、前端 `npm run build` 与后端 `mvn test` 通过。

## 2. 重复字段清单与统一规则

### 2.1 用户头像 URL（跨 users.profile_photo、fellowship_profiles.avatar、fellowship_profile.avatar_url）

| 项目 | 说明 |
|------|------|
| 业务含义 | 用户对外展示的头像图片 URL |
| 旧字段 / 别名 | API 曾混用 `avatar`、`profilePhoto`；库表列 `users.profile_photo`、`fellowship_profiles.avatar`、`fellowship_profile.avatar_url` |
| 新字段 / 最终保留（API） | **`avatarUrl`** |
| 数据库 | 上述列均保留；写入业务以统一 Profile 服务为准；`V54` 在两端头像为空时互填 legacy 与 main 表 |
| 后端 | `UnifiedProfileService` 合并结果对内统一为 `avatarUrl`；对外 JSON 以 `avatarUrl` 为主；`profilePhoto` 仅作过渡期与 `users` 字段对齐的别名（同值） |
| 前端 | 使用 `getAvatar()` / `userAvatarUrlFromApi()`，优先读取 `avatarUrl` |

### 2.2 团体动态流中的「团体封面」误用 `avatarUrl`

| 项目 | 说明 |
|------|------|
| 业务含义 | 团体在动态卡片上的封面图（来自 `plat_groups.cover_url`） |
| 旧字段 | `GET .../feed` 曾错误使用键名 **`avatarUrl`** 存放封面 |
| 最终保留（API） | **`coverUrl`**（与 `PlatformGroupSupport`、其它团体摘要一致） |
| 兼容 | 前端 `normalizeGroupFeedCover(item)` 使用 `item.coverUrl \|\| item.avatarUrl` |

### 2.3 团体拥有者用户 ID（platform_groups）

| 项目 | 说明 |
|------|------|
| 业务含义 | 团体拥有者 |
| 旧字段 | `created_by`（创建者，历史上与拥有者一致） |
| 新字段 | **`owner_user_id` / API `ownerUserId`** |
| 最终保留 | **`ownerUserId`**；`created_by` 列保留；查询时若 `ownerUserId` 为空可 fallback `createdBy`（仅兼容层，见 Service） |
| 迁移 | `V33` 与 **`V54`** 幂等回填 `owner_user_id` |

### 2.4 群成员 / 管理员头像（JSON 键）

| 项目 | 说明 |
|------|------|
| 业务含义 | 成员或管理员用户头像 URL |
| 旧字段 | `avatar` |
| 最终保留 | **`avatarUrl`**（与 2.1 一致） |

### 2.5 动态 / 帖子作者头像

| 项目 | 说明 |
|------|------|
| 业务含义 | 发帖人头像 URL |
| 旧字段 | `authorAvatar` |
| 最终保留 | **`authorAvatarUrl`**（避免与全局 `avatarUrl` 语义混淆时仍保持「URL」后缀一致） |

### 2.6 Plat 团体摘要中的拥有者头像

| 项目 | 说明 |
|------|------|
| 业务含义 | 拥有者用户头像 URL |
| 旧字段 | `ownerAvatar` |
| 最终保留 | **`ownerAvatarUrl`** |

### 2.7 聊天列表项头像

| 项目 | 说明 |
|------|------|
| 业务含义 | 会话对端用户头像 URL |
| 旧字段 | `avatar` |
| 最终保留 | **`avatarUrl`** |

### 2.8 其它已识别但本次未改库表的重复（仅记录）

| 组 | 说明 |
|----|------|
| `userId` / `userid` / `id` | `User` 模型与历史 JSON 曾混用；新代码统一优先 **`userId`**（长整型），`userid` 仅兼容旧客户端 |
| `nickname` / `username` | 展示昵称与账号名不同语义；列表展示以 **`nickname`** 为主，账号字段 **`username`** |
| `groupId`（String）与 `PlatGroup.id`（Long） | 两套团体体系（`platform_groups` vs `plat_groups`），**非同一语义**，禁止强行合并键名 |
| `status` / `state` | 按各业务实体文档使用，未在本次统一改名 |

## 3. 何时可删除旧字段

- **数据库列**：待全站读写均走新列、生产数据回填完成且观察 1～2 个版本无回滚需求后，再发 **新 Flyway** `DROP COLUMN`（禁止改历史迁移文件）。
- **API 键**：待监控与前端无 `avatar` / `authorAvatar` 等旧键依赖后，在 CHANGELOG 声明破坏性变更再移除。

## 4. 相关代码入口

- 头像合并与联谊资料：`UnifiedProfileService`
- 团体 Plat 摘要：`PlatformGroupSupport`
- 前端头像解析：`frontend/src/utils/image.js`、`frontend/src/utils/displayFields.js`
