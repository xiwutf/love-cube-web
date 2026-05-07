<template>
  <section class="admin-page dashboard-page">
    <section class="dash-section">
      <div class="section-title-row">
        <h2>今日概览</h2>
        <div class="date-pill">
          <span>{{ todayDateText }}</span>
          <span>{{ todayWeekText }}</span>
          <span class="date-icon">日</span>
        </div>
      </div>

      <div class="overview-grid">
        <article v-for="item in overviewCards" :key="item.key" class="overview-card">
          <div class="stat-icon" :class="`stat-${item.tone}`">{{ item.icon }}</div>
          <div class="stat-content">
            <p>{{ item.label }}</p>
            <strong>{{ formatNumber(item.value) }}</strong>
                <span>{{ item.meta || '较昨日' }} <em :class="item.delta >= 0 ? 'up' : 'down'">{{ formatDelta(item.delta) }}</em></span>
          </div>
          <svg class="sparkline" viewBox="0 0 92 36" aria-hidden="true">
            <polyline :class="`spark-${item.tone}`" :points="item.spark"></polyline>
          </svg>
        </article>
      </div>
    </section>

    <section class="dash-section">
      <div class="section-title-row">
        <h2>待办工作台</h2>
      </div>

      <div class="todo-grid">
        <article v-for="item in todoItems" :key="item.key" class="todo-card">
          <router-link class="todo-more" :to="item.to" aria-label="进入处理">›</router-link>
          <div class="todo-icon" :class="`todo-${item.tone}`">{{ item.icon }}</div>
          <div>
            <h3>{{ item.title }}</h3>
            <strong>{{ formatNumber(item.count) }}</strong>
            <p>{{ item.desc }}</p>
          </div>
          <router-link class="todo-action" :to="item.to">立即处理</router-link>
        </article>
      </div>
    </section>

    <section class="dash-section">
      <div class="section-title-row">
        <h2>运营数据</h2>
      </div>

      <div class="ops-grid">
        <article class="ops-card trend-card">
          <h3>用户增长趋势（最近7天）</h3>
          <div class="chart-legend">
            <span><i class="blue-dot"></i>新增用户</span>
            <span><i class="green-dot"></i>活跃用户</span>
          </div>
          <svg class="line-chart" viewBox="0 0 420 150" aria-label="用户增长趋势">
            <g class="grid-lines">
              <line x1="16" y1="24" x2="404" y2="24"></line>
              <line x1="16" y1="64" x2="404" y2="64"></line>
              <line x1="16" y1="104" x2="404" y2="104"></line>
            </g>
            <polyline class="line-blue" :points="userLinePoints"></polyline>
            <polyline class="line-green" :points="activeLinePoints"></polyline>
          </svg>
          <div class="axis-labels">
            <span v-for="item in sevenDayLabels" :key="item">{{ item }}</span>
          </div>
        </article>

        <article class="ops-card trend-card">
          <h3>内容发布趋势（最近7天）</h3>
          <div class="chart-legend">
            <span><i class="blue-dot"></i>动态数</span>
            <span><i class="green-dot"></i>评论数</span>
          </div>
          <svg class="line-chart" viewBox="0 0 420 150" aria-label="内容发布趋势">
            <g class="grid-lines">
              <line x1="16" y1="24" x2="404" y2="24"></line>
              <line x1="16" y1="64" x2="404" y2="64"></line>
              <line x1="16" y1="104" x2="404" y2="104"></line>
            </g>
            <polyline class="line-blue" :points="contentLinePoints"></polyline>
            <polyline class="line-green" :points="commentLinePoints"></polyline>
          </svg>
          <div class="axis-labels">
            <span v-for="item in sevenDayLabels" :key="item">{{ item }}</span>
          </div>
        </article>

        <article class="ops-card ratio-card">
          <h3>模块活跃占比</h3>
          <div class="donut-wrap">
            <div class="donut" :style="donutStyle"></div>
            <div class="ratio-list">
              <div v-for="item in activityRatio" :key="item.key" class="ratio-row">
                <span><i :class="`ratio-dot ${item.key}`"></i>{{ item.label }}</span>
                <strong>{{ item.value }}%</strong>
              </div>
            </div>
          </div>
        </article>

        <article class="ops-card completion-card">
          <h3>找对象资料完善率</h3>
          <div class="progress-ring" :style="profileRingStyle">
          <div class="progress-ring__inner">{{ profileCompletionRate }}%</div>
          </div>
          <p>已完善 {{ formatNumber(profileCompletedCount) }} / {{ formatNumber(profileTotalCount) }}</p>
        </article>
      </div>
    </section>

    <section class="dash-section">
      <div class="section-title-row">
        <h2>快捷入口</h2>
      </div>

      <div class="shortcut-grid">
        <article v-for="group in shortcutGroups" :key="group.title" class="shortcut-group">
          <h3>{{ group.title }}</h3>
          <div class="shortcut-items">
            <template v-for="item in group.items" :key="item.label">
              <router-link v-if="item.to" class="shortcut-item" :to="item.to">
                <span :class="`shortcut-icon ${item.tone}`">{{ item.icon }}</span>
                <strong>{{ item.label }}</strong>
              </router-link>
              <span v-else class="shortcut-item shortcut-item--disabled">
                <span :class="`shortcut-icon ${item.tone}`">{{ item.icon }}</span>
                <strong>{{ item.label }}</strong>
              </span>
            </template>
          </div>
        </article>
      </div>
    </section>

    <section class="dash-section">
      <div class="section-title-row">
        <h2>最近动态</h2>
      </div>

      <div class="recent-grid">
        <article v-for="column in recentColumns" :key="column.key" class="recent-card">
          <header>
            <h3>{{ column.title }}</h3>
            <router-link v-if="column.to" :to="column.to">更多 ›</router-link>
            <span v-else>更多 ›</span>
          </header>
          <ul>
            <li v-for="item in column.items" :key="`${column.key}-${item.title}`">
              <span class="recent-avatar" :class="column.key">{{ item.avatar }}</span>
              <div>
                <strong>{{ item.title }}</strong>
                <p>{{ item.desc }}</p>
              </div>
              <time>{{ item.time }}</time>
            </li>
          </ul>
        </article>
      </div>
    </section>

    <p v-if="error" class="dashboard-error">{{ error }}</p>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getAdminStats } from '@/api/adminContent.js'

const emptyDashboardData = {
  userData: {
    todayNewUsers: 0,
    sevenDayNewUsers: 0,
    totalUsers: 0
  },
  contentData: {
    todayNewDynamics: 0,
    pendingContent: 0
  },
  fellowshipData: {
    pendingDynamics: 0,
    pendingReports: 0,
    pendingVerifications: 0
  },
  governanceData: {
    pendingReports: 0
  },
  feedbackData: {
    pendingFeedbacks: 0
  },
  localResourceData: {
    pendingLocalResources: 0
  },
  helpAndShareData: {
    helpRequestsPending: 0
  },
  growthData: {
    fellowshipProfilesTotal: 0,
    fellowshipProfilesBasicFilled: 0
  },
  trends: {
    labels: [],
    newUsers: [],
    activeUsers: [],
    dynamics: [],
    comments: [],
    contentPublish: []
  },
  activityRatio: {
    platform: 0,
    dating: 0,
    group: 0
  },
  recent: {
    users: [],
    contents: [],
    reports: [],
    feedbacks: [],
    notices: []
  }
}

const dashboardData = ref(emptyDashboardData)
const error = ref('')
const loading = ref(false)

function pick(groupKey, key, fallback = 0) {
  return dashboardData.value?.[groupKey]?.[key] ?? dashboardData.value?.[key] ?? fallback
}

function formatNumber(value) {
  return (Number(value) || 0).toLocaleString('zh-CN')
}

function formatDelta(value) {
  const n = Number(value) || 0
  return `${n > 0 ? '+' : ''}${n.toFixed(1)}%`
}

function toNumberArray(value) {
  return Array.isArray(value) ? value.map(item => Number(item) || 0) : []
}

function trendDelta(values) {
  const rows = toNumberArray(values)
  if (rows.length < 2) return 0
  const current = rows[rows.length - 1]
  const previous = rows[rows.length - 2]
  if (!previous) return current > 0 ? 100 : 0
  return ((current - previous) / previous) * 100
}

function sparkPoints(values) {
  const rows = toNumberArray(values)
  if (!rows.length) return '4,30 90,30'
  const max = Math.max(...rows, 1)
  const min = Math.min(...rows, 0)
  const range = Math.max(max - min, 1)
  return rows
    .map((value, index) => {
      const x = 4 + (index * 86) / Math.max(rows.length - 1, 1)
      const y = 32 - ((value - min) / range) * 26
      return `${x.toFixed(1)},${y.toFixed(1)}`
    })
    .join(' ')
}

function linePoints(values) {
  const rows = toNumberArray(values)
  if (!rows.length) return ''
  const max = Math.max(...rows, 1)
  const min = Math.min(...rows, 0)
  const range = Math.max(max - min, 1)
  return rows
    .map((value, index) => {
      const x = 26 + index * 60
      const y = 126 - ((value - min) / range) * 96
      return `${x},${y.toFixed(1)}`
    })
    .join(' ')
}

function mergeDashboardData(apiData = {}) {
  return {
    ...emptyDashboardData,
    ...apiData,
    userData: { ...emptyDashboardData.userData, ...(apiData.userData || {}) },
    contentData: { ...emptyDashboardData.contentData, ...(apiData.contentData || {}) },
    fellowshipData: { ...emptyDashboardData.fellowshipData, ...(apiData.fellowshipData || {}) },
    governanceData: { ...emptyDashboardData.governanceData, ...(apiData.governanceData || {}) },
    feedbackData: { ...emptyDashboardData.feedbackData, ...(apiData.feedbackData || {}) },
    localResourceData: { ...emptyDashboardData.localResourceData, ...(apiData.localResourceData || {}) },
    helpAndShareData: { ...emptyDashboardData.helpAndShareData, ...(apiData.helpAndShareData || {}) },
    growthData: { ...emptyDashboardData.growthData, ...(apiData.growthData || {}) },
    trends: { ...emptyDashboardData.trends, ...(apiData.trends || {}) },
    activityRatio: { ...emptyDashboardData.activityRatio, ...(apiData.activityRatio || {}) },
    recent: { ...emptyDashboardData.recent, ...(apiData.recent || {}) }
  }
}

const todayDateText = computed(() =>
  new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' }).replaceAll('/', '-')
)
const todayWeekText = computed(() =>
  new Date().toLocaleDateString('zh-CN', { weekday: 'long' })
)
const trends = computed(() => dashboardData.value.trends || emptyDashboardData.trends)
const sevenDayLabels = computed(() => {
  if (Array.isArray(trends.value.labels) && trends.value.labels.length) return trends.value.labels
  return toNumberArray(trends.value.newUsers).map((_, index) => `D-${6 - index}`)
})
const profileCompletedCount = computed(() => pick('growthData', 'fellowshipProfilesBasicFilled'))
const profileTotalCount = computed(() => pick('growthData', 'fellowshipProfilesTotal'))
const profileCompletionRate = computed(() => {
  if (!profileTotalCount.value) return 0
  return Math.round((profileCompletedCount.value / profileTotalCount.value) * 1000) / 10
})

const overviewCards = computed(() => [
  {
    key: 'users',
    label: '今日新增用户',
    value: pick('userData', 'todayNewUsers'),
    delta: trendDelta(trends.value.newUsers),
    tone: 'blue',
    icon: '人',
    spark: sparkPoints(trends.value.newUsers)
  },
  {
    key: 'dynamics',
    label: '今日新增动态',
    value: pick('contentData', 'todayNewDynamics'),
    delta: trendDelta(trends.value.dynamics),
    tone: 'green',
    icon: '文',
    spark: sparkPoints(trends.value.dynamics)
  },
  {
    key: 'review',
    label: '待审核内容',
    value: pick('contentData', 'pendingContent'),
    delta: trendDelta(trends.value.contentPublish),
    tone: 'orange',
    icon: '审',
    spark: sparkPoints(trends.value.contentPublish)
  },
  {
    key: 'risk',
    label: '待处理举报',
    value: pick('governanceData', 'pendingReports'),
    delta: trendDelta(trends.value.comments) * -1,
    tone: 'red',
    icon: '盾',
    spark: sparkPoints(trends.value.comments),
    meta: '较昨日'
  }
])

const todoItems = computed(() => [
  {
    key: 'dynamics',
    title: '待审核动态',
    count: pick('fellowshipData', 'pendingDynamics'),
    desc: '待审核用户发布的动态',
    tone: 'red',
    icon: '动',
    to: '/admin/fellowship-dynamics'
  },
  {
    key: 'profiles',
    title: '待审核用户资料',
    count: pick('fellowshipData', 'pendingVerifications'),
    desc: '待审核用户资料信息',
    tone: 'blue',
    icon: '资',
    to: '/admin/verifications'
  },
  {
    key: 'reports',
    title: '待处理举报',
    count: pick('governanceData', 'pendingReports'),
    desc: '用户举报待处理',
    tone: 'green',
    icon: '举',
    to: '/admin/reports'
  },
  {
    key: 'resources',
    title: '待审核本地资源',
    count: pick('localResourceData', 'pendingLocalResources'),
    desc: '本地资源待审核',
    tone: 'orange',
    icon: '源',
    to: '/admin/local-resources'
  },
  {
    key: 'helps',
    title: '待处理互助内容',
    count: pick('helpAndShareData', 'helpRequestsPending'),
    desc: '互助内容待审核',
    tone: 'red',
    icon: '助',
    to: '/admin/help-requests'
  }
])

const userLinePoints = computed(() => linePoints(trends.value.newUsers))
const activeLinePoints = computed(() => linePoints(trends.value.activeUsers))
const contentLinePoints = computed(() => linePoints(trends.value.dynamics))
const commentLinePoints = computed(() => linePoints(trends.value.comments))

const activityRatio = computed(() => {
  const source = dashboardData.value.activityRatio || emptyDashboardData.activityRatio
  return [
    { key: 'platform', label: '平台', value: Number(source.platform) || 0 },
    { key: 'dating', label: '找对象', value: Number(source.dating) || 0 },
    { key: 'group', label: '团体', value: Number(source.group) || 0 }
  ]
})
const donutStyle = computed(() => {
  const platform = activityRatio.value[0]?.value || 0
  const dating = activityRatio.value[1]?.value || 0
  const group = activityRatio.value[2]?.value || 0
  const platformEnd = platform
  const datingEnd = platform + dating
  return {
    background: `radial-gradient(circle, var(--lc-surface) 0 52%, transparent 53%),
      conic-gradient(var(--lc-blue) 0 ${platformEnd}%, var(--lc-emerald) ${platformEnd}% ${datingEnd}%, var(--lc-orange) ${datingEnd}% ${Math.max(100, platform + dating + group)}%)`
  }
})
const profileRingStyle = computed(() => ({
  background: `radial-gradient(circle, var(--lc-surface) 0 56%, transparent 57%),
    conic-gradient(var(--lc-blue) 0 ${profileCompletionRate.value}%, var(--lc-soft) ${profileCompletionRate.value}% 100%)`
}))

const shortcutGroups = [
  {
    title: '平台运营',
    items: [
      { label: '平台动态', icon: '动', tone: 'blue', to: '/admin/positive-shares' },
      { label: '公告管理', icon: '告', tone: 'blue', to: '/admin/announcements' },
      { label: '本地资源', icon: '源', tone: 'green', to: '/admin/local-resources' },
      { label: '互助广场', icon: '助', tone: 'blue', to: '/admin/help-requests' }
    ]
  },
  {
    title: '找对象运营',
    items: [
      { label: '资料审核', icon: '资', tone: 'green', to: '/admin/verifications' },
      { label: '动态审核', icon: '审', tone: 'green', to: '/admin/fellowship-dynamics' },
      { label: '匹配数据', icon: '配', tone: 'purple', to: '/admin/analytics' },
      { label: '举报处理', icon: '举', tone: 'red', to: '/admin/reports' }
    ]
  },
  {
    title: '团体运营',
    items: [
      { label: '团体管理', icon: '团', tone: 'orange', to: '/admin/platform/groups' },
      { label: '打卡管理', icon: '卡', tone: 'green' },
      { label: '排行榜', icon: '榜', tone: 'orange' },
      { label: '内容审核', icon: '审', tone: 'orange', to: '/admin/my-groups' }
    ]
  },
  {
    title: '系统管理',
    items: [
      { label: '用户管理', icon: '用', tone: 'blue', to: '/admin/users' },
      { label: '角色权限', icon: '权', tone: 'blue', to: '/admin/users' },
      { label: '通知管理', icon: '通', tone: 'blue' },
      { label: '系统配置', icon: '设', tone: 'blue', to: '/admin/modules' }
    ]
  }
]

const recentColumns = computed(() => {
  const recent = dashboardData.value.recent || emptyDashboardData.recent
  return [
    { key: 'users', title: '最新注册用户', to: '/admin/users', items: recent.users || [] },
    { key: 'contents', title: '最新发布内容', to: '/admin/fellowship-dynamics', items: recent.contents || [] },
    { key: 'reports', title: '最新举报', to: '/admin/reports', items: recent.reports || [] },
    { key: 'feedbacks', title: '最新问卷', to: '/admin/feedbacks', items: recent.feedbacks || [] },
    { key: 'notices', title: '系统提醒', items: recent.notices || [] }
  ]
})

async function load(forceRefresh = false) {
  loading.value = true
  error.value = ''
  try {
    const data = await getAdminStats(forceRefresh)
    dashboardData.value = mergeDashboardData(data || {})
  } catch (e) {
    dashboardData.value = emptyDashboardData
    error.value = `真实统计接口暂不可用：${e.message || '加载失败'}`
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  load(false)
})
</script>

<style scoped>
.dashboard-page {
  display: grid;
  gap: 14px;
  color: var(--lc-text);
}

.dash-section {
  border: 1px solid var(--lc-border);
  border-radius: 8px;
  background: var(--lc-surface);
  padding: 16px 18px;
  box-shadow: var(--lc-shadow-sm);
}

.section-title-row {
  min-height: 30px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.section-title-row h2 {
  margin: 0;
  color: var(--lc-text);
  font-size: 16px;
  font-weight: 800;
}

.date-pill {
  min-height: 34px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: 1px solid var(--lc-border);
  border-radius: 6px;
  padding: 0 10px;
  background: var(--lc-bg);
  color: var(--lc-muted);
  font-size: 13px;
  font-weight: 700;
}

.date-icon {
  color: var(--lc-text);
  font-weight: 900;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.overview-card {
  min-height: 98px;
  border: 1px solid var(--lc-border);
  border-radius: 8px;
  background: var(--lc-surface);
  padding: 14px;
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) 86px;
  align-items: center;
  gap: 12px;
  box-shadow: 0 8px 20px rgb(15 23 42 / 4%);
}

.stat-icon {
  width: 58px;
  height: 58px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  font-weight: 900;
}

.stat-blue {
  color: var(--lc-blue);
  background: var(--lc-blue-light);
}

.stat-green {
  color: var(--lc-emerald);
  background: var(--lc-emerald-light);
}

.stat-orange {
  color: var(--lc-orange);
  background: var(--lc-orange-light);
}

.stat-red {
  color: var(--lc-rose);
  background: var(--lc-red-light);
}

.stat-content {
  min-width: 0;
}

.stat-content p,
.stat-content span {
  margin: 0;
  color: var(--lc-muted);
  font-size: 13px;
}

.stat-content strong {
  display: block;
  margin: 6px 0 8px;
  color: var(--lc-text);
  font-size: 28px;
  line-height: 1;
  font-variant-numeric: tabular-nums;
}

.stat-content em,
.completion-card em {
  font-style: normal;
  font-weight: 800;
}

.up {
  color: var(--lc-emerald);
}

.down {
  color: var(--lc-rose);
}

.sparkline {
  width: 86px;
  height: 36px;
}

.sparkline polyline {
  fill: none;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.spark-blue {
  stroke: var(--lc-blue);
}

.spark-green {
  stroke: var(--lc-emerald);
}

.spark-orange {
  stroke: var(--lc-orange);
}

.spark-red {
  stroke: var(--lc-rose);
}

.todo-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 14px;
}

.todo-card {
  position: relative;
  min-height: 124px;
  border: 1px solid var(--lc-border);
  border-radius: 8px;
  background: var(--lc-surface);
  padding: 16px 14px 12px;
  display: grid;
  grid-template-columns: auto 1fr;
  grid-template-rows: 1fr auto;
  gap: 10px 12px;
}

.todo-more {
  position: absolute;
  top: 12px;
  right: 12px;
  color: var(--lc-subtle);
  text-decoration: none;
  font-size: 22px;
  line-height: 1;
}

.todo-icon {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  font-size: 12px;
  font-weight: 900;
}

.todo-red {
  color: var(--lc-rose);
  background: var(--lc-red-light);
}

.todo-blue {
  color: var(--lc-blue);
  background: var(--lc-blue-light);
}

.todo-green {
  color: var(--lc-emerald);
  background: var(--lc-emerald-light);
}

.todo-orange {
  color: var(--lc-orange);
  background: var(--lc-orange-light);
}

.todo-card h3 {
  margin: 0 0 7px;
  font-size: 13px;
  color: var(--lc-text);
}

.todo-card strong {
  display: block;
  color: var(--lc-rose);
  font-size: 20px;
  font-variant-numeric: tabular-nums;
}

.todo-card p {
  margin: 7px 0 0;
  color: var(--lc-muted);
  font-size: 12px;
}

.todo-action {
  grid-column: 2;
  width: fit-content;
  min-height: 28px;
  display: inline-flex;
  align-items: center;
  border: 1px solid var(--lc-pink-border);
  border-radius: 5px;
  padding: 0 14px;
  background: var(--lc-red-light);
  color: var(--lc-rose);
  font-size: 12px;
  font-weight: 800;
  text-decoration: none;
}

.ops-grid {
  display: grid;
  grid-template-columns: 1.35fr 1.35fr 0.86fr 0.7fr;
  gap: 14px;
}

.ops-card {
  min-height: 156px;
  border: 1px solid var(--lc-border);
  border-radius: 8px;
  background: var(--lc-surface);
  padding: 12px 14px;
}

.ops-card h3 {
  margin: 0;
  color: var(--lc-text);
  font-size: 13px;
  font-weight: 800;
}

.chart-legend {
  display: flex;
  justify-content: center;
  gap: 28px;
  margin-top: 10px;
  color: var(--lc-muted);
  font-size: 11px;
}

.chart-legend span,
.ratio-row span {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.blue-dot,
.green-dot,
.ratio-dot {
  width: 7px;
  height: 7px;
  border-radius: 99px;
  display: inline-block;
}

.blue-dot,
.ratio-dot.platform {
  background: var(--lc-blue);
}

.green-dot,
.ratio-dot.dating {
  background: var(--lc-emerald);
}

.ratio-dot.group {
  background: var(--lc-orange);
}

.line-chart {
  width: 100%;
  height: 116px;
  margin-top: 2px;
}

.grid-lines line {
  stroke: var(--lc-border);
  stroke-width: 1;
}

.line-blue,
.line-green {
  fill: none;
  stroke-width: 3;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.line-blue {
  stroke: var(--lc-blue);
}

.line-green {
  stroke: var(--lc-emerald);
}

.axis-labels {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  color: var(--lc-subtle);
  font-size: 10px;
  text-align: center;
}

.donut-wrap {
  display: grid;
  grid-template-columns: 120px 1fr;
  align-items: center;
  gap: 14px;
  min-height: 126px;
}

.donut {
  width: 112px;
  height: 112px;
  border-radius: 50%;
  background:
    radial-gradient(circle, var(--lc-surface) 0 52%, transparent 53%),
    conic-gradient(var(--lc-blue) 0 38.6%, var(--lc-emerald) 38.6% 72.8%, var(--lc-orange) 72.8% 100%);
}

.ratio-list {
  display: grid;
  gap: 12px;
}

.ratio-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  color: var(--lc-muted);
  font-size: 13px;
}

.ratio-row strong {
  color: var(--lc-slate);
  font-variant-numeric: tabular-nums;
}

.completion-card {
  text-align: center;
}

.progress-ring {
  width: 108px;
  height: 108px;
  margin: 14px auto 8px;
  border-radius: 50%;
  background:
    radial-gradient(circle, var(--lc-surface) 0 56%, transparent 57%),
    conic-gradient(var(--lc-blue) 0 68.6%, var(--lc-soft) 68.6% 100%);
  display: grid;
  place-items: center;
}

.progress-ring__inner {
  color: var(--lc-text);
  font-size: 22px;
  font-weight: 900;
}

.completion-card p {
  margin: 0;
  color: var(--lc-muted);
  font-size: 13px;
}

.completion-card em {
  color: var(--lc-emerald);
}

.shortcut-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.shortcut-group {
  border: 1px solid var(--lc-border);
  border-radius: 8px;
  background: var(--lc-surface);
  overflow: hidden;
}

.shortcut-group h3 {
  margin: 0;
  padding: 10px 12px;
  background: var(--lc-bg);
  color: var(--lc-text);
  font-size: 13px;
  font-weight: 900;
}

.shortcut-items {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
  padding: 14px 10px 12px;
}

.shortcut-item {
  min-width: 0;
  display: grid;
  justify-items: center;
  gap: 8px;
  color: var(--lc-text);
  text-decoration: none;
  font-size: 12px;
  font-weight: 700;
}

.shortcut-icon {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  display: grid;
  place-items: center;
  font-size: 12px;
  font-weight: 900;
}

.shortcut-icon.blue {
  color: var(--lc-blue);
  background: var(--lc-blue-light);
}

.shortcut-icon.green {
  color: var(--lc-emerald);
  background: var(--lc-emerald-light);
}

.shortcut-icon.orange {
  color: var(--lc-orange);
  background: var(--lc-orange-light);
}

.shortcut-icon.red {
  color: var(--lc-rose);
  background: var(--lc-red-light);
}

.shortcut-icon.purple {
  color: var(--lc-purple);
  background: var(--lc-indigo-light);
}

.shortcut-item--disabled {
  color: var(--lc-subtle);
  cursor: not-allowed;
}

.recent-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 14px;
}

.recent-card {
  border: 1px solid var(--lc-border);
  border-radius: 8px;
  background: var(--lc-surface);
  padding: 10px 12px;
}

.recent-card header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 10px;
}

.recent-card h3 {
  margin: 0;
  color: var(--lc-text);
  font-size: 13px;
  font-weight: 900;
}

.recent-card header a,
.recent-card header span {
  color: var(--lc-subtle);
  font-size: 12px;
  text-decoration: none;
}

.recent-card ul {
  margin: 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 10px;
}

.recent-card li {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 8px;
}

.recent-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: var(--lc-soft);
  color: var(--lc-muted);
  font-size: 11px;
  font-weight: 900;
}

.recent-avatar.reports,
.recent-avatar.feedbacks {
  border-radius: 8px;
}

.recent-avatar.reports {
  color: var(--lc-rose);
  background: var(--lc-red-light);
}

.recent-avatar.feedbacks {
  color: var(--lc-emerald);
  background: var(--lc-emerald-light);
}

.recent-avatar.notices {
  width: 10px;
  height: 10px;
  color: var(--lc-emerald);
  background: var(--lc-emerald);
}

.recent-card strong,
.recent-card p {
  display: block;
  min-width: 0;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recent-card strong {
  color: var(--lc-text);
  font-size: 12px;
  line-height: 1.4;
}

.recent-card p,
.recent-card time {
  color: var(--lc-muted);
  font-size: 11px;
}

.recent-card time {
  white-space: nowrap;
}

.dashboard-error {
  margin: 0;
  color: var(--lc-orange);
  font-size: 13px;
}

@media (max-width: 1500px) {
  .overview-grid,
  .shortcut-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .todo-grid,
  .recent-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .ops-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .section-title-row {
    align-items: flex-start;
    flex-direction: column;
  }

  .overview-grid,
  .todo-grid,
  .ops-grid,
  .shortcut-grid,
  .recent-grid {
    grid-template-columns: 1fr;
  }

  .overview-card {
    grid-template-columns: auto minmax(0, 1fr);
  }

  .sparkline {
    display: none;
  }

  .shortcut-items {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 520px) {
  .dash-section {
    padding: 14px 12px;
  }

  .overview-card {
    padding: 12px;
  }

  .donut-wrap {
    grid-template-columns: 1fr;
    justify-items: center;
  }

  .recent-card li {
    grid-template-columns: auto minmax(0, 1fr);
  }

  .recent-card time {
    grid-column: 2;
  }
}
</style>
