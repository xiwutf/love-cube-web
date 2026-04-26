<template>
  <section class="platform-page module-page">
    <section class="platform-card module-hero">
      <p class="platform-kicker">Events Center</p>
      <h1 class="platform-title">活动中心</h1>
      <p class="platform-subtitle">
        集中展示线上线下活动，支持快速查看时间、地点和报名情况，形成平台活动运营闭环。
      </p>
    </section>

    <section class="module-grid">
      <router-link
        v-for="item in list"
        :key="item.id"
        :to="`/events/${item.id}`"
        class="platform-card module-card module-card-link"
      >
        <p class="module-card-meta">{{ formatDate(item.eventTime || item.time, true) }} · {{ item.location || '待定' }}</p>
        <h3 class="module-card-title">{{ item.title || '未命名活动' }}</h3>
        <p class="module-card-desc">{{ item.summary || '暂无活动说明' }}</p>
        <p class="module-card-caption">已报名 {{ item.signupCount || 0 }} 人</p>
        <span class="module-card-action">查看活动</span>
      </router-link>

      <article v-if="!loading && !list.length" class="platform-card module-empty">
        <h3 class="platform-heading">暂无活动</h3>
        <p class="platform-text">当前没有已发布活动，后续上新会第一时间展示在这里。</p>
      </article>
    </section>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { fetchEvents } from '@/api/platformContent.js'

const loading = ref(false)
const list = ref([])

function formatDate(value, withTime = false) {
  if (!value) return ''
  const normalized = String(value).replace('T', ' ')
  return withTime ? normalized.slice(0, 16) : normalized.slice(0, 10)
}

onMounted(async () => {
  loading.value = true
  try {
    const data = await fetchEvents({ status: 'published' })
    list.value = Array.isArray(data) ? data : []
  } finally {
    loading.value = false
  }
})
</script>
