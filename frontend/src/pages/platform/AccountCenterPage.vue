<template>
  <section class="platform-page me-layout">
    <aside class="me-sidebar">
      <h2>平台个人中心</h2>
      <nav class="me-menu" aria-label="平台个人中心导航">
        <router-link to="/me" class="me-menu-item is-active">
          <span class="me-menu-icon is-pink">⌂</span>
          <span>个人中心首页</span>
        </router-link>
        <div class="me-menu-group">
          <router-link to="/platform/positive-share" class="me-menu-item">
            <span class="me-menu-icon">▣</span>
            <span>内容中心</span>
            <span class="me-menu-caret">⌃</span>
          </router-link>
          <router-link to="/platform/positive-share" class="me-sub-item">我的内容</router-link>
          <router-link to="/platform/positive-share" class="me-sub-item">每日心声</router-link>
          <router-link to="/messages" class="me-sub-item">消息中心</router-link>
          <router-link to="/me/favorites" class="me-sub-item">收藏夹</router-link>
          <router-link to="/me/drafts" class="me-sub-item">草稿箱</router-link>
        </div>
        <router-link to="/events" class="me-menu-item">
          <span class="me-menu-icon">□</span>
          <span>活动中心</span>
        </router-link>
        <router-link to="/modules" class="me-menu-item">
          <span class="me-menu-icon">◇</span>
          <span>模块中心</span>
        </router-link>
        <router-link to="/messages" class="me-menu-item">
          <span class="me-menu-icon">♧</span>
          <span>通知中心</span>
          <span v-if="unreadCount > 0" class="me-dot">{{ unreadCount }}</span>
        </router-link>
        <router-link :to="accountSettingsRoute" class="me-menu-item">
          <span class="me-menu-icon">⚙</span>
          <span>账号设置</span>
        </router-link>
      </nav>
      <div class="me-fellowship-card">
        <p>切换到联谊中心</p>
        <span>管理你的联谊资料、匹配、聊天记录等</span>
        <router-link to="/fellowship">进入联谊中心 ></router-link>
      </div>
    </aside>

    <main class="me-main">
      <header class="mobile-me-header mobile-only">
        <h1>平台个人中心</h1>
        <div class="mobile-header-actions">
          <router-link to="/messages" class="mobile-header-icon">
            ♧
            <span v-if="unreadCount > 0">{{ unreadCount }}</span>
          </router-link>
          <router-link :to="accountSettingsRoute" class="mobile-header-icon">⚙</router-link>
        </div>
      </header>

      <div class="me-hero">
        <div class="me-user">
          <img v-if="user?.avatar" :src="user.avatar" class="me-avatar" alt="头像" />
          <div v-else class="me-avatar me-avatar-fallback">{{ avatarFallback }}</div>
          <div class="me-meta">
            <div class="me-title-row">
              <h1>{{ user?.username || '平台用户' }}</h1>
              <span class="me-level">平台创作者</span>
            </div>
            <p class="me-bio">{{ user?.bio || '分享美好，连接你我' }} <button type="button" @click="toggleEdit">✎</button></p>
            <div class="me-profile-line">
              <span>ID: {{ user?.id || '--' }}</span>
              <span>注册时间: {{ registerDate }}</span>
              <span>{{ user?.location || '广州' }}</span>
            </div>
            <button type="button" class="me-edit-btn" @click="toggleEdit">
              {{ editOpen ? '收起编辑' : '编辑资料' }} >
            </button>
            <router-link to="/fellowship" class="mobile-fellowship-pill mobile-only">♡ 切换联谊中心 ></router-link>
          </div>
        </div>
        <div class="me-kpis">
          <article v-for="item in heroStats" :key="item.key">
            <span class="me-kpi-icon" :class="`is-${item.theme}`">{{ item.icon }}</span>
            <p>{{ item.label }}</p>
            <strong>{{ item.value }}</strong>
            <small>{{ item.sub }}</small>
          </article>
        </div>
      </div>

      <section class="mobile-shortcut-card mobile-only">
        <router-link v-for="item in mobileShortcutItems" :key="item.title" :to="item.to" class="mobile-shortcut-item">
          <span class="mobile-shortcut-icon" :class="`is-${item.theme}`">{{ item.icon }}</span>
          <strong>{{ item.title }}</strong>
          <small>{{ item.desc }}</small>
        </router-link>
      </section>

      <section class="mobile-data-card mobile-only">
        <div class="mobile-section-head">
          <h2>我的数据</h2>
          <router-link to="/modules">全部数据 ></router-link>
        </div>
        <div class="mobile-data-row">
          <router-link v-for="item in mobileDataItems" :key="item.title" :to="item.to" class="mobile-data-item">
            <span class="mobile-data-icon" :class="`is-${item.theme}`">{{ item.icon }}</span>
            <strong>{{ item.value }}</strong>
            <small>{{ item.title }}</small>
          </router-link>
        </div>
      </section>

      <section class="mobile-list-card mobile-only">
        <router-link v-for="item in mobileListItems" :key="item.title" :to="item.to" class="mobile-list-item">
          <span class="mobile-list-icon" :class="`is-${item.theme}`">{{ item.icon }}</span>
          <strong>{{ item.title }}</strong>
          <small>{{ item.desc }}</small>
          <em>{{ item.extra }} ></em>
        </router-link>
      </section>

      <div v-if="editOpen" class="platform-card me-edit-card">
        <h3>编辑个人资料</h3>
        <form class="me-edit-form" @submit.prevent="handleSaveProfile">
          <label>
            <span>昵称</span>
            <input v-model.trim="editForm.username" type="text" maxlength="20" placeholder="请输入昵称（最多20字）" />
          </label>
          <label>
            <span>头像</span>
            <div class="me-avatar-uploader">
              <img v-if="editForm.avatar" :src="editForm.avatar" class="me-avatar-upload-preview" alt="头像预览" />
              <div v-else class="me-avatar-upload-preview me-avatar-upload-fallback">{{ avatarFallback }}</div>
              <div class="me-avatar-upload-actions">
                <button type="button" class="me-avatar-upload-btn" :disabled="uploading || saving" @click="handlePickAvatar">
                  {{ uploading ? '上传中...' : '选择图片' }}
                </button>
                <p class="me-avatar-upload-tip">支持常见图片格式，选择后自动上传并填充头像</p>
              </div>
            </div>
          </label>
          <label>
            <span>所在地</span>
            <input v-model.trim="editForm.location" type="text" maxlength="60" placeholder="例如：上海·浦东" />
          </label>
          <label>
            <span>个人简介</span>
            <textarea v-model.trim="editForm.bio" rows="3" maxlength="200" placeholder="简单介绍一下你自己" />
          </label>
          <p v-if="saveMessage" :class="['me-save-message', { 'is-error': saveError }]">{{ saveMessage }}</p>
          <div class="me-edit-actions">
            <button type="button" class="me-edit-cancel" :disabled="saving" @click="resetEditForm">重置</button>
            <button type="submit" class="me-edit-submit" :disabled="saving">{{ saving ? '保存中...' : '保存资料' }}</button>
          </div>
        </form>
      </div>

      <div class="me-center-grid">
        <article v-for="card in centerCards" :key="card.key" class="me-center-card">
          <div class="me-card-head">
            <span class="me-card-icon" :class="`is-${card.theme}`">{{ card.icon }}</span>
            <div>
              <h3>{{ card.title }}</h3>
              <p>{{ card.desc }}</p>
            </div>
          </div>
          <router-link :to="card.actionTo" class="me-card-action" :class="`is-${card.theme}`">{{ card.action }} ></router-link>
        </article>
      </div>

      <section class="me-recommend-panel">
        <h3>为你推荐</h3>
        <div class="me-recommend-list">
          <router-link v-for="item in recommendItems" :key="item.title" :to="item.to" class="me-recommend-item">
            <span class="me-recommend-icon" :class="`is-${item.theme}`">{{ item.icon }}</span>
            <span>
              <strong>{{ item.title }}</strong>
              <small>{{ item.desc }}</small>
              <em>{{ item.action }} →</em>
            </span>
          </router-link>
        </div>
      </section>

      <section class="mobile-recommend-card mobile-only">
        <h2>为你推荐</h2>
        <div class="mobile-recommend-grid">
          <router-link v-for="item in recommendItems" :key="item.title" :to="item.to" class="mobile-recommend-item">
            <span class="mobile-recommend-icon" :class="`is-${item.theme}`">{{ item.icon }}</span>
            <strong>{{ item.title }}</strong>
            <small>{{ item.desc }}</small>
            <em>{{ item.action }}</em>
          </router-link>
        </div>
      </section>
    </main>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user.js'
import { getNotifUnreadCountCached } from '@/api/notification.js'
import { getUserStatsCached, updateProfile } from '@/api/user.js'
import { useImageUpload } from '@/composables/useImageUpload.js'

const userStore = useUserStore()
const route = useRoute()
const user = computed(() => userStore.userInfo)
const unreadCount = ref(0)
const editOpen = ref(false)
const saving = ref(false)
const saveMessage = ref('')
const saveError = ref(false)
const { pickAndUpload, uploading } = useImageUpload()
const editForm = reactive({
  username: '',
  avatar: '',
  location: '',
  bio: ''
})

const myContentCount = ref(0)
const myEventCount = ref(0)
const myFavoriteCount = ref(0)
const accountSettingsRoute = { path: '/me', query: { panel: 'settings' } }

const avatarFallback = computed(() => {
  const name = user.value?.username || ''
  return name.slice(0, 1).toUpperCase() || 'U'
})

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

const heroStats = computed(() => [
  { key: 'content', label: '内容发布', value: myContentCount.value, sub: '累计发布', icon: '▣', theme: 'blue' },
  { key: 'activity', label: '活动参与', value: myEventCount.value, sub: '累计参与', icon: '□', theme: 'purple' },
  { key: 'favorite', label: '收藏内容', value: myFavoriteCount.value, sub: '收藏总数', icon: '★', theme: 'amber' },
  { key: 'like', label: '互动热度', value: '--', sub: '待接入', icon: '👍', theme: 'orange' }
])

const centerCards = computed(() => [
  {
    key: 'content',
    title: '内容中心',
    desc: '管理你的内容创作',
    icon: '▣',
    theme: 'purple',
    action: '去创作',
    actionTo: '/platform/positive-share',
    rows: [
      { label: '我的内容', value: myContentCount.value, to: '/platform/positive-share' },
      { label: '每日心声', value: Math.min(myContentCount.value, 7), to: '/platform/positive-share' },
      { label: '消息中心', value: unreadCount.value, to: '/messages' },
      { label: '收藏夹', value: myFavoriteCount.value, to: '/me/favorites' },
      { label: '草稿箱', value: '--', to: '/me/drafts' }
    ]
  },
  {
    key: 'activity',
    title: '活动中心',
    desc: '浏览并参与平台活动',
    icon: '□',
    theme: 'orange',
    action: '查看活动',
    actionTo: '/events',
    rows: [
      { label: '活动报名', value: myEventCount.value, to: '/events' },
      { label: '活动记录', value: myEventCount.value, to: '/events' },
      { label: '活动收藏', value: Math.min(myFavoriteCount.value, 2), to: '/events' }
    ]
  },
  {
    key: 'growth',
    title: '模块中心',
    desc: '查看平台模块与成长信息',
    icon: '◇',
    theme: 'teal',
    action: '查看模块',
    actionTo: '/modules',
    rows: [
      { label: '当前等级', value: '--', to: '/modules' },
      { label: '经验值', value: '--', to: '/modules' },
      { label: '积分余额', value: '--', to: '/modules' },
      { label: '勋章墙', value: '--', to: '/modules' }
    ]
  },
  {
    key: 'account',
    title: '资料设置',
    desc: '编辑个人资料信息',
    icon: '⚙',
    theme: 'blue',
    action: '去编辑',
    actionTo: accountSettingsRoute,
    rows: [
      { label: '基础资料', value: '>', to: '/me' },
      { label: '账号角色', value: roleLabel.value, to: accountSettingsRoute },
      { label: '通知消息', value: unreadCount.value, to: '/messages' },
      { label: '认证状态', value: verifyLabel.value, to: accountSettingsRoute }
    ]
  }
])

const recommendItems = [
  {
    title: '发布今日心声',
    desc: '记录此刻的想法，分享你的心情',
    action: '去发布',
    icon: '✎',
    theme: 'pink',
    to: '/platform/positive-share'
  },
  {
    title: '参加热门活动',
    desc: '发现有趣的活动，结识新朋友',
    action: '去看看',
    icon: '□',
    theme: 'blue',
    to: '/events'
  },
  {
    title: '完善个人资料',
    desc: '完善资料可提升曝光和互动',
    action: '去完善',
    icon: '●',
    theme: 'orange',
    to: '/me'
  }
]

const mobileShortcutItems = computed(() => [
  { title: '我的内容', desc: `${myContentCount.value} 篇内容`, icon: '▣', theme: 'purple', to: '/platform/positive-share' },
  { title: '每日心声', desc: `${Math.min(myContentCount.value, 7)} 条心声`, icon: '♡', theme: 'pink', to: '/platform/positive-share' },
  { title: '活动中心', desc: `${myEventCount.value} 个活动`, icon: '□', theme: 'orange', to: '/events' },
  { title: '编辑资料', desc: '修改账号资料', icon: '◇', theme: 'blue', to: accountSettingsRoute }
])

const mobileDataItems = computed(() => [
  { title: '发布内容', value: myContentCount.value, icon: '▣', theme: 'purple', to: '/platform/positive-share' },
  { title: '活动参与', value: myEventCount.value, icon: '□', theme: 'pink', to: '/events' },
  { title: '收藏内容', value: myFavoriteCount.value, icon: '★', theme: 'amber', to: '/me/favorites' },
  { title: '互动热度', value: '--', icon: '👍', theme: 'teal', to: '/platform/positive-share' },
  { title: '当前等级', value: '--', icon: '◇', theme: 'blue', to: '/modules' }
])

const mobileListItems = computed(() => [
  { title: '内容中心', desc: '管理我的内容创作', extra: `${myContentCount.value} 篇内容`, icon: '▣', theme: 'purple', to: '/platform/positive-share' },
  { title: '模块中心', desc: '创作者成长体系', extra: '待接入', icon: '♕', theme: 'amber', to: '/modules' },
  { title: '通知中心', desc: '平台消息与评论', extra: unreadCount.value > 0 ? `${unreadCount.value} 条未读` : '暂无未读', icon: '♧', theme: 'pink', to: '/messages' },
  { title: '资料设置', desc: '编辑头像昵称简介', extra: verifyLabel.value, icon: '◇', theme: 'teal', to: accountSettingsRoute },
  { title: '邀请码', desc: '邀请好友得奖励', extra: '去邀请', icon: '✉', theme: 'blue', to: '/fellowship/invite' },
  { title: '资料帮助', desc: '资料编辑相关说明', extra: '查看', icon: '?', theme: 'purple', to: accountSettingsRoute }
])

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

function toggleEdit() {
  editOpen.value = !editOpen.value
  saveMessage.value = ''
  saveError.value = false
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
    if (!avatarUrl) {
      throw new Error('上传失败，请重试')
    }
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
  } catch (error) {
    saveError.value = true
    saveMessage.value = error?.message || '保存失败，请稍后重试'
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  if (route.query?.panel === 'settings') {
    editOpen.value = true
  }
  if (!user.value) {
    await userStore.refreshCurrentUser().catch(() => {})
  }
  const [notifRes, statsRes] = await Promise.allSettled([
    getNotifUnreadCountCached(),
    getUserStatsCached()
  ])
  if (notifRes.status === 'fulfilled') {
    unreadCount.value = Number(notifRes.value?.count ?? notifRes.value?.unreadCount ?? 0)
  }
  if (statsRes.status === 'fulfilled' && statsRes.value) {
    myContentCount.value = Number(statsRes.value.contentCount ?? 0)
    myEventCount.value = Number(statsRes.value.eventCount ?? 0)
    myFavoriteCount.value = Number(statsRes.value.favoriteCount ?? 0)
  }
})
</script>

<style scoped>
.me-layout {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 20px;
}

.me-sidebar {
  position: sticky;
  top: 88px;
  height: fit-content;
}

.me-sidebar h2 {
  margin: 0;
  font-size: 22px;
}

.me-menu {
  margin-top: 14px;
  display: grid;
  gap: 6px;
}

.me-menu-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 10px;
  color: var(--lc-muted);
  text-decoration: none;
  font-weight: 600;
}

.me-menu-item.is-active,
.me-menu-item:hover {
  background: #feeef3;
  color: #e11d48;
}

.me-dot {
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: #ef4444;
  color: #fff;
  font-size: 11px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.me-main {
  display: grid;
  gap: 14px;
}

.me-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 420px;
  gap: 16px;
  background: linear-gradient(100deg, #f8a2ce, #5a8ff2);
}

.me-user {
  display: flex;
  gap: 12px;
  align-items: center;
}

.me-avatar {
  width: 84px;
  height: 84px;
  border-radius: 50%;
  object-fit: cover;
}

.me-avatar-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: rgba(255, 255, 255, 0.2);
  font-size: 28px;
  font-weight: 800;
}

.me-meta {
  min-width: 0;
}

.me-meta h1 {
  margin: 0;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.me-meta p {
  margin: 6px 0;
  color: rgba(255, 255, 255, 0.88);
}

.me-tags {
  display: flex;
  gap: 8px;
}

.me-tag {
  padding: 3px 10px;
  border-radius: 999px;
  color: #fff;
  background: rgba(255, 255, 255, 0.25);
  font-size: 12px;
}

.me-edit-btn {
  margin-top: 8px;
  border: 1px solid rgba(255, 255, 255, 0.52);
  background: rgba(255, 255, 255, 0.18);
  color: #fff;
  border-radius: 999px;
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
}

.me-kpis {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.me-kpis article {
  border-radius: 10px;
  padding: 10px 12px;
  background: rgba(255, 255, 255, 0.92);
}

.me-kpis p {
  margin: 0;
  color: var(--lc-muted);
  font-size: 12px;
}

.me-kpis strong {
  font-size: 26px;
}

.me-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.me-edit-card h3 {
  margin-top: 0;
}

.me-edit-form {
  display: grid;
  gap: 12px;
}

.me-edit-form label {
  display: grid;
  gap: 6px;
}

.me-edit-form span {
  font-size: 13px;
  font-weight: 700;
  color: #334155;
}

.me-edit-form input,
.me-edit-form textarea {
  width: 100%;
  border: 1px solid #d7e0ed;
  border-radius: 8px;
  padding: 10px;
  font: inherit;
  color: #0f172a;
  background: #fff;
}

.me-edit-form textarea {
  resize: vertical;
}

.me-avatar-uploader {
  display: flex;
  align-items: center;
  gap: 12px;
}

.me-avatar-upload-preview {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #d7e0ed;
}

.me-avatar-upload-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
  background: #f8fafc;
  font-size: 22px;
  font-weight: 800;
}

.me-avatar-upload-actions {
  display: grid;
  gap: 6px;
}

.me-avatar-upload-btn {
  width: fit-content;
  border: 1px solid #d7e0ed;
  border-radius: 8px;
  background: #fff;
  color: #334155;
  padding: 7px 12px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}

.me-avatar-upload-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.me-avatar-upload-tip {
  margin: 0;
  font-size: 12px;
  color: #64748b;
}

.me-save-message {
  margin: 0;
  color: #16a34a;
  font-size: 13px;
  font-weight: 700;
}

.me-save-message.is-error {
  color: #dc2626;
}

.me-edit-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.me-edit-cancel,
.me-edit-submit {
  border: 1px solid #d7e0ed;
  border-radius: 8px;
  padding: 8px 14px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}

.me-edit-cancel {
  background: #fff;
  color: #334155;
}

.me-edit-submit {
  border-color: #2563eb;
  background: #2563eb;
  color: #fff;
}

.me-shortcuts {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.me-shortcuts a {
  border: 1px solid var(--lc-border);
  border-radius: 10px;
  padding: 10px;
  color: var(--lc-text);
  text-decoration: none;
  text-align: center;
}

@media (max-width: 1023px) {
  .me-layout {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .me-sidebar {
    position: static;
  }

  .me-hero,
  .me-grid {
    grid-template-columns: 1fr;
  }

  .me-kpis strong {
    font-size: 22px;
  }
}

@media (max-width: 767px) {
  .me-layout {
    gap: 10px;
  }

  .me-layout .platform-card {
    padding: 12px;
    border-radius: 12px;
  }

  .me-sidebar h2 {
    font-size: 20px;
  }

  .me-menu {
    margin-top: 10px;
    display: flex;
    gap: 8px;
    overflow-x: auto;
    padding-bottom: 2px;
    -webkit-overflow-scrolling: touch;
    scrollbar-width: none;
  }

  .me-menu::-webkit-scrollbar {
    display: none;
  }

  .me-menu-item {
    flex: 0 0 auto;
    min-height: 36px;
    padding: 7px 12px;
    border-radius: 999px;
    background: #f8fafc;
    font-size: 13px;
    white-space: nowrap;
  }

  .me-dot {
    margin-left: 6px;
  }

  .me-main {
    gap: 10px;
  }

  .me-hero {
    gap: 12px;
    border-radius: 12px;
  }

  .me-user {
    align-items: flex-start;
  }

  .me-avatar {
    width: 64px;
    height: 64px;
  }

  .me-avatar-fallback {
    font-size: 24px;
  }

  .me-meta h1 {
    font-size: 22px;
    white-space: normal;
    line-height: 1.2;
  }

  .me-meta p {
    margin: 4px 0;
    font-size: 13px;
  }

  .me-tags {
    flex-wrap: wrap;
    gap: 6px;
  }

  .me-tag {
    font-size: 11px;
    padding: 2px 8px;
  }

  .me-edit-btn {
    margin-top: 6px;
    min-height: 32px;
    padding: 0 12px;
  }

  .me-kpis {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 8px;
  }

  .me-kpis article {
    padding: 8px 10px;
  }

  .me-kpis p {
    font-size: 11px;
  }

  .me-kpis strong {
    font-size: 20px;
  }

  .me-edit-form {
    gap: 10px;
  }

  .me-avatar-uploader {
    align-items: flex-start;
  }

  .me-avatar-upload-preview {
    width: 56px;
    height: 56px;
  }

  .me-avatar-upload-fallback {
    font-size: 20px;
  }

  .me-edit-actions {
    justify-content: stretch;
  }

  .me-edit-cancel,
  .me-edit-submit {
    flex: 1;
    min-height: 36px;
  }

  .me-shortcuts {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .me-shortcuts a {
    text-align: left;
    padding: 10px 12px;
    font-size: 14px;
  }
}

.me-layout {
  width: calc(100% - 56px);
  margin: var(--lc-space-6) auto var(--lc-space-8);
  grid-template-columns: 196px minmax(0, 1fr);
  gap: var(--lc-space-5);
  color: var(--lc-text);
}

.me-sidebar {
  top: 84px;
  padding: var(--lc-space-5) var(--lc-space-4);
  border: 1px solid rgba(226, 232, 240, .82);
  border-radius: var(--lc-radius);
  background: rgba(255, 255, 255, .92);
  box-shadow: 0 18px 42px rgba(15, 23, 42, .08);
}

.me-sidebar h2 {
  margin: 0 0 var(--lc-space-4);
  font-size: var(--lc-text-xl);
  letter-spacing: -.02em;
}

.me-menu {
  margin: 0;
  gap: var(--lc-space-1);
}

.me-menu-group {
  display: grid;
  gap: 2px;
}

.me-menu-item,
.me-sub-item {
  display: grid;
  align-items: center;
  min-height: 38px;
  color: #40516b;
  text-decoration: none;
}

.me-menu-item {
  grid-template-columns: 22px 1fr auto;
  gap: var(--lc-space-2);
  padding: 0 var(--lc-space-3);
  border-radius: var(--lc-radius-sm);
  font-size: var(--lc-text-base);
}

.me-menu-item.is-active,
.me-menu-item:hover {
  background: linear-gradient(90deg, rgba(236, 72, 153, .12), rgba(236, 72, 153, .05));
  color: var(--lc-pink);
}

.me-menu-icon {
  color: #75839a;
  font-size: var(--lc-text-md);
  text-align: center;
}

.me-menu-icon.is-pink,
.me-menu-item.is-active .me-menu-icon {
  color: var(--lc-pink);
}

.me-menu-caret {
  color: var(--lc-subtle);
  font-size: var(--lc-text-xs);
}

.me-sub-item {
  margin-left: 34px;
  padding: 0 var(--lc-space-2);
  border-radius: var(--lc-radius-xs);
  font-size: var(--lc-text-sm);
}

.me-sub-item:hover {
  background: var(--lc-soft);
  color: var(--lc-blue);
}

.me-dot {
  width: 18px;
  min-width: 18px;
  height: 18px;
  background: var(--lc-red);
  font-size: 10px;
}

.me-fellowship-card {
  position: relative;
  overflow: hidden;
  margin-top: var(--lc-space-6);
  min-height: 128px;
  padding: var(--lc-space-4);
  border-radius: var(--lc-radius-sm);
  background:
    radial-gradient(circle at 86% 76%, rgba(236, 72, 153, .32), transparent 26%),
    radial-gradient(circle at 100% 94%, rgba(37, 99, 235, .22), transparent 28%),
    linear-gradient(135deg, #f5efff 0%, #eef3ff 100%);
}

.me-fellowship-card p {
  margin: 0 0 var(--lc-space-2);
  color: #5b48d6;
  font-weight: 800;
}

.me-fellowship-card span {
  display: block;
  max-width: 142px;
  color: #66728a;
  font-size: var(--lc-text-xs);
  line-height: 1.6;
}

.me-fellowship-card a {
  display: inline-flex;
  margin-top: var(--lc-space-4);
  padding: 7px 12px;
  border: 1px solid rgba(91, 72, 214, .18);
  border-radius: 999px;
  color: #5b48d6;
  background: rgba(255, 255, 255, .62);
  font-size: var(--lc-text-xs);
  font-weight: 800;
  text-decoration: none;
}

.me-main {
  gap: var(--lc-space-5);
}

.me-hero {
  position: relative;
  overflow: hidden;
  grid-template-columns: minmax(280px, 1fr) minmax(360px, 560px);
  align-items: center;
  min-height: 208px;
  padding: var(--lc-space-6) var(--lc-space-8);
  border: 1px solid rgba(255, 255, 255, .76);
  border-radius: var(--lc-radius-lg);
  background:
    radial-gradient(circle at 10% 10%, rgba(255, 255, 255, .72), transparent 24%),
    radial-gradient(circle at 88% 20%, rgba(103, 122, 255, .26), transparent 32%),
    linear-gradient(105deg, #ffd8f0 0%, #f6dcff 42%, #b8c8ff 100%);
  box-shadow: 0 18px 42px rgba(91, 107, 193, .18);
}

.me-hero::after {
  position: absolute;
  right: -44px;
  bottom: -72px;
  width: 240px;
  height: 180px;
  border-radius: 999px;
  background: rgba(255, 255, 255, .26);
  content: "";
}

.me-user {
  position: relative;
  z-index: 1;
  gap: var(--lc-space-5);
}

.me-avatar {
  width: 116px;
  height: 116px;
  border: 5px solid rgba(255, 255, 255, .9);
  box-shadow: 0 16px 34px rgba(89, 55, 136, .24);
}

.me-avatar-fallback {
  color: var(--lc-blue);
  background: linear-gradient(135deg, #ffffff, #eef3ff);
  font-size: 44px;
}

.me-title-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--lc-space-2);
}

.me-meta h1 {
  color: #1f2442;
  font-size: 28px;
  font-weight: 900;
}

.me-level {
  padding: 4px 9px;
  border-radius: 999px;
  color: #5661d9;
  background: rgba(255, 255, 255, .72);
  font-size: var(--lc-text-xs);
  font-weight: 800;
}

.me-bio {
  display: flex;
  align-items: center;
  gap: var(--lc-space-2);
  margin: var(--lc-space-2) 0 var(--lc-space-3);
  color: #5f6380;
}

.me-bio button {
  border: 0;
  color: #6670d9;
  background: transparent;
  cursor: pointer;
}

.me-profile-line {
  display: flex;
  flex-wrap: wrap;
  gap: var(--lc-space-4);
  color: #66728a;
  font-size: var(--lc-text-xs);
}

.me-edit-btn {
  margin-top: var(--lc-space-4);
  border: 0;
  color: #5b48d6;
  background: rgba(255, 255, 255, .72);
  box-shadow: 0 8px 18px rgba(91, 72, 214, .12);
}

.me-kpis {
  position: relative;
  z-index: 1;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--lc-space-4);
}

.me-kpis article {
  display: grid;
  min-height: 122px;
  padding: var(--lc-space-4);
  border: 1px solid rgba(255, 255, 255, .72);
  border-radius: var(--lc-radius);
  background: rgba(255, 255, 255, .78);
  box-shadow: 0 12px 28px rgba(89, 102, 160, .12);
}

.me-kpi-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border-radius: 8px;
  color: #fff;
  font-size: var(--lc-text-sm);
}

.me-kpi-icon.is-blue,
.me-card-icon.is-blue,
.me-recommend-icon.is-blue {
  background: linear-gradient(135deg, #60a5fa, var(--lc-blue));
}

.me-kpi-icon.is-purple,
.me-card-icon.is-purple {
  background: linear-gradient(135deg, #8b5cf6, #6d5dfc);
}

.me-kpi-icon.is-amber {
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
}

.me-kpi-icon.is-orange,
.me-card-icon.is-orange,
.me-recommend-icon.is-orange {
  background: linear-gradient(135deg, #fb923c, #f97316);
}

.me-card-icon.is-teal {
  background: linear-gradient(135deg, #2dd4bf, #14b8a6);
}

.me-recommend-icon.is-pink {
  background: linear-gradient(135deg, #f472b6, var(--lc-pink));
}

.me-kpis p {
  margin-top: var(--lc-space-2);
  color: #566076;
  font-weight: 700;
}

.me-kpis strong {
  margin-top: var(--lc-space-1);
  color: #141b34;
  font-size: 30px;
  line-height: 1;
}

.me-kpis small {
  color: #7d8799;
  font-size: var(--lc-text-xs);
}

.me-center-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--lc-space-4);
}

.me-center-card,
.me-recommend-panel {
  border: 1px solid rgba(226, 232, 240, .84);
  border-radius: var(--lc-radius);
  background: rgba(255, 255, 255, .94);
  box-shadow: var(--lc-shadow-sm);
}

.me-center-card {
  display: grid;
  grid-template-rows: auto 1fr auto;
  gap: var(--lc-space-4);
  min-height: 236px;
  padding: var(--lc-space-5);
}

.me-card-head {
  display: flex;
  gap: var(--lc-space-3);
  align-items: center;
}

.me-card-icon,
.me-recommend-icon {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 12px;
  color: #fff;
  font-weight: 900;
}

.me-card-head h3,
.me-recommend-panel h3 {
  margin: 0;
  color: #1f2937;
  font-size: var(--lc-text-lg);
}

.me-card-head p {
  margin: 4px 0 0;
  color: #8a94a6;
  font-size: var(--lc-text-xs);
}

.me-card-list {
  display: grid;
  align-content: start;
  gap: var(--lc-space-2);
}

.me-card-list a {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 24px;
  border-bottom: 1px solid rgba(226, 232, 240, .72);
  color: #5d687a;
  font-size: var(--lc-text-xs);
  text-decoration: none;
}

.me-card-list a:hover {
  color: var(--lc-blue);
}

.me-card-list strong {
  color: #667085;
  font-weight: 700;
}

.me-card-action {
  display: inline-flex;
  justify-content: center;
  padding: 10px 12px;
  border-radius: var(--lc-radius-sm);
  font-size: var(--lc-text-xs);
  font-weight: 800;
  text-decoration: none;
}

.me-card-action.is-purple {
  color: #6d5dfc;
  background: rgba(109, 93, 252, .1);
}

.me-card-action.is-orange {
  color: #f97316;
  background: rgba(249, 115, 22, .1);
}

.me-card-action.is-teal {
  color: #0f9f92;
  background: rgba(20, 184, 166, .1);
}

.me-card-action.is-blue {
  color: var(--lc-blue);
  background: var(--lc-blue-light);
}

.me-recommend-panel {
  position: relative;
  overflow: hidden;
  padding: var(--lc-space-4) var(--lc-space-5);
}

.me-recommend-panel::after {
  position: absolute;
  right: 24px;
  bottom: -28px;
  width: 128px;
  height: 128px;
  border-radius: 36px;
  background:
    radial-gradient(circle at 38% 38%, rgba(236, 72, 153, .18), transparent 38%),
    linear-gradient(135deg, rgba(255, 255, 255, .4), rgba(236, 72, 153, .12));
  transform: rotate(10deg);
  content: "";
}

.me-recommend-list {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--lc-space-4);
  margin-top: var(--lc-space-3);
}

.me-recommend-item {
  display: flex;
  align-items: center;
  gap: var(--lc-space-3);
  min-height: 74px;
  padding: var(--lc-space-3);
  border-radius: var(--lc-radius-sm);
  color: inherit;
  background: linear-gradient(135deg, rgba(248, 250, 252, .9), rgba(255, 255, 255, .68));
  text-decoration: none;
}

.me-recommend-item strong,
.me-recommend-item small,
.me-recommend-item em {
  display: block;
}

.me-recommend-item strong {
  color: #1f2937;
  font-size: var(--lc-text-base);
}

.me-recommend-item small {
  margin-top: 2px;
  color: #7d8799;
  font-size: var(--lc-text-xs);
}

.me-recommend-item em {
  margin-top: var(--lc-space-1);
  color: #e07a18;
  font-size: var(--lc-text-xs);
  font-style: normal;
  font-weight: 800;
}

@media (max-width: 960px) {
  .me-hero {
    grid-template-columns: 1fr;
  }

  .me-center-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 1023px) {
  .me-layout {
    width: calc(100% - 32px);
    grid-template-columns: 1fr;
  }

  .me-sidebar {
    position: static;
  }

  .me-kpis {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .me-recommend-list {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 767px) {
  .me-layout {
    width: calc(100% - 24px);
    margin-top: var(--lc-space-4);
  }

  .me-sidebar {
    padding: var(--lc-space-4);
  }

  .me-menu {
    display: grid;
    overflow: visible;
  }

  .me-menu-item {
    border-radius: var(--lc-radius-sm);
  }

  .me-hero {
    min-height: 0;
    padding: var(--lc-space-5);
  }

  .me-user {
    flex-direction: column;
  }

  .me-avatar {
    width: 88px;
    height: 88px;
  }

  .me-meta h1 {
    font-size: 24px;
  }

  .me-profile-line {
    gap: var(--lc-space-2);
  }

  .me-center-grid,
  .me-kpis {
    grid-template-columns: 1fr;
  }
}

/* Prototype fidelity pass: keep this page close to the supplied desktop mock. */
.me-layout {
  position: relative;
  width: calc(100% - 40px);
  max-width: 1880px;
  margin: 18px auto 26px;
  display: grid;
  grid-template-columns: 304px minmax(0, 1fr);
  gap: 20px;
  align-items: start;
  color: #1f2937;
}

.me-layout::before {
  position: fixed;
  inset: 0;
  z-index: -1;
  background:
    radial-gradient(circle at 18% 8%, rgba(236, 72, 153, .08), transparent 30%),
    radial-gradient(circle at 86% 24%, rgba(99, 102, 241, .08), transparent 34%),
    #f7f9fd;
  content: "";
}

.me-sidebar {
  position: sticky;
  top: 86px;
  min-height: 780px;
  padding: 26px 22px 22px;
  border: 1px solid rgba(229, 234, 244, .95);
  border-radius: 18px;
  background: rgba(255, 255, 255, .96);
  box-shadow: 0 16px 36px rgba(31, 42, 68, .07);
}

.me-sidebar h2 {
  margin: 0 0 20px;
  color: #172033;
  font-size: 22px;
  font-weight: 900;
}

.me-menu {
  display: grid;
  gap: 7px;
}

.me-menu-group {
  display: grid;
  gap: 2px;
}

.me-menu-item {
  display: grid;
  grid-template-columns: 24px 1fr auto;
  align-items: center;
  min-height: 44px;
  padding: 0 14px;
  border-radius: 12px;
  color: #5f6b80;
  font-size: 14px;
  font-weight: 800;
  text-decoration: none;
}

.me-menu-item.is-active,
.me-menu-item:hover {
  background: #fff0f6;
  color: #e63d8c;
}

.me-menu-icon {
  width: 22px;
  color: #7c899d;
  text-align: center;
}

.me-menu-icon.is-pink,
.me-menu-item.is-active .me-menu-icon {
  color: #e63d8c;
}

.me-menu-caret {
  color: #97a3b6;
  font-size: 12px;
}

.me-sub-item {
  display: flex;
  align-items: center;
  min-height: 30px;
  margin-left: 46px;
  padding: 0 8px;
  border-radius: 8px;
  color: #5f6b80;
  font-size: 13px;
  font-weight: 700;
  text-decoration: none;
}

.me-sub-item:hover {
  color: #e63d8c;
  background: #fff7fb;
}

.me-dot {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 999px;
  background: #ff315f;
  color: #fff;
  font-size: 11px;
  line-height: 1;
}

.me-fellowship-card {
  position: relative;
  overflow: hidden;
  margin-top: 24px;
  min-height: 150px;
  padding: 20px 16px;
  border-radius: 16px;
  background:
    radial-gradient(circle at 84% 70%, rgba(236, 72, 153, .42), transparent 25%),
    radial-gradient(circle at 104% 92%, rgba(96, 165, 250, .35), transparent 26%),
    linear-gradient(135deg, #f3edff 0%, #eef3ff 100%);
}

.me-fellowship-card::after {
  position: absolute;
  right: 16px;
  bottom: 12px;
  width: 78px;
  height: 62px;
  border-radius: 24px 24px 18px 18px;
  background:
    radial-gradient(circle at 32% 36%, rgba(255, 255, 255, .88) 0 7px, transparent 8px),
    radial-gradient(circle at 62% 32%, rgba(255, 255, 255, .78) 0 6px, transparent 7px),
    linear-gradient(135deg, rgba(236, 72, 153, .32), rgba(96, 165, 250, .24));
  content: "";
  filter: blur(.1px);
}

.me-fellowship-card p {
  margin: 0 0 8px;
  color: #6d5dfc;
  font-size: 15px;
  font-weight: 900;
}

.me-fellowship-card span {
  display: block;
  max-width: 164px;
  color: #68758b;
  font-size: 12px;
  line-height: 1.7;
}

.me-fellowship-card a {
  display: inline-flex;
  margin-top: 20px;
  padding: 8px 14px;
  border: 1px solid rgba(109, 93, 252, .18);
  border-radius: 999px;
  color: #6d5dfc;
  background: rgba(255, 255, 255, .72);
  font-size: 12px;
  font-weight: 900;
  text-decoration: none;
}

.me-main {
  display: grid;
  gap: 20px;
}

.me-hero {
  position: relative;
  overflow: hidden;
  display: grid;
  grid-template-columns: minmax(460px, 1fr) 760px;
  gap: 28px;
  align-items: center;
  min-height: 246px;
  padding: 38px 34px 36px 54px;
  border: 1px solid rgba(255, 255, 255, .86);
  border-radius: 20px;
  background:
    radial-gradient(circle at 12% 14%, rgba(255, 255, 255, .68), transparent 21%),
    radial-gradient(circle at 88% 42%, rgba(255, 255, 255, .26), transparent 30%),
    linear-gradient(103deg, #ffd9ef 0%, #f6d8ff 36%, #bacaff 100%);
  box-shadow: 0 18px 40px rgba(96, 111, 191, .16);
}

.me-hero::after {
  position: absolute;
  right: -70px;
  bottom: -110px;
  width: 360px;
  height: 240px;
  border-radius: 999px;
  background: rgba(255, 255, 255, .22);
  content: "";
}

.me-user {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 34px;
  min-width: 0;
}

.me-avatar {
  width: 132px;
  height: 132px;
  flex: 0 0 auto;
  border: 6px solid rgba(255, 255, 255, .94);
  border-radius: 999px;
  object-fit: cover;
  box-shadow: 0 18px 36px rgba(79, 53, 134, .26);
}

.me-avatar-fallback {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #6d5dfc;
  background:
    radial-gradient(circle at 36% 34%, #ffffff 0 12px, transparent 13px),
    radial-gradient(circle at 62% 34%, #ffffff 0 12px, transparent 13px),
    linear-gradient(135deg, #fff, #eef3ff);
  font-size: 46px;
  font-weight: 900;
}

.me-title-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.me-meta h1 {
  margin: 0;
  color: #20243a;
  font-size: 29px;
  font-weight: 900;
  line-height: 1.2;
}

.me-level {
  padding: 4px 10px;
  border-radius: 999px;
  color: #6d63d8;
  background: rgba(255, 255, 255, .72);
  font-size: 12px;
  font-weight: 900;
}

.me-bio {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 14px 0 14px;
  color: #5f647b;
  font-size: 14px;
}

.me-bio button {
  border: 0;
  color: #5964d5;
  background: transparent;
  cursor: pointer;
}

.me-profile-line {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  color: #6e7890;
  font-size: 12px;
  font-weight: 700;
}

.me-edit-btn {
  margin-top: 24px;
  min-width: 104px;
  height: 34px;
  border: 0;
  border-radius: 999px;
  color: #6d5dfc;
  background: rgba(255, 255, 255, .78);
  box-shadow: 0 8px 18px rgba(91, 72, 214, .12);
  font-size: 13px;
  font-weight: 900;
  cursor: pointer;
}

.me-kpis {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.me-kpis article {
  display: grid;
  align-content: start;
  min-height: 142px;
  padding: 24px 20px 18px;
  border: 1px solid rgba(255, 255, 255, .76);
  border-radius: 16px;
  background: rgba(255, 255, 255, .84);
  box-shadow: 0 14px 28px rgba(80, 92, 150, .12);
}

.me-kpi-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  margin-bottom: 12px;
  border-radius: 8px;
  color: #fff;
  font-size: 13px;
  font-weight: 900;
}

.me-kpis p {
  margin: 0 0 12px;
  color: #5b6678;
  font-size: 13px;
  font-weight: 800;
}

.me-kpis strong {
  margin: 0;
  color: #121827;
  font-size: 32px;
  font-weight: 900;
  line-height: 1;
}

.me-kpis small {
  margin-top: 10px;
  color: #818b9d;
  font-size: 12px;
}

.me-center-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 20px;
}

.me-center-card,
.me-recommend-panel {
  border: 1px solid rgba(226, 232, 240, .9);
  border-radius: 18px;
  background: rgba(255, 255, 255, .97);
  box-shadow: 0 14px 32px rgba(31, 42, 68, .07);
}

.me-center-card {
  display: grid;
  grid-template-rows: auto 1fr auto;
  gap: 18px;
  min-height: 258px;
  padding: 28px 22px 22px;
}

.me-card-head {
  display: flex;
  align-items: center;
  gap: 16px;
}

.me-card-icon,
.me-recommend-icon {
  display: inline-flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  width: 54px;
  height: 54px;
  border-radius: 14px;
  color: #fff;
  font-size: 21px;
  font-weight: 900;
}

.me-card-head h3 {
  margin: 0;
  color: #20273a;
  font-size: 20px;
  font-weight: 900;
}

.me-card-head p {
  margin: 5px 0 0;
  color: #94a0b2;
  font-size: 12px;
  font-weight: 700;
}

.me-card-list {
  display: grid;
  align-content: start;
  gap: 10px;
}

.me-card-list a {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 24px;
  padding-bottom: 7px;
  border-bottom: 1px solid #edf1f7;
  color: #637084;
  font-size: 13px;
  text-decoration: none;
}

.me-card-list strong {
  color: #697489;
  font-weight: 900;
}

.me-card-action {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 40px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 900;
  text-decoration: none;
}

.me-recommend-panel {
  position: relative;
  overflow: hidden;
  min-height: 120px;
  padding: 20px 26px 22px;
}

.me-recommend-panel h3 {
  margin: 0;
  color: #20273a;
  font-size: 18px;
  font-weight: 900;
}

.me-recommend-panel::after {
  position: absolute;
  right: 42px;
  bottom: -22px;
  width: 126px;
  height: 118px;
  border-radius: 32px;
  background:
    linear-gradient(90deg, rgba(255, 255, 255, .55) 47%, rgba(236, 72, 153, .22) 48% 54%, rgba(255, 255, 255, .55) 55%),
    radial-gradient(circle at 34% 26%, rgba(236, 72, 153, .18), transparent 34%),
    linear-gradient(135deg, rgba(255, 255, 255, .3), rgba(96, 165, 250, .16));
  transform: rotate(8deg);
  content: "";
}

.me-recommend-list {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
  margin-top: 16px;
  padding-right: 120px;
}

.me-recommend-item {
  display: flex;
  align-items: center;
  gap: 14px;
  min-height: 68px;
  padding: 12px 16px;
  border-radius: 14px;
  color: inherit;
  background: linear-gradient(135deg, rgba(248, 250, 252, .82), rgba(255, 255, 255, .74));
  text-decoration: none;
}

.me-recommend-icon {
  width: 48px;
  height: 48px;
  border-radius: 13px;
  font-size: 18px;
}

.me-recommend-item strong,
.me-recommend-item small,
.me-recommend-item em {
  display: block;
}

.me-recommend-item strong {
  color: #20273a;
  font-size: 15px;
  font-weight: 900;
}

.me-recommend-item small {
  margin-top: 3px;
  color: #8b96a8;
  font-size: 12px;
}

.me-recommend-item em {
  margin-top: 6px;
  color: #e07a18;
  font-size: 12px;
  font-style: normal;
  font-weight: 900;
}

@media (max-width: 1500px) {
  .me-hero {
    grid-template-columns: 1fr;
  }

  .me-kpis {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }

  .me-center-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 1023px) {
  .me-layout {
    width: calc(100% - 28px);
    grid-template-columns: 1fr;
  }

  .me-sidebar {
    position: static;
    min-height: 0;
  }

  .me-kpis {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 10px;
  }

  .me-kpis article {
    min-height: 108px;
    padding: 12px 10px;
    border-radius: 12px;
  }

  .me-kpi-icon {
    width: 24px;
    height: 24px;
    margin-bottom: 8px;
    font-size: 12px;
  }

  .me-kpis p {
    margin-bottom: 6px;
    font-size: 12px;
  }

  .me-kpis strong {
    font-size: 20px;
  }

  .me-kpis small {
    margin-top: 4px;
    font-size: 11px;
  }

  .me-center-grid,
  .me-recommend-list {
    grid-template-columns: 1fr;
  }

  .me-recommend-list {
    padding-right: 0;
  }
}

@media (max-width: 767px) {
  .me-layout {
    display: block;
    width: calc(100% - 16px);
    margin: 8px auto 82px;
  }

  .me-layout::before {
    background: #f5f7fc;
  }

  .me-sidebar {
    display: none;
  }

  .me-main {
    display: grid;
    gap: 10px;
  }

  .me-hero {
    min-height: 0;
    padding: 14px 12px;
    gap: 12px;
    border: 1px solid #ebeff7;
    border-radius: 16px;
    background: #fff;
    box-shadow: 0 8px 20px rgba(15, 23, 42, .05);
  }

  .me-hero::after {
    display: none;
  }

  .me-user {
    align-items: center;
    flex-direction: row;
    gap: 12px;
  }

  .me-avatar {
    width: 68px;
    height: 68px;
    border-width: 3px;
    box-shadow: none;
  }

  .me-meta h1 {
    font-size: 20px;
  }

  .me-level {
    padding: 3px 8px;
    font-size: 11px;
  }

  .me-bio {
    margin: 4px 0 6px;
    font-size: 12px;
  }

  .me-profile-line {
    gap: 8px;
    font-size: 11px;
  }

  .me-edit-btn {
    margin-top: 8px;
    height: 30px;
    min-width: 88px;
    font-size: 12px;
  }

  .me-kpis {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 8px;
  }

  .me-kpis article {
    min-height: 92px;
    padding: 10px;
    border: 1px solid #edf1f8;
    border-radius: 12px;
    background: #f9fbff;
    box-shadow: none;
  }

  .me-kpi-icon {
    width: 22px;
    height: 22px;
    margin-bottom: 6px;
    border-radius: 7px;
    font-size: 11px;
  }

  .me-kpis p {
    margin: 0 0 6px;
    font-size: 12px;
  }

  .me-kpis strong {
    font-size: 22px;
  }

  .me-kpis small {
    margin-top: 3px;
    font-size: 11px;
  }

  .me-center-grid {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .me-center-card {
    min-height: 0;
    padding: 12px;
    gap: 8px;
    border: 1px solid #ebeff7;
    border-radius: 14px;
    box-shadow: none;
  }

  .me-card-head {
    gap: 10px;
  }

  .me-card-head h3 {
    font-size: 17px;
  }

  .me-card-head p {
    margin-top: 2px;
    font-size: 11px;
  }

  .me-card-icon {
    width: 36px;
    height: 36px;
    border-radius: 11px;
    font-size: 14px;
  }

  .me-card-list {
    gap: 2px;
  }

  .me-card-list a {
    min-height: 26px;
    padding-bottom: 4px;
    font-size: 13px;
  }

  .me-card-action {
    min-height: 34px;
    border-radius: 10px;
    font-size: 12px;
  }

  .me-recommend-panel {
    min-height: 0;
    padding: 12px;
    border: 1px solid #ebeff7;
    border-radius: 14px;
    box-shadow: none;
  }

  .me-recommend-panel::after {
    display: none;
  }

  .me-recommend-list {
    grid-template-columns: 1fr;
    gap: 8px;
    margin-top: 10px;
    padding-right: 0;
  }

  .me-recommend-item {
    min-height: 56px;
    padding: 10px 12px;
    border-radius: 12px;
    background: #f8faff;
  }

  :deep(.co-creation-toolbar) {
    display: none;
  }
}

.mobile-only {
  display: none;
}

@media (max-width: 767px) {
  :global(.platform-header),
  :global(.platform-route-back),
  :global(.platform-footer),
  :global(.co-creation-toolbar) {
    display: none !important;
  }

  .mobile-only {
    display: block;
  }

  .me-layout {
    width: 100%;
    margin: 0;
    padding: 0 14px 84px;
    background:
      linear-gradient(160deg, #ffd6e9 0%, #f2ddff 34%, #d9e6ff 68%, #f7f8fc 68%);
  }

  .me-main {
    gap: 10px;
  }

  .mobile-me-header {
    position: relative;
    padding: 42px 8px 12px;
    text-align: center;
  }

  .mobile-me-header h1 {
    margin: 0;
    color: #141827;
    font-size: 18px;
    font-weight: 900;
    letter-spacing: .02em;
  }

  .mobile-header-actions {
    position: absolute;
    top: 40px;
    right: 8px;
    display: flex;
    gap: 14px;
  }

  .mobile-header-icon {
    position: relative;
    color: #1f2937;
    font-size: 18px;
    font-weight: 900;
    text-decoration: none;
  }

  .mobile-header-icon span {
    position: absolute;
    top: -8px;
    right: -8px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 15px;
    height: 15px;
    padding: 0 4px;
    border-radius: 999px;
    background: #ff315f;
    color: #fff;
    font-size: 10px;
  }

  .me-hero {
    overflow: visible;
    padding: 10px 6px 8px;
    border: 0;
    background: transparent;
    box-shadow: none;
  }

  .me-user {
    align-items: center;
    gap: 13px;
  }

  .me-avatar {
    width: 74px;
    height: 74px;
    border: 3px solid rgba(255, 255, 255, .94);
    box-shadow: 0 10px 24px rgba(104, 78, 180, .18);
  }

  .me-meta h1 {
    font-size: 18px;
  }

  .me-level {
    background: rgba(255, 255, 255, .7);
  }

  .me-bio {
    margin: 6px 0;
    color: #6b7280;
  }

  .me-profile-line {
    color: #6d7487;
  }

  .me-edit-btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    margin-top: 10px;
    margin-right: 8px;
    padding: 0 16px;
    border-radius: 999px;
    color: #6d5dfc;
    background: rgba(255, 255, 255, .76);
  }

  .mobile-fellowship-pill {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-height: 30px;
    margin-top: 10px;
    padding: 0 16px;
    border-radius: 999px;
    color: #fff;
    background: linear-gradient(135deg, #8b5cf6, #6d5dfc);
    font-size: 12px;
    font-weight: 900;
    text-decoration: none;
  }

  .me-kpis,
  .me-center-grid,
  .me-recommend-panel {
    display: none;
  }

  .mobile-shortcut-card,
  .mobile-data-card,
  .mobile-list-card,
  .mobile-recommend-card {
    border: 1px solid rgba(235, 239, 247, .86);
    border-radius: 16px;
    background: rgba(255, 255, 255, .94);
    box-shadow: 0 10px 24px rgba(31, 42, 68, .06);
  }

  .mobile-shortcut-card {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 4px;
    padding: 14px 8px 12px;
  }

  .mobile-shortcut-item {
    display: grid;
    justify-items: center;
    gap: 5px;
    color: #111827;
    text-align: center;
    text-decoration: none;
  }

  .mobile-shortcut-icon,
  .mobile-data-icon,
  .mobile-list-icon,
  .mobile-recommend-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-weight: 900;
  }

  .mobile-shortcut-icon {
    width: 42px;
    height: 42px;
    border-radius: 13px;
    font-size: 18px;
  }

  .mobile-shortcut-item strong {
    font-size: 13px;
    font-weight: 900;
  }

  .mobile-shortcut-item small {
    color: #6f788a;
    font-size: 11px;
  }

  .mobile-section-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 14px 14px 8px;
  }

  .mobile-section-head h2,
  .mobile-recommend-card h2 {
    margin: 0;
    color: #171d2e;
    font-size: 15px;
    font-weight: 900;
  }

  .mobile-section-head a {
    color: #8a93a5;
    font-size: 11px;
    text-decoration: none;
  }

  .mobile-data-row {
    display: grid;
    grid-template-columns: repeat(5, minmax(0, 1fr));
    gap: 8px;
    padding: 8px 12px 15px;
  }

  .mobile-data-item {
    display: grid;
    justify-items: center;
    gap: 5px;
    min-width: 0;
    padding: 9px 4px;
    border-radius: 12px;
    background: #fafbff;
    color: #111827;
    text-decoration: none;
  }

  .mobile-data-icon {
    width: 28px;
    height: 28px;
    border-radius: 9px;
    font-size: 13px;
  }

  .mobile-data-item strong {
    font-size: 20px;
    line-height: 1;
  }

  .mobile-data-item small {
    color: #6f788a;
    font-size: 10px;
    white-space: nowrap;
  }

  .mobile-list-card {
    padding: 8px 14px;
  }

  .mobile-list-item {
    display: grid;
    grid-template-columns: 28px minmax(0, auto) 1fr auto;
    align-items: center;
    gap: 10px;
    min-height: 42px;
    border-bottom: 1px solid #eef2f7;
    color: inherit;
    text-decoration: none;
  }

  .mobile-list-item:last-child {
    border-bottom: 0;
  }

  .mobile-list-icon {
    width: 24px;
    height: 24px;
    border-radius: 8px;
    font-size: 12px;
  }

  .mobile-list-item strong {
    color: #1f2937;
    font-size: 13px;
    font-weight: 900;
  }

  .mobile-list-item small {
    display: none;
  }

  .mobile-list-item em {
    color: #7b8496;
    font-size: 11px;
    font-style: normal;
    text-align: right;
  }

  .mobile-recommend-card {
    padding: 14px;
  }

  .mobile-recommend-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 10px;
    margin-top: 12px;
  }

  .mobile-recommend-item {
    display: grid;
    gap: 5px;
    justify-items: center;
    min-width: 0;
    padding: 10px 6px;
    border-radius: 12px;
    background: #fbf7ff;
    color: inherit;
    text-align: center;
    text-decoration: none;
  }

  .mobile-recommend-icon {
    width: 32px;
    height: 32px;
    border-radius: 10px;
  }

  .mobile-recommend-item strong {
    font-size: 12px;
  }

  .mobile-recommend-item small {
    color: #7b8496;
    font-size: 10px;
    line-height: 1.35;
  }

  .mobile-recommend-item em {
    margin-top: 3px;
    min-width: 58px;
    padding: 6px 8px;
    border-radius: 999px;
    background: rgba(236, 72, 153, .1);
    color: #e63d8c;
    font-size: 11px;
    font-style: normal;
    font-weight: 900;
  }

  .is-purple {
    background: linear-gradient(135deg, #8b5cf6, #6d5dfc);
  }

  .is-pink {
    background: linear-gradient(135deg, #ff7ab6, #ec4899);
  }

  .is-orange {
    background: linear-gradient(135deg, #ffb13d, #f97316);
  }

  .is-blue {
    background: linear-gradient(135deg, #60a5fa, #3b82f6);
  }

  .is-amber {
    background: linear-gradient(135deg, #fbbf24, #f59e0b);
  }

  .is-teal {
    background: linear-gradient(135deg, #34d399, #14b8a6);
  }
}
</style>

