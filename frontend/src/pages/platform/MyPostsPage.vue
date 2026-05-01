<template>
  <section class="sp-page">
    <header class="sp-head">
      <button type="button" class="sp-back" aria-label="返回" @click="$router.back()">‹</button>
      <h1 class="sp-title">我的动态</h1>
      <router-link class="sp-action-btn" to="/platform/positive-share">发布</router-link>
    </header>

    <div class="sp-body">
      <p v-if="loading" class="sp-status">加载中...</p>
      <p v-else-if="!items.length" class="sp-status sp-empty">
        还没有发布过动态，去分享你的想法吧 ✨
      </p>
      <template v-else>
        <article v-for="item in items" :key="item.id" class="sp-card post-card">
          <div class="post-head">
            <span class="post-cat">{{ item.category || '日常' }}</span>
            <span class="post-date">{{ formatDate(item.createdAt) }}</span>
          </div>
          <p class="post-content">{{ item.content }}</p>
          <div class="post-meta">
            <span>鼓励 {{ item.encourageCount || 0 }}</span>
            <span>评论 {{ item.commentCount || 0 }}</span>
          </div>
        </article>
      </template>

      <div v-if="hasMore && !loading" class="sp-more">
        <button type="button" class="sp-more-btn" :disabled="loadingMore" @click="loadMore">
          {{ loadingMore ? '加载中...' : '加载更多' }}
        </button>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { fetchMyPositiveShares } from '@/api/positiveShare.js'

const items = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const pageNum = ref(1)
const hasMore = ref(false)

function formatDate(raw) {
  if (!raw) return ''
  return String(raw).replace('T', ' ').slice(0, 16)
}

async function fetchList(append = false) {
  if (!append) loading.value = true
  else loadingMore.value = true
  try {
    const res = await fetchMyPositiveShares({ pageNum: pageNum.value, pageSize: 10 })
    const list = Array.isArray(res?.list) ? res.list : (Array.isArray(res) ? res : [])
    items.value = append ? items.value.concat(list) : list
    hasMore.value = Boolean(res?.hasMore)
  } catch {
    if (!append) items.value = []
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

async function loadMore() {
  pageNum.value++
  await fetchList(true)
}

onMounted(() => fetchList())
</script>

<style scoped>
.sp-page { min-height: 100vh; background: var(--lc-bg); color: var(--lc-text); }

.sp-head {
  position: sticky; top: 0; z-index: 10;
  display: flex; align-items: center;
  background: var(--lc-surface); border-bottom: 1px solid var(--lc-soft-alt);
}
.sp-back {
  width: 48px; height: 52px; flex: 0 0 auto;
  display: grid; place-items: center;
  border: 0; background: none; font-size: 22px; color: var(--lc-indigo); cursor: pointer;
}
.sp-title { flex: 1; margin: 0; font-size: 17px; font-weight: 800; }
.sp-action-btn {
  padding: 0 16px; height: 52px; line-height: 52px;
  font-size: 14px; color: var(--lc-indigo); font-weight: 700;
  text-decoration: none; white-space: nowrap;
}

.sp-body {
  padding: 16px 14px calc(80px + env(safe-area-inset-bottom));
  max-width: 680px; margin: 0 auto;
}

.sp-status { text-align: center; padding: 40px 0; color: var(--lc-subtle); font-size: 14px; }
.sp-empty::before { display: block; font-size: 32px; margin-bottom: 10px; content: "📝"; }

.sp-card {
  background: var(--lc-surface); border: 1px solid var(--lc-soft-alt); border-radius: 16px;
  box-shadow: 0 3px 12px rgba(15,23,42,0.04); margin-bottom: 12px; padding: 14px 16px;
}

.post-head {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 10px;
}
.post-cat {
  border-radius: 999px; padding: 2px 10px;
  background: var(--lc-indigo-soft); color: var(--lc-indigo); font-size: 11px; font-weight: 700;
}
.post-date { font-size: 11px; color: var(--lc-subtle); }
.post-content { margin: 0 0 10px; font-size: 14px; color: var(--lc-text-deep); line-height: 1.6; }
.post-meta { display: flex; gap: 14px; font-size: 12px; color: var(--lc-subtle); }

.sp-more { text-align: center; margin-top: 8px; }
.sp-more-btn {
  border: 1px solid var(--lc-soft-alt); border-radius: 999px;
  background: var(--lc-surface); color: var(--lc-muted-light);
  font-size: 13px; padding: 8px 24px; cursor: pointer;
}
.sp-more-btn:disabled { opacity: 0.5; }

@media (max-width: 767px) {
  :global(.platform-header), :global(.platform-footer), :global(.co-creation-toolbar) {
    display: none !important;
  }
}
@media (min-width: 768px) {
  .sp-body { padding-top: 24px; padding-bottom: 48px; }
}
</style>
