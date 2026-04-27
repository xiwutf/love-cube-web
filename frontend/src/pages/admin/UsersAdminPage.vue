<template>
  <section class="admin-page">
    <section class="platform-card">
      <h1 class="platform-title">用户管理</h1>
      <p class="platform-subtitle">展示用户列表并支持角色与状管理</p>
    </section>

    <section class="platform-card" style="margin-top: 12px;">
      <p class="platform-text">当前 {{ users.length }} 位用户</p>
    </section>

    <section class="admin-table-wrap admin-desktop-only">
      <table class="admin-table">
        <thead>
          <tr>
            <th>用户</th>
            <th>手机</th>
            <th>瑙掕壊</th>
            <th>璁よ瘉</th>
            <th>状</th>
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
                <option v-for="opt in roleOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
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
                <button
                  v-if="canForceDelete(item)"
                  class="admin-btn danger"
                  type="button"
                  @click="forceDelete(item)"
                >
                  强制删除
                </button>
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
            <option v-for="opt in roleOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
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
          <button v-if="canForceDelete(item)" class="admin-btn danger" type="button" @click="forceDelete(item)">强制删除</button>
        </div>
      </article>
      <van-empty v-if="!loading && !users.length" description="暂无用户数据" />
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { showConfirmDialog, showToast } from 'vant'
import { forceDeleteAdminUser, getAdminUsers, updateAdminUserRole, updateAdminUserStatus } from '@/api/adminContent.js'
import { useUserStore } from '@/stores/user.js'

const loading = ref(false)
const users = ref([])
const savingRoleUserId = ref(null)
const userStore = useUserStore()

const roleOptions = [
  { value: 'user', label: '普通用户' },
  { value: 'admin', label: '管理员' }
]

const currentAdminRole = computed(() => normalizeRole(userStore.syncCurrentUser()?.role || 'user'))

function normalizeUsers(rows) {
  return (Array.isArray(rows) ? rows : [])
    .map((item) => ({
      userId: item.userId ? item.id,
      username: item.username ? '',
      phone: item.phone ? '',
      role: normalizeRole(item.role ? 'user'),
      verificationStatus: item.verificationStatus ? 'none',
      status: item.status ? 'active',
      createdAt: item.createdAt ? null,
      canForceDelete: !!item.canForceDelete
    }))
    .sort((a, b) => new Date(b.createdAt || 0).getTime() - new Date(a.createdAt || 0).getTime())
}

function normalizeRole(value) {
  const role = String(value || '').trim().toLowerCase()
  return role === 'admin' ? 'admin' : 'user'
}

function canEditRole(item) {
  if (!item?.userId) return false
  return currentAdminRole.value === 'admin'
}

function canForceDelete(item) {
  return !!item?.canForceDelete
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

async function setStatus(item, status) {
  try {
    const result = await updateAdminUserStatus(item.userId, status)
    item.status = result.status
    showToast({ type: 'success', message: result.message || '状已更新' })
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '操作失败' })
  }
}

async function saveRole(item) {
  if (!item?.userId) {
    showToast({ type: 'fail', message: '缺少用户ID，无法保' })
    return
  }
  if (!canEditRole(item)) {
    showToast({ type: 'fail', message: '当前账号无法修改角色' })
    return
  }
  if (!['user', 'admin'].includes(item.role)) {
    showToast({ type: 'fail', message: '浠呭彲璁剧疆 USER/ADMIN' })
    return
  }
  try {
    savingRoleUserId.value = item.userId
    await updateAdminUserRole(item.userId, item.role)
    showToast({ type: 'success', message: '角色已更' })
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '角色更新失败' })
  } finally {
    savingRoleUserId.value = null
  }
}

async function forceDelete(item) {
  if (!canForceDelete(item)) {
    showToast({ type: 'fail', message: '当前账号无强制删除权' })
    return
  }
  try {
    await showConfirmDialog({
      title: '强制删除账号',
      message: `该操作将直接删除用户 ${item.username || `用户${item.userId}`} 的数据库账号，且不可恢复，确认继续吗？`
    })
  } catch {
    return
  }
  try {
    const result = await forceDeleteAdminUser(item.userId)
    users.value = users.value.filter((u) => u.userId !== item.userId)
    showToast({ type: 'success', message: result.message || '用户已删' })
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '强制删除失败' })
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



