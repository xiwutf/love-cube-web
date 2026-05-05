<template>
  <section class="platform-page home-config-page">
    <header class="platform-card page-hero">
      <p class="platform-kicker">Roadmap</p>
      <h1 class="platform-title">待更新</h1>
      <p class="platform-subtitle">
        公开已知问题与计划中的改进，状态由运营维护。
      </p>
      <router-link class="back-link" to="/">← 返回首页</router-link>
    </header>

    <section class="platform-card list-card" aria-label="待更新列表">
      <div v-if="loading" class="state-muted">加载中…</div>
      <ul v-else-if="pendingUpdates.length" class="pending-list">
        <li v-for="item in pendingUpdates" :key="item.id || item.title" class="pending-row">
          <div class="pending-headline">
            <strong>{{ item.title }}</strong>
            <em>{{ item.status || '处理中' }}</em>
          </div>
          <p v-if="item.detail">{{ item.detail }}</p>
        </li>
      </ul>
      <p v-else class="state-muted">暂无待更新事项。</p>
    </section>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { fetchHomeConfig } from '@/api/platformContent.js'

const loading = ref(true)
const pendingUpdates = ref([])

onMounted(async () => {
  try {
    const data = await fetchHomeConfig()
    pendingUpdates.value = Array.isArray(data?.pendingUpdates) ? data.pendingUpdates : []
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

.pending-row p {
  margin: 0;
  color: var(--lc-subtle);
  font-size: 13px;
  line-height: 1.45;
}

.state-muted {
  margin: 0;
  color: var(--lc-subtle);
  font-size: 14px;
}
</style>
