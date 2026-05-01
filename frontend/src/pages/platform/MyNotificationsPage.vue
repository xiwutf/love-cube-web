<template>
  <section class="sp-page">
    <header class="sp-head">
      <button type="button" class="sp-back" aria-label="返回" @click="$router.back()">‹</button>
      <h1 class="sp-title">消息通知</h1>
      <button v-if="unreadCount > 0" type="button" class="sp-action-btn" @click="handleReadAll">
        全部已读
      </button>
    </header>

    <div class="sp-body">
      <!-- 筛选标签 -->
      <div class="nt-tab-row">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          type="button"
          class="nt-tab"
          :class="{ active: activeTab === tab.value }"
          @click="switchTab(tab.value)"
        >{{ tab.label }}</button>
      </div>

      <p v-if="loading" class="sp-status">加载中...</p>
      <p v-else-if="!items.length" class="sp-status sp-empty">暂无通知消息</p>
      <template v-else>
        <div
          v-for="item in items"
          :key="item.id"
          class="sp-card nt-card"
          :class="{ unread: !item.read }"
          @click="handleRead(item)"
        >
          <div class="nt-dot" v-if="!item.read"></div>
          <div class="nt-type-icon">{{ typeIcon(item.type) }}</div>
          <div class="nt-body">
            <div class="nt-title">{{ item.title || item.subject || item.content?.slice(0, 30) }}</div>
            <div class="nt-content">{{ item.content }}</div>
            <div class="nt-time">{{ formatDate(item.createdAt) }}</div>
          </div>
        </div>
      </template>

      <div v-if="hasMore && !loading" class="sp-more">
        <button type="button" class="sp-more-btn" :disabled="loadingMore" @click="loadMore">
          {{ loadingMore ? '加载中...' : '加载更多' }}
        </button>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getNotifications, getNotificationsByType, markNotifRead, markAllNotifRead, getNotifUnreadCountCached } from '@/api/notification.js'

const loading = ref(false)
const loadingMore = ref(false)
const items = ref([])
const hasMore = ref(false)
const pageOffset = ref(0)
const PAGE_SIZE = 15
const activeTab = ref('all')
const unreadCount = ref(0)

const tabs = [
  { value: 'all', label: '全部' },
  { value: 'system', label: '系统' },
  { value: 'review', label: '审核' },
  { value: 'interaction', label: '互动' }
]

function typeIcon(type) {
  const map = { system: '🔔', review: '📋', interaction: '💬', like: '👍', comment: '💬', follow: '👤' }
  return map[String(type).toLowerCase()] || '📩'
}

function formatDate(raw) {
  if (!raw) return ''
  const d = new Date(raw)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  return String(raw).replace('T', ' ').slice(0, 16)
}

async function fetchList(append = false) {
  if (!append) { loading.value = true; pageOffset.value = 0 }
  else loadingMore.value = true
  try {
    const limit = PAGE_SIZE
    const res = activeTab.value === 'all'
      ? await getNotifications(limit)
      : await getNotificationsByType(activeTab.value, limit)
    const list = Array.isArray(res?.list) ? res.list : (Array.isArray(res) ? res : [])
    items.value = append ? items.value.concat(list) : list
    hasMore.value = list.length >= PAGE_SIZE
  } catch {
    if (!append) items.value = []
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

async function loadMore() {
  pageOffset.value += PAGE_SIZE
  await fetchList(true)
}

function switchTab(tab) {
  activeTab.value = tab
  fetchList()
}

async function handleRead(item) {
  if (item.read) return
  item.read = true
  if (unreadCount.value > 0) unreadCount.value--
  await markNotifRead(item.id).catch(() => {})
}

async function handleReadAll() {
  items.value.forEach(i => { i.read = true })
  unreadCount.value = 0
  window.dispatchEvent(new CustomEvent('platform-notif-read-all'))
  await markAllNotifRead().catch(() => {})
}

onMounted(async () => {
  await fetchList()
  const res = await getNotifUnreadCountCached().catch(() => null)
  unreadCount.value = Number(res?.count ?? res?.unreadCount ?? 0)
})
</script>

<style scoped>
.sp-page { min-height: 100vh; background: var(--lc-bg); color: var(--lc-text); }

.sp-head {
  position: sticky; top: 0; z-index: 10;
  display: flex; align-items: center;
  background: var(--lc-surface); border-bottom: 1px solid var(--lc-soft-alt);
}
.sp-back {
  width: 48px; height: 52px; flex: 0 0 auto;
  display: grid; place-items: center;
  border: 0; background: none; font-size: 22px; color: var(--lc-indigo); cursor: pointer;
}
.sp-title { flex: 1; margin: 0; font-size: 17px; font-weight: 800; }
.sp-action-btn {
  padding: 0 14px; height: 52px;
  border: 0; background: none; font-size: 13px; color: var(--lc-indigo); font-weight: 700; cursor: pointer;
  white-space: nowrap;
}

.sp-body {
  padding: 12px 14px calc(80px + env(safe-area-inset-bottom));
  max-width: 680px; margin: 0 auto;
}
.sp-status { text-align: center; padding: 40px 0; color: var(--lc-subtle); font-size: 14px; }
.sp-empty::before { display: block; font-size: 32px; margin-bottom: 10px; content: "🔔"; }

.nt-tab-row {
  display: flex; gap: 6px; margin-bottom: 14px; overflow-x: auto; padding-bottom: 2px;
  scrollbar-width: none;
}
.nt-tab-row::-webkit-scrollbar { display: none; }
.nt-tab {
  flex: 0 0 auto; border: 1.5px solid var(--lc-soft-alt); border-radius: 999px;
  background: var(--lc-surface); color: var(--lc-muted-light); font-size: 13px; padding: 6px 14px; cursor: pointer;
  transition: all 0.15s;
}
.nt-tab.active { border-color: var(--lc-violet); background: var(--lc-indigo-soft); color: var(--lc-indigo); font-weight: 700; }

.sp-card {
  background: var(--lc-surface); border: 1px solid var(--lc-soft-alt); border-radius: 16px;
  box-shadow: 0 3px 12px rgba(15,23,42,0.04); margin-bottom: 10px; padding: 14px 16px;
}

.nt-card {
  position: relative; display: flex; align-items: flex-start; gap: 12px; cursor: pointer;
}
.nt-card.unread { background: #f9f8ff; border-color: var(--lc-indigo-soft); }

.nt-dot {
  position: absolute; top: 14px; right: 14px;
  width: 8px; height: 8px; border-radius: 50%; background: var(--lc-violet);
}

.nt-type-icon { font-size: 22px; flex: 0 0 auto; margin-top: 1px; }

.nt-body { flex: 1; min-width: 0; }
.nt-title { font-size: 14px; font-weight: 700; color: var(--lc-text); margin-bottom: 4px; }
.nt-content { font-size: 13px; color: var(--lc-muted); line-height: 1.5; margin-bottom: 6px; }
.nt-time { font-size: 11px; color: var(--lc-subtle); }

.sp-more { text-align: center; margin-top: 8px; }
.sp-more-btn {
  border: 1px solid var(--lc-soft-alt); border-radius: 999px;
  background: var(--lc-surface); color: var(--lc-muted-light); font-size: 13px; padding: 8px 24px; cursor: pointer;
}
.sp-more-btn:disabled { opacity: 0.5; }

@media (max-width: 767px) {
  :global(.platform-header), :global(.platform-footer), :global(.co-creation-toolbar) {
    display: none !important;
  }
}
@media (min-width: 768px) {
  .sp-body { padding-top: 20px; padding-bottom: 48px; }
}
</style>
