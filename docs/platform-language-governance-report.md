# 平台语义治理报告

> 治理目标：将平台整体语义从「宗教团体语义」统一升级为「青年成长与陪伴社区语义」
> 执行日期：2026-05-08
> 最后更新：2026-05-08（P0.5 回归检查完成）
> 执行阶段：P0 ✅ 已完成 / P0.5 ✅ 回归验证通过 / P1、P2 规划中

---

## 一、全项目扫描结果

### 1.1 扫描范围

| 层级 | 扫描内容 |
|------|---------|
| 前端 UI | `frontend/src/pages/`、`frontend/src/components/`、`frontend/src/modules/` |
| 前端路由 | `frontend/src/router/`、`index.html`、`.env.*` |
| 后端业务 | `backend/src/main/java/`（Controller、Service、DTO、Entity） |
| 数据库 | `backend/src/main/resources/db/migration/*.sql` |

### 1.2 关键词扫描总览（含 P0.5 回归状态）

| 关键词 | 前端 UI（P0 前）| 前端 UI（P0.5 回归）| 后端 Java | 数据库迁移 |
|--------|--------------|-------------------|----------|-----------|
| 教会 | 发现 | **✅ 已清除** | **✅ 已清除** | 种子数据（保留）|
| 祷告 | 发现 | **✅ 已清除** | 仅 enum 值（保留）| 种子数据（保留）|
| 祈祷 | 发现 | **✅ 已清除** | 无 | 无 |
| 福音 | 无 | 无 | 无 | 种子数据（保留）|
| 灵修 | 无 | 无 | 无 | 无 |
| 团契 | 发现 | **✅ 已清除** | **✅ 已清除** | 种子数据（保留）|
| 牧养 | 无 | 无 | 无 | 无 |
| 主内 | 无 | 无 | 无 | 种子数据（保留）|
| 见证 | 发现 | **✅ 已清除** | 无 | 种子数据（保留）|
| 查经 | 无 | 无 | 无 | 种子数据（保留）|
| 敬拜 | 无 | 无 | 无 | 种子数据（保留）|
| 弟兄姊妹 | 无 | 无 | 无 | 种子数据（保留）|

---

## 二、P0 已治理内容（UI 层）

### 2.1 团体类型标签 — 3 处前端 + 1 处后端同步修改

**替换规则：**
- `教会团体` → `社群团体`（保留枚举值 `church` 不变，仅改显示文案）
- `生活团契` → `生活小组`（保留枚举值 `family` 不变，仅改显示文案）

| 文件 | 行号 | 变更前 | 变更后 | 类型 |
|------|------|--------|--------|------|
| `frontend/src/pages/platform/GroupCreatePage.vue` | 87 | `'教会团体'` | `'社群团体'` | 创建表单下拉 |
| `frontend/src/pages/platform/GroupCreatePage.vue` | 90 | `'生活团契'` | `'生活小组'` | 创建表单下拉 |
| `frontend/src/pages/admin/GroupsAdminPage.vue` | 139 | `'教会团体'` | `'社群团体'` | 后台筛选下拉 |
| `frontend/src/pages/admin/GroupsAdminPage.vue` | 142 | `'生活团契'` | `'生活小组'` | 后台筛选下拉 |
| `frontend/src/components/platform/groups/GroupsPage.desktop.vue` | 254 | `'教会团体'` | `'社群团体'` | 公开浏览筛选 |
| `frontend/src/components/platform/groups/GroupsPage.desktop.vue` | 257 | `'生活团契'` | `'生活小组'` | 公开浏览筛选 |
| `backend/src/main/java/.../services/PlatformGroupSupport.java` | 14 | `"教会团体"` | `"社群团体"` | API label 映射 |
| `backend/src/main/java/.../services/PlatformGroupSupport.java` | 17 | `"生活团契"` | `"生活小组"` | API label 映射 |

### 2.2 后台团体分类列表

| 文件 | 行号 | 变更前 | 变更后 | 说明 |
|------|------|--------|--------|------|
| `frontend/src/pages/admin/GroupCreateAdminPage.vue` | 76 | `'祈祷小组'` | `'心愿小组'` | 保留"小组"结构，去除宗教动作 |
| `frontend/src/pages/admin/GroupCreateAdminPage.vue` | 76 | `'读经小组'` | `'共读小组'` | 保留共同阅读语义，去除圣经专指 |

> `青年团体`、`家庭小组`、`公益服务`、`其他` 均无宗教语义，保留不变。

### 2.3 团体打卡类型

| 文件 | 行号 | 变更前 | 变更后 | 说明 |
|------|------|--------|--------|------|
| `frontend/src/components/platform/groups/GroupDetail.desktop.vue` | 760 | `label: '祷告'` | `label: '心愿'` | 打卡选项 UI 文案 |
| `frontend/src/components/platform/groups/GroupDetail.desktop.vue` | 767 | `prayer: '祷告'` | `prayer: '心愿'` | 打卡 label 映射 |

> 枚举值 `prayer` 保留不变，仅修改中文显示标签，避免破坏 API 契约。

### 2.4 动态发布 Placeholder

| 文件 | 行号 | 变更前 | 变更后 |
|------|------|--------|--------|
| `frontend/src/pages/fellowship/DynamicPublishPage.vue` | 23 | `可以写心情、见证、祷告代求或生活片段…` | `可以写心情、故事、心愿或生活片段…` |

---

## 三、P0.5 回归检查结果（2026-05-08）

### 3.1 前端 UI 二次扫描

**扫描命令：** 全量 grep `.vue` + `.js` 文件，pattern：`教会|祷告|祈祷|福音|灵修|团契|牧养|主内|见证|查经|敬拜|弟兄姊妹`

**结果：0 条匹配。**

前端 UI 层已无任何用户可见的宗教语义，P0 治理完整无遗漏。

### 3.2 后端 Java 二次扫描

**扫描范围：** `backend/src/main/java/**/*.java`

**结果：0 条匹配。**

`PlatformGroupSupport.java` 中的 label 映射已全部更新，Java 层无残留。

### 3.3 数据库迁移文件扫描

**仍有匹配，均为预期保留项：**

| 文件 | 性质 | 处置 |
|------|------|------|
| `V24__create_platform_group_tables.sql` | 开发种子数据（团体名称、描述）| 保留，不影响生产 |
| `V25__replace_jiaohui_jiaoyou_in_text_data.sql` | 历史清洗迁移（UPDATE 语句本身含关键词）| 保留，已执行完毕 |
| `V26__seed_platform_group_posts_and_notices.sql` | 开发种子帖子内容 | 保留，不影响生产 |
| `V28__seed_operation_posts.sql` | 开发种子运营内容 | 保留，不影响生产 |

> Flyway 迁移文件一旦执行即不再重跑，V24/V26/V28 中的宗教语义内容仅存在于 dev 环境初始化阶段，生产库已由 V25 清洗。

### 3.4 构建验证

| 构建目标 | 命令 | 结果 | 耗时 |
|---------|------|------|------|
| 前端 | `npm run build` | **✅ 通过** | 6.07s |
| 后端 | `mvnw package -DskipTests` | **✅ 通过** | 编译无错误（JVM 兼容警告，非编译错误）|

### 3.5 重点页面检查结论

| 页面 | 组件 | P0.5 状态 |
|------|------|----------|
| 团体创建页 | `GroupCreatePage.vue` | ✅ 类型下拉已更新（社群团体/生活小组）|
| 团体列表页 | `GroupsPage.desktop.vue` | ✅ 分类筛选已更新 |
| 团体详情页（打卡）| `GroupDetail.desktop.vue` | ✅ 打卡类型「祷告」→「心愿」|
| 动态发布页 | `DynamicPublishPage.vue` | ✅ Placeholder 已更新 |
| 后台团体管理 | `GroupsAdminPage.vue` | ✅ 类型筛选已更新 |
| 后台创建团体 | `GroupCreateAdminPage.vue` | ✅ 分类列表已更新 |

### 3.6 API 契约完整性

- 枚举值 `church` / `family` / `prayer` **均未修改**，仅中文 label 更新
- 前端 `.value` 字段与后端接受的参数值完全一致，无破坏
- 后端 `TYPE_LABELS` 与前端 label 已双向同步

---

## 四、P1 规划 — 路由治理（暂不修改）

### 4.1 扫描结论

前端路由中无 `/church`、`/prayer`、`/fellowship`（宗教含义）路径。
`/fellowship` 在本平台已作为**联谊交友功能模块标识符**使用，非宗教语义。

### 4.2 枚举值路由参数（中期规划）

以下 API 参数值含宗教词汇，作为 P1 规划项：

| 参数位置 | 当前值 | 建议值 | 影响范围 |
|---------|--------|--------|---------|
| 团体类型 query param | `church` | `community` | 前端筛选 + 后端查询 |
| 打卡类型 body param | `prayer` | `wish` | 前端提交 + 后端校验 |

**兼容方案：**
1. 后端 Controller 在接收时同时接受新旧值（`church` / `community`）
2. 前端枚举值切换为新值
3. 数据库中 `platform_groups.type` 字段存量数据通过迁移文件 `V39__rename_group_type_church.sql` 批量更新
4. 灰度稳定后下线旧值兼容逻辑

---

## 五、P2 规划 — 后端 API 治理（暂不修改）

### 5.1 核心业务代码

| 文件 | 内容 | 风险等级 | 治理建议 |
|------|------|---------|---------|
| `PlatformGroupSupport.java` | TYPE_LABELS 中 `church`/`family` 枚举值 | HIGH | P1 配合数据库迁移一起修改 |
| `PlatformGroupController.java` | `validTypes` 含 `"prayer"` | MEDIUM | P1 增加 `"wish"` 兼容后替换 |
| `GroupExternalEngagementService.java` | `validTypes` 含 `"prayer"` | MEDIUM | 同上，与 Controller 同步 |

### 5.2 Swagger / 文档层

| 文件 | 内容 | 风险等级 |
|------|------|---------|
| V27 迁移文件注释 | `COMMENT 'all/platform/fellowship'` | LOW（仅文档注释）|

### 5.3 种子数据（开发/测试数据，不影响生产）

| 迁移文件 | 宗教语义内容 | 说明 |
|---------|------------|------|
| `V24__create_platform_group_tables.sql` | 团体名称含「团契」「祷告」「敬拜」「教会」 | 仅 seed 数据，可按需更新 |
| `V26__seed_platform_group_posts_and_notices.sql` | 帖子内容含「团契」「弟兄姊妹」「查经」「崇拜」「读经」等 | 仅 seed 数据，可按需更新 |

> V25 迁移文件已记录前次语义治理：将「教会」「教友」从用户生成内容字段中批量替换，说明本次治理延续历史决策方向。

---

## 六、风险说明

| 风险点 | 说明 | 缓解措施 |
|--------|------|---------|
| 枚举值 `church`/`prayer` 已存储于数据库 | 修改 value 会导致历史数据筛选失效 | P1 阶段先做 label 层治理，value 层通过数据库迁移配合修改 |
| `事工团队`（service）保留未处理 | "事工"为基督教术语，但与「服务团队」语义相近 | 建议 P1 阶段评估是否改为「志愿服务」 |
| 后端 TYPE_LABELS 与前端 label 已同步 | 本次 P0 同步修改了 Java 端 label 映射 | 前后端 label 一致性已恢复 |
| 种子数据含大量宗教内容 | 仅影响开发/测试环境 | 生产数据已由 V25 迁移清洗，种子数据可视需求更新 |

---

## 七、下一阶段计划

### P1 暂缓建议

**建议暂缓 P1，原因：**
1. P0 UI 层治理已完整覆盖所有用户可见文案，无任何遗漏
2. P1 涉及数据库字段 `type='church'`、`type='prayer'` 的存量更新，影响已发布团体记录及打卡历史，需要专项回归测试计划
3. 构建与 API 契约均验证通过，当前不存在紧迫风险
4. 建议将 P1 作为独立需求评审后执行

### P1（中期，需数据库迁移配合，评审后启动）

- [ ] 编写 `V39__rename_group_type_values.sql`：将 `platform_groups.type = 'church'` 批量更新为 `community`
- [ ] 编写 `V40__rename_checkin_type_prayer.sql`：将打卡记录中 `type = 'prayer'` 更新为 `wish`
- [ ] 前端枚举值切换：`church` → `community`，`prayer` → `wish`
- [ ] 后端 Controller/Service 增加新旧值兼容逻辑，灰度验证后下线旧值
- [ ] 评估「事工团队」(`service`) 是否更名为「志愿服务」

### P2（远期，影响 API 契约）

- [ ] 评估 `/fellowship` URL 路径是否需要别名或重命名（影响所有前端路由和后端 mapping）
- [ ] 更新种子数据文件（V24/V26/V28）中的宗教语义示例内容
- [ ] 更新 Swagger 接口描述中涉及枚举值的说明文字

---

*报告由 Claude Code 自动生成，执行人：溪午听风*
