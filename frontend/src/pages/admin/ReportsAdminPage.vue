<template>
  <section class="admin-page">
    <section class="platform-card">
      <h1 class="platform-title">举报处理</h1>
      <p class="platform-subtitle">审核举报、驳回无效举报，或直接封禁违规用户。</p>
    </section>

    <div class="admin-filters">
      <select v-model="filterStatus" class="admin-select" @change="applyFilter">
        <option value="">全部状态</option>
        <option value="PENDING">待处理</option>
        <option value="REVIEWED">已审核</option>
        <option value="REJECTED">已驳回</option>
        <option value="BANNED">已封禁</option>
      </select>
      <select v-model="filterType" class="admin-select" @change="applyFilter">
        <option value="">全部类型</option>
        <option value="USER">用户举报</option>
        <option value="DYNAMIC">动态举报</option>
        <option value="MESSAGE">消息举报</option>
      </select>
    </div>

    <div v-if="loading" class="admin-loading">加载中…</div>
    <div v-else-if="error" class="admin-error">{{ error }} <button class="admin-btn" @click="load">重试</button></div>

    <section v-else class="admin-table-wrap admin-desktop-only">
      <table class="admin-table">
        <thead>
          <tr>
            <th>类型</th>
            <th>举报原因</th>
            <th>内容</th>
            <th>举报人 → 被举报人</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in filtered" :key="item.id">
            <td>{{ targetTypeLabel(item.targetType || item.reportType) }}</td>
            <td>{{ item.reasonType || item.reportType || '-' }}</td>
            <td>{{ item.content || '-' }}</td>
            <td>
              {{ item.reporterId }} → {{ item.targetUserId }}<br />
              <span class="admin-row-meta">{{ formatDate(item.createdAt) }}</span>
              <span v-if="item.reviewedAt" class="admin-row-meta"> · 审核：{{ formatDate(item.reviewedAt) }}</span>
            </td>
            <td>
              <span class="admin-tag" :class="statusClass(item.status)">
                {{ statusLabel(item.status) }}
              </span>
            </td>
            <td>
              <div class="admin-cell-actions" v-if="isPending(item)">
                <button class="admin-btn" :disabled="reviewing" @click="review(item, 'reviewed')">已审核</button>
                <button class="admin-btn" :disabled="reviewing" @click="review(item, 'rejected')">驳回</button>
                <button class="admin-btn danger" :disabled="reviewing" @click="review(item, 'banned')">封禁用户</button>
              </div>
              <span v-else class="admin-row-meta">已处理</span>
            </td>
          </tr>
        </tbody>
      </table>
      <van-empty v-if="!filtered.length" description="暂无举报记录" />
    </section>

    <div v-if="!loading && !error" class="admin-list admin-mobile-only">
      <article v-for="item in filtered" :key="item.id" class="admin-row">
        <div class="admin-row-head">
          <strong>{{ targetTypeLabel(item.targetType || item.reportType) }} · {{ item.reasonType || item.reportType }}</strong>
          <span class="admin-tag" :class="statusClass(item.status)">{{ statusLabel(item.status) }}</span>
        </div>
        <p>{{ item.content || '（无附加内容）' }}</p>
        <p class="admin-row-meta">举报人：{{ item.reporterId }} · 被举报：{{ item.targetUserId }} · {{ formatDate(item.createdAt) }}</p>
        <div v-if="isPending(item)" class="admin-toolbar">
          <button class="admin-btn" :disabled="reviewing" @click="review(item, 'reviewed')">已审核</button>
          <button class="admin-btn" :disabled="reviewing" @click="review(item, 'rejected')">驳回</button>
          <button class="admin-btn danger" :disabled="reviewing" @click="review(item, 'banned')">封禁用户</button>
        </div>
      </article>
      <van-empty v-if="!filtered.length" description="暂无举报记录" />
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { showConfirmDialog, showToast } from 'vant'
import { getReports, reviewReport } from '@/api/adminContent.js'

const loading   = ref(true)
const reviewing = ref(false)
const error     = ref('')
const items     = ref([])
const filterStatus = ref('')
const filterType   = ref('')

const filtered = computed(() => items.value.filter(item => {
  const matchStatus = !filterStatus.value || item.status === filterStatus.value
  const matchType   = !filterType.value
    || (item.targetType || item.reportType || '').toUpperCase() === filterType.value
  return matchStatus && matchType
}))

async function load() {
  loading.value = true
  error.value = ''
  try {
    items.value = await getReports()
  } catch (e) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

async function review(item, action) {
  const labels = { reviewed: '标记为已审核', rejected: '驳回该举报', banned: '封禁该用户' }
  try {
    await showConfirmDialog({
      title: '确认操作',
      message: `确认${labels[action]}？${action === 'banned' ? '\n\n被举报用户将被立即封禁。' : ''}`,
    })
  } catch { return }

  reviewing.value = true
  try {
    const result = await reviewReport(item.id, action)
    Object.assign(item, result)
    showToast({ message: result.message || '处理完成', type: 'success' })
  } catch (e) {
    showToast({ message: e.message || '操作失败', type: 'fail' })
  } finally {
    reviewing.value = false
  }
}

function applyFilter() {}

function isPending(item) {
  const s = (item.status || '').toUpperCase()
  return s === 'PENDING' || s === 'pending'
}

function statusLabel(status) {
  const map = {
    PENDING: '待处理', pending: '待处理',
    REVIEWED: '已审核', reviewed: '已审核',
    REJECTED: '已驳回', rejected: '已驳回',
    BANNED: '已封禁', banned: '已封禁',
    processing: '处理中', resolved: '已解决',
  }
  return map[status] || status || '未知'
}

function statusClass(status) {
  const s = (status || '').toLowerCase()
  if (s === 'pending') return 'pending'
  if (s === 'reviewed' || s === 'resolved') return 'approved'
  if (s === 'rejected') return 'rejected'
  if (s === 'banned') return 'banned'
  return ''
}

function targetTypeLabel(type) {
  const map = { USER: '用户', DYNAMIC: '动态', MESSAGE: '消息' }
  return map[(type || '').toUpperCase()] || type || '-'
}

function formatDate(value) {
  if (!value) return '-'
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}

onMounted(load)
</script>

<style scoped>
.admin-filters { display: flex; gap: 8px; padding: 12px 0; }
.admin-tag.banned { background: #fee2e2; color: #dc2626; }
.admin-tag.rejected { background: #fef3c7; color: #92400e; }
.admin-btn.danger { background: #dc2626; color: #fff; border-color: #dc2626; }
.admin-btn.danger:hover { background: #b91c1c; }
</style>
