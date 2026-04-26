<template>
  <section class="admin-page">
    <section class="platform-card">
      <h1 class="platform-title">认证审核</h1>
      <p class="platform-subtitle">优先保证手机端可审核：通过、驳回、填写原因均可操作。</p>
    </section>

    <div v-if="loading" class="admin-loading">加载中…</div>
    <div v-else-if="error" class="admin-error">{{ error }} <button class="admin-btn" @click="load">重试</button></div>

    <section v-else class="admin-table-wrap admin-desktop-only">
      <table class="admin-table">
        <thead>
          <tr>
            <th>申请人 ID</th>
            <th>实名信息</th>
            <th>提交 / 审核</th>
            <th>状态</th>
            <th>驳回原因</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in items" :key="item.id">
            <td>用户 {{ item.userId }}</td>
            <td>
              姓名：{{ item.realName }}<br />
              证件：{{ item.idNumber }}
            </td>
            <td>{{ formatDate(item.submittedAt) }}<br />{{ item.reviewedAt ? formatDate(item.reviewedAt) : '待审核' }}</td>
            <td><span class="admin-tag" :class="item.status">{{ statusLabel(item.status) }}</span></td>
            <td><textarea v-model="rejectMemo[item.id]" class="admin-textarea" placeholder="驳回原因" /></td>
            <td>
              <div class="admin-cell-actions">
                <button class="admin-btn primary" type="button" :disabled="saving || item.status !== 'pending'" @click="approve(item)">通过</button>
                <button class="admin-btn" type="button" :disabled="saving || item.status !== 'pending'" @click="reject(item)">驳回</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <van-empty v-if="!items.length" description="暂无认证申请" />
    </section>

    <div v-if="!loading && !error" class="admin-list admin-mobile-only">
      <article v-for="item in items" :key="item.id" class="admin-row">
        <div class="admin-row-head">
          <strong>用户 {{ item.userId }}</strong>
          <span class="admin-tag" :class="item.status">{{ statusLabel(item.status) }}</span>
        </div>
        <p class="admin-row-meta">姓名：{{ item.realName }} · 证件号：{{ item.idNumber }}</p>
        <p class="admin-row-meta">提交：{{ formatDate(item.submittedAt) }} · 审核：{{ item.reviewedAt ? formatDate(item.reviewedAt) : '待审核' }}</p>
        <textarea v-model="rejectMemo[item.id]" class="admin-textarea" placeholder="驳回原因（驳回时填写）" />
        <div class="admin-toolbar">
          <button class="admin-btn primary" type="button" :disabled="saving || item.status !== 'pending'" @click="approve(item)">通过</button>
          <button class="admin-btn" type="button" :disabled="saving || item.status !== 'pending'" @click="reject(item)">驳回</button>
        </div>
      </article>
      <van-empty v-if="!items.length" description="暂无认证申请" />
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { showToast } from 'vant'
import { getVerifications, reviewVerification } from '@/api/adminContent.js'

const loading = ref(true)
const saving = ref(false)
const error = ref('')
const items = ref([])
const rejectMemo = reactive({})

async function load() {
  loading.value = true
  error.value = ''
  try {
    items.value = await getVerifications()
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
    showToast({ message: e.message || '操作失败', type: 'fail' })
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
    showToast({ message: e.message || '操作失败', type: 'fail' })
  } finally {
    saving.value = false
  }
}

function statusLabel(status) {
  const map = { pending: '待审核', approved: '已通过', rejected: '已驳回' }
  return map[status] || status
}

function formatDate(value) {
  if (!value) return '-'
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}

onMounted(load)
</script>
