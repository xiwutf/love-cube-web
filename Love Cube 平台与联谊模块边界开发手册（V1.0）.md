# Love Cube 项目现状总结与架构梳理（基于当前代码）

> 文档更新时间：2026-04-27  
> 依据：当前仓库 `frontend/` 与 `backend/` 真实代码、路由、页面、组件、API 文件

## 1. 项目基础信息

### 1.1 前端技术栈
- 核心：`Vue 3` + `Vite` + `Vue Router 4` + `Pinia`（`frontend/package.json`、`frontend/src/main.js`）
- UI：全站引入 `Vant 4`，即使平台层也可直接使用（`frontend/src/main.js`）
- 请求：`Axios` 统一封装，`baseURL = VITE_API_BASE_URL + '/api'`（`frontend/src/api/request.js`）
- 样式：`tokens.css + platform.css + fellowship.css + admin.css`（`frontend/src/assets/styles/index.css`）

### 1.2 后端技术栈
- 核心：`Spring Boot 3.2.3` + `Spring Security` + `Spring Data JPA` + `MyBatis`（`backend/pom.xml`）
- 数据层：`MySQL` + `Flyway`（`backend/pom.xml`）
- 鉴权：JWT（`jjwt` 依赖，`backend/pom.xml`）
- 其他：WebSocket、阿里云 OSS 上传（`backend/pom.xml`）

### 1.3 当前运行方式
- 前端：`npm run dev`（Vite）
- 后端：`mvnw.cmd spring-boot:run`
- 前端通过环境变量请求 `/api/*`，开发环境通常由 Vite 代理至后端（`frontend/src/api/request.js`、仓库配置约定）

### 1.4 主要目录结构（当前有效）
- 前端入口与路由：`frontend/src/main.js`、`frontend/src/router/index.js`
- 布局层：`frontend/src/layouts/PlatformLayout.vue`、`frontend/src/layouts/FellowshipLayout.vue`、`frontend/src/layouts/AdminLayout.vue`
- 页面层：`frontend/src/pages/platform/*`、`frontend/src/pages/fellowship/*`、`frontend/src/pages/admin/*`、`frontend/src/pages/*`
- 组件层：`frontend/src/components/*`、`frontend/src/components/fellowship/*`、`frontend/src/components/admin/*`
- API 层：`frontend/src/api/*`
- 后端接口：`backend/src/main/java/com/lovecube/backend/controllers/*`

### 1.5 路由结构（真实）
- 平台层：`/`（含 `/announcements`、`/articles`、`/events`、`/me`、`/messages`、`/modules`、`/fellowship-intro`）
- 联谊层：`/fellowship/*`（如 `/fellowship/discover`、`/fellowship/messages`、`/fellowship/me`）
- 管理后台：`/admin/*`（公告、文章、活动、用户、审核、举报等）
- 统一鉴权守卫：`requiresAuth` 与 `requiresAdmin`（`frontend/src/router/index.js`）

---

## 2. 当前已完成页面（按模块）

> 说明：以下是“当前代码中已有页面与数据接入状态”，不是产品设计目标。

### 2.1 平台首页
- 页面路径：`/`
- 对应文件：`frontend/src/pages/WebsiteHome.vue`
- 当前功能：平台 Hero、模块入口、热门资讯/公告/活动聚合、平台数据展示
- 数据来源：优先真实接口 `fetchAnnouncements/fetchArticles/fetchEvents/fetchHomeConfig`，失败回退静态 fallback
- 是否已接后端：**部分已接**（有接口调用，同时保留大量 fallback）

### 2.2 联谊中心
- 页面路径：`/fellowship`、`/fellowship/discover`、`/fellowship/match`、`/fellowship/messages`、`/fellowship/me` 等
- 对应文件：`frontend/src/pages/fellowship/landing.vue`、`frontend/src/pages/home/index.vue`、`frontend/src/pages/match/index.vue`、`frontend/src/pages/message/index.vue`、`frontend/src/pages/fellowship/MePage.vue`
- 当前功能：联谊落地页、推荐/匹配、消息、资料编辑、黑名单、偏好、邀请、认证
- 数据来源：真实接口 + fallback + 局部本地状态（例如个人页活动区块为本地数组）
- 是否已接后端：**中高**（核心资料/照片/匹配/消息接口已接，局部仍有静态占位）

### 2.3 活动中心
- 页面路径：`/events`、`/events/:id`
- 对应文件：`frontend/src/pages/platform/EventsPage.vue`、`frontend/src/pages/platform/EventDetailPage.vue`
- 当前功能：活动筛选、分类、排序、详情、报名
- 数据来源：`fetchEvents`、`fetchEventDetail`、`signupEvent`，失败回退 `fallbackEvents`
- 是否已接后端：**已接但强依赖 fallback**

### 2.4 平台动态
- 页面路径：`/announcements`、`/announcements/:id`
- 对应文件：`frontend/src/pages/platform/AnnouncementsPage.vue`、`frontend/src/pages/platform/AnnouncementDetailPage.vue`
- 当前功能：公告列表、分类筛选、搜索、热门阅读、详情页
- 数据来源：`fetchAnnouncements` / `fetchAnnouncementDetail` + fallback
- 是否已接后端：**已接（有完整后备静态）**

### 2.5 精选内容/资讯
- 页面路径：`/articles`、`/articles/:id`
- 对应文件：`frontend/src/pages/platform/ArticlesPage.vue`、`frontend/src/pages/platform/ArticleDetailPage.vue`
- 当前功能：分类筛选、排序、列表、置顶卡片、详情
- 数据来源：`fetchArticles` / `fetchArticleDetail` + fallback
- 是否已接后端：**已接（有完整后备静态）**

### 2.6 个人中心
- 平台个人中心：`/me` -> `frontend/src/pages/platform/AccountCenterPage.vue`
- 联谊个人中心：`/fellowship/me` -> `frontend/src/pages/fellowship/MePage.vue`
- 当前功能：平台侧账号与模块入口；联谊侧资料、照片、动态、VIP 入口
- 数据来源：
  - 平台侧：用户接口 + 通知未读接口 + 多个 KPI 为前端固定值
  - 联谊侧：`fellowshipProfile` 相关真实接口 + 部分展示数据静态
- 是否已接后端：**双中心并存，接入程度不一致**

### 2.7 消息中心
- 平台消息：`/messages` -> `frontend/src/pages/platform/MessagesCenterPage.vue`
- 联谊消息：`/fellowship/messages` -> `frontend/src/pages/message/index.vue`
- 当前功能：平台消息分频道（全部/系统/活动/互动/联谊入口/订单预留）
- 数据来源：平台通知接口 `getNotifications` + 活动/互动 tab 本地静态数组
- 是否已接后端：**部分已接**

### 2.8 登录/注册
- 页面路径：`/login`、`/fellowship/login`（共用）
- 对应文件：`frontend/src/pages/login/index.vue`
- 当前功能：登录、注册（含邀请码）、登录后跳转、注册后引导
- 数据来源：`/api/auth/login`、`/api/auth/register`
- 是否已接后端：**已接**

### 2.9 后台管理
- 页面路径：`/admin`、`/admin/*`
- 对应文件：`frontend/src/layouts/AdminLayout.vue` + `frontend/src/pages/admin/*`
- 当前功能：总览、公告/文章/活动/用户/邀请/认证/举报/反馈/模块/首页配置管理
- 数据来源：`frontend/src/api/adminContent.js`（真实接口）
- 是否已接后端：**已接，功能框架完整**

---

## 3. 当前核心业务模块边界（是否混杂）

### 3.1 平台模块
- 事实：已形成独立布局与导航（`frontend/src/layouts/PlatformLayout.vue`）
- 问题：平台页大量“联谊语义文案”与“恋爱导向内容”仍出现，边界不彻底
- 判断：**基本独立，但语义有渗透**

### 3.2 联谊模块
- 事实：有独立布局、二级导航、路由前缀 `/fellowship/*`（`frontend/src/layouts/FellowshipLayout.vue`）
- 事实：旧目录 `frontend/src/modules/fellowship/` 已标注废弃，但仍保留同名组件/页面副本（`frontend/src/modules/fellowship/DEPRECATED.md`）
- 判断：**运行态独立，代码资产层面存在历史重复耦合**

### 3.3 活动模块
- 事实：活动主入口在平台路由 `/events`（`frontend/src/router/index.js`）
- 事实：活动文案中既有平台活动也有联谊社交活动表达（`frontend/src/pages/platform/EventsPage.vue`）
- 判断：**归属偏平台，但业务语义与联谊耦合明显**

### 3.4 内容/资讯模块
- 事实：平台资讯路径清晰：`/articles`、`/announcements`
- 事实：内容分类中混有“活动中心/AI工具/本地服务”标签（`frontend/src/pages/platform/ArticlesPage.vue`）
- 判断：**页面独立，内容边界尚未抽象清晰**

### 3.5 咨询服务模块
- 事实：路由、页面、API 中未发现明确“咨询服务”模块实现
- 判断：**暂无（仅概念层预留）**

### 3.6 通知/消息模块
- 事实：平台消息中心已建立（`/messages`），联谊消息也有独立中心（`/fellowship/messages`）
- 事实：平台消息页面使用 Vant Tabs，且部分频道数据是静态（`frontend/src/pages/platform/MessagesCenterPage.vue`）
- 判断：**双中心并存，聚合策略未定，边界不清**

### 3.7 会员/支付模块
- 事实：前端有 `buyVip` 接口（`frontend/src/api/vip.js`），联谊侧有 `/fellowship/vip` 页面入口
- 事实：后端有 `PaymentController` 的 `/api/payment/vip`
- 判断：**能力存在，但仍偏联谊子能力，不是平台统一会员体系**

### 3.8 账号/个人中心模块
- 事实：存在平台个人中心 `/me` 与联谊个人中心 `/fellowship/me`
- 事实：账号体系统一（同一 `user` store + `/api/auth/*`）
- 判断：**账号统一，个人中心双轨并行（职责分层还不够干净）**

---

## 4. 当前数据模型与接口现状

> 以 `frontend/src/api/*` 与 `backend/controllers/*` 实际接口为准。

### 4.1 用户相关接口
- 前端：`/auth/login`、`/auth/register`、`/users/me`、`/users/profile`
- 后端：`AuthController`、`UserController`
- 结论：**有**

### 4.2 联谊相关接口
- 资料：`/fellowship/profile/me`、`/fellowship/profile/completion`
- 用户：`/fellowship/users`、`/fellowship/users/{id}`
- 匹配互动：`/matches/list`、`/matches/filter`、`/interactions/*`
- 黑名单：`/blacklist/*`
- 邀请：`/fellowship/invite/*`
- 结论：**有（最完整）**

### 4.3 活动相关接口
- 平台活动：`/events`、`/events/{id}`、`/events/{id}/signup`
- 后台活动管理：`/admin/events`（CRUD）
- 结论：**有**

### 4.4 内容/资讯相关接口
- 公告：`/announcements`、`/announcements/{id}`
- 文章：`/articles`、`/articles/{id}`
- 首页配置：`/home/config`
- 后台内容管理：`/admin/announcements`、`/admin/articles`、`/admin/home-config`
- 结论：**有**

### 4.5 通知/消息相关接口
- 通知：`/notifications`、`/notifications/unread-count`、`/notifications/{id}/read`、`/notifications/read-all`
- 消息：`/messages/chat`、`/messages/interact`、`/messages/visitor`、`/messages/unread`
- 聊天：`/chat/history/*`、`/chat/send`、`/chat/unread/*`
- 结论：**有（平台通知与联谊消息并行）**

### 4.6 咨询相关接口
- 结论：**暂无**

### 4.7 会员/订单/支付相关接口
- 支付：`/payment/vip`
- 订单消息：前端页面仅“预留”频道，无订单接口映射
- 结论：**会员支付有，订单体系暂无**

---

## 5. 当前前端组件现状

### 5.1 公共组件
- `frontend/src/components/RouteBackButton.vue`
- `frontend/src/components/NavBar.vue`
- `frontend/src/components/AppTabBar.vue`
- `frontend/src/components/UserCard.vue`

### 5.2 业务组件
- 联谊业务组件：`frontend/src/components/fellowship/*`（如 `UserCard.vue`、`PhotoWall.vue`、`ProfileProgress.vue`）
- 管理组件：`frontend/src/components/admin/CoverUploadField.vue`

### 5.3 页面组件
- 平台页面：`frontend/src/pages/platform/*`
- 联谊页面：`frontend/src/pages/fellowship/*` 与 `frontend/src/pages/{home/match/message/...}/index.vue`
- 后台页面：`frontend/src/pages/admin/*`

### 5.4 布局组件
- 平台：`frontend/src/layouts/PlatformLayout.vue`
- 联谊：`frontend/src/layouts/FellowshipLayout.vue`
- 后台：`frontend/src/layouts/AdminLayout.vue`

### 5.5 是否存在重复组件
- 存在：`src/components/UserCard.vue` 与 `src/components/fellowship/UserCard.vue`
- 存在：`src/components/NavBar.vue` 与 `src/modules/fellowship/components/NavBar.vue`（历史遗留目录）
- 判断：**存在重复与历史副本**

### 5.6 是否存在样式不统一
- 事实：平台与联谊理论上分层，但平台页面中仍大量手写颜色值与强视觉差异写法
- 事实：平台消息页使用 Vant，平台其他页多为自定义 CSS
- 判断：**存在局部不统一**

---

## 6. 当前样式与设计系统现状

### 6.1 是否有统一 CSS 变量
- 有：`frontend/src/assets/styles/tokens.css` 定义 `--lc-*`、`--color-*`

### 6.2 是否有统一主题色
- 有：平台蓝色系 + 联谊粉色系（`tokens.css`）
- 但：很多页面仍直接写十六进制色值，未完全 token 化

### 6.3 是否有公共布局规范
- 有：`platform.css`、`fellowship.css`、`admin.css`
- 路由布局也按三层拆分（Platform/Fellowship/Admin）

### 6.4 是否存在页面各写各的情况
- 存在：如 `WebsiteHome.vue`、`ArticlesPage.vue`、`AnnouncementsPage.vue` 都是大体量页面内样式
- 判断：**有统一基础，但页面自由度高，维护成本偏高**

### 6.5 是否适合继续扩展为平台型网站
- 结论：**可以扩展，但应先治理边界与设计规范一致性**

---

## 7. 当前最大问题（重点）

### 7.1 平台与联谊是否混在一起
- 路由层：已分开（`/` vs `/fellowship/*`）
- 体验与语义层：仍混（平台页频繁出现联谊导向文案、联谊状态文案）
- 结论：**结构分离，业务叙事仍混**

### 7.2 活动中心到底是平台活动还是联谊活动
- 路由归属是平台（`/events`）
- 内容语义偏联谊（活动标签、文案、推荐口径）
- 结论：**归属定义不清，当前实现为“平台入口+联谊语义内容”**

### 7.3 消息中心是否应平台统一
- 现在是“双消息中心”：`/messages` + `/fellowship/messages`
- 平台中心仅部分频道真实，联谊中心承载即时互动主链路
- 结论：**未统一，聚合关系尚未产品化**

### 7.4 个人中心是否承担太多联谊属性
- 是：联谊个人中心功能极重；平台个人中心较轻且部分数据静态
- 结论：**双中心权重失衡**

### 7.5 导航结构是否清晰
- 平台一级导航较完整（`PlatformLayout.vue`）
- 联谊二级导航也存在（`FellowshipLayout.vue`）
- 问题在于跨层跳转频繁、概念边界命名不稳定（如“平台动态/内容/活动/联谊介绍”与联谊内部重复）
- 结论：**可用但不够清晰**

### 7.6 是否支持后续扩展咨询/内容/通知/会员
- 内容/通知/会员：已有基础能力
- 咨询服务：缺路由、页面、接口、后台配置
- 结论：**具备扩展基础，但需先做模块边界治理**

---

## 8. 下一步怎么改

### P0（必须调整）
1. 明确模块归属文档与路由命名规则：平台 vs 联谊 vs 后台（先统一“活动中心”定义）
2. 收敛双消息中心策略：定义“平台聚合消息”和“联谊会话消息”的职责与跳转
3. 清理历史重复资产：冻结并逐步下线 `frontend/src/modules/fellowship/*` 的重复组件引用风险
4. 平台个人中心去联谊化，联谊个人中心聚焦婚恋资料与互动能力

### P1（可以优化）
1. 为页面数据来源打标（接口/fallback/mock），减少“看似接了后端但默认静态”的误判
2. 推进样式 token 化，减少页面硬编码颜色
3. 拆分大页面样式与逻辑（如平台首页、公告页、资讯页）
4. 平台层尽量减少 Vant 依赖，联谊层保留 Vant 为主

### P2（后续扩展）
1. 新增咨询服务模块（平台一级入口 + 内容联动 + 后台配置）
2. 建立统一会员中心（平台维度），联谊 VIP 作为会员子权益
3. 建立订单中心与订单消息频道，打通支付后链路

---

## 9. 《我需要让 ChatGPT 进一步判断的问题》

1. 平台定位应是“联谊驱动的平台”还是“多模块平台（联谊只是其一）”？
2. 一级导航是否应固定为：首页/模块中心/内容/活动/消息/我的？
3. 活动中心应拆为“平台活动”与“联谊活动”两条线，还是保留一个中心按标签分流？
4. 个人中心是否应明确拆成“平台个人中心”与“联谊个人中心”，并设置不同 KPI？
5. 消息系统应做一个统一消息中心，还是保留“聚合中心 + 业务中心”双层结构？
6. 咨询服务应作为一级模块，还是挂在内容资讯下作为子频道？
7. 会员体系应平台统一（权益分发），还是继续联谊独立 VIP？
8. 现阶段应优先“架构重构”还是“继续补页面”，分界点是什么？
9. 后台管理是否要按平台模块拆菜单（内容后台/联谊后台/活动后台）？
10. 是否需要建立统一信息架构词典（模块名、页面名、频道名）避免命名漂移？

---

## 10. 最终结论（10条以内）

1. 当前项目已具备“平台层 + 联谊层 + 后台层”三层路由与布局骨架。  
2. 平台与联谊在代码结构上已分开，但在内容语义与页面职责上仍有混杂。  
3. 活动中心技术上归平台，业务语义上偏联谊，归属定义不清是核心矛盾。  
4. 消息与个人中心均是“双中心并行”状态，尚未形成稳定产品边界。  
5. 平台资讯、公告、活动、首页配置均有真实后端接口，但前端 fallback 数据占比高。  
6. 联谊链路（资料、匹配、互动、黑名单、邀请）是当前最完整的业务闭环。  
7. 咨询服务与订单体系当前基本为空白，只有概念或频道预留。  
8. 组件与目录存在历史重复（尤其废弃 `modules/fellowship` 资产），需持续清理。  
9. 设计系统已有 token 基础，但页面样式实现仍存在“各写各的”现象。  
10. 下一阶段应先做 P0 边界治理，再进行模块扩展，否则新增功能会继续放大耦合。  