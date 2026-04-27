<template>
  <section class="platform-page module-page">
    <div class="platform-card ac-profile-card">
      <div class="ac-profile-main">
        <div class="ac-avatar-wrap">
          <img v-if="user?.avatar" :src="user.avatar" class="ac-avatar" alt="头像" />
          <div v-else class="ac-avatar ac-avatar-fallback">{{ avatarFallback }}</div>
        </div>
        <div class="ac-profile-info">
          <h1 class="ac-username">{{ user?.username || '用户' }}</h1>
          <p class="ac-phone">{{ user?.phone || '未绑定手机号' }}</p>
          <div class="ac-badges">
            <span class="ac-badge" :class="roleBadgeClass">{{ roleLabel }}</span>
            <span class="ac-badge" :class="verifyBadgeClass">{{ verifyLabel }}</span>
            <span v-if="isCertified" class="ac-badge ac-badge--achievement">已认证成就</span>
            <span v-if="user?.status === 'banned'" class="ac-badge ac-badge--banned">已封禁</span>
          </div>
        </div>
      </div>
      <router-link v-if="userStore.isAdmin" to="/admin" class="platform-btn platform-btn-primary ac-admin-btn">
        进入管理后台
      </router-link>
    </div>

    <div class="platform-card ac-advice-card">
      <div class="ac-section-head">
        <h2 class="ac-section-title">下一步建议</h2>
      </div>
      <div class="ac-advice-grid">
        <router-link v-for="item in nextSteps" :key="item.key" :to="item.to" class="ac-advice-item">
          <p class="ac-advice-title">{{ item.title }}</p>
          <p class="ac-advice-desc">{{ item.desc }}</p>
        </router-link>
      </div>
    </div>

    <div class="platform-card ac-modules-card">
      <div class="ac-section-head">
        <h2 class="ac-section-title">我的模块</h2>
        <router-link to="/modules" class="ac-sec-link">全部模块</router-link>
      </div>
      <div class="ac-mod-grid">
        <router-link v-for="mod in myModules" :key="mod.key" :to="mod.to" class="ac-mod-card">
          <span class="ac-mod-icon" :style="{ color: mod.color, background: mod.bg }">
            <svg viewBox="0 0 24 24" fill="currentColor" width="22" height="22">
              <path :d="mod.icon" />
            </svg>
          </span>
          <span class="ac-mod-name">{{ mod.name }}</span>
        </router-link>
      </div>
    </div>

    <div class="ac-two-col">
      <div class="platform-card">
        <div class="ac-section-head">
          <h2 class="ac-section-title">我的消息</h2>
          <router-link to="/fellowship/messages" class="ac-sec-link">进入消息</router-link>
        </div>
        <div class="ac-msg-grid">
          <div class="ac-msg-item">
            <span class="ac-msg-num" :class="{ 'ac-msg-num--has': unreadChat > 0 }">{{ unreadChat }}</span>
            <span class="ac-msg-label">私信未读</span>
          </div>
          <div class="ac-msg-item">
            <span class="ac-msg-num" :class="{ 'ac-msg-num--has': unreadNotif > 0 }">{{ unreadNotif }}</span>
            <span class="ac-msg-label">通知未读</span>
          </div>
        </div>
      </div>

      <div class="platform-card">
        <div class="ac-section-head">
          <h2 class="ac-section-title">我的认证</h2>
          <router-link to="/fellowship/verify" class="ac-sec-link">前往认证</router-link>
        </div>
        <div class="ac-verify-list">
          <div class="ac-verify-item">
            <span class="ac-verify-label">真人认证</span>
            <span class="ac-verify-status" :class="livenessClass">{{ livenessLabel }}</span>
          </div>
          <div class="ac-verify-item">
            <span class="ac-verify-label">实名认证</span>
            <span class="ac-verify-status" :class="realnameClass">{{ realnameLabel }}</span>
          </div>
        </div>
        <p v-if="verifyHint" class="ac-verify-hint">{{ verifyHint }}</p>
      </div>
    </div>

    <div class="platform-card ac-logout-card">
      <button type="button" class="ac-logout-btn" @click="handleLogout">退出登录</button>
    </div>
  </section>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user.js'
import { getUnreadCount } from '@/api/message.js'
import { getNotifUnreadCount } from '@/api/notification.js'
import { getMyVerifications } from '@/api/verification.js'

const router = useRouter()
const userStore = useUserStore()
const user = computed(() => userStore.userInfo)

const unreadChat = ref(0)
const unreadNotif = ref(0)
const livenessStatus = ref('none')
const realnameStatus = ref('none')

const avatarFallback = computed(() => {
  const name = user.value?.username || ''
  return name.charAt(0).toUpperCase() || 'U'
})

const roleLabel = computed(() => {
  const role = String(user.value?.role || '').toLowerCase()
  if (role === 'super_admin' || role === 'root') return '超级管理员'
  if (role === 'admin') return '管理员'
  return '普通用户'
})

const roleBadgeClass = computed(() => {
  const role = String(user.value?.role || '').toLowerCase()
  return ['admin', 'super_admin', 'root'].includes(role) ? 'ac-badge--admin' : 'ac-badge--user'
})

const verifyLabel = computed(() => {
  const status = user.value?.verificationStatus || 'none'
  if (status === 'approved') return '已认证'
  if (status === 'pending') return '认证中'
  if (status === 'rejected') return '认证失败'
  return '未认证'
})

const verifyBadgeClass = computed(() => {
  const status = user.value?.verificationStatus || 'none'
  if (status === 'approved') return 'ac-badge--verified'
  if (status === 'pending') return 'ac-badge--pending'
  return 'ac-badge--unverified'
})

const isCertified = computed(
  () => livenessStatus.value === 'approved' || realnameStatus.value === 'approved' || user.value?.verificationStatus === 'approved'
)

const nextSteps = computed(() => {
  const list = []
  if (!isCertified.value) list.push({ key: 'verify', title: '去真人认证', desc: '提升信任度和回复率', to: '/fellowship/verify' })
  list.push({ key: 'profile', title: '去完善资料', desc: '让系统更懂你，推荐更精准', to: '/fellowship/profile/edit' })
  list.push({ key: 'discover', title: '去查看推荐用户', desc: '从推荐列表开始互动', to: '/fellowship/discover' })
  list.push({ key: 'notice', title: '查看新通知', desc: '不错过系统和互动消息', to: '/fellowship/messages?tab=notification' })
  return list.slice(0, 4)
})

function statusLabel(status) {
  if (status === 'approved') return '已通过'
  if (status === 'pending') return '审核中'
  if (status === 'rejected') return '已驳回'
  return '未认证'
}

function statusClass(status) {
  if (status === 'approved') return 'ac-vstatus--ok'
  if (status === 'pending') return 'ac-vstatus--pending'
  if (status === 'rejected') return 'ac-vstatus--fail'
  return 'ac-vstatus--none'
}

const livenessLabel = computed(() => statusLabel(livenessStatus.value))
const realnameLabel = computed(() => statusLabel(realnameStatus.value))
const livenessClass = computed(() => statusClass(livenessStatus.value))
const realnameClass = computed(() => statusClass(realnameStatus.value))

const verifyHint = computed(() => {
  if (livenessStatus.value === 'none' && realnameStatus.value === 'none') {
    return '完成认证可提升匹配权重与平台可信度。'
  }
  if (livenessStatus.value === 'rejected' || realnameStatus.value === 'rejected') {
    return '部分认证被驳回，请根据提示重新提交。'
  }
  return ''
})

const ICONS = {
  heart: 'M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z',
  calendar: 'M17 12h-5v5h5v-5zM16 1v2H8V1H6v2H5c-1.11 0-1.99.9-1.99 2L3 19c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2h-1V1h-2zm3 18H5V8h14v11z',
  article: 'M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z',
  grid: 'M4 11h5V5H4v6zm0 7h5v-6H4v6zm6 0h5v-6h-5v6zm6 0h5v-6h-5v6zm-6-7h5V5h-5v6zm6-6v6h5V5h-5z'
}

const myModules = [
  { key: 'fellowship', name: '联谊交友', to: '/fellowship/me', icon: ICONS.heart, color: '#f45b7a', bg: '#fff0f4' },
  { key: 'events', name: '活动中心', to: '/events', icon: ICONS.calendar, color: '#1f4fd8', bg: '#eff6ff' },
  { key: 'articles', name: '内容资讯', to: '/articles', icon: ICONS.article, color: '#059669', bg: '#f0fdf4' },
  { key: 'modules', name: '模块中心', to: '/modules', icon: ICONS.grid, color: '#7c3aed', bg: '#f5f3ff' }
]

onMounted(async () => {
  if (!user.value) {
    await userStore.refreshCurrentUser().catch(() => {})
  }

  const [msgRes, notifRes, verifyRes] = await Promise.allSettled([
    getUnreadCount(),
    getNotifUnreadCount(),
    getMyVerifications()
  ])

  if (msgRes.status === 'fulfilled') {
    const data = msgRes.value || {}
    unreadChat.value = data.chatCount ?? data.unreadChat ?? data.unread ?? data.count ?? 0
  }

  if (notifRes.status === 'fulfilled') {
    const data = notifRes.value || {}
    unreadNotif.value = data.unreadCount ?? data.count ?? data.unread ?? (typeof data === 'number' ? data : 0)
  }

  if (verifyRes.status === 'fulfilled') {
    const list = Array.isArray(verifyRes.value) ? verifyRes.value : []
    const liveness = list.find((item) => (item.verifyType || item.type || '').includes('liveness'))
    const realname = list.find((item) => (item.verifyType || item.type || '').includes('realname'))
    if (liveness) livenessStatus.value = liveness.status || 'none'
    if (realname) realnameStatus.value = realname.status || 'none'
    if (!liveness && !realname && user.value?.verificationStatus === 'approved') {
      livenessStatus.value = 'approved'
    }
  }
})

function handleLogout() {
  userStore.logout()
  router.push('/')
}
</script>

<style scoped>
.module-page {
  --platform-primary: #2563eb;
  --platform-primary-dark: #1e40af;
  --platform-border: #e5e7eb;
  --platform-radius-card: 16px;
  --platform-radius-btn: 8px;
  --platform-shadow-card: 0 14px 36px rgba(15, 23, 42, 0.06);
  --platform-shadow-btn: 0 12px 26px rgba(37, 99, 235, 0.22);
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(360px, 1fr);
  gap: 18px;
}

.ac-profile-card,
.ac-two-col,
.ac-logout-card {
  grid-column: 1 / -1;
}

.ac-advice-card {
  grid-column: 1 / 2;
}

.ac-modules-card {
  grid-column: 2 / 3;
}

.platform-card {
  border-color: var(--platform-border);
  border-radius: var(--platform-radius-card);
  box-shadow: var(--platform-shadow-card);
  padding: 24px;
}

.platform-btn-primary {
  background: var(--platform-primary);
  box-shadow: var(--platform-shadow-btn);
}

.ac-profile-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  flex-wrap: wrap;
}

.ac-profile-main {
  display: flex;
  align-items: center;
  gap: 18px;
  flex: 1;
  min-width: 0;
}

.ac-avatar {
  width: 76px;
  height: 76px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #dbeafe;
}

.ac-avatar-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #2563eb;
  color: #fff;
  font-size: 26px;
  font-weight: 900;
}

.ac-username {
  margin: 0 0 4px;
  font-size: 24px;
  font-weight: 900;
  color: #0f172a;
}

.ac-phone {
  margin: 0 0 10px;
  font-size: 14px;
  color: #64748b;
}

.ac-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.ac-badge {
  font-size: 11px;
  font-weight: 700;
  padding: 3px 9px;
  border-radius: 999px;
}

.ac-badge--admin {
  background: #ffe4e6;
  color: #be123c;
}

.ac-badge--user {
  background: #f1f5f9;
  color: #475569;
}

.ac-badge--verified {
  background: #dcfce7;
  color: #15803d;
}

.ac-badge--pending {
  background: #fef9c3;
  color: #854d0e;
}

.ac-badge--unverified {
  background: #f1f5f9;
  color: #94a3b8;
}

.ac-badge--achievement {
  background: #ede9fe;
  color: #6d28d9;
}

.ac-badge--banned {
  background: #fef2f2;
  color: #dc2626;
}

.ac-admin-btn {
  min-height: 44px;
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
}

.ac-section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.ac-section-title {
  margin: 0;
  font-size: 18px;
  font-weight: 900;
  color: #0f172a;
}

.ac-sec-link {
  font-size: 13px;
  font-weight: 700;
  color: var(--platform-primary-dark);
  text-decoration: none;
}

.ac-advice-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.ac-advice-item {
  display: block;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 14px;
  text-decoration: none;
  background: #fff;
}

.ac-advice-title {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: #111827;
}

.ac-advice-desc {
  margin: 6px 0 0;
  font-size: 12px;
  color: #64748b;
  line-height: 1.6;
}

.ac-mod-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.ac-mod-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  min-height: 120px;
  padding: 16px 10px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  text-decoration: none;
  color: inherit;
  background: #fff;
}

.ac-mod-icon {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ac-mod-name {
  font-size: 14px;
  font-weight: 700;
  color: #374151;
}

.ac-two-col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
}

.ac-msg-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.ac-msg-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 16px 10px;
  background: #f8fafc;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
}

.ac-msg-num {
  font-size: 28px;
  font-weight: 900;
  color: #94a3b8;
  line-height: 1;
}

.ac-msg-num--has {
  color: var(--platform-primary);
}

.ac-msg-label {
  font-size: 12px;
  color: #64748b;
}

.ac-verify-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 13px 0;
  border-bottom: 1px solid #f1f5f9;
}

.ac-verify-item:last-child {
  border-bottom: none;
}

.ac-verify-label {
  font-size: 14px;
  color: #374151;
  font-weight: 600;
}

.ac-verify-status {
  font-size: 12px;
  font-weight: 700;
  padding: 3px 10px;
  border-radius: 999px;
}

.ac-vstatus--ok {
  background: #dcfce7;
  color: #15803d;
}

.ac-vstatus--pending {
  background: #fef9c3;
  color: #854d0e;
}

.ac-vstatus--fail {
  background: #fef2f2;
  color: #dc2626;
}

.ac-vstatus--none {
  background: #f1f5f9;
  color: #94a3b8;
}

.ac-verify-hint {
  margin: 10px 0 0;
  font-size: 12px;
  color: #94a3b8;
}

.ac-logout-card {
  padding: 18px 24px;
}

.ac-logout-btn {
  width: 100%;
  min-height: 44px;
  border: 1px solid #fecdd3;
  background: #fff8fa;
  color: #e11d48;
  font-size: 15px;
  font-weight: 800;
  border-radius: 8px;
}

@media (max-width: 1023px) {
  .module-page {
    grid-template-columns: 1fr;
  }

  .ac-advice-card,
  .ac-modules-card {
    grid-column: 1 / -1;
  }

  .ac-advice-grid,
  .ac-mod-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .ac-two-col {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 767px) {
  .module-page {
    gap: 14px;
  }

  .platform-card {
    border-radius: 14px;
    padding: 18px;
  }

  .ac-profile-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 14px;
  }

  .ac-admin-btn {
    width: 100%;
  }

  .ac-advice-grid,
  .ac-mod-grid {
    grid-template-columns: 1fr;
  }
}
</style>
