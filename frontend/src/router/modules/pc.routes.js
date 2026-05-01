export default {
  path: '/pc',
  component: () => import('@/layouts/PcLayout.vue'),
  children: [
    {
      path: '',
      redirect: '/pc/platform'
    },
    {
      path: 'platform',
      component: () => import('@/pages/pc/platform/HomePage.vue'),
      meta: { module: 'platform' }
    },
    {
      path: 'platform/me',
      component: () => import('@/pages/pc/platform/MePage.vue'),
      meta: { module: 'platform', requiresAuth: true }
    },
    {
      path: 'platform/growth',
      component: () => import('@/pages/pc/platform/GrowthRecordPage.vue'),
      meta: { module: 'platform', requiresAuth: true }
    }
  ]
}
