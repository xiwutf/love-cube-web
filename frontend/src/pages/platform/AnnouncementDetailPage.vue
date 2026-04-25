<template>
  <section class="platform-page">
    <router-link to="/announcements" class="platform-backlink">← 返回公告列表</router-link>

    <article v-if="item" class="platform-card platform-block">
      <p class="platform-meta">{{ formatDate(item.publishDate) }}</p>
      <h1 class="platform-title">{{ item.title }}</h1>
      <p class="platform-subtitle">{{ item.summary }}</p>
      <p class="platform-text">{{ item.content }}</p>
    </article>

    <div v-else class="platform-card platform-empty">
      <h2 class="platform-heading">未找到公告</h2>
      <p class="platform-text">该公告可能已下线，或链接无效。</p>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { fetchAnnouncementDetail } from '@/api/platformContent.js'

const route = useRoute()
const item = ref(null)

function formatDate(value) {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(0, 10)
}

onMounted(async () => {
  try {
    item.value = await fetchAnnouncementDetail(route.params.id)
  } catch {
    item.value = null
  }
})
</script>
