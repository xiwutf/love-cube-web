<template>
  <section class="admin-page">
    <section class="platform-card">
      <h1 class="platform-title">用户管理</h1>
      <p class="platform-subtitle">
        展示数据库中的真实用户列表（按注册时间倒序）。
      </p>
    </section>

    <section class="platform-card" style="margin-top: 12px;">
      <p class="platform-text">当前共 {{ users.length }} 位用户</p>
    </section>

    <section class="admin-table-wrap admin-desktop-only">
      <table class="admin-table">
        <thead>
          <tr>
            <th>用户</th>
            <th>手机号</th>
            <th>角色</th>
            <th>认证</th>
            <th>状态</th>
            <th>注册时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in users" :key="item.userId">
            <td>{{ item.username || `用户${item.userId}` }}</td>
            <td>{{ item.phone || '无手机号' }}</td>
            <td>
              <select v-model="item.role" class="admin-select" :disabled="!canEditRole(item)">
                <option v-for="opt in allowedRoleOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
              </select>
            </td>
            <td>{{ item.verificationStatus || 'none' }}</td>
            <td><span class="admin-tag" :class="item.status || 'active'">{{ item.status || 'active' }}</span></td>
            <td>{{ formatDate(item.createdAt) }}</td>
            <td>
              <div class="admin-cell-actions">
                <button
                  class="admin-btn"
                  type="button"
                  :disabled="savingRoleUserId === item.userId || !canEditRole(item)"
                  @click="saveRole(item)"
                >
                  {{ savingRoleUserId === item.userId ? '保存中...' : '保存角色' }}
                </button>
                <button class="admin-btn" type="button" @click="setStatus(item, 'active')">解封</button>
                <button class="admin-btn" type="button" @click="setStatus(item, 'banned')">封禁</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <van-empty v-if="!loading && !users.length" description="暂无用户数据" />
    </section>

    <div class="admin-list admin-mobile-only">
      <article v-for="item in users" :key="item.userId" class="admin-row">
        <div class="admin-row-head">
          <strong>{{ item.username || `用户${item.userId}` }}</strong>
          <span class="admin-tag" :class="item.status || 'active'">{{ item.status || 'active' }}</span>
        </div>
        <p class="admin-row-meta">{{ item.phone || '无手机号' }}</p>
        <div class="admin-row-role">
          <select v-model="item.role" class="admin-select" :disabled="!canEditRole(item)">
            <option v-for="opt in allowedRoleOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
          </select>
          <button
            class="admin-btn"
            type="button"
            :disabled="savingRoleUserId === item.userId || !canEditRole(item)"
            @click="saveRole(item)"
          >
            {{ savingRoleUserId === item.userId ? '保存中...' : '保存角色' }}
          </button>
        </div>
        <p class="admin-row-meta">认证：{{ item.verificationStatus || 'none' }} · 注册：{{ formatDate(item.createdAt) }}</p>
        <div class="admin-toolbar">
          <button class="admin-btn" type="button" @click="setStatus(item, 'active')">解封</button>
          <button class="admin-btn" type="button" @click="setStatus(item, 'banned')">封禁</button>
        </div>
      </article>
      <van-empty v-if="!loading && !users.length" description="暂无用户数据" />
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { showToast } from 'vant'
import { getAdminUsers, updateAdminUserRole } from '@/api/adminContent.js'
import { useUserStore } from '@/stores/user.js'

const loading = ref(false)
const users = ref([])
const savingRoleUserId = ref(null)
const userStore = useUserStore()
const roleOptions = [
  { value: 'user', label: '普通用户' },
  { value: 'admin', label: '管理员' },
  { value: 'super_admin', label: '超级管理员' },
  { value: 'root', label: 'Root' }
]
const rootRoleOptions = roleOptions
const adminRoleOptions = roleOptions.filter((item) => item.value === 'user' || item.value === 'admin')
const currentAdminRole = computed(() => normalizeRole(userStore.syncCurrentUser()?.role || 'user'))
const allowedRoleOptions = computed(() => (currentAdminRole.value === 'root' ? rootRoleOptions : adminRoleOptions))

function normalizeUsers(rows) {
  return (Array.isArray(rows) ? rows : [])
    .map((item) => ({
      userId: item.userId ?? item.id,
      username: item.username ?? '',
      phone: item.phone ?? '',
      role: normalizeRole(item.role ?? 'user'),
      verificationStatus: item.verificationStatus ?? 'none',
      status: item.status ?? 'active',
      createdAt: item.createdAt ?? null
    }))
    .sort((a, b) => new Date(b.createdAt || 0).getTime() - new Date(a.createdAt || 0).getTime())
}

function normalizeRole(value) {
  const role = String(value || '').trim().toLowerCase()
  if (role === 'admin' || role === 'super_admin' || role === 'root') return role
  return 'user'
}

function isHighPrivilegeRole(value) {
  const role = normalizeRole(value)
  return role === 'root' || role === 'super_admin'
}

function canEditRole(item) {
  if (!item?.userId) return false
  if (currentAdminRole.value === 'root') return true
  return !isHighPrivilegeRole(item.role)
}

async function loadUsers() {
  loading.value = true
  try {
    const data = await getAdminUsers()
    users.value = normalizeUsers(data)
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '用户列表加载失败' })
  } finally {
    loading.value = false
  }
}

function formatDate(value) {
  if (!value) return '-'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value)
  return d.toLocaleString('zh-CN', { hour12: false })
}

function setStatus(_item, _status) {
  showToast({ type: 'fail', message: '封禁状态接口待接入（当前仅展示真实用户）' })
}

async function saveRole(item) {
  if (!item?.userId) {
    showToast({ type: 'fail', message: '缺少用户ID，无法保存' })
    return
  }
  if (!canEditRole(item)) {
    showToast({ type: 'fail', message: '仅 ROOT 可修改 ROOT/SUPER_ADMIN 角色' })
    return
  }
  if (currentAdminRole.value !== 'root' && !['user', 'admin'].includes(item.role)) {
    showToast({ type: 'fail', message: '普通管理员仅可设置 USER/ADMIN' })
    return
  }
  try {
    savingRoleUserId.value = item.userId
    await updateAdminUserRole(item.userId, item.role)
    showToast({ type: 'success', message: '角色已更新' })
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '角色更新失败' })
  } finally {
    savingRoleUserId.value = null
  }
}

onMounted(loadUsers)
</script>

<style scoped>
.admin-select {
  min-width: 120px;
  height: 32px;
  border: 1px solid #d8deea;
  border-radius: 6px;
  background: #fff;
  padding: 0 8px;
  color: #1f2937;
}

.admin-row-role {
  margin: 8px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
