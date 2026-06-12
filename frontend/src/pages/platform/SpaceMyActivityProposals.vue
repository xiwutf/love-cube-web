<template>
  <section class="my-proposals-page">
    <header class="page-head">
      <router-link class="back-link" :to="groupsPath(groupId)">← 返回团体</router-link>
      <h1>我的活动投稿</h1>
      <p class="subtitle">查看投稿审核状态与运营者反馈</p>
    </header>

    <div v-if="loading" class="state-card">加载中…</div>
    <div v-else-if="error" class="state-card err">
      <p>{{ error }}</p>
      <button type="button" class="btn primary" @click="load">重试</button>
    </div>

    <template v-else>
      <div class="filter-row">
        <button
          v-for="opt in filters"
          :key="opt.key"
          type="button"
          :class="{ active: activeFilter === opt.key }"
          @click="activeFilter = opt.key"
        >
          {{ opt.label }}
          <span v-if="opt.key !== 'all'" class="count">{{ countByFilter(opt.key) }}</span>
        </button>
      </div>

      <div v-if="filteredItems.length" class="proposal-list">
        <article v-for="item in filteredItems" :key="item.id" class="proposal-card platform-card">
          <header>
            <strong>{{ item.title }}</strong>
            <span class="status-badge" :class="statusTone(item.status)">{{ item.statusLabel || statusLabel(item.status) }}</span>
          </header>
          <p class="desc">{{ item.description || '暂无简介' }}</p>
          <p class="meta">
            {{ formatDate(item.startTime) }} — {{ formatDate(item.endTime) }}
            <span v-if="item.location"> · {{ item.location }}</span>
          </p>
          <p v-if="item.reviewComment && item.status === 'rejected'" class="review-note">
            审核意见：{{ item.reviewComment }}
          </p>
          <p class="meta muted">投稿于 {{ formatDate(item.createdAt) }}</p>
        </article>
      </div>
      <p v-else class="state-card">暂无{{ activeFilter === 'all' ? '' : '该状态的' }}投稿记录</p>
    </template>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { fetchMyActivityProposals } from '@/api/groupActivityProposals.js'
import { usePlatformPath } from '@/composables/usePlatformPath.js'

const route = useRoute()
const { groupsPath } = usePlatformPath()

const groupId = computed(() => String(route.params.id || ''))
const loading = ref(true)
const error = ref('')
const items = ref([])
const activeFilter = ref('all')

const filters = [
  { key: 'all', label: '全部' },
  { key: 'pending', label: '待审核' },
  { key: 'published', label: '已发布' },
  { key: 'rejected', label: '已拒绝' }
]

const filteredItems = computed(() => {
  if (activeFilter.value === 'all') return items.value
  return items.value.filter((item) => item.status === activeFilter.value)
})

function countByFilter(key) {
  if (key === 'all') return items.value.length
  return items.value.filter((item) => item.status === key).length
}

function statusLabel(status) {
  if (status === 'pending') return '待审核'
  if (status === 'rejected') return '已拒绝'
  if (status === 'published') return '已发布'
  return status
}

function statusTone(status) {
  if (status === 'pending') return 'warning'
  if (status === 'rejected') return 'danger'
  if (status === 'published') return 'success'
  return 'neutral'
}

function formatDate(v) {
  if (!v) return '—'
  const d = new Date(v)
  if (Number.isNaN(d.getTime())) return String(v)
  return d.toLocaleString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const res = await fetchMyActivityProposals(groupId.value)
    const data = res?.data ?? res
    items.value = data?.items ?? []
  } catch (err) {
    error.value = err?.message || '加载失败'
    items.value = []
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.my-proposals-page {
  max-width: 760px;
  margin: 0 auto;
  padding: var(--lc-space-6) var(--lc-space-4) var(--lc-space-8);
}

.page-head {
  margin-bottom: var(--lc-space-5);
}

.back-link {
  display: inline-block;
  margin-bottom: var(--lc-space-2);
  color: var(--lc-text-muted);
  text-decoration: none;
}

.page-head h1 {
  margin: 0 0 var(--lc-space-1);
}

.subtitle {
  margin: 0;
  color: var(--lc-text-muted);
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: var(--lc-space-2);
  margin-bottom: var(--lc-space-4);
}

.filter-row button {
  display: inline-flex;
  align-items: center;
  gap: var(--lc-space-1);
  padding: var(--lc-space-2) var(--lc-space-3);
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius-full);
  background: var(--lc-surface);
  cursor: pointer;
}

.filter-row button.active {
  border-color: var(--lc-blue);
  color: var(--lc-blue);
  background: var(--lc-blue-soft);
}

.count {
  font-size: var(--lc-text-xs);
  opacity: 0.8;
}

.proposal-list {
  display: flex;
  flex-direction: column;
  gap: var(--lc-space-3);
}

.proposal-card {
  padding: var(--lc-space-4);
}

.proposal-card header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--lc-space-3);
  margin-bottom: var(--lc-space-2);
}

.desc {
  margin: 0 0 var(--lc-space-2);
}

.meta {
  margin: 0;
  font-size: var(--lc-text-sm);
  color: var(--lc-text-muted);
}

.meta.muted {
  margin-top: var(--lc-space-2);
}

.review-note {
  margin: var(--lc-space-2) 0 0;
  padding: var(--lc-space-2) var(--lc-space-3);
  border-radius: var(--lc-radius-md);
  background: var(--lc-danger-soft, #fef2f2);
  color: var(--lc-danger);
  font-size: var(--lc-text-sm);
}

.state-card {
  padding: var(--lc-space-5);
  text-align: center;
  color: var(--lc-text-muted);
}

.state-card.err {
  color: var(--lc-danger);
}
</style>
