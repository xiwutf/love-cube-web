<template>
  <section class="admin-page operation-shell dashboard-page">
    <header class="operation-hero admin-operation-hero">
      <div class="hero-main">
        <div class="hero-kicker-row">
          <span class="status-badge info">Admin Operations</span>
          <div class="date-pill">
            <span>{{ todayDateText }}</span>
            <span>{{ todayWeekText }}</span>
          </div>
        </div>
        <h1>平台运营控制台</h1>
        <p>统一查看平台增长、内容供给、Space 运营和风险待办，优先处理影响用户体验的事项。</p>
      </div>
      <div class="hero-summary-grid">
        <article v-for="item in platformSummaryCards" :key="item.key" class="summary-card" :class="item.tone">
          <span>{{ item.label }}</span>
          <strong>{{ formatNumber(item.value) }}</strong>
          <small>{{ item.hint }}</small>
        </article>
      </div>
    </header>

    <section class="dashboard-section">
      <div class="section-title-row operation-section-head">
        <div>
          <p class="section-kicker">Core Metrics</p>
          <h2>核心指标</h2>
        </div>
      </div>

      <div class="metric-grid">
        <article v-for="item in coreMetricCards" :key="item.key" class="metric-card" :class="item.tone">
          <div class="metric-card-head">
            <span class="metric-label">{{ item.label }}</span>
            <span class="metric-chip">{{ item.badge }}</span>
          </div>
          <strong>{{ formatNumber(item.value) }}</strong>
          <p>{{ item.hint }}</p>
          <div class="metric-trend">
            <span>{{ item.meta || '趋势' }}</span>
            <em :class="item.delta >= 0 ? 'up' : 'down'">{{ formatDelta(item.delta) }}</em>
          </div>
        </article>
      </div>
    </section>

    <section class="dashboard-section risk-section">
      <div class="section-title-row operation-section-head">
        <div>
          <p class="section-kicker">Risks</p>
          <h2>风险待办</h2>
        </div>
        <span class="status-badge warning">{{ formatNumber(totalRiskCount) }} 项需关注</span>
      </div>

      <div class="risk-grid">
        <article v-for="item in riskCards" :key="item.key" class="risk-card" :class="item.tone">
          <div class="risk-card-head">
            <span class="risk-dot" :class="item.tone"></span>
            <h3>{{ item.title }}</h3>
            <strong>{{ formatNumber(item.count) }}</strong>
          </div>
          <p>{{ item.desc }}</p>
          <router-link class="card-link" :to="item.to">{{ item.actionLabel }}</router-link>
        </article>
      </div>
    </section>

    <section class="dashboard-section">
      <div class="section-title-row operation-section-head">
        <div>
          <p class="section-kicker">Next Actions</p>
          <h2>运营建议</h2>
        </div>
      </div>

      <div class="action-grid">
        <article v-for="item in actionCards" :key="item.key" class="action-card" :class="item.tone">
          <span class="status-badge" :class="item.badgeTone">{{ item.badge }}</span>
          <h3>{{ item.title }}</h3>
          <p>{{ item.desc }}</p>
          <router-link class="card-link" :to="item.to">{{ item.actionLabel }}</router-link>
        </article>
      </div>
    </section>

    <section class="dashboard-section">
      <div class="section-title-row operation-section-head">
        <div>
          <p class="section-kicker">Trends</p>
          <h2>运营数据</h2>
        </div>
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
      </div>
    </section>

    <section class="dashboard-section">
      <div class="section-title-row operation-section-head">
        <div>
          <p class="section-kicker">Recent Events</p>
          <h2>最近动态</h2>
        </div>
      </div>

      <div class="recent-grid">
        <article v-for="column in recentColumns" :key="column.key" class="recent-card">
          <header>
            <h3>{{ column.title }}</h3>
            <router-link v-if="column.to" :to="column.to">更多 →</router-link>
            <span v-else>更多 →</span>
          </header>
          <ul>
            <li v-for="item in column.items" :key="`${column.key}-${item.title}-${item.time}`">
              <router-link
                v-if="resolveRecentLink(column, item)"
                :to="resolveRecentLink(column, item)"
                class="recent-row recent-row--link"
              >
                <span class="recent-avatar" :class="column.key">{{ item.avatar }}</span>
                <div>
                  <strong>{{ item.title }}</strong>
                  <p>{{ item.desc }}</p>
                </div>
                <time>{{ item.time }}</time>
              </router-link>
              <div v-else class="recent-row">
                <span class="recent-avatar" :class="column.key">{{ item.avatar }}</span>
                <div>
                  <strong>{{ item.title }}</strong>
                  <p>{{ item.desc }}</p>
                </div>
                <time>{{ item.time }}</time>
              </div>
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
  engagementData: {
    dynamicsSevenDays: 0
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

function lastValue(values) {
  const rows = toNumberArray(values)
  return rows.length ? rows[rows.length - 1] : 0
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
    engagementData: { ...emptyDashboardData.engagementData, ...(apiData.engagementData || {}) },
    trends: { ...emptyDashboardData.trends, ...(apiData.trends || {}) },
    activityRatio: { ...emptyDashboardData.activityRatio, ...(apiData.activityRatio || {}) },
    recent: { ...emptyDashboardData.recent, ...(apiData.recent || {}) }
  }
}

const todayDateText = computed(() =>
  new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
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

const pendingReviewBreakdown = computed(() => ({
  dynamics7d: pick('engagementData', 'dynamicsSevenDays'),
  shares: pick('helpAndShareData', 'positiveSharesPending'),
  helps: pick('helpAndShareData', 'helpRequestsPending'),
  resources: pick('localResourceData', 'pendingLocalResources')
}))

const totalContentCount = computed(() =>
  Number(pick('contentData', 'totalAnnouncements')) +
  Number(pick('contentData', 'totalArticles')) +
  Number(pick('contentData', 'totalEvents')) +
  Number(pick('engagementData', 'dynamicsTotal')) +
  Number(pick('helpAndShareData', 'positiveSharesPublished')) +
  Number(pick('helpAndShareData', 'helpRequestsActive'))
)

const todayNewTotal = computed(() =>
  Number(pick('userData', 'todayNewUsers')) +
  Number(pick('contentData', 'todayNewDynamics')) +
  Number(pick('communityData', 'groupPostsToday')) +
  Number(pick('helpAndShareData', 'positiveSharesToday')) +
  Number(pick('helpAndShareData', 'helpRequestsToday'))
)

const activeSituationCount = computed(() =>
  lastValue(trends.value.activeUsers) ||
  Number(pick('engagementData', 'dynamicsSevenDays')) +
  Number(pick('communityData', 'activeGroups'))
)

const totalRiskCount = computed(() =>
  Number(pick('contentData', 'pendingContent')) +
  Number(pick('communityData', 'pendingGroupJoinRequests')) +
  Number(pick('governanceData', 'pendingReports')) +
  Number(pick('feedbackData', 'pendingFeedbacks'))
)

const platformSummaryCards = computed(() => [
  {
    key: 'users-total',
    label: '用户总量',
    value: pick('userData', 'totalUsers'),
    hint: `今日新增 ${formatNumber(pick('userData', 'todayNewUsers'))}`,
    tone: 'info'
  },
  {
    key: 'content-total',
    label: '内容总量',
    value: totalContentCount.value,
    hint: `近 7 日发布 ${formatNumber(pick('contentData', 'contentPublishedLast7d'))}`,
    tone: 'success'
  },
  {
    key: 'space-total',
    label: 'Space 数量',
    value: pick('communityData', 'totalGroups'),
    hint: `活跃 ${formatNumber(pick('communityData', 'activeGroups'))}`,
    tone: 'info'
  },
  {
    key: 'active',
    label: '活跃情况',
    value: activeSituationCount.value,
    hint: '活跃用户 / 活跃 Space',
    tone: 'success'
  },
  {
    key: 'today-new',
    label: '今日新增',
    value: todayNewTotal.value,
    hint: '用户、内容与互助合计',
    tone: 'info'
  },
  {
    key: 'risks',
    label: '风险事项',
    value: totalRiskCount.value,
    hint: '审核、举报、反馈待处理',
    tone: totalRiskCount.value > 0 ? 'warning' : 'success'
  }
])

const coreMetricCards = computed(() => [
  {
    key: 'users',
    label: '用户',
    badge: '增长',
    value: pick('userData', 'totalUsers'),
    hint: `近 7 日新增 ${formatNumber(pick('userData', 'sevenDayNewUsers'))}`,
    delta: trendDelta(trends.value.newUsers),
    tone: 'accent'
  },
  {
    key: 'content',
    label: '内容',
    badge: '供给',
    value: totalContentCount.value,
    hint: `今日新增动态 ${formatNumber(pick('contentData', 'todayNewDynamics'))}`,
    delta: trendDelta(trends.value.dynamics),
    tone: ''
  },
  {
    key: 'groups',
    label: '团体 / Space',
    badge: '运营',
    value: pick('communityData', 'totalGroups'),
    hint: `30 日新建 ${formatNumber(pick('communityData', 'groupsCreatedLast30d'))}`,
    delta: trendDelta(trends.value.contentPublish),
    tone: ''
  },
  {
    key: 'growth',
    label: '增长',
    badge: '转化',
    value: profileCompletionRate.value,
    hint: `资料完善 ${formatNumber(profileCompletedCount.value)} / ${formatNumber(profileTotalCount.value)}`,
    delta: profileCompletionRate.value,
    tone: profileCompletionRate.value >= 60 ? 'accent' : 'warn'
  }
])

const riskCards = computed(() => [
  {
    key: 'pending-content',
    title: '待审核内容',
    count: pick('contentData', 'pendingContent'),
    desc: `分享 ${formatNumber(pendingReviewBreakdown.value.shares)}，互助 ${formatNumber(pendingReviewBreakdown.value.helps)}，资源 ${formatNumber(pendingReviewBreakdown.value.resources)}`,
    tone: 'warning',
    to: '/admin/fellowship-dynamics',
    actionLabel: '处理内容'
  },
  {
    key: 'pending-members',
    title: '待审核成员',
    count: pick('communityData', 'pendingGroupJoinRequests'),
    desc: `今日新增申请 ${formatNumber(pick('communityData', 'groupJoinRequestsToday'))}`,
    tone: 'warning',
    to: '/admin/my-groups',
    actionLabel: '查看 Space'
  },
  {
    key: 'reports',
    title: '举报处理',
    count: pick('governanceData', 'pendingReports'),
    desc: `今日新增举报 ${formatNumber(pick('governanceData', 'todayReports'))}`,
    tone: 'danger',
    to: '/admin/reports',
    actionLabel: '处理举报'
  },
  {
    key: 'growth-risk',
    title: '异常增长',
    count: pick('userData', 'bannedUsers'),
    desc: `封禁账号 ${formatNumber(pick('userData', 'bannedUsers'))}，待处理反馈 ${formatNumber(pick('feedbackData', 'pendingFeedbacks'))}`,
    tone: 'danger',
    to: '/admin/users',
    actionLabel: '查看用户'
  }
])

const actionCards = computed(() => [
  {
    key: 'new-users',
    badge: '增长',
    badgeTone: 'info',
    title: '查看新增用户',
    desc: `今日新增 ${formatNumber(pick('userData', 'todayNewUsers'))} 位用户，关注注册后资料完善和首日行为。`,
    to: '/admin/users',
    actionLabel: '进入用户管理',
    tone: ''
  },
  {
    key: 'growth-activity',
    badge: '活动',
    badgeTone: 'success',
    title: '查看增长活动',
    desc: `近 7 日动态 ${formatNumber(pick('engagementData', 'dynamicsSevenDays'))}，可结合活跃趋势判断内容供给。`,
    to: '/admin/analytics',
    actionLabel: '查看数据',
    tone: ''
  },
  {
    key: 'pending-content',
    badge: '审核',
    badgeTone: totalRiskCount.value > 0 ? 'warning' : 'success',
    title: '查看待审核内容',
    desc: `当前风险事项 ${formatNumber(totalRiskCount.value)} 项，建议先清理内容和举报队列。`,
    to: '/admin/fellowship-dynamics',
    actionLabel: '去审核',
    tone: totalRiskCount.value > 0 ? 'warning' : ''
  }
])

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
    label: '待审核内容合计',
    value: pick('contentData', 'pendingContent'),
    delta: trendDelta(trends.value.contentPublish),
    meta: '较昨日',
    detail: `分享${formatNumber(pendingReviewBreakdown.value.shares)} / 互助${formatNumber(pendingReviewBreakdown.value.helps)} / 资源${formatNumber(pendingReviewBreakdown.value.resources)}；近7天动态 ${formatNumber(pendingReviewBreakdown.value.dynamics7d)}（仅统计，非待清零队列）`,
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
    title: '近7天新增动态',
    count: pick('engagementData', 'dynamicsSevenDays'),
    desc: '统计近7天发帖量，数字不会因「已看过」而减少；处置需删帖或等超过7天',
    tone: 'green',
    icon: '动',
    to: '/admin/fellowship-dynamics',
    actionLabel: '去查看'
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
    title: '审核与用户',
    items: [
      { label: '资料审核', icon: '资', tone: 'green', to: '/admin/verifications' },
      { label: '动态审核', icon: '审', tone: 'green', to: '/admin/fellowship-dynamics' },
      { label: '邀请记录', icon: '邀', tone: 'purple', to: '/admin/invites' },
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
      { label: '访客分析', icon: '数', tone: 'blue', to: '/admin/analytics' },
      { label: '角色权限', icon: '权', tone: 'blue', to: '/admin/users' },
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
    { key: 'notices', title: '系统提醒', to: '/admin/analytics', items: recent.notices || [] }
  ]
})

const recentLinkFallback = {
  users: '/admin/users',
  contents: '/admin/fellowship-dynamics',
  reports: '/admin/reports?status=PENDING',
  feedbacks: '/admin/feedbacks',
  notices: ''
}

function resolveRecentLink(column, item) {
  const direct = String(item?.to || '').trim()
  if (direct) return direct
  const fallback = recentLinkFallback[column?.key]
  return fallback || column?.to || ''
}

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

.stat-content .stat-detail {
  display: block;
  margin-top: 4px;
  color: var(--lc-subtle);
  font-size: 11px;
  line-height: 1.35;
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
  margin: 0;
}

.recent-row {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 8px;
  border: 1px solid transparent;
  border-radius: 10px;
  padding: 6px 8px;
  color: inherit;
  text-decoration: none;
}

.recent-row--link {
  cursor: pointer;
  transition: background 0.15s ease, border-color 0.15s ease;
}

.recent-row--link:hover {
  background: rgba(37, 99, 235, 0.05);
  border-color: rgba(37, 99, 235, 0.12);
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

.dashboard-page {
  gap: var(--lc-space-5);
  max-width: 1440px;
}

.hero-main {
  min-width: 0;
}

.hero-kicker-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--lc-space-3);
}

.hero-kicker-row {
  margin-bottom: var(--lc-space-4);
}

.admin-operation-hero h1 {
  margin: 0;
  color: var(--lc-text);
  font-size: 34px;
  line-height: 1.16;
}

.admin-operation-hero p {
  max-width: 680px;
  margin: var(--lc-space-3) 0 0;
  color: var(--lc-muted);
  font-weight: 700;
  line-height: 1.7;
}

.hero-summary-grid,
.summary-card {
  display: grid;
  gap: var(--lc-space-3);
}

.hero-summary-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.summary-card {
  display: grid;
  gap: 8px;
  background: var(--lc-bg);
  border: 1px solid var(--lc-border);
  border-radius: 10px;
  padding: var(--lc-space-4);
}

.summary-card.info {
  border-color: var(--lc-blue-border);
  background: color-mix(in srgb, var(--lc-blue-light) 54%, var(--lc-surface));
}

.summary-card.success {
  border-color: color-mix(in srgb, var(--lc-green) 24%, var(--lc-border));
  background: color-mix(in srgb, var(--lc-green-light) 54%, var(--lc-surface));
}

.summary-card.warning {
  border-color: color-mix(in srgb, var(--lc-amber) 34%, var(--lc-border));
  background: color-mix(in srgb, var(--lc-amber-light) 62%, var(--lc-surface));
}

.summary-card span {
  color: var(--lc-muted);
  font-size: 12px;
  font-weight: 900;
}

.summary-card strong {
  overflow: hidden;
  color: var(--lc-text);
  font-size: 30px;
  line-height: 1;
  font-variant-numeric: tabular-nums;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.summary-card small {
  margin: 0;
  color: var(--lc-muted);
  font-size: 13px;
  font-weight: 700;
  line-height: 1.55;
}

.metric-trend {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--lc-space-2);
  color: var(--lc-muted);
  font-size: 12px;
  font-weight: 800;
}

.risk-section {
  border-color: color-mix(in srgb, var(--lc-amber) 18%, var(--lc-border));
}

.risk-card h3,
.action-card h3 {
  margin: 0;
  color: var(--lc-text);
  font-size: 15px;
}

.risk-card-head strong {
  color: var(--lc-text);
  font-size: 18px;
  font-variant-numeric: tabular-nums;
}

.card-link {
  justify-self: start;
  color: var(--lc-blue);
  font-size: 13px;
  font-weight: 900;
  text-decoration: none;
}

.ops-grid {
  grid-template-columns: minmax(0, 1.15fr) minmax(0, 1.15fr) 360px;
}

.ops-card {
  border-radius: 10px;
  padding: var(--lc-space-4);
}

@media (max-width: 1280px) {
  .ops-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .hero-summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
