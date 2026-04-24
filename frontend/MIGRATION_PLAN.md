# Love Cube H5 迁移计划

> 项目路径：`D:\00-ProjecData\00-person\love-cube-h5`
> 后端地址：`http://xifg.com.cn:8090/admin`
> 更新时间：2026-04-24

---

## 技术栈

| 类别 | 方案 |
|------|------|
| 框架 | Vue 3 (Composition API) |
| 构建 | Vite 7 |
| 路由 | Vue Router 4（Hash 模式） |
| 状态 | Pinia |
| UI | Vant 4 |
| 请求 | Axios（统一封装） |
| 实时通信 | 原生 WebSocket |

---

## 总体进度

```
P0  基础骨架        ████████████ 100% ✅
P1  核心页面        ████████████ 100% ✅
P2  实时/社交       ████████████ 100% ✅
P3  复杂交互        ████████████ 100% ✅
```

> 最后更新：2026-04-24（全量完成首版）

---

## 分阶段任务详情

### ✅ P0 — 基础骨架（已完成）

| # | 任务 | 文件 | 状态 |
|---|------|------|------|
| 0.1 | 项目初始化 | `package.json` / `vite.config.js` / `.env.*` | ✅ |
| 0.2 | 全局样式 | `src/assets/global.css` | ✅ |
| 0.3 | Vue 入口 | `src/main.js` / `src/App.vue` | ✅ |
| 0.4 | 路由（基础） | `src/router/index.js` | ✅ |
| 0.5 | 用户状态 | `src/stores/user.js` | ✅ |
| 0.6 | 平台适配层 | `src/utils/storage.js` | ✅ |
| 0.7 | 请求层 | `src/api/request.js` | ✅ |
| 0.8 | 认证 API | `src/api/auth.js` | ✅ |
| 0.9 | 首页 API | `src/api/home.js` / `src/api/user.js` | ✅ |
| 0.10 | 登录/注册页 | `src/pages/login/index.vue` | ✅ |
| 0.11 | 首页 | `src/pages/home/index.vue` | ✅ |
| 0.12 | 个人中心（基础） | `src/pages/profile/index.vue` | ✅ |

---

### 🔄 P1 — 核心页面（进行中）

| # | 任务 | 文件 | 状态 |
|---|------|------|------|
| 1.1 | 共用工具函数 | `src/utils/format.js` / `image.js` / `normalizeUser.js` | ✅ |
| 1.2 | 共用组件 | `NavBar.vue` / `AppTabBar.vue` / `UserCard.vue` | ✅ |
| 1.3 | 消息状态 | `src/stores/message.js` | ✅ |
| 1.4 | Welcome 页 | `src/pages/welcome/index.vue` | ✅ |
| 1.5 | Settings 页 | `src/pages/settings/index.vue` | ✅ |
| 1.6 | Newcomers 页 | `src/pages/newcomers/index.vue` | ✅ |
| 1.7 | User-Profile 页 | `src/pages/user-profile/index.vue` | ✅ |
| 1.8 | Personal 完整页 | `src/pages/personal/index.vue` | ✅ |
| 1.9 | Profile-Edit 页 | `src/pages/profile-edit/index.vue` | ✅ |
| 1.10 | Message 三 Tab 页 | `src/pages/message/index.vue` | ✅ |

---

### ⏳ P2 — 实时与社交（待开始）

| # | 任务 | 文件 | 状态 |
|---|------|------|------|
| 2.1 | useWebSocket composable | `src/composables/useWebSocket.js` | ✅ |
| 2.2 | Chat 实时聊天页 | `src/pages/chat/index.vue` | ✅ |
| 2.3 | Dynamic 动态页 | `src/pages/dynamic/index.vue` | ✅ |
| 2.4 | Search 搜索页 | `src/pages/search/index.vue` | ✅ |

---

### ⏳ P3 — 复杂交互（待开始）

| # | 任务 | 文件 | 状态 |
|---|------|------|------|
| 3.1 | SwipeCard 组件 | `src/components/SwipeCard.vue` | ✅ |
| 3.2 | Match 卡片匹配页 | `src/pages/match/index.vue` | ✅ |

---

### ⏳ 收尾

| # | 任务 | 文件 | 状态 |
|---|------|------|------|
| 4.1 | 补全路由表 | `src/router/index.js` | ✅ |
| 4.2 | AppTabBar 跳转全部接通 | 各页面 | ✅ |

---

## 文件结构总览

```
love-cube-h5/
├── .env.development          # 开发环境变量
├── .env.production           # 生产环境变量
├── vite.config.js
├── nginx.conf                # 部署配置
├── MIGRATION_PLAN.md         # 本文件
└── src/
    ├── main.js
    ├── App.vue
    ├── api/
    │   ├── request.js        ✅ axios 实例
    │   ├── auth.js           ✅ 登录/注册
    │   ├── home.js           ✅ banners/recommends/newcomers
    │   ├── user.js           ✅ /users/me / 更新资料
    │   ├── match.js          ✅ 匹配相关
    │   ├── message.js        ✅ 消息/互动/访客
    │   ├── chat.js           ✅ 聊天记录
    │   ├── dynamic.js        ✅ 动态
    │   └── upload.js         ✅ 文件上传
    ├── composables/
    │   ├── useInfiniteScroll.js  ✅
    │   ├── useWebSocket.js       ✅
    │   └── useImageUpload.js     ✅
    ├── components/
    │   ├── NavBar.vue        ✅
    │   ├── AppTabBar.vue     ✅
    │   ├── UserCard.vue      ✅
    │   └── SwipeCard.vue     ✅
    ├── stores/
    │   ├── user.js           ✅
    │   └── message.js        ✅
    ├── utils/
    │   ├── storage.js        ✅ wx.Storage 替代
    │   ├── format.js         ✅ 时间/年龄/距离格式化
    │   ├── image.js          ✅ 图片 URL 处理
    │   └── normalizeUser.js  ✅ 用户对象字段归一化
    ├── assets/
    │   └── global.css        ✅
    └── pages/
        ├── login/            ✅
        ├── home/             ✅
        ├── profile/          ✅ 查看个人信息
        ├── welcome/          ✅
        ├── settings/         ✅
        ├── newcomers/        ✅
        ├── user-profile/     ✅ 查看他人资料
        ├── personal/         ✅
        ├── profile-edit/     ✅
        ├── message/          ✅
        ├── chat/             ✅
        ├── dynamic/          ✅
        ├── search/           ✅
        └── match/            ✅
```

---

## 后端改动记录

| 文件 | 改动 | 原因 |
|------|------|------|
| `AuthController.java` | **新增** | H5 账号密码登录/注册 |
| `SecurityConfig.java` | **修改** 加 BCryptPasswordEncoder Bean | 密码加密 |
| `UserRepository.java` | **修改** 加 findByPhoneNumber/findByEmail | 按手机/邮箱查用户 |

> 后续后端零改动计划。唯一风险：生产环境部署 HTTPS 后 WebSocket 需改为 `wss://`。

---

## 本地启动

```bash
cd D:\00-ProjecData\00-person\love-cube-h5
npm install
npm run dev
# 浏览器访问 http://localhost:5173
```

## 生产部署

```bash
npm run build          # 产物在 dist/
# 将 dist/ 上传到服务器，使用 nginx.conf 配置
```

---

## wx.* → H5 替换速查

| wx.* | H5 方案 |
|------|---------|
| `wx.setStorageSync` | `localStorage.setItem` (via storage.js) |
| `wx.navigateTo` | `router.push` |
| `wx.switchTab` | `router.push` |
| `wx.reLaunch` | `router.replace` |
| `wx.showToast` | Vant `showToast` |
| `wx.showModal` | Vant `showConfirmDialog` |
| `wx.showLoading` | Vant `showLoadingToast` |
| `wx.chooseMedia` | `<input type="file">` |
| `wx.compressImage` | `browser-image-compression` 库 |
| `wx.uploadFile` | `axios` + `FormData` |
| `wx.connectSocket` | `new WebSocket()` |
| `wx.previewImage` | Vant `showImagePreview` |
| `wx.createAnimation` | CSS transition / Vue `<Transition>` |
| `wx.pageScrollTo` | `element.scrollIntoView()` |
