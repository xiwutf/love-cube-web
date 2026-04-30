<template>
  <section class="platform-page me-page">
    <main class="me-shell" aria-label="平台个人中心（移动端）">
      <header class="me-header">
        <div>
          <h1>个人中心</h1>
          <p>管理你的内容、团队、通知与账号资料</p>
        </div>
        <button type="button" class="icon-button" aria-label="账号设置" @click="openSettingsPanel">⚙</button>
      </header>

      <div class="me-layout" aria-label="个人中心主体布局">
        <section class="dashboard-card profile-card">
          <div class="profile-main">
            <img v-if="user?.avatar" :src="user.avatar" class="profile-avatar" alt="头像" />
            <div v-else class="profile-avatar avatar-fallback">{{ avatarFallback }}</div>

            <div class="profile-info">
              <div class="profile-name-row">
                <h2>{{ displayName }}</h2>
                <span class="creator-tag">平台创作者</span>
              </div>
              <p>ID：{{ userIdDisplay }}</p>
              <p>注册地：{{ locationDisplay }}</p>
              <div class="invite-row">
                <span>邀请码：{{ inviteCodeDisplay }}</span>
                <button type="button" :disabled="!inviteCodeDisplay" @click="copyInviteCode">复制</button>
                <em v-if="copyFeedback" :class="{ 'is-error': copyFeedbackError }">{{ copyFeedback }}</em>
              </div>
            </div>

            <div class="profile-badges" aria-label="平台徽章">
              <span class="tone-amber">☆</span>
              <span class="tone-rose">♨</span>
              <span class="tone-blue">◇</span>
            </div>
          </div>

          <div class="profile-light-stats">
            <span v-for="item in profileLightStats" :key="item.label">
              <small>{{ item.label }}</small>
              <strong>{{ item.value }}</strong>
            </span>
          </div>

          <div class="profile-actions">
            <button type="button" class="outline-action profile-edit-action" @click="openEditPanel">编辑资料</button>
            <router-link class="primary-action fellowship-action" to="/fellowship">切换联谊中心</router-link>
          </div>
        </section>

        <aside class="me-aside">
          <section class="dashboard-card growth-card">
            <div class="section-head">
              <h2>成长等级</h2>
              <router-link to="/modules">成长记录 ></router-link>
            </div>
            <div class="growth-body">
              <div class="level-badge">Lv.{{ growthLevel.level }}</div>
              <div class="level-content">
                <h3>{{ growthLevel.name }}</h3>
                <p>再获得 {{ growthLevel.nextExp - growthLevel.currentExp }} 经验可升级 Lv.{{ growthLevel.level + 1 }}</p>
                <div class="progress-track">
                  <span :style="{ width: growthProgress }"></span>
                </div>
                <div class="progress-meta">
                  <span>{{ growthLevel.currentExp }} / {{ growthLevel.nextExp }}</span>
                </div>
              </div>
              <ul class="exp-list" aria-label="经验来源">
                <li v-for="item in growthLevel.sources" :key="item.label">
                  <span>{{ item.label }}</span>
                  <strong>+{{ item.exp }} 经验</strong>
                </li>
              </ul>
            </div>
          </section>

          <section class="dashboard-card task-card">
            <div class="section-head task-head">
              <div>
                <h2>今日任务 <span>每日 0 点刷新</span></h2>
              </div>
              <p>已完成 {{ completedTaskCount }}/{{ dailyTasks.length }} <b aria-hidden="true">礼</b></p>
            </div>
            <div class="task-grid">
              <article v-for="task in dailyTasks" :key="task.title" class="task-item" :class="{ 'is-done': task.done }">
                <div class="task-check">{{ task.done ? '✓' : task.current }}</div>
                <h3>{{ task.title }}</h3>
                <p>+{{ task.exp }} 经验</p>
                <div v-if="!task.done" class="mini-progress">
                  <span :style="{ width: `${Math.min(100, (task.current / task.total) * 100)}%` }"></span>
                </div>
                <small>{{ task.done ? '已完成' : `${task.current}/${task.total}` }}</small>
                <router-link :to="task.to">{{ task.done ? '已完成' : '去完成' }}</router-link>
              </article>
            </div>
          </section>

          <section class="dashboard-card badge-card">
            <div class="section-head">
              <h2>我的徽章</h2>
            </div>
            <div class="badge-list">
              <article v-for="badge in badges" :key="badge.code" :class="{ locked: !badge.unlocked }">
                <strong>{{ badge.name }}</strong>
                <small>{{ badge.progress }}/{{ badge.conditionValue }}</small>
              </article>
            </div>
          </section>
        </aside>

        <div class="me-main-stack">
          <section class="workspace-grid">
            <router-link v-for="item in workspaceItems" :key="item.title" :to="item.to" class="workspace-item dashboard-card">
              <span class="soft-icon" :class="`tone-${item.tone}`">{{ item.icon }}</span>
              <strong>{{ item.title }}</strong>
              <small>{{ item.desc }}</small>
              <em>{{ item.value }} ></em>
            </router-link>
          </section>

          <section class="dashboard-card overview-card">
            <div class="section-head">
              <h2>数据概览</h2>
              <router-link to="/modules">全部数据 ></router-link>
            </div>
            <div class="overview-row">
              <router-link v-for="item in overviewItems" :key="item.label" :to="item.to" class="overview-item">
                <span class="soft-icon" :class="`tone-${item.tone}`">{{ item.icon }}</span>
                <strong>{{ item.value }}</strong>
                <small>{{ item.label }}</small>
              </router-link>
            </div>
          </section>

          <section class="dashboard-card group-card">
            <div class="section-head">
              <h2>我的团体</h2>
              <router-link to="/platform/me/groups">全部团体 ></router-link>
            </div>
            <div class="group-summary">
              <div class="group-avatar" aria-hidden="true">团</div>
              <div>
                <h3>{{ groupInfo.name }} <span>{{ groupInfo.role }}</span></h3>
                <p>成员 {{ groupInfo.members }} 人 <i></i> 活跃度：{{ groupInfo.activity }}</p>
                <p>本周贡献 {{ groupInfo.weekExp }} 经验</p>
              </div>
            </div>
            <div class="group-actions">
              <router-link class="group-enter-action" to="/platform/me/groups">进入团体</router-link>
              <router-link class="group-post-action" to="/platform/groups">发布公告</router-link>
              <router-link class="group-member-action" to="/platform/me/groups">成员管理</router-link>
            </div>
            <div class="ranking-head">
              <h3>团体活跃榜（本周）</h3>
              <router-link to="/platform/groups">查看完整榜单 ></router-link>
            </div>
            <ol class="ranking-list">
              <li v-for="item in groupRanking" :key="item.name">
                <span class="rank-medal">{{ item.rank }}</span>
                <strong>{{ item.name }}</strong>
                <small>活跃度 {{ item.activity }}</small>
              </li>
            </ol>
          </section>

          <section class="quick-grid">
            <router-link v-for="item in quickActions" :key="item.title" :to="item.to" class="quick-item dashboard-card">
              <span class="soft-icon" :class="`tone-${item.tone}`">{{ item.icon }}</span>
              <strong>{{ item.title }}</strong>
              <small>{{ item.desc }}</small>
            </router-link>
          </section>
        </div>
      </div>
    </main>

    <div v-if="editOpen" class="me-modal-backdrop" @click.self="closeEditPanel">
      <section class="me-modal" role="dialog" aria-modal="true" aria-label="编辑个人资料">
        <div class="me-modal-head">
          <h3>编辑个人资料</h3>
          <button type="button" aria-label="关闭" @click="closeEditPanel">×</button>
        </div>
        <form class="me-edit-form" @submit.prevent="handleSaveProfile">
          <label>
            <span>昵称</span>
            <input v-model.trim="editForm.username" type="text" maxlength="20" placeholder="请输入昵称" />
          </label>
          <label>
            <span>头像</span>
            <div class="avatar-uploader">
              <img v-if="editForm.avatar" :src="editForm.avatar" alt="头像预览" />
              <div v-else>{{ avatarFallback }}</div>
              <button type="button" :disabled="uploading || saving" @click="handlePickAvatar">
                {{ uploading ? '上传中...' : '选择图片' }}
              </button>
            </div>
          </label>
          <label>
            <span>所在地</span>
            <input v-model.trim="editForm.location" type="text" maxlength="60" placeholder="例如：河北省 保定市" />
          </label>
          <label>
            <span>个人简介</span>
            <textarea v-model.trim="editForm.bio" rows="3" maxlength="200" placeholder="简单介绍一下你自己" />
          </label>
          <p v-if="saveMessage" class="save-message" :class="{ 'is-error': saveError }">{{ saveMessage }}</p>
          <div class="modal-actions">
            <button type="button" class="outline-action" :disabled="saving" @click="resetEditForm">重置</button>
            <button type="submit" class="primary-action" :disabled="saving">{{ saving ? '保存中...' : '保存资料' }}</button>
          </div>
        </form>
      </section>
    </div>

    <div v-if="settingsOpen" class="me-modal-backdrop" @click.self="closeSettingsPanel">
      <section class="me-modal" role="dialog" aria-modal="true" aria-label="账号设置">
        <div class="me-modal-head">
          <h3>账号设置</h3>
          <button type="button" aria-label="关闭" @click="closeSettingsPanel">×</button>
        </div>
        <div class="settings-summary">
          <div class="profile-avatar avatar-fallback">{{ avatarFallback }}</div>
          <div>
            <strong>{{ displayName }}</strong>
            <span>{{ roleLabel }}</span>
          </div>
        </div>
        <div class="settings-list">
          <div><span>账号 ID</span><strong>{{ userIdDisplay }}</strong></div>
          <div><span>手机号</span><strong>{{ user?.phone || '未绑定' }}</strong></div>
          <div><span>认证状态</span><strong>{{ verifyLabel }}</strong></div>
          <div><span>注册时间</span><strong>{{ registerDate }}</strong></div>
        </div>
        <div class="modal-actions settings-actions">
          <button type="button" class="primary-action" @click="openEditPanel">编辑个人资料</button>
          <button type="button" class="outline-action" @click="goChangePasswordPage">修改密码</button>
          <button type="button" class="danger-action" @click="handleLogout">退出登录</button>
        </div>
      </section>
    </div>
  </section>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user.js'
import { getNotifUnreadCountCached } from '@/api/notification.js'
import { getUserStatsCached, updateProfile } from '@/api/user.js'
import { getMyInviteCode } from '@/api/invite.js'
import { getMyGrowth } from '@/api/growth.js'
import { useImageUpload } from '@/composables/useImageUpload.js'

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const user = computed(() => userStore.userInfo)
const unreadCount = ref(0)
const editOpen = ref(false)
const settingsOpen = ref(false)
const saving = ref(false)
const saveMessage = ref('')
const saveError = ref(false)
const { pickAndUpload, uploading } = useImageUpload()
const inviteCode = ref('')
const copyFeedback = ref('')
const copyFeedbackError = ref(false)
const editForm = reactive({
  username: '',
  avatar: '',
  location: '',
  bio: ''
})

const myContentCount = ref(0)
const myEventCount = ref(0)
const myFavoriteCount = ref(0)
const growthInfo = ref(null)
const badges = ref([])

const growthLevel = computed(() => {
  const data = growthInfo.value
  return {
    level: Number(data?.level ?? 1),
    name: data?.title || '新手用户',
    currentExp: Number(data?.exp ?? 0),
    nextExp: Number(data?.nextLevelExp ?? 100),
    sources: [
      { label: '登录', exp: 2 },
      { label: '发布内容', exp: 10 },
      { label: '浏览内容', exp: 1 },
      { label: '点赞内容', exp: 2 }
    ]
  }
})

const dailyTasks = computed(() => {
  const codeToRoute = {
    DAILY_LOGIN: '/m/platform/me',
    DAILY_POST: '/platform/positive-share',
    DAILY_VIEW: '/articles',
    DAILY_LIKE: '/platform/positive-share'
  }
  const rows = growthInfo.value?.dailyTasks
  if (!Array.isArray(rows)) return []
  return rows.map(item => ({
    title: item.name || item.code,
    exp: Number(item.rewardExp ?? 0),
    current: Number(item.progress ?? 0),
    total: Number(item.targetCount ?? 1),
    done: Boolean(item.completed),
    to: codeToRoute[item.code] || '/m/platform/me'
  }))
})

const groupInfo = {
  name: 'LoveCube 官方团队',
  role: '管理员',
  members: 23,
  activity: '中等',
  weekExp: 120
}

const groupRanking = [
  { rank: 1, name: 'LoveCube 官方团队', activity: 320 },
  { rank: 2, name: '星空联谊社', activity: 280 },
  { rank: 3, name: '绿来是你', activity: 210 }
]

const displayName = computed(() => user.value?.username || user.value?.nickname || 'LoveCube 官方团队')
const userIdDisplay = computed(() => user.value?.id || user.value?.userId || '1')
const locationDisplay = computed(() => user.value?.location || '河北省 保定市')
const avatarFallback = computed(() => String(displayName.value || 'L').slice(0, 1).toUpperCase())
const inviteCodeDisplay = computed(() => inviteCode.value || 'LC69UWM')
const growthProgress = computed(() => {
  const nextExp = Math.max(1, Number(growthLevel.value.nextExp))
  const currentExp = Number(growthLevel.value.currentExp)
  return `${Math.min(100, Math.round((currentExp / nextExp) * 100))}%`
})
const completedTaskCount = computed(() => dailyTasks.value.filter(item => item.done).length)

const roleLabel = computed(() => {
  const role = String(user.value?.role || '').toLowerCase()
  if (role === 'admin' || role === 'super_admin' || role === 'root') return '平台管理员'
  return '普通用户'
})

const verifyLabel = computed(() => {
  const status = String(user.value?.verificationStatus || 'none')
  if (status === 'approved') return '账号已认证'
  if (status === 'pending') return '认证审核中'
  if (status === 'rejected') return '认证未过'
  return '账号未认证'
})

const registerDate = computed(() => {
  const raw = user.value?.createdAt
  if (!raw) return '--'
  return String(raw).replace('T', ' ').slice(0, 10)
})

const profileLightStats = [
  { label: '关注', value: '12' },
  { label: '粉丝', value: '236' },
  { label: '获赞', value: '1,234' }
]

const workspaceItems = computed(() => [
  { title: '我的内容', desc: '发布、管理文章', value: `${myContentCount.value} 篇内容`, icon: '▤', tone: 'violet', to: '/platform/positive-share' },
  { title: '每日心声', desc: '记录每日想法', value: `${Math.min(myContentCount.value, 7)} 条心声`, icon: '♡', tone: 'rose', to: '/platform/positive-share' },
  { title: '活动中心', desc: '查看活动参与', value: `${myEventCount.value} 个活动`, icon: '▣', tone: 'amber', to: '/events' },
  { title: '消息中心', desc: '系统通知与互动', value: unreadCount.value > 0 ? `${unreadCount.value} 条未读` : '暂无未读', icon: '●', tone: 'blue', to: '/messages' }
])

const overviewItems = computed(() => [
  { label: '发布内容', value: myContentCount.value, icon: '↗', tone: 'violet', to: '/platform/positive-share' },
  { label: '活动参与', value: myEventCount.value, icon: '✦', tone: 'rose', to: '/events' },
  { label: '收藏内容', value: myFavoriteCount.value, icon: '☆', tone: 'amber', to: '/me/favorites' },
  { label: '互动热度', value: '--', icon: '♨', tone: 'green', to: '/platform/positive-share' },
  { label: '当前等级', value: `Lv.${growthLevel.value.level}`, icon: '◇', tone: 'blue', to: '/modules' }
])

const quickActions = computed(() => [
  { title: '内容中心', desc: '管理文章和内容', icon: '▤', tone: 'violet', to: '/platform/positive-share' },
  { title: '模块中心', desc: '管理平台模块', icon: '▦', tone: 'green', to: '/modules' },
  { title: '通知中心', desc: '查看系统通知', icon: '●', tone: 'rose', to: '/messages' },
  { title: '账号设置', desc: '账号与安全', icon: '◇', tone: 'green', to: { path: '/m/platform/me', query: { panel: 'settings' } } },
  { title: '邀请码', desc: '邀请好友加入', icon: '▭', tone: 'amber', to: '/fellowship/invite' },
  { title: '编辑资料', desc: '修改个人资料', icon: '✎', tone: 'blue', to: { path: '/m/platform/me', query: { panel: 'edit' } } }
])

async function refreshUnreadCount() {
  const notifRes = await getNotifUnreadCountCached().catch(() => null)
  unreadCount.value = Number(notifRes?.count ?? notifRes?.unreadCount ?? 0)
}

function handleNotifReadAll() {
  unreadCount.value = 0
}

watch(
  () => user.value,
  (value) => {
    editForm.username = value?.username || ''
    editForm.avatar = value?.avatar || ''
    editForm.location = value?.location || ''
    editForm.bio = value?.bio || ''
  },
  { immediate: true }
)

function openEditPanel() {
  editOpen.value = true
  settingsOpen.value = false
  saveMessage.value = ''
  saveError.value = false
}

function closeEditPanel() {
  editOpen.value = false
}

function openSettingsPanel() {
  settingsOpen.value = true
  editOpen.value = false
  if (route.query?.panel !== 'settings') {
    router.replace({ path: '/m/platform/me', query: { ...route.query, panel: 'settings' } })
  }
}

function closeSettingsPanel() {
  settingsOpen.value = false
}

function resetEditForm() {
  editForm.username = user.value?.username || ''
  editForm.avatar = user.value?.avatar || ''
  editForm.location = user.value?.location || ''
  editForm.bio = user.value?.bio || ''
  saveMessage.value = ''
  saveError.value = false
}

async function handlePickAvatar() {
  saveMessage.value = ''
  saveError.value = false
  try {
    const avatarUrl = await pickAndUpload({ quality: 0.8 })
    if (!avatarUrl) throw new Error('上传失败，请重试')
    editForm.avatar = avatarUrl
  } catch (error) {
    saveError.value = true
    saveMessage.value = error?.message || '头像上传失败，请稍后重试'
  }
}

async function handleSaveProfile() {
  saveMessage.value = ''
  saveError.value = false
  const username = editForm.username.trim()
  if (!username) {
    saveError.value = true
    saveMessage.value = '昵称不能为空'
    return
  }
  if (username.length > 20) {
    saveError.value = true
    saveMessage.value = '昵称最多 20 个字符'
    return
  }
  saving.value = true
  try {
    await updateProfile({
      username,
      profilePhoto: editForm.avatar,
      location: editForm.location,
      bio: editForm.bio
    })
    await userStore.refreshCurrentUser().catch(() => {})
    saveMessage.value = '资料已更新'
    window.setTimeout(() => {
      if (!saveError.value) closeEditPanel()
    }, 600)
  } catch (error) {
    saveError.value = true
    saveMessage.value = error?.message || '保存失败，请稍后重试'
  } finally {
    saving.value = false
  }
}

function handleLogout() {
  userStore.logout()
  router.replace('/login')
}

function goChangePasswordPage() {
  closeSettingsPanel()
  router.push('/fellowship/change-password')
}

async function copyInviteCode() {
  if (!inviteCodeDisplay.value) return
  copyFeedback.value = ''
  copyFeedbackError.value = false
  try {
    if (navigator?.clipboard?.writeText) {
      await navigator.clipboard.writeText(inviteCodeDisplay.value)
    } else {
      const input = document.createElement('input')
      input.value = inviteCodeDisplay.value
      document.body.appendChild(input)
      input.select()
      document.execCommand('copy')
      document.body.removeChild(input)
    }
    copyFeedback.value = '已复制'
    window.setTimeout(() => { copyFeedback.value = '' }, 2000)
  } catch {
    copyFeedbackError.value = true
    copyFeedback.value = '复制失败'
    window.setTimeout(() => { copyFeedback.value = '' }, 2000)
  }
}

onMounted(async () => {
  window.addEventListener('platform-notif-read-all', handleNotifReadAll)
  if (route.query?.panel === 'settings') openSettingsPanel()
  if (route.query?.panel === 'edit') openEditPanel()
  if (!user.value) await userStore.refreshCurrentUser().catch(() => {})
  await refreshUnreadCount()
  const [statsRes, inviteRes, growthRes] = await Promise.allSettled([getUserStatsCached(), getMyInviteCode(), getMyGrowth()])
  if (statsRes.status === 'fulfilled' && statsRes.value) {
    myContentCount.value = Number(statsRes.value.contentCount ?? 0)
    myEventCount.value = Number(statsRes.value.eventCount ?? 0)
    myFavoriteCount.value = Number(statsRes.value.favoriteCount ?? 0)
  }
  if (inviteRes.status === 'fulfilled') {
    inviteCode.value = String(inviteRes.value?.inviteCode || inviteRes.value?.code || '').trim()
  }
  if (growthRes.status === 'fulfilled') {
    growthInfo.value = growthRes.value || null
    badges.value = Array.isArray(growthRes.value?.badges) ? growthRes.value.badges : []
  }
})

watch(
  () => route.query?.panel,
  (panel) => {
    if (panel === 'settings') openSettingsPanel()
    if (panel === 'edit') openEditPanel()
  }
)

onBeforeUnmount(() => {
  window.removeEventListener('platform-notif-read-all', handleNotifReadAll)
})
</script>

<style scoped>
.me-page {
  --me-bg: #f6f7fb;
  --me-bg-soft: #f8fafc;
  --me-card: #ffffff;
  --me-border: #eef0f4;
  --me-text: #0f172a;
  --me-muted: #64748b;
  --me-primary: #6d5dfb;
  --me-primary-dark: #4f46e5;
  --me-white: #ffffff;
  --me-success: #059669;
  --me-danger: #dc2626;
  --me-text-soft: #334155;
  --me-border-strong: #dfe4ee;
  --me-violet: #4f46e5;
  --me-rose: #db2777;
  --me-amber: #d97706;
  --me-blue: #2563eb;
  --me-medal: #9a6700;
  --me-orange: #f97316;
  --me-shadow: 0 14px 34px rgba(15, 23, 42, 0.06);
  min-height: 100vh;
  background: var(--me-bg);
  color: var(--me-text);
}

.me-shell {
  width: min(100%, 560px);
  margin: 0 auto;
  padding: 12px 10px calc(72px + env(safe-area-inset-bottom));
}

.me-layout {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.me-aside,
.me-main-stack {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.me-layout .growth-card,
.me-layout .task-card,
.me-main-stack .workspace-grid,
.me-main-stack .overview-card,
.me-main-stack .group-card,
.me-main-stack .quick-grid {
  margin-top: 0;
}

.me-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 10px 8px 12px;
}

.me-header h1 {
  margin: 0;
  font-size: 40px;
  line-height: 1.12;
  font-weight: 800;
  letter-spacing: 0;
}

.me-header p {
  margin: 6px 0 0;
  color: var(--me-muted);
  font-size: 18px;
  line-height: 1.45;
}

.icon-button {
  width: 38px;
  height: 38px;
  border: 0;
  border-radius: 50%;
  background: transparent;
  color: var(--me-text);
  font-size: 22px;
  cursor: pointer;
}

.dashboard-card {
  border: 1px solid var(--me-border);
  border-radius: 16px;
  background: var(--me-card);
  box-shadow: var(--me-shadow);
}

.profile-card {
  padding: 10px 10px;
  position: relative;
}

.profile-main {
  display: grid;
  grid-template-columns: 64px minmax(0, 1fr);
  gap: 10px;
  align-items: start;
}

.profile-avatar {
  width: 64px;
  height: 64px;
  flex: 0 0 auto;
  border-radius: 50%;
  object-fit: cover;
  box-shadow: 0 10px 24px rgba(79, 70, 229, 0.16);
}

.avatar-fallback {
  display: grid;
  place-items: center;
  color: var(--me-white);
  background: linear-gradient(135deg, var(--me-primary), var(--me-primary-dark));
  font-size: 22px;
  font-weight: 800;
}

.profile-info {
  min-width: 0;
  padding-right: 118px;
}

.profile-badges {
  position: absolute;
  right: 12px;
  top: 14px;
  display: flex;
  gap: 8px;
}

.profile-badges span {
  position: relative;
  display: grid;
  place-items: center;
  width: 30px;
  height: 30px;
  border: 1px solid rgba(255, 255, 255, 0.7);
  border-radius: 10px;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.9), 0 12px 24px rgba(15, 23, 42, 0.08);
  font-size: 12px;
  font-weight: 900;
}

.profile-badges .tone-amber { background: linear-gradient(145deg, #fff8e8, #fff0c9); }
.profile-badges .tone-rose { background: linear-gradient(145deg, #fff1f7, #ffe3ef); }
.profile-badges .tone-blue { background: linear-gradient(145deg, #eef6ff, #e1edff); }

.profile-name-row {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.profile-name-row h2 {
  margin: 0;
  font-size: 18px;
  line-height: 1.25;
  font-weight: 800;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.creator-tag {
  border-radius: 999px;
  padding: 2px 8px;
  color: var(--me-primary-dark);
  background: #f1efff;
  font-size: 10px;
  font-weight: 700;
}

.profile-info p,
.invite-row {
  margin: 4px 0 0;
  color: var(--me-muted);
  font-size: 11px;
}

.invite-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.invite-row button {
  border: 0;
  border-radius: 999px;
  padding: 2px 8px;
  color: var(--me-primary-dark);
  background: #f0efff;
  font-size: 10px;
  font-weight: 800;
  cursor: pointer;
}

.invite-row em,
.save-message {
  color: var(--me-success);
  font-size: 12px;
  font-style: normal;
}

.invite-row em.is-error,
.save-message.is-error {
  color: var(--me-danger);
}

.profile-light-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  width: 100%;
  margin: 8px 0 0;
  padding: 4px 6px;
  border: 1px solid rgba(226, 232, 240, 0.72);
  border-radius: 12px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.94), rgba(248, 250, 252, 0.76));
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.96), 0 10px 22px rgba(15, 23, 42, 0.04);
}

.profile-light-stats span {
  position: relative;
  display: grid;
  gap: 3px;
  text-align: center;
}

.profile-light-stats span + span::before {
  position: absolute;
  left: -5px;
  top: 7px;
  width: 1px;
  height: 28px;
  background: linear-gradient(180deg, rgba(226, 232, 240, 0), #dbe1ea, rgba(226, 232, 240, 0));
  content: "";
}

.profile-light-stats small,
.overview-item small,
.quick-item small,
.workspace-item small {
  color: var(--me-muted);
  font-size: 10px;
  line-height: 1.15;
}

.profile-light-stats strong {
  font-size: 14px;
}

.profile-actions,
.modal-actions,
.group-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  margin-top: 8px;
}

.profile-actions {
  margin-top: 6px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.primary-action,
.outline-action,
.danger-action,
.group-actions a,
.task-item a {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 36px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 800;
  text-decoration: none;
  cursor: pointer;
}

.profile-edit-action::before { margin-right: 8px; content: "✎"; font-size: 15px; }
.fellowship-action::before { margin-right: 8px; content: "♡"; font-size: 16px; }
.group-enter-action::before { margin-right: 6px; content: "↗"; font-size: 13px; }
.group-post-action::before { margin-right: 6px; content: "✈"; font-size: 13px; }
.group-member-action::before { margin-right: 6px; content: "♙"; font-size: 13px; }

.primary-action {
  border: 0;
  color: var(--me-white);
  background: linear-gradient(135deg, var(--me-primary), var(--me-primary-dark));
}

.outline-action,
.group-actions a {
  border: 1px solid #dfe4ee;
  color: var(--me-text);
  background: #ffffff;
}

.danger-action {
  border: 1px solid #fecaca;
  color: var(--me-danger);
  background: #fff5f5;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 6px;
}

.section-head h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
}

.section-head a,
.ranking-head a {
  color: var(--me-muted);
  font-size: 10px;
  font-weight: 700;
  text-decoration: none;
  white-space: nowrap;
}

.growth-card,
.task-card,
.badge-card,
.overview-card,
.group-card {
  margin-top: 8px;
  padding: 9px;
}

.growth-body {
  display: grid;
  grid-template-columns: 70px minmax(0, 1fr) 120px;
  gap: 8px;
  align-items: center;
}

.level-badge {
  position: relative;
  display: grid;
  place-items: center;
  width: 64px;
  height: 64px;
  color: var(--me-white);
  background: radial-gradient(circle at 32% 24%, rgba(255, 255, 255, 0.34), rgba(255, 255, 255, 0) 30%),
    linear-gradient(145deg, #9b8cff 0%, var(--me-primary) 48%, var(--me-primary-dark) 100%);
  clip-path: polygon(25% 5%, 75% 5%, 100% 50%, 75% 95%, 25% 95%, 0 50%);
  font-size: 16px;
  font-weight: 900;
  filter: drop-shadow(0 14px 22px rgba(79, 70, 229, 0.22));
}

.level-content h3 { margin: 4px 0 6px; font-size: 14px; }
.level-content p { margin: 0; color: var(--me-muted); font-size: 10px; }

.progress-track,
.mini-progress {
  overflow: hidden;
  height: 8px;
  border-radius: 999px;
  background: #edf0f5;
}

.progress-track { margin-top: 8px; }

.progress-track span,
.mini-progress span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: var(--me-primary);
}

.progress-meta {
  margin-top: 2px;
  color: var(--me-muted);
  font-size: 10px;
  text-align: right;
}

.exp-list {
  display: grid;
  gap: 4px;
  margin: 0;
  padding: 2px 0 2px 8px;
  border-left: 1px solid var(--me-border);
  list-style: none;
}

.exp-list li {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  color: var(--me-muted);
  font-size: 10px;
}

.exp-list li span { flex: 1; }
.exp-list strong { color: var(--me-muted); }

.task-head h2 span {
  margin-left: 6px;
  color: var(--me-muted);
  font-size: 10px;
  font-weight: 500;
}

.task-head p {
  margin: 0;
  color: var(--me-muted);
  font-size: 10px;
  font-weight: 700;
}

.task-head b {
  color: var(--me-primary);
  font-weight: 900;
}

.task-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 6px;
  padding: 1px 0 2px;
}

.task-item {
  display: grid;
  box-sizing: border-box;
  justify-items: center;
  width: auto;
  min-width: 0;
  padding: 8px 5px 6px;
  border: 1px solid var(--me-border);
  border-radius: 12px;
  background: linear-gradient(145deg, #fbfcff, #ffffff);
  text-align: center;
}

.task-item:nth-child(n + 2) { background: linear-gradient(145deg, #fffaf2, #fffdf8); }
.task-item.is-done { background: linear-gradient(145deg, #f8faff, #ffffff); }

.task-check {
  display: grid;
  place-items: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  color: var(--me-white);
  background: var(--me-primary);
  font-size: 12px;
  font-weight: 900;
}

.task-item h3 { margin: 5px 0 3px; font-size: 10px; line-height: 1.25; }
.task-item p,
.task-item small { margin: 0; color: var(--me-muted); font-size: 11px; }

.mini-progress {
  width: 100%;
  height: 4px;
  margin: 4px 0 3px;
}

.task-item a {
  min-height: 24px;
  width: 100%;
  margin-top: 4px;
  border: 1px solid #cfd4ff;
  color: var(--me-primary-dark);
  background: #ffffff;
}

.task-item.is-done a {
  color: var(--me-muted);
  border-color: var(--me-border-strong);
}

.badge-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.badge-list article {
  border: 1px solid #dbeafe;
  border-radius: 10px;
  background: #eff6ff;
  padding: 8px;
  display: grid;
  gap: 4px;
}

.badge-list article.locked {
  border-color: var(--me-border);
  background: #f8fafc;
  color: var(--me-muted);
}

.workspace-grid,
.quick-grid {
  display: grid;
  gap: 8px;
  margin-top: 8px;
}

.workspace-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
.quick-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }

.workspace-item,
.quick-item,
.overview-item {
  display: grid;
  gap: 7px;
  min-width: 0;
  color: inherit;
  text-decoration: none;
}

.workspace-item {
  min-height: 72px;
  padding: 8px 6px;
}

.workspace-item .soft-icon {
  color: var(--me-white);
  box-shadow: 0 12px 20px rgba(79, 70, 229, 0.16);
}

.workspace-item .tone-violet { background: linear-gradient(135deg, #7c66ff, var(--me-primary-dark)); }
.workspace-item .tone-rose { background: linear-gradient(135deg, #ff5fa8, #ec4899); }
.workspace-item .tone-amber { background: linear-gradient(135deg, #ffae3d, #f97316); }
.workspace-item .tone-blue { background: linear-gradient(135deg, #60a5fa, #3b82f6); }

.workspace-item strong,
.quick-item strong { font-size: 10px; line-height: 1.2; }

.workspace-item em {
  color: var(--me-muted);
  font-size: 9px;
  font-style: normal;
  line-height: 1.2;
}

.soft-icon {
  display: grid;
  place-items: center;
  width: 30px;
  height: 30px;
  border-radius: 9px;
  font-size: 14px;
  font-weight: 900;
}

.tone-violet { color: var(--me-violet); background: #f1efff; }
.tone-rose { color: var(--me-rose); background: #fdf2f8; }
.tone-amber { color: var(--me-amber); background: #fffbeb; }
.tone-blue { color: var(--me-blue); background: #eff6ff; }
.tone-green { color: var(--me-success); background: #ecfdf5; }

.overview-row {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 8px;
  overflow: hidden;
}

.overview-item {
  min-width: 0;
  justify-items: center;
  padding: 1px;
  text-align: center;
}

.overview-item strong { font-size: 12px; line-height: 1.1; }
.overview-item strong {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.group-summary {
  display: flex;
  align-items: center;
  gap: 10px;
}

.group-avatar {
  display: grid;
  place-items: center;
  width: 54px;
  height: 54px;
  flex: 0 0 auto;
  border-radius: 50%;
  color: var(--me-white);
  background: linear-gradient(135deg, var(--me-primary), var(--me-primary-dark));
  box-shadow: 0 10px 18px rgba(109, 93, 251, 0.22);
  font-weight: 900;
}

.group-summary h3 { margin: 0 0 4px; font-size: 12px; line-height: 1.2; }
.group-summary h3 {
  display: flex;
  align-items: center;
  min-width: 0;
}
.group-summary h3 span {
  margin-left: 6px;
  border-radius: 999px;
  padding: 3px 8px;
  color: var(--me-primary-dark);
  background: #f1efff;
  font-size: 10px;
  flex: 0 0 auto;
  white-space: nowrap;
}

.group-summary p { margin: 1px 0 0; color: var(--me-muted); font-size: 9px; line-height: 1.2; }
.group-summary p {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.group-summary i { display: inline-block; width: 1px; height: 10px; margin: 0 8px; background: #dbe1ea; }

.group-actions {
  grid-template-columns: repeat(3, minmax(0, 1fr));
  margin-left: 0;
}

.group-actions a { min-height: 26px; font-size: 9px; }

.ranking-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-top: 12px;
}

.ranking-head h3 { margin: 0; font-size: 11px; }

.ranking-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 6px;
  margin: 8px 0 0;
  padding: 0;
  list-style: none;
}

.ranking-list li {
  display: grid;
  grid-template-columns: auto 1fr;
  align-items: center;
  gap: 8px;
  min-height: 48px;
  padding: 4px;
  border: 1px solid var(--me-border);
  border-radius: 12px;
  background: var(--me-bg-soft);
}

.rank-medal {
  display: grid;
  place-items: center;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  color: var(--me-medal);
  background: #fff1c2;
  font-weight: 900;
}

.ranking-list li:nth-child(2) .rank-medal { color: var(--me-muted); background: #e7edf7; }
.ranking-list li:nth-child(3) .rank-medal { color: var(--me-orange); background: #ffead6; }
.ranking-list strong { font-size: 10px; line-height: 1.2; }
.ranking-list small { grid-column: 2; color: var(--me-muted); font-size: 9px; line-height: 1.2; }
.ranking-list strong,
.ranking-list small {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.quick-item {
  justify-items: center;
  min-height: 64px;
  padding: 6px 3px;
  text-align: center;
}

.quick-item .soft-icon { width: 28px; height: 28px; }
.quick-item strong,
.quick-item small {
  width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.me-modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 200;
  display: grid;
  place-items: center;
  padding: 18px;
  background: rgba(15, 23, 42, 0.42);
}

.me-modal {
  width: min(100%, 430px);
  max-height: calc(100vh - 36px);
  overflow: auto;
  border-radius: 20px;
  background: #ffffff;
  padding: 18px;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.22);
}

.me-modal-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.me-modal-head h3 { margin: 0; font-size: 18px; }
.me-modal-head button { width: 34px; height: 34px; border: 0; border-radius: 50%; background: #f1f5f9; font-size: 20px; }

.me-edit-form { display: grid; gap: 14px; }
.me-edit-form label { display: grid; gap: 7px; color: var(--me-text-soft); font-size: 13px; font-weight: 800; }

.me-edit-form input,
.me-edit-form textarea {
  width: 100%;
  border: 1px solid #dfe4ee;
  border-radius: 12px;
  padding: 11px 12px;
  color: var(--me-text);
  background: #ffffff;
  font: inherit;
  outline: 0;
}

.avatar-uploader { display: flex; align-items: center; gap: 12px; }
.avatar-uploader img,
.avatar-uploader div {
  display: grid;
  place-items: center;
  width: 58px;
  height: 58px;
  border-radius: 50%;
  object-fit: cover;
  color: var(--me-white);
  background: linear-gradient(135deg, var(--me-primary), var(--me-primary-dark));
  font-weight: 900;
}

.avatar-uploader button {
  border: 1px solid #dfe4ee;
  border-radius: 12px;
  padding: 10px 12px;
  color: var(--me-text);
  background: #ffffff;
}

.settings-summary { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
.settings-summary .profile-avatar { width: 52px; height: 52px; font-size: 20px; }
.settings-summary strong,
.settings-summary span { display: block; }
.settings-summary span { margin-top: 4px; color: var(--me-muted); font-size: 13px; }

.settings-list { border: 1px solid var(--me-border); border-radius: 14px; overflow: hidden; }
.settings-list div {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 13px 14px;
  border-bottom: 1px solid var(--me-border);
  color: var(--me-muted);
  font-size: 13px;
}
.settings-list div:last-child { border-bottom: 0; }
.settings-list strong { color: var(--me-text); }
.settings-actions {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

@media (max-width: 420px) {
  .me-shell { padding-right: 8px; padding-left: 8px; }

  .me-header h1 { font-size: 34px; }
  .me-header p { font-size: 14px; }
  .profile-main { grid-template-columns: 58px minmax(0, 1fr); gap: 8px; }
  .profile-avatar { width: 58px; height: 58px; }
  .profile-badges { gap: 6px; }
  .profile-badges span { width: 26px; height: 26px; font-size: 11px; border-radius: 9px; }
  .profile-info { padding-right: 96px; }

  .growth-body { grid-template-columns: 64px minmax(0, 1fr); gap: 8px; }
  .exp-list { grid-column: 1 / -1; padding: 8px 0 0; border-top: 1px solid var(--me-border); border-left: 0; }
  .level-badge { width: 58px; height: 58px; font-size: 15px; }

  .task-grid { grid-template-columns: repeat(4, minmax(0, 1fr)); }
  .overview-row { grid-template-columns: repeat(4, minmax(0, 1fr)); }
  .ranking-list { grid-template-columns: repeat(3, minmax(0, 1fr)); }

  .workspace-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .quick-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
}
</style>
