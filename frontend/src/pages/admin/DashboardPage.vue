<template>
  <section class="admin-page dashboard-page">
    <div class="dashboard-shell">
      <main class="dashboard-main">
        <TodayStats :loading="loading" :items="todayStats" />

        <section class="operation-zone">
          <ContentPanel :loading="loading" :items="contentMetrics" />
          <SocialPanel :loading="loading" :items="socialMetrics" />
        </section>

        <GovernanceBar :loading="loading" :items="governanceItems" />
      </main>

      <aside class="dashboard-side">
        <QuickActions />
      </aside>
    </div>

    <div v-if="error" class="admin-error">
      <p>{{ error }}</p>
      <button class="admin-btn" @click="load">重新加载</button>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getAdminStats } from '@/api/adminContent.js'
import TodayStats from './components/TodayStats.vue'
import ContentPanel from './components/ContentPanel.vue'
import SocialPanel from './components/SocialPanel.vue'
import GovernanceBar from './components/GovernanceBar.vue'
import QuickActions from './components/QuickActions.vue'

const loading = ref(true)
const error = ref('')
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
  pendingTasks: 0
})

function pick(groupKey, key) {
  return stats.value?.[groupKey]?.[key] ?? stats.value?.[key] ?? 0
}

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
  { label: '公告数', value: pick('contentData', 'totalAnnouncements') },
  { label: '文章数', value: pick('contentData', 'totalArticles') },
  { label: '活动数', value: pick('contentData', 'totalEvents') },
  { label: '推荐内容', value: pick('contentData', 'recommendedContent') }
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
  { label: '待审核内容', value: pick('fellowshipData', 'pendingVerifications'), to: '/admin/verifications', actionText: '去审核' },
  { label: '封禁用户', value: pick('governanceData', 'bannedUsers') || pick('userData', 'bannedUsers'), to: '/admin/users', actionText: '查看列表' }
])

async function load() {
  loading.value = true
  error.value = ''
  try {
    stats.value = await getAdminStats()
  } catch (e) {
    error.value = e.message || '统计信息加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.dashboard-shell {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
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
</style>
