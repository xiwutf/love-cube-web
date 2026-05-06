<template>
  <section class="admin-page">
    <section class="platform-card">
      <h1 class="platform-title">联谊动态与点赞</h1>
      <p class="platform-subtitle">
        对应总览「互动与匹配」中的联谊动态数据。可查看每条动态正文及点赞用户；评论数仅为字段累计，本站联谊动态流未提供评论发表能力，故无评论正文列表。
      </p>
    </section>

    <div v-if="loading" class="admin-loading">加载中...</div>
    <div v-else-if="error" class="admin-error">{{ error }}</div>

    <section v-else class="admin-list">
      <article v-for="item in items" :key="item.id" class="admin-row">
        <div class="admin-row-head">
          <strong>{{ rowValue(item, ['authorName', 'username'], '-') }}</strong>
          <span class="admin-tag">ID {{ rowValue(item, ['fellowshipDynamicId', 'id'], '-') }}</span>
        </div>
        <p class="admin-row-meta">{{ formatDate(rowValue(item, ['publishedAt', 'createdAt'], '')) }} · 赞 {{ rowValue(item, ['likeCount'], 0) }} · 评论数(字段) {{ rowValue(item, ['commentCount'], 0) }}</p>
        <p class="admin-row-content">{{ rowValue(item, ['content'], '') }}</p>
        <div v-if="dynamicImages(item).length" class="thumb-row">
          <a
            v-for="(img, idx) in dynamicImages(item).slice(0, 6)"
            :key="`${rowValue(item, ['fellowshipDynamicId', 'id'], 'x')}-${idx}`"
            :href="fullImageUrl(img)"
            target="_blank"
            rel="noopener"
            class="thumb-link"
          >
            <img :src="fullImageUrl(img)" alt="" class="thumb">
          </a>
        </div>
        <div class="admin-cell-actions">
          <button class="admin-btn" type="button" @click="toggleLikes(item)">
            {{ expandedId === item.id ? '收起点赞' : '查看点赞用户' }}
          </button>
        </div>
        <div v-if="expandedId === item.id" class="like-panel">
          <p v-if="likeLoading" class="admin-row-meta">点赞列表加载中...</p>
          <p v-else-if="!likeRows.length" class="admin-row-meta">暂无点赞记录</p>
          <ul v-else class="like-list">
            <li v-for="row in likeRows" :key="row.id" class="like-line">
              <span class="like-user">{{ rowValue(row, ['likedUsername', 'username'], '-') }}</span>
              <span class="like-meta">用户 {{ rowValue(row, ['likedUserId', 'userId'], '-') }} · {{ formatDate(rowValue(row, ['likedAt', 'createdAt'], '')) }}</span>
            </li>
          </ul>
        </div>
      </article>
    </section>

    <div v-if="!loading && !error && items.length" class="pager">
      <button class="admin-btn" type="button" :disabled="pageNum <= 1" @click="prevPage">上一页</button>
      <span class="pager-meta">第 {{ pageNum }} 页 · 共 {{ total }} 条</span>
      <button class="admin-btn" type="button" :disabled="!hasNext" @click="nextPage">下一页</button>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { showToast } from 'vant'
import { getAdminFellowshipDynamicLikes, getAdminFellowshipDynamics } from '@/api/adminFellowshipDynamics.js'
import { toFullUrl } from '@/utils/image.js'

const items = ref([])
const loading = ref(true)
const error = ref('')
const pageNum = ref(1)
const pageSize = 20
const total = ref(0)
const hasNext = ref(false)

const expandedId = ref(null)
const likeLoading = ref(false)
const likeRows = ref([])

function rowValue(row, keys, fallback = '') {
  for (const key of keys) {
    const value = row?.[key]
    if (value != null) return value
  }
  return fallback
}

function dynamicImages(row) {
  const images = row?.images
  return Array.isArray(images) ? images : []
}

function formatDate(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}

function fullImageUrl(path) {
  return toFullUrl(path)
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const res = await getAdminFellowshipDynamics(pageNum.value, pageSize)
    const rows = Array.isArray(res?.rows) ? res.rows : (Array.isArray(res?.list) ? res.list : [])
    items.value = rows
    total.value = Number(res?.total ?? items.value.length)
    hasNext.value = Boolean(res?.hasNext)
  } catch (e) {
    error.value = e.message || '加载失败'
    items.value = []
  } finally {
    loading.value = false
  }
}

async function toggleLikes(item) {
  const id = item?.id
  if (id == null) return
  if (expandedId.value === id) {
    expandedId.value = null
    likeRows.value = []
    return
  }
  expandedId.value = id
  likeLoading.value = true
  likeRows.value = []
  try {
    const res = await getAdminFellowshipDynamicLikes(id, 1, 100)
    likeRows.value = Array.isArray(res?.rows) ? res.rows : (Array.isArray(res?.list) ? res.list : [])
  } catch (e) {
    likeRows.value = []
    showToast({ type: 'fail', message: e.message || '点赞列表加载失败' })
  } finally {
    likeLoading.value = false
  }
}

function prevPage() {
  if (pageNum.value <= 1) return
  pageNum.value -= 1
  expandedId.value = null
  likeRows.value = []
  load()
}

function nextPage() {
  if (!hasNext.value) return
  pageNum.value += 1
  expandedId.value = null
  likeRows.value = []
  load()
}

onMounted(load)
</script>

<style scoped>
.thumb-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.thumb-link {
  display: block;
  width: 72px;
  height: 72px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid var(--lc-border);
}

.thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.like-panel {
  margin-top: 10px;
  border-top: 1px dashed var(--lc-border);
  padding-top: 10px;
}

.like-list {
  margin: 0;
  padding: 0;
  list-style: none;
}

.like-line {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 6px 0;
  border-bottom: 1px solid var(--lc-border);
}

.like-line:last-child {
  border-bottom: none;
}

.like-user {
  font-weight: 600;
  color: var(--lc-text);
}

.like-meta {
  font-size: 12px;
  color: var(--lc-muted);
}

.pager {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin: 20px 0 40px;
}

.pager-meta {
  font-size: 13px;
  color: var(--lc-muted);
}

.admin-row-content {
  margin: 8px 0 0;
  white-space: pre-wrap;
  line-height: 1.7;
  color: var(--lc-slate);
}
</style>
