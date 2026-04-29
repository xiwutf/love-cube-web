<template>
  <section class="admin-page">
    <section class="platform-card">
      <h1 class="platform-title">认证审核</h1>
      <p class="platform-subtitle">审核用户提交的真人头像与实名认证申请</p>
    </section>

    <!-- Filter -->
    <section class="platform-card" style="margin-top:12px;">
      <select v-model="filterStatus" class="admin-select" style="margin-right:8px;">
        <option value="">全部状态</option>
        <option value="pending">待审核</option>
        <option value="approved">已通过</option>
        <option value="rejected">已驳回</option>
      </select>
      <select v-model="filterType" class="admin-select">
        <option value="">全部类型</option>
        <option value="PHOTO">真人头像</option>
        <option value="REALNAME">实名认证</option>
        <option value="IDCARD">身份证</option>
      </select>
    </section>

    <div v-if="loading" class="admin-loading">加载中...</div>
    <div v-else-if="error" class="admin-error">{{ error }} <button class="admin-btn" @click="load">重试</button></div>

    <section v-else class="admin-table-wrap admin-desktop-only">
      <div class="batch-toolbar">
        <label class="batch-select-all">
          <input
            type="checkbox"
            :checked="allFilteredPendingSelected"
            :disabled="!pendingFilteredItems.length || saving"
            @change="toggleAllFilteredPending"
          />
          选择本页待审核
        </label>
        <span class="batch-count">已选 {{ selectedPendingItems.length }} 项</span>
        <div class="batch-actions">
          <button class="admin-btn primary" type="button" :disabled="saving || !selectedPendingItems.length" @click="batchApprove">批量通过</button>
          <button class="admin-btn" type="button" :disabled="saving || !selectedPendingItems.length" @click="batchReject">批量驳回</button>
          <button class="admin-btn" type="button" :disabled="saving || !selectedPendingItems.length" @click="clearSelection">清空选择</button>
        </div>
      </div>
      <table class="admin-table">
        <thead>
          <tr>
            <th>选择</th>
            <th>申请</th>
            <th>类型</th>
            <th>提交内容</th>
            <th>提交 / 审核</th>
            <th>状态</th>
            <th>驳回原因</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in filteredItems" :key="item.id">
            <td>
              <input
                type="checkbox"
                :checked="selectedIds.has(item.id)"
                :disabled="saving || item.status !== 'pending'"
                @change="toggleSelection(item)"
              />
            </td>
            <td>用户 {{ item.userId }}</td>
            <td><span class="admin-tag" :class="item.verifyType?.toLowerCase()">{{ typeLabel(item.verifyType) }}</span></td>
            <td class="submit-data-cell">{{ parseSubmitData(item) }}</td>
            <td>{{ formatDate(item.submittedAt) }}<br />{{ item.reviewedAt ? formatDate(item.reviewedAt) : '待审核' }}</td>
            <td><span class="admin-tag" :class="item.status">{{ statusLabel(item.status) }}</span></td>
            <td><textarea v-model="rejectMemo[item.id]" class="admin-textarea" placeholder="驳回原因" ></textarea></td>
            <td>
              <div class="admin-cell-actions">
                <button class="admin-btn primary" type="button" :disabled="saving || item.status !== 'pending'" @click="approve(item)">通过</button>
                <button class="admin-btn" type="button" :disabled="saving || item.status !== 'pending'" @click="reject(item)">驳回</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <van-empty v-if="!filteredItems.length" description="暂无认证申请" />
    </section>

    <div v-if="!loading && !error" class="admin-list admin-mobile-only">
      <div class="batch-toolbar mobile">
        <label class="batch-select-all">
          <input
            type="checkbox"
            :checked="allFilteredPendingSelected"
            :disabled="!pendingFilteredItems.length || saving"
            @change="toggleAllFilteredPending"
          />
          选择待审核
        </label>
        <span class="batch-count">已选 {{ selectedPendingItems.length }} 项</span>
        <button class="admin-btn primary" type="button" :disabled="saving || !selectedPendingItems.length" @click="batchApprove">批量通过</button>
        <button class="admin-btn" type="button" :disabled="saving || !selectedPendingItems.length" @click="batchReject">批量驳回</button>
      </div>
      <article v-for="item in filteredItems" :key="item.id" class="admin-row">
        <div class="admin-row-head">
          <label class="batch-row-select">
            <input
              type="checkbox"
              :checked="selectedIds.has(item.id)"
              :disabled="saving || item.status !== 'pending'"
              @change="toggleSelection(item)"
            />
            <strong>用户 {{ item.userId }}</strong>
          </label>
          <div style="display:flex;gap:6px;align-items:center;">
            <span class="admin-tag" :class="item.verifyType?.toLowerCase()">{{ typeLabel(item.verifyType) }}</span>
            <span class="admin-tag" :class="item.status">{{ statusLabel(item.status) }}</span>
          </div>
        </div>
        <p class="admin-row-meta">{{ parseSubmitData(item) }}</p>
        <p class="admin-row-meta">提交：{{ formatDate(item.submittedAt) }} / 审核：{{ item.reviewedAt ? formatDate(item.reviewedAt) : '待审核' }}</p>
        <textarea v-model="rejectMemo[item.id]" class="admin-textarea" placeholder="驳回原因（驳回时填写）" ></textarea>
        <div class="admin-toolbar">
          <button class="admin-btn primary" type="button" :disabled="saving || item.status !== 'pending'" @click="approve(item)">通过</button>
          <button class="admin-btn" type="button" :disabled="saving || item.status !== 'pending'" @click="reject(item)">驳回</button>
        </div>
      </article>
      <van-empty v-if="!filteredItems.length" description="暂无认证申请" />
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { showToast } from 'vant'
import { getAdminAuthContext, getVerifications, reviewVerification } from '@/api/adminContent.js'

const loading = ref(true)
const saving  = ref(false)
const error   = ref('')
const items   = ref([])
const selectedIds = ref(new Set())
const rejectMemo  = reactive({})
const filterStatus = ref('')
const filterType   = ref('')

const filteredItems = computed(() =>
  items.value.filter(i =>
    (!filterStatus.value || i.status === filterStatus.value) &&
    (!filterType.value   || (i.verifyType || '').toUpperCase() === filterType.value)
  )
)
const pendingFilteredItems = computed(() => filteredItems.value.filter(item => item.status === 'pending'))
const selectedPendingItems = computed(() =>
  pendingFilteredItems.value.filter(item => selectedIds.value.has(item.id))
)
const allFilteredPendingSelected = computed(() =>
  pendingFilteredItems.value.length > 0 &&
  pendingFilteredItems.value.every(item => selectedIds.value.has(item.id))
)

async function load() {
  loading.value = true
  error.value = ''
  try {
    items.value = await getVerifications()
    clearSelection()
  } catch (e) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

async function approve(item) {
  saving.value = true
  try {
    const updated = await reviewVerification(item.id, 'approve')
    Object.assign(item, updated)
    showToast({ message: '审核通过', type: 'success' })
  } catch (e) {
    showToast({ message: await buildReviewErrorMessage(e), type: 'fail' })
  } finally {
    saving.value = false
  }
}

async function reject(item) {
  const reason = rejectMemo[item.id] || '资料不完整，请补充后重新提交。'
  saving.value = true
  try {
    const updated = await reviewVerification(item.id, 'reject', reason)
    Object.assign(item, updated)
    showToast({ message: '已驳回', type: 'success' })
  } catch (e) {
    showToast({ message: await buildReviewErrorMessage(e), type: 'fail' })
  } finally {
    saving.value = false
  }
}

function toggleSelection(item) {
  if (item.status !== 'pending') return
  const next = new Set(selectedIds.value)
  if (next.has(item.id)) {
    next.delete(item.id)
  } else {
    next.add(item.id)
  }
  selectedIds.value = next
}

function toggleAllFilteredPending() {
  const next = new Set(selectedIds.value)
  if (allFilteredPendingSelected.value) {
    pendingFilteredItems.value.forEach(item => next.delete(item.id))
  } else {
    pendingFilteredItems.value.forEach(item => next.add(item.id))
  }
  selectedIds.value = next
}

function clearSelection() {
  selectedIds.value = new Set()
}

async function batchApprove() {
  await runBatchReview('approve')
}

async function batchReject() {
  const reason = window.prompt('请输入批量驳回原因', '资料不完整，请补充后重新提交。')
  if (reason === null) return
  await runBatchReview('reject', reason.trim() || '资料不完整，请补充后重新提交。')
}

async function runBatchReview(action, reason = '') {
  const targets = selectedPendingItems.value.slice()
  if (!targets.length) return
  saving.value = true
  let successCount = 0
  let failedMessage = ''
  try {
    for (const item of targets) {
      try {
        const updated = await reviewVerification(item.id, action, action === 'reject' ? reason : undefined)
        Object.assign(item, updated)
        selectedIds.value.delete(item.id)
        successCount += 1
      } catch (e) {
        failedMessage = failedMessage || await buildReviewErrorMessage(e)
      }
    }
    selectedIds.value = new Set(selectedIds.value)
    if (failedMessage) {
      showToast({ message: `已处理 ${successCount} 项，失败：${failedMessage}`, type: 'fail' })
    } else {
      showToast({ message: `已处理 ${successCount} 项`, type: 'success' })
    }
  } finally {
    saving.value = false
  }
}

async function buildReviewErrorMessage(error) {
  const baseMessage = error?.message || '操作失败'
  if (!/403|Forbidden|无管理员权限/.test(baseMessage)) {
    return baseMessage
  }
  try {
    const auth = await getAdminAuthContext()
    return `${baseMessage}；当前 token 用户 ${auth.userId}，role=${auth.role || '-'}，isAdmin=${auth.isAdmin}`
  } catch {
    return baseMessage
  }
}

function typeLabel(type) {
  return { PHOTO: '📷 真人头像', REALNAME: '🪪 实名认证', IDCARD: '🧾 身份证' }[type] || type || '未知'
}

function statusLabel(status) {
  return { pending: '待审核', approved: '已通过', rejected: '已驳回' }[status] || status
}

function parseSubmitData(item) {
  if (!item.submitData) {
    // Legacy records: show realName + idNumber fields
    const parts = []
    if (item.realName) parts.push(`姓名：${item.realName}`)
    if (item.idNumber) parts.push(`证件：${item.idNumber}`)
    return parts.join(' / ') || '--'
  }
  try {
    const d = JSON.parse(item.submitData)
    const parts = []
    if (d.realName) parts.push(`姓名：${d.realName}`)
    if (d.idLast4) parts.push(`证件后四位：${d.idLast4}`)
    if (d.selfieUrl) parts.push('[已上传照片]')
    return parts.join(' / ') || item.submitData.slice(0, 60)
  } catch {
    return item.submitData?.slice(0, 60) || '--'
  }
}

function formatDate(value) {
  if (!value) return '-'
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}

onMounted(load)
</script>

<style scoped>
.submit-data-cell { max-width: 200px; word-break: break-all; font-size: 12px; color: #666; }
.admin-tag.photo    { background: #e8f4ff; color: #1989fa; }
.admin-tag.realname { background: #e8f8f0; color: #07c160; }
.admin-tag.idcard   { background: #fff8e1; color: #ff9800; }
.batch-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-bottom: 1px solid #e8edf5;
  background: #f8fbff;
}
.batch-toolbar.mobile {
  flex-wrap: wrap;
  margin-bottom: 12px;
  border: 1px solid #e8edf5;
  border-radius: 12px;
}
.batch-select-all,
.batch-row-select {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.batch-count {
  color: #58708f;
  font-size: 14px;
}
.batch-actions {
  display: flex;
  gap: 8px;
  margin-left: auto;
}
</style>



