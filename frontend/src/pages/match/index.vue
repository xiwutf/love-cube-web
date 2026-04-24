<template>
  <div class="match-page">
    <div class="top-bar">
      <span class="top-title">💝 匹配</span>
      <van-icon name="filter-o" size="22" color="#666" @click="showFilter = true" />
    </div>

    <!-- 卡片区 -->
    <div class="card-area">
      <div v-if="pageLoading" class="page-loading">
        <van-loading type="spinner" color="#FF6B8A" size="40" />
      </div>

      <template v-else-if="cardStack.length">
        <!-- 只渲染最顶部 2 张（下层卡片做缩放预览） -->
        <div
          v-for="(user, i) in cardStack.slice(0, 2)"
          :key="user.userId"
          class="card-wrapper"
          :style="{ zIndex: cardStack.length - i, transform: i === 1 ? 'scale(0.95) translateY(10px)' : '' }"
        >
          <SwipeCard
            v-if="i === 0"
            :ref="el => topCardRef = el"
            :user="user"
            @like="onAction('like')"
            @dislike="onAction('dislike')"
            @superlike="onAction('superlike')"
          />
          <div v-else class="card-back" />
        </div>
      </template>

      <div v-else-if="!pageLoading" class="no-more">
        <p class="no-more-text">今天的推荐已看完 😊</p>
        <van-button round type="primary" size="small" @click="loadCards">刷新一下</van-button>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="action-bar">
      <div class="action-btn dislike" @click="topCardRef?.triggerDislike()">
        <van-icon name="close" size="28" color="#ee0a24" />
      </div>
      <div class="action-btn superlike" @click="topCardRef?.triggerSuperlike()">
        <van-icon name="star-o" size="24" color="#FF6B8A" />
      </div>
      <div class="action-btn like" @click="topCardRef?.triggerLike()">
        <van-icon name="like-o" size="28" color="#4CAF50" />
      </div>
    </div>

    <!-- 筛选 Popup -->
    <van-popup v-model:show="showFilter" position="bottom" round style="padding: 20px 16px 32px">
      <h3 class="filter-title">筛选条件</h3>
      <p class="filter-label">年龄范围：{{ filter.ageRange[0] }} - {{ filter.ageRange[1] }} 岁</p>
      <van-slider v-model="filter.ageRange" range :min="18" :max="60" bar-height="4" active-color="#FF6B8A" />
      <p class="filter-label" style="margin-top:20px">性别</p>
      <van-radio-group v-model="filter.gender" direction="horizontal">
        <van-radio name="">不限</van-radio>
        <van-radio name="男">男</van-radio>
        <van-radio name="女">女</van-radio>
      </van-radio-group>
      <van-button round block type="primary" style="margin-top:24px" @click="applyFilter">应用筛选</van-button>
    </van-popup>

    <!-- 匹配成功弹窗 -->
    <van-dialog v-model:show="showMatched" title="🎉 匹配成功！" :show-cancel-button="true"
                confirm-button-text="去聊天" cancel-button-text="继续滑动"
                @confirm="goChat" @cancel="showMatched = false">
      <div class="matched-content">
        <p>你们互相喜欢了对方！</p>
      </div>
    </van-dialog>

    <AppTabBar />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import AppTabBar from '@/components/AppTabBar.vue'
import SwipeCard from '@/components/SwipeCard.vue'
import { getMatchList, likeUser, dislikeUser, superlikeUser } from '@/api/match.js'
import { normalizeUser } from '@/utils/normalizeUser.js'

const router      = useRouter()
const pageLoading = ref(true)
const cardStack   = ref([])
const topCardRef  = ref(null)
const showFilter  = ref(false)
const showMatched = ref(false)
let   matchedUserId = null

const filter = reactive({ ageRange: [18, 40], gender: '' })

async function loadCards() {
  pageLoading.value = true
  try {
    const data    = await getMatchList()
    cardStack.value = (Array.isArray(data) ? data : []).map(normalizeUser)
  } catch {
    showToast({ message: '加载失败', type: 'fail' })
  } finally {
    pageLoading.value = false
  }
}

async function onAction(action) {
  const top = cardStack.value[0]
  if (!top) return

  cardStack.value.shift()   // 移除顶部卡片

  try {
    let res
    if (action === 'like')      res = await likeUser(top.userId)
    if (action === 'dislike')   res = await dislikeUser(top.userId)
    if (action === 'superlike') res = await superlikeUser(top.userId)

    // 后端返回 matched:true 表示双向喜欢
    if (res?.matched) {
      matchedUserId = top.userId
      showMatched.value = true
    }
  } catch { /* 静默失败 */ }

  // 卡片快用完时预加载
  if (cardStack.value.length <= 2) loadMore()
}

async function loadMore() {
  try {
    const data  = await getMatchList()
    const items = (Array.isArray(data) ? data : []).map(normalizeUser)
    // 去重
    const existing = new Set(cardStack.value.map(u => u.userId))
    cardStack.value.push(...items.filter(u => !existing.has(u.userId)))
  } catch {}
}

function applyFilter() {
  showFilter.value = false
  loadCards()
}

function goChat() {
  showMatched.value = false
  if (matchedUserId) router.push(`/chat/${matchedUserId}`)
}

onMounted(loadCards)
</script>

<style scoped>
.match-page { min-height: 100vh; background: #f8f8f8; display: flex; flex-direction: column; padding-bottom: 60px; }

.top-bar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 14px 16px; background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
}
.top-title { font-size: 18px; font-weight: 700; color: #FF6B8A; }

.card-area {
  flex: 1; position: relative; margin: 16px;
  min-height: 420px;
}

.page-loading { display: flex; justify-content: center; align-items: center; height: 420px; }

.card-wrapper {
  position: absolute; inset: 0;
  transition: transform 0.2s ease;
}

.card-back {
  width: 100%; height: 100%; border-radius: 16px;
  background: #fff; box-shadow: 0 4px 16px rgba(0,0,0,.08);
}

.no-more {
  display: flex; flex-direction: column; align-items: center;
  justify-content: center; height: 420px; gap: 16px;
}
.no-more-text { font-size: 16px; color: #999; }

.action-bar {
  display: flex; justify-content: center; align-items: center;
  gap: 24px; padding: 8px 0 16px;
}
.action-btn {
  width: 56px; height: 56px; border-radius: 50%; background: #fff;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 4px 12px rgba(0,0,0,.12); cursor: pointer;
  transition: transform .15s;
}
.action-btn:active { transform: scale(.9); }
.action-btn.superlike { width: 44px; height: 44px; }

.filter-title { font-size: 17px; font-weight: 600; margin-bottom: 20px; }
.filter-label { font-size: 14px; color: #666; margin-bottom: 12px; }

.matched-content { text-align: center; padding: 16px; font-size: 15px; color: #666; }
</style>
