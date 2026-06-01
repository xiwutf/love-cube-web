<template>
  <div class="signups-page">
    <NavBar title="我的报名" />

    <div v-if="loading" class="loading-wrap">
      <van-loading size="20" />
    </div>

    <section v-else class="content">
      <article v-for="item in list" :key="item.eventId" class="event-item">
        <div class="event-main" @click="goDetail(item.eventId)">
          <p class="title">{{ item.title || '活动' }}</p>
          <p class="meta">{{ formatEventTime(item.eventTime) }} · {{ item.location || '线上/待定' }}</p>
          <p class="signup-time">报名时间：{{ formatTime(item.signupAt) }}</p>
          <p v-if="item.checkedIn" class="checked-tag">已签到</p>
        </div>
        <div class="event-actions">
          <van-tag type="primary" plain>已报名</van-tag>
          <van-button
            v-if="item.checkinEnabled && !item.checkedIn"
            size="small"
            type="primary"
            plain
            @click="openCheckin(item)"
          >
            签到
          </van-button>
          <van-button
            v-if="item.canReview"
            size="small"
            type="warning"
            plain
            @click="openReview(item)"
          >
            互评
          </van-button>
        </div>
      </article>

      <van-empty v-if="!list.length" description="你还没有报名活动" />
    </section>

    <van-dialog v-model:show="showCheckin" title="活动签到" show-cancel-button @confirm="submitCheckin">
      <van-field v-model="checkinCode" label="签到码" placeholder="输入现场 6 位签到码" maxlength="6" />
    </van-dialog>

    <van-popup v-model:show="showReview" position="bottom" round :style="{ maxHeight: '80vh' }">
      <div class="review-popup">
        <h3>活动互评</h3>
        <p class="review-sub">为同行伙伴打个分吧（1-5 星）</p>
        <div v-if="reviewLoading" class="review-loading">加载中…</div>
        <div v-for="user in reviewCandidates" :key="user.userId" class="review-row">
          <span>{{ user.nickname || '伙伴' }}</span>
          <van-rate v-model="reviewDraft[user.userId].rating" :count="5" size="18" color="#ff5f84" />
          <van-tag v-if="user.reviewed" type="success" plain>已评</van-tag>
          <van-button v-else size="mini" type="primary" @click="submitReview(user.userId)">提交</van-button>
        </div>
        <van-empty v-if="!reviewLoading && !reviewCandidates.length" description="暂无可评价对象" image-size="60" />
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import NavBar from '@/components/NavBar.vue'
import {
  getMyEventSignups,
  checkinEvent,
  fetchEventReviewCandidates,
  submitEventReview
} from '@/api/platformContent.js'

const router = useRouter()
const loading = ref(false)
const list = ref([])
const showCheckin = ref(false)
const showReview = ref(false)
const checkinCode = ref('')
const activeEventId = ref('')
const reviewLoading = ref(false)
const reviewCandidates = ref([])
const reviewDraft = reactive({})

async function loadList() {
  loading.value = true
  try {
    const data = await getMyEventSignups()
    list.value = Array.isArray(data) ? data : []
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '加载失败' })
  } finally {
    loading.value = false
  }
}

function goDetail(eventId) {
  if (!eventId) return
  router.push(`/events/${eventId}`)
}

function openCheckin(item) {
  activeEventId.value = item.eventId
  checkinCode.value = ''
  showCheckin.value = true
}

async function submitCheckin() {
  if (!activeEventId.value || !checkinCode.value.trim()) {
    showToast('请输入签到码')
    return
  }
  try {
    await checkinEvent(activeEventId.value, checkinCode.value.trim())
    showToast({ type: 'success', message: '签到成功' })
    showCheckin.value = false
    await loadList()
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '签到失败' })
  }
}

async function openReview(item) {
  activeEventId.value = item.eventId
  showReview.value = true
  reviewLoading.value = true
  try {
    const rows = await fetchEventReviewCandidates(item.eventId)
    reviewCandidates.value = Array.isArray(rows) ? rows : []
    reviewCandidates.value.forEach((user) => {
      if (!reviewDraft[user.userId]) {
        reviewDraft[user.userId] = { rating: 5, comment: '' }
      }
    })
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '加载失败' })
    showReview.value = false
  } finally {
    reviewLoading.value = false
  }
}

async function submitReview(targetUserId) {
  const draft = reviewDraft[targetUserId]
  if (!draft?.rating) {
    showToast('请选择评分')
    return
  }
  try {
    await submitEventReview(activeEventId.value, {
      targetUserId,
      rating: draft.rating,
      comment: draft.comment || ''
    })
    showToast({ type: 'success', message: '评价已提交' })
    const rows = await fetchEventReviewCandidates(activeEventId.value)
    reviewCandidates.value = Array.isArray(rows) ? rows : []
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '提交失败' })
  }
}

function formatEventTime(value) {
  return formatTime(value)
}

function formatTime(value) {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN', { hour12: false })
}

onMounted(loadList)
</script>

<style scoped>
.signups-page {
  min-height: 100vh;
  background: #f8f9fb;
}

.loading-wrap {
  display: flex;
  justify-content: center;
  padding: 36px 0;
}

.content {
  padding: 12px;
}

.event-item {
  background: #fff;
  border-radius: 12px;
  padding: 12px;
  margin-bottom: 10px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.event-main {
  flex: 1;
}

.event-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.title {
  margin: 0;
  font-size: 15px;
  color: #111827;
  font-weight: 600;
}

.meta {
  margin: 6px 0 0;
  font-size: 12px;
  color: #6b7280;
}

.signup-time {
  margin: 6px 0 0;
  font-size: 12px;
  color: #9ca3af;
}

.checked-tag {
  margin: 6px 0 0;
  font-size: 12px;
  color: #059669;
  font-weight: 700;
}

.review-popup {
  padding: 16px;
}

.review-popup h3 {
  margin: 0;
}

.review-sub {
  margin: 6px 0 14px;
  font-size: 12px;
  color: #6b7280;
}

.review-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 0;
  border-bottom: 1px solid #f3f4f6;
  font-size: 13px;
}

.review-row span:first-child {
  flex: 1;
}

.review-loading {
  text-align: center;
  padding: 20px 0;
  color: #9ca3af;
}
</style>
