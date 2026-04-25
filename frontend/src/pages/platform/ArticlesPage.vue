<template>
  <section class="platform-page">
    <section class="platform-card">
      <p class="platform-kicker">Articles</p>
      <h1 class="platform-title">平台资讯</h1>
      <p class="platform-subtitle">功能更新、使用指南和社区建议在这里集中呈现。</p>
    </section>

    <div class="platform-list">
      <router-link
        v-for="item in list"
        :key="item.id"
        :to="`/articles/${item.id}`"
        class="platform-card platform-item-link"
      >
        <p class="platform-meta">{{ item.tag }}</p>
        <h3 class="platform-heading">{{ item.title }}</h3>
        <p class="platform-text">{{ item.summary }}</p>
      </router-link>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { fetchArticles } from '@/api/platformContent.js'

const list = ref([])

onMounted(async () => {
  list.value = await fetchArticles({ status: 'published' })
})
</script>
