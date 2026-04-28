import authRoutes from './auth.routes.js'

export default {
  path: '/',
  component: () => import('@/layouts/PlatformLayout.vue'),
  children: [
    ...authRoutes,
    { path: '', component: () => import('@/pages/WebsiteHome.vue'), meta: { module: 'platform' } },
    { path: 'me', component: () => import('@/pages/platform/AccountCenterPage.vue'), meta: { module: 'platform', requiresAuth: true } },
    { path: 'me/favorites', component: () => import('@/pages/platform/MyFavoritesPage.vue'), meta: { module: 'platform', requiresAuth: true } },
    { path: 'me/drafts', component: () => import('@/pages/platform/MyDraftsPage.vue'), meta: { module: 'platform', requiresAuth: true } },
    { path: 'account', redirect: '/me', meta: { module: 'platform' } },
    { path: 'messages', component: () => import('@/pages/platform/MessagesCenterPage.vue'), meta: { module: 'platform', requiresAuth: true } },
    { path: 'announcements', component: () => import('@/pages/platform/AnnouncementsPage.vue'), meta: { module: 'platform' } },
    { path: 'announcements/:id', component: () => import('@/pages/platform/AnnouncementDetailPage.vue'), meta: { module: 'platform' } },
    { path: 'articles', component: () => import('@/pages/platform/ArticlesPage.vue'), meta: { module: 'platform' } },
    { path: 'articles/:id', component: () => import('@/pages/platform/ArticleDetailPage.vue'), meta: { module: 'platform' } },
    { path: 'events', component: () => import('@/pages/platform/EventsPage.vue'), meta: { module: 'platform' } },
    { path: 'events/:id', component: () => import('@/pages/platform/EventDetailPage.vue'), meta: { module: 'platform' } },
    { path: 'about', component: () => import('@/pages/platform/AboutPage.vue'), meta: { module: 'platform' } },
    { path: 'fellowship-intro', component: () => import('@/pages/platform/FellowshipIntroPage.vue'), meta: { module: 'platform' } },
    { path: 'modules', component: () => import('@/pages/platform/ModulesPage.vue'), meta: { module: 'platform' } },
    { path: 'platform/positive-share', component: () => import('@/pages/platform/PositiveSharePage.vue'), meta: { module: 'platform' } },
    { path: 'policies/:id', component: () => import('@/pages/platform/PolicyPage.vue'), meta: { module: 'platform' } }
  ]
}
