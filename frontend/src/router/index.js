import { createRouter, createWebHashHistory } from 'vue-router'
import { useUserStore } from '@/stores/user.js'

const auth = { meta: { requiresAuth: true } }
const adminAuth = { meta: { requiresAuth: true, requiresAdmin: true } }

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/PlatformLayout.vue'),
    children: [
      { path: '', component: () => import('@/pages/WebsiteHome.vue') },
      { path: 'login', component: () => import('@/pages/login/index.vue') },
      { path: 'register', redirect: '/login' },
      { path: 'account', component: () => import('@/pages/platform/AccountCenterPage.vue'), ...auth },
      { path: 'announcements', component: () => import('@/pages/platform/AnnouncementsPage.vue') },
      { path: 'announcements/:id', component: () => import('@/pages/platform/AnnouncementDetailPage.vue') },
      { path: 'articles', component: () => import('@/pages/platform/ArticlesPage.vue') },
      { path: 'articles/:id', component: () => import('@/pages/platform/ArticleDetailPage.vue') },
      { path: 'events', component: () => import('@/pages/platform/EventsPage.vue') },
      { path: 'events/:id', component: () => import('@/pages/platform/EventDetailPage.vue') },
      { path: 'about', component: () => import('@/pages/platform/AboutPage.vue') },
      { path: 'fellowship-intro', component: () => import('@/pages/platform/FellowshipIntroPage.vue') },
      { path: 'modules', component: () => import('@/pages/platform/ModulesPage.vue') },
      { path: 'policies/:id', component: () => import('@/pages/platform/PolicyPage.vue') }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    ...adminAuth,
    children: [
      { path: '', component: () => import('@/pages/admin/DashboardPage.vue') },
      { path: 'announcements', component: () => import('@/pages/admin/AnnouncementsAdminPage.vue') },
      { path: 'articles', component: () => import('@/pages/admin/ArticlesAdminPage.vue') },
      { path: 'events', component: () => import('@/pages/admin/EventsAdminPage.vue') },
      { path: 'users', component: () => import('@/pages/admin/UsersAdminPage.vue') },
      { path: 'invites', component: () => import('@/pages/admin/InvitesAdminPage.vue') },
      { path: 'verifications', component: () => import('@/pages/admin/VerificationsAdminPage.vue') },
      { path: 'reports', component: () => import('@/pages/admin/ReportsAdminPage.vue') },
      { path: 'feedbacks', component: () => import('@/pages/admin/FeedbacksAdminPage.vue') },
      { path: 'modules', component: () => import('@/pages/admin/ModulesAdminPage.vue') }
    ]
  },
  {
    path: '/fellowship',
    component: () => import('@/layouts/FellowshipLayout.vue'),
    children: [
      { path: '', component: () => import('@/pages/fellowship/landing.vue') },
      { path: 'login', component: () => import('@/pages/login/index.vue') },
      { path: 'welcome', component: () => import('@/pages/welcome/index.vue') },

      { path: 'discover', component: () => import('@/pages/home/index.vue'), ...auth },
      { path: 'match', component: () => import('@/pages/match/index.vue'), ...auth },
      { path: 'messages', component: () => import('@/pages/message/index.vue'), ...auth },
      { path: 'me', component: () => import('@/pages/fellowship/MePage.vue'), ...auth },

      { path: 'search', component: () => import('@/pages/search/index.vue'), ...auth },
      { path: 'dynamic', component: () => import('@/pages/dynamic/index.vue'), ...auth },
      { path: 'profile-edit', component: () => import('@/pages/profile-edit/index.vue'), ...auth },
      { path: 'profile', component: () => import('@/pages/fellowship/profile/index.vue'), ...auth },
      { path: 'profile/edit', component: () => import('@/pages/fellowship/profile/edit.vue'), ...auth },
      { path: 'settings', component: () => import('@/pages/settings/index.vue'), ...auth },
      { path: 'verify', component: () => import('@/pages/fellowship/VerifyPage.vue'), ...auth },
      { path: 'blacklist', component: () => import('@/pages/fellowship/BlacklistPage.vue'), ...auth },
      { path: 'invite', component: () => import('@/pages/fellowship/InvitePage.vue'), ...auth },
      { path: 'my-likes', component: () => import('@/pages/personal/MyLikesPage.vue'), ...auth },
      { path: 'following', component: () => import('@/pages/personal/FollowingPage.vue'), ...auth },
      { path: 'privacy', component: () => import('@/pages/personal/PrivacySettingsPage.vue'), ...auth },
      { path: 'help', component: () => import('@/pages/personal/HelpFeedbackPage.vue'), ...auth },
      { path: 'newcomers', component: () => import('@/pages/newcomers/index.vue'), ...auth },
      { path: 'user-profile/:id', component: () => import('@/pages/user-profile/index.vue'), ...auth },
      { path: 'chat/:receiverId', component: () => import('@/pages/chat/index.vue'), ...auth },
      { path: 'vip', component: () => import('@/pages/vip/index.vue'), ...auth },

      { path: 'home', redirect: '/fellowship/discover' },
      { path: 'message', redirect: '/fellowship/messages' },
      { path: 'personal', redirect: '/fellowship/me' }
    ]
  },
  { path: '/vip', redirect: '/fellowship/vip' },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
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

  if (to.meta.requiresAdmin) {
    await userStore.refreshCurrentUser().catch(() => null)
    if (!userStore.isAdmin) {
      return { path: '/' }
    }
  }
})

export default router
