export default {
  path: '/fellowship',
  component: () => import('@/layouts/FellowshipLayout.vue'),
  children: [
    { path: '', component: () => import('@/pages/fellowship/landing.vue'), meta: { module: 'fellowship' } },
    { path: 'login', component: () => import('@/pages/login/index.vue'), meta: { module: 'auth' } },
    { path: 'welcome', component: () => import('@/pages/welcome/index.vue'), meta: { module: 'auth' } },

    { path: 'discover', component: () => import('@/pages/fellowship/DiscoverPage.vue'), meta: { module: 'fellowship', requiresAuth: true } },
    { path: 'groups', component: () => import('@/pages/fellowship/GroupsPage.vue'), meta: { module: 'fellowship', requiresAuth: true } },
    { path: 'match', component: () => import('@/pages/fellowship/MatchPage.vue'), meta: { module: 'fellowship', requiresAuth: true } },
    { path: 'messages', component: () => import('@/pages/fellowship/MessagePage.vue'), meta: { module: 'fellowship', requiresAuth: true } },
    { path: 'me', component: () => import('@/pages/fellowship/MePage.vue'), meta: { module: 'fellowship', requiresAuth: true } },

    { path: 'search', component: () => import('@/pages/search/index.vue'), meta: { module: 'fellowship', requiresAuth: true } },
    { path: 'match/all', component: () => import('@/pages/match/all.vue'), meta: { module: 'fellowship', requiresAuth: true } },
    { path: 'dynamic', component: () => import('@/pages/dynamic/index.vue'), meta: { module: 'fellowship', requiresAuth: true } },
    { path: 'profile-edit', component: () => import('@/pages/fellowship/ProfileEditPage.vue'), meta: { module: 'fellowship', requiresAuth: true } },
    { path: 'profile', component: () => import('@/pages/fellowship/profile/index.vue'), meta: { module: 'fellowship', requiresAuth: true } },
    { path: 'profile/edit', component: () => import('@/pages/fellowship/profile/edit.vue'), meta: { module: 'fellowship', requiresAuth: true } },
    { path: 'settings', component: () => import('@/pages/fellowship/SettingsPage.vue'), meta: { module: 'fellowship', requiresAuth: true } },
    { path: 'verify', component: () => import('@/pages/fellowship/VerifyPage.vue'), meta: { module: 'fellowship', requiresAuth: true } },
    { path: 'preferences', component: () => import('@/pages/fellowship/PreferencesPage.vue'), meta: { module: 'fellowship', requiresAuth: true } },
    { path: 'blacklist', component: () => import('@/pages/fellowship/BlacklistPage.vue'), meta: { module: 'fellowship', requiresAuth: true } },
    { path: 'invite', component: () => import('@/pages/fellowship/InvitePage.vue'), meta: { module: 'fellowship', requiresAuth: true } },
    { path: 'my-likes', component: () => import('@/pages/personal/MyLikesPage.vue'), meta: { module: 'fellowship', requiresAuth: true } },
    { path: 'liked-me', component: () => import('@/pages/personal/LikedMePage.vue'), meta: { module: 'fellowship', requiresAuth: true } },
    { path: 'event-signups', component: () => import('@/pages/fellowship/EventSignupsPage.vue'), meta: { module: 'fellowship', requiresAuth: true } },
    { path: 'following', component: () => import('@/pages/personal/FollowingPage.vue'), meta: { module: 'fellowship', requiresAuth: true } },
    { path: 'privacy', component: () => import('@/pages/personal/PrivacySettingsPage.vue'), meta: { module: 'fellowship', requiresAuth: true } },
    { path: 'help', component: () => import('@/pages/personal/HelpFeedbackPage.vue'), meta: { module: 'fellowship', requiresAuth: true } },
    { path: 'newcomers', component: () => import('@/pages/newcomers/index.vue'), meta: { module: 'fellowship', requiresAuth: true } },
    { path: 'user-profile/:id', component: () => import('@/pages/fellowship/UserProfilePage.vue'), meta: { module: 'fellowship', requiresAuth: true } },
    { path: 'chat/:receiverId', component: () => import('@/pages/fellowship/ChatPage.vue'), meta: { module: 'fellowship', requiresAuth: true } },
    { path: 'vip', component: () => import('@/pages/fellowship/VipPage.vue'), meta: { module: 'fellowship', requiresAuth: true } },

    { path: 'home', redirect: '/fellowship/discover' },
    { path: 'message', redirect: '/fellowship/messages' },
    { path: 'personal', redirect: '/fellowship/me' }
  ]
}
