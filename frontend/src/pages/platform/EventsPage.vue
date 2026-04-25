<template>
  <section class="platform-page">
    <section class="platform-card">
      <p class="platform-kicker">Events</p>
      <h1 class="platform-title">平台活动</h1>
      <p class="platform-subtitle">活动页提供报名信息、时间地点与注意事项。</p>
    </section>

    <div class="platform-list">
      <router-link
        v-for="item in list"
        :key="item.id"
        :to="`/events/${item.id}`"
        class="platform-card platform-item-link"
      >
        <p class="platform-meta">{{ formatDate(item.eventTime, true) }} · {{ item.location }}</p>
        <h3 class="platform-heading">{{ item.title }}</h3>
        <p class="platform-text">{{ item.summary }}（报名 {{ item.signupCount || 0 }} 人）</p>
      </router-link>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { fetchEvents } from '@/api/platformContent.js'

const list = ref([])

function formatDate(value, withTime = false) {
  if (!value) return ''
  const normalized = String(value).replace('T', ' ')
  return withTime ? normalized.slice(0, 16) : normalized.slice(0, 10)
}

onMounted(async () => {
  list.value = await fetchEvents({ status: 'published' })
})
</script>
