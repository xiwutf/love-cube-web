<template>
  <section class="groups-page">
    <div class="groups-panel desktop-shell">
      <header class="hero">
        <div class="hero-text">
          <h1>寻找或创建你的团体</h1>
          <p>你可以加入已有团体，也可以创建自己的团体，成为团体管理者。</p>
        </div>
        <div class="hero-actions">
          <button type="button" class="primary-btn" @click="goCreate">创建团体</button>
          <router-link to="/platform/me/groups" class="secondary-link">我的团体</router-link>
        </div>
      </header>

      <div class="toolbar-row">
        <form class="search-bar" role="search" @submit.prevent="onSearch">
          <span aria-hidden="true">⌕</span>
          <input v-model.trim="searchKeyword" type="search" placeholder="搜索团体名称">
          <button type="submit">搜索</button>
        </form>
        <div class="selects">
          <select v-model="category" aria-label="分类" @change="reloadFirstPage">
            <option value="">全部分类</option>
            <option v-for="item in categories" :key="item.value" :value="item.value">{{ item.label }}</option>
          </select>
          <select v-model="joinMode" aria-label="加入方式" @change="reloadFirstPage">
            <option value="">全部方式</option>
            <option value="open">公开加入</option>
            <option value="audit">审核加入</option>
            <option value="invite">邀请加入</option>
          </select>
          <select v-model="sort" aria-label="排序" @change="reloadFirstPage">
            <option value="newest">最新创建</option>
            <option value="members">成员最多</option>
          </select>
        </div>
      </div>

      <div v-if="message" class="page-message" :class="{ error: messageType === 'error' }">{{ message }}</div>

      <div class="groups-body">
        <main class="groups-main">
          <div v-if="loading" class="loading-state">加载中...</div>
          <template v-else>
            <div v-if="errors.list" class="error-card">
              <h3>团体列表加载失败</h3>
              <p>{{ errors.list }}</p>
              <button type="button" @click="loadGroups">重试</button>
            </div>
            <div v-else-if="groupItems.length" class="groups-grid">
              <article v-for="group in groupItems" :key="group.id" class="group-card">
                <router-link :to="`/platform/groups/${group.id}`" class="card-cover">
                  <img :src="group.coverUrl" :alt="group.name">
                </router-link>
                <div class="group-info">
                  <div class="title-line">
                    <h2>{{ group.name }}</h2>
                    <span class="pill">{{ group.category }}</span>
                  </div>
                  <p class="meta">
                    {{ group.memberCount }} 人
                    <template v-if="group.postCount"> · {{ group.postCount }} 条动态</template>
                    <template v-if="group.lastActiveLabel"> · 活跃 {{ group.lastActiveLabel }}</template>
                    · {{ group.joinModeLabel }}
                    <template v-if="group.ownerName"> · {{ group.ownerName }}</template>
                  </p>
                  <p class="desc">{{ group.description }}</p>
                  <div class="tags" v-if="group.tags">
                    <span v-for="t in group.tagList" :key="t" class="tag">{{ t }}</span>
                  </div>
                  <div class="card-actions">
                    <router-link class="link-detail" :to="`/platform/groups/${group.id}`">查看详情</router-link>
                    <button type="button" class="action-btn" :disabled="actionDisabled(group)" @click="onAction(group)">
                      {{ actionLabel(group) }}
                    </button>
                  </div>
                </div>
              </article>
            </div>
            <div v-else class="empty-card">
              <h3>暂时没有找到合适的团体</h3>
              <p>可以尝试更换筛选条件，或创建一个新团体。</p>
              <button type="button" class="primary-btn small" @click="goCreate">创建团体</button>
            </div>
          </template>

          <div v-if="totalPages > 1" class="pagination">
            <button type="button" :disabled="page === 1" @click="goPage(-1)">←</button>
            <span class="page-info">第 {{ page }} / {{ totalPages }} 页</span>
            <button type="button" :disabled="page === totalPages" @click="goPage(1)">→</button>
          </div>
        </main>

        <aside class="groups-aside">
          <section class="aside-card">
            <div class="aside-title"><h2>热门团体</h2></div>
            <div v-if="sideLoading.hot" class="side-state">加载中...</div>
            <div v-else-if="errors.hot" class="side-error">{{ errors.hot }}</div>
            <div v-else class="compact-list">
              <router-link v-for="group in popularGroups" :key="group.id" :to="`/platform/groups/${group.id}`" class="compact-item">
                <img :src="group.coverUrl" :alt="group.name">
                <div>
                  <strong>{{ group.name }}</strong>
                  <span>{{ group.region }} · {{ group.memberCount }} 人<template v-if="group.postCount"> · {{ group.postCount }} 动态</template></span>
                </div>
              </router-link>
              <div v-if="!popularGroups.length" class="side-state">暂无</div>
            </div>
          </section>

          <section class="aside-card">
            <div class="aside-title"><h2>团体动态</h2></div>
            <div v-if="sideLoading.feed" class="side-state">加载中...</div>
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
              <div v-if="!activities.length" class="side-state">暂无动态</div>
            </div>
          </section>
        </aside>
      </div>
    </div>

    <div class="mobile-shell">
      <header class="m-hero">
        <div>
          <h1>团体广场</h1>
          <p>找到你的团体，或创建属于你的团体</p>
        </div>
        <button type="button" class="m-create" @click="goCreate">创建团体</button>
      </header>

      <form class="m-search" role="search" @submit.prevent="onSearch">
        <span aria-hidden="true">⌕</span>
        <input v-model.trim="searchKeyword" type="search" placeholder="搜索团体名称、地区、标签">
      </form>

      <section class="m-quick-grid" aria-label="快捷入口">
        <router-link class="m-quick-item" to="/platform/me/groups">我的团体</router-link>
        <button type="button" class="m-quick-item" @click="goCreate">创建团体</button>
        <router-link class="m-quick-item" to="/platform/me/groups">加入审核</router-link>
        <router-link class="m-quick-item" to="/platform/me/groups">团体管理</router-link>
      </section>

      <div v-if="message" class="page-message" :class="{ error: messageType === 'error' }">{{ message }}</div>

      <main class="m-list-wrap">
        <div v-if="loading" class="loading-state">加载中...</div>
        <template v-else>
          <div v-if="errors.list" class="error-card">
            <h3>团体列表加载失败</h3>
            <p>{{ errors.list }}</p>
            <button type="button" @click="loadGroups">重试</button>
          </div>

          <section v-else-if="groupItems.length" class="m-group-list">
            <article v-for="group in groupItems" :key="group.id" class="m-group-card">
              <router-link :to="`/platform/groups/${group.id}`" class="m-cover">
                <img :src="group.coverUrl" :alt="group.name">
              </router-link>

              <div class="m-main">
                <div class="m-title-row">
                  <h2>{{ group.name }}</h2>
                  <span class="m-cat">{{ group.category }}</span>
                </div>
                <p class="m-meta">
                  {{ group.region }} · {{ group.memberCount }}人
                  <template v-if="group.postCount"> · {{ group.postCount }}条动态</template>
                </p>
                <p class="m-desc">{{ group.description }}</p>

                <div class="m-statuses">
                  <span class="m-status">{{ group.joinModeLabel }}</span>
                  <span v-if="group.isOwner || group.managed" class="m-status subtle">管理员</span>
                  <span v-if="group.isMember" class="m-status subtle">已加入</span>
                  <span v-if="group.hasPendingRequest" class="m-status subtle">审核中</span>
                </div>

                <div v-if="group.tags" class="m-tags">
                  <span v-for="t in group.tagList.slice(0, 3)" :key="t" class="m-tag">{{ t }}</span>
                </div>

                <div class="m-actions">
                  <router-link class="m-btn ghost" :to="`/platform/groups/${group.id}`">查看团体</router-link>
                  <button type="button" class="m-btn" :disabled="actionDisabled(group)" @click="onAction(group)">
                    {{ mobileActionLabel(group) }}
                  </button>
                </div>
              </div>
            </article>
          </section>

          <section v-else class="m-empty">
            <h3>暂无团体，你可以创建第一个团体</h3>
            <button type="button" class="m-btn" @click="goCreate">创建团体</button>
          </section>
        </template>

        <div v-if="totalPages > 1" class="pagination">
          <button type="button" :disabled="page === 1" @click="goPage(-1)">←</button>
          <span class="page-info">第 {{ page }} / {{ totalPages }} 页</span>
          <button type="button" :disabled="page === totalPages" @click="goPage(1)">→</button>
        </div>
      </main>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  fetchGroupFeed,
  fetchGroups,
  fetchHotGroups,
  joinGroup,
  unwrapPlatformGroupList
} from '@/api/groups.js'
import { useUserStore } from '@/stores/user.js'

const PAGE_SIZE = 8
const DEFAULT_COVER = 'https://images.unsplash.com/photo-1529156069898-49953e39b3ac?auto=format&fit=crop&w=640&q=80'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const listTotal = ref(0)
const remoteGroups = ref([])
const hotGroups = ref([])
const groupFeed = ref([])

const searchKeyword = ref('')
const category = ref('')
const joinMode = ref('')
const sort = ref('newest')
const page = ref(1)

const message = ref('')
const messageType = ref('success')
const errors = reactive({ list: '', hot: '', feed: '' })
const sideLoading = reactive({ hot: false, feed: false })

const categories = [
  { label: '地区团体', value: 'region' },
  { label: '教会团体', value: 'church' },
  { label: '学习小组', value: 'study' },
  { label: '兴趣团体', value: 'interest' },
  { label: '生活团契', value: 'family' },
  { label: '事工团队', value: 'service' }
]
const categoryLabelMap = categories.reduce((acc, item) => {
  acc[item.value] = item.label
  return acc
}, {})

const groupItems = computed(() => remoteGroups.value)
const totalPages = computed(() => Math.max(1, Math.ceil(listTotal.value / PAGE_SIZE)))
const activities = computed(() => groupFeed.value)
const popularGroups = computed(() => hotGroups.value)

function joinModeLabel(g) {
  const k = g.joinModeKey || (g.joinMode === 'free' ? 'open' : g.joinMode === 'invite' ? 'invite' : 'audit')
  if (k === 'open') return '公开加入'
  if (k === 'invite') return '邀请加入'
  return '审核加入'
}

function formatLastActive(raw) {
  if (raw == null || raw === '') return ''
  const d = new Date(raw)
  if (Number.isNaN(d.getTime())) return ''
  const diff = Date.now() - d.getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  if (diff < 86400000 * 7) return `${Math.floor(diff / 86400000)} 天前`
  return d.toISOString().slice(0, 10)
}

function normalizeCategory(item) {
  const raw = (item.category || item.typeName || item.type || '团体').toString().trim()
  return categoryLabelMap[raw] || raw
}

function normalizeGroup(item) {
  const joinKey = item.joinModeKey || (item.joinMode === 'free' ? 'open' : item.joinMode === 'invite' ? 'invite' : 'audit')
  const tagStr = item.tags || ''
  const tagList = tagStr
    ? tagStr.split(/[,，]/).map((s) => s.trim()).filter(Boolean).slice(0, 6)
    : []
  return {
    id: item.id,
    name: item.name || '未命名团体',
    category: normalizeCategory(item),
    region: item.location || item.region || '未设置地区',
    memberCount: Number(item.memberCount || 0),
    description: item.description || '暂无团体简介',
    coverUrl: item.coverUrl || DEFAULT_COVER,
    joinMode: item.joinMode,
    joinModeKey: joinKey,
    joinModeLabel: joinModeLabel({ joinModeKey: joinKey, joinMode: item.joinMode }),
    ownerName: item.ownerName || '',
    tags: tagStr,
    tagList,
    isMember: Boolean(item.isMember),
    managed: Boolean(item.managed),
    isOwner: Boolean(item.isOwner),
    hasPendingRequest: Boolean(item.hasPendingRequest),
    postCount: Number(item.postCount || 0),
    lastActiveLabel: formatLastActive(item.lastActiveAt)
  }
}

function actionLabel(g) {
  if (!userStore.isLoggedIn && needsInteraction(g)) return '登录后加入'
  if ((g.isOwner || g.managed) && g.isMember) return '管理'
  if (g.isMember) return '已加入'
  if (g.hasPendingRequest) return '审核中'
  if (g.joinModeKey === 'invite') return '仅限邀请'
  if (g.joinModeKey === 'open') return '加入团体'
  return '申请加入'
}

function mobileActionLabel(g) {
  if ((g.isOwner || g.managed) && g.isMember) return '管理'
  if (g.isMember) return '已加入'
  if (g.hasPendingRequest) return '审核中'
  if (g.joinModeKey === 'invite') return '仅限邀请'
  return '申请加入'
}

function needsInteraction(g) {
  if (g.isMember || g.hasPendingRequest) return false
  return true
}

function actionDisabled(g) {
  if ((g.isOwner || g.managed) && g.isMember) return false
  if (g.joinModeKey === 'invite' && !g.isMember) return true
  if (g.isMember || g.hasPendingRequest) return true
  return false
}

function goCreate() {
  if (!userStore.isLoggedIn) {
    userStore.setPostLoginRedirect('/platform/groups/create')
    router.push('/login')
    return
  }
  router.push('/platform/groups/create')
}

function onAction(group) {
  if (!userStore.isLoggedIn) {
    userStore.setPostLoginRedirect('/platform/groups')
    router.push('/login')
    return
  }
  if (group.isOwner || group.managed) {
    router.push(`/platform/groups/${group.id}`)
    return
  }
  if (group.joinModeKey === 'invite') return
  applyJoin(group)
}

async function applyJoin(group) {
  message.value = ''
  let applyMessage = ''
  if (group.joinModeKey === 'audit') {
    const input = window.prompt('请输入申请说明（必填）', '')
    if (input === null) return
    applyMessage = input.trim()
    if (!applyMessage) {
      flashMessage('请填写申请说明', 'error')
      return
    }
  }
  try {
    const res = await joinGroup(group.id, applyMessage)
    const idx = remoteGroups.value.findIndex((x) => x.id === group.id)
    if (idx >= 0) {
      remoteGroups.value[idx].isMember = Boolean(res?.joined)
      remoteGroups.value[idx].hasPendingRequest = Boolean(res?.pending)
      if (remoteGroups.value[idx].isMember) {
        remoteGroups.value[idx].memberCount += 1
      }
    }
    await Promise.all([loadGroups({ resetPage: false }), loadHotGroups()])
    flashMessage(res?.message || (res?.joined ? '加入成功' : '申请已提交'), 'success')
  } catch (error) {
    flashMessage(error.message || '操作失败', 'error')
  }
}

function flashMessage(text, type = 'success') {
  messageType.value = type
  message.value = text
  window.setTimeout(() => {
    message.value = ''
  }, 2400)
}

function buildListParams() {
  return {
    keyword: searchKeyword.value || undefined,
    category: category.value || undefined,
    joinMode: joinMode.value || undefined,
    sort: sort.value === 'newest' ? 'newest' : undefined,
    page: page.value,
    pageSize: PAGE_SIZE,
    status: undefined
  }
}

async function loadGroups(opts = {}) {
  if (opts.resetPage !== false) page.value = 1
  loading.value = true
  errors.list = ''
  try {
    const data = await fetchGroups(buildListParams())
    const items = unwrapPlatformGroupList(data)
    remoteGroups.value = items.map(normalizeGroup)
    listTotal.value = typeof data?.total === 'number' ? data.total : items.length
  } catch (error) {
    remoteGroups.value = []
    listTotal.value = 0
    errors.list = error.message || '无法加载团体列表'
  } finally {
    loading.value = false
  }
}

async function loadHotGroups() {
  sideLoading.hot = true
  errors.hot = ''
  try {
    const data = await fetchHotGroups()
    const arr = Array.isArray(data) ? data : []
    hotGroups.value = arr.map(normalizeGroup)
  } catch (error) {
    hotGroups.value = []
    errors.hot = error.message || '热门团体加载失败'
  } finally {
    sideLoading.hot = false
  }
}

async function loadGroupFeed() {
  sideLoading.feed = true
  errors.feed = ''
  try {
    const data = await fetchGroupFeed()
    const arr = Array.isArray(data) ? data : []
    groupFeed.value = arr.map((item) => ({
      id: item.id,
      groupId: item.groupId,
      groupName: item.groupName || '团体',
      text: item.text || item.content || '',
      time: item.time || '',
      avatarUrl: item.avatarUrl || DEFAULT_COVER
    }))
  } catch (error) {
    groupFeed.value = []
    errors.feed = error.message || '动态加载失败'
  } finally {
    sideLoading.feed = false
  }
}

function onSearch() {
  page.value = 1
  loadGroups({ resetPage: false })
}

function reloadFirstPage() {
  page.value = 1
  loadGroups({ resetPage: false })
}

function goPage(delta) {
  const next = page.value + delta
  const max = Math.max(1, Math.ceil(listTotal.value / PAGE_SIZE))
  if (next < 1 || next > max) return
  page.value = next
  loadGroups({ resetPage: false })
}

onMounted(() => {
  loadGroups({ resetPage: false })
  loadHotGroups()
  loadGroupFeed()
})
</script>

<style scoped>
.groups-page {
  width: calc(100% - var(--lc-space-8));
  margin: var(--lc-space-4) auto 0;
}

.mobile-shell {
  display: none;
}

.groups-panel {
  padding: var(--lc-space-8);
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius);
  background: var(--lc-surface);
  box-shadow: var(--lc-shadow-sm);
}

.hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--lc-space-4);
  flex-wrap: wrap;
}

.hero-text h1 {
  margin: 0;
  color: var(--lc-text);
  font-size: 30px;
}

.hero-text p {
  margin: var(--lc-space-2) 0 0;
  color: var(--lc-muted);
  font-weight: 700;
  max-width: 520px;
}

.hero-actions {
  display: flex;
  gap: var(--lc-space-3);
  flex-wrap: wrap;
}

.primary-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 120px;
  height: 42px;
  padding: 0 var(--lc-space-4);
  border: 0;
  border-radius: var(--lc-radius-xs);
  color: var(--lc-surface);
  background: linear-gradient(135deg, var(--lc-pink), var(--lc-blue));
  box-shadow: var(--lc-shadow-blue);
  font-weight: 900;
  cursor: pointer;
}

.primary-btn.small {
  min-width: 100px;
  height: 36px;
  font-size: var(--lc-text-sm);
}

.secondary-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 42px;
  padding: 0 var(--lc-space-4);
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius-xs);
  color: var(--lc-blue);
  background: var(--lc-surface);
  font-weight: 900;
  text-decoration: none;
}

.toolbar-row {
  display: grid;
  grid-template-columns: minmax(280px, 520px) auto;
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
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius-xs);
  overflow: hidden;
  background: var(--lc-surface);
}

.search-bar span {
  padding-left: var(--lc-space-3);
  color: var(--lc-subtle);
}

.search-bar input {
  min-width: 0;
  height: 100%;
  border: 0;
  outline: 0;
  padding: 0 var(--lc-space-3);
  font-weight: 700;
  color: var(--lc-text);
  background: transparent;
}

.search-bar button {
  height: 100%;
  border: 0;
  border-radius: 0;
  color: var(--lc-surface);
  background: linear-gradient(135deg, var(--lc-pink), var(--lc-blue));
  font-weight: 900;
  cursor: pointer;
}

.selects {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: var(--lc-space-3);
}

.selects select {
  height: 38px;
  min-width: 112px;
  padding: 0 var(--lc-space-3);
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius-xs);
  color: var(--lc-muted);
  background: var(--lc-surface);
  font-weight: 700;
}

.page-message {
  margin-top: var(--lc-space-3);
  padding: var(--lc-space-2) var(--lc-space-3);
  border-radius: var(--lc-radius-xs);
  color: var(--lc-green);
  background: var(--lc-green-light);
  font-weight: 900;
  font-size: var(--lc-text-sm);
}

.page-message.error {
  color: var(--lc-red);
  background: var(--lc-red-light);
}

.groups-body {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: var(--lc-space-6);
  margin-top: var(--lc-space-4);
}

.loading-state,
.empty-card,
.error-card {
  min-height: 220px;
  display: grid;
  place-items: center;
  text-align: center;
  margin-top: var(--lc-space-4);
}

.empty-card {
  border: 1px dashed var(--lc-border);
  border-radius: var(--lc-radius-sm);
  color: var(--lc-muted);
}

.empty-card h3 {
  margin: 0;
  color: var(--lc-text);
}

.error-card {
  border: 1px solid var(--lc-red-light);
  background: var(--lc-red-light);
  color: var(--lc-red);
}

.groups-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--lc-space-4);
}

.group-card {
  display: grid;
  grid-template-columns: 140px minmax(0, 1fr);
  gap: var(--lc-space-4);
  padding: var(--lc-space-3);
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius-sm);
  background: var(--lc-surface);
  box-shadow: 0 7px 18px rgba(15, 23, 42, 0.04);
}

.card-cover {
  display: block;
  border-radius: var(--lc-radius-xs);
  overflow: hidden;
  align-self: start;
}

.card-cover img {
  width: 140px;
  height: 120px;
  object-fit: cover;
}

.title-line {
  display: flex;
  align-items: center;
  gap: var(--lc-space-2);
  flex-wrap: wrap;
}

.title-line h2 {
  margin: 0;
  font-size: var(--lc-text-lg);
  color: var(--lc-text);
}

.pill {
  padding: 3px var(--lc-space-2);
  border-radius: 999px;
  font-size: var(--lc-text-xs);
  font-weight: 900;
  color: var(--lc-blue);
  background: var(--lc-blue-light);
}

.meta,
.desc {
  margin: var(--lc-space-2) 0 0;
  color: var(--lc-muted);
  font-size: var(--lc-text-sm);
}

.desc {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--lc-space-2);
  margin-top: var(--lc-space-2);
}

.tag {
  padding: 2px 8px;
  border-radius: 999px;
  font-size: var(--lc-text-xs);
  font-weight: 800;
  color: var(--lc-muted);
  background: var(--lc-bg);
}

.card-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: var(--lc-space-3);
  margin-top: auto;
  padding-top: var(--lc-space-3);
}

.link-detail {
  font-size: var(--lc-text-sm);
  font-weight: 900;
  color: var(--lc-blue);
  text-decoration: none;
}

.action-btn {
  min-width: 88px;
  height: 32px;
  padding: 0 var(--lc-space-3);
  border-radius: var(--lc-radius-xs);
  border: 1px solid var(--lc-blue-border);
  color: var(--lc-blue);
  background: var(--lc-surface);
  font-weight: 900;
  font-size: var(--lc-text-sm);
  cursor: pointer;
}

.action-btn:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--lc-space-4);
  margin-top: var(--lc-space-6);
}

.pagination button {
  min-width: 36px;
  height: 36px;
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius-xs);
  background: var(--lc-surface);
  font-weight: 900;
  cursor: pointer;
}

.page-info {
  color: var(--lc-muted);
  font-weight: 800;
  font-size: var(--lc-text-sm);
}

.groups-aside {
  display: grid;
  gap: var(--lc-space-4);
  align-content: start;
}

.aside-card {
  padding: var(--lc-space-4);
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius-sm);
  background: var(--lc-surface);
}

.aside-title h2 {
  margin: 0;
  font-size: var(--lc-text-lg);
}

.compact-list,
.activity-list {
  display: grid;
  gap: var(--lc-space-3);
  margin-top: var(--lc-space-3);
}

.compact-item,
.activity-item {
  display: grid;
  grid-template-columns: 40px minmax(0, 1fr);
  gap: var(--lc-space-3);
  align-items: center;
  color: inherit;
  text-decoration: none;
}

.compact-item img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.compact-item strong {
  display: block;
  font-size: var(--lc-text-sm);
  color: var(--lc-text);
}

.compact-item span {
  font-size: var(--lc-text-xs);
  color: var(--lc-muted);
}

.activity-item {
  grid-template-columns: 40px minmax(0, 1fr) auto;
}

.activity-item img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.activity-item time {
  font-size: var(--lc-text-xs);
  color: var(--lc-subtle);
}

.side-state,
.side-error {
  margin-top: var(--lc-space-3);
  font-size: var(--lc-text-sm);
  font-weight: 800;
}

.side-error {
  color: var(--lc-red);
}

@media (max-width: 1100px) {
  .groups-body {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .groups-page {
    width: 100%;
    margin: 0;
    padding: 0 12px calc(112px + env(safe-area-inset-bottom));
    box-sizing: border-box;
  }

  .desktop-shell {
    display: none;
  }

  .mobile-shell {
    display: block;
  }

  .m-hero {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 10px;
    margin: 12px 0 10px;
  }

  .m-hero h1 {
    margin: 0;
    font-size: 20px;
    line-height: 1.25;
    color: var(--lc-text);
  }

  .m-hero p {
    margin: 4px 0 0;
    font-size: 13px;
    color: var(--lc-muted);
    line-height: 1.4;
  }

  .m-create {
    height: 34px;
    border: 1px solid var(--lc-blue-border);
    background: var(--lc-surface);
    color: var(--lc-blue);
    border-radius: 999px;
    padding: 0 12px;
    font-size: 13px;
    font-weight: 700;
    flex: none;
  }

  .m-search {
    margin-top: 12px;
    height: 42px;
    display: grid;
    grid-template-columns: auto 1fr;
    align-items: center;
    gap: 6px;
    padding: 0 12px;
    border-radius: 14px;
    border: 1px solid var(--lc-border);
    background: var(--lc-surface);
  }

  .m-search span {
    color: var(--lc-subtle);
    font-size: 14px;
  }

  .m-search input {
    border: 0;
    outline: 0;
    font-size: 14px;
    color: var(--lc-text);
    background: transparent;
  }

  .m-quick-grid {
    margin-top: 12px;
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 8px;
  }

  .m-quick-item {
    height: 42px;
    border: 1px solid var(--lc-border);
    border-radius: 12px;
    background: var(--lc-surface);
    display: grid;
    place-items: center;
    color: var(--lc-text);
    text-decoration: none;
    font-size: 13px;
    font-weight: 700;
    padding: 0;
  }

  .m-list-wrap {
    margin-top: 12px;
  }

  .m-group-list {
    display: grid;
    gap: 10px;
  }

  .m-group-card {
    border: 1px solid var(--lc-border);
    background: var(--lc-surface);
    border-radius: 16px;
    padding: 10px;
    display: grid;
    grid-template-columns: 88px minmax(0, 1fr);
    gap: 10px;
  }

  .m-cover {
    border-radius: 12px;
    overflow: hidden;
    display: block;
    height: 88px;
  }

  .m-cover img {
    width: 88px;
    height: 88px;
    object-fit: cover;
  }

  .m-title-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
  }

  .m-title-row h2 {
    margin: 0;
    font-size: 16px;
    color: var(--lc-text);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .m-cat {
    font-size: 11px;
    color: var(--lc-blue);
    background: var(--lc-blue-light);
    border-radius: 999px;
    padding: 2px 8px;
    flex: none;
  }

  .m-meta,
  .m-desc {
    margin: 6px 0 0;
    font-size: 13px;
    color: var(--lc-muted);
  }

  .m-desc {
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
    line-height: 1.45;
  }

  .m-statuses,
  .m-tags {
    margin-top: 6px;
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
  }

  .m-status,
  .m-tag {
    font-size: 11px;
    padding: 2px 8px;
    border-radius: 999px;
    background: var(--lc-bg);
    color: var(--lc-muted);
  }

  .m-status.subtle {
    opacity: 0.78;
  }

  .m-actions {
    margin-top: 8px;
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 8px;
  }

  .m-btn {
    height: 34px;
    border-radius: 10px;
    border: 1px solid var(--lc-blue-border);
    background: var(--lc-blue);
    color: var(--lc-surface);
    font-size: 13px;
    font-weight: 700;
    text-decoration: none;
    display: grid;
    place-items: center;
    padding: 0;
  }

  .m-btn.ghost {
    background: var(--lc-surface);
    color: var(--lc-blue);
  }

  .m-btn:disabled {
    opacity: 0.65;
  }

  .m-empty {
    border: 1px dashed var(--lc-border);
    border-radius: 16px;
    padding: 20px 14px;
    text-align: center;
    color: var(--lc-muted);
  }

  .m-empty h3 {
    margin: 0 0 10px;
    color: var(--lc-text);
    font-size: 15px;
    line-height: 1.45;
    font-weight: 700;
  }

  .pagination {
    margin-top: 12px;
  }
}
</style>
