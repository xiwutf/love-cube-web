<template>
  <section class="admin-page analytics-page">
    <section class="platform-card analytics-hero">
      <div>
        <h1 class="platform-title">访客分析</h1>
        <p class="platform-subtitle">查看网站访问趋势、来源结构和近期访客行为。</p>
      </div>
      <div class="analytics-actions">
        <select v-model="range" class="admin-select">
          <option value="today">今天</option>
          <option value="7d">近 7 天</option>
          <option value="30d">近 30 天</option>
          <option value="90d">近 90 天</option>
        </select>
        <button class="admin-btn" :disabled="loading" @click="loadAll">{{ loading ? '加载中...' : '刷新数据' }}</button>
      </div>
    </section>

    <section class="analytics-kpi-grid">
      <article class="platform-card kpi-card">
        <p class="kpi-label">今日浏览量</p>
        <p class="kpi-value">{{ overview.todayPv }}</p>
        <p class="kpi-meta">昨日 {{ overview.yesterdayPv }}</p>
      </article>
      <article class="platform-card kpi-card">
        <p class="kpi-label">今日访客数</p>
        <p class="kpi-value">{{ overview.todayUv }}</p>
        <p class="kpi-meta">昨日 {{ overview.yesterdayUv }}</p>
      </article>
      <article class="platform-card kpi-card">
        <p class="kpi-label">累计浏览量</p>
        <p class="kpi-value">{{ overview.totalPv }}</p>
        <p class="kpi-meta">累计访客 {{ overview.totalUv }}</p>
      </article>
      <article class="platform-card kpi-card">
        <p class="kpi-label">在线访客</p>
        <p class="kpi-value">{{ overview.onlineUsers }}</p>
        <p class="kpi-meta">最近 5 分钟活跃</p>
      </article>
    </section>

    <section class="analytics-main-grid">
      <article class="platform-card analytics-panel">
        <h3>访问趋势（PV / UV）</h3>
        <div class="trend-list">
          <div v-for="point in trend.points" :key="point.date" class="trend-item">
            <span>{{ point.date }}</span>
            <strong>PV {{ point.pv }} / UV {{ point.uv }}</strong>
          </div>
          <p v-if="!trend.points.length" class="empty-text">暂无趋势数据</p>
        </div>
      </article>

      <article class="platform-card analytics-panel">
        <h3>热门页面</h3>
        <div class="rank-list">
          <div v-for="(item, idx) in topPages.items" :key="`${item.url}-${idx}`" class="rank-item">
            <span class="rank-index">{{ idx + 1 }}</span>
            <span class="rank-title">{{ item.url || '/' }}</span>
            <strong>PV {{ item.pv }} · UV {{ item.uv }}</strong>
          </div>
          <p v-if="!topPages.items.length" class="empty-text">暂无页面数据</p>
        </div>
      </article>
    </section>

    <section class="analytics-main-grid">
      <article class="platform-card analytics-panel">
        <h3>来源分析</h3>
        <div class="simple-list">
          <div v-for="item in sources.items" :key="item.name" class="simple-item">
            <span>{{ item.name }}</span>
            <strong>{{ item.count }}</strong>
          </div>
          <p v-if="!sources.items.length" class="empty-text">暂无来源数据</p>
        </div>
      </article>
      <article class="platform-card analytics-panel">
        <h3>终端分布</h3>
        <div class="simple-list">
          <p class="subheading">设备</p>
          <div v-for="item in client.devices" :key="`d-${item.name}`" class="simple-item">
            <span>{{ item.name }}</span><strong>{{ item.count }}</strong>
          </div>
          <p class="subheading">浏览器</p>
          <div v-for="item in client.browsers" :key="`b-${item.name}`" class="simple-item">
            <span>{{ item.name }}</span><strong>{{ item.count }}</strong>
          </div>
        </div>
      </article>
    </section>

    <section class="admin-table-wrap">
      <table class="admin-table">
        <thead>
          <tr>
            <th>访客ID</th>
            <th>访问路径</th>
            <th>IP</th>
            <th>设备 / 浏览器</th>
            <th>最后活跃</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in visitors.items" :key="item.id">
            <td>{{ item.visitorId }}</td>
            <td>{{ item.path || '/' }}</td>
            <td>{{ item.ip || '-' }}</td>
            <td>{{ item.deviceType || 'other' }} / {{ item.browser || 'other' }}</td>
            <td>{{ formatTime(item.updatedAt) }}</td>
          </tr>
        </tbody>
      </table>
      <div class="pagination-bar">
        <button class="admin-btn" :disabled="visitors.page <= 1 || loading" @click="changePage(visitors.page - 1)">上一页</button>
        <span>第 {{ visitors.page }} 页</span>
        <button class="admin-btn" :disabled="visitors.page * visitors.pageSize >= visitors.total || loading" @click="changePage(visitors.page + 1)">下一页</button>
      </div>
    </section>

    <div v-if="error" class="admin-error">
      <p>{{ error }}</p>
      <button class="admin-btn" @click="loadAll">重试</button>
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import {
  getAdminAnalyticsClientDistribution,
  getAdminAnalyticsOverview,
  getAdminAnalyticsSources,
  getAdminAnalyticsTopPages,
  getAdminAnalyticsTrend,
  getAdminAnalyticsVisitors
} from '@/api/analytics.js'

const loading = ref(false)
const error = ref('')
const range = ref('7d')
const overview = reactive({
  todayPv: 0,
  todayUv: 0,
  yesterdayPv: 0,
  yesterdayUv: 0,
  totalPv: 0,
  totalUv: 0,
  onlineUsers: 0
})
const trend = reactive({ points: [] })
const topPages = reactive({ items: [] })
const sources = reactive({ items: [] })
const client = reactive({ devices: [], browsers: [], os: [] })
const visitors = reactive({ total: 0, page: 1, pageSize: 20, items: [] })

function assign(target, patch, fallback = {}) {
  Object.assign(target, fallback, patch || {})
}

async function loadAll() {
  loading.value = true
  error.value = ''
  try {
    const [overviewRes, trendRes, topPageRes, sourceRes, clientRes, visitorRes] = await Promise.all([
      getAdminAnalyticsOverview(),
      getAdminAnalyticsTrend(range.value),
      getAdminAnalyticsTopPages(range.value),
      getAdminAnalyticsSources(range.value),
      getAdminAnalyticsClientDistribution(range.value),
      getAdminAnalyticsVisitors(visitors.page, visitors.pageSize)
    ])
    assign(overview, overviewRes)
    assign(trend, trendRes, { points: [] })
    assign(topPages, topPageRes, { items: [] })
    assign(sources, sourceRes, { items: [] })
    assign(client, clientRes, { devices: [], browsers: [], os: [] })
    assign(visitors, visitorRes, { items: [] })
  } catch (e) {
    error.value = e.message || '访客分析加载失败'
  } finally {
    loading.value = false
  }
}

function changePage(nextPage) {
  visitors.page = Math.max(1, nextPage)
  loadAll()
}

function formatTime(value) {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN', { hour12: false })
}

watch(range, () => {
  visitors.page = 1
  loadAll()
})

onMounted(loadAll)
</script>

<style scoped>
.analytics-page { display: grid; gap: 14px; }
.analytics-hero { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
.analytics-actions { display: flex; gap: 8px; align-items: center; }
.analytics-kpi-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 12px; }
.kpi-card { padding: 16px; }
.kpi-label { margin: 0; font-size: 12px; color: var(--lc-subtle); }
.kpi-value { margin: 8px 0; font-size: 30px; font-weight: 900; color: var(--lc-text); }
.kpi-meta { margin: 0; color: var(--lc-muted); font-size: 12px; }
.analytics-main-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px; }
.analytics-panel { padding: 16px; }
.analytics-panel h3 { margin: 0 0 12px; font-size: 18px; color: var(--lc-text); }
.trend-list, .rank-list, .simple-list { display: grid; gap: 8px; }
.trend-item, .rank-item, .simple-item {
  display: flex; justify-content: space-between; align-items: center; gap: 8px;
  border: 1px solid var(--lc-border); border-radius: 10px; padding: 10px 12px; background: var(--lc-surface);
}
.rank-index {
  width: 22px; height: 22px; border-radius: 50%; background: var(--lc-soft);
  display: inline-flex; justify-content: center; align-items: center; font-size: 12px;
}
.rank-title { flex: 1; min-width: 0; }
.subheading { margin: 6px 0 0; font-size: 12px; color: var(--lc-subtle); font-weight: 700; }
.empty-text { margin: 8px 0 0; color: var(--lc-subtle); }
.pagination-bar {
  display: flex; justify-content: flex-end; align-items: center; gap: 10px;
  padding: 10px 14px; border-top: 1px solid var(--lc-border);
}
@media (max-width: 1023px) {
  .analytics-hero { flex-direction: column; align-items: flex-start; }
  .analytics-kpi-grid, .analytics-main-grid { grid-template-columns: 1fr; }
}
</style>
