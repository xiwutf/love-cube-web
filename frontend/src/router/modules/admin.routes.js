export default {
  path: '/admin',
  component: () => import('@/layouts/AdminLayout.vue'),
  meta: { module: 'admin', requiresAuth: true, requiresAdmin: true },
  children: [
    { path: '', component: () => import('@/pages/admin/DashboardPage.vue'), meta: { module: 'admin' } },
    { path: 'announcements', component: () => import('@/pages/admin/AnnouncementsAdminPage.vue'), meta: { module: 'admin' } },
    { path: 'articles', component: () => import('@/pages/admin/ArticlesAdminPage.vue'), meta: { module: 'admin' } },
    { path: 'positive-shares', component: () => import('@/pages/admin/PositiveSharesAdminPage.vue'), meta: { module: 'admin' } },
    { path: 'events', component: () => import('@/pages/admin/EventsAdminPage.vue'), meta: { module: 'admin' } },
    { path: 'users', component: () => import('@/pages/admin/UsersAdminPage.vue'), meta: { module: 'admin' } },
    { path: 'invites', component: () => import('@/pages/admin/InvitesAdminPage.vue'), meta: { module: 'admin' } },
    { path: 'verifications', component: () => import('@/pages/admin/VerificationsAdminPage.vue'), meta: { module: 'admin' } },
    { path: 'reports', component: () => import('@/pages/admin/ReportsAdminPage.vue'), meta: { module: 'admin' } },
    { path: 'feedbacks', component: () => import('@/pages/admin/FeedbacksAdminPage.vue'), meta: { module: 'admin' } },
    { path: 'modules', component: () => import('@/pages/admin/ModulesAdminPage.vue'), meta: { module: 'admin' } },
    { path: 'home-config', component: () => import('@/pages/admin/HomeConfigAdminPage.vue'), meta: { module: 'admin' } }
  ]
}
