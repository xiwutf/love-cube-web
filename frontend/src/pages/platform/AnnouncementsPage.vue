<template>
  <section class="platform-page module-page">
    <section class="platform-card module-hero">
      <p class="platform-kicker">Platform Bulletin</p>
      <h1 class="platform-title">平台动态公告</h1>
      <p class="platform-subtitle">
        聚合发布平台更新、规则说明、功能上新和活动通知，帮助用户快速了解平台动态。
      </p>
    </section>

    <div v-if="categories.length" class="content-filter">
      <button
        v-for="cat in ['全部', ...categories]"
        :key="cat"
        class="filter-btn"
        :class="{ active: (cat === '全部' ? '' : cat) === activeCategory }"
        type="button"
        @click="setCategory(cat === '全部' ? '' : cat)"
      >{{ cat }}</button>
    </div>

    <section class="module-grid">
      <router-link
        v-for="item in visibleList"
        :key="item.id"
        :to="`/announcements/${item.id}`"
        class="platform-card module-card module-card-link"
        :class="{ 'card-pinned': item.pinned }"
      >
        <div v-if="item.coverUrl" class="card-cover">
          <img :src="item.coverUrl" :alt="item.title" loading="lazy" />
        </div>
        <div class="card-badges">
          <span v-if="item.pinned" class="badge badge-pin">置顶</span>
          <span v-if="item.recommended" class="badge badge-rec">推荐</span>
          <span v-if="item.category" class="badge badge-cat">{{ item.category }}</span>
        </div>
        <p class="module-card-meta">{{ formatDate(item.publishDate || item.date) }}</p>
        <h3 class="module-card-title">{{ item.title || '未命名公告' }}</h3>
        <p class="module-card-desc">{{ item.summary || '暂无摘要信息' }}</p>
        <div class="card-footer">
          <span class="module-card-action">查看详情</span>
          <span v-if="item.viewCount" class="card-views">{{ item.viewCount }} 次阅读</span>
        </div>
      </router-link>

      <article v-if="!loading && !filteredList.length" class="platform-card module-empty">
        <h3 class="platform-heading">暂无公告</h3>
        <p class="platform-text">当前还没有已发布公告，稍后再来看看。</p>
      </article>
    </section>

    <div v-if="hasMore" class="load-more-wrap">
      <button type="button" class="load-more-btn" @click="page++">查看更多</button>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { fetchAnnouncements } from '@/api/platformContent.js'

const PAGE_SIZE = 9
const loading = ref(false)
const allItems = ref([])
const activeCategory = ref('')
const page = ref(1)

const categories = computed(() => {
  const cats = [...new Set(allItems.value.map(i => i.category).filter(Boolean))]
  return cats
})

const filteredList = computed(() => {
  if (!activeCategory.value) return allItems.value
  return allItems.value.filter(i => i.category === activeCategory.value)
})

const visibleList = computed(() => filteredList.value.slice(0, page.value * PAGE_SIZE))
const hasMore = computed(() => filteredList.value.length > page.value * PAGE_SIZE)

function setCategory(cat) {
  activeCategory.value = cat
  page.value = 1
}

watch(activeCategory, () => { page.value = 1 })

function formatDate(value) {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(0, 16)
}

onMounted(async () => {
  loading.value = true
  try {
    const data = await fetchAnnouncements({ status: 'published' })
    allItems.value = Array.isArray(data) ? data : []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.content-filter {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  padding: 0 4px 4px;
}

.filter-btn {
  border: 1px solid #e2e8f0;
  background: #fff;
  color: #475569;
  border-radius: 999px;
  padding: 6px 14px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.18s ease;
}

.filter-btn:hover {
  border-color: #ffd0db;
  color: #e84f73;
}

.filter-btn.active {
  background: linear-gradient(135deg, #ff5f84, #e84f73);
  color: #fff;
  border-color: transparent;
}

.card-pinned {
  border-left: 3px solid #e84f73 !important;
}

.card-cover {
  border-radius: 10px;
  overflow: hidden;
  margin-bottom: 10px;
  aspect-ratio: 16/7;
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-badges {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 6px;
}

.badge {
  font-size: 11px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 999px;
}

.badge-pin {
  background: #fff1f3;
  color: #e84f73;
  border: 1px solid #ffd4df;
}

.badge-rec {
  background: #fffbeb;
  color: #d97706;
  border: 1px solid #fde68a;
}

.badge-cat {
  background: #f0f9ff;
  color: #0284c7;
  border: 1px solid #bae6fd;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.card-views {
  font-size: 12px;
  color: #94a3b8;
}

.load-more-wrap {
  display: flex;
  justify-content: center;
  padding: 8px 0 16px;
}

.load-more-btn {
  border: 1px solid #e2e8f0;
  background: #fff;
  color: #475569;
  border-radius: 999px;
  padding: 10px 28px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.18s ease;
}

.load-more-btn:hover {
  border-color: #ffd0db;
  color: #e84f73;
}
</style>
