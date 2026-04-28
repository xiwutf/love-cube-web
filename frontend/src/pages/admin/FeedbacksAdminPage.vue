<template>
  <section class="admin-page">
    <section class="platform-card">
      <h1 class="platform-title">用户反馈</h1>
      <p class="platform-subtitle">查看用户反馈并跟进处理，支持按状态维护处理进度。</p>
    </section>

    <div v-if="loading" class="admin-loading">加载中</div>
    <div v-else-if="error" class="admin-error">{{ error }} <button class="admin-btn" @click="load">重试</button></div>

    <section v-if="!loading && !error" class="platform-card summary-card">
      <h2 class="platform-title">问卷统计概览</h2>
      <p class="platform-subtitle">总样本 {{ summary.total }} 份</p>
      <div v-if="summary.total > 0" class="summary-grid">
        <div class="summary-panel">
          <h3>Q1 最关注模块</h3>
          <div v-if="summary.moduleRanking.length" class="summary-list">
            <div v-for="item in summary.moduleRanking" :key="`module-${item.module}`" class="summary-item">
              <div class="summary-item-head">
                <span class="summary-label">{{ item.module }}</span>
                <span class="summary-value">{{ item.percent }}%（{{ item.count }}）</span>
              </div>
              <div class="summary-bar">
                <span class="summary-bar-fill" :style="{ width: `${item.percent}%` }" />
              </div>
            </div>
          </div>
          <p v-else class="admin-row-meta">暂无可用统计数据</p>
        </div>

        <div class="summary-panel">
          <h3>Q2 来站目标（多选）</h3>
          <div v-if="summary.goalRanking.length" class="summary-list">
            <div v-for="item in summary.goalRanking" :key="`goal-${item.goal}`" class="summary-item">
              <div class="summary-item-head">
                <span class="summary-label">{{ item.goal }}</span>
                <span class="summary-value">{{ item.percent }}%（{{ item.count }}）</span>
              </div>
              <div class="summary-bar">
                <span class="summary-bar-fill goal" :style="{ width: `${item.percent}%` }" />
              </div>
            </div>
          </div>
          <p v-else class="admin-row-meta">暂无可用统计数据</p>
        </div>

        <div class="summary-panel">
          <h3>Q3 最需要改进</h3>
          <div v-if="summary.improvementRanking.length" class="summary-list">
            <div v-for="item in summary.improvementRanking" :key="`improvement-${item.improvement}`" class="summary-item">
              <div class="summary-item-head">
                <span class="summary-label">{{ item.improvement }}</span>
                <span class="summary-value">{{ item.percent }}%（{{ item.count }}）</span>
              </div>
              <div class="summary-bar">
                <span class="summary-bar-fill improvement" :style="{ width: `${item.percent}%` }" />
              </div>
            </div>
          </div>
          <p v-else class="admin-row-meta">暂无可用统计数据</p>
        </div>
      </div>
      <p v-else class="admin-row-meta">暂无可用统计数据</p>
    </section>

    <section v-else class="admin-table-wrap admin-desktop-only">
      <table class="admin-table">
        <thead>
          <tr>
            <th>用户</th>
            <th>联系方式</th>
            <th>反馈内容</th>
            <th>状态</th>
            <th>处理备注</th>
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
                <option value="pending">待处理</option>
                <option value="processing">处理中</option>
                <option value="resolved">已解决</option>
              </select>
            </td>
            <td><textarea v-model="item.adminNote" class="admin-textarea" placeholder="记录处理方案或回访说明" ></textarea></td>
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
          <option value="pending">待处理</option>
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
import { getFeedbacks, getFeedbackSummary, updateFeedback } from '@/api/adminContent.js'

const loading = ref(true)
const saving = ref(false)
const error = ref('')
const items = ref([])
const summary = ref({
  total: 0,
  moduleRanking: [],
  goalRanking: [],
  improvementRanking: []
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [feedbackList, feedbackSummary] = await Promise.all([
      getFeedbacks(),
      getFeedbackSummary().catch(() => ({ total: 0, moduleRanking: [], goalRanking: [], improvementRanking: [] }))
    ])
    items.value = Array.isArray(feedbackList) ? feedbackList : []
    summary.value = {
      total: Number(feedbackSummary?.total || 0),
      moduleRanking: Array.isArray(feedbackSummary?.moduleRanking) ? feedbackSummary.moduleRanking : [],
      goalRanking: Array.isArray(feedbackSummary?.goalRanking) ? feedbackSummary.goalRanking : [],
      improvementRanking: Array.isArray(feedbackSummary?.improvementRanking) ? feedbackSummary.improvementRanking : []
    }
  } catch (e) {
    error.value = e.message || '加载失败'
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
    showToast({ message: '状态已更新', type: 'success' })
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

<style scoped>
.feedback-content {
  max-width: 320px;
  white-space: pre-wrap;
}

.summary-card {
  margin-bottom: 12px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 12px;
}

.summary-panel {
  border: 1px solid #eceff5;
  border-radius: 12px;
  background: #fff;
  padding: 12px;
}

.summary-panel h3 {
  margin: 0 0 10px;
  font-size: 15px;
  color: #1f2a44;
}

.summary-list {
  gap: 8px;
  display: grid;
}

.summary-item {
  border-radius: 10px;
  background: #f8faff;
  padding: 8px;
  font-size: 14px;
}

.summary-item-head {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 6px;
}

.summary-label {
  color: #2b3a55;
}

.summary-value {
  color: #51607d;
  white-space: nowrap;
}

.summary-bar {
  height: 7px;
  border-radius: 999px;
  background: #e6edf8;
  overflow: hidden;
}

.summary-bar-fill {
  display: block;
  height: 100%;
  background: linear-gradient(90deg, #3b82f6, #60a5fa);
}

.summary-bar-fill.goal {
  background: linear-gradient(90deg, #8b5cf6, #a78bfa);
}

.summary-bar-fill.improvement {
  background: linear-gradient(90deg, #10b981, #34d399);
}
</style>



