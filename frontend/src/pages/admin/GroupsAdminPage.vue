<template>
  <section class="admin-page">
    <section class="platform-card">
      <div class="page-head">
        <div>
          <h1 class="platform-title">团体管理</h1>
          <p class="platform-subtitle">管理平台公共团体、成员审核、动态与公告。</p>
        </div>
        <router-link to="/admin/platform/groups/create" class="admin-btn primary create-btn">新建团体</router-link>
      </div>

      <div class="filters">
        <label class="search-box">
          <span>搜索</span>
          <input v-model.trim="keyword" class="admin-input" type="search" placeholder="团体名称、简介">
        </label>
        <label>
          <span>地区</span>
          <input v-model.trim="region" class="admin-input" type="search" placeholder="输入地区筛选">
        </label>
        <label>
          <span>类型</span>
          <select v-model="category" class="admin-input">
            <option value="">全部类型</option>
            <option v-for="item in categories" :key="item" :value="item">{{ item }}</option>
          </select>
        </label>
        <label>
          <span>状态</span>
          <select v-model="status" class="admin-input">
            <option value="">全部状态</option>
            <option value="active">启用</option>
            <option value="inactive">禁用</option>
            <option value="published">已发布</option>
            <option value="disabled">已禁用</option>
          </select>
        </label>
      </div>
    </section>

    <div v-if="message" class="admin-message" :class="{ error: messageType === 'error' }">{{ message }}</div>
    <div v-if="loading" class="admin-loading">加载中...</div>
    <div v-else-if="error" class="admin-error">
      {{ error }}
      <button class="admin-btn" type="button" @click="load">重试</button>
    </div>

    <section v-else class="admin-table-wrap admin-desktop-only">
      <table class="admin-table">
        <thead>
          <tr>
            <th>团体</th>
            <th>地区</th>
            <th>类型</th>
            <th>加入方式</th>
            <th>成员数</th>
            <th>待审核</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in filteredItems" :key="item.id">
            <td>
              <div class="group-name-cell">
                <img v-if="item.coverUrl" :src="item.coverUrl" alt="">
                <span v-else class="group-thumb-placeholder">{{ item.name.charAt(0) }}</span>
                <div>
                  <strong>{{ item.name }}</strong>
                  <small>{{ item.id }}</small>
                </div>
              </div>
            </td>
            <td>{{ item.region || '-' }}</td>
            <td>{{ item.category || '-' }}</td>
            <td><span class="admin-tag" :class="item.joinType">{{ joinTypeLabel(item.joinType) }}</span></td>
            <td>{{ item.memberCount }}</td>
            <td>
              <span v-if="item.pendingRequestCount > 0" class="pending-badge">{{ item.pendingRequestCount }}</span>
              <span v-else>-</span>
            </td>
            <td><span class="admin-tag" :class="statusClass(item.status)">{{ statusLabel(item.status) }}</span></td>
            <td>
              <div class="admin-cell-actions">
                <router-link :to="`/admin/platform/groups/${item.id}`" class="admin-btn">详情</router-link>
                <button class="admin-btn" type="button" :disabled="saving" @click="toggleStatus(item)">
                  {{ isEnabled(item.status) ? '禁用' : '启用' }}
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="!filteredItems.length" class="empty-hint">暂无符合条件的团体</div>
    </section>

    <div v-if="!loading && !error" class="admin-list admin-mobile-only">
      <article v-for="item in filteredItems" :key="item.id" class="admin-row">
        <div class="admin-row-head">
          <strong>{{ item.name }}</strong>
          <span class="admin-tag" :class="statusClass(item.status)">{{ statusLabel(item.status) }}</span>
        </div>
        <p class="admin-row-meta">{{ item.region || '未设置地区' }} · {{ item.category || '未分类' }} · {{ item.memberCount }} 位成员</p>
        <p class="admin-row-meta">{{ joinTypeLabel(item.joinType) }}<span v-if="item.pendingRequestCount"> · 待审核 {{ item.pendingRequestCount }}</span></p>
        <div class="admin-cell-actions">
          <router-link :to="`/admin/platform/groups/${item.id}`" class="admin-btn">详情</router-link>
          <button class="admin-btn" type="button" :disabled="saving" @click="toggleStatus(item)">{{ isEnabled(item.status) ? '禁用' : '启用' }}</button>
        </div>
      </article>
      <div v-if="!filteredItems.length" class="empty-hint">暂无符合条件的团体</div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getAdminGroups, updateAdminGroup } from '@/api/groups.js'

const loading = ref(true)
const saving = ref(false)
const error = ref('')
const message = ref('')
const messageType = ref('success')
const items = ref([])
const keyword = ref('')
const region = ref('')
const category = ref('')
const status = ref('')

const categories = computed(() => {
  const values = items.value.map(item => item.category).filter(Boolean)
  return [...new Set(values)]
})

const filteredItems = computed(() => {
  const kw = keyword.value.toLowerCase()
  const area = region.value.toLowerCase()
  return items.value.filter((item) => {
    const byKeyword = !kw || item.name.toLowerCase().includes(kw) || item.description.toLowerCase().includes(kw)
    const byRegion = !area || item.region.toLowerCase().includes(area)
    const byCategory = !category.value || item.category === category.value
    const byStatus = !status.value || item.status === status.value
    return byKeyword && byRegion && byCategory && byStatus
  })
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    const data = await getAdminGroups()
    items.value = unwrapList(data).map(normalizeGroup)
  } catch (err) {
    items.value = []
    error.value = err.message || '团体列表加载失败'
  } finally {
    loading.value = false
  }
}

async function toggleStatus(item) {
  saving.value = true
  const nextStatus = isEnabled(item.status) ? disabledStatus(item.status) : enabledStatus(item.status)
  try {
    const updated = await updateAdminGroup(item.id, { status: nextStatus })
    Object.assign(item, normalizeGroup({ ...item, ...updated }))
    flash(`${item.name} 已${isEnabled(item.status) ? '启用' : '禁用'}`)
  } catch (err) {
    flash(err.message || '状态更新失败', 'error')
  } finally {
    saving.value = false
  }
}

function normalizeGroup(item) {
  return {
    id: String(item.id || ''),
    name: item.name || '未命名团体',
    description: item.description || '',
    region: item.region || item.location || '',
    category: item.category || item.typeName || item.type || '',
    coverUrl: item.coverUrl || '',
    status: item.status || 'active',
    joinType: item.joinType || (item.joinMode === 'free' ? 'open' : 'approval'),
    memberCount: Number(item.memberCount || 0),
    pendingRequestCount: Number(item.pendingRequestCount || 0),
    pinned: Boolean(item.pinned),
    createdAt: item.createdAt || '',
    updatedAt: item.updatedAt || ''
  }
}

function unwrapList(res) {
  if (Array.isArray(res)) return res
  if (Array.isArray(res?.data)) return res.data
  return []
}

function isEnabled(value) {
  return value === 'active' || value === 'published'
}

function enabledStatus(value) {
  return value === 'disabled' ? 'published' : 'active'
}

function disabledStatus(value) {
  return value === 'published' ? 'disabled' : 'inactive'
}

function statusClass(value) {
  return isEnabled(value) ? 'active' : 'inactive'
}

function statusLabel(value) {
  return isEnabled(value) ? '启用' : '禁用'
}

function joinTypeLabel(value) {
  return value === 'open' ? '自由加入' : '审核加入'
}

function flash(text, type = 'success') {
  message.value = text
  messageType.value = type
  window.setTimeout(() => {
    message.value = ''
  }, 2200)
}

onMounted(load)
</script>

<style scoped>
.page-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.create-btn {
  flex-shrink: 0;
  text-decoration: none;
}

.filters {
  display: grid;
  grid-template-columns: minmax(220px, 1.4fr) repeat(3, minmax(140px, 1fr));
  gap: 14px;
  margin-top: 20px;
}

.filters label {
  display: grid;
  gap: 7px;
  color: var(--lc-muted);
  font-size: 13px;
  font-weight: 800;
}

.group-name-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.group-name-cell img,
.group-thumb-placeholder {
  width: 44px;
  height: 32px;
  border-radius: 6px;
  flex-shrink: 0;
}

.group-name-cell img {
  object-fit: cover;
}

.group-thumb-placeholder {
  display: grid;
  place-items: center;
  color: var(--lc-surface);
  background: linear-gradient(135deg, var(--lc-pink), var(--lc-blue));
  font-weight: 900;
}

.group-name-cell strong,
.group-name-cell small {
  display: block;
}

.group-name-cell small {
  margin-top: 2px;
  color: var(--lc-subtle);
  font-size: 11px;
}

.pending-badge {
  display: inline-grid;
  place-items: center;
  min-width: 22px;
  height: 22px;
  padding: 0 6px;
  border-radius: 999px;
  color: var(--lc-surface);
  background: var(--lc-pink);
  font-size: 12px;
  font-weight: 900;
}

.empty-hint {
  padding: 32px;
  color: var(--lc-muted);
  text-align: center;
  font-weight: 800;
}

.admin-message {
  margin: 12px 0;
  padding: 10px 12px;
  border-radius: 8px;
  color: var(--lc-green);
  background: var(--lc-green-light);
  font-size: 13px;
  font-weight: 900;
}

.admin-message.error {
  color: var(--lc-red);
  background: var(--lc-red-light);
}

.admin-cell-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.admin-tag.active,
.admin-tag.open {
  color: var(--lc-green);
  background: var(--lc-green-light);
}

.admin-tag.inactive {
  color: var(--lc-muted);
  background: var(--lc-soft);
}

.admin-tag.approval {
  color: var(--lc-blue);
  background: var(--lc-blue-light);
}

@media (max-width: 980px) {
  .filters {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 640px) {
  .page-head {
    flex-direction: column;
  }

  .filters {
    grid-template-columns: 1fr;
  }
}
</style>
