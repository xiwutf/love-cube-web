<template>
  <div class="search-page">
    <!-- Search bar -->
    <div class="search-header">
      <van-search
        v-model="keyword"
        placeholder="搜索用户昵称"
        show-action
        shape="round"
        background="transparent"
        @search="doSearch"
        @input="onInput"
        @cancel="onCancel"
      />
    </div>

    <!-- Empty / discovery state -->
    <div v-if="!keyword && !list.length && !loading" class="discover-state">
      <div class="discover-hint">
        <div class="discover-icon">🔍</div>
        <p class="discover-title">探索新朋友</p>
        <p class="discover-desc">输入昵称，找到你感兴趣的人</p>
      </div>
      <div class="hot-tags">
        <span class="hot-tag">五象新区</span>
        <span class="hot-tag">在校学生</span>
        <span class="hot-tag">职场新人</span>
        <span class="hot-tag">爱运动</span>
        <span class="hot-tag">爱旅行</span>
        <span class="hot-tag">摄影爱好</span>
      </div>
    </div>

    <!-- Results -->
    <div v-else class="results-wrap">
      <van-list v-model:loading="loading" :finished="noMore" finished-text="没有更多了" @load="loadMore">
        <div class="result-list">
          <UserCard
            v-for="u in list"
            :key="u.userId"
            :user="u"
            @click="router.push(`/fellowship/user-profile/${u.userId}`)"
          />
        </div>
        <van-empty
          v-if="!loading && searched && !list.length"
          description="未找到相关用户"
          image-size="70"
        />
      </van-list>
    </div>

    <AppTabBar />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import AppTabBar from '@/components/AppTabBar.vue'
import UserCard from '@/components/UserCard.vue'
import { normalizeUser } from '@/utils/normalizeUser.js'
import { debounce } from '@/utils/format.js'
import request from '@/api/request.js'

const router = useRouter()
const keyword = ref('')
const list = ref([])
const loading = ref(false)
const noMore = ref(false)
const searched = ref(false)
let page = 0

async function fetchSearch(reset = false) {
  if (reset) {
    page = 0
    list.value = []
    noMore.value = false
  }

  if (!keyword.value.trim()) {
    loading.value = false
    return
  }
  if (loading.value || noMore.value) return

  loading.value = true
  searched.value = true
  try {
    const data = await request.get('/search', {
      params: { keyword: keyword.value, page, size: 10 }
    })
    const items = (Array.isArray(data) ? data : []).map(normalizeUser)
    list.value.push(...items)
    if (items.length < 10) noMore.value = true
    else page++
  } finally {
    loading.value = false
  }
}

function doSearch() {
  fetchSearch(true)
}

function onCancel() {
  keyword.value = ''
  list.value = []
  searched.value = false
  noMore.value = false
  loading.value = false
  page = 0
}

const onInput = debounce(() => {
  if (keyword.value.trim()) fetchSearch(true)
  else loading.value = false
}, 400)

function loadMore() {
  if (!keyword.value.trim()) {
    loading.value = false
    return
  }
  fetchSearch()
}
</script>

<style scoped>
.search-page {
  min-height: 100vh;
  background: #f4f6fb;
  padding-bottom: 72px;
}

/* ── Search header ── */
.search-header {
  position: sticky;
  top: 0;
  z-index: 20;
  background: #fff;
  padding: 8px 4px 6px;
  box-shadow: 0 1px 0 #f0f2f8;
}

/* ── Discovery state ── */
.discover-state {
  padding: 40px 20px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

.discover-hint {
  text-align: center;
}
.discover-icon {
  font-size: 48px;
  margin-bottom: 12px;
  opacity: 0.6;
}
.discover-title {
  font-size: 18px;
  font-weight: 700;
  color: #1a2236;
  letter-spacing: -0.01em;
}
.discover-desc {
  font-size: 13px;
  color: #8898aa;
  margin-top: 6px;
}

.hot-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}
.hot-tag {
  padding: 6px 14px;
  background: #fff;
  border: 1px solid #f0d4dd;
  border-radius: 999px;
  font-size: 13px;
  color: #FF5F84;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s;
}
.hot-tag:active {
  background: #fff0f4;
}

/* ── Results ── */
.results-wrap {
  background: #fff;
  margin: 12px 0 0;
}

.result-list {
  display: flex;
  flex-direction: column;
}

:deep(.user-card) {
  border-bottom: 1px solid #f4f6fb;
  padding: 12px 16px;
}
:deep(.user-card:last-child) {
  border-bottom: none;
}
</style>
