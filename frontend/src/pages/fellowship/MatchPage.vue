<template>
  <div class="match-page">
    <!-- Top bar -->
    <header class="match-header">
      <div class="match-header-left">
        <span class="match-logo-icon">❤</span>
        <span class="match-title">认识</span>
      </div>
      <div class="match-header-actions">
        <button class="header-btn" @click="goAllOppositeUsers">
          <van-icon name="friends-o" size="18" />
          <span>查看全部</span>
        </button>
        <button class="header-btn" @click="showFilter = true">
          <van-icon name="filter-o" size="18" />
          <span>筛选</span>
        </button>
      </div>
    </header>

    <!-- Card area -->
    <div class="card-area">
      <div v-if="pageLoading" class="page-loading">
        <van-loading type="spinner" color="#FF5F84" size="36" />
        <p class="loading-hint">正在为你匹配...</p>
      </div>

      <template v-else-if="cardStack.length">
        <div
          v-for="(user, i) in cardStack.slice(0, 2)"
          :key="user.userId"
          class="card-wrapper"
          :style="{ zIndex: cardStack.length - i, transform: i === 1 ? 'scale(0.95) translateY(12px)' : '' }"
        >
          <SwipeCard
            v-if="i === 0"
            :ref="el => topCardRef = el"
            :user="user"
            @view-profile="goUserProfile(user.userId)"
            @like="onAction('like')"
            @dislike="onAction('dislike')"
            @superlike="onAction('superlike')"
          />
          <div v-else class="card-back" />
        </div>
      </template>

      <div v-else-if="!pageLoading" class="no-more">
        <div class="no-more-icon">💤</div>
        <p class="no-more-title">今日推荐已看</p>
        <p class="no-more-desc">明天还有更多新朋友等</p>
        <van-button round color="#FF5F84" size="small" style="margin-top: 16px" @click="loadCards">
          刷新推荐
        </van-button>
      </div>
    </div>

    <!-- Action bar -->
    <div class="action-bar">
      <button class="action-btn btn-dislike" @click="topCardRef?.triggerDislike()">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><path d="M18 6L6 18M6 6l12 12"/></svg>
      </button>
      <button class="action-btn btn-superlike" @click="topCardRef?.triggerSuperlike()">
        <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2l2.09 6.26H21l-5.47 3.97 2.09 6.26L12 14.52l-5.62 3.97 2.09-6.26L3 8.26h6.91z"/></svg>
      </button>
      <button class="action-btn btn-like" @click="topCardRef?.triggerLike()">
        <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
      </button>
    </div>

    <!-- Filter popup -->
    <van-popup v-model:show="showFilter" position="bottom" round>
      <div class="filter-popup">
        <div class="filter-handle" />
        <h3 class="filter-title">筛选条件</h3>
        <div class="filter-block">
          <p class="filter-label">年龄范围</p>
          <p class="filter-value">{{ filter.ageRange[0] }} - {{ filter.ageRange[1] }} 岁</p>
          <van-slider v-model="filter.ageRange" range :min="18" :max="60" bar-height="4" active-color="#FF5F84" />
        </div>
        <div class="filter-block">
          <p class="filter-label">性别偏好</p>
          <p class="filter-hint">「男生 / 女生」指希望看到的对方性别；与交友异性策略不一致时会自动按异性推荐。</p>
          <van-radio-group v-model="filter.gender" direction="horizontal" class="filter-radio-group">
            <van-radio name="" icon-size="16">不限</van-radio>
            <van-radio name="male" icon-size="16">男生</van-radio>
            <van-radio name="female" icon-size="16">女生</van-radio>
          </van-radio-group>
        </div>
        <div class="filter-block">
          <p class="filter-label">地区关键词</p>
          <van-field
            v-model="filter.region"
            placeholder="例如：上海 / 杭州 / 深圳"
            clearable
            input-align="left"
          />
        </div>
        <div class="filter-block verify-only">
          <p class="filter-label">认证过滤</p>
          <van-switch v-model="filter.verifiedOnly" size="22px" active-color="#FF5F84" />
          <span class="verify-only-text">仅看已认证用户</span>
        </div>
        <van-button round block color="#FF5F84" style="margin-top: 8px" @click="applyFilter">
          应用筛选
        </van-button>
      </div>
    </van-popup>

    <!-- Match dialog -->
    <van-dialog
      v-model:show="showMatched"
      :show-cancel-button="true"
      confirm-button-text="去聊天"
      cancel-button-text="继续浏览"
      confirm-button-color="#FF5F84"
      @confirm="goChat"
      @cancel="showMatched = false"
    >
      <div class="matched-content">
        <div class="matched-emoji">🎉</div>
        <h3 class="matched-title">配对成功</h3>
        <p class="matched-desc">你们互相感兴趣了<br/>快去打个招呼</p>
      </div>
    </van-dialog>

    <AppTabBar />
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import AppTabBar from '@/components/AppTabBar.vue'
import SwipeCard from '@/components/SwipeCard.vue'
import { getMatchList, likeUser, dislikeUser, superlikeUser } from '@/api/match.js'
import { normalizeUser } from '@/utils/normalizeUser.js'

const router = useRouter()
const pageLoading = ref(true)
const cardStack = ref([])
const topCardRef = ref(null)
const showFilter = ref(false)
const showMatched = ref(false)
const pager = reactive({
  page: 1,
  size: 20,
  hasMore: true,
  loadingMore: false
})
let matchedUserId = null

const filter = reactive({
  ageRange: [18, 40],
  gender: '',
  region: '',
  verifiedOnly: false
})

async function loadCards() {
  pageLoading.value = true
  pager.page = 1
  pager.hasMore = true
  try {
    const result = await getMatchList({
      gender: filter.gender,
      ageRange: filter.ageRange,
      region: filter.region,
      verifiedOnly: filter.verifiedOnly,
      page: pager.page,
      size: pager.size
    })
    const list = Array.isArray(result?.list) ? result.list : []
    cardStack.value = list.map(normalizeUser)
    pager.hasMore = Boolean(result?.hasMore)
  } catch {
    showToast({ message: '加载失败', type: 'fail' })
  } finally {
    pageLoading.value = false
  }
}

async function onAction(action) {
  const top = cardStack.value[0]
  if (!top) return

  cardStack.value.shift()

  try {
    let res
    if (action === 'like') res = await likeUser(top.userId)
    if (action === 'dislike') res = await dislikeUser(top.userId)
    if (action === 'superlike') res = await superlikeUser(top.userId)

    if (res?.matched) {
      matchedUserId = top.userId
      showMatched.value = true
    }
  } catch {
    // ignore and continue browsing
  }

  if (cardStack.value.length <= 3) loadMore()
}

async function loadMore() {
  if (!pager.hasMore || pager.loadingMore) return
  pager.loadingMore = true
  try {
    const nextPage = pager.page + 1
    const result = await getMatchList({
      gender: filter.gender,
      ageRange: filter.ageRange,
      region: filter.region,
      verifiedOnly: filter.verifiedOnly,
      page: nextPage,
      size: pager.size
    })
    const items = (Array.isArray(result?.list) ? result.list : []).map(normalizeUser)
    const existing = new Set(cardStack.value.map(u => u.userId))
    cardStack.value.push(...items.filter(u => !existing.has(u.userId)))
    pager.page = nextPage
    pager.hasMore = Boolean(result?.hasMore)
  } catch {
    // ignore
  } finally {
    pager.loadingMore = false
  }
}

async function applyFilter() {
  showFilter.value = false
  await loadCards()
}

function goChat() {
  showMatched.value = false
  if (matchedUserId) router.push(`/fellowship/chat/${matchedUserId}`)
}

function goUserProfile(userId) {
  if (!userId) return
  router.push(`/fellowship/user-profile/${userId}`)
}

function goAllOppositeUsers() {
  router.push('/fellowship/match/all')
}

onMounted(loadCards)
</script>

<style scoped>
/* 鈹€鈹€ Page 鈹€鈹€ */
.match-page {
  min-height: 100vh;
  background: #f4f6fb;
  display: flex;
  flex-direction: column;
  padding-bottom: 72px;
}

/* 鈹€鈹€ Header 鈹€鈹€ */
.match-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  height: 52px;
  background: #fff;
  box-shadow: 0 1px 0 #f0f2f8;
  flex-shrink: 0;
}
.match-header-left {
  display: flex;
  align-items: center;
  gap: 6px;
}
.match-logo-icon {
  font-size: 20px;
  color: #FF5F84;
}
.match-title {
  font-size: 18px;
  font-weight: 800;
  color: #1a2236;
  letter-spacing: -0.01em;
}
.match-header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.header-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border-radius: 999px;
  border: 1px solid #eef1f8;
  background: #f8f9fd;
  color: #5b6b8a;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
}

/* 鈹€鈹€ Card area 鈹€鈹€ */
.card-area {
  flex: 1;
  position: relative;
  margin: 16px 14px 12px;
  min-height: 400px;
}

.page-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 400px;
  gap: 12px;
}
.loading-hint {
  font-size: 13px;
  color: #a0abbe;
}

.card-wrapper {
  position: absolute;
  inset: 0;
  transition: transform 0.22s ease;
}

.card-back {
  width: 100%;
  height: 100%;
  border-radius: 20px;
  background: #fff;
  box-shadow: 0 4px 20px rgba(15, 23, 42, 0.08);
}

/* 鈹€鈹€ No more 鈹€鈹€ */
.no-more {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 400px;
  gap: 8px;
}
.no-more-icon {
  font-size: 52px;
  margin-bottom: 4px;
}
.no-more-title {
  font-size: 17px;
  font-weight: 700;
  color: #1a2236;
}
.no-more-desc {
  font-size: 13px;
  color: #a0abbe;
}

/* 鈹€鈹€ Action bar 鈹€鈹€ */
.action-bar {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  padding: 4px 0 12px;
  flex-shrink: 0;
}

.action-btn {
  border: none;
  cursor: pointer;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}
.action-btn:active {
  transform: scale(0.88);
}

.btn-dislike {
  width: 60px;
  height: 60px;
  background: #fff;
  box-shadow: 0 6px 20px rgba(238, 10, 36, 0.14);
  color: #ee0a24;
}
.btn-dislike svg {
  width: 26px;
  height: 26px;
}

.btn-superlike {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #FF5F84, #FF3366);
  box-shadow: 0 6px 20px rgba(255, 95, 132, 0.4);
  color: #fff;
}
.btn-superlike svg {
  width: 20px;
  height: 20px;
}

.btn-like {
  width: 60px;
  height: 60px;
  background: #fff;
  box-shadow: 0 6px 20px rgba(16, 185, 129, 0.18);
  color: #10b981;
}
.btn-like svg {
  width: 26px;
  height: 26px;
}

/* 鈹€鈹€ Filter popup 鈹€鈹€ */
.filter-popup {
  padding: 12px 20px 32px;
}
.filter-handle {
  width: 36px;
  height: 4px;
  border-radius: 2px;
  background: #e2e8f0;
  margin: 0 auto 20px;
}
.filter-title {
  font-size: 17px;
  font-weight: 700;
  color: #1a2236;
  margin-bottom: 20px;
}
.filter-block {
  margin-bottom: 20px;
}
.filter-label {
  font-size: 13px;
  font-weight: 600;
  color: #5b6b8a;
  margin-bottom: 4px;
}
.filter-hint {
  font-size: 12px;
  color: #8898aa;
  line-height: 1.45;
  margin: 0 0 10px;
}
.filter-value {
  font-size: 15px;
  font-weight: 700;
  color: #FF5F84;
  margin-bottom: 10px;
}
.filter-radio-group {
  gap: 12px;
  flex-wrap: wrap;
}
.verify-only {
  display: flex;
  align-items: center;
  gap: 10px;
}
.verify-only-text {
  font-size: 13px;
  color: #5b6b8a;
}

/* 鈹€鈹€ Matched dialog 鈹€鈹€ */
.matched-content {
  padding: 24px 16px 16px;
  text-align: center;
}
.matched-emoji {
  font-size: 52px;
  margin-bottom: 10px;
}
.matched-title {
  font-size: 20px;
  font-weight: 800;
  color: #1a2236;
  margin-bottom: 8px;
}
.matched-desc {
  font-size: 14px;
  color: #8898aa;
  line-height: 1.6;
}
</style>

