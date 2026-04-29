<template>
  <section class="groups-page">
    <div class="groups-panel">
      <header class="groups-head">
        <div>
          <h1>团体</h1>
          <p>寻找属于你的团体，连接更多志同道合的伙伴</p>
        </div>
        <div class="head-actions">
          <router-link to="/platform/groups/my" class="secondary-link">我的团体</router-link>
          <button type="button" class="create-btn" @click="openCreateDialog">
            <span aria-hidden="true">+</span>
            创建团体
          </button>
        </div>
      </header>

      <div class="toolbar-row">
        <form class="search-bar" role="search" @submit.prevent="loadGroups">
          <span aria-hidden="true">⌕</span>
          <input v-model.trim="searchKeyword" type="search" placeholder="搜索团体名称、关键词...">
          <button type="submit">搜索</button>
        </form>
        <div class="selects">
          <select v-model="region" aria-label="地区筛选">
            <option value="">全部地区</option>
            <option value="北京">北京</option>
            <option value="上海">上海</option>
            <option value="广州">广州</option>
            <option value="深圳">深圳</option>
            <option value="全国">全国</option>
          </select>
          <select v-model="category" aria-label="类型筛选">
            <option value="">全部类型</option>
            <option v-for="item in categories" :key="item.value" :value="item.value">{{ item.label }}</option>
          </select>
          <select v-model="sort" aria-label="排序">
            <option value="latest">最新创建</option>
            <option value="members">成员最多</option>
          </select>
        </div>
      </div>

      <div v-if="message" class="page-message" :class="{ error: messageType === 'error' }">{{ message }}</div>

      <div class="groups-body">
        <main class="groups-main">
          <nav class="tabs" aria-label="团体状态">
            <button v-for="tab in tabs" :key="tab.value" type="button" :class="{ active: activeTab === tab.value }" @click="activeTab = tab.value">
              {{ tab.label }}
            </button>
          </nav>

          <div v-if="loading" class="loading-state">加载中...</div>
          <template v-else>
            <div v-if="errors.list" class="error-card">
              <h3>团体列表加载失败</h3>
              <p>{{ errors.list }}</p>
              <button type="button" @click="loadGroups">重试</button>
            </div>
            <div v-else-if="pagedGroups.length" class="groups-grid">
              <router-link v-for="group in pagedGroups" :key="group.id" :to="`/platform/groups/${group.id}`" class="group-card">
                <img :src="group.coverUrl" :alt="group.name">
                <div class="group-info">
                  <div class="title-line">
                    <h2>{{ group.name }}</h2>
                    <span>{{ group.category }}</span>
                  </div>
                  <p class="meta">{{ group.region }} · {{ group.memberCount }} 人</p>
                  <p class="desc">{{ group.description }}</p>
                  <div class="card-foot">
                    <span v-if="group.isMember" class="joined">已加入</span>
                    <span v-else-if="group.hasPendingRequest" class="pending">申请中</span>
                    <button v-else type="button" @click.prevent="applyJoin(group)">申请加入</button>
                  </div>
                </div>
              </router-link>
            </div>
            <div v-else class="empty-card">
              <h3>暂无符合条件的团体</h3>
              <p>换一个筛选条件试试，或创建一个新的团体。</p>
            </div>
          </template>

          <div v-if="totalPages > 1" class="pagination">
            <button type="button" :disabled="page === 1" @click="page -= 1">‹</button>
            <button v-for="num in totalPages" :key="num" type="button" :class="{ active: page === num }" @click="page = num">{{ num }}</button>
            <button type="button" :disabled="page === totalPages" @click="page += 1">›</button>
          </div>
        </main>

        <aside class="groups-aside" aria-label="团体推荐">
          <section class="create-banner">
            <h2>创建属于你的团体</h2>
            <p>建立小组，邀请伙伴，一起在爱中成长。</p>
            <button type="button" @click="openCreateDialog">立即创建</button>
          </section>

          <section class="aside-card">
            <div class="aside-title">
              <h2>热门团体</h2>
            </div>
            <div v-if="sideLoading.hot" class="side-state">热门团体加载中...</div>
            <div v-else-if="errors.hot" class="side-error">{{ errors.hot }}</div>
            <div v-else class="compact-list">
              <router-link v-for="group in popularGroups" :key="group.id" :to="`/platform/groups/${group.id}`" class="compact-item">
                <img :src="group.avatarUrl" :alt="group.name">
                <div>
                  <strong>{{ group.name }}</strong>
                  <span>{{ group.region }} · {{ group.memberCount }} 人</span>
                </div>
                <em>{{ group.isMember ? '已加入' : group.hasPendingRequest ? '申请中' : '查看' }}</em>
              </router-link>
              <div v-if="!popularGroups.length" class="side-state">暂无热门团体</div>
            </div>
          </section>

          <section class="aside-card">
            <div class="aside-title">
              <h2>团体动态</h2>
            </div>
            <div v-if="sideLoading.feed" class="side-state">团体动态加载中...</div>
            <div v-else-if="errors.feed" class="side-error">{{ errors.feed }}</div>
            <div v-else class="activity-list">
              <router-link v-for="item in activities" :key="item.id" :to="`/platform/groups/${item.groupId}/posts`" class="activity-item">
                <img :src="item.avatarUrl" :alt="item.groupName">
                <div>
                  <strong>{{ item.groupName }}</strong>
                  <p>{{ item.text }}</p>
                </div>
                <time>{{ item.time }}</time>
              </router-link>
              <div v-if="!activities.length" class="side-state">暂无团体动态</div>
            </div>
          </section>
        </aside>
      </div>
    </div>

    <transition name="dialog-fade">
      <div v-if="createDialogOpen" class="dialog-mask" @click.self="closeCreateDialog">
        <section class="create-dialog" role="dialog" aria-modal="true" aria-labelledby="create-group-title">
          <button type="button" class="dialog-close" aria-label="关闭创建团体弹窗" @click="closeCreateDialog">×</button>
          <header>
            <h2 id="create-group-title">创建团体</h2>
            <p>填写基础资料后即可创建平台公共团体。</p>
          </header>
          <form class="create-form" @submit.prevent="submitCreateGroup">
            <label>团体名称<input v-model.trim="createForm.name" required maxlength="40" placeholder="请输入团体名称"></label>
            <label>团体类型<select v-model="createForm.type" required><option v-for="item in categories" :key="item.value" :value="item.value">{{ item.label }}</option></select></label>
            <label>所属地区<input v-model.trim="createForm.region" required maxlength="20" placeholder="例如：北京"></label>
            <fieldset>
              <legend>加入方式</legend>
              <label><input v-model="createForm.joinMode" type="radio" value="free"> 自由加入</label>
              <label><input v-model="createForm.joinMode" type="radio" value="audit"> 审核加入</label>
            </fieldset>
            <label>团体简介<textarea v-model.trim="createForm.description" required maxlength="240" rows="4" placeholder="介绍团体定位、适合人群与活动内容"></textarea></label>
            <label>封面图 URL<input v-model.trim="createForm.coverUrl" type="url" placeholder="https://..."></label>
            <p v-if="createError" class="form-error">{{ createError }}</p>
            <div class="dialog-actions">
              <button type="button" class="cancel-btn" @click="closeCreateDialog">取消</button>
              <button type="submit" class="submit-btn" :disabled="creating">{{ creating ? '创建中...' : '创建' }}</button>
            </div>
          </form>
        </section>
      </div>
    </transition>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { createGroup, fetchGroupFeed, fetchGroups, fetchHotGroups, joinGroup } from '@/api/groups.js'

const PAGE_SIZE = 6
const DEFAULT_COVER = 'https://images.unsplash.com/photo-1529156069898-49953e39b3ac?auto=format&fit=crop&w=640&q=80'

const loading = ref(false)
const remoteGroups = ref([])
const hotGroups = ref([])
const groupFeed = ref([])
const searchKeyword = ref('')
const activeTab = ref('all')
const region = ref('')
const category = ref('')
const sort = ref('latest')
const page = ref(1)
const message = ref('')
const messageType = ref('success')
const createDialogOpen = ref(false)
const creating = ref(false)
const createError = ref('')

const createForm = reactive({
  name: '',
  type: 'region',
  region: '',
  joinMode: 'audit',
  description: '',
  coverUrl: ''
})

const errors = reactive({ list: '', hot: '', feed: '' })
const sideLoading = reactive({ hot: false, feed: false })

const tabs = [
  { label: '全部团体', value: 'all' },
  { label: '我加入的', value: 'joined' },
  { label: '我管理的', value: 'managed' },
  { label: '申请中', value: 'pending' }
]

const categories = [
  { label: '地区团体', value: 'region' },
  { label: 'jiaohui团体', value: 'church' },
  { label: '学习小组', value: 'study' },
  { label: '兴趣团体', value: 'interest' },
  { label: '生活团契', value: 'family' },
  { label: '事工团队', value: 'service' }
]

const activities = computed(() => groupFeed.value)
const popularGroups = computed(() => hotGroups.value)

const filteredGroups = computed(() => {
  let list = remoteGroups.value.filter((item) => {
    return activeTab.value === 'all'
      || (activeTab.value === 'joined' && item.isMember)
      || (activeTab.value === 'managed' && item.managed)
      || (activeTab.value === 'pending' && item.hasPendingRequest)
  })
  if (sort.value === 'members') {
    list = [...list].sort((a, b) => b.memberCount - a.memberCount)
  }
  return list
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredGroups.value.length / PAGE_SIZE)))
const pagedGroups = computed(() => filteredGroups.value.slice((page.value - 1) * PAGE_SIZE, page.value * PAGE_SIZE))

watch([activeTab, region, category, sort], () => {
  page.value = 1
  if (region.value || category.value || sort.value) loadGroups()
})

async function loadGroups() {
  loading.value = true
  errors.list = ''
  page.value = 1
  try {
    const data = await fetchGroups(buildListParams())
    remoteGroups.value = unwrapList(data).map(normalizeGroup)
  } catch (error) {
    remoteGroups.value = []
    errors.list = error.message || '无法连接 /api/platform/groups，请确认后端服务和接口路径。'
  } finally {
    loading.value = false
  }
}

async function loadHotGroups() {
  sideLoading.hot = true
  errors.hot = ''
  try {
    const data = await fetchHotGroups()
    hotGroups.value = unwrapList(data).map(normalizeGroup)
  } catch (error) {
    hotGroups.value = []
    errors.hot = error.message || '热门团体接口加载失败。'
  } finally {
    sideLoading.hot = false
  }
}

async function loadGroupFeed() {
  sideLoading.feed = true
  errors.feed = ''
  try {
    const data = await fetchGroupFeed()
    groupFeed.value = unwrapList(data).map(normalizeFeedItem)
  } catch (error) {
    groupFeed.value = []
    errors.feed = error.message || '团体动态接口加载失败。'
  } finally {
    sideLoading.feed = false
  }
}

function buildListParams() {
  return {
    keyword: searchKeyword.value || undefined,
    region: region.value || undefined,
    type: category.value || undefined,
    sort: sort.value === 'latest' ? 'newest' : undefined
  }
}

function unwrapList(res) {
  if (Array.isArray(res)) return res
  if (Array.isArray(res?.data)) return res.data
  return []
}

function normalizeGroup(item) {
  return {
    id: item.id || item.slug,
    name: item.name || '未命名团体',
    category: item.category || item.typeName || item.type || '团体',
    region: item.location || item.region || '未设置地区',
    memberCount: Number(item.memberCount || 0),
    description: item.description || '暂无团体简介',
    coverUrl: item.coverUrl || DEFAULT_COVER,
    avatarUrl: item.avatarUrl || item.coverUrl || DEFAULT_COVER,
    isMember: Boolean(item.isMember),
    managed: Boolean(item.managed),
    hasPendingRequest: Boolean(item.hasPendingRequest),
    createdAt: item.createdAt || ''
  }
}

function normalizeFeedItem(item) {
  return {
    id: item.id,
    groupId: item.groupId,
    groupName: item.groupName || '未命名团体',
    text: item.text || item.content || '发布了新的团体动态',
    time: item.time || formatDate(item.createdAt) || '',
    avatarUrl: item.avatarUrl || DEFAULT_COVER
  }
}

async function applyJoin(group) {
  message.value = ''
  try {
    const res = await joinGroup(group.id)
    group.isMember = Boolean(res?.joined)
    group.hasPendingRequest = Boolean(res?.pending)
    syncGroupStatus(group)
    flashMessage(res?.message || (group.isMember ? '加入成功' : '申请已提交'), 'success')
  } catch (error) {
    flashMessage(error.message || '申请加入失败，请确认已登录并稍后重试。', 'error')
  }
}

function openCreateDialog() {
  createDialogOpen.value = true
  createError.value = ''
}

function closeCreateDialog() {
  if (creating.value) return
  createDialogOpen.value = false
}

async function submitCreateGroup() {
  creating.value = true
  createError.value = ''
  try {
    await createGroup({ ...createForm })
    createDialogOpen.value = false
    resetCreateForm()
    await loadGroups()
    flashMessage('团体创建成功', 'success')
  } catch (error) {
    createError.value = error.message || '创建失败，请稍后重试。'
  } finally {
    creating.value = false
  }
}

function resetCreateForm() {
  createForm.name = ''
  createForm.type = 'region'
  createForm.region = ''
  createForm.joinMode = 'audit'
  createForm.description = ''
  createForm.coverUrl = ''
}

function syncGroupStatus(group) {
  const target = hotGroups.value.find(item => item.id === group.id)
  if (target) {
    target.isMember = group.isMember
    target.hasPendingRequest = group.hasPendingRequest
  }
}

function flashMessage(text, type = 'success') {
  messageType.value = type
  message.value = text
  window.setTimeout(() => {
    message.value = ''
  }, 2200)
}

function formatDate(value) {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  return date.toISOString().slice(0, 10)
}

onMounted(() => {
  loadGroups()
  loadHotGroups()
  loadGroupFeed()
})
</script>

<style scoped>
.groups-page {
  width: calc(100% - var(--lc-space-8));
  margin: var(--lc-space-4) auto 0;
}

.groups-panel,
.feature-strip {
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius);
  background: var(--lc-surface);
  box-shadow: var(--lc-shadow-sm);
}

.groups-panel {
  padding: var(--lc-space-8);
}

.groups-head,
.head-actions,
.section-row,
.dialog-actions {
  display: flex;
  align-items: center;
}

.groups-head {
  justify-content: space-between;
  gap: var(--lc-space-4);
}

.groups-head h1 {
  margin: 0;
  color: var(--lc-text);
  font-size: 30px;
  line-height: 1.15;
}

.groups-head p {
  margin: var(--lc-space-2) 0 0;
  color: var(--lc-muted);
  font-size: var(--lc-text-base);
  font-weight: 700;
}

.head-actions {
  gap: var(--lc-space-3);
}

.create-btn,
.search-bar button,
.create-banner button,
.submit-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 0;
  border-radius: var(--lc-radius-xs);
  color: var(--lc-surface);
  background: linear-gradient(135deg, var(--lc-pink), var(--lc-blue));
  box-shadow: var(--lc-shadow-blue);
  font-weight: 900;
  cursor: pointer;
}

.create-btn {
  gap: var(--lc-space-2);
  min-width: 124px;
  height: 42px;
  padding: 0 var(--lc-space-4);
}

.secondary-link,
.cancel-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 42px;
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius-xs);
  padding: 0 var(--lc-space-4);
  color: var(--lc-blue);
  background: var(--lc-surface);
  text-decoration: none;
  font-weight: 900;
}

.toolbar-row {
  display: grid;
  grid-template-columns: minmax(360px, 620px) auto;
  gap: var(--lc-space-5);
  align-items: center;
  margin-top: var(--lc-space-6);
  padding-bottom: var(--lc-space-4);
  border-bottom: 1px solid var(--lc-border);
}

.search-bar {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) 86px;
  align-items: center;
  height: 40px;
  overflow: hidden;
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius-xs);
  background: var(--lc-surface);
  color: var(--lc-subtle);
}

.search-bar span {
  padding-left: var(--lc-space-3);
}

.search-bar input {
  min-width: 0;
  height: 100%;
  border: 0;
  outline: 0;
  padding: 0 var(--lc-space-3);
  color: var(--lc-text);
  background: transparent;
  font-weight: 700;
}

.search-bar button {
  height: 100%;
  border-radius: 0;
  box-shadow: none;
}

.selects {
  display: flex;
  justify-content: flex-end;
  gap: var(--lc-space-3);
}

.selects select,
.create-form input,
.create-form select,
.create-form textarea {
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius-xs);
  color: var(--lc-text);
  background: var(--lc-surface);
  font: inherit;
  font-weight: 700;
}

.selects select {
  height: 38px;
  min-width: 112px;
  padding: 0 var(--lc-space-3);
  color: var(--lc-muted);
}

.groups-body {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 390px;
  gap: var(--lc-space-6);
  margin-top: var(--lc-space-3);
}

.tabs {
  display: flex;
  gap: var(--lc-space-8);
  overflow-x: auto;
  border-bottom: 1px solid var(--lc-border);
}

.tabs button {
  position: relative;
  flex: 0 0 auto;
  height: 48px;
  border: 0;
  background: transparent;
  color: var(--lc-muted);
  font-size: var(--lc-text-base);
  font-weight: 900;
  cursor: pointer;
}

.tabs button.active {
  color: var(--lc-blue);
}

.tabs button.active::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: -1px;
  height: 3px;
  border-radius: 999px;
  background: var(--lc-blue);
}

.page-message,
.form-error {
  margin-top: var(--lc-space-3);
  padding: var(--lc-space-2) var(--lc-space-3);
  border-radius: var(--lc-radius-xs);
  color: var(--lc-green);
  background: var(--lc-green-light);
  font-size: var(--lc-text-sm);
  font-weight: 900;
}

.page-message.error,
.form-error {
  color: var(--lc-red);
  background: var(--lc-red-light);
}

.loading-state,
.empty-card,
.error-card {
  min-height: 240px;
  display: grid;
  place-items: center;
  margin-top: var(--lc-space-4);
  text-align: center;
}

.loading-state,
.empty-card {
  color: var(--lc-muted);
}

.empty-card {
  border: 1px dashed var(--lc-border);
  border-radius: var(--lc-radius-sm);
}

.empty-card h3,
.error-card h3 {
  margin: 0;
  color: var(--lc-text);
}

.empty-card p,
.error-card p {
  margin: var(--lc-space-2) 0 0;
}

.error-card {
  border: 1px solid var(--lc-red-light);
  border-radius: var(--lc-radius-sm);
  color: var(--lc-red);
  background: var(--lc-red-light);
}

.error-card button {
  height: 34px;
  border: 1px solid var(--lc-red);
  border-radius: var(--lc-radius-xs);
  padding: 0 var(--lc-space-4);
  color: var(--lc-red);
  background: var(--lc-surface);
  font-weight: 900;
  cursor: pointer;
}

.groups-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--lc-space-4);
  padding-top: var(--lc-space-4);
}

.group-card {
  display: grid;
  grid-template-columns: 144px minmax(0, 1fr);
  gap: var(--lc-space-4);
  min-height: 150px;
  padding: var(--lc-space-3);
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius-sm);
  background: var(--lc-surface);
  color: inherit;
  text-decoration: none;
  box-shadow: 0 7px 18px rgba(15, 23, 42, 0.04);
  transition: var(--lc-transition);
}

.group-card:hover {
  border-color: var(--lc-blue-border);
  box-shadow: var(--lc-shadow);
  transform: translateY(-1px);
}

.group-card img {
  width: 144px;
  height: 126px;
  border-radius: var(--lc-radius-xs);
  object-fit: cover;
}

.group-info {
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.title-line {
  display: flex;
  align-items: center;
  gap: var(--lc-space-2);
  min-width: 0;
}

.title-line h2 {
  margin: 0;
  min-width: 0;
  overflow: hidden;
  color: var(--lc-text);
  font-size: var(--lc-text-lg);
  line-height: 1.35;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.title-line span,
.joined,
.pending {
  flex: 0 0 auto;
  border-radius: 999px;
  color: var(--lc-blue);
  background: var(--lc-blue-light);
  font-size: var(--lc-text-xs);
  font-weight: 900;
}

.title-line span {
  padding: 3px var(--lc-space-2);
}

.meta,
.desc {
  margin: var(--lc-space-2) 0 0;
  color: var(--lc-muted);
  font-size: var(--lc-text-sm);
  line-height: 1.55;
}

.desc {
  display: -webkit-box;
  min-height: 40px;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.card-foot {
  display: flex;
  justify-content: flex-end;
  margin-top: auto;
}

.card-foot button,
.joined,
.pending {
  min-width: 78px;
  height: 30px;
  border-radius: var(--lc-radius-xs);
  font-size: var(--lc-text-sm);
  font-weight: 900;
}

.card-foot button {
  border: 1px solid var(--lc-blue-border);
  color: var(--lc-blue);
  background: var(--lc-surface);
  cursor: pointer;
}

.joined,
.pending {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.pending {
  color: var(--lc-amber);
  background: var(--lc-amber-light);
}

.pagination {
  display: flex;
  justify-content: center;
  gap: var(--lc-space-2);
  margin-top: var(--lc-space-6);
}

.pagination button {
  min-width: 32px;
  height: 32px;
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius-xs);
  color: var(--lc-muted);
  background: var(--lc-surface);
  font-weight: 900;
}

.pagination button.active {
  color: var(--lc-surface);
  border-color: var(--lc-blue);
  background: var(--lc-blue);
}

.groups-aside {
  display: grid;
  align-content: start;
  gap: var(--lc-space-4);
  padding-top: var(--lc-space-4);
}

.create-banner,
.aside-card {
  border-radius: var(--lc-radius-sm);
}

.create-banner {
  min-height: 136px;
  padding: var(--lc-space-5);
  color: var(--lc-surface);
  background: linear-gradient(135deg, var(--lc-blue), var(--lc-blue-mid));
}

.create-banner h2,
.aside-title h2 {
  margin: 0;
  font-size: var(--lc-text-lg);
}

.create-banner p {
  margin: var(--lc-space-2) 0 var(--lc-space-4);
  color: rgba(255, 255, 255, 0.86);
  font-size: var(--lc-text-sm);
  font-weight: 800;
}

.create-banner button {
  height: 34px;
  padding: 0 var(--lc-space-3);
  color: var(--lc-blue);
  background: var(--lc-surface);
  box-shadow: none;
  font-size: var(--lc-text-sm);
}

.aside-card {
  padding: var(--lc-space-4);
  border: 1px solid var(--lc-border);
  background: var(--lc-surface);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.04);
}

.aside-title {
  margin-bottom: var(--lc-space-3);
}

.compact-list,
.activity-list {
  display: grid;
  gap: var(--lc-space-3);
}

.compact-item,
.activity-item {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr) auto;
  gap: var(--lc-space-3);
  align-items: center;
  color: inherit;
  text-decoration: none;
}

.compact-item img,
.activity-item img {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  object-fit: cover;
}

.compact-item strong,
.activity-item strong {
  display: block;
  overflow: hidden;
  color: var(--lc-text);
  font-size: var(--lc-text-sm);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.compact-item span,
.activity-item p {
  display: block;
  margin: 3px 0 0;
  color: var(--lc-muted);
  font-size: var(--lc-text-xs);
  line-height: 1.35;
}

.compact-item em {
  min-width: 60px;
  padding: var(--lc-space-2);
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius-xs);
  color: var(--lc-blue);
  font-size: var(--lc-text-xs);
  font-style: normal;
  font-weight: 900;
  text-align: center;
}

.side-state,
.side-error {
  padding: var(--lc-space-3);
  border-radius: var(--lc-radius-xs);
  font-size: var(--lc-text-sm);
  font-weight: 800;
}

.side-state {
  color: var(--lc-muted);
  background: var(--lc-bg);
}

.side-error {
  color: var(--lc-red);
  background: var(--lc-red-light);
}

.activity-item {
  grid-template-columns: 42px minmax(0, 1fr) 58px;
}

.activity-item time {
  color: var(--lc-subtle);
  font-size: var(--lc-text-xs);
  text-align: right;
}

.dialog-mask {
  position: fixed;
  inset: 0;
  z-index: var(--lc-z-modal);
  display: grid;
  place-items: center;
  padding: var(--lc-space-5);
  background: rgba(15, 23, 42, 0.36);
}

.create-dialog {
  position: relative;
  width: min(100%, 560px);
  max-height: calc(100vh - 40px);
  overflow: auto;
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius);
  padding: var(--lc-space-6);
  background: var(--lc-surface);
  box-shadow: var(--lc-shadow-lg);
}

.dialog-close {
  position: absolute;
  top: var(--lc-space-4);
  right: var(--lc-space-4);
  width: 34px;
  height: 34px;
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius-xs);
  color: var(--lc-muted);
  background: var(--lc-surface);
  font-size: var(--lc-text-xl);
  cursor: pointer;
}

.create-dialog h2 {
  margin: 0;
  color: var(--lc-text);
}

.create-dialog header p {
  margin: var(--lc-space-2) 0 0;
  color: var(--lc-muted);
}

.create-form {
  display: grid;
  gap: var(--lc-space-4);
  margin-top: var(--lc-space-5);
}

.create-form label {
  display: grid;
  gap: var(--lc-space-2);
  color: var(--lc-text);
  font-weight: 900;
}

.create-form input,
.create-form select {
  height: 40px;
  padding: 0 var(--lc-space-3);
}

.create-form textarea {
  min-height: 96px;
  padding: var(--lc-space-3);
  resize: vertical;
}

.create-form fieldset {
  display: flex;
  gap: var(--lc-space-4);
  margin: 0;
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius-xs);
  padding: var(--lc-space-3);
}

.create-form legend {
  color: var(--lc-text);
  font-weight: 900;
}

.create-form fieldset label {
  display: flex;
  align-items: center;
  gap: var(--lc-space-2);
  color: var(--lc-muted);
}

.dialog-actions {
  justify-content: flex-end;
  gap: var(--lc-space-3);
}

.submit-btn {
  min-width: 88px;
  height: 42px;
  padding: 0 var(--lc-space-5);
}

.submit-btn:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.dialog-fade-enter-active,
.dialog-fade-leave-active {
  transition: opacity 0.18s ease;
}

.dialog-fade-enter-from,
.dialog-fade-leave-to {
  opacity: 0;
}

@media (max-width: 1320px) {
  .groups-body {
    grid-template-columns: 1fr;
  }

  .groups-aside {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 920px) {
  .groups-page {
    width: calc(100% - var(--lc-space-4));
  }

  .groups-panel {
    padding: var(--lc-space-4);
  }

  .toolbar-row {
    grid-template-columns: 1fr;
  }

  .selects {
    justify-content: stretch;
  }

  .selects select {
    flex: 1;
    min-width: 0;
  }

  .groups-grid,
  .groups-aside {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .groups-head,
  .head-actions {
    align-items: stretch;
    flex-direction: column;
  }

  .create-btn,
  .secondary-link {
    width: 100%;
  }

  .search-bar {
    grid-template-columns: auto minmax(0, 1fr);
  }

  .search-bar button {
    display: none;
  }

  .selects {
    flex-direction: column;
  }

  .group-card {
    grid-template-columns: 104px minmax(0, 1fr);
    gap: var(--lc-space-3);
  }

  .group-card img {
    width: 104px;
    height: 104px;
  }

  .title-line {
    align-items: flex-start;
    flex-direction: column;
  }

  .create-form fieldset {
    flex-direction: column;
  }
}
</style>
