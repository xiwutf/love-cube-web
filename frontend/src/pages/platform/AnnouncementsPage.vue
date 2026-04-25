<template>
  <section class="platform-page">
    <section class="platform-card">
      <p class="platform-kicker">Announcements</p>
      <h1 class="platform-title">平台公告</h1>
      <p class="platform-subtitle">平台制度更新、活动通知与运营说明会在此统一发布。</p>
    </section>

    <div class="platform-list">
      <router-link
        v-for="item in list"
        :key="item.id"
        :to="`/announcements/${item.id}`"
        class="platform-card platform-item-link"
      >
        <p class="platform-meta">{{ formatDate(item.publishDate) }}</p>
        <h3 class="platform-heading">{{ item.title }}</h3>
        <p class="platform-text">{{ item.summary }}</p>
      </router-link>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { fetchAnnouncements } from '@/api/platformContent.js'

const list = ref([])

function formatDate(value) {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(0, 10)
}

onMounted(async () => {
  list.value = await fetchAnnouncements({ status: 'published' })
})
</script>
