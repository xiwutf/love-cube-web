<template>
  <section class="help-my">
    <header class="page-head">
      <h1>我的互助</h1>
      <router-link :to="helpPath()" class="back">← 返回广场</router-link>
    </header>

    <section v-if="userStore.isLoggedIn" class="stats-card">
      <h2>我的互助数据</h2>
      <p v-if="statsLoading" class="muted">加载统计中…</p>
      <p v-else-if="statsErr" class="err">{{ statsErr }}</p>
      <dl v-else class="stats-grid">
        <div>
          <dt>帮助次数</dt>
          <dd>{{ stats.helpCount ?? 0 }}</dd>
          <span class="hint">我发起的「我能帮忙」次数</span>
        </div>
        <div>
          <dt>被接受次数</dt>
          <dd>{{ stats.acceptedCount ?? 0 }}</dd>
          <span class="hint">发布者确认互助意向的次数</span>
        </div>
        <div>
          <dt>成功帮助次数</dt>
          <dd>{{ stats.successCount ?? 0 }}</dd>
          <span class="hint">被标记为最终帮助人并解决需求</span>
        </div>
      </dl>
    </section>

    <section class="block">
      <h2>我发布的</h2>
      <p v-if="loadA" class="muted">加载中…</p>
      <p v-else-if="errA" class="err">{{ errA }}</p>
      <ul v-else class="rows authored">
        <li v-for="row in authored" :key="row.id">
          <div class="row-main">
            <router-link :to="helpPath(String(row.id))" class="row-title">{{ row.title }}</router-link>
            <span class="pill">{{ typeLabel(row.helpType) }}</span>
            <span :class="['st', row.status]">{{ statusLabel(row.status) }}</span>
          </div>
          <div class="row-sub muted sm">
            回应 {{ row.replyCount ?? 0 }} 人
            <template v-if="row.isResolved"> · 已解决</template>
            <template v-else-if="row.status === 'active'"> · 未解决</template>
            <template v-if="row.helperName"> · 最终帮助人：{{ row.helperName }}</template>
            <span> · {{ formatTime(row.createdAt) }}</span>
          </div>
        </li>
      </ul>
      <p v-if="!loadA && !authored.length" class="muted">暂无发布记录</p>
    </section>

    <section class="block">
      <h2>我回应的</h2>
      <p v-if="loadB" class="muted">加载中…</p>
      <p v-else-if="errB" class="err">{{ errB }}</p>
      <ul v-else class="rows">
        <li v-for="(item, idx) in replied" :key="idx">
          <div class="row-main">
            <router-link :to="helpPath(String(item.request?.id))" class="row-title">{{ item.request?.title }}</router-link>
            <span class="pill accent">{{ myReplyOutcome(item) }}</span>
          </div>
          <div class="row-sub muted sm">{{ formatTime(item.reply?.createdAt) }}</div>
        </li>
      </ul>
      <p v-if="!loadB && !replied.length" class="muted">暂无互助意向</p>
    </section>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { fetchHelpUserStats, fetchMyHelpAuthored, fetchMyHelpReplied } from '@/api/help.js'
import { usePlatformPath } from '@/composables/usePlatformPath.js'
import { useUserStore } from '@/stores/user.js'

const { helpPath } = usePlatformPath()
const userStore = useUserStore()

const authored = ref([])
const replied = ref([])
const loadA = ref(true)
const loadB = ref(true)
const errA = ref('')
const errB = ref('')

const stats = reactive({ helpCount: 0, acceptedCount: 0, successCount: 0 })
const statsLoading = ref(true)
const statsErr = ref('')

const TYPE_LABELS = {
  JOB_SEEK: '找工作',
  RECRUIT: '招人',
  FIND_MATERIAL: '找资料',
  OFFER_RESOURCE: '提供资源',
  ASK_EXP: '求经验',
  OTHER: '其他'
}

function typeLabel(c) {
  return TYPE_LABELS[c] || c
}

function statusLabel(s) {
  const m = {
    pending: '审核中',
    active: '进行中',
    resolved: '已解决',
    closed: '已关闭',
    rejected: '未通过审核'
  }
  return m[s] || s
}

/** 我回应的：意向结果文案 */
function myReplyOutcome(item) {
  const rep = item?.reply
  if (!rep) return '—'
  if (rep.isHelper) return '已成为最终帮助人'
  if (rep.status === 'pending') return '待确认'
  if (rep.status === 'accepted') return '已接受'
  if (rep.status === 'rejected') return '已拒绝'
  return rep.statusLabel || rep.status
}

function formatTime(iso) {
  if (!iso) return ''
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return iso
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

async function loadStats() {
  if (!userStore.isLoggedIn || !userStore.userId) {
    statsLoading.value = false
    return
  }
  statsLoading.value = true
  statsErr.value = ''
  try {
    const s = await fetchHelpUserStats(userStore.userId)
    stats.helpCount = s?.helpCount ?? 0
    stats.acceptedCount = s?.acceptedCount ?? 0
    stats.successCount = s?.successCount ?? 0
  } catch (e) {
    statsErr.value = e.message || '统计加载失败'
  } finally {
    statsLoading.value = false
  }
}

async function load() {
  loadA.value = true
  loadB.value = true
  errA.value = ''
  errB.value = ''
  try {
    const a = await fetchMyHelpAuthored({ pageNum: 1, pageSize: 50 })
    authored.value = Array.isArray(a?.list) ? a.list : []
  } catch (e) {
    errA.value = e.message || '加载失败'
  } finally {
    loadA.value = false
  }
  try {
    const b = await fetchMyHelpReplied({ pageNum: 1, pageSize: 50 })
    replied.value = Array.isArray(b?.list) ? b.list : []
  } catch (e) {
    errB.value = e.message || '加载失败'
  } finally {
    loadB.value = false
  }
}

onMounted(async () => {
  await userStore.syncCurrentUser()
  await loadStats()
  await load()
})
</script>

<style scoped>
.help-my {
  max-width: 800px;
  margin: 0 auto;
  padding: 1.5rem 1rem 3rem;
  color: var(--lc-text);
}

.page-head {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  justify-content: space-between;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
}

.page-head h1 {
  margin: 0;
  font-size: 1.5rem;
}

.back {
  color: var(--lc-blue);
  text-decoration: none;
  font-weight: 600;
}

.stats-card {
  margin-bottom: 1.5rem;
  padding: 1rem 1.25rem;
  border-radius: 12px;
  border: 1px solid var(--lc-blue-border);
  background: var(--lc-blue-light);
}

.stats-card h2 {
  margin: 0 0 0.75rem;
  font-size: 1.05rem;
  color: var(--lc-blue-dark);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 1rem;
  margin: 0;
}

.stats-grid div {
  background: var(--lc-surface);
  border-radius: 8px;
  padding: 0.65rem 0.75rem;
  border: 1px solid var(--lc-border);
}

.stats-grid dt {
  margin: 0;
  font-size: 0.78rem;
  color: var(--lc-muted);
  font-weight: 600;
}

.stats-grid dd {
  margin: 0.25rem 0 0;
  font-size: 1.35rem;
  font-weight: 700;
  color: var(--lc-blue);
}

.stats-grid .hint {
  display: block;
  margin-top: 0.35rem;
  font-size: 0.72rem;
  color: var(--lc-subtle);
  line-height: 1.35;
}

.block {
  margin-bottom: 2rem;
  padding: 1rem 1.25rem;
  border-radius: 12px;
  border: 1px solid var(--lc-border);
  background: var(--lc-surface);
}

.block h2 {
  margin: 0 0 0.75rem;
  font-size: 1.1rem;
}

.rows {
  list-style: none;
  margin: 0;
  padding: 0;
}

.rows li {
  padding: 0.65rem 0;
  border-bottom: 1px solid var(--lc-border);
}

.rows li:last-child {
  border-bottom: none;
}

.row-main {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.5rem 0.75rem;
}

.row-sub {
  margin-top: 0.35rem;
}

.row-title {
  font-weight: 600;
  color: var(--lc-blue);
  text-decoration: none;
}

.pill {
  font-size: 0.75rem;
  padding: 0.15rem 0.45rem;
  border-radius: 6px;
  background: var(--lc-soft);
  color: var(--lc-muted);
}

.pill.accent {
  background: var(--lc-indigo-light);
  color: var(--lc-indigo);
  font-weight: 600;
}

.st.resolved {
  color: var(--lc-emerald);
  font-weight: 600;
}

.muted {
  color: var(--lc-muted);
}

.muted.sm {
  font-size: 0.8rem;
}

.err {
  color: var(--lc-red);
}
</style>
