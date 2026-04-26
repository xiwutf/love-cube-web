<template>
  <section class="platform-page module-page">
    <section class="platform-card module-hero">
      <p class="platform-kicker">Featured Content</p>
      <h1 class="platform-title">精选内容资讯</h1>
      <p class="platform-subtitle">
        围绕关系成长、沟通技巧、活动指南等内容持续更新，打造平台知识内容中心。
      </p>
    </section>

    <section class="module-grid">
      <router-link
        v-for="item in list"
        :key="item.id"
        :to="`/articles/${item.id}`"
        class="platform-card module-card module-card-link"
      >
        <p class="module-card-meta">{{ item.tag || '精选内容' }}</p>
        <h3 class="module-card-title">{{ item.title || '未命名文章' }}</h3>
        <p class="module-card-desc">{{ item.summary || '暂无摘要信息' }}</p>
        <span class="module-card-action">阅读全文</span>
      </router-link>

      <article v-if="!loading && !list.length" class="platform-card module-empty">
        <h3 class="platform-heading">暂无内容</h3>
        <p class="platform-text">当前还没有已发布文章，稍后再来看看。</p>
      </article>
    </section>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { fetchArticles } from '@/api/platformContent.js'

const loading = ref(false)
const list = ref([])

onMounted(async () => {
  loading.value = true
  try {
    const data = await fetchArticles({ status: 'published' })
    list.value = Array.isArray(data) ? data : []
  } finally {
    loading.value = false
  }
})
</script>
