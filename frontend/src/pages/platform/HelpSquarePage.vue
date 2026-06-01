<template>
  <section class="help-square">
    <header class="hero">
      <div class="hero-text">
        <p class="hero-kicker">Help Hub</p>
        <h1>互助广场</h1>
        <p class="hero-desc">需求 → 互助 → 解决 → 信任沉淀。成功标准是「有多少需求被真正解决」。</p>
        <div class="hero-metrics">
          <span class="metric-chip">当前需求 {{ items.length }}</span>
          <span class="metric-chip">总回应 {{ totalReplies }}</span>
          <span class="metric-chip">高热度 {{ hotCount }}</span>
        </div>
        <div class="hero-actions">
          <router-link v-if="userStore.isLoggedIn" :to="helpPath('create')" class="btn primary">发布需求</router-link>
          <router-link v-else to="/login" class="btn primary">登录后发布</router-link>
          <router-link :to="helpPath('my')" class="btn ghost">我的互助</router-link>
        </div>
      </div>
    </header>

    <div class="toolbar">
      <nav class="type-tabs" aria-label="分类">
        <button
          v-for="t in typeFilters"
          :key="t.value || 'ALL'"
          type="button"
          :class="['tab', { active: filterType === t.value }]"
          @click="filterType = t.value; load(1)"
        >
          {{ t.label }}
        </button>
      </nav>
    </div>

    <section v-if="leaderboard.length" class="leaderboard-panel platform-card">
      <h2>互助达人榜</h2>
      <p class="lb-sub">信用分 = 回应×1 + 被接受×3 + 成功解决×10</p>
      <ol class="lb-list">
        <li v-for="row in leaderboard" :key="row.userId">
          <span class="lb-rank">#{{ row.rank }}</span>
          <span class="lb-name">{{ row.nickname }}</span>
          <span class="lb-score">{{ row.creditScore }} 分</span>
          <span class="lb-meta">成功 {{ row.successCount }} 次</span>
        </li>
      </ol>
    </section>

    <p v-if="error" class="banner error">{{ error }}</p>
    <p v-else-if="loading" class="banner">加载中…</p>

    <div v-else class="list-wrap">
      <!-- 桌面：表格式列表 -->
      <table class="help-table desktop-only" aria-label="互助需求列表">
        <thead>
          <tr>
            <th>类型</th>
            <th>标题 / 简述</th>
            <th>发布人</th>
            <th>地区</th>
            <th>状态</th>
            <th>回应</th>
            <th>时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in items" :key="row.id">
            <td><span class="pill">{{ typeLabel(row.helpType) }}</span></td>
            <td>
              <div class="title-cell">
                <strong>{{ row.title }}</strong>
                <span class="sum">{{ row.summary }}</span>
              </div>
            </td>
            <td>{{ row.publisherName }}</td>
            <td>{{ row.region || '—' }}</td>
            <td><span class="status">进行中</span></td>
            <td>{{ row.replyCount ?? 0 }}</td>
            <td class="nowrap">{{ formatTime(row.createdAt) }}</td>
            <td class="actions">
              <router-link class="link" :to="helpPath(String(row.id))">查看详情</router-link>
              <router-link
                v-if="canOffer(row)"
                class="link accent"
                :to="`${helpPath(String(row.id))}?offer=1`"
              >我能帮忙</router-link>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- 移动端：卡片 -->
      <div class="card-list mobile-only">
        <article v-for="row in items" :key="row.id" class="card">
          <div class="card-top">
            <span class="pill">{{ typeLabel(row.helpType) }}</span>
            <span class="status">进行中</span>
          </div>
          <h2 class="card-title">{{ row.title }}</h2>
          <p class="card-sum">{{ row.summary }}</p>
          <p class="card-meta">{{ row.publisherName }} · {{ row.region || '地区未填' }} · {{ formatTime(row.createdAt) }}</p>
          <p class="card-meta">回应 {{ row.replyCount ?? 0 }} 人</p>
          <div class="card-actions">
            <router-link class="btn sm ghost" :to="helpPath(String(row.id))">查看详情</router-link>
            <router-link
              v-if="canOffer(row)"
              class="btn sm primary"
              :to="`${helpPath(String(row.id))}?offer=1`"
            >我能帮忙</router-link>
          </div>
        </article>
      </div>

      <p v-if="!items.length" class="empty">暂无已上架的互助需求，欢迎发布第一条。</p>

      <div v-if="totalPages > 1" class="pager">
        <button type="button" :disabled="page <= 1" @click="goPage(page - 1)">上一页</button>
        <span>第 {{ page }} / {{ totalPages }} 页</span>
        <button type="button" :disabled="page >= totalPages" @click="goPage(page + 1)">下一页</button>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { fetchHelpLeaderboard, fetchHelpRequests } from '@/api/help.js'
import { usePlatformPath } from '@/composables/usePlatformPath.js'
import { useUserStore } from '@/stores/user.js'

const { helpPath } = usePlatformPath()
const userStore = useUserStore()
const loading = ref(true)
const error = ref('')
const items = ref([])
const page = ref(1)
const totalPages = ref(1)
const filterType = ref('')
const leaderboard = ref([])

const typeFilters = [
  { value: '', label: '全部' },
  { value: 'JOB_SEEK', label: '找工作' },
  { value: 'RECRUIT', label: '招人' },
  { value: 'FIND_MATERIAL', label: '找资料' },
  { value: 'OFFER_RESOURCE', label: '提供资源' },
  { value: 'ASK_EXP', label: '求经验' },
  { value: 'OTHER', label: '其他' }
]

const totalReplies = computed(() => items.value.reduce((sum, row) => sum + Number(row.replyCount || 0), 0))
const hotCount = computed(() => items.value.filter(row => Number(row.replyCount || 0) >= 3).length)

const TYPE_LABELS = {
  JOB_SEEK: '找工作',
  RECRUIT: '招人',
  FIND_MATERIAL: '找资料',
  OFFER_RESOURCE: '提供资源',
  ASK_EXP: '求经验',
  OTHER: '其他'
}

function typeLabel(code) {
  return TYPE_LABELS[code] || code
}

function formatTime(iso) {
  if (!iso) return '—'
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return iso
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function canOffer(row) {
  if (!userStore.isLoggedIn) return true
  return String(row.userId) !== String(userStore.userId)
}

async function load(p) {
  loading.value = true
  error.value = ''
  page.value = p || page.value
  try {
    const res = await fetchHelpRequests({
      helpType: filterType.value || undefined,
      pageNum: page.value,
      pageSize: 15
    })
    items.value = Array.isArray(res?.list) ? res.list : []
    totalPages.value = Number(res?.totalPages) || 1
  } catch (e) {
    error.value = e.message || '加载失败'
    items.value = []
  } finally {
    loading.value = false
  }
}

function goPage(p) {
  page.value = p
  load(p)
}

onMounted(async () => {
  load(1)
  try {
    const res = await fetchHelpLeaderboard({ limit: 8 })
    leaderboard.value = Array.isArray(res?.items) ? res.items : []
  } catch {
    leaderboard.value = []
  }
})
</script>

<style scoped>
.help-square {
  width: calc(100% - 48px);
  margin: 0 auto;
  padding: 1.5rem 0 3rem;
  color: var(--lc-text);
}

.hero {
  position: relative;
  overflow: hidden;
  margin-bottom: 1.5rem;
  padding: 1.5rem;
  border-radius: 16px;
  background: linear-gradient(120deg, color-mix(in srgb, var(--lc-blue-light) 82%, #ffffff 18%), var(--lc-surface));
  border: 1px solid var(--lc-blue-border);
  box-shadow: 0 14px 36px -24px color-mix(in srgb, var(--lc-blue) 36%, transparent);
}

.hero::after {
  content: '';
  position: absolute;
  right: -60px;
  top: -72px;
  width: 220px;
  height: 220px;
  border-radius: 50%;
  background: radial-gradient(circle, color-mix(in srgb, var(--lc-indigo) 34%, transparent), transparent 72%);
  pointer-events: none;
}

.hero-kicker {
  display: inline-flex;
  margin: 0 0 0.5rem;
  padding: 0.2rem 0.65rem;
  border-radius: 999px;
  font-size: 0.74rem;
  font-weight: 700;
  color: var(--lc-indigo);
  background: color-mix(in srgb, var(--lc-indigo-light) 66%, #fff 34%);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.hero-text h1 {
  margin: 0 0 0.5rem;
  font-size: 1.75rem;
  color: var(--lc-blue-dark);
}

.hero-desc {
  margin: 0 0 1rem;
  color: var(--lc-muted);
  line-height: 1.6;
  max-width: 52rem;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.hero-metrics {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 0.95rem;
}

.metric-chip {
  display: inline-flex;
  align-items: center;
  padding: 0.32rem 0.62rem;
  border-radius: 999px;
  border: 1px solid color-mix(in srgb, var(--lc-blue-border) 82%, #fff 18%);
  background: color-mix(in srgb, var(--lc-surface) 80%, var(--lc-blue-light) 20%);
  color: color-mix(in srgb, var(--lc-text) 74%, var(--lc-blue) 26%);
  font-size: 0.78rem;
  font-weight: 600;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.55rem 1.1rem;
  border-radius: 8px;
  font-weight: 600;
  text-decoration: none;
  border: 1px solid transparent;
  cursor: pointer;
  font-size: 0.95rem;
}

.btn.primary {
  background: var(--lc-blue);
  color: #fff;
}

.btn.ghost {
  background: var(--lc-surface);
  color: var(--lc-blue);
  border-color: var(--lc-blue-border);
}

.btn.sm {
  padding: 0.4rem 0.75rem;
  font-size: 0.85rem;
}

.toolbar {
  margin-bottom: 1rem;
}

.type-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.tab {
  border: 1px solid var(--lc-border);
  background: var(--lc-surface);
  color: var(--lc-muted);
  padding: 0.35rem 0.75rem;
  border-radius: 999px;
  cursor: pointer;
  font-size: 0.875rem;
}

.tab.active {
  border-color: var(--lc-blue);
  color: var(--lc-blue);
  background: var(--lc-blue-light);
}

.banner {
  padding: 0.75rem 1rem;
  border-radius: 8px;
  background: var(--lc-soft);
  color: var(--lc-muted);
}

.banner.error {
  background: #FEF2F2;
  color: var(--lc-red);
}

.help-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
  background: var(--lc-surface);
  border: 1px solid var(--lc-border);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 16px 28px -26px color-mix(in srgb, var(--lc-text) 40%, transparent);
}

.help-table th,
.help-table td {
  padding: 0.65rem 0.75rem;
  text-align: left;
  border-bottom: 1px solid var(--lc-border);
  vertical-align: top;
}

.help-table th {
  background: var(--lc-soft);
  color: var(--lc-muted);
  font-weight: 600;
}

.title-cell strong {
  display: block;
  color: var(--lc-text);
}

.sum {
  display: block;
  margin-top: 0.25rem;
  color: var(--lc-subtle);
  font-size: 0.8rem;
  line-height: 1.4;
}

.pill {
  display: inline-block;
  padding: 0.15rem 0.5rem;
  border-radius: 6px;
  background: var(--lc-indigo-light);
  color: var(--lc-indigo);
  font-size: 0.75rem;
  font-weight: 600;
}

.status {
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--lc-emerald);
}

.nowrap {
  white-space: nowrap;
  color: var(--lc-muted);
  font-size: 0.85rem;
}

.actions {
  white-space: nowrap;
}

.actions .link {
  margin-right: 0.5rem;
  color: var(--lc-blue);
  text-decoration: none;
  font-weight: 600;
}

.actions .link.accent {
  color: var(--lc-indigo);
}

.card-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.card {
  padding: 1rem;
  border-radius: 14px;
  border: 1px solid var(--lc-border);
  background: var(--lc-surface);
  box-shadow: 0 12px 24px -24px color-mix(in srgb, var(--lc-text) 40%, transparent);
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.card-title {
  margin: 0 0 0.35rem;
  font-size: 1.05rem;
}

.card-sum {
  margin: 0 0 0.5rem;
  color: var(--lc-muted);
  font-size: 0.9rem;
  line-height: 1.5;
}

.card-meta {
  margin: 0.15rem 0;
  font-size: 0.8rem;
  color: var(--lc-subtle);
}

.card-actions {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.75rem;
}

.empty {
  text-align: center;
  color: var(--lc-muted);
  padding: 2rem 1rem;
}

.pager {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  margin-top: 1.5rem;
}

.pager button {
  padding: 0.4rem 0.9rem;
  border-radius: 8px;
  border: 1px solid var(--lc-border);
  background: var(--lc-surface);
  cursor: pointer;
}

.desktop-only {
  display: none;
}

.mobile-only {
  display: block;
}

@media (min-width: 900px) {
  .desktop-only {
    display: table;
  }

  .mobile-only {
    display: none;
  }
}

.leaderboard-panel {
  padding: 16px 18px;
  margin-bottom: 12px;
}

.leaderboard-panel h2 {
  margin: 0 0 6px;
  font-size: 17px;
}

.lb-sub {
  margin: 0 0 12px;
  font-size: 12px;
  color: var(--lc-subtle);
}

.lb-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.lb-list li {
  display: grid;
  grid-template-columns: 36px 1fr auto auto;
  gap: 8px;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid var(--lc-soft);
  font-size: 13px;
}

.lb-rank {
  font-weight: 800;
  color: var(--lc-indigo);
}

.lb-score {
  font-weight: 700;
  color: var(--lc-emerald);
}

.lb-meta {
  font-size: 11px;
  color: var(--lc-subtle);
}

@media (min-width: 768px) and (max-width: 1199px) {
  .help-square {
    width: calc(100% - 32px);
  }
}

@media (max-width: 900px) {

  .hero {
    padding: 1.1rem 1rem;
  }
}

@media (max-width: 767px) {
  .help-square {
    width: calc(100% - 24px);
  }
}
</style>
