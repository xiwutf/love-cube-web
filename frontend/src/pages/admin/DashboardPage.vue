<template>
  <section class="admin-page dashboard-page">
    <section class="platform-card admin-hero">
      <p class="platform-kicker">Operations</p>
      <h1 class="platform-title">后台运营看板</h1>
      <p class="platform-subtitle">
        汇总用户增长、内容运营、联谊互动和平台治理数据，便于快速定位待处理事项。
      </p>
    </section>

    <section class="dashboard-groups">
      <article
        v-for="group in statGroups"
        :key="group.title"
        class="platform-card dashboard-group"
      >
        <div class="group-head">
          <div>
            <p class="group-kicker">{{ group.kicker }}</p>
            <h2>{{ group.title }}</h2>
          </div>
          <router-link v-if="group.to" :to="group.to" class="group-link">进入处理</router-link>
        </div>

        <div class="metric-grid">
          <div
            v-for="item in group.items"
            :key="item.label"
            class="metric-card"
            :class="{ alert: item.alert }"
          >
            <p class="metric-label">{{ item.label }}</p>
            <p class="metric-value" :class="{ loading }">{{ loading ? '...' : item.value }}</p>
            <p class="metric-desc">{{ item.desc }}</p>
          </div>
        </div>
      </article>
    </section>

    <section class="platform-card quick-section">
      <div class="group-head">
        <div>
          <p class="group-kicker">Shortcuts</p>
          <h2>快捷入口</h2>
        </div>
      </div>

      <div class="quick-grid">
        <router-link
          v-for="item in quickCards"
          :key="item.to"
          :to="item.to"
          class="quick-card"
        >
          <span class="quick-dot" :class="item.tone"></span>
          <span>
            <strong>{{ item.title }}</strong>
            <em>{{ item.desc }}</em>
          </span>
        </router-link>
      </div>
    </section>

    <div v-if="error" class="admin-error">
      <p>{{ error }}</p>
      <button class="admin-btn" @click="load">重新加载</button>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getAdminStats } from '@/api/adminContent.js'

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
  return stats.value?.[groupKey]?.[key] ? stats.value?.[key] ? 0
}

const statGroups = computed(() => [
  {
    title: '用户数据',
    kicker: 'Users',
    to: '/admin/users',
    items: [
      { label: '总用户数', value: pick('userData', 'totalUsers'), desc: '全站注册账号', alert: false },
      { label: '今日新增', value: pick('userData', 'todayNewUsers'), desc: '今日新注册用户', alert: false },
      { label: '近7日新增', value: pick('userData', 'sevenDayNewUsers'), desc: '最近7天新增用户', alert: false },
      { label: '封禁用户数', value: pick('userData', 'bannedUsers'), desc: '当前被封禁账号', alert: pick('userData', 'bannedUsers') > 0 }
    ]
  },
  {
    title: '内容数据',
    kicker: 'Content',
    to: '/admin/announcements',
    items: [
      { label: '公告数', value: pick('contentData', 'totalAnnouncements'), desc: '公告通知内容', alert: false },
      { label: '文章数', value: pick('contentData', 'totalArticles'), desc: '资讯文章内容', alert: false },
      { label: '活动数', value: pick('contentData', 'totalEvents'), desc: '平台活动内容', alert: false },
      { label: '置顶内容数', value: pick('contentData', 'pinnedContent'), desc: '首页优先展示内容', alert: false },
      { label: '推荐内容数', value: pick('contentData', 'recommendedContent'), desc: '运营推荐内容', alert: false }
    ]
  },
  {
    title: '联谊数据',
    kicker: 'Fellowship',
    to: '/admin/verifications',
    items: [
      { label: '今日喜欢数', value: pick('fellowshipData', 'todayLikes'), desc: '今日 LIKE / SUPER LIKE', alert: false },
      { label: '今日私信数', value: pick('fellowshipData', 'todayMessages'), desc: '今日聊天消息', alert: false },
      { label: '待审核认证', value: pick('fellowshipData', 'pendingVerifications'), desc: '实名认证与资料审核', alert: pick('fellowshipData', 'pendingVerifications') > 0 },
      { label: '待处理举报', value: pick('fellowshipData', 'pendingReports'), desc: '用户举报待处理', alert: pick('fellowshipData', 'pendingReports') > 0 }
    ]
  },
  {
    title: '平台治理',
    kicker: 'Governance',
    to: '/admin/reports',
    items: [
      { label: '今日举报', value: pick('governanceData', 'todayReports'), desc: '今日新增举报', alert: pick('governanceData', 'todayReports') > 0 },
      { label: '已处理举报', value: pick('governanceData', 'handledReports'), desc: '已审核或处置举报', alert: false },
      { label: '封禁用户', value: pick('governanceData', 'bannedUsers'), desc: '账号治理结果', alert: pick('governanceData', 'bannedUsers') > 0 },
      { label: '待处理事项', value: pick('governanceData', 'pendingTasks'), desc: '认证、举报、反馈合计', alert: pick('governanceData', 'pendingTasks') > 0 }
    ]
  }
])

const quickCards = [
  { title: '用户管理', desc: '角色、状态与账号处理', to: '/admin/users', tone: 'tone-blue' },
  { title: '内容管理', desc: '公告、文章、活动运营', to: '/admin/announcements', tone: 'tone-green' },
  { title: '认证审核', desc: '处理实名认证申请', to: '/admin/verifications', tone: 'tone-purple' },
  { title: '举报处理', desc: '审核举报并执行处置', to: '/admin/reports', tone: 'tone-red' },
  { title: '模块管理', desc: '平台模块入口配置', to: '/admin/modules', tone: 'tone-cyan' }
]

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

.dashboard-page {
  display: grid;
  gap: 16px;
}

.dashboard-groups {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.dashboard-group,
.quick-section {
  padding: 22px;
}

.group-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 16px;
}

.group-kicker {
  margin: 0 0 6px;
  color: var(--lc-blue);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.group-head h2 {
  margin: 0;
  color: var(--lc-text);
  font-size: 20px;
  font-weight: 900;
}

.group-link {
  flex-shrink: 0;
  color: var(--lc-blue-dark);
  font-size: 13px;
  font-weight: 800;
  text-decoration: none;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.metric-card {
  min-height: 128px;
  padding: 16px;
  border: 1px solid var(--lc-border);
  border-radius: 14px;
  background: var(--lc-surface);
}

.metric-card.alert {
  border-color: var(--lc-pink-border);
  background: var(--lc-pink-light);
}

.metric-label {
  margin: 0;
  color: var(--lc-muted);
  font-size: 13px;
  font-weight: 800;
}

.metric-value {
  margin: 10px 0 6px;
  color: var(--lc-text);
  font-size: 32px;
  line-height: 1;
  font-weight: 900;
}

.metric-value.loading {
  color: var(--lc-border);
}

.metric-card.alert .metric-value {
  color: var(--lc-red);
}

.metric-desc {
  margin: 0;
  color: var(--lc-subtle);
  font-size: 12px;
  line-height: 1.55;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.quick-card {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 84px;
  padding: 14px;
  border: 1px solid var(--lc-border);
  border-radius: 14px;
  color: inherit;
  text-decoration: none;
  background: var(--lc-surface);
  transition: var(--lc-transition);
}

.quick-card:hover {
  transform: translateY(-2px);
  border-color: var(--lc-blue-border);
  box-shadow: var(--lc-shadow);
}

.quick-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex: 0 0 auto;
}

.quick-card strong {
  display: block;
  color: var(--lc-text);
  font-size: 15px;
  font-weight: 900;
}

.quick-card em {
  display: block;
  margin-top: 4px;
  color: var(--lc-muted);
  font-size: 12px;
  line-height: 1.4;
  font-style: normal;
}

.tone-blue   { background: var(--lc-blue); }
.tone-green  { background: #059669; }
.tone-purple { background: #7c3aed; }
.tone-red    { background: var(--lc-red); }
.tone-cyan   { background: #0891b2; }

@media (max-width: 1180px) {
  .dashboard-groups {
    grid-template-columns: 1fr;
  }

  .quick-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 767px) {
  .dashboard-page {
    gap: 14px;
  }

  .dashboard-group,
  .quick-section {
    padding: 16px;
  }

  .metric-grid,
  .quick-grid {
    grid-template-columns: 1fr;
  }

  .metric-card {
    min-height: auto;
  }
}
</style>
