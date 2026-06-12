<template>
  <section class="platform-page home-config-page">
    <header class="platform-card page-hero">
      <p class="platform-kicker">Release notes</p>
      <h1 class="platform-title">更新日志</h1>
      <p class="platform-subtitle">
        按版本记录你能直接感受到的功能与体验优化；后台技术变更不在此展示。
      </p>
      <router-link class="back-link" to="/">← 返回首页</router-link>
    </header>

    <section class="platform-card list-card" aria-label="版本列表">
      <div v-if="loading" class="state-muted">加载中…</div>
      <ul v-else-if="changelog.length" class="log-list">
        <li v-for="item in changelog" :key="`${item.version}-${item.title}`" class="log-row">
          <span class="version">{{ item.version }}</span>
          <div class="log-body">
            <strong>{{ item.title }}</strong>
            <time v-if="item.date">{{ item.date }}</time>
            <p v-if="item.detail" class="log-detail">{{ item.detail }}</p>
          </div>
        </li>
      </ul>
      <p v-else class="state-muted">暂无更新日志条目。</p>
    </section>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { fetchHomeConfig } from '@/api/platformContent.js'

const loading = ref(true)
const changelog = ref([])

onMounted(async () => {
  try {
    const data = await fetchHomeConfig()
    changelog.value = Array.isArray(data?.changelog) ? data.changelog : []
  } catch {
    changelog.value = []
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

.log-list {
  margin: 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: var(--lc-space-3);
}

.log-row {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  gap: var(--lc-space-4);
  align-items: start;
  padding-bottom: var(--lc-space-3);
  border-bottom: 1px solid var(--lc-border);
}

.log-row:last-child {
  padding-bottom: 0;
  border-bottom: 0;
}

.version {
  color: var(--lc-green);
  font-size: 14px;
  font-weight: 950;
  white-space: nowrap;
}

.log-body {
  display: grid;
  gap: 4px;
  min-width: 0;
}

.log-body strong {
  color: var(--lc-text);
  font-size: 15px;
  font-weight: 800;
  line-height: 1.4;
}

.log-body time {
  color: var(--lc-subtle);
  font-size: 12px;
}

.log-detail {
  margin: var(--lc-space-2) 0 0;
  color: var(--lc-subtle);
  font-size: 14px;
  line-height: 1.65;
  white-space: pre-line;
}

.state-muted {
  margin: 0;
  color: var(--lc-subtle);
  font-size: 14px;
}
</style>
