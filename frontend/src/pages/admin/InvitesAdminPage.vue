<template>
  <section class="admin-page">
    <section class="platform-card">
      <h1 class="platform-title">邀请关系</h1>
      <p class="platform-subtitle">按邀请人、被邀请人、邀请码和时间筛选邀请注册记录。</p>
    </section>

    <section class="platform-card filter-card" style="margin-top: 12px;">
      <div class="filter-grid">
        <input v-model="filters.inviterUserId" placeholder="邀请人ID" />
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

    <section class="admin-table-wrap admin-desktop-only">
      <table class="admin-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>邀请码</th>
            <th>邀请人</th>
            <th>被邀请人</th>
            <th>状态</th>
            <th>注册IP</th>
            <th>设备信息</th>
            <th>注册时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in rows" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.inviteCode }}</td>
            <td>{{ item.inviterUserId }} / {{ item.inviterName || '-' }}</td>
            <td>{{ item.inviteeUserId || '-' }} / {{ item.inviteeName || '-' }}</td>
            <td>{{ item.status || '-' }}</td>
            <td>{{ item.registerIp || '-' }}</td>
            <td class="ua">{{ item.registerUserAgent || '-' }}</td>
            <td>{{ formatDate(item.createdAt) }}</td>
          </tr>
        </tbody>
      </table>
      <van-empty v-if="!loading && !rows.length" description="暂无邀请记录" />
    </section>

    <div class="admin-list admin-mobile-only">
      <article v-for="item in rows" :key="item.id" class="admin-row">
        <div class="admin-row-head">
          <strong>{{ item.inviteCode }}</strong>
          <span class="admin-tag active">{{ item.status || '-' }}</span>
        </div>
        <p class="admin-row-meta">邀请人：{{ item.inviterUserId }} / {{ item.inviterName || '-' }}</p>
        <p class="admin-row-meta">被邀请人：{{ item.inviteeUserId || '-' }} / {{ item.inviteeName || '-' }}</p>
        <p class="admin-row-meta">IP：{{ item.registerIp || '-' }}</p>
        <p class="admin-row-meta">设备：{{ item.registerUserAgent || '-' }}</p>
        <p class="admin-row-meta">时间：{{ formatDate(item.createdAt) }}</p>
      </article>
      <van-empty v-if="!loading && !rows.length" description="暂无邀请记录" />
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { showToast } from 'vant'
import { getAdminInvites } from '@/api/invite.js'

const loading = ref(false)
const rows = ref([])
const filters = reactive({
  inviterUserId: '',
  inviteeUserId: '',
  inviteCode: '',
  startTime: '',
  endTime: '',
  status: ''
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
  } catch (err) {
    showToast({ type: 'fail', message: err.message || '邀请记录加载失败' })
  } finally {
    loading.value = false
  }
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

onMounted(loadInvites)
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
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 8px 10px;
  font-size: 13px;
}

.filter-actions {
  display: flex;
  gap: 8px;
}

.ua {
  max-width: 260px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

@media (max-width: 1023px) {
  .filter-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>

