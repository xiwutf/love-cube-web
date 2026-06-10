<template>
  <div class="signups-page">
    <NavBar title="我的报名" />

    <div v-if="loading" class="loading-wrap">
      <van-loading size="20" />
    </div>

    <section v-else class="content">
      <div v-if="pendingReviewTotal > 0" class="review-banner">
        <div>
          <p class="review-banner-title">你有 {{ pendingReviewTotal }} 位活动伙伴待互评</p>
          <p class="review-banner-sub">完成互评有助于后续活动匹配与信任建立</p>
        </div>
        <van-button size="small" type="warning" round @click="scrollToPendingReview">去互评</van-button>
      </div>

      <van-tabs v-model:active="activeTab" color="#ff5f84" shrink sticky offset-top="46">
        <van-tab title="全部" name="all" />
        <van-tab title="待参加" name="upcoming" :badge="tabCounts.upcoming || ''" />
        <van-tab title="待互评" name="review_pending" :badge="tabCounts.review_pending || ''" />
        <van-tab title="已完成" name="completed" />
      </van-tabs>

      <article
        v-for="item in filteredList"
        :key="item.eventId"
        class="event-item"
        :data-status="item.status"
      >
        <div class="event-main" @click="goDetail(item.eventId)">
          <div class="title-row">
            <p class="title">{{ item.title || '活动' }}</p>
            <van-tag :type="statusTag(item.status).type" plain>{{ statusTag(item.status).label }}</van-tag>
          </div>
          <p class="meta">{{ formatEventTime(item.eventTime) }} · {{ item.location || '线上/待定' }}</p>
          <p class="signup-time">报名时间：{{ formatTime(item.signupAt) }}</p>
          <p v-if="item.checkedInAt" class="checked-tag">签到于 {{ formatTime(item.checkedInAt) }}</p>
          <p v-if="item.canReview && item.pendingReviewCount > 0" class="pending-tag">
            还有 {{ item.pendingReviewCount }} 人待互评
          </p>
        </div>
        <div class="event-actions">
          <van-button
            v-if="item.checkinEnabled && !item.checkedIn && !item.eventEnded"
            size="small"
            type="primary"
            plain
            @click="openCheckin(item)"
          >
            签到
          </van-button>
          <van-button
            v-if="item.canReview && item.pendingReviewCount > 0"
            size="small"
            type="warning"
            @click="openReview(item)"
          >
            互评
          </van-button>
          <van-button
            v-else-if="item.canReview && item.reviewCompleted"
            size="small"
            type="success"
            plain
            disabled
          >
            已互评
          </van-button>
        </div>
      </article>

      <van-empty v-if="!filteredList.length" :description="emptyText" />
    </section>

    <van-dialog v-model:show="showCheckin" title="活动签到" show-cancel-button @confirm="submitCheckin">
      <van-field v-model="checkinCode" label="签到码" placeholder="输入现场 6 位签到码" maxlength="6" />
    </van-dialog>

    <van-popup v-model:show="showReview" position="bottom" round :style="{ maxHeight: '85vh' }">
      <div class="review-popup">
        <h3>活动互评</h3>
        <p class="review-sub">为同行伙伴打个分并写一句简短感受（1-5 星）</p>
        <div v-if="reviewLoading" class="review-loading">加载中…</div>
        <div v-for="user in reviewCandidates" :key="user.userId" class="review-row">
          <div class="review-user">
            <span class="review-name">{{ user.nickname || '伙伴' }}</span>
            <van-rate v-model="reviewDraft[user.userId].rating" :count="5" size="18" color="#ff5f84" />
          </div>
          <van-field
            v-if="!user.reviewed"
            v-model="reviewDraft[user.userId].comment"
            rows="1"
            autosize
            type="textarea"
            maxlength="100"
            placeholder="可选：一句话评价"
            class="review-comment"
          />
          <van-tag v-if="user.reviewed" type="success" plain>已评</van-tag>
          <van-button v-else size="mini" type="primary" @click="submitReview(user.userId)">提交</van-button>
        </div>
        <van-empty v-if="!reviewLoading && !reviewCandidates.length" description="暂无可评价对象" image-size="60" />
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
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
const activeTab = ref('all')
const showCheckin = ref(false)
const showReview = ref(false)
const checkinCode = ref('')
const activeEventId = ref('')
const reviewLoading = ref(false)
const reviewCandidates = ref([])
const reviewDraft = reactive({})

const STATUS_MAP = {
  upcoming: { label: '待参加', type: 'primary' },
  checked_in: { label: '已签到', type: 'success' },
  review_pending: { label: '待互评', type: 'warning' },
  completed: { label: '已完成', type: 'default' },
  missed: { label: '未签到', type: 'danger' }
}

const tabCounts = computed(() => {
  const counts = { upcoming: 0, review_pending: 0, completed: 0 }
  list.value.forEach((item) => {
    if (item.status === 'upcoming' || item.status === 'checked_in') counts.upcoming += 1
    if (item.status === 'review_pending') counts.review_pending += 1
    if (item.status === 'completed') counts.completed += 1
  })
  return counts
})

const pendingReviewTotal = computed(() =>
  list.value.reduce((sum, item) => sum + Number(item.pendingReviewCount || 0), 0)
)

const filteredList = computed(() => {
  if (activeTab.value === 'all') return list.value
  if (activeTab.value === 'upcoming') {
    return list.value.filter((item) => item.status === 'upcoming' || item.status === 'checked_in')
  }
  return list.value.filter((item) => item.status === activeTab.value)
})

const emptyText = computed(() => {
  if (activeTab.value === 'review_pending') return '暂无待互评活动'
  if (activeTab.value === 'upcoming') return '暂无待参加活动'
  if (activeTab.value === 'completed') return '暂无已完成活动'
  return '你还没有报名活动'
})

function statusTag(status) {
  return STATUS_MAP[status] || { label: '已报名', type: 'primary' }
}

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
    showToast({ type: 'success', message: '签到成功，活动结束后可为伙伴互评' })
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
    await loadList()
    const rows = await fetchEventReviewCandidates(activeEventId.value)
    reviewCandidates.value = Array.isArray(rows) ? rows : []
    if (reviewCandidates.value.every((u) => u.reviewed)) {
      showReview.value = false
    }
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '提交失败' })
  }
}

async function scrollToPendingReview() {
  activeTab.value = 'review_pending'
  await nextTick()
  const el = document.querySelector('[data-status="review_pending"]')
  el?.scrollIntoView?.({ behavior: 'smooth', block: 'center' })
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
  padding: 0 0 12px;
}

.review-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin: 12px;
  padding: 12px 14px;
  border-radius: 12px;
  background: linear-gradient(135deg, #fff7ed, #ffedd5);
  border: 1px solid #fdba74;
}

.review-banner-title {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: #9a3412;
}

.review-banner-sub {
  margin: 4px 0 0;
  font-size: 12px;
  color: #c2410c;
}

.event-item {
  background: #fff;
  border-radius: 12px;
  padding: 12px;
  margin: 10px 12px 0;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.event-main {
  flex: 1;
}

.title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
}

.event-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  flex-shrink: 0;
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

.pending-tag {
  margin: 6px 0 0;
  font-size: 12px;
  color: #d97706;
  font-weight: 600;
}

.review-popup {
  padding: 16px;
  overflow-y: auto;
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
  flex-direction: column;
  gap: 8px;
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;
  font-size: 13px;
}

.review-user {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.review-name {
  flex: 1;
  font-weight: 600;
}

.review-comment {
  padding: 0;
}

.review-loading {
  text-align: center;
  padding: 20px 0;
  color: #9ca3af;
}
</style>
