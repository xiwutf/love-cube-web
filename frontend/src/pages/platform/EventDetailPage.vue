<template>
  <section class="platform-page module-page">
    <div class="detail-nav-row">
      <router-link to="/events" class="platform-backlink">返回活动列表</router-link>
      <router-link to="/fellowship-intro" class="platform-link">查看联谊介绍</router-link>
    </div>

    <article v-if="item" class="platform-card detail-article">
      <p class="module-card-meta">
        活动时间：{{ formatDate(item.eventTime || item.time, true) }} · 地点：{{ item.location || '待定' }}
      </p>
      <h1 class="detail-title">{{ item.title || '未命名活动' }}</h1>
      <p class="detail-lead">{{ item.summary || '暂无活动说明' }}</p>
      <div class="detail-body">
        <p>{{ item.content || '暂无活动详情。' }}</p>
        <div class="detail-stat">当前报名：{{ item.signupCount || 0 }} 人</div>
      </div>
    </article>

    <article v-else class="platform-card module-empty">
      <h3 class="platform-heading">活动不存在</h3>
      <p class="platform-text">该活动可能已下线或链接错误，请返回活动中心查看。</p>
    </article>
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
