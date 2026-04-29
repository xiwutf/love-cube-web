<template>
  <section class="my-groups-page">
    <header class="page-head">
      <div>
        <h1>我的团体</h1>
        <p>管理你加入、负责和正在申请中的团体</p>
      </div>
      <router-link to="/platform/groups">发现团体</router-link>
    </header>

    <div v-if="loading" class="loading-state">加载我的团体中...</div>
    <div v-else-if="error" class="error-card">
      <h2>我的团体加载失败</h2>
      <p>{{ error }}</p>
      <button type="button" @click="loadMyGroups">重试</button>
    </div>

    <template v-else>
      <section v-for="section in sections" :key="section.key" class="group-section">
        <div class="section-head">
          <h2>{{ section.title }}</h2>
          <span>{{ section.items.length }}</span>
        </div>
        <div v-if="section.items.length" class="groups-grid">
          <router-link v-for="group in section.items" :key="group.id" :to="`/platform/groups/${group.id}`" class="group-card">
            <img :src="group.coverUrl" :alt="group.name">
            <div class="group-info">
              <div class="title-line">
                <h3>{{ group.name }}</h3>
                <span>{{ group.category }}</span>
              </div>
              <p class="meta">{{ group.region }} · {{ group.memberCount }} 人</p>
              <p class="desc">{{ group.description }}</p>
              <div class="card-foot">
                <em :class="section.key">{{ section.badge }}</em>
              </div>
            </div>
          </router-link>
        </div>
        <div v-else class="empty-card">
          <h3>{{ section.emptyTitle }}</h3>
          <p>{{ section.emptyText }}</p>
        </div>
      </section>
    </template>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { fetchMyGroups } from '@/api/groups.js'

const DEFAULT_COVER = 'https://images.unsplash.com/photo-1529156069898-49953e39b3ac?auto=format&fit=crop&w=640&q=80'

const loading = ref(false)
const error = ref('')
const groups = ref([])

const joinedGroups = computed(() => groups.value.filter(item => item.isMember && !item.managed))
const managedGroups = computed(() => groups.value.filter(item => item.managed))
const pendingGroups = computed(() => groups.value.filter(item => item.hasPendingRequest))

const sections = computed(() => [
  {
    key: 'joined',
    title: '我加入的团体',
    badge: '已加入',
    items: joinedGroups.value,
    emptyTitle: '还没有加入团体',
    emptyText: '去发现页看看适合你的团体。'
  },
  {
    key: 'managed',
    title: '我管理的团体',
    badge: '管理中',
    items: managedGroups.value,
    emptyTitle: '还没有管理的团体',
    emptyText: '创建团体后，你负责的团体会显示在这里。'
  },
  {
    key: 'pending',
    title: '申请中的团体',
    badge: '申请中',
    items: pendingGroups.value,
    emptyTitle: '暂无申请中的团体',
    emptyText: '提交审核加入申请后，会在这里跟踪状态。'
  }
])

async function loadMyGroups() {
  loading.value = true
  error.value = ''
  try {
    const data = await fetchMyGroups()
    groups.value = unwrapList(data).map(normalizeGroup)
  } catch (err) {
    groups.value = []
    error.value = err.message || '无法连接 /api/platform/groups'
  } finally {
    loading.value = false
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
    isMember: Boolean(item.isMember),
    managed: Boolean(item.managed),
    hasPendingRequest: Boolean(item.hasPendingRequest)
  }
}

onMounted(loadMyGroups)
</script>

<style scoped>
.my-groups-page {
  width: calc(100% - var(--lc-space-8));
  margin: var(--lc-space-4) auto 0;
  padding: var(--lc-space-8);
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius);
  background: var(--lc-surface);
  box-shadow: var(--lc-shadow-sm);
}

.page-head,
.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--lc-space-4);
}

.page-head h1 {
  margin: 0;
  color: var(--lc-text);
  font-size: 30px;
}

.page-head p {
  margin: var(--lc-space-2) 0 0;
  color: var(--lc-muted);
  font-weight: 700;
}

.page-head a {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 42px;
  border-radius: var(--lc-radius-xs);
  padding: 0 var(--lc-space-4);
  color: var(--lc-surface);
  background: linear-gradient(135deg, var(--lc-pink), var(--lc-blue));
  box-shadow: var(--lc-shadow-blue);
  text-decoration: none;
  font-weight: 900;
}

.group-section {
  margin-top: var(--lc-space-8);
}

.section-head {
  margin-bottom: var(--lc-space-4);
  border-bottom: 1px solid var(--lc-border);
  padding-bottom: var(--lc-space-3);
}

.section-head h2 {
  margin: 0;
  color: var(--lc-text);
  font-size: var(--lc-text-lg);
}

.section-head span {
  display: grid;
  place-items: center;
  min-width: 28px;
  height: 28px;
  border-radius: 999px;
  color: var(--lc-blue);
  background: var(--lc-blue-light);
  font-weight: 900;
}

.groups-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--lc-space-4);
}

.group-card {
  display: grid;
  grid-template-columns: 132px minmax(0, 1fr);
  gap: var(--lc-space-4);
  min-height: 140px;
  padding: var(--lc-space-3);
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius-sm);
  color: inherit;
  background: var(--lc-surface);
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
  width: 132px;
  height: 116px;
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

.title-line h3 {
  margin: 0;
  min-width: 0;
  overflow: hidden;
  color: var(--lc-text);
  font-size: var(--lc-text-lg);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.title-line span {
  flex: 0 0 auto;
  border-radius: 999px;
  padding: 3px var(--lc-space-2);
  color: var(--lc-blue);
  background: var(--lc-blue-light);
  font-size: var(--lc-text-xs);
  font-weight: 900;
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
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.card-foot {
  display: flex;
  justify-content: flex-end;
  margin-top: auto;
}

.card-foot em {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 78px;
  height: 30px;
  border-radius: var(--lc-radius-xs);
  color: var(--lc-blue);
  background: var(--lc-blue-light);
  font-size: var(--lc-text-sm);
  font-style: normal;
  font-weight: 900;
}

.card-foot em.pending {
  color: var(--lc-amber);
  background: var(--lc-amber-light);
}

.loading-state,
.empty-card,
.error-card {
  min-height: 220px;
  display: grid;
  place-items: center;
  align-content: center;
  gap: var(--lc-space-2);
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
.error-card h2 {
  margin: 0;
  color: var(--lc-text);
}

.empty-card p,
.error-card p {
  margin: 0;
  color: var(--lc-muted);
}

.error-card {
  margin-top: var(--lc-space-6);
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

@media (max-width: 920px) {
  .my-groups-page {
    width: calc(100% - var(--lc-space-4));
    padding: var(--lc-space-4);
  }

  .groups-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .page-head {
    align-items: stretch;
    flex-direction: column;
  }

  .group-card {
    grid-template-columns: 104px minmax(0, 1fr);
  }

  .group-card img {
    width: 104px;
    height: 104px;
  }

  .title-line {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
