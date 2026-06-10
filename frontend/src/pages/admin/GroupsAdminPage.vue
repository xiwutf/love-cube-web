<template>
  <section class="admin-page">
    <section class="platform-card">
      <div class="page-head">
        <div>
          <h1 class="platform-title">团体管理</h1>
          <p class="platform-subtitle">管理平台公共团体、成员审核、动态与公告。</p>
          <p class="scope-hint">数据口径：此页展示全站团体（平台监管视角）。</p>
        </div>
        <router-link to="/admin/platform/groups/create" class="admin-btn primary create-btn">新建团体</router-link>
      </div>

      <div v-if="platformStats" class="stats-grid">
        <article class="stat-card">
          <strong>{{ platformStats.publishedGroups }}</strong>
          <span>已发布团体</span>
        </article>
        <article class="stat-card">
          <strong>{{ platformStats.membersTotal }}</strong>
          <span>成员总数</span>
        </article>
        <article class="stat-card">
          <strong>{{ platformStats.pendingMembers }}</strong>
          <span>待审核入团</span>
        </article>
        <article class="stat-card">
          <strong>{{ platformStats.postsToday }}</strong>
          <span>今日动态</span>
        </article>
        <article class="stat-card">
          <strong>{{ platformStats.weekActiveCheckinUsers }}</strong>
          <span>7日打卡人次</span>
        </article>
        <article class="stat-card">
          <strong>{{ platformStats.activitySignupsLast7d }}</strong>
          <span>7日活动报名</span>
        </article>
      </div>

      <form class="filters" @submit.prevent="load(true)">
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
          <select v-model="type" class="admin-input">
            <option value="">全部类型</option>
            <option v-for="item in typeOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
          </select>
        </label>
        <label>
          <span>状态</span>
          <select v-model="status" class="admin-input">
            <option value="">全部状态</option>
            <option value="active">启用</option>
            <option value="inactive">禁用</option>
          </select>
        </label>
        <label>
          <span>排序</span>
          <select v-model="sort" class="admin-input">
            <option value="members">成员数优先</option>
            <option value="newest">最新创建</option>
          </select>
        </label>
        <button class="admin-btn primary" type="submit">查询</button>
      </form>
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
            <td>{{ labelForGroupCategory(item.category) || '-' }}</td>
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
        </div>
      </article>
      <div v-if="!filteredItems.length" class="empty-hint">暂无符合条件的团体</div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { fetchAdminPlatformGroupStats, getAdminGroups } from '@/api/groups.js'
import { PLATFORM_GROUP_CATEGORY_OPTIONS, labelForGroupCategory } from '@/utils/groupCategories.js'

let _cache = null
let _cacheAt = 0
const CACHE_TTL = 30_000

const loading = ref(true)
const error = ref('')
const message = ref('')
const messageType = ref('success')
const items = ref([])
const keyword = ref('')
const region = ref('')
const type = ref('')
const status = ref('')
const sort = ref('members')
const platformStats = ref(null)

const typeOptions = PLATFORM_GROUP_CATEGORY_OPTIONS

const filteredItems = computed(() => {
  const kw = keyword.value.toLowerCase()
  const area = region.value.toLowerCase()
  return items.value.filter((item) => {
    const byKeyword = !kw || item.name.toLowerCase().includes(kw) || item.description.toLowerCase().includes(kw)
    const byRegion = !area || item.region.toLowerCase().includes(area)
    const byCategory = !type.value || item.type === type.value
    const byStatus = !status.value || item.status === status.value
    return byKeyword && byRegion && byCategory && byStatus
  })
})

async function load(force = false) {
  loading.value = true
  error.value = ''
  try {
    const now = Date.now()
    let groups
    if (!force && _cache && now - _cacheAt < CACHE_TTL) {
      groups = _cache
    } else {
      groups = unwrapList(await getAdminGroups()).map(normalizeGroup)
      _cache = groups
      _cacheAt = now
    }
    items.value = groups
  } catch (err) {
    items.value = []
    error.value = err.message || '团体列表加载失败'
  } finally {
    loading.value = false
  }
}

function normalizeGroup(item) {
  return {
    id: String(item.id || ''),
    name: item.name || '未命名团体',
    description: item.description || '',
    region: item.region || item.location || '',
    type: item.type || '',
    category: item.category || item.typeName || item.type || '',
    coverUrl: item.coverUrl || '',
    status: normalizeStatus(item.status),
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
  if (Array.isArray(res?.items)) return res.items
  if (Array.isArray(res?.data)) return res.data
  return []
}

function normalizeStatus(value) {
  if (value === 'published') return 'active'
  if (value === 'disabled') return 'inactive'
  return value || 'active'
}

function isEnabled(value) {
  return value === 'active'
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

async function loadStats() {
  try {
    platformStats.value = await fetchAdminPlatformGroupStats()
  } catch {
    platformStats.value = null
  }
}

onMounted(() => {
  load()
  loadStats()
})
</script>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 12px;
  margin-bottom: 20px;
}

.stat-card {
  padding: 14px 12px;
  border-radius: 10px;
  border: 1px solid var(--lc-border);
  background: var(--lc-surface-muted, #f8fafc);
  text-align: center;
}

.stat-card strong {
  display: block;
  font-size: 1.35rem;
  color: var(--lc-blue);
}

.stat-card span {
  font-size: 12px;
  color: var(--lc-subtle);
}

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

.scope-hint {
  margin: 8px 0 0;
  font-size: 12px;
  color: var(--lc-subtle);
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
