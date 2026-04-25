<template>
  <section class="platform-page">
    <router-link to="/articles" class="platform-backlink">← 返回资讯列表</router-link>

    <article v-if="item" class="platform-card platform-block">
      <p class="platform-meta">{{ item.tag }}</p>
      <h1 class="platform-title">{{ item.title }}</h1>
      <p class="platform-subtitle">{{ item.summary }}</p>
      <p class="platform-text">{{ item.content }}</p>
    </article>

    <div v-else class="platform-card platform-empty">
      <h2 class="platform-heading">未找到资讯</h2>
      <p class="platform-text">该资讯可能已下线，或链接无效。</p>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { fetchArticleDetail } from '@/api/platformContent.js'

const route = useRoute()
const item = ref(null)

onMounted(async () => {
  try {
    item.value = await fetchArticleDetail(route.params.id)
  } catch {
    item.value = null
  }
})
</script>
