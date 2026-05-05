<template>
  <section class="admin-page">
    <section class="platform-card">
      <h1 class="platform-title">互助需求审核</h1>
      <p class="platform-subtitle">仅「待审核」需求可通过 / 驳回；上架后用户可在互助广场回应。</p>
      <div class="admin-toolbar">
        <select v-model="statusFilter" class="admin-input" @change="load">
          <option value="pending">待审核</option>
          <option value="ALL">全部状态</option>
          <option value="active">进行中</option>
          <option value="resolved">已解决</option>
          <option value="closed">已关闭</option>
          <option value="rejected">已驳回</option>
        </select>
        <button class="admin-btn" type="button" :disabled="loading" @click="load">刷新</button>
      </div>
    </section>

    <div v-if="loading" class="admin-loading">加载中...</div>
    <div v-else-if="error" class="admin-error">{{ error }}</div>

    <section v-else class="admin-list">
      <article v-for="item in items" :key="item.id" class="admin-row">
        <div class="admin-row-head">
          <strong>#{{ item.id }} {{ item.title }}</strong>
          <span class="admin-tag">{{ item.status }}</span>
        </div>
        <p class="admin-row-meta">{{ typeLabel(item.helpType) }} · {{ item.publisherName }} · {{ formatDate(item.createdAt) }}</p>
        <p class="admin-row-content">{{ item.content }}</p>
        <div v-if="item.status === 'pending'" class="admin-cell-actions">
          <button class="admin-btn" type="button" :disabled="saving" @click="review(item.id, 'active')">通过并上架</button>
          <button class="admin-btn danger" type="button" :disabled="saving" @click="review(item.id, 'rejected')">驳回</button>
        </div>
      </article>
    </section>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { showToast } from 'vant'
import { getAdminHelpRequests, reviewAdminHelpRequest } from '@/api/adminHelp.js'

const loading = ref(false)
const saving = ref(false)
const error = ref('')
const statusFilter = ref('pending')
const items = ref([])

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

function formatDate(iso) {
  if (!iso) return ''
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return iso
  return d.toLocaleString()
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const res = await getAdminHelpRequests({
      status: statusFilter.value,
      pageNum: 1,
      pageSize: 50
    })
    items.value = Array.isArray(res?.list) ? res.list : []
  } catch (e) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

async function review(id, status) {
  saving.value = true
  try {
    await reviewAdminHelpRequest(id, status)
    showToast({ type: 'success', message: '已更新' })
    await load()
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '操作失败' })
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>
