<template>
  <section class="platform-page home-config-page">
    <header class="platform-card page-hero">
      <p class="platform-kicker">Roadmap</p>
      <h1 class="platform-title">待更新</h1>
      <p class="platform-subtitle">{{ subtitle }}</p>
      <router-link class="back-link" to="/">← 返回首页</router-link>
    </header>

    <section class="platform-card list-card" aria-label="待更新列表">
      <div v-if="loading" class="state-muted">加载中…</div>
      <ul v-else-if="pendingUpdates.length" class="pending-list">
        <li v-for="item in pendingUpdates" :key="item.id || item.title" class="pending-row">
          <div class="pending-headline">
            <strong>{{ item.title }}</strong>
            <em :class="`status-${item.status || 'pending'}`">{{ statusLabel(item.status) }}</em>
          </div>
          <p v-if="item.detail">{{ item.detail }}</p>
          <div v-if="isAdmin && item.id" class="pending-actions">
            <button
              v-if="item.status !== 'published'"
              type="button"
              class="action-btn primary"
              :disabled="saving"
              @click="handlePublish(item)"
            >发布</button>
            <button
              v-else
              type="button"
              class="action-btn"
              :disabled="saving"
              @click="handleOffline(item)"
            >下架</button>
            <router-link class="action-btn ghost" to="/admin/local-resources">去后台编辑</router-link>
          </div>
        </li>
      </ul>
      <p v-else class="state-muted">暂无待更新事项。</p>
    </section>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { showToast } from 'vant'
import { useUserStore } from '@/stores/user.js'
import { getAdminLocalResources, offlineAdminLocalResource, publishAdminLocalResource } from '@/api/adminContent.js'
import { fetchHomeConfig } from '@/api/platformContent.js'

const userStore = useUserStore()
const loading = ref(true)
const saving = ref(false)
const pendingUpdates = ref([])
const isAdmin = computed(() => userStore.isAdmin)
const subtitle = computed(() => (
  isAdmin.value
    ? '你可以在这里快速处理待发布资源，重编辑请进入后台。'
    : '公开已知问题与计划中的改进，状态由运营维护。'
))

function statusLabel(status) {
  const map = {
    pending: '待处理',
    draft: '草稿',
    published: '已发布',
    offline: '已下架'
  }
  return map[status] || (status || '处理中')
}

function normalizeAdminPendingItem(item) {
  return {
    id: item.id,
    title: item.title || '本地资源',
    status: item.status || 'pending',
    detail: [item.type, item.location].filter(Boolean).join(' · ') || '待补充详细信息'
  }
}

async function loadAdminPending() {
  const rows = await getAdminLocalResources()
  pendingUpdates.value = (Array.isArray(rows) ? rows : [])
    .filter(item => ['pending', 'draft', 'offline', 'published'].includes(String(item.status || '')))
    .map(normalizeAdminPendingItem)
    .slice(0, 20)
}

async function loadPublicPending() {
  const data = await fetchHomeConfig()
  pendingUpdates.value = Array.isArray(data?.pendingUpdates) ? data.pendingUpdates : []
}

async function handlePublish(item) {
  saving.value = true
  try {
    const updated = await publishAdminLocalResource(item.id)
    item.status = updated?.status || 'published'
    showToast({ type: 'success', message: '已发布' })
  } catch (error) {
    showToast({ type: 'fail', message: error?.message || '发布失败' })
  } finally {
    saving.value = false
  }
}

async function handleOffline(item) {
  saving.value = true
  try {
    const updated = await offlineAdminLocalResource(item.id)
    item.status = updated?.status || 'offline'
    showToast({ type: 'success', message: '已下架' })
  } catch (error) {
    showToast({ type: 'fail', message: error?.message || '下架失败' })
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  try {
    if (userStore.isLoggedIn) {
      await userStore.refreshCurrentUser().catch(() => {})
      if (!userStore.isAdmin) {
        await userStore.loadAdminContext().catch(() => {})
      }
    }
    if (isAdmin.value) {
      await loadAdminPending()
    } else {
      await loadPublicPending()
    }
  } catch {
    pendingUpdates.value = []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.home-config-page {
  max-width: 800px;
  margin: 0 auto;
  padding: var(--lc-space-5) var(--lc-space-4);
}

.page-hero {
  margin-bottom: var(--lc-space-4);
}

.page-hero .platform-title {
  margin-top: 0;
}

.back-link {
  display: inline-block;
  margin-top: var(--lc-space-3);
  color: var(--lc-blue);
  font-size: var(--lc-text-sm);
  font-weight: 800;
  text-decoration: none;
}

.list-card {
  padding: var(--lc-space-6);
}

.pending-list {
  margin: 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: var(--lc-space-4);
}

.pending-row {
  display: grid;
  gap: 6px;
  padding-bottom: var(--lc-space-3);
  border-bottom: 1px dashed var(--lc-border);
}

.pending-row:last-child {
  padding-bottom: 0;
  border-bottom: 0;
}

.pending-headline {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--lc-space-3);
}

.pending-headline strong {
  color: var(--lc-text);
  font-size: 15px;
  font-weight: 800;
  line-height: 1.35;
}

.pending-headline em {
  flex: 0 0 auto;
  font-style: normal;
  font-size: 12px;
  color: var(--lc-surface);
  background: var(--lc-amber);
  border-radius: 999px;
  padding: 3px 10px;
  font-weight: 800;
}
.pending-headline em.status-published { background: var(--lc-green); }
.pending-headline em.status-offline { background: var(--lc-subtle); }
.pending-headline em.status-draft { background: var(--lc-violet); }

.pending-row p {
  margin: 0;
  color: var(--lc-subtle);
  font-size: 13px;
  line-height: 1.45;
}

.pending-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.action-btn {
  border: 1px solid var(--lc-border);
  background: var(--lc-surface);
  color: var(--lc-text);
  border-radius: 8px;
  height: 32px;
  padding: 0 12px;
  font-size: 12px;
  font-weight: 700;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
}

.action-btn.primary {
  background: var(--lc-blue);
  border-color: var(--lc-blue);
  color: var(--lc-surface);
}

.action-btn.ghost {
  color: var(--lc-blue);
  border-color: var(--lc-blue-border);
}

.state-muted {
  margin: 0;
  color: var(--lc-subtle);
  font-size: 14px;
}
</style>
