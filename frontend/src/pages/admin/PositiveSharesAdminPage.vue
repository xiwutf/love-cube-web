<template>
  <section class="admin-page">
    <section class="platform-card">
      <h1 class="platform-title">每日心声审核</h1>
      <p class="platform-subtitle">审核用户提交内容，支持通过、驳回、删除。</p>
      <div class="admin-toolbar">
        <select v-model="statusFilter" class="admin-input" @change="load">
          <option value="ALL">全部</option>
          <option value="PENDING">待审核</option>
          <option value="PUBLISHED">已发布</option>
          <option value="REJECTED">已驳回</option>
          <option value="DELETED">已删除</option>
        </select>
        <label class="admin-check">
          <input v-model="pendingOnly" type="checkbox" @change="handlePendingOnlyChange">
          仅看待审核
        </label>
        <button class="admin-btn" type="button" :disabled="saving || !selectedIds.length" @click="batchReview('PUBLISHED')">
          批量通过 ({{ selectedIds.length }})
        </button>
        <button class="admin-btn danger" type="button" :disabled="saving || !selectedIds.length" @click="batchReview('REJECTED')">
          批量驳回
        </button>
        <button class="admin-btn danger" type="button" :disabled="saving || !selectedIds.length" @click="batchReview('DELETED')">
          批量删除
        </button>
        <button class="admin-btn" type="button" :disabled="saving || !selectedIds.length" @click="batchReview('PENDING')">
          恢复待审核
        </button>
      </div>
    </section>

    <div v-if="loading" class="admin-loading">加载中...</div>
    <div v-else-if="error" class="admin-error">{{ error }}</div>

    <section v-else class="admin-list">
      <article v-for="item in items" :key="item.id" class="admin-row">
        <div class="admin-row-head">
          <label class="admin-check">
            <input v-model="selectedIds" type="checkbox" :value="item.id">
            选择
          </label>
          <strong>{{ item.nickname }}</strong>
          <span class="admin-tag">{{ item.status }}</span>
        </div>
        <p class="admin-row-meta">{{ item.category }} · {{ formatDate(item.createdAt) }}</p>
        <p class="admin-row-content">{{ item.content }}</p>
        <p class="admin-row-meta">鼓励 {{ item.encourageCount }} · 留言 {{ item.commentCount }}</p>
        <div class="admin-cell-actions">
          <button class="admin-btn" type="button" :disabled="saving" @click="review(item.id, 'PUBLISHED')">通过</button>
          <button class="admin-btn" type="button" :disabled="saving" @click="review(item.id, 'REJECTED')">驳回</button>
          <button class="admin-btn danger" type="button" :disabled="saving" @click="review(item.id, 'DELETED')">删除</button>
          <button class="admin-btn" type="button" @click="toggleComments(item)">
            {{ expandedId === item.id ? '收起评论' : '查看评论' }}
          </button>
        </div>
        <div v-if="expandedId === item.id" class="comment-list">
          <p v-if="commentLoading">评论加载中...</p>
          <p v-else-if="!comments.length" class="admin-row-meta">暂无评论</p>
          <p v-for="comment in comments" :key="comment.id" class="admin-row-meta">
            {{ comment.username }}：{{ comment.content }}
          </p>
        </div>
      </article>
    </section>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { showToast } from 'vant'
import {
  batchReviewAdminPositiveShare,
  getAdminPositiveShareComments,
  getAdminPositiveShares,
  reviewAdminPositiveShare
} from '@/api/adminContent.js'

const loading = ref(false)
const saving = ref(false)
const error = ref('')
const statusFilter = ref('PENDING')
const pendingOnly = ref(true)
const items = ref([])
const expandedId = ref(null)
const commentLoading = ref(false)
const comments = ref([])
const selectedIds = ref([])

async function load() {
  loading.value = true
  error.value = ''
  try {
    const res = await getAdminPositiveShares({ status: statusFilter.value, pageNum: 1, pageSize: 50 })
    items.value = Array.isArray(res?.list) ? res.list : []
    selectedIds.value = []
  } catch (e) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

function handlePendingOnlyChange() {
  statusFilter.value = pendingOnly.value ? 'PENDING' : 'ALL'
  load()
}

async function review(id, status) {
  saving.value = true
  try {
    await reviewAdminPositiveShare(id, status)
    showToast({ type: 'success', message: '状态已更新' })
    await load()
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '操作失败' })
  } finally {
    saving.value = false
  }
}

async function batchReview(status) {
  if (!selectedIds.value.length) return
  saving.value = true
  try {
    await batchReviewAdminPositiveShare(selectedIds.value, status)
    showToast({ type: 'success', message: '批量审核完成' })
    await load()
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '批量操作失败' })
  } finally {
    saving.value = false
  }
}

async function toggleComments(item) {
  if (expandedId.value === item.id) {
    expandedId.value = null
    comments.value = []
    return
  }
  expandedId.value = item.id
  commentLoading.value = true
  try {
    const res = await getAdminPositiveShareComments(item.id, { pageNum: 1, pageSize: 30 })
    comments.value = Array.isArray(res?.list) ? res.list : []
  } catch (e) {
    comments.value = []
  } finally {
    commentLoading.value = false
  }
}

function formatDate(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 16)
}

onMounted(load)
</script>

<style scoped>
.admin-row-content {
  margin: 8px 0 0;
  white-space: pre-wrap;
  line-height: 1.7;
  color: #334155;
}

.comment-list {
  margin-top: 10px;
  border-top: 1px dashed #e2e8f0;
  padding-top: 8px;
}
</style>
