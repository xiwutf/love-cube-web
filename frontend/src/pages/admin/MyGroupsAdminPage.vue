<template>
  <section class="admin-page">
    <section class="platform-card">
      <div class="page-head">
        <div>
          <h1 class="platform-title">我的团体</h1>
          <p class="platform-subtitle">管理你负责的团体，可见功能取决于你的团体角色。</p>
        </div>
      </div>
      <label class="search-box">
        <span>搜索</span>
        <input v-model.trim="keyword" class="admin-input" type="search" placeholder="团体名称">
      </label>
    </section>

    <div v-if="loading" class="admin-loading">加载中...</div>
    <div v-else-if="error" class="admin-error">
      {{ error }}
      <button class="admin-btn" type="button" @click="load">重试</button>
    </div>

    <template v-else>
      <div v-if="!filteredItems.length" class="empty-card">
        <p>暂无可管理的团体</p>
        <p class="hint">{{ emptyHintText }}</p>
      </div>

      <section v-else class="groups-grid">
        <article v-for="item in filteredItems" :key="item.id" class="group-card">
          <div class="group-card-cover">
            <img v-if="item.coverUrl" :src="item.coverUrl" :alt="item.name">
            <span v-else class="cover-placeholder">{{ item.name.charAt(0) }}</span>
            <span v-if="item.pendingRequestCount > 0" class="pending-badge">{{ item.pendingRequestCount }} 待审</span>
          </div>

          <div class="group-card-body">
            <div class="group-card-title-row">
              <strong class="group-card-name">{{ item.name }}</strong>
              <span
                class="role-badge"
                :class="item.regulatingAsPlatformAdmin ? 'role-platform' : `role-${(item.userRole || 'owner').toLowerCase()}`"
              >
                {{ item.userRoleName || '团长' }}
              </span>
            </div>

            <p class="group-card-meta">
              <span class="admin-tag" :class="item.joinType === 'open' ? 'open' : 'approval'">
                {{ item.joinType === 'open' ? '自由加入' : '审核加入' }}
              </span>
              <span class="admin-tag" :class="item.status === 'active' ? 'active' : 'inactive'">
                {{ item.status === 'active' ? '启用' : '禁用' }}
              </span>
              <span class="member-count">{{ item.memberCount }} 人</span>
            </p>

            <p v-if="item.description" class="group-card-desc">{{ item.description }}</p>
          </div>

          <div class="group-card-actions">
            <!-- OWNER / ADMIN：编辑团体资料 -->
            <router-link
              v-if="canManage(item)"
              :to="groupTab(item.id, 'info')"
              class="admin-btn"
            >编辑团体</router-link>

            <!-- 所有角色：成员审核 -->
            <router-link
              :to="groupTab(item.id, 'members')"
              class="admin-btn primary"
            >
              成员审核
              <span v-if="item.pendingRequestCount > 0" class="btn-badge">{{ item.pendingRequestCount }}</span>
            </router-link>

            <!-- OWNER / ADMIN：公告 & 动态 -->
            <template v-if="canManage(item)">
              <router-link :to="groupTab(item.id, 'notices')" class="admin-btn">团体公告</router-link>
              <router-link :to="groupTab(item.id, 'posts')"   class="admin-btn">团体动态</router-link>
            </template>

            <!-- OWNER only：管理员设置 -->
            <router-link
              v-if="isOwner(item)"
              :to="groupTab(item.id, 'admins')"
              class="admin-btn"
            >管理员设置</router-link>
          </div>
        </article>
      </section>
    </template>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getAdminGroups } from '@/api/groups.js'
import { useUserStore } from '@/stores/user.js'

const loading = ref(true)
const error = ref('')
const items = ref([])
const keyword = ref('')
const userStore = useUserStore()

const hasManageAllPermission = computed(() =>
  userStore.hasPermission('group.manage.all')
)

const emptyHintText = computed(() =>
  hasManageAllPermission.value
    ? '你当前是全站团体管理员；若仍为空，通常表示当前暂无团体数据。'
    : '如需获得管理权限，请联系超级管理员。'
)

const filteredItems = computed(() => {
  const kw = keyword.value.toLowerCase()
  if (!kw) return items.value
  return items.value.filter(item =>
    item.name.toLowerCase().includes(kw) || (item.description || '').toLowerCase().includes(kw)
  )
})

function groupTab(id, tab) {
  return { path: `/admin/my-groups/${id}`, query: { tab } }
}

function isOwner(item) {
  return item.userPermissions?.includes('group.manage.admins') ?? item.userRole === 'OWNER'
}

function canManage(item) {
  return item.userPermissions?.includes('group.edit.info') ?? item.userRole !== 'REVIEWER'
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    // 确保当前页可读取后台权限，便于空状态给出准确提示。
    await userStore.loadAdminContext()
    const data = await getAdminGroups()
    items.value = (Array.isArray(data) ? data : data?.data ?? []).map(normalizeGroup)
  } catch (err) {
    error.value = err.message || '加载失败'
  } finally {
    loading.value = false
  }
}

function normalizeGroup(item) {
  const regulating = Boolean(item.regulatingAsPlatformAdmin)
  return {
    id: String(item.id || ''),
    name: item.name || '未命名团体',
    description: item.description || '',
    coverUrl: item.coverUrl || '',
    status: item.status || 'active',
    joinType: item.joinType || 'approval',
    memberCount: Number(item.memberCount || 0),
    pendingRequestCount: Number(item.pendingRequestCount || 0),
    userRole: item.userRole ?? (regulating ? null : 'OWNER'),
    userRoleName: regulating ? '平台监管' : (item.userRoleName || '团长'),
    userPermissions: item.userPermissions || [],
    regulatingAsPlatformAdmin: regulating
  }
}

onMounted(load)
</script>

<style scoped>
.page-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.search-box {
  display: grid;
  gap: 6px;
  font-size: 13px;
  font-weight: 700;
  color: var(--lc-muted);
  max-width: 300px;
}

.groups-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
  margin-top: 16px;
}

.group-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.group-card-cover {
  position: relative;
  height: 110px;
  background: linear-gradient(135deg, #fce7f3, #dbeafe);
  flex-shrink: 0;
}

.group-card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  font-size: 40px;
  font-weight: 900;
  color: #94a3b8;
}

.pending-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 3px 8px;
  border-radius: 999px;
  background: #ef4444;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
}

.group-card-body {
  padding: 14px 16px 10px;
  flex: 1;
}

.group-card-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 8px;
}

.group-card-name {
  font-size: 16px;
  font-weight: 800;
  color: #0f172a;
}

.role-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  flex-shrink: 0;
}

.role-badge.role-owner {
  background: #fef3c7;
  color: #b45309;
}

.role-badge.role-admin {
  background: #dbeafe;
  color: #1d4ed8;
}

.role-badge.role-reviewer {
  background: #f1f5f9;
  color: #475569;
}

.role-badge.role-platform {
  background: #ede9fe;
  color: #5b21b6;
}

.group-card-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
  margin: 0 0 8px;
}

.member-count {
  font-size: 12px;
  color: #64748b;
  font-weight: 600;
}

.group-card-desc {
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.group-card-actions {
  padding: 10px 16px 14px;
  border-top: 1px solid #f1f5f9;
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.btn-badge {
  display: inline-grid;
  place-items: center;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: 999px;
  background: #ef4444;
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  margin-left: 3px;
}

.empty-card {
  margin-top: 40px;
  text-align: center;
  color: #64748b;
}

.empty-card p {
  margin: 0 0 8px;
  font-weight: 700;
}

.empty-card .hint {
  font-size: 13px;
  font-weight: 400;
  color: #94a3b8;
}

.admin-tag.active, .admin-tag.open { color: #16a34a; background: #dcfce7; }
.admin-tag.inactive { color: #64748b; background: #f1f5f9; }
.admin-tag.approval { color: #1d4ed8; background: #dbeafe; }
</style>
