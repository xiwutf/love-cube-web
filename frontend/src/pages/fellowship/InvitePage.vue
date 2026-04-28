<template>
  <div class="invite-page">
    <header class="header">
      <van-icon name="arrow-left" size="20" @click="router.back()" />
      <h1>我的邀请码</h1>
      <span class="placeholder"></span>
    </header>

    <section class="card code-card">
      <p class="label">我的邀请码</p>
      <p class="code">{{ summary.inviteCode || '-' }}</p>
      <p class="count">已邀请 {{ summary.inviteCount || 0 }} 人</p>
      <div class="actions">
        <van-button type="primary" round size="small" @click="copyCode">一键复制邀请码</van-button>
      </div>
    </section>

    <section class="card">
      <h3>邀请说明</h3>
      <ul>
        <li>Love Cube 采用邀请制注册</li>
        <li>请将邀请码分享给你信任的朋友</li>
        <li>注册时间与 IP 信息将用于安全风控</li>
      </ul>
    </section>

    <section class="card">
      <h3>我邀请的用户</h3>
      <div v-if="loading" class="state">加载中...</div>
      <div v-else-if="!invitees.length" class="state">暂无邀请记录</div>
      <div v-else class="invitee-list">
        <article v-for="item in invitees" :key="item.userId" class="invitee-item">
          <div>
            <p class="name">{{ item.nickname || item.username || `用户${item.userId}` }}</p>
            <p class="meta">ID {{ item.userId }} · 状态：{{ item.status || 'NORMAL' }}</p>
          </div>
          <p class="time">{{ formatTime(item.registeredAt) }}</p>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getMyInviteCode, getMyInvitees } from '@/api/invite.js'

const router = useRouter()
const loading = ref(false)
const summary = ref({ inviteCode: '', inviteCount: 0 })
const invitees = ref([])

async function loadData() {
  loading.value = true
  try {
    const [summaryRes, listRes] = await Promise.all([getMyInviteCode(), getMyInvitees()])
    summary.value = summaryRes || summary.value
    invitees.value = Array.isArray(listRes) ? listRes : []
  } catch (err) {
    showToast({ type: 'fail', message: err.message || '邀请码数据加载失败' })
  } finally {
    loading.value = false
  }
}

async function copyCode() {
  const code = summary.value.inviteCode
  if (!code) {
    showToast('暂无邀请码')
    return
  }
  try {
    await navigator.clipboard.writeText(code)
    showToast({ type: 'success', message: '邀请码已复制' })
  } catch {
    showToast({ type: 'fail', message: '复制失败，请手动复制' })
  }
}

function formatTime(value) {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN', { hour12: false })
}

onMounted(loadData)
</script>

<style scoped>
.invite-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #fff7fb 0%, #f7f9fc 100%);
  padding: 12px 12px 90px;
}

.header {
  display: grid;
  grid-template-columns: 28px 1fr 28px;
  align-items: center;
  margin-bottom: 10px;
}

.header h1 {
  margin: 0;
  text-align: center;
  font-size: 18px;
  color: #111827;
}

.placeholder {
  width: 28px;
}

.card {
  background: #fff;
  border-radius: 16px;
  padding: 14px;
  margin-bottom: 12px;
  box-shadow: 0 8px 22px rgba(15, 23, 42, 0.08);
}

.label {
  margin: 0;
  color: #6b7280;
  font-size: 13px;
}

.code {
  margin: 8px 0 0;
  font-size: 28px;
  letter-spacing: 0.08em;
  font-weight: 700;
  color: #ef5d8e;
}

.count {
  margin: 8px 0 0;
  font-size: 13px;
  color: #4b5563;
}

.actions {
  margin-top: 12px;
}

h3 {
  margin: 0 0 10px;
  font-size: 16px;
  color: #111827;
}

ul {
  margin: 0;
  padding-left: 18px;
  color: #4b5563;
  font-size: 13px;
  line-height: 1.7;
}

.state {
  color: #9ca3af;
  font-size: 13px;
  padding: 12px 0;
}

.invitee-list {
  display: grid;
  gap: 10px;
}

.invitee-item {
  border: 1px solid #f1f5f9;
  border-radius: 12px;
  padding: 10px;
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.name {
  margin: 0;
  color: #111827;
  font-weight: 600;
}

.meta,
.time {
  margin: 4px 0 0;
  color: #6b7280;
  font-size: 12px;
}
</style>



