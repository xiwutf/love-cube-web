<template>
  <section class="admin-page">
    <section class="platform-card">
      <h1 class="platform-title">邀请记录</h1>
      <p class="platform-subtitle">按邀请人、被邀请人、邀请码和时间查询邀请注册记录</p>
    </section>

    <section class="platform-card filter-card" style="margin-top: 12px;">
      <div class="filter-grid">
        <input v-model="filters.inviterUserId" placeholder="邀请人 ID" />
        <input v-model="filters.inviteeUserId" placeholder="被邀请人ID" />
        <input v-model="filters.inviteCode" placeholder="邀请码" />
        <select v-model="filters.status">
          <option value="">全部状态</option>
          <option value="SUCCESS">SUCCESS</option>
          <option value="INVALID">INVALID</option>
          <option value="FAILED">FAILED</option>
          <option value="DISABLED">DISABLED</option>
        </select>
        <input v-model="filters.startTime" type="date" placeholder="开始日期" />
        <input v-model="filters.endTime" type="date" placeholder="结束日期" />
      </div>
      <div class="filter-actions">
        <button type="button" class="admin-btn" @click="loadInvites">查询</button>
        <button type="button" class="admin-btn" @click="resetFilters">重置</button>
      </div>
    </section>

    <section class="admin-table-wrap admin-desktop-only invites-table-wrap">
      <table class="admin-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>邀请码</th>
            <th>邀请人</th>
            <th>被邀请人</th>
            <th>状态</th>
            <th>注册 IP</th>
            <th>友好摘要</th>
            <th>设备信息</th>
            <th>注册时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in paginatedRows" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.inviteCode }}</td>
            <td>{{ item.inviterUserId }} / {{ item.inviterName || '-' }}</td>
            <td>{{ item.inviteeUserId || '-' }} / {{ item.inviteeName || '-' }}</td>
            <td>{{ item.status || '-' }}</td>
            <td>{{ item.registerIp || '-' }}</td>
            <td class="ua-summary">{{ summarizeRegisterUserAgent(item.registerUserAgent) }}</td>
            <td class="ua">
              <div class="ua-inline">
                <div
                  class="ua-body"
                  :title="item.registerUserAgent || undefined"
                >
                  {{ item.registerUserAgent || '-' }}
                </div>
                <button
                  v-if="item.registerUserAgent"
                  type="button"
                  class="ua-copy admin-btn ghost"
                  @click="copyUa(item.registerUserAgent)"
                >
                  复制
                </button>
              </div>
            </td>
            <td>{{ formatDate(item.createdAt) }}</td>
          </tr>
        </tbody>
      </table>
      <van-empty v-if="!loading && !rows.length" description="暂无邀请记录" />
    </section>

    <div class="admin-list admin-mobile-only">
      <article v-for="item in paginatedRows" :key="item.id" class="admin-row">
        <div class="admin-row-head">
          <strong>{{ item.inviteCode }}</strong>
          <span class="admin-tag active">{{ item.status || '-' }}</span>
        </div>
        <p class="admin-row-meta">邀请人：{{ item.inviterUserId }} / {{ item.inviterName || '-' }}</p>
        <p class="admin-row-meta">被邀请人：{{ item.inviteeUserId || '-' }} / {{ item.inviteeName || '-' }}</p>
        <p class="admin-row-meta">IP：{{ item.registerIp || '-' }}</p>
        <p class="admin-row-meta ua-friendly">
          <span class="ua-friendly-label">友好摘要</span>
          {{ summarizeRegisterUserAgent(item.registerUserAgent) }}
        </p>
        <p class="admin-row-meta ua-mobile-label">原始设备信息</p>
        <div
          class="ua-mobile"
          :title="item.registerUserAgent || undefined"
        >
          {{ item.registerUserAgent || '-' }}
        </div>
        <button
          v-if="item.registerUserAgent"
          type="button"
          class="admin-btn ghost ua-copy-mobile"
          @click="copyUa(item.registerUserAgent)"
        >
          复制设备信息
        </button>
        <p class="admin-row-meta">时间：{{ formatDate(item.createdAt) }}</p>
      </article>
      <van-empty v-if="!loading && !rows.length" description="暂无邀请记录" />
    </div>

    <section v-if="rows.length" class="platform-card invite-pagination-card">
      <div class="admin-pagination">
        <span class="total-count">共 {{ rows.length }} 条</span>
        <label class="admin-page-size">
          每页
          <select v-model.number="pageSize" class="admin-select">
            <option :value="10">10</option>
            <option :value="20">20</option>
            <option :value="50">50</option>
          </select>
          条
        </label>
        <div class="admin-page-actions">
          <button class="admin-btn" type="button" :disabled="currentPage <= 1" @click="goPrevPage">上一页</button>
          <span class="admin-page-indicator">{{ currentPage }} / {{ totalPages }}</span>
          <button class="admin-btn" type="button" :disabled="currentPage >= totalPages" @click="goNextPage">下一页</button>
        </div>
        <div class="jump-page">
          <span>跳至</span>
          <input v-model.number="jumpPageInput" type="number" min="1" :max="totalPages">
          <button class="admin-btn" type="button" @click="goToPage">确定</button>
        </div>
      </div>
    </section>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { showToast } from 'vant'
import { getAdminInvites } from '@/api/invite.js'
import { summarizeRegisterUserAgent } from '@/utils/userAgentSummary.js'

const loading = ref(false)
const rows = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const jumpPageInput = ref(1)
const filters = reactive({
  inviterUserId: '',
  inviteeUserId: '',
  inviteCode: '',
  startTime: '',
  endTime: '',
  status: ''
})

const totalPages = computed(() => Math.max(1, Math.ceil(rows.value.length / pageSize.value)))
const paginatedRows = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return rows.value.slice(start, end)
})

function buildParams() {
  const params = {}
  if (filters.inviterUserId) params.inviterUserId = Number(filters.inviterUserId)
  if (filters.inviteeUserId) params.inviteeUserId = Number(filters.inviteeUserId)
  if (filters.inviteCode) params.inviteCode = filters.inviteCode.trim()
  if (filters.startTime) params.startTime = filters.startTime
  if (filters.endTime) params.endTime = filters.endTime
  if (filters.status) params.status = filters.status
  return params
}

async function loadInvites() {
  loading.value = true
  try {
    const data = await getAdminInvites(buildParams())
    rows.value = Array.isArray(data) ? data : []
    currentPage.value = 1
    jumpPageInput.value = 1
  } catch (err) {
    showToast({ type: 'fail', message: err.message || '邀请记录加载失败' })
  } finally {
    loading.value = false
  }
}

function goPrevPage() {
  if (currentPage.value > 1) currentPage.value -= 1
}

function goNextPage() {
  if (currentPage.value < totalPages.value) currentPage.value += 1
}

function goToPage() {
  const target = Number(jumpPageInput.value)
  if (!Number.isFinite(target)) return
  currentPage.value = Math.min(totalPages.value, Math.max(1, Math.floor(target)))
  jumpPageInput.value = currentPage.value
}

function resetFilters() {
  filters.inviterUserId = ''
  filters.inviteeUserId = ''
  filters.inviteCode = ''
  filters.startTime = ''
  filters.endTime = ''
  filters.status = ''
  loadInvites()
}

function formatDate(value) {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN', { hour12: false })
}

async function copyUa(text) {
  const t = String(text || '').trim()
  if (!t) return
  try {
    await navigator.clipboard.writeText(t)
    showToast({ type: 'success', message: '设备信息已复制' })
  } catch {
    showToast({ type: 'fail', message: '复制失败，请手动选择文本' })
  }
}

onMounted(loadInvites)

watch(pageSize, () => {
  currentPage.value = 1
  jumpPageInput.value = 1
})

watch(totalPages, (pages) => {
  if (currentPage.value > pages) currentPage.value = pages
  if (jumpPageInput.value > pages) jumpPageInput.value = pages
})
</script>

<style scoped>
.filter-card {
  display: grid;
  gap: 10px;
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.filter-grid input,
.filter-grid select {
  border: 1px solid var(--lc-border);
  border-radius: 8px;
  padding: 8px 10px;
  font-size: 13px;
}

.filter-actions {
  display: flex;
  gap: 8px;
}

.invites-table-wrap .admin-table {
  min-width: 1240px;
}

.ua-summary {
  min-width: 200px;
  max-width: 300px;
  font-size: 13px;
  line-height: 1.45;
  color: var(--lc-text);
}

.ua-friendly {
  padding: 8px 10px;
  margin: 6px 0;
  border-radius: 8px;
  background: rgba(37, 99, 235, 0.06);
  border: 1px solid var(--lc-border);
  color: var(--lc-text);
  font-size: 13px;
  line-height: 1.5;
}

.ua-friendly-label {
  display: block;
  font-size: 11px;
  font-weight: 700;
  color: var(--lc-muted);
  margin-bottom: 4px;
}

.ua {
  min-width: 180px;
  max-width: 340px;
}

.ua-inline {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.ua-body {
  flex: 1;
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 12px;
  line-height: 1.45;
  font-family: ui-monospace, 'Cascadia Code', 'Consolas', monospace;
  color: var(--lc-text);
}

.ua-copy {
  flex-shrink: 0;
  margin-top: 0;
  padding: 4px 10px;
  font-size: 12px;
}

.admin-btn.ghost {
  background: transparent;
  border: 1px solid var(--lc-border);
  color: var(--lc-muted);
}

.ua-mobile-label {
  margin-bottom: 4px;
  font-weight: 600;
  color: var(--lc-muted);
}

.ua-mobile {
  margin: 0 0 8px;
  padding: 8px 10px;
  border-radius: 8px;
  background: var(--lc-soft);
  border: 1px solid var(--lc-border);
  font-size: 11px;
  line-height: 1.45;
  font-family: ui-monospace, 'Cascadia Code', 'Consolas', monospace;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: var(--lc-text);
}

.ua-copy-mobile {
  margin-bottom: 8px;
  padding: 6px 12px;
  font-size: 12px;
}

.invite-pagination-card {
  margin-top: 12px;
}

.admin-pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.admin-page-size {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--lc-muted-light);
  font-size: 13px;
}

.admin-page-actions {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.admin-page-indicator {
  min-width: 56px;
  text-align: center;
  color: var(--lc-slate);
  font-weight: 600;
  font-size: 13px;
}

.total-count {
  color: var(--lc-muted);
  font-size: 13px;
  font-weight: 600;
}

.jump-page {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--lc-muted-light);
  font-size: 13px;
}

.jump-page input {
  width: 64px;
  height: 34px;
  border: 1px solid var(--lc-border);
  border-radius: 8px;
  text-align: center;
}

@media (max-width: 1023px) {
  .filter-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>





