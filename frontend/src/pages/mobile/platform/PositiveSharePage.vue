<template>
  <div class="ps-m">
    <header class="ps-m-head">
      <router-link to="/m/platform" class="ps-m-back">‹ 玩法</router-link>
      <h1>每日心声</h1>
      <p>记录温暖与成长，连续分享有 streak 奖励</p>
    </header>

    <section v-if="dailyTopic" class="ps-m-topic">
      <span class="ps-m-topic-label">今日话题</span>
      <p class="ps-m-topic-text">{{ dailyTopic.topicText }}</p>
      <p v-if="dailyTopic.hintText" class="ps-m-topic-hint">{{ dailyTopic.hintText }}</p>
    </section>

    <section class="ps-m-meta">
      <article class="ps-m-stat">
        <strong>{{ streakDays }}</strong>
        <span>连续天数</span>
      </article>
      <article class="ps-m-stat">
        <strong>{{ shareStreak.longestStreak || 0 }}</strong>
        <span>最长记录</span>
      </article>
      <article class="ps-m-stat" :class="{ done: shareStreak.checkedToday }">
        <strong>{{ shareStreak.checkedToday ? '✓' : '—' }}</strong>
        <span>今日已发</span>
      </article>
    </section>

    <section v-if="weeklyRank.length" class="ps-m-rank">
      <h2>本周心声榜</h2>
      <ol>
        <li v-for="row in weeklyRank" :key="row.userId || row.rank">
          <span class="rank">#{{ row.rank }}</span>
          <span class="name">{{ row.nickname || '用户' }}</span>
          <span class="score">{{ row.score ?? row.encourageCount ?? 0 }} 鼓励</span>
        </li>
      </ol>
    </section>

    <div ref="editorRef">
      <PositiveShareEditor @published="handlePublished" />
    </div>

    <nav class="ps-m-tabs" aria-label="列表筛选">
      <button
        v-for="tab in mainTabs"
        :key="tab.value"
        type="button"
        :class="['ps-m-tab', { active: activeTab === tab.value }]"
        @click="switchTab(tab.value)"
      >
        {{ tab.label }}
      </button>
    </nav>

    <div class="ps-m-cats">
      <button
        v-for="cat in filterCats"
        :key="cat"
        type="button"
        :class="['ps-m-cat', { active: activeCategory === cat }]"
        @click="toggleCategory(cat)"
      >
        {{ cat }}
      </button>
    </div>

    <div class="ps-m-list">
      <p v-if="loading" class="ps-m-loading">加载中…</p>
      <template v-else>
        <div v-for="item in visibleList" :key="item.id" class="ps-m-item">
          <PositiveShareCard
            :item="item"
            @like="handleLike"
            @bookmark="handleBookmark"
            @comment="openComment"
          />
          <button type="button" class="ps-m-comments-toggle" @click="toggleComments(item)">
            {{ expandedShareId === item.id ? '收起评论' : '查看评论' }}
          </button>
          <div v-if="expandedShareId === item.id" class="ps-m-comments">
            <p v-if="commentsLoading" class="ps-m-loading">加载评论…</p>
            <ul v-else>
              <li v-for="c in comments" :key="c.id">
                <strong>{{ c.username || '用户' }}</strong>
                <span>{{ c.content }}</span>
                <small>{{ formatDate(c.createdAt) }}</small>
              </li>
            </ul>
            <p v-if="!commentsLoading && !comments.length" class="ps-m-muted">暂无评论</p>
          </div>
        </div>
        <p v-if="!visibleList.length" class="ps-m-muted">暂无心声，写下第一条吧</p>
        <button
          v-if="hasMore"
          type="button"
          class="ps-m-more"
          :disabled="loadingMore"
          @click="loadMore"
        >
          {{ loadingMore ? '加载中…' : '加载更多' }}
        </button>
      </template>
    </div>

    <div v-if="commentTarget" class="ps-m-modal" @click.self="commentTarget = null">
      <section class="ps-m-modal-box">
        <h3>写一句鼓励</h3>
        <textarea
          v-model.trim="commentContent"
          maxlength="500"
          rows="4"
          placeholder="写下你的鼓励留言…"
        />
        <div class="ps-m-modal-actions">
          <button type="button" @click="commentTarget = null">取消</button>
          <button type="button" class="primary" :disabled="commentSubmitting" @click="submitComment">
            {{ commentSubmitting ? '提交中…' : '提交' }}
          </button>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import {
  bookmarkPositiveShare,
  commentPositiveShare,
  fetchPositiveShareComments,
  fetchMyPositiveShares,
  fetchPositiveShares,
  fetchPositiveShareDailyTopic,
  fetchMyPositiveShareStreak,
  fetchPositiveShareWeeklyRankings,
  likePositiveShare,
  unbookmarkPositiveShare,
  unlikePositiveShare
} from '@/api/positiveShare.js'
import PositiveShareEditor from '@/components/platform/positive/PositiveShareEditor.vue'
import PositiveShareCard from '@/components/platform/positive/PositiveShareCard.vue'

const mainTabs = [
  { label: '最新', value: 'latest' },
  { label: '今日', value: 'today' },
  { label: '热门', value: 'hot' },
  { label: '我的', value: 'my' }
]
const filterCats = ['感恩', '鼓励', '理想', '成长', '生活']

const activeTab = ref('latest')
const activeCategory = ref(null)
const list = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const pageNum = ref(1)
const hasMore = ref(false)
const commentTarget = ref(null)
const commentContent = ref('')
const commentSubmitting = ref(false)
const expandedShareId = ref(null)
const commentsLoading = ref(false)
const comments = ref([])
const editorRef = ref(null)
const dailyTopic = ref(null)
const shareStreak = ref({ currentStreak: 0, longestStreak: 0, checkedToday: false })
const weeklyRank = ref([])

const streakDays = computed(() => Number(shareStreak.value?.currentStreak ?? 0))

const visibleList = computed(() => {
  if (!activeCategory.value) return list.value
  return list.value.filter(item => item.category === activeCategory.value)
})

async function fetchList(append = false) {
  const currentPage = pageNum.value
  if (!append) loading.value = true
  try {
    const api = activeTab.value === 'my' ? fetchMyPositiveShares : fetchPositiveShares
    const res = await api({ tab: activeTab.value, pageNum: currentPage, pageSize: 10 })
    const nextList = Array.isArray(res?.list) ? res.list : []
    list.value = append ? list.value.concat(nextList) : nextList
    hasMore.value = Boolean(res?.hasMore)
  } catch {
    if (!append) list.value = []
    hasMore.value = false
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

function toggleCategory(cat) {
  activeCategory.value = activeCategory.value === cat ? null : cat
}

function switchTab(tab) {
  if (activeTab.value === tab) return
  activeTab.value = tab
  pageNum.value = 1
  fetchList(false)
}

async function loadMore() {
  if (!hasMore.value || loadingMore.value) return
  loadingMore.value = true
  pageNum.value += 1
  await fetchList(true)
}

function handlePublished(created) {
  if (!created) return
  if (activeTab.value === 'my') {
    list.value = [created, ...list.value]
    loadGameplayMeta()
    return
  }
  if ((activeTab.value === 'latest' || activeTab.value === 'today') && created.status === 'PUBLISHED') {
    list.value = [created, ...list.value]
  }
  loadGameplayMeta()
}

async function handleLike(item) {
  try {
    const res = item.liked ? await unlikePositiveShare(item.id) : await likePositiveShare(item.id)
    item.liked = Boolean(res?.liked)
    item.encourageCount = Number(res?.encourageCount ?? item.encourageCount ?? 0)
  } catch {
    /* ignore */
  }
}

async function handleBookmark(item) {
  try {
    const res = item.bookmarked ? await unbookmarkPositiveShare(item.id) : await bookmarkPositiveShare(item.id)
    item.bookmarked = Boolean(res?.bookmarked)
  } catch {
    /* ignore */
  }
}

function openComment(item) {
  commentTarget.value = item
  commentContent.value = ''
}

async function submitComment() {
  if (!commentTarget.value || !commentContent.value || commentSubmitting.value) return
  commentSubmitting.value = true
  try {
    const res = await commentPositiveShare(commentTarget.value.id, { content: commentContent.value })
    commentTarget.value.commentCount = Number(res?.commentCount ?? commentTarget.value.commentCount ?? 0)
    if (expandedShareId.value === commentTarget.value.id) {
      await loadComments(commentTarget.value.id)
    }
    commentTarget.value = null
    commentContent.value = ''
  } catch {
    /* ignore */
  } finally {
    commentSubmitting.value = false
  }
}

async function toggleComments(item) {
  if (expandedShareId.value === item.id) {
    expandedShareId.value = null
    comments.value = []
    return
  }
  expandedShareId.value = item.id
  await loadComments(item.id)
}

async function loadComments(shareId) {
  commentsLoading.value = true
  try {
    const res = await fetchPositiveShareComments(shareId, { pageNum: 1, pageSize: 30 })
    comments.value = Array.isArray(res?.list) ? res.list : []
  } catch {
    comments.value = []
  } finally {
    commentsLoading.value = false
  }
}

function formatDate(value) {
  if (!value) return '刚刚'
  return String(value).replace('T', ' ').slice(0, 16)
}

async function loadGameplayMeta() {
  try {
    const [topicRes, streakRes, rankRes] = await Promise.allSettled([
      fetchPositiveShareDailyTopic(),
      fetchMyPositiveShareStreak(),
      fetchPositiveShareWeeklyRankings({ limit: 5 })
    ])
    if (topicRes.status === 'fulfilled') dailyTopic.value = topicRes.value
    if (streakRes.status === 'fulfilled') shareStreak.value = streakRes.value || shareStreak.value
    if (rankRes.status === 'fulfilled') {
      weeklyRank.value = Array.isArray(rankRes.value?.items) ? rankRes.value.items : []
    }
  } catch {
    /* silent */
  }
}

onMounted(() => {
  fetchList(false)
  loadGameplayMeta()
})
</script>

<style scoped>
.ps-m {
  min-height: 100vh;
  padding: 16px 14px 32px;
  background: linear-gradient(180deg, #fff7fb 0%, #f4f5fb 120px, #f4f5fb 100%);
}

.ps-m-head h1 {
  margin: 8px 0 0;
  font-size: 22px;
  font-weight: 800;
  color: var(--lc-text, #1a2236);
}

.ps-m-head p {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--lc-subtle, #6b7280);
}

.ps-m-back {
  font-size: 14px;
  color: var(--lc-blue, #3b82f6);
  text-decoration: none;
}

.ps-m-topic {
  margin-top: 14px;
  padding: 12px 14px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid #fbcfe8;
}

.ps-m-topic-label {
  font-size: 11px;
  font-weight: 700;
  color: #db2777;
}

.ps-m-topic-text {
  margin: 6px 0 0;
  font-size: 14px;
  line-height: 1.5;
  color: var(--lc-text, #1a2236);
}

.ps-m-topic-hint {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--lc-subtle, #6b7280);
}

.ps-m-meta {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-top: 12px;
}

.ps-m-stat {
  padding: 10px 8px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid var(--lc-soft, #e8ecf4);
  text-align: center;
}

.ps-m-stat strong {
  display: block;
  font-size: 18px;
  color: var(--lc-text, #1a2236);
}

.ps-m-stat span {
  font-size: 10px;
  color: var(--lc-subtle, #8898aa);
}

.ps-m-stat.done strong {
  color: #16a34a;
}

.ps-m-rank {
  margin-top: 12px;
  padding: 12px 14px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid var(--lc-soft, #e8ecf4);
}

.ps-m-rank h2 {
  margin: 0 0 8px;
  font-size: 14px;
}

.ps-m-rank ol {
  margin: 0;
  padding: 0;
  list-style: none;
}

.ps-m-rank li {
  display: flex;
  gap: 8px;
  align-items: center;
  padding: 6px 0;
  font-size: 13px;
  border-bottom: 1px solid #f1f5f9;
}

.ps-m-rank li:last-child {
  border-bottom: none;
}

.ps-m-rank .rank {
  font-weight: 700;
  color: #7c3aed;
  min-width: 28px;
}

.ps-m-rank .name {
  flex: 1;
}

.ps-m-rank .score {
  font-size: 11px;
  color: var(--lc-subtle, #6b7280);
}

.ps-m-tabs {
  display: flex;
  gap: 6px;
  margin-top: 16px;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

.ps-m-tab {
  flex-shrink: 0;
  padding: 8px 14px;
  border: 1px solid var(--lc-soft, #e8ecf4);
  border-radius: 999px;
  background: #fff;
  font-size: 13px;
  color: var(--lc-subtle, #6b7280);
}

.ps-m-tab.active {
  background: #fdf2f8;
  border-color: #f9a8d4;
  color: #be185d;
  font-weight: 600;
}

.ps-m-cats {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 10px;
}

.ps-m-cat {
  padding: 6px 10px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  background: #fff;
  font-size: 12px;
}

.ps-m-cat.active {
  background: #eff6ff;
  border-color: #93c5fd;
  color: #1d4ed8;
}

.ps-m-list {
  margin-top: 12px;
}

.ps-m-item {
  margin-bottom: 12px;
}

.ps-m-comments-toggle {
  width: 100%;
  margin-top: 4px;
  padding: 8px;
  border: none;
  background: transparent;
  font-size: 12px;
  color: var(--lc-blue, #3b82f6);
}

.ps-m-comments {
  padding: 8px 12px;
  background: #f8fafc;
  border-radius: 10px;
  font-size: 12px;
}

.ps-m-comments li {
  margin-bottom: 8px;
}

.ps-m-comments strong {
  margin-right: 6px;
}

.ps-m-comments small {
  display: block;
  color: var(--lc-subtle, #94a3b8);
  margin-top: 2px;
}

.ps-m-loading,
.ps-m-muted {
  text-align: center;
  font-size: 13px;
  color: var(--lc-subtle, #6b7280);
  padding: 16px 0;
}

.ps-m-more {
  width: 100%;
  margin-top: 8px;
  padding: 12px;
  border-radius: 12px;
  border: 1px dashed var(--lc-soft, #cbd5e1);
  background: #fff;
  font-size: 14px;
  color: var(--lc-text, #334155);
}

.ps-m-modal {
  position: fixed;
  inset: 0;
  z-index: 200;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: flex-end;
  padding: 16px;
}

.ps-m-modal-box {
  width: 100%;
  padding: 16px;
  border-radius: 16px 16px 0 0;
  background: #fff;
}

.ps-m-modal-box h3 {
  margin: 0 0 10px;
  font-size: 16px;
}

.ps-m-modal-box textarea {
  width: 100%;
  box-sizing: border-box;
  border: 1px solid var(--lc-soft, #e2e8f0);
  border-radius: 10px;
  padding: 10px;
  font-size: 14px;
  resize: vertical;
}

.ps-m-modal-actions {
  display: flex;
  gap: 10px;
  margin-top: 12px;
}

.ps-m-modal-actions button {
  flex: 1;
  padding: 10px;
  border-radius: 10px;
  border: 1px solid var(--lc-soft, #e2e8f0);
  background: #fff;
}

.ps-m-modal-actions .primary {
  background: var(--lc-pink, #ec4899);
  border-color: transparent;
  color: #fff;
}
</style>
