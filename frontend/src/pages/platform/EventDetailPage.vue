<template>
  <section class="platform-page">
    <router-link to="/events" class="platform-backlink">← 返回活动列表</router-link>

    <article v-if="item" class="platform-card platform-block">
      <p class="platform-meta">{{ formatDate(item.eventTime, true) }} · {{ item.location }}</p>
      <h1 class="platform-title">{{ item.title }}</h1>
      <p class="platform-subtitle">{{ item.summary }}</p>
      <p class="platform-text">{{ item.content }}</p>
      <p class="platform-text">当前报名人数：{{ item.signupCount || 0 }}</p>
    </article>

    <div v-else class="platform-card platform-empty">
      <h2 class="platform-heading">未找到活动</h2>
      <p class="platform-text">该活动可能已下线，或链接无效。</p>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { fetchEventDetail } from '@/api/platformContent.js'

const route = useRoute()
const item = ref(null)

function formatDate(value, withTime = false) {
  if (!value) return ''
  const normalized = String(value).replace('T', ' ')
  return withTime ? normalized.slice(0, 16) : normalized.slice(0, 10)
}

onMounted(async () => {
  try {
    item.value = await fetchEventDetail(route.params.id)
  } catch {
    item.value = null
  }
})
</script>
