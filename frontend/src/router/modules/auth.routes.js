export default [
  { path: 'login', component: () => import('@/pages/login/index.vue'), meta: { module: 'auth' } },
  { path: 'register', redirect: '/login', meta: { module: 'auth' } }
]
