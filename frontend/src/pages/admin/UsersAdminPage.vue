<template>
  <section class="admin-page">
    <section class="platform-card">
      <h1 class="platform-title">用户管理</h1>
      <p class="platform-subtitle">展示用户列表并支持角色与状态管理</p>
    </section>

    <section class="platform-card" style="margin-top: 12px;">
      <p class="platform-text">
        当前 {{ filteredUsers.length }} / {{ users.length }} 位用户
        （第 {{ currentPage }} / {{ totalPages }} 页）
      </p>
    </section>

    <section class="platform-card admin-filter-card">
      <div class="admin-filter-bar">
        <input
          v-model.trim="filters.keyword"
          class="admin-filter-input"
          type="text"
          placeholder="搜索用户名 / 手机号 / 用户ID"
        >
        <select v-model="filters.role" class="admin-select">
          <option value="">全部角色</option>
          <option value="user">普通用户</option>
          <option value="admin">管理员</option>
        </select>
        <select v-model="filters.status" class="admin-select">
          <option value="">全部状态</option>
          <option value="active">active</option>
          <option value="banned">banned</option>
        </select>
        <select v-model="filters.fellowshipEnabled" class="admin-select">
          <option value="">联谊全部</option>
          <option value="enabled">已开通</option>
          <option value="disabled">未开通</option>
        </select>
        <button class="admin-btn" type="button" @click="resetFilters">重置筛选</button>
      </div>
    </section>

    <section class="admin-table-wrap admin-desktop-only">
      <table class="admin-table">
        <thead>
          <tr>
            <th>用户</th>
            <th>手机</th>
            <th>角色</th>
            <th>认证</th>
            <th>状态</th>
            <th>联谊</th>
            <th>注册时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in paginatedUsers" :key="item.userId">
            <td>{{ item.username || `用户${item.userId}` }}</td>
            <td>{{ item.phone || '无手机号' }}</td>
            <td>
              <select v-model="item.role" class="admin-select" :disabled="!canEditRole(item)">
                <option v-for="opt in roleOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
              </select>
            </td>
            <td>{{ item.verificationStatus || 'none' }}</td>
            <td><span class="admin-tag" :class="item.status || 'active'">{{ item.status || 'active' }}</span></td>
            <td>
              <span class="admin-tag" :class="item.fellowshipEnabled ? 'active' : 'disabled'">
                {{ item.fellowshipEnabled ? '已开通' : '未开通' }}
              </span>
            </td>
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
                <button class="admin-btn" type="button" @click="setFellowship(item, true)">开通联谊</button>
                <button class="admin-btn" type="button" @click="setFellowship(item, false)">关闭联谊</button>
                <button
                  v-if="canForceDelete(item)"
                  class="admin-btn danger"
                  type="button"
                  @click="forceDelete(item)"
                >
                  强制删除
                </button>
                <button
                  v-if="canResetPassword(item)"
                  class="admin-btn warning"
                  type="button"
                  @click="resetPassword(item)"
                >
                  重置密码
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <van-empty v-if="!loading && !filteredUsers.length" description="暂无匹配用户" />
    </section>

    <div class="admin-list admin-mobile-only">
      <article v-for="item in paginatedUsers" :key="item.userId" class="admin-row">
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
        <p class="admin-row-meta">联谊：{{ item.fellowshipEnabled ? '已开通' : '未开通' }}</p>
        <div class="admin-toolbar">
          <button class="admin-btn" type="button" @click="setStatus(item, 'active')">解封</button>
          <button class="admin-btn" type="button" @click="setStatus(item, 'banned')">封禁</button>
          <button class="admin-btn" type="button" @click="setFellowship(item, true)">开通联谊</button>
          <button class="admin-btn" type="button" @click="setFellowship(item, false)">关闭联谊</button>
          <button v-if="canForceDelete(item)" class="admin-btn danger" type="button" @click="forceDelete(item)">强制删除</button>
          <button v-if="canResetPassword(item)" class="admin-btn warning" type="button" @click="resetPassword(item)">重置密码</button>
        </div>
      </article>
      <van-empty v-if="!loading && !filteredUsers.length" description="暂无匹配用户" />
    </div>

    <section v-if="filteredUsers.length" class="platform-card admin-pagination-card">
      <div class="admin-pagination">
        <label class="admin-page-size">
          每页
          <select v-model.number="pageSize" class="admin-select">
            <option :value="10">10</option>
            <option :value="20">20</option>
            <option :value="50">50</option>
          </select>
          条
        </label>
        <div class="admin-page-actions">
          <button class="admin-btn" type="button" :disabled="currentPage <= 1" @click="goPrevPage">上一页</button>
          <span class="admin-page-indicator">{{ currentPage }} / {{ totalPages }}</span>
          <button class="admin-btn" type="button" :disabled="currentPage >= totalPages" @click="goNextPage">下一页</button>
        </div>
      </div>
    </section>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { showConfirmDialog, showToast } from 'vant'
import {
  forceDeleteAdminUser,
  getAdminUsers,
  resetAdminUserPassword,
  updateAdminUserFellowshipStatus,
  updateAdminUserRole,
  updateAdminUserStatus
} from '@/api/adminContent.js'
import { useUserStore } from '@/stores/user.js'

const loading = ref(false)
const users = ref([])
const savingRoleUserId = ref(null)
const userStore = useUserStore()
const currentPage = ref(1)
const pageSize = ref(10)
const filters = reactive({
  keyword: '',
  role: '',
  status: '',
  fellowshipEnabled: ''
})

const roleOptions = [
  { value: 'user', label: '普通用户' },
  { value: 'admin', label: '管理员' }
]

const currentAdminRole = computed(() => normalizeRole(userStore.syncCurrentUser()?.role || 'user'))
const filteredUsers = computed(() => {
  const keyword = filters.keyword.trim().toLowerCase()
  return users.value.filter((item) => {
    if (filters.role && item.role !== filters.role) return false
    if (filters.status && (item.status || 'active') !== filters.status) return false
    if (filters.fellowshipEnabled === 'enabled' && !item.fellowshipEnabled) return false
    if (filters.fellowshipEnabled === 'disabled' && item.fellowshipEnabled) return false
    if (!keyword) return true
    const username = String(item.username || '').toLowerCase()
    const phone = String(item.phone || '').toLowerCase()
    const userId = String(item.userId || '').toLowerCase()
    return username.includes(keyword) || phone.includes(keyword) || userId.includes(keyword)
  })
})
const totalPages = computed(() => Math.max(1, Math.ceil(filteredUsers.value.length / pageSize.value)))
const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredUsers.value.slice(start, end)
})

function normalizeUsers(rows) {
  return (Array.isArray(rows) ? rows : [])
    .map((item) => ({
      userId: item.userId ?? item.id ?? null,
      username: item.username || '',
      phone: item.phone || '',
      role: normalizeRole(item.role || 'user'),
      verificationStatus: item.verificationStatus || 'none',
      status: item.status || 'active',
      fellowshipEnabled: Boolean(item.fellowshipEnabled),
      createdAt: item.createdAt || null,
      canForceDelete: !!item.canForceDelete,
      canResetPassword: !!item.canResetPassword
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

function canResetPassword(item) {
  return !!item?.canResetPassword
}

function resetFilters() {
  filters.keyword = ''
  filters.role = ''
  filters.status = ''
  filters.fellowshipEnabled = ''
  currentPage.value = 1
}

function goPrevPage() {
  if (currentPage.value > 1) currentPage.value -= 1
}

function goNextPage() {
  if (currentPage.value < totalPages.value) currentPage.value += 1
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
    showToast({ type: 'success', message: result.message || '状态已更新' })
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '操作失败' })
  }
}

async function setFellowship(item, fellowshipEnabled) {
  try {
    const result = await updateAdminUserFellowshipStatus(item.userId, fellowshipEnabled)
    item.fellowshipEnabled = Boolean(result.fellowshipEnabled)
    showToast({ type: 'success', message: result.message || '联谊状态已更新' })
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '操作失败' })
  }
}

async function saveRole(item) {
  if (!item?.userId) {
    showToast({ type: 'fail', message: '缺少用户ID，无法保存' })
    return
  }
  if (!canEditRole(item)) {
    showToast({ type: 'fail', message: '当前账号无法修改角色' })
    return
  }
  if (!['user', 'admin'].includes(item.role)) {
    showToast({ type: 'fail', message: '仅可设置 USER/ADMIN' })
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
    showToast({ type: 'success', message: result.message || '用户已删除' })
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '强制删除失败' })
  }
}

async function resetPassword(item) {
  if (!canResetPassword(item)) {
    showToast({ type: 'fail', message: '当前账号无重置密码权限' })
    return
  }
  try {
    await showConfirmDialog({
      title: '重置密码',
      message: `确认将用户 ${item.username || `用户${item.userId}`} 的密码重置为 123456 吗？`
    })
  } catch {
    return
  }
  try {
    const result = await resetAdminUserPassword(item.userId)
    showToast({ type: 'success', message: result.message || '密码已重置为 123456' })
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '重置密码失败' })
  }
}

onMounted(loadUsers)

watch([() => filters.keyword, () => filters.role, () => filters.status, () => filters.fellowshipEnabled], () => {
  currentPage.value = 1
})

watch(pageSize, () => {
  currentPage.value = 1
})

watch(totalPages, (pages) => {
  if (currentPage.value > pages) {
    currentPage.value = pages
  }
})
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

.admin-filter-card {
  margin-top: 12px;
}

.admin-pagination-card {
  margin-top: 12px;
}

.admin-filter-bar {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) repeat(3, minmax(120px, 160px)) auto;
  gap: 8px;
  align-items: center;
}

.admin-filter-input {
  height: 32px;
  border: 1px solid #d8deea;
  border-radius: 6px;
  background: #fff;
  padding: 0 10px;
  color: #1f2937;
}

.admin-pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.admin-page-size {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #64748b;
  font-size: 13px;
}

.admin-page-actions {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.admin-page-indicator {
  min-width: 56px;
  text-align: center;
  color: #334155;
  font-weight: 600;
  font-size: 13px;
}

@media (max-width: 1023px) {
  .admin-filter-bar {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>



