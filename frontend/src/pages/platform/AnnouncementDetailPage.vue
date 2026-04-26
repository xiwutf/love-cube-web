<template>
  <section class="platform-page module-page">
    <div class="detail-nav-row">
      <router-link to="/announcements" class="platform-backlink">返回公告列表</router-link>
      <router-link to="/articles" class="platform-link">查看精选内容</router-link>
    </div>

    <article v-if="item" class="platform-card detail-article">
      <p class="module-card-meta">发布时间：{{ formatDate(item.publishDate || item.date) }}</p>
      <h1 class="detail-title">{{ item.title || '未命名公告' }}</h1>
      <p class="detail-lead">{{ item.summary || '暂无摘要信息' }}</p>
      <div class="detail-body">
        <p>{{ item.content || '暂无正文内容。' }}</p>
      </div>
    </article>

    <article v-else class="platform-card module-empty">
      <h3 class="platform-heading">公告不存在</h3>
      <p class="platform-text">可能已下线或链接有误，请返回公告列表查看。</p>
    </article>
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
  return String(value).replace('T', ' ').slice(0, 16)
}

onMounted(async () => {
  try {
    item.value = await fetchAnnouncementDetail(route.params.id)
  } catch {
    item.value = null
  }
})
</script>
