import { createRouter, createWebHashHistory } from 'vue-router'
import { storage } from '@/utils/storage.js'

const auth = { meta: { requiresAuth: true } }

const routes = [
  // 公开页
  { path: '/login',   component: () => import('@/pages/login/index.vue') },
  { path: '/welcome', component: () => import('@/pages/welcome/index.vue') },

  // 需登录 - TabBar 主页面
  { path: '/',          component: () => import('@/pages/home/index.vue'),     ...auth },
  { path: '/match',     component: () => import('@/pages/match/index.vue'),    ...auth },
  { path: '/message',   component: () => import('@/pages/message/index.vue'),  ...auth },
  { path: '/personal',  component: () => import('@/pages/personal/index.vue'), ...auth },
  { path: '/search',    component: () => import('@/pages/search/index.vue'),   ...auth },
  { path: '/dynamic',   component: () => import('@/pages/dynamic/index.vue'),  ...auth },

  // 需登录 - 子页面
  { path: '/profile',                   component: () => import('@/pages/profile/index.vue'),      ...auth },
  { path: '/profile-edit',              component: () => import('@/pages/profile-edit/index.vue'), ...auth },
  { path: '/settings',                  component: () => import('@/pages/settings/index.vue'),     ...auth },
  { path: '/newcomers',                 component: () => import('@/pages/newcomers/index.vue'),    ...auth },
  { path: '/user-profile/:id',          component: () => import('@/pages/user-profile/index.vue'), ...auth },
  { path: '/chat/:receiverId',          component: () => import('@/pages/chat/index.vue'),         ...auth },

  // 兜底
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  // hash 模式：静态托管无需服务端配置 fallback
  history: createWebHashHistory(),
  routes
})

// 全局路由守卫：未登录跳登录页
router.beforeEach((to) => {
  const token = storage.get('token')
  if (to.meta.requiresAuth && !token) {
    return { path: '/login' }
  }
})

export default router
