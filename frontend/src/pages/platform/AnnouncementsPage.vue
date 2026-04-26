<template>
  <section class="platform-page module-page">
    <section class="platform-card module-hero">
      <p class="platform-kicker">Platform Bulletin</p>
      <h1 class="platform-title">平台动态公告</h1>
      <p class="platform-subtitle">
        聚合发布平台更新、规则说明、功能上新和活动通知，帮助用户快速了解平台动态。
      </p>
    </section>

    <section class="module-grid">
      <router-link
        v-for="item in list"
        :key="item.id"
        :to="`/announcements/${item.id}`"
        class="platform-card module-card module-card-link"
      >
        <p class="module-card-meta">{{ formatDate(item.publishDate || item.date) }}</p>
        <h3 class="module-card-title">{{ item.title || '未命名公告' }}</h3>
        <p class="module-card-desc">{{ item.summary || '暂无摘要信息' }}</p>
        <span class="module-card-action">查看详情</span>
      </router-link>

      <article v-if="!loading && !list.length" class="platform-card module-empty">
        <h3 class="platform-heading">暂无公告</h3>
        <p class="platform-text">当前还没有已发布公告，稍后再来看看。</p>
      </article>
    </section>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { fetchAnnouncements } from '@/api/platformContent.js'

const loading = ref(false)
const list = ref([])

function formatDate(value) {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(0, 16)
}

onMounted(async () => {
  loading.value = true
  try {
    const data = await fetchAnnouncements({ status: 'published' })
    list.value = Array.isArray(data) ? data : []
  } finally {
    loading.value = false
  }
})
</script>
