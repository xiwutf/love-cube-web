import { createRouter, createWebHashHistory } from 'vue-router'
import { useUserStore } from '@/stores/user.js'
import platformRoutes from './modules/platform.routes.js'
import fellowshipRoutes from './modules/fellowship.routes.js'
import adminRoutes from './modules/admin.routes.js'
import pcRoutes from './modules/pc.routes.js'
import mobileRoutes from './modules/mobile.routes.js'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    platformRoutes,
    adminRoutes,
    fellowshipRoutes,
    pcRoutes,
    mobileRoutes,
    { path: '/vip', redirect: '/fellowship/vip' },
    { path: '/:pathMatch(.*)*', redirect: '/' }
  ]
})

router.beforeEach(async (to) => {
  const userStore = useUserStore()
  const token = userStore.token

  if (to.meta.requiresAuth && !token) {
    userStore.setPostLoginRedirect(to.fullPath)
    if (to.path.startsWith('/fellowship')) {
      return { path: '/fellowship/login', query: { redirect: encodeURIComponent(to.fullPath) } }
    }
    return { path: '/login', query: { redirect: encodeURIComponent(to.fullPath) } }
  }

  if (to.path.startsWith('/fellowship') && to.path !== '/fellowship' && token) {
    if (userStore.userInfo) {
      userStore.refreshCurrentUser().catch(() => null)
    } else {
      await userStore.refreshCurrentUser().catch(() => null)
    }
    if (!userStore.isFellowshipEnabled) {
      const allowedBeforeFellowship = to.matched.some((record) => record.meta.allowBeforeFellowship)
      if (!allowedBeforeFellowship) {
        return { path: '/fellowship', query: { redirect: encodeURIComponent(to.fullPath) } }
      }
    }
  }

  if (to.meta.requiresAdmin) {
    await userStore.refreshCurrentUser().catch(() => null)
    // Load fine-grained permissions (cached after first load)
    await userStore.loadAdminContext().catch(() => null)
    if (!userStore.isAdmin) {
      return { path: '/' }
    }
    // Permission-gated route check
    if (to.meta.permission && !userStore.hasPermission(to.meta.permission)) {
      return { path: '/admin/403' }
    }
  }
})

export default router
