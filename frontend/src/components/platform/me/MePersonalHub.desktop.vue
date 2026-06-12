<template>
  <section class="me-hub" aria-label="个人中心">
    <aside class="me-hub-side">
      <p class="side-kicker">我的</p>
      <nav class="side-nav" aria-label="个人中心导航">
        <router-link
          v-for="item in sideNavItems"
          :key="item.to"
          :to="item.to"
          class="side-nav-item"
          :class="{ active: isNavActive(item) }"
        >
          <span class="side-nav-icon" aria-hidden="true">{{ item.icon }}</span>
          <span class="side-nav-label">{{ item.label }}</span>
          <em v-if="item.badge" class="side-nav-badge">{{ item.badge > 99 ? '99+' : item.badge }}</em>
        </router-link>
      </nav>

      <div class="side-progress">
        <div class="side-progress-head">
          <strong>完善资料</strong>
          <span>{{ profileCompletion }}%</span>
        </div>
        <div class="side-progress-bar" role="progressbar" :aria-valuenow="profileCompletion" aria-valuemin="0" aria-valuemax="100">
          <i :style="{ width: `${profileCompletion}%` }"></i>
        </div>
        <button type="button" class="side-progress-btn" @click="onOpenEdit">去完善</button>
      </div>

      <div class="side-foot">
        <router-link to="/me/feedback">客服中心</router-link>
        <router-link to="/platform/help">帮助与反馈</router-link>
      </div>
    </aside>

    <div class="me-hub-main">
      <header class="profile-hero platform-card">
        <div class="profile-hero-top">
          <div class="profile-identity">
            <div class="avatar-ring">
              <img v-if="avatarSrc" :src="avatarSrc" alt="头像" class="avatar-img">
              <div v-else class="avatar-fb">{{ avatarFallback }}</div>
              <span class="lv-badge">Lv.{{ growthLevel.level }}</span>
            </div>
            <div class="profile-copy">
              <div class="name-row">
                <h1>{{ displayName }}</h1>
                <span class="pill pill-violet">{{ growthLevel.name }}</span>
              </div>
              <div class="tag-row">
                <span v-for="tag in profileTags" :key="tag" class="pill pill-soft">{{ tag }}</span>
              </div>
              <p class="bio">{{ bioText }}</p>
            </div>
          </div>

          <div class="completion-ring" aria-label="资料完整度">
            <svg viewBox="0 0 88 88" class="ring-svg">
              <circle cx="44" cy="44" r="36" class="ring-track"></circle>
              <circle
                cx="44"
                cy="44"
                r="36"
                class="ring-fill"
                :style="{ strokeDashoffset: ringOffset }"
              ></circle>
            </svg>
            <div class="ring-center">
              <strong>{{ profileCompletion }}%</strong>
              <small>资料完整度</small>
            </div>
          </div>
        </div>

        <div class="profile-actions">
          <button type="button" class="hub-btn hub-btn-primary" @click="onOpenEdit">编辑资料</button>
          <router-link class="hub-btn hub-btn-ghost" to="/me/verify">身份认证</router-link>
          <button type="button" class="hub-btn hub-btn-ghost" @click="onOpenSettings">账号设置</button>
          <router-link class="hub-btn hub-btn-ghost" to="/me/privacy">隐私设置</router-link>
        </div>
      </header>

      <div class="hub-grid">
        <div class="hub-col hub-col-left">
          <section class="hub-card platform-card">
            <header class="hub-card-head">
              <h2>个人资料</h2>
              <button type="button" class="text-link" @click="onOpenEdit">编辑</button>
            </header>
            <dl class="info-grid">
              <div v-for="row in profileRows" :key="row.label">
                <dt>{{ row.label }}</dt>
                <dd>{{ row.value }}</dd>
              </div>
            </dl>
          </section>

          <section class="hub-card platform-card">
            <header class="hub-card-head">
              <h2>认证状态</h2>
            </header>
            <ul class="verify-list">
              <li v-for="item in verifyRows" :key="item.label">
                <span>{{ item.label }}</span>
                <em :class="item.done ? 'ok' : 'pending'">{{ item.done ? '已认证' : '未认证' }}</em>
              </li>
            </ul>
          </section>

          <section class="hub-card platform-card">
            <header class="hub-card-head">
              <h2>账号安全</h2>
            </header>
            <ul class="security-list">
              <li v-for="item in securityRows" :key="item.label">
                <div>
                  <strong>{{ item.label }}</strong>
                  <p>{{ item.value }}</p>
                </div>
                <router-link :to="item.to" class="text-link">{{ item.action }}</router-link>
              </li>
            </ul>
          </section>
        </div>

        <div class="hub-col hub-col-right">
          <section class="hub-card platform-card">
            <header class="hub-card-head">
              <h2>我的动态</h2>
              <router-link to="/me/posts" class="text-link">查看全部 ›</router-link>
            </header>
            <div v-if="postsLoading" class="mini-empty">加载中…</div>
            <ul v-else-if="recentPosts.length" class="feed-list">
              <li v-for="post in recentPosts" :key="post.id">
                <div class="feed-thumb" aria-hidden="true">📝</div>
                <div class="feed-body">
                  <p>{{ postPreview(post.content) }}</p>
                  <span>{{ formatDate(post.createdAt) }} · 鼓励 {{ post.encourageCount || 0 }}</span>
                </div>
              </li>
            </ul>
            <p v-else class="mini-empty">还没有动态，<router-link to="/platform/positive-share">去发布</router-link></p>
          </section>

          <section class="hub-card platform-card">
            <header class="hub-card-head">
              <h2>我的活动</h2>
              <router-link to="/platform/my-activities" class="text-link">查看全部 ›</router-link>
            </header>
            <div v-if="activitiesLoading" class="mini-empty">加载中…</div>
            <article v-else-if="featuredActivity" class="activity-spotlight">
              <div class="activity-thumb" aria-hidden="true">▣</div>
              <div class="activity-body">
                <h3>{{ featuredActivity.title }}</h3>
                <p>{{ featuredActivity.meta }}</p>
                <span class="activity-status">{{ featuredActivity.statusLabel }}</span>
              </div>
            </article>
            <p v-else class="mini-empty">暂无活动报名，<router-link :to="eventsPath()">去活动广场</router-link></p>
          </section>

          <section class="hub-card platform-card">
            <header class="hub-card-head">
              <h2>我的消息</h2>
              <router-link to="/me/notifications" class="text-link">查看全部 ›</router-link>
            </header>
            <div class="msg-grid">
              <router-link
                v-for="item in messageShortcuts"
                :key="item.label"
                :to="item.to"
                class="msg-tile"
              >
                <span class="msg-icon" aria-hidden="true">{{ item.icon }}</span>
                <strong>{{ item.label }}</strong>
                <em v-if="item.badge">{{ item.badge }}</em>
              </router-link>
            </div>
          </section>

          <section class="hub-card platform-card">
            <header class="hub-card-head">
              <h2>快捷入口</h2>
            </header>
            <div class="shortcut-grid">
              <router-link
                v-for="item in shortcutItems"
                :key="item.label"
                :to="item.to"
                class="shortcut-item"
              >
                <span class="shortcut-icon" aria-hidden="true">{{ item.icon }}</span>
                <span>{{ item.label }}</span>
              </router-link>
            </div>
          </section>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { toFullUrl } from '@/utils/image.js'
import { userAvatarUrlFromApi } from '@/utils/displayFields.js'
import { getMyFellowshipProfile } from '@/api/fellowshipProfile.js'
import { fetchMyPositiveShares } from '@/api/positiveShare.js'
import { getMyEventSignups } from '@/api/platformContent.js'
import { fetchMyGroupActivitySignups } from '@/api/myActivities.js'
import { getActivityDisplayStatus, getParticipationStatusLabel } from '@/utils/activityStatus.js'
import { usePlatformPath } from '@/composables/usePlatformPath.js'

const props = defineProps({
  user: { type: Object, default: null },
  displayName: { type: String, required: true },
  unreadCount: { type: Number, default: 0 },
  growthLevel: { type: Object, required: true },
  onOpenEdit: { type: Function, required: true },
  onOpenSettings: { type: Function, required: true }
})

const route = useRoute()
const { eventsPath } = usePlatformPath()

const fellowshipProfile = ref(null)
const recentPosts = ref([])
const postsLoading = ref(true)
const featuredActivity = ref(null)
const activitiesLoading = ref(true)

const avatarFallback = computed(() => String(props.displayName || 'U').trim().slice(0, 1).toUpperCase())
const avatarSrc = computed(() => {
  const raw = userAvatarUrlFromApi(props.user)
  return raw ? toFullUrl(raw) : ''
})

const verificationStatus = computed(() => String(props.user?.verificationStatus || 'none').toLowerCase())

const profileCompletion = computed(() => {
  const fp = fellowshipProfile.value
  const checks = [
    props.user?.username || props.user?.nickname,
    userAvatarUrlFromApi(props.user),
    props.user?.location || fp?.city,
    props.user?.bio || fp?.bio,
    props.user?.phone,
    fp?.gender,
    fp?.age || fp?.birthYear,
    fp?.occupation,
    fp?.education,
    verificationStatus.value === 'approved'
  ]
  const done = checks.filter(Boolean).length
  return Math.round((done / checks.length) * 100)
})

const ringOffset = computed(() => {
  const circumference = 2 * Math.PI * 36
  return circumference - (profileCompletion.value / 100) * circumference
})

const bioText = computed(() => {
  const text = String(props.user?.bio || fellowshipProfile.value?.bio || '').trim()
  return text || '写一句简介，让伙伴更了解你。'
})

const profileTags = computed(() => {
  const fp = fellowshipProfile.value
  const tags = []
  const genderMap = { female: '女', male: '男', other: '其他' }
  if (fp?.gender) tags.push(genderMap[String(fp.gender).toLowerCase()] || fp.gender)
  if (fp?.age) tags.push(`${fp.age}岁`)
  else if (fp?.birthYear) tags.push(`${new Date().getFullYear() - Number(fp.birthYear)}岁`)
  const city = fp?.city || props.user?.location
  if (city) tags.push(String(city).split(/[\s/]/)[0])
  if (fp?.occupation) tags.push(fp.occupation)
  if (!tags.length) tags.push('完善资料后展示标签')
  return tags.slice(0, 4)
})

const profileRows = computed(() => {
  const fp = fellowshipProfile.value
  const genderMap = { female: '女', male: '男', other: '其他' }
  return [
    { label: '昵称', value: props.displayName || '—' },
    { label: '性别', value: fp?.gender ? (genderMap[String(fp.gender).toLowerCase()] || fp.gender) : '未填写' },
    {
      label: '年龄',
      value: fp?.age ? `${fp.age}岁` : (fp?.birthYear ? `${new Date().getFullYear() - Number(fp.birthYear)}岁` : '未填写')
    },
    { label: '城市', value: fp?.city || props.user?.location || '未填写' },
    { label: '行业', value: fp?.occupation || '未填写' },
    { label: '学历', value: fp?.education || '未填写' }
  ]
})

const verifyRows = computed(() => [
  { label: '实名认证', done: verificationStatus.value === 'approved' },
  { label: '学历认证', done: Boolean(fellowshipProfile.value?.education) },
  { label: '职业认证', done: Boolean(fellowshipProfile.value?.occupation) }
])

const securityRows = computed(() => [
  { label: '登录密码', value: '建议定期更换', to: '/fellowship/change-password', action: '修改' },
  { label: '手机号', value: props.user?.phone || '未绑定', to: '/fellowship/change-phone', action: '换绑' },
  { label: '邮箱', value: props.user?.email || '未绑定', to: '/me/security', action: '设置' },
  { label: '第三方账号', value: '微信 / QQ', to: '/me/security', action: '管理' }
])

const sideNavItems = computed(() => [
  { label: '我的主页', to: '/platform/me', icon: '🏠', exact: true },
  { label: '我的动态', to: '/me/posts', icon: '📝' },
  { label: '联谊中心', to: '/fellowship', icon: '💞' },
  { label: '我的活动', to: '/platform/my-activities', icon: '▣' },
  { label: '共读内容', to: '/articles', icon: '📚' },
  { label: '我的收藏', to: '/me/favorites', icon: '⭐' },
  { label: '我的消息', to: '/me/notifications', icon: '🔔', badge: props.unreadCount || 0 },
  { label: '任务成长', to: '/me/tasks', icon: '✅' },
  { label: '邀请好友', to: '/fellowship/invite', icon: '🎁' }
])

const messageShortcuts = computed(() => [
  { label: '互动消息', icon: '💬', to: '/me/notifications', badge: 0 },
  { label: '系统通知', icon: '🔔', to: '/me/notifications', badge: props.unreadCount || 0 },
  { label: '活动提醒', icon: '▣', to: '/platform/my-activities', badge: 0 },
  { label: '联谊消息', icon: '💞', to: '/fellowship/messages', badge: 0 }
])

const shortcutItems = [
  { label: '我的团体', icon: '🏠', to: '/me/groups' },
  { label: '心声收藏', icon: '⭐', to: '/me/favorites' },
  { label: '每日心声', icon: '♡', to: '/platform/positive-share' },
  { label: '活动广场', icon: '✦', to: '/platform/events' },
  { label: '我的徽章', icon: '🏅', to: '/me/badges' },
  { label: '问题工单', icon: '💡', to: '/me/feedback' },
  { label: '通知渠道', icon: '📬', to: '/me/notification-settings' },
  { label: '更新日志', icon: '📋', to: '/platform/changelog' }
]

function isNavActive(item) {
  if (item.exact) return route.path === item.to || route.path === '/me'
  return route.path === item.to || route.path.startsWith(`${item.to}/`)
}

function postPreview(text) {
  const raw = String(text || '').trim()
  if (!raw) return '（无文字内容）'
  return raw.length > 56 ? `${raw.slice(0, 56)}…` : raw
}

function formatDate(raw) {
  if (!raw) return '—'
  const d = new Date(raw)
  if (Number.isNaN(d.getTime())) return String(raw).slice(0, 10)
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${m}.${day} ${h}:${min}`
}

function formatActivityMeta(item) {
  const time = item.startTime || item.eventTime
  const loc = item.location || item.venue || '待定'
  return `${formatDate(time)} · ${loc}`
}

function normalizeActivity(item, type) {
  const display = getActivityDisplayStatus(item)
  const participation = getParticipationStatusLabel(item.status || item.signupStatus)
  return {
    title: item.title || item.eventTitle || '活动',
    meta: formatActivityMeta(item),
    statusLabel: participation || display.label,
    sortTime: new Date(item.startTime || item.eventTime || 0).getTime()
  }
}

async function loadSideData() {
  postsLoading.value = true
  activitiesLoading.value = true
  try {
    const [fpRes, postsRes, platformRes, groupRes] = await Promise.allSettled([
      getMyFellowshipProfile(),
      fetchMyPositiveShares({ pageNum: 1, pageSize: 3 }),
      getMyEventSignups(),
      fetchMyGroupActivitySignups()
    ])
    if (fpRes.status === 'fulfilled') fellowshipProfile.value = fpRes.value || null

    if (postsRes.status === 'fulfilled') {
      const res = postsRes.value
      recentPosts.value = Array.isArray(res?.list) ? res.list.slice(0, 3) : (Array.isArray(res) ? res.slice(0, 3) : [])
    } else {
      recentPosts.value = []
    }

    const platform = platformRes.status === 'fulfilled'
      ? (Array.isArray(platformRes.value) ? platformRes.value : (platformRes.value?.list || []))
      : []
    const group = groupRes.status === 'fulfilled'
      ? (Array.isArray(groupRes.value) ? groupRes.value : (groupRes.value?.list || []))
      : []
    const merged = [
      ...platform.map((i) => normalizeActivity(i, 'PLATFORM')),
      ...group.map((i) => normalizeActivity(i, 'GROUP'))
    ].sort((a, b) => b.sortTime - a.sortTime)
    featuredActivity.value = merged[0] || null
  } finally {
    postsLoading.value = false
    activitiesLoading.value = false
  }
}

onMounted(() => {
  loadSideData()
})
</script>

<style scoped>
.me-hub {
  --hub-accent: var(--lc-violet);
  --hub-accent-soft: var(--lc-indigo-soft);
  --hub-accent-border: color-mix(in srgb, var(--lc-violet) 22%, var(--lc-border));
  width: 100%;
  max-width: none;
  box-sizing: border-box;
  display: grid;
  grid-template-columns: minmax(200px, 236px) minmax(0, 1fr);
  gap: var(--lc-space-5);
  align-items: start;
  padding: 0 0 var(--lc-space-6);
}

.me-hub-side {
  grid-column: 1;
  grid-row: 1;
  align-self: start;
  position: sticky;
  top: 88px;
  display: grid;
  gap: var(--lc-space-4);
  padding: var(--lc-space-4);
  border: 1px solid var(--lc-border);
  border-radius: 18px;
  background: var(--lc-surface);
  box-shadow: var(--lc-shadow-sm);
}

.side-kicker {
  margin: 0;
  color: var(--lc-subtle);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.side-nav {
  display: grid;
  gap: 4px;
}

.side-nav-item {
  display: grid;
  grid-template-columns: 28px minmax(0, 1fr) auto;
  align-items: center;
  gap: 8px;
  min-height: 42px;
  padding: 0 12px;
  border-radius: 12px;
  color: var(--lc-muted);
  font-size: 14px;
  font-weight: 700;
  text-decoration: none;
  transition: background 0.15s ease, color 0.15s ease;
}

.side-nav-item:hover {
  background: var(--lc-soft);
  color: var(--lc-text);
}

.side-nav-item.active {
  background: var(--hub-accent-soft);
  color: var(--hub-accent);
}

.side-nav-icon {
  font-size: 16px;
  line-height: 1;
  text-align: center;
}

.side-nav-badge {
  min-width: 18px;
  padding: 2px 6px;
  border-radius: 999px;
  background: #ef4444;
  color: #fff;
  font-size: 11px;
  font-style: normal;
  font-weight: 800;
  line-height: 1.2;
  text-align: center;
}

.side-progress {
  display: grid;
  gap: 8px;
  padding: 12px;
  border-radius: 14px;
  background: var(--lc-bg);
}

.side-progress-head {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  color: var(--lc-text);
  font-size: 13px;
}

.side-progress-bar {
  height: 8px;
  border-radius: 999px;
  background: var(--lc-border);
  overflow: hidden;
}

.side-progress-bar i {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, var(--hub-accent), color-mix(in srgb, var(--hub-accent) 70%, #fff));
}

.side-progress-btn {
  border: 0;
  background: transparent;
  color: var(--hub-accent);
  font-size: 13px;
  font-weight: 800;
  text-align: left;
  cursor: pointer;
  padding: 0;
}

.side-foot {
  display: grid;
  gap: 6px;
  padding-top: 4px;
  border-top: 1px dashed var(--lc-border);
}

.side-foot a {
  color: var(--lc-subtle);
  font-size: 12px;
  font-weight: 700;
  text-decoration: none;
}

.side-foot a:hover {
  color: var(--hub-accent);
}

.me-hub-main {
  grid-column: 2;
  grid-row: 1;
  display: grid;
  gap: var(--lc-space-4);
  min-width: 0;
}

.profile-hero {
  padding: var(--lc-space-6);
  border: 1px solid var(--hub-accent-border);
  background:
    radial-gradient(circle at top right, color-mix(in srgb, var(--hub-accent) 10%, transparent), transparent 42%),
    var(--lc-surface);
}

.profile-hero-top {
  display: flex;
  justify-content: space-between;
  gap: var(--lc-space-5);
  align-items: center;
}

.profile-identity {
  display: flex;
  gap: var(--lc-space-4);
  align-items: center;
  min-width: 0;
}

.avatar-ring {
  position: relative;
  flex: 0 0 auto;
}

.avatar-img,
.avatar-fb {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  border: 3px solid color-mix(in srgb, var(--hub-accent) 25%, #fff);
  object-fit: cover;
}

.avatar-fb {
  display: grid;
  place-items: center;
  background: var(--hub-accent-soft);
  color: var(--hub-accent);
  font-size: 30px;
  font-weight: 900;
}

.lv-badge {
  position: absolute;
  right: -4px;
  bottom: -2px;
  padding: 3px 8px;
  border-radius: 999px;
  background: var(--hub-accent);
  color: #fff;
  font-size: 11px;
  font-weight: 900;
}

.profile-copy {
  min-width: 0;
}

.name-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.name-row h1 {
  margin: 0;
  font-size: 28px;
  line-height: 1.2;
}

.pill {
  display: inline-flex;
  align-items: center;
  min-height: 26px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 800;
}

.pill-violet {
  background: var(--hub-accent-soft);
  color: var(--hub-accent);
}

.pill-soft {
  background: var(--lc-soft);
  color: var(--lc-muted);
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 10px;
}

.bio {
  margin: 10px 0 0;
  color: var(--lc-muted);
  font-size: 14px;
  line-height: 1.6;
}

.completion-ring {
  position: relative;
  width: 108px;
  height: 108px;
  flex: 0 0 auto;
}

.ring-svg {
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
}

.ring-track,
.ring-fill {
  fill: none;
  stroke-width: 8;
}

.ring-track {
  stroke: var(--lc-border);
}

.ring-fill {
  stroke: var(--hub-accent);
  stroke-linecap: round;
  stroke-dasharray: 226.2;
  transition: stroke-dashoffset 0.35s ease;
}

.ring-center {
  position: absolute;
  inset: 0;
  display: grid;
  place-content: center;
  text-align: center;
}

.ring-center strong {
  font-size: 22px;
  color: var(--hub-accent);
}

.ring-center small {
  color: var(--lc-subtle);
  font-size: 11px;
}

.profile-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: var(--lc-space-5);
}

.hub-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 40px;
  padding: 0 18px;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 800;
  text-decoration: none;
  cursor: pointer;
  border: 1px solid transparent;
}

.hub-btn-primary {
  border-color: var(--hub-accent);
  background: var(--hub-accent);
  color: #fff;
}

.hub-btn-ghost {
  border-color: var(--hub-accent-border);
  background: #fff;
  color: var(--hub-accent);
}

.hub-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(0, 0.95fr);
  gap: var(--lc-space-4);
}

.hub-col {
  display: grid;
  gap: var(--lc-space-4);
  align-content: start;
}

.hub-card {
  padding: var(--lc-space-5);
}

.hub-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: var(--lc-space-4);
}

.hub-card-head h2 {
  margin: 0;
  font-size: 18px;
}

.text-link {
  border: 0;
  background: transparent;
  color: var(--hub-accent);
  font-size: 13px;
  font-weight: 800;
  text-decoration: none;
  cursor: pointer;
  padding: 0;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 16px;
  margin: 0;
}

.info-grid div {
  display: grid;
  gap: 4px;
}

.info-grid dt {
  color: var(--lc-subtle);
  font-size: 12px;
  font-weight: 700;
}

.info-grid dd {
  margin: 0;
  color: var(--lc-text);
  font-size: 14px;
  font-weight: 700;
}

.verify-list,
.security-list,
.feed-list {
  margin: 0;
  padding: 0;
  list-style: none;
}

.verify-list {
  display: grid;
  gap: 10px;
}

.verify-list li {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 12px;
  background: var(--lc-bg);
  font-size: 14px;
  font-weight: 700;
}

.verify-list em {
  font-style: normal;
  font-size: 12px;
  font-weight: 800;
}

.verify-list em.ok {
  color: var(--lc-green);
}

.verify-list em.pending {
  color: var(--lc-subtle);
}

.security-list {
  display: grid;
  gap: 12px;
}

.security-list li {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--lc-border);
}

.security-list li:last-child {
  padding-bottom: 0;
  border-bottom: 0;
}

.security-list strong {
  display: block;
  font-size: 14px;
}

.security-list p {
  margin: 4px 0 0;
  color: var(--lc-subtle);
  font-size: 12px;
}

.feed-list {
  display: grid;
  gap: 12px;
}

.feed-list li {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr);
  gap: 10px;
  align-items: start;
}

.feed-thumb,
.activity-thumb {
  display: grid;
  place-items: center;
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: var(--hub-accent-soft);
  font-size: 18px;
}

.feed-body p {
  margin: 0;
  color: var(--lc-text);
  font-size: 14px;
  line-height: 1.5;
}

.feed-body span {
  display: block;
  margin-top: 4px;
  color: var(--lc-subtle);
  font-size: 12px;
}

.activity-spotlight {
  display: grid;
  grid-template-columns: 56px minmax(0, 1fr);
  gap: 12px;
  align-items: start;
}

.activity-thumb {
  width: 56px;
  height: 56px;
  border-radius: 14px;
}

.activity-body h3 {
  margin: 0;
  font-size: 15px;
  line-height: 1.4;
}

.activity-body p {
  margin: 6px 0 0;
  color: var(--lc-subtle);
  font-size: 12px;
}

.activity-status {
  display: inline-flex;
  margin-top: 8px;
  padding: 4px 10px;
  border-radius: 999px;
  background: var(--hub-accent-soft);
  color: var(--hub-accent);
  font-size: 12px;
  font-weight: 800;
}

.msg-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.msg-tile {
  position: relative;
  display: grid;
  gap: 6px;
  justify-items: center;
  padding: 14px 8px;
  border-radius: 14px;
  background: var(--lc-bg);
  color: var(--lc-text);
  font-size: 12px;
  font-weight: 800;
  text-decoration: none;
}

.msg-icon {
  font-size: 20px;
}

.msg-tile em {
  position: absolute;
  top: 8px;
  right: 8px;
  min-width: 16px;
  padding: 1px 5px;
  border-radius: 999px;
  background: #ef4444;
  color: #fff;
  font-size: 10px;
  font-style: normal;
  font-weight: 800;
  line-height: 1.3;
  text-align: center;
}

.shortcut-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.shortcut-item {
  display: grid;
  gap: 8px;
  justify-items: center;
  padding: 14px 8px;
  border-radius: 14px;
  border: 1px solid var(--lc-border);
  background: var(--lc-surface);
  color: var(--lc-text);
  font-size: 12px;
  font-weight: 800;
  text-decoration: none;
}

.shortcut-icon {
  font-size: 22px;
}

.mini-empty {
  margin: 0;
  color: var(--lc-subtle);
  font-size: 13px;
  line-height: 1.6;
}

.mini-empty a {
  color: var(--hub-accent);
  font-weight: 800;
  text-decoration: none;
}

@media (min-width: 1400px) {
  .me-hub {
    grid-template-columns: 236px minmax(0, 1fr);
    gap: var(--lc-space-6);
  }

  .hub-grid {
    grid-template-columns: minmax(0, 1.05fr) minmax(0, 0.95fr);
    gap: var(--lc-space-5);
  }
}

@media (max-width: 1180px) {
  .hub-grid {
    grid-template-columns: 1fr;
  }

  .msg-grid,
  .shortcut-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 1023px) {
  .me-hub {
    display: none;
  }
}
</style>
