// ─────────────────────────────────────────────────────────────────────────────
// Mobile 独立路由（UI 分离架构 / Phase 1）
// 这些是新的移动端专属入口，用于灰度验证 H5 独立 UI。
// 不要与旧路由（/fellowship/*、/me）混改。
// API / store / 权限逻辑继续复用，只隔离 UI 层。
// 新页面放在 src/pages/mobile/，布局使用 MobileLayout.vue（max-width 480px）。
// 注意：/m/fellowship/* 不触发 router/index.js 的 fellowship 启用检查，
//       需要在页面内自行处理或在后续迭代中补充守卫。
// 参考规范：docs/ui-architecture.md
// ─────────────────────────────────────────────────────────────────────────────
export default {
  path: '/m',
  component: () => import('@/layouts/MobileLayout.vue'),
  children: [
    {
      path: 'platform/me',
      component: () => import('@/pages/mobile/platform/MePage.vue'),
      meta: { module: 'platform', requiresAuth: true }
    },
    {
      path: 'fellowship/me',
      component: () => import('@/pages/mobile/fellowship/MePage.vue'),
      meta: { module: 'fellowship', requiresAuth: true }
    }
  ]
}
