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
      <div class="ac-profile-side">
        <div class="ac-stat-strip">
          <div class="ac-stat-pill">
            <span class="ac-stat-value">{{ unreadTotal }}</span>
            <span class="ac-stat-name">未读消息</span>
          </div>
          <div class="ac-stat-pill">
            <span class="ac-stat-value">{{ certifiedCount }}/2</span>
            <span class="ac-stat-name">认证进度</span>
          </div>
          <div class="ac-stat-pill">
            <span class="ac-stat-value">{{ nextSteps.length }}</span>
            <span class="ac-stat-name">待办建议</span>
          </div>
        </div>
        <div class="ac-profile-actions">
          <router-link to="/fellowship/profile/edit" class="platform-btn platform-btn-primary ac-main-action">
            完善资料
          </router-link>
          <router-link v-if="userStore.isAdmin" to="/admin" class="platform-btn ac-admin-btn">
            管理后台
          </router-link>
        </div>
      </div>
    </div>

    <div class="platform-card ac-advice-card">
      <div class="ac-section-head">
        <h2 class="ac-section-title">下一步建议</h2>
      </div>
      <div class="ac-advice-grid">
        <router-link v-for="(item, index) in nextSteps" :key="item.key" :to="item.to" class="ac-advice-item">
          <span class="ac-advice-index">{{ index + 1 }}</span>
          <span class="ac-advice-copy">
            <span class="ac-advice-title">{{ item.title }}</span>
            <span class="ac-advice-desc">{{ item.desc }}</span>
          </span>
          <span class="ac-card-arrow">→</span>
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
          <span class="ac-mod-copy">
            <span class="ac-mod-name">{{ mod.name }}</span>
            <span class="ac-mod-desc">{{ mod.desc }}</span>
          </span>
          <span class="ac-card-arrow">→</span>
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
const unreadTotal = computed(() => unreadChat.value + unreadNotif.value)
const certifiedCount = computed(() => [livenessStatus.value, realnameStatus.value].filter((status) => status === 'approved').length)

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
  { key: 'fellowship', name: '联谊交友', desc: '资料、推荐与互动', to: '/fellowship/me', icon: ICONS.heart, color: '#f45b7a', bg: '#fff0f4' },
  { key: 'events', name: '活动中心', desc: '报名线下与主题活动', to: '/events', icon: ICONS.calendar, color: '#1f4fd8', bg: '#eff6ff' },
  { key: 'articles', name: '内容资讯', desc: '公告、资讯与精选内容', to: '/articles', icon: ICONS.article, color: '#059669', bg: '#f0fdf4' },
  { key: 'modules', name: '模块中心', desc: '查看平台能力矩阵', to: '/modules', icon: ICONS.grid, color: '#7c3aed', bg: '#f5f3ff' }
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
  display: grid;
  grid-template-columns: minmax(0, 1.18fr) minmax(380px, 0.82fr);
  gap: 20px;
  align-items: start;
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
  border-color: var(--lc-border);
  border-radius: var(--lc-radius);
  box-shadow: var(--lc-shadow-sm);
  padding: 22px;
}

.platform-btn-primary {
  background: var(--lc-blue);
  box-shadow: var(--lc-shadow-blue);
}

.ac-profile-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 30px;
  flex-wrap: wrap;
  padding: 28px 30px;
  background:
    linear-gradient(135deg, rgba(37, 99, 235, 0.08), rgba(255, 255, 255, 0) 42%),
    var(--lc-surface);
}

.ac-profile-main {
  display: flex;
  align-items: center;
  gap: 18px;
  flex: 1;
  min-width: 0;
}

.ac-avatar {
  width: 82px;
  height: 82px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid var(--lc-surface);
  box-shadow: 0 0 0 1px var(--lc-blue-border), var(--lc-shadow-blue);
}

.ac-avatar-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--lc-blue);
  color: var(--lc-surface);
  font-size: 26px;
  font-weight: 900;
}

.ac-username {
  margin: 0 0 4px;
  font-size: 24px;
  font-weight: 900;
  color: var(--lc-text);
}

.ac-phone {
  margin: 0 0 10px;
  font-size: 14px;
  color: var(--lc-muted);
}

.ac-profile-side {
  display: grid;
  gap: 14px;
  min-width: min(520px, 100%);
}

.ac-stat-strip {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.ac-stat-pill {
  padding: 13px 14px;
  border: 1px solid var(--lc-border);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.78);
}

.ac-stat-value,
.ac-stat-name {
  display: block;
}

.ac-stat-value {
  font-size: 20px;
  font-weight: 900;
  color: var(--lc-text);
  line-height: 1;
}

.ac-stat-name {
  margin-top: 7px;
  font-size: 12px;
  color: var(--lc-muted);
}

.ac-profile-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  flex-wrap: wrap;
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
  background: var(--lc-red-light);
  color: var(--lc-red);
}

.ac-badge--user {
  background: var(--lc-soft);
  color: var(--lc-muted);
}

.ac-badge--verified {
  background: var(--lc-green-light);
  color: var(--lc-green);
}

.ac-badge--pending {
  background: var(--lc-amber-light);
  color: var(--lc-amber);
}

.ac-badge--unverified {
  background: var(--lc-soft);
  color: var(--lc-subtle);
}

.ac-badge--achievement {
  --_c: #6d28d9;
  background: #ede9fe;
  color: var(--_c);
}

.ac-badge--banned {
  background: var(--lc-red-light);
  color: var(--lc-red);
}

.ac-admin-btn,
.ac-main-action {
  min-height: 44px;
  padding: 10px 20px;
  border-radius: var(--lc-radius-sm);
  font-size: 14px;
}

.ac-admin-btn {
  border: 1px solid var(--lc-blue-border);
  color: var(--lc-blue-dark);
  background: var(--lc-surface);
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
  font-size: 19px;
  font-weight: 900;
  color: var(--lc-text);
}

.ac-sec-link {
  font-size: 13px;
  font-weight: 700;
  color: var(--lc-blue-dark);
  text-decoration: none;
}

.ac-advice-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.ac-advice-item {
  display: grid;
  grid-template-columns: 34px minmax(0, 1fr) auto;
  align-items: center;
  gap: 12px;
  border: 1px solid var(--lc-border);
  border-radius: 12px;
  min-height: 82px;
  padding: 16px;
  text-decoration: none;
  background: linear-gradient(180deg, var(--lc-surface), var(--lc-bg));
  transition: var(--lc-transition);
}

.ac-advice-item:hover,
.ac-mod-card:hover {
  border-color: var(--lc-blue-border);
  box-shadow: var(--lc-shadow-sm);
  transform: translateY(-1px);
}

.ac-advice-index {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: var(--lc-blue-light);
  color: var(--lc-blue-dark);
  font-size: 14px;
  font-weight: 900;
}

.ac-advice-copy,
.ac-mod-copy {
  display: grid;
  gap: 5px;
  min-width: 0;
}

.ac-advice-title {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: var(--lc-text);
}

.ac-advice-desc {
  margin: 0;
  font-size: 12px;
  color: var(--lc-muted);
  line-height: 1.45;
}

.ac-card-arrow {
  color: var(--lc-subtle);
  font-size: 16px;
  font-weight: 900;
}

.ac-mod-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.ac-mod-card {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr) auto;
  align-items: center;
  gap: 12px;
  min-height: 108px;
  padding: 16px;
  border: 1px solid var(--lc-border);
  border-radius: 12px;
  text-decoration: none;
  color: inherit;
  background: var(--lc-surface);
  transition: var(--lc-transition);
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
  color: var(--lc-text);
}

.ac-mod-desc {
  color: var(--lc-muted);
  font-size: 12px;
  line-height: 1.45;
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
  min-height: 82px;
  padding: 18px 10px;
  background: var(--lc-bg);
  border-radius: 12px;
  border: 1px solid var(--lc-border);
}

.ac-msg-num {
  font-size: 28px;
  font-weight: 900;
  color: var(--lc-subtle);
  line-height: 1;
}

.ac-msg-num--has {
  color: var(--lc-blue);
}

.ac-msg-label {
  font-size: 12px;
  color: var(--lc-muted);
}

.ac-verify-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 13px 0;
  border-bottom: 1px solid var(--lc-soft);
}

.ac-verify-item:last-child {
  border-bottom: none;
}

.ac-verify-label {
  font-size: 14px;
  color: var(--lc-text);
  font-weight: 600;
}

.ac-verify-status {
  font-size: 12px;
  font-weight: 700;
  padding: 3px 10px;
  border-radius: 999px;
}

.ac-vstatus--ok {
  background: var(--lc-green-light);
  color: var(--lc-green);
}

.ac-vstatus--pending {
  background: var(--lc-amber-light);
  color: var(--lc-amber);
}

.ac-vstatus--fail {
  background: var(--lc-red-light);
  color: var(--lc-red);
}

.ac-vstatus--none {
  background: var(--lc-soft);
  color: var(--lc-subtle);
}

.ac-verify-hint {
  margin: 10px 0 0;
  font-size: 12px;
  color: var(--lc-subtle);
}

.ac-logout-card {
  padding: 18px 22px;
}

.ac-logout-btn {
  width: 100%;
  min-height: 44px;
  border: 1px solid var(--lc-pink-border);
  background: var(--lc-pink-light);
  color: var(--lc-red);
  font-size: 15px;
  font-weight: 800;
  border-radius: var(--lc-radius-sm);
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
    padding: 20px;
  }

  .ac-profile-side {
    min-width: 0;
    width: 100%;
  }

  .ac-stat-strip {
    grid-template-columns: 1fr;
  }

  .ac-profile-actions,
  .ac-admin-btn,
  .ac-main-action {
    width: 100%;
  }

  .ac-advice-grid,
  .ac-mod-grid,
  .ac-msg-grid {
    grid-template-columns: 1fr;
  }
}
</style>
