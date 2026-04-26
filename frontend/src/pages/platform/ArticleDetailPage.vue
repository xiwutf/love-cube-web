<template>
  <section class="platform-page module-page">
    <div class="detail-nav-row">
      <router-link to="/articles" class="platform-backlink">返回资讯列表</router-link>
      <router-link to="/events" class="platform-link">查看活动中心</router-link>
    </div>

    <article v-if="item" class="platform-card detail-article">
      <p class="module-card-meta">内容分类：{{ item.tag || '精选内容' }}</p>
      <h1 class="detail-title">{{ item.title || '未命名文章' }}</h1>
      <p class="detail-lead">{{ item.summary || '暂无摘要信息' }}</p>
      <div class="detail-body">
        <p>{{ item.content || '暂无正文内容。' }}</p>
      </div>
    </article>

    <article v-else class="platform-card module-empty">
      <h3 class="platform-heading">内容不存在</h3>
      <p class="platform-text">该内容可能已下线，请返回资讯页查看最新列表。</p>
    </article>
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
