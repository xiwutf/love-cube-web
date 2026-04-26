<template>
  <section class="admin-page">
    <section class="platform-card">
      <h1 class="platform-title">举报处理</h1>
      <p class="platform-subtitle">PC 提供完整处理视图；手机端保留状态更新与备注填写。</p>
    </section>

    <div v-if="loading" class="admin-loading">加载中…</div>
    <div v-else-if="error" class="admin-error">{{ error }} <button class="admin-btn" @click="load">重试</button></div>

    <section v-else class="admin-table-wrap admin-desktop-only">
      <table class="admin-table">
        <thead>
          <tr>
            <th>类型</th>
            <th>举报内容</th>
            <th>关联用户</th>
            <th>状态</th>
            <th>处理备注</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in items" :key="item.id">
            <td>{{ item.reportType }}</td>
            <td>{{ item.content }}</td>
            <td>{{ item.reporterId }} → {{ item.targetUserId }}<br /><span class="admin-row-meta">{{ formatDate(item.createdAt) }}</span></td>
            <td>
              <select v-model="item.status" class="admin-select">
                <option value="pending">待处理</option>
                <option value="processing">处理中</option>
                <option value="resolved">已解决</option>
              </select>
            </td>
            <td><textarea v-model="item.note" class="admin-textarea" placeholder="处理备注" /></td>
            <td>
              <div class="admin-cell-actions">
                <button class="admin-btn" type="button" :disabled="saving" @click="save(item)">保存处理结果</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <van-empty v-if="!items.length" description="暂无举报记录" />
    </section>

    <div v-if="!loading && !error" class="admin-list admin-mobile-only">
      <article v-for="item in items" :key="item.id" class="admin-row">
        <div class="admin-row-head">
          <strong>{{ item.reportType }}</strong>
          <span class="admin-tag" :class="item.status">{{ statusLabel(item.status) }}</span>
        </div>
        <p>举报内容：{{ item.content }}</p>
        <p class="admin-row-meta">举报人：{{ item.reporterId }} · 被举报：{{ item.targetUserId }} · {{ formatDate(item.createdAt) }}</p>
        <select v-model="item.status" class="admin-select">
          <option value="pending">待处理</option>
          <option value="processing">处理中</option>
          <option value="resolved">已解决</option>
        </select>
        <textarea v-model="item.note" class="admin-textarea" placeholder="处理备注" />
        <div class="admin-toolbar">
          <button class="admin-btn" type="button" :disabled="saving" @click="save(item)">保存处理结果</button>
        </div>
      </article>
      <van-empty v-if="!items.length" description="暂无举报记录" />
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { showToast } from 'vant'
import { getReports, updateReport } from '@/api/adminContent.js'

const loading = ref(true)
const saving = ref(false)
const error = ref('')
const items = ref([])

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

async function save(item) {
  saving.value = true
  try {
    const updated = await updateReport(item.id, { status: item.status, note: item.note || '' })
    Object.assign(item, updated)
    showToast({ message: '举报处理已更新', type: 'success' })
  } catch (e) {
    showToast({ message: e.message || '保存失败', type: 'fail' })
  } finally {
    saving.value = false
  }
}

function statusLabel(status) {
  const map = { pending: '待处理', processing: '处理中', resolved: '已解决' }
  return map[status] || status
}

function formatDate(value) {
  if (!value) return '-'
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}

onMounted(load)
</script>
