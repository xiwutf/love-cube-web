// ─────────────────────────────────────────────────────────────────────────────
// PC 独立路由（UI 分离架构 / Phase 1）
// 这些是新的 PC 专属入口，用于灰度验证 PC 端独立 UI。
// 不要与旧路由（/、/me、/platform/*）混改。
// API / store / 权限逻辑继续复用，只隔离 UI 层。
// 新页面放在 src/pages/pc/，布局使用 PcLayout.vue（无移动端导航）。
// 参考规范：docs/ui-architecture.md
// ─────────────────────────────────────────────────────────────────────────────
export default {
  path: '/pc',
  component: () => import('@/layouts/PcLayout.vue'),
  children: [
    {
      path: 'platform',
      component: () => import('@/pages/pc/platform/HomePage.vue'),
      meta: { module: 'platform' }
    },
    {
      path: 'platform/me',
      component: () => import('@/pages/pc/platform/MePage.vue'),
      meta: { module: 'platform', requiresAuth: true }
    }
  ]
}
