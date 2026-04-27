<template>
  <section class="admin-page">
    <section class="platform-card">
      <h1 class="platform-title">用户反馈</h1>
      <p class="platform-subtitle">в鿴û뽨飬ִ֧Ⱥάע汾</p>
    </section>

    <div v-if="loading" class="admin-loading">加载中</div>
    <div v-else-if="error" class="admin-error">{{ error }} <button class="admin-btn" @click="load">閲嶈瘯</button></div>

    <section v-else class="admin-table-wrap admin-desktop-only">
      <table class="admin-table">
        <thead>
          <tr>
            <th>用户</th>
            <th>联系方式</th>
            <th>反馈内容</th>
            <th>状</th>
            <th>澶勭悊澶囨敞</th>
            <th>提交时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in items" :key="item.id">
            <td>{{ item.username || `用户${item.userId}` }}</td>
            <td>{{ item.contact || '-' }}</td>
            <td class="feedback-content">{{ item.content }}</td>
            <td>
              <select v-model="item.status" class="admin-select">
                <option value="pending">待处</option>
                <option value="processing">澶勭悊涓</option>
                <option value="resolved">已解</option>
              </select>
            </td>
            <td><textarea v-model="item.adminNote" class="admin-textarea" placeholder="记录处理方案或发布时间计" ></textarea></td>
            <td><span class="admin-row-meta">{{ formatDate(item.createdAt) }}</span></td>
            <td>
              <button class="admin-btn" type="button" :disabled="saving" @click="save(item)">保存</button>
            </td>
          </tr>
        </tbody>
      </table>
      <van-empty v-if="!items.length" description="暂无用户反馈" />
    </section>

    <div v-if="!loading && !error" class="admin-list admin-mobile-only">
      <article v-for="item in items" :key="item.id" class="admin-row">
        <div class="admin-row-head">
          <strong>{{ item.username || `用户${item.userId}` }}</strong>
          <span class="admin-tag" :class="item.status">{{ statusLabel(item.status) }}</span>
        </div>
        <p>{{ item.content }}</p>
        <p class="admin-row-meta">联系方式：{{ item.contact || '-' }}</p>
        <p class="admin-row-meta">提交时间：{{ formatDate(item.createdAt) }}</p>
        <select v-model="item.status" class="admin-select">
          <option value="pending">待处</option>
          <option value="processing">处理中</option>
          <option value="resolved">已解决</option>
        </select>
        <textarea v-model="item.adminNote" class="admin-textarea" placeholder="处理备注" ></textarea>
        <div class="admin-toolbar">
          <button class="admin-btn" type="button" :disabled="saving" @click="save(item)">保存</button>
        </div>
      </article>
      <van-empty v-if="!items.length" description="暂无用户反馈" />
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { showToast } from 'vant'
import { getFeedbacks, updateFeedback } from '@/api/adminContent.js'

const loading = ref(true)
const saving = ref(false)
const error = ref('')
const items = ref([])

async function load() {
  loading.value = true
  error.value = ''
  try {
    items.value = await getFeedbacks()
  } catch (e) {
    error.value = e.message || 'ʧ'
  } finally {
    loading.value = false
  }
}

async function save(item) {
  saving.value = true
  try {
    const updated = await updateFeedback(item.id, {
      status: item.status,
      adminNote: item.adminNote || ''
    })
    Object.assign(item, updated)
    showToast({ message: '״Ѹ', type: 'success' })
  } catch (e) {
    showToast({ message: e.message || '保存失败', type: 'fail' })
  } finally {
    saving.value = false
  }
}

function statusLabel(status) {
  const map = { pending: '', processing: '', resolved: 'ѽ' }
  return map[status] || status
}

function formatDate(value) {
  if (!value) return '-'
  return new Date(value).toLocaleString('zh-CN', { hour12: false })
}

onMounted(load)
</script>

<style scoped>
.feedback-content {
  max-width: 320px;
  white-space: pre-wrap;
}
</style>



