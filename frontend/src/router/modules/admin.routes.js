export default {
  path: '/admin',
  component: () => import('@/layouts/AdminLayout.vue'),
  meta: { module: 'admin', requiresAuth: true, requiresAdmin: true },
  children: [
    // 仪表盘（所有管理员可见）
    { path: '', component: () => import('@/pages/admin/DashboardPage.vue'), meta: { module: 'admin' } },
    { path: 'dashboard', redirect: '/admin' },

    // 403 无权限页
    { path: '403', component: () => import('@/pages/admin/ForbiddenPage.vue'), meta: { module: 'admin' } },

    // 内容管理（CONTENT_ADMIN / SUPER_ADMIN）
    {
      path: 'announcements',
      component: () => import('@/pages/admin/AnnouncementsAdminPage.vue'),
      meta: { module: 'admin', permission: 'content.announcement.manage' }
    },
    {
      path: 'articles',
      component: () => import('@/pages/admin/ArticlesAdminPage.vue'),
      meta: { module: 'admin', permission: 'content.article.manage' }
    },
    {
      path: 'events',
      component: () => import('@/pages/admin/EventsAdminPage.vue'),
      meta: { module: 'admin', permission: 'content.event.manage' }
    },
    {
      path: 'feedbacks',
      component: () => import('@/pages/admin/FeedbacksAdminPage.vue'),
      meta: { module: 'admin', permission: 'content.manage' }
    },
    {
      path: 'local-resources',
      component: () => import('@/pages/admin/LocalResourcesAdminPage.vue'),
      meta: { module: 'admin', permission: 'content.manage' }
    },

    // 审核模块（REVIEWER / CONTENT_ADMIN / SUPER_ADMIN）
    {
      path: 'positive-shares',
      component: () => import('@/pages/admin/PositiveSharesAdminPage.vue'),
      meta: { module: 'admin', permission: 'review.manage' }
    },
    {
      path: 'help-requests',
      component: () => import('@/pages/admin/HelpRequestsAdminPage.vue'),
      meta: { module: 'admin', permission: 'review.manage' }
    },
    {
      path: 'verifications',
      component: () => import('@/pages/admin/VerificationsAdminPage.vue'),
      meta: { module: 'admin', permission: 'review.manage' }
    },
    {
      path: 'reports',
      component: () => import('@/pages/admin/ReportsAdminPage.vue'),
      meta: { module: 'admin', permission: 'review.manage' }
    },
    {
      path: 'fellowship-dynamics',
      component: () => import('@/pages/admin/FellowshipDynamicsAdminPage.vue'),
      meta: { module: 'admin', permission: 'review.manage' }
    },

    // 用户管理（SUPER_ADMIN）
    {
      path: 'users',
      component: () => import('@/pages/admin/UsersAdminPage.vue'),
      meta: { module: 'admin', permission: 'user.manage' }
    },
    {
      path: 'invites',
      component: () => import('@/pages/admin/InvitesAdminPage.vue'),
      meta: { module: 'admin', permission: 'user.manage' }
    },

    // 系统配置（SUPER_ADMIN）
    {
      path: 'modules',
      component: () => import('@/pages/admin/ModulesAdminPage.vue'),
      meta: { module: 'admin', permission: 'system.manage' }
    },
    {
      path: 'home-config',
      component: () => import('@/pages/admin/HomeConfigAdminPage.vue'),
      meta: { module: 'admin', permission: 'system.manage' }
    },
    {
      path: 'analytics',
      component: () => import('@/pages/admin/AnalyticsAdminPage.vue'),
      meta: { module: 'admin', permission: 'system.manage' }
    },

    // 全站团体管理（SUPER_ADMIN）
    {
      path: 'platform/groups',
      component: () => import('@/pages/admin/GroupsAdminPage.vue'),
      meta: { module: 'admin', permission: 'group.manage.all' }
    },
    {
      path: 'platform/groups/create',
      component: () => import('@/pages/admin/GroupCreateAdminPage.vue'),
      meta: { module: 'admin', permission: 'group.manage.all' }
    },
    {
      path: 'platform/groups/:id',
      component: () => import('@/pages/admin/GroupEditAdminPage.vue'),
      meta: { module: 'admin', permission: 'group.manage.all' }
    },
    { path: 'platform/groups/:id/edit', redirect: to => `/admin/platform/groups/${to.params.id}` },

    // 我的团体（GROUP_OWNER）
    {
      path: 'my-groups',
      component: () => import('@/pages/admin/MyGroupsAdminPage.vue'),
      meta: { module: 'admin', permission: 'group.manage.own' }
    },
    {
      path: 'my-groups/:id',
      component: () => import('@/pages/admin/GroupEditAdminPage.vue'),
      meta: { module: 'admin', permission: 'group.manage.own' }
    }
  ]
}
