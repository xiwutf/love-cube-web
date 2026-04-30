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
      <figure v-if="item.coverUrl" class="detail-cover">
        <img :src="item.coverUrl" :alt="item.title || '公告封面'" loading="lazy">
      </figure>
      <div class="detail-body">
        <p>{{ item.content || '暂无正文内容。' }}</p>
      </div>
      <section v-if="item.attachmentUrl" class="detail-attachment">
        <h3>公告附件</h3>
        <img :src="item.attachmentUrl" :alt="`${item.title || '公告'}附件`" loading="lazy">
      </section>
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

<style scoped>
.detail-cover {
  margin: 16px 0;
  overflow: hidden;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
}

.detail-cover img {
  display: block;
  width: 100%;
  max-height: 420px;
  object-fit: cover;
}

.detail-attachment {
  margin-top: 18px;
  padding-top: 14px;
  border-top: 1px dashed #cbd5e1;
}

.detail-attachment h3 {
  margin: 0 0 10px;
  color: #334155;
  font-size: 16px;
}

.detail-attachment img {
  display: block;
  width: 100%;
  max-height: 560px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: #ffffff;
  object-fit: contain;
}
</style>

