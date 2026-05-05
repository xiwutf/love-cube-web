<template>
  <section class="admin-page">
    <section class="platform-card">
      <h1 class="platform-title">用户管理</h1>
      <p class="platform-subtitle">展示用户列表并支持角色与状态管理</p>
    </section>

    <section class="platform-card users-stats-card">
      <p class="platform-text">
        当前 {{ filteredUsers.length }} / {{ users.length }} 位用户
        （第 {{ currentPage }} / {{ totalPages }} 页）
      </p>
      <div class="users-overview">
        <span class="overview-chip">已认证 {{ stats.verifiedCount }}</span>
        <span class="overview-chip">男性 {{ stats.maleCount }}</span>
        <span class="overview-chip">女性 {{ stats.femaleCount }}</span>
        <span class="overview-chip">未知性别 {{ stats.unknownGenderCount }}</span>
        <span class="overview-chip">已开通联谊 {{ stats.fellowshipEnabledCount }}</span>
        <span class="overview-chip">已上传图片 {{ stats.hasPhotosCount }}</span>
        <span class="overview-chip">近7天活跃 {{ stats.recentLoginCount }}</span>
      </div>
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
        <select v-model="filters.gender" class="admin-select">
          <option value="">性别全部</option>
          <option value="male">男性</option>
          <option value="female">女性</option>
          <option value="unknown">未知</option>
        </select>
        <select v-model="filters.hasPhotos" class="admin-select">
          <option value="">图片全部</option>
          <option value="yes">已上传图片</option>
          <option value="no">未上传图片</option>
        </select>
        <button class="admin-btn" type="button" @click="resetFilters">重置筛选</button>
      </div>
    </section>

    <section class="admin-table-wrap admin-desktop-only">
      <table class="admin-table users-table">
        <thead>
          <tr>
            <th>用户</th>
            <th>手机</th>
            <th>性别</th>
            <th>角色</th>
            <th>认证</th>
            <th>状态</th>
            <th>联谊</th>
            <th>地区</th>
            <th>最近登录</th>
            <th>注册时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in paginatedUsers" :key="item.userId">
            <td class="col-user">{{ item.username || `用户${item.userId}` }}</td>
            <td class="col-phone">{{ item.phone || '无手机号' }}</td>
            <td><span class="admin-tag" :class="genderTagClass(item.gender)">{{ genderLabel(item.gender) }}</span></td>
            <td>
              <select v-model="item.role" class="admin-select" :disabled="!canEditRole(item)">
                <option v-for="opt in roleOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
              </select>
            </td>
            <td><span class="admin-tag" :class="verificationTagClass(item.verificationStatus)">{{ verificationLabel(item.verificationStatus) }}</span></td>
            <td><span class="admin-tag" :class="item.status || 'active'">{{ item.status || 'active' }}</span></td>
            <td>
              <span class="admin-tag" :class="item.fellowshipEnabled ? 'active' : 'disabled'">
                {{ item.fellowshipEnabled ? '已开通' : '未开通' }}
              </span>
            </td>
            <td class="col-location">{{ item.location || '-' }}</td>
            <td class="col-login">{{ formatDate(item.lastLoginAt) }}</td>
            <td class="col-date">{{ formatDate(item.createdAt) }}</td>
            <td>
              <button class="admin-btn primary" type="button" @click="openDetailDialog(item)">用户详情</button>
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
        <p class="admin-row-meta">性别：{{ genderLabel(item.gender) }}</p>
        <p class="admin-row-meta">认证：{{ verificationLabel(item.verificationStatus) }} · 联谊：{{ item.fellowshipEnabled ? '已开通' : '未开通' }}</p>
        <p class="admin-row-meta">地区：{{ item.location || '-' }} · 登录：{{ formatDate(item.lastLoginAt) }}</p>
        <p class="admin-row-meta">注册：{{ formatDate(item.createdAt) }}</p>
        <div class="admin-toolbar">
          <button class="admin-btn primary" type="button" @click="openDetailDialog(item)">用户详情</button>
        </div>
      </article>
      <van-empty v-if="!loading && !filteredUsers.length" description="暂无匹配用户" />
    </div>

    <AdminDetailDialogShell
      :visible="detailDialog.visible"
      :title="detailDialog.title"
      :loading="detailDialog.loading"
      :max-width="960"
      @update:visible="onDetailDialogVisible"
    >
      <section class="user-detail-wrap">
        <div class="user-detail-meta">
          <p><strong>用户ID：</strong>{{ detailDialog.user?.userId || '-' }}</p>
          <p><strong>用户名：</strong>{{ detailDialog.user?.username || '-' }}</p>
          <p><strong>手机号：</strong>{{ detailDialog.user?.phone || '-' }}</p>
          <p><strong>角色：</strong>{{ detailDialog.user?.role || '-' }}</p>
          <p><strong>状态：</strong>{{ detailDialog.user?.status || '-' }}</p>
          <p><strong>联谊：</strong>{{ detailDialog.user?.fellowshipEnabled ? '已开通' : '未开通' }}</p>
        </div>
        <div v-if="detailDialog.user" class="user-detail-actions">
          <div class="user-detail-action-card">
            <p class="user-detail-action-title">账号设置</p>
            <div class="user-detail-role-row">
              <select v-model="detailDialog.user.role" class="admin-select" :disabled="!canEditRole(detailDialog.user)">
                <option v-for="opt in roleOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
              </select>
              <button
                class="admin-btn"
                type="button"
                :disabled="savingRoleUserId === detailDialog.user.userId || !canEditRole(detailDialog.user)"
                @click="saveRole(detailDialog.user)"
              >
                {{ savingRoleUserId === detailDialog.user.userId ? '保存中...' : '保存角色' }}
              </button>
            </div>
            <div class="user-action-group user-detail-actions-grid">
              <button class="admin-btn" type="button" @click="setStatus(detailDialog.user, 'active')">解封</button>
              <button class="admin-btn" type="button" @click="setStatus(detailDialog.user, 'banned')">封禁</button>
              <button class="admin-btn" type="button" @click="setFellowship(detailDialog.user, true)">开通联谊</button>
              <button class="admin-btn" type="button" @click="setFellowship(detailDialog.user, false)">关闭联谊</button>
            </div>
          </div>
          <div class="user-detail-action-card user-detail-action-card-danger">
            <p class="user-detail-action-title">高风险操作</p>
            <p class="user-detail-action-hint">执行前请确认用户身份及申诉记录，操作不可逆或影响登录。</p>
            <div class="user-action-group user-detail-actions-grid user-detail-actions-grid-danger">
              <button
                v-if="canResetPassword(detailDialog.user)"
                class="admin-btn warning"
                type="button"
                @click="resetPassword(detailDialog.user)"
              >
                重置密码
              </button>
              <button
                v-if="canForceDelete(detailDialog.user)"
                class="admin-btn danger"
                type="button"
                @click="forceDelete(detailDialog.user)"
              >
                强制删除
              </button>
            </div>
          </div>
        </div>
        <div class="user-photo-header">
          <h4>上传图片（{{ detailDialog.photos.length }}）</h4>
          <div class="user-photo-filters">
            <button
              class="admin-btn"
              :class="{ primary: detailDialog.photoFilter === 'all' }"
              type="button"
              @click="detailDialog.photoFilter = 'all'"
            >
              全部
            </button>
            <button
              class="admin-btn"
              :class="{ primary: detailDialog.photoFilter === 'active' }"
              type="button"
              @click="detailDialog.photoFilter = 'active'"
            >
              仅正常
            </button>
          </div>
        </div>
        <div v-if="detailDialog.loading" class="admin-loading user-detail-loading">图片加载中...</div>
        <div v-else-if="!filteredDetailPhotos.length" class="user-photo-empty">当前筛选下暂无图片</div>
        <div v-else class="user-photo-grid">
          <a
            v-for="photo in filteredDetailPhotos"
            :key="photo.id"
            class="user-photo-card"
            :href="resolvePhotoUrl(photo.photoUrl)"
            target="_blank"
            rel="noopener noreferrer"
          >
            <img :src="resolvePhotoUrl(photo.photoUrl)" alt="用户上传图片" loading="lazy">
            <span class="user-photo-badge">{{ formatPhotoStatus(photo.status) }}</span>
            <span v-if="photo.primary" class="user-photo-primary">主图</span>
            <span class="user-photo-time">{{ formatDate(photo.createdAt) }}</span>
          </a>
        </div>
      </section>
    </AdminDetailDialogShell>

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
import AdminDetailDialogShell from '@/components/admin/AdminDetailDialogShell.vue'
import {
  forceDeleteAdminUser,
  getAdminUserPhotos,
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
  fellowshipEnabled: '',
  gender: '',
  hasPhotos: ''
})
const detailDialog = reactive({
  visible: false,
  loading: false,
  user: null,
  title: '用户详情',
  photos: [],
  photoFilter: 'all'
})
const filteredDetailPhotos = computed(() => {
  const list = Array.isArray(detailDialog.photos) ? detailDialog.photos : []
  if (detailDialog.photoFilter === 'active') {
    return list.filter((item) => String(item.status || '').toUpperCase() === 'ACTIVE')
  }
  return list
})

const roleOptions = [
  { value: 'user', label: '普通用户' },
  { value: 'admin', label: '管理员' }
]

const currentAdminRole = computed(() => normalizeRole(userStore.syncCurrentUser()?.role || 'user'))
const stats = computed(() => {
  let verifiedCount = 0
  let maleCount = 0
  let femaleCount = 0
  let unknownGenderCount = 0
  let fellowshipEnabledCount = 0
  let hasPhotosCount = 0
  let recentLoginCount = 0
  for (const item of users.value) {
    if (isVerified(item.verificationStatus)) verifiedCount += 1
    if (item.fellowshipEnabled) fellowshipEnabledCount += 1
    if (item.hasUploadedPhotos) hasPhotosCount += 1
    if (item.gender === 'male') maleCount += 1
    else if (item.gender === 'female') femaleCount += 1
    else unknownGenderCount += 1
    if (isRecentLogin(item.lastLoginAt)) recentLoginCount += 1
  }
  return { verifiedCount, maleCount, femaleCount, unknownGenderCount, fellowshipEnabledCount, hasPhotosCount, recentLoginCount }
})

const filteredUsers = computed(() => {
  const keyword = filters.keyword.trim().toLowerCase()
  return users.value.filter((item) => {
    if (filters.role && item.role !== filters.role) return false
    if (filters.status && (item.status || 'active') !== filters.status) return false
    if (filters.fellowshipEnabled === 'enabled' && !item.fellowshipEnabled) return false
    if (filters.fellowshipEnabled === 'disabled' && item.fellowshipEnabled) return false
    if (filters.gender && item.gender !== filters.gender) return false
    if (filters.hasPhotos === 'yes' && !item.hasUploadedPhotos) return false
    if (filters.hasPhotos === 'no' && item.hasUploadedPhotos) return false
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
      gender: normalizeGender(item.gender ?? item.sex ?? item.userGender),
      role: normalizeRole(item.role || 'user'),
      verificationStatus: item.verificationStatus || 'none',
      status: item.status || 'active',
      fellowshipEnabled: Boolean(item.fellowshipEnabled),
      hasUploadedPhotos: Boolean(item.hasUploadedPhotos ?? Number(item.uploadedPhotoCount || 0) > 0),
      uploadedPhotoCount: Number(item.uploadedPhotoCount || 0),
      location: item.location || item.city || item.region || '',
      lastLoginAt: item.lastLoginAt || item.lastActiveAt || item.loginAt || null,
      createdAt: item.createdAt || null,
      canForceDelete: !!item.canForceDelete,
      canResetPassword: !!item.canResetPassword
    }))
    .sort((a, b) => new Date(b.createdAt || 0).getTime() - new Date(a.createdAt || 0).getTime())
}

function normalizeGender(value) {
  const raw = String(value ?? '').trim().toLowerCase()
  if (['male', 'man', 'm', '1', 'boy', '男'].includes(raw)) return 'male'
  if (['female', 'woman', 'f', '2', 'girl', '女'].includes(raw)) return 'female'
  return 'unknown'
}

function genderLabel(value) {
  return value === 'male' ? '男性' : value === 'female' ? '女性' : '未知'
}

function genderTagClass(value) {
  return value === 'male' ? 'active' : value === 'female' ? 'pending' : 'disabled'
}

function isVerified(value) {
  const raw = String(value || '').toLowerCase()
  return ['verified', 'approved', 'passed', 'success', 'done', 'active', 'completed', '1', 'true'].includes(raw)
}

function isRecentLogin(value) {
  if (!value) return false
  const time = new Date(value).getTime()
  if (Number.isNaN(time)) return false
  return Date.now() - time <= 7 * 24 * 60 * 60 * 1000
}

function verificationLabel(value) {
  const raw = String(value || '').toLowerCase()
  if (['none', '', 'unverified', 'not_verified', '0', 'false'].includes(raw)) return '未认证'
  if (['pending', 'reviewing', 'processing'].includes(raw)) return '审核中'
  if (['rejected', 'failed', 'denied'].includes(raw)) return '未通过'
  if (isVerified(raw)) return '已认证'
  return value || '未认证'
}

function verificationTagClass(value) {
  const raw = String(value || '').toLowerCase()
  if (['pending', 'reviewing', 'processing'].includes(raw)) return 'pending'
  if (['rejected', 'failed', 'denied'].includes(raw)) return 'rejected'
  if (isVerified(raw)) return 'active'
  return 'disabled'
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
  filters.gender = ''
  filters.hasPhotos = ''
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

function onDetailDialogVisible(visible) {
  if (!visible) {
    detailDialog.visible = false
    return
  }
  detailDialog.visible = true
}

async function openDetailDialog(item) {
  detailDialog.visible = true
  detailDialog.user = item
  detailDialog.title = `用户详情：${item.username || `用户${item.userId}`}`
  detailDialog.photos = []
  detailDialog.photoFilter = 'all'
  detailDialog.loading = true
  try {
    const data = await getAdminUserPhotos(item.userId)
    const photos = Array.isArray(data?.photos) ? data.photos : []
    detailDialog.photos = photos.sort((a, b) => {
      const aPrimary = Boolean(a?.primary)
      const bPrimary = Boolean(b?.primary)
      if (aPrimary !== bPrimary) return aPrimary ? -1 : 1
      return Number(a?.sortOrder || 0) - Number(b?.sortOrder || 0)
    })
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '用户图片加载失败' })
  } finally {
    detailDialog.loading = false
  }
}

function resolvePhotoUrl(url) {
  const value = String(url || '').trim()
  if (!value) return ''
  if (/^https?:\/\//i.test(value)) return value
  if (value.startsWith('/admin/uploads/')) return value
  if (value.startsWith('/uploads/')) return `/admin${value}`
  if (value.startsWith('/')) return value
  return `/admin/uploads/photos/${value}`
}

function formatPhotoStatus(status) {
  const raw = String(status || '').toUpperCase()
  if (raw === 'ACTIVE') return '正常'
  if (raw === 'DISABLED') return '禁用'
  return raw || '未知'
}

onMounted(loadUsers)

watch([() => filters.keyword, () => filters.role, () => filters.status, () => filters.fellowshipEnabled, () => filters.gender, () => filters.hasPhotos], () => {
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
  height: 38px;
}

.users-table {
  min-width: 1400px;
}

.users-table th {
  white-space: nowrap;
}

.users-table td {
  vertical-align: middle;
}

.admin-filter-card {
  margin-top: 14px;
}

.admin-pagination-card {
  margin-top: 14px;
}

.users-stats-card {
  margin-top: 14px;
}

.admin-filter-bar {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) repeat(4, minmax(120px, 160px)) auto;
  gap: 10px;
  align-items: center;
  padding: 2px;
}

.admin-filter-bar :deep(.admin-select),
.admin-filter-bar .admin-filter-input {
  box-shadow: inset 0 0 0 1px rgba(148, 163, 184, 0.08);
}

.admin-filter-input {
  height: 38px;
  border: 1px solid var(--lc-border);
  border-radius: 8px;
  background: var(--lc-surface);
  padding: 0 12px;
  color: var(--lc-text);
}

.users-table :deep(.admin-select) {
  min-width: 92px;
  width: 100%;
}

.users-table .col-user,
.users-table .col-phone,
.users-table .col-location,
.users-table .col-login,
.users-table .col-date {
  white-space: nowrap;
}

.users-table .col-user {
  min-width: 160px;
  font-weight: 600;
}

.users-table .col-phone {
  min-width: 140px;
}

.users-overview {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.overview-chip {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid var(--lc-border);
  background: var(--lc-soft);
  font-size: 12px;
  color: var(--lc-muted);
  font-weight: 600;
}

.users-table .col-date {
  min-width: 170px;
  color: var(--lc-muted);
}

.users-table .col-location {
  min-width: 120px;
  color: var(--lc-muted);
}

.users-table .col-login {
  min-width: 170px;
  color: var(--lc-muted);
}

.user-action-group {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 6px;
}

.user-action-group:nth-child(2) {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.user-action-group:first-child {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.user-action-group-danger {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.user-actions :deep(.admin-btn),
.user-action-group :deep(.admin-btn) {
  width: 100%;
  min-height: 32px;
  padding: 0 10px;
  font-size: 12px;
  border-radius: 8px;
}

.admin-filter-bar :deep(.admin-btn) {
  height: 38px;
  border-radius: 8px;
}

.user-detail-wrap {
  display: grid;
  gap: 12px;
}

.user-detail-meta {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px 12px;
  padding: 12px;
  border: 1px solid var(--lc-border);
  border-radius: 10px;
  background: var(--lc-soft);
}

.user-detail-meta p {
  margin: 0;
  font-size: 13px;
  color: var(--lc-muted);
}

.user-detail-actions {
  display: grid;
  gap: 10px;
}

.user-detail-action-card {
  display: grid;
  gap: 8px;
  padding: 12px;
  border: 1px solid var(--lc-border);
  border-radius: 10px;
  background: var(--lc-surface);
}

.user-detail-action-card-danger {
  border-color: rgba(248, 113, 113, 0.35);
  background: linear-gradient(180deg, rgba(254, 242, 242, 0.82), rgba(255, 255, 255, 0.95));
}

.user-detail-action-title {
  margin: 0;
  font-size: 13px;
  color: var(--lc-slate);
  font-weight: 700;
}

.user-detail-action-hint {
  margin: 0;
  font-size: 12px;
  color: var(--lc-subtle);
  line-height: 1.5;
}

.user-detail-role-row {
  display: grid;
  grid-template-columns: minmax(160px, 220px) minmax(100px, 120px);
  gap: 8px;
  align-items: center;
}

.user-detail-actions-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.user-detail-actions-grid-danger {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.user-photo-header h4 {
  margin: 0;
  color: var(--lc-text);
  font-size: 15px;
}

.user-photo-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  flex-wrap: wrap;
}

.user-photo-filters {
  display: inline-flex;
  gap: 6px;
}

.user-photo-filters :deep(.admin-btn) {
  min-height: 30px;
  padding: 0 10px;
  font-size: 12px;
}

.user-detail-loading {
  margin-top: 0;
  padding: 24px 0;
}

.user-photo-empty {
  padding: 18px;
  border: 1px dashed var(--lc-border);
  border-radius: 10px;
  color: var(--lc-subtle);
  background: #fff;
  text-align: center;
}

.user-photo-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.user-photo-card {
  position: relative;
  display: block;
  border: 1px solid var(--lc-border);
  border-radius: 10px;
  overflow: hidden;
  background: #fff;
}

.user-photo-card img {
  width: 100%;
  aspect-ratio: 1 / 1;
  object-fit: cover;
  display: block;
}

.user-photo-badge {
  position: absolute;
  right: 8px;
  bottom: 8px;
  border-radius: 999px;
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 700;
  color: #fff;
  background: rgba(15, 23, 42, 0.7);
}

.user-photo-primary {
  position: absolute;
  left: 8px;
  top: 8px;
  border-radius: 999px;
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 700;
  color: #fff;
  background: rgba(37, 99, 235, 0.82);
}

.user-photo-time {
  position: absolute;
  left: 8px;
  bottom: 8px;
  font-size: 11px;
  color: #fff;
  text-shadow: 0 1px 2px rgba(15, 23, 42, 0.7);
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
  color: var(--lc-muted-light);
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
  color: var(--lc-slate);
  font-weight: 600;
  font-size: 13px;
}

@media (max-width: 1023px) {
  .admin-filter-bar {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .admin-filter-bar :deep(.admin-btn) {
    grid-column: 1 / -1;
  }

  .user-detail-meta {
    grid-template-columns: 1fr 1fr;
  }

  .user-photo-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .user-detail-role-row,
  .user-detail-actions-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>



