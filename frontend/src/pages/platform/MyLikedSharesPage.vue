<template>
  <section class="platform-page panel-page">
    <header class="panel-head">
      <div>
        <h1>我点赞的心声</h1>
        <p>这里展示你点击「为他加油」鼓励过的每日心声，与收藏分开保存。</p>
      </div>
      <div class="head-actions">
        <router-link to="/me/favorites" class="link-secondary">我的心声收藏</router-link>
        <router-link to="/me" class="back-btn">返回个人中心</router-link>
      </div>
    </header>

    <section class="panel-card">
      <p v-if="loading" class="status">加载中...</p>
      <template v-else>
        <p v-if="!items.length" class="status">你还没有点赞过心声，去每日心声逛逛吧。</p>
        <div v-else class="list">
          <article v-for="item in items" :key="item.id" class="item">
            <div class="item-head">
              <strong>{{ item.nickname || '平台用户' }}</strong>
              <span>{{ formatDate(item.createdAt) }}</span>
            </div>
            <p class="item-content">{{ item.content }}</p>
            <footer class="item-meta">
              <span>分类：{{ item.category || '其他' }}</span>
              <span>鼓励 {{ item.encourageCount || 0 }}</span>
              <span>评论 {{ item.commentCount || 0 }}</span>
            </footer>
          </article>
        </div>
      </template>

      <div v-if="hasMore && !loading" class="more-wrap">
        <button type="button" class="more-btn" :disabled="loadingMore" @click="loadMore">
          {{ loadingMore ? '加载中...' : '加载更多' }}
        </button>
      </div>
    </section>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { fetchMyLikedPositiveShares } from '@/api/positiveShare.js'

const items = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const pageNum = ref(1)
const hasMore = ref(false)

async function fetchList(append = false) {
  if (!append) loading.value = true
  try {
    const res = await fetchMyLikedPositiveShares({ pageNum: pageNum.value, pageSize: 10 })
    const list = Array.isArray(res?.list) ? res.list : []
    items.value = append ? items.value.concat(list) : list
    hasMore.value = Boolean(res?.hasMore)
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

async function loadMore() {
  if (loadingMore.value || !hasMore.value) return
  loadingMore.value = true
  pageNum.value += 1
  await fetchList(true)
}

function formatDate(value) {
  if (!value) return '--'
  return String(value).replace('T', ' ').slice(0, 16)
}

onMounted(() => {
  fetchList(false)
})
</script>

<style scoped>
.panel-page { display: grid; gap: 14px; }
.panel-head { display: flex; justify-content: space-between; align-items: flex-start; gap: 12px; flex-wrap: wrap; }
.panel-head h1 { margin: 0; font-size: 28px; }
.panel-head p { margin: 6px 0 0; color: var(--lc-muted); max-width: 520px; }
.head-actions { display: flex; align-items: center; gap: 14px; flex-wrap: wrap; }
.link-secondary { color: var(--lc-muted-light); text-decoration: none; font-weight: 600; font-size: 14px; }
.link-secondary:hover { color: var(--lc-blue); }
.back-btn { color: var(--lc-blue); text-decoration: none; font-weight: 700; }
.panel-card {
  border: 1px solid var(--lc-border);
  border-radius: 14px;
  background: var(--lc-surface);
  padding: 16px;
}
.status { margin: 0; color: var(--lc-muted); text-align: center; padding: 20px 0; }
.list { display: grid; gap: 12px; }
.item { border: 1px solid var(--lc-soft-alt); border-radius: 12px; padding: 12px; }
.item-head { display: flex; justify-content: space-between; gap: 8px; color: var(--lc-muted-light); font-size: 12px; }
.item-content { margin: 8px 0; color: var(--lc-text); line-height: 1.6; white-space: pre-wrap; }
.item-meta { display: flex; gap: 14px; color: var(--lc-muted-light); font-size: 12px; }
.more-wrap { margin-top: 14px; display: flex; justify-content: center; }
.more-btn {
  border: 1px solid var(--lc-border);
  border-radius: 999px;
  padding: 8px 16px;
  background: var(--lc-surface);
  color: var(--lc-slate);
  cursor: pointer;
}
</style>
