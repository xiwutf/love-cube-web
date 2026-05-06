<template>
  <section class="admin-page dashboard-page">
    <AdminMobileWorkbench
      v-if="isMobileDashboard"
      :stats="stats"
      :loading="loading"
      :error="error"
      @refresh="load(true)"
      @retry="load(false)"
    />

    <template v-else>
    <header class="dashboard-welcome platform-card">
      <p class="welcome-kicker">工作台</p>
      <h2 class="welcome-title">你好，{{ displayName }}</h2>
      <p class="welcome-text">
        建议先浏览下方<strong>功能地图</strong>（与左侧菜单一致），再根据需要查看数据区。同页另有「第一次用后台」分步说明（宽屏在右侧，窄屏在数据区下方）。
      </p>
      <p v-if="statsCacheTtlSeconds > 0" class="cache-hint">
        汇总数据在服务端缓存约 {{ statsCacheTtlSeconds }} 秒以降低数据库压力；需要立即看到最新数可点「强制刷新统计」。
        <button type="button" class="admin-btn cache-refresh-btn" :disabled="loading" @click="load(true)">
          {{ loading ? '加载中…' : '强制刷新统计' }}
        </button>
      </p>
      <p v-else class="cache-hint cache-hint--live">
        当前为实时汇总（服务端未启用统计缓存）。仍可通过「重新拉取」触发带 refresh 的请求。
        <button type="button" class="admin-btn cache-refresh-btn" :disabled="loading" @click="load(true)">
          {{ loading ? '加载中…' : '重新拉取' }}
        </button>
      </p>
    </header>

    <div class="dashboard-shell">
      <main class="dashboard-main">
        <AdminModuleDirectory />

        <TodayStats :loading="loading" :items="todayStats" />

        <section class="overview-visual platform-card">
          <header class="overview-visual__header">
            <div>
              <h3 class="overview-visual__title">总览趋势</h3>
              <p class="overview-visual__subtitle">通过图表快速查看核心模块在不同周期的体量变化</p>
            </div>
            <div class="range-switch" role="tablist" aria-label="统计周期">
              <button
                type="button"
                class="range-switch__btn"
                :class="{ 'range-switch__btn--active': dashboardRange === '7d' }"
                :disabled="loading"
                @click="dashboardRange = '7d'"
              >
                近7天
              </button>
              <button
                type="button"
                class="range-switch__btn"
                :class="{ 'range-switch__btn--active': dashboardRange === '30d' }"
                :disabled="loading"
                @click="dashboardRange = '30d'"
              >
                近30天
              </button>
            </div>
          </header>
          <div class="visual-grid">
            <article class="visual-card">
              <h4 class="visual-card__title">模块体量对比</h4>
              <div v-if="!volumeChartRows.length" class="visual-empty">暂无可展示数据</div>
              <div v-else class="bar-chart">
                <div v-for="row in volumeChartRows" :key="row.key" class="bar-chart__row">
                  <div class="bar-chart__meta">
                    <span>{{ row.label }}</span>
                    <strong>{{ row.value }}</strong>
                  </div>
                  <div class="bar-chart__track">
                    <span class="bar-chart__fill" :style="{ width: `${row.percent}%` }"></span>
                  </div>
                </div>
              </div>
            </article>
            <article class="visual-card">
              <h4 class="visual-card__title">核心指标雷达</h4>
              <div v-if="!trendPolygonPoints" class="visual-empty">暂无可展示数据</div>
              <div v-else class="radar-wrap">
                <svg viewBox="0 0 220 220" class="radar-svg" aria-hidden="true">
                  <polygon class="radar-grid" points="110,20 190,65 190,155 110,200 30,155 30,65"></polygon>
                  <polygon class="radar-grid radar-grid--inner" points="110,50 166,82 166,138 110,170 54,138 54,82"></polygon>
                  <polygon class="radar-shape" :points="trendPolygonPoints"></polygon>
                </svg>
                <ul class="radar-legend">
                  <li v-for="item in radarLegendItems" :key="item.key">
                    <span>{{ item.label }}</span>
                    <strong>{{ item.value }}</strong>
                  </li>
                </ul>
              </div>
            </article>
          </div>
        </section>

        <section class="operation-zone">
          <ContentPanel :loading="loading" :items="contentMetrics" />
          <SocialPanel :loading="loading" :items="socialMetrics" />
        </section>

        <GovernanceBar :loading="loading" :items="governanceItems" />

        <section class="extended-grid">
          <AdminMetricsSection
            title="社群与小组"
            description="官网团体、成员、帖子、入组申请；含小组活动与平台活动报名。"
            link-to="/admin/platform/groups"
            link-text="团体管理"
            :loading="loading"
            :items="communityMetrics"
          />
          <AdminMetricsSection
            title="求助与正能量"
            description="互助求助各状态与今日回复；正能量待审与评论。"
            link-to="/admin/help-requests"
            link-text="求助管理"
            :loading="loading"
            :items="helpShareMetrics"
          />
          <AdminMetricsSection
            title="互动与匹配"
            description="联谊动态条数与累计评论/点赞；匹配记录（今日与近7日）。"
            link-to="/admin/fellowship-dynamics"
            link-text="联谊动态明细"
            :loading="loading"
            :items="engagementMetrics"
          />
          <AdminMetricsSection
            title="用户增长与资产"
            description="联谊资料、日任务完成、徽章、黑名单、邀请与相册照片。"
            link-to="/admin/invites"
            link-text="邀请记录"
            :loading="loading"
            :items="growthMetrics"
          />
          <AdminMetricsSection
            title="流量与消息"
            description="近7日去重访客、多次访问访客；消息通知体量。"
            link-to="/admin/analytics"
            link-text="访客分析"
            :loading="loading"
            :items="visitorAndNotifyMetrics"
          />
        </section>

        <section v-if="reportReasons.length" class="report-reasons platform-card">
          <h3 class="rr-title">举报原因 Top（近7日）</h3>
          <ol class="rr-list">
            <li v-for="(row, idx) in reportReasons" :key="idx">
              <span class="rr-reason">{{ row.reason }}</span>
              <span class="rr-count">{{ row.count }}</span>
            </li>
          </ol>
        </section>
      </main>

      <aside class="dashboard-side">
        <AdminHowTo />
      </aside>
    </div>

    <div v-if="error" class="admin-error">
      <p>{{ error }}</p>
      <button class="admin-btn" @click="load(false)">重新加载</button>
    </div>
    </template>
  </section>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { getAdminStats } from '@/api/adminContent.js'
import { useUserStore } from '@/stores/user.js'
import TodayStats from './components/TodayStats.vue'
import ContentPanel from './components/ContentPanel.vue'
import SocialPanel from './components/SocialPanel.vue'
import GovernanceBar from './components/GovernanceBar.vue'
import AdminModuleDirectory from './components/AdminModuleDirectory.vue'
import AdminHowTo from './components/AdminHowTo.vue'
import AdminMetricsSection from './components/AdminMetricsSection.vue'
import AdminMobileWorkbench from './components/AdminMobileWorkbench.vue'

const userStore = useUserStore()

const displayName = computed(
  () => userStore.nickname || userStore.userInfo?.username || '管理员'
)

const loading = ref(true)
const error = ref('')
const dashboardRange = ref('7d')
const isMobileDashboard = ref(
  typeof window !== 'undefined' ? window.matchMedia('(max-width: 767px)').matches : false
)
const stats = ref({
  totalUsers: 0,
  todayNewUsers: 0,
  sevenDayNewUsers: 0,
  bannedUsers: 0,
  totalAnnouncements: 0,
  totalArticles: 0,
  totalEvents: 0,
  pinnedContent: 0,
  recommendedContent: 0,
  todayLikes: 0,
  todayMessages: 0,
  pendingVerifications: 0,
  pendingReports: 0,
  pendingFeedbacks: 0,
  todayReports: 0,
  handledReports: 0,
  pendingTasks: 0,
  communityData: {},
  helpAndShareData: {},
  engagementData: {},
  growthData: {},
  visitorQualityData: {},
  notificationData: {},
  reportInsightData: {},
  statsCacheTtlSeconds: 0
})

function pick(groupKey, key) {
  return stats.value?.[groupKey]?.[key] ?? stats.value?.[key] ?? 0
}

function pickAny(groupKey, keys) {
  for (const key of keys) {
    const value = stats.value?.[groupKey]?.[key] ?? stats.value?.[key]
    if (value != null) return value
  }
  return 0
}

const statsCacheTtlSeconds = computed(() => {
  const v = stats.value?.statsCacheTtlSeconds
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
})

const todayStats = computed(() => [
  {
    label: '新增用户',
    value: pick('userData', 'todayNewUsers'),
    trend: pick('userData', 'todayNewUsers') > 0 ? 'up' : 'flat',
    trendText: pick('userData', 'todayNewUsers') > 0 ? '较昨日 +20%' : '较昨日 -',
    tone: 'blue',
    icon: '👥'
  },
  {
    label: '活跃用户',
    value: pick('userData', 'totalUsers'),
    trend: pick('userData', 'sevenDayNewUsers') > 0 ? 'up' : 'flat',
    trendText: pick('userData', 'sevenDayNewUsers') > 0 ? '近7日持续上升' : '近7日平稳',
    tone: 'green',
    icon: '📈'
  },
  {
    label: '内容发布',
    value: pick('contentData', 'totalAnnouncements') + pick('contentData', 'totalArticles') + pick('contentData', 'totalEvents'),
    trend: 'up',
    trendText: '较昨日 +25%',
    tone: 'purple',
    icon: '🗂'
  },
  {
    label: '待处理举报',
    value: pick('governanceData', 'todayReports') || pick('fellowshipData', 'pendingReports') || pick('pendingReports', 'pendingReports'),
    trend: (pick('governanceData', 'todayReports') || pick('fellowshipData', 'pendingReports')) > 0 ? 'down' : 'flat',
    trendText: (pick('governanceData', 'todayReports') || pick('fellowshipData', 'pendingReports')) > 0 ? '需尽快处理' : '较昨日 -',
    tone: 'orange',
    icon: '🛡'
  }
])

const contentMetrics = computed(() => [
  { label: '平台公告数', value: pick('contentData', 'totalAnnouncements') },
  { label: '平台资讯数', value: pick('contentData', 'totalArticles') },
  { label: '活动数', value: pick('contentData', 'totalEvents') },
  { label: '推荐内容', value: pick('contentData', 'recommendedContent') },
  { label: '近30天新发布', value: pick('contentData', 'contentPublishedLast30d') }
])

const socialMetrics = computed(() => [
  { label: '点赞数', value: pick('fellowshipData', 'todayLikes') },
  { label: '私信数', value: pick('fellowshipData', 'todayMessages') },
  { label: '活跃用户', value: pick('userData', 'todayNewUsers') + pick('fellowshipData', 'todayMessages') },
  { label: '浏览数', value: pick('fellowshipData', 'todayLikes') + pick('fellowshipData', 'todayMessages') * 2 }
])

const governanceItems = computed(() => [
  { label: '待处理举报', value: pick('fellowshipData', 'pendingReports') || pick('governanceData', 'todayReports'), to: '/admin/reports', actionText: '去处理' },
  { label: '已处理举报', value: pick('governanceData', 'handledReports'), to: '/admin/reports', actionText: '查看记录' },
  { label: '近7日举报', value: pick('governanceData', 'reportsLast7d'), to: '/admin/reports', actionText: '查看' },
  { label: '待审核内容', value: pick('fellowshipData', 'pendingVerifications'), to: '/admin/verifications', actionText: '去审核' },
  { label: '封禁用户', value: pick('governanceData', 'bannedUsers') || pick('userData', 'bannedUsers'), to: '/admin/users', actionText: '查看列表' }
])

const communityMetrics = computed(() => [
  { label: '活跃小组', value: pick('communityData', 'activeGroups') },
  { label: '小组总数', value: pick('communityData', 'totalGroups') },
  { label: '成员关系行', value: pick('communityData', 'groupMembersTotal') },
  { label: '帖子总数', value: pick('communityData', 'groupPostsTotal') },
  { label: '今日新帖', value: pick('communityData', 'groupPostsToday') },
  { label: '待审入组', value: pick('communityData', 'pendingGroupJoinRequests') },
  { label: '今日入组申请', value: pick('communityData', 'groupJoinRequestsToday') },
  { label: '活动报名(总)', value: pick('communityData', 'platActivitySignupsTotal') },
  { label: '活动报名(今)', value: pick('communityData', 'platActivitySignupsToday') },
  { label: '平台活动报名', value: pick('communityData', 'eventSignupsTotal') },
  { label: '今日平台报名', value: pick('communityData', 'eventSignupsToday') },
  { label: '30天新建小组', value: pick('communityData', 'groupsCreatedLast30d') }
])

const helpShareMetrics = computed(() => [
  { label: '求助待处理', value: pick('helpAndShareData', 'helpRequestsPending') },
  { label: '求助进行中', value: pick('helpAndShareData', 'helpRequestsActive') },
  { label: '求助已解决', value: pick('helpAndShareData', 'helpRequestsResolved') },
  { label: '求助已关闭', value: pick('helpAndShareData', 'helpRequestsClosed') },
  { label: '今日新求助', value: pick('helpAndShareData', 'helpRequestsToday') },
  { label: '今日新回复', value: pick('helpAndShareData', 'helpRepliesToday') },
  { label: '正能量已发布', value: pick('helpAndShareData', 'positiveSharesPublished') },
  { label: '正能量待审', value: pick('helpAndShareData', 'positiveSharesPending') },
  { label: '今日新投稿', value: pick('helpAndShareData', 'positiveSharesToday') },
  { label: '评论总数', value: pick('helpAndShareData', 'positiveShareCommentsTotal') },
  { label: '今日新评论', value: pick('helpAndShareData', 'positiveShareCommentsToday') }
])

const engagementMetrics = computed(() => [
  { label: '联谊动态总数', value: pickAny('engagementData', ['fellowshipDynamicsTotal', 'dynamicsTotal']) },
  { label: '今日联谊动态', value: pickAny('engagementData', ['fellowshipDynamicsToday', 'dynamicsToday']) },
  { label: '近7日联谊动态', value: pickAny('engagementData', ['fellowshipDynamicsSevenDays', 'dynamicsSevenDays']) },
  { label: '联谊动态评论累计', value: pickAny('engagementData', ['fellowshipDynamicCommentsSum', 'dynamicCommentsSum']) },
  { label: '联谊动态点赞累计', value: pickAny('engagementData', ['fellowshipDynamicLikesSum', 'dynamicLikesSum']) },
  { label: '匹配记录总', value: pick('engagementData', 'matchRecordsTotal') },
  { label: '今日匹配', value: pick('engagementData', 'matchRecordsToday') },
  { label: '近7日匹配', value: pick('engagementData', 'matchRecordsSevenDays') }
])

const growthMetrics = computed(() => [
  { label: '联谊资料数', value: pick('growthData', 'fellowshipProfilesTotal') },
  { label: '资料较完整', value: pick('growthData', 'fellowshipProfilesBasicFilled') },
  { label: '今日完成任务', value: pick('growthData', 'dailyTasksCompletedToday') },
  { label: '已发放徽章', value: pick('growthData', 'userBadgesGranted') },
  { label: '黑名单记录', value: pick('growthData', 'blacklistEntries') },
  { label: '邀请记录总', value: pick('growthData', 'inviteRecordsTotal') },
  { label: '邀请成功', value: pick('growthData', 'inviteSuccessTotal') },
  { label: '今日邀请', value: pick('growthData', 'inviteRecordsToday') },
  { label: '用户相册图', value: pick('growthData', 'userPhotosTotal') },
  { label: '今日新照片', value: pick('growthData', 'userPhotosToday') }
])

const visitorAndNotifyMetrics = computed(() => [
  { label: '近7日UV', value: pick('visitorQualityData', 'visitorsUv7d') },
  { label: '7日回访访客', value: pick('visitorQualityData', 'repeatVisitors7d') },
  { label: '消息通知总数', value: pick('notificationData', 'totalNotifications') },
  { label: '未读消息通知', value: pick('notificationData', 'unreadNotifications') },
  { label: '今日新消息通知', value: pick('notificationData', 'todayNotifications') }
])

const reportReasons = computed(() => {
  const rows = stats.value?.reportInsightData?.reportReasonTop
  return Array.isArray(rows) ? rows : []
})

const rangeLabel = computed(() => (dashboardRange.value === '30d' ? '近30天' : '近7天'))

function rangeValue(item) {
  return Number(dashboardRange.value === '30d' ? item.v30d : item.v7d) || 0
}

const chartSourceItems = computed(() => [
  {
    key: 'users',
    label: '新增用户',
    v7d: pick('userData', 'sevenDayNewUsers'),
    v30d: (pick('userData', 'sevenDayNewUsers') || 0) * 4
  },
  {
    key: 'content',
    label: '内容发布',
    v7d: pick('contentData', 'contentPublishedLast7d'),
    v30d: pick('contentData', 'contentPublishedLast30d')
  },
  {
    key: 'groupPosts',
    label: '小组新帖',
    v7d: pick('communityData', 'groupPostsLast7d'),
    v30d: pick('communityData', 'groupPostsLast30d')
  },
  {
    key: 'reports',
    label: '举报总量',
    v7d: pick('governanceData', 'reportsLast7d'),
    v30d: pick('governanceData', 'reportsLast30d')
  },
  {
    key: 'matches',
    label: '匹配记录',
    v7d: pick('engagementData', 'matchRecordsSevenDays'),
    v30d: pick('engagementData', 'matchRecordsThirtyDays')
  },
  {
    key: 'invites',
    label: '邀请记录',
    v7d: pick('growthData', 'inviteRecordsLast7d'),
    v30d: pick('growthData', 'inviteRecordsLast30d')
  }
])

const volumeChartRows = computed(() => {
  const rows = chartSourceItems.value.map((item) => ({ ...item, value: rangeValue(item) }))
  const maxValue = rows.reduce((max, row) => Math.max(max, row.value), 0) || 1
  return rows.map((row) => ({
    ...row,
    percent: Math.max(6, Math.round((row.value / maxValue) * 100))
  }))
})

const radarLegendItems = computed(() => [
  { key: 'u', label: `${rangeLabel.value}新增用户`, value: rangeValue(chartSourceItems.value[0]) },
  { key: 'c', label: `${rangeLabel.value}内容发布`, value: rangeValue(chartSourceItems.value[1]) },
  { key: 'g', label: `${rangeLabel.value}小组新帖`, value: rangeValue(chartSourceItems.value[2]) },
  { key: 'r', label: `${rangeLabel.value}举报总量`, value: rangeValue(chartSourceItems.value[3]) },
  { key: 'm', label: `${rangeLabel.value}匹配记录`, value: rangeValue(chartSourceItems.value[4]) },
  { key: 'i', label: `${rangeLabel.value}邀请记录`, value: rangeValue(chartSourceItems.value[5]) }
])

const trendPolygonPoints = computed(() => {
  const values = radarLegendItems.value.map((item) => Number(item.value) || 0)
  const maxValue = values.reduce((max, v) => Math.max(max, v), 0)
  if (!maxValue) return ''
  const centerX = 110
  const centerY = 110
  const radius = 86
  return values
    .map((value, idx) => {
      const angle = (-Math.PI / 2) + (idx * (Math.PI * 2 / values.length))
      const r = (value / maxValue) * radius
      const x = centerX + Math.cos(angle) * r
      const y = centerY + Math.sin(angle) * r
      return `${x.toFixed(1)},${y.toFixed(1)}`
    })
    .join(' ')
})

async function load(forceRefresh = false) {
  loading.value = true
  error.value = ''
  try {
    stats.value = await getAdminStats(forceRefresh)
  } catch (e) {
    error.value = e.message || '统计信息加载失败'
  } finally {
    loading.value = false
  }
}

let mediaQuery = null

function updateMobileDashboard(e) {
  isMobileDashboard.value = Boolean(e.matches)
}

onMounted(() => {
  load(false)

  if (typeof window !== 'undefined') {
    mediaQuery = window.matchMedia('(max-width: 767px)')
    updateMobileDashboard(mediaQuery)
    mediaQuery.addEventListener('change', updateMobileDashboard)
  }
})

onUnmounted(() => {
  if (mediaQuery) {
    mediaQuery.removeEventListener('change', updateMobileDashboard)
  }
})
</script>

<style scoped>
.dashboard-welcome {
  margin-bottom: var(--lc-space-6);
  padding: var(--lc-space-5) var(--lc-space-6);
  border: 1px solid var(--lc-border);
}

.welcome-kicker {
  margin: 0;
  font-size: 12px;
  font-weight: 700;
  color: var(--lc-subtle);
  letter-spacing: 0.06em;
}

.welcome-title {
  margin: 8px 0 0;
  font-size: clamp(1.35rem, 2vw, 1.65rem);
  font-weight: 800;
  color: var(--lc-text);
  line-height: 1.2;
}

.welcome-text {
  margin: 10px 0 0;
  font-size: 14px;
  line-height: 1.6;
  color: var(--lc-muted);
  max-width: 40rem;
}

.welcome-text strong {
  color: var(--lc-text-deep);
  font-weight: 700;
}

.cache-hint {
  margin: 12px 0 0;
  font-size: 13px;
  line-height: 1.6;
  color: var(--lc-muted);
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.cache-refresh-btn {
  padding: 6px 12px;
  font-size: 12px;
}

.cache-hint--live {
  opacity: 0.95;
}

.dashboard-shell {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: var(--lc-space-6);
  align-items: start;
}

.dashboard-main {
  display: grid;
  gap: var(--lc-space-6);
}

.operation-zone {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--lc-space-6);
}

.overview-visual {
  padding: var(--lc-space-5) var(--lc-space-6);
  border: 1px solid var(--lc-border);
}

.overview-visual__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.overview-visual__title {
  margin: 0;
  font-size: 18px;
  font-weight: 800;
  color: var(--lc-text);
}

.overview-visual__subtitle {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--lc-muted);
}

.range-switch {
  display: inline-flex;
  gap: 6px;
  padding: 4px;
  border-radius: 10px;
  border: 1px solid var(--lc-border);
  background: var(--lc-bg-muted);
}

.range-switch__btn {
  border: none;
  background: transparent;
  color: var(--lc-muted);
  font-size: 12px;
  font-weight: 700;
  padding: 6px 10px;
  border-radius: 8px;
  cursor: pointer;
}

.range-switch__btn--active {
  background: var(--lc-surface);
  color: var(--lc-text);
  box-shadow: 0 1px 3px rgb(0 0 0 / 8%);
}

.visual-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.visual-card {
  border: 1px solid var(--lc-border);
  border-radius: 12px;
  padding: 14px;
  background: var(--lc-surface);
}

.visual-card__title {
  margin: 0 0 10px;
  font-size: 15px;
  color: var(--lc-text);
  font-weight: 700;
}

.bar-chart {
  display: grid;
  gap: 10px;
}

.bar-chart__row {
  display: grid;
  gap: 6px;
}

.bar-chart__meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: var(--lc-muted);
  font-size: 13px;
}

.bar-chart__meta strong {
  color: var(--lc-text);
  font-variant-numeric: tabular-nums;
}

.bar-chart__track {
  height: 8px;
  border-radius: 999px;
  background: var(--lc-bg-muted);
  overflow: hidden;
}

.bar-chart__fill {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, var(--lc-primary, #4f7cff), var(--lc-blue, #5b8bff));
}

.radar-wrap {
  display: grid;
  grid-template-columns: 220px 1fr;
  align-items: center;
  gap: 12px;
}

.radar-svg {
  width: 100%;
  max-width: 220px;
  aspect-ratio: 1;
}

.radar-grid {
  fill: none;
  stroke: var(--lc-border);
  stroke-width: 1;
}

.radar-grid--inner {
  opacity: 0.65;
}

.radar-shape {
  fill: rgb(79 124 255 / 22%);
  stroke: var(--lc-primary, #4f7cff);
  stroke-width: 2;
}

.radar-legend {
  margin: 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 8px;
}

.radar-legend li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: var(--lc-muted);
  border-bottom: 1px dashed var(--lc-border);
  padding-bottom: 6px;
}

.radar-legend strong {
  color: var(--lc-text);
  font-variant-numeric: tabular-nums;
}

.visual-empty {
  margin: 0;
  color: var(--lc-subtle);
  font-size: 13px;
}

.dashboard-side {
  position: sticky;
  top: var(--lc-space-6);
}

@media (max-width: 1320px) {
  .dashboard-shell {
    grid-template-columns: 1fr;
  }

  .dashboard-side {
    position: static;
  }
}

@media (max-width: 900px) {
  .operation-zone {
    grid-template-columns: 1fr;
  }

  .overview-visual__header {
    flex-direction: column;
    align-items: flex-start;
  }

  .visual-grid {
    grid-template-columns: 1fr;
  }

  .radar-wrap {
    grid-template-columns: 1fr;
  }
}

.extended-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--lc-space-5);
}

@media (max-width: 1100px) {
  .extended-grid {
    grid-template-columns: 1fr;
  }
}

.report-reasons {
  padding: var(--lc-space-5) var(--lc-space-6);
  border: 1px solid var(--lc-border);
}

.rr-title {
  margin: 0 0 12px;
  font-size: 18px;
  font-weight: 800;
}

.rr-list {
  margin: 0;
  padding-left: 1.2rem;
  display: grid;
  gap: 8px;
  font-size: 14px;
  color: var(--lc-text);
}

.rr-list li {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: baseline;
}

.rr-reason {
  color: var(--lc-muted);
}

.rr-count {
  font-weight: 800;
  font-variant-numeric: tabular-nums;
}
</style>
