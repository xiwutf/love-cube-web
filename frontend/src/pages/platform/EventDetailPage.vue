<template>
  <section class="platform-page module-page">
    <div class="detail-nav-row">
      <router-link :to="eventsPath()" class="platform-backlink">返回活动列表</router-link>
      <router-link to="/platform/my-activities" class="platform-link">我的活动</router-link>
    </div>

    <article v-if="item" class="platform-card detail-article">
      <div class="detail-head">
        <span class="status-badge" :class="displayStatus.tone">{{ displayStatus.label }}</span>
        <p class="module-card-meta">
          开始：{{ formatDate(item.eventTime || item.time, true) }}
          · 结束：{{ formatDate(effectiveEndTime, true) }}
          · 地点：{{ item.location || '待定' }}
        </p>
      </div>
      <h1 class="detail-title">{{ item.title || '未命名活动' }}</h1>
      <p class="detail-lead">{{ item.summary || '暂无活动说明' }}</p>
      <div class="detail-body">
        <p class="detail-content">{{ item.content || '暂无活动详情。' }}</p>
        <div class="detail-stat">当前报名：{{ item.signupCount || 0 }} 人</div>

        <section v-if="userStore.isAdmin && item.status === 'published'" class="organizer-onsite-card">
          <h2 class="section-subtitle">现场入口</h2>
          <p class="section-hint">参与者扫码此链接可快速加入本场活动（报名与签到）。</p>
          <p class="organizer-onsite-link">{{ onsiteEntryUrl }}</p>
          <button type="button" class="platform-btn sm" @click="copyOnsiteLink">复制现场链接</button>
          <p v-if="onsiteCopyHint" class="organizer-onsite-hint">{{ onsiteCopyHint }}</p>
        </section>

        <!-- 参与状态卡 -->
        <section v-if="userStore.isLoggedIn" class="participation-card">
          <h2 class="section-subtitle">我的参与状态</h2>
          <p class="participation-status" :class="participationTone">{{ participationLabel }}</p>
          <p v-if="signupRow?.checkedInAt" class="participation-meta">签到于 {{ formatDate(signupRow.checkedInAt, true) }}</p>
          <p v-if="signupRow?.badgeLabel" class="participation-meta dating-badge">
            活动编号：{{ signupRow.badgeLabel }}
          </p>
          <p v-if="signupRow?.pendingReviewCount > 0" class="participation-meta warn">
            还有 {{ signupRow.pendingReviewCount }} 位伙伴待互评
          </p>
        </section>

        <section v-if="isDatingEvent && signedUp && checkedIn" class="engagement-section dating-entry">
          <h2 class="section-subtitle">联谊专场</h2>
          <p class="section-hint">完善本场活动资料，查看介绍卡与参与者花名册。</p>
          <router-link
            :to="`/fellowship/events/${item.id}/dating`"
            class="platform-btn platform-btn-primary sm"
          >
            进入联谊专场
          </router-link>
        </section>

        <!-- 报名 -->
        <div v-if="!signedUp" class="event-signup-panel">
          <button
            class="platform-btn platform-btn-primary event-signup-btn"
            :disabled="signingUp || !userStore.isLoggedIn"
            @click="handleSignup"
          >
            {{ signupButtonText }}
          </button>
          <span v-if="!userStore.isLoggedIn" class="event-signup-hint">
            <router-link to="/login">登录</router-link> 后即可报名
          </span>
          <span v-else class="event-signup-hint">报名后请留意站内消息与活动提醒</span>
        </div>

        <!-- 签到区 -->
        <section v-if="signedUp && showCheckinSection" class="engagement-section">
          <h2 class="section-subtitle">现场签到</h2>
          <p class="section-hint">请输入组织者提供的 6 位签到码</p>
          <div class="checkin-row">
            <input
              v-model.trim="checkinCode"
              type="text"
              maxlength="6"
              class="checkin-input"
              placeholder="签到码"
              :disabled="checkinSaving"
            >
            <button
              type="button"
              class="platform-btn platform-btn-primary"
              :disabled="checkinSaving || !checkinCode"
              @click="handleCheckin"
            >
              {{ checkinSaving ? '签到中…' : '确认签到' }}
            </button>
          </div>
          <p v-if="checkinMessage" class="feedback-msg" :class="{ error: checkinError }">{{ checkinMessage }}</p>
        </section>

        <!-- 签到后现场状态说明 -->
        <section v-if="signedUp && checkedIn && onSiteMessage" class="engagement-section on-site-card">
          <h2 class="section-subtitle">现场状态</h2>
          <p class="on-site-text">{{ onSiteMessage }}</p>
        </section>

        <!-- 互评区 -->
        <section v-if="signedUp && showReviewSection" class="engagement-section">
          <h2 class="section-subtitle">活动互评</h2>
          <p class="section-hint">为已签到的伙伴评分（1-5 星），每人仅可评价一次。</p>
          <div v-if="reviewLoading" class="inline-state">加载中…</div>
          <div v-for="user in reviewCandidates" :key="user.userId" class="review-row">
            <div class="review-head">
              <strong>{{ user.nickname || '伙伴' }}</strong>
              <div class="star-row">
                <button
                  v-for="n in 5"
                  :key="n"
                  type="button"
                  class="star-btn"
                  :class="{ on: (reviewDraft[user.userId]?.rating || 0) >= n }"
                  :disabled="user.reviewed"
                  @click="setRating(user.userId, n)"
                >★</button>
              </div>
            </div>
            <textarea
              v-if="!user.reviewed"
              v-model="reviewDraft[user.userId].comment"
              rows="2"
              maxlength="100"
              placeholder="可选：一句话评价"
            />
            <span v-else class="status-badge success">已完成互评</span>
            <button
              v-if="!user.reviewed"
              type="button"
              class="platform-btn platform-btn-primary sm"
              :disabled="reviewSubmitting === user.userId"
              @click="handleSubmitReview(user.userId)"
            >
              {{ reviewSubmitting === user.userId ? '提交中…' : '提交评价' }}
            </button>
          </div>
          <p v-if="!reviewLoading && reviewCompleted" class="feedback-msg success">你已完成本场活动互评，感谢参与！</p>
          <p v-else-if="!reviewLoading && !reviewCandidates.length" class="inline-state">暂无可评价对象</p>
        </section>

        <p v-if="signupMessage && !signedUp" class="event-signup-message">{{ signupMessage }}</p>
      </div>
    </article>

    <article v-else class="platform-card module-empty">
      <h3 class="platform-heading">活动不存在</h3>
      <p class="platform-text">该活动可能已下线或链接错误，请返回活动中心查看</p>
    </article>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import {
  fetchEventDetail,
  signupEvent,
  getMyEventSignups,
  checkinEvent,
  fetchEventReviewCandidates,
  submitEventReview
} from '@/api/platformContent.js'
import { usePlatformPath } from '@/composables/usePlatformPath.js'
import { useUserStore } from '@/stores/user.js'
import {
  getActivityDisplayStatus,
  getOnSiteStatusMessage,
  getParticipationStatusLabel,
  getParticipationStatusTone,
  resolveEffectiveEndMs
} from '@/utils/activityStatus.js'
import { createEventOnsiteUrl } from '@/utils/eventOnsiteUrl.js'

const route = useRoute()
const userStore = useUserStore()
const { eventsPath } = usePlatformPath()

const item = ref(null)
const signupRow = ref(null)
const signingUp = ref(false)
const signupMessage = ref('')

const checkinCode = ref('')
const checkinSaving = ref(false)
const checkinMessage = ref('')
const checkinError = ref(false)

const reviewCandidates = ref([])
const reviewLoading = ref(false)
const reviewDraft = reactive({})
const reviewSubmitting = ref(null)

const onsiteEntryUrl = computed(() => (item.value?.id ? createEventOnsiteUrl(item.value.id) : ''))
const onsiteCopyHint = ref('')

const signedUp = computed(() => Boolean(signupRow.value))
const checkedIn = computed(() => Boolean(signupRow.value?.checkedIn))
const eventEnded = computed(() => Boolean(signupRow.value?.eventEnded))
const checkinEnabled = computed(() => Boolean(signupRow.value?.checkinEnabled))
const reviewCompleted = computed(() => Boolean(signupRow.value?.reviewCompleted))
const isDatingEvent = computed(() => {
  const t = item.value?.templateType || signupRow.value?.templateType
  return String(t || '').toUpperCase() === 'DATING'
})

const effectiveEndTime = computed(() => {
  const ms = resolveEffectiveEndMs(item.value || {})
  if (ms == null) return ''
  return new Date(ms).toISOString().replace('T', ' ').slice(0, 19)
})

const displayStatus = computed(() => getActivityDisplayStatus({
  status: item.value?.status || 'published',
  eventTime: item.value?.eventTime || item.value?.time,
  endTime: item.value?.endTime || effectiveEndTime.value
}))

const onSiteMessage = computed(() => {
  if (!checkedIn.value) return null
  return getOnSiteStatusMessage(signupRow.value?.status || '')
})

const participationLabel = computed(() => {
  if (!userStore.isLoggedIn) return '登录后查看参与状态'
  if (!signedUp.value) return '未报名'
  return getParticipationStatusLabel(signupRow.value?.status || 'upcoming')
})

const participationTone = computed(() => {
  if (!signedUp.value) return 'neutral'
  return getParticipationStatusTone(signupRow.value?.status || 'upcoming')
})

const signupButtonText = computed(() => {
  if (!userStore.isLoggedIn) return '登录后报名'
  return signingUp.value ? '报名中...' : '报名参加'
})

const showCheckinSection = computed(() =>
  signedUp.value && checkinEnabled.value && !checkedIn.value && !eventEnded.value
)

const showReviewSection = computed(() =>
  signedUp.value && checkedIn.value && eventEnded.value && !reviewCompleted.value
)

function formatDate(value, withTime = false) {
  if (!value) return ''
  const normalized = String(value).replace('T', ' ')
  return withTime ? normalized.slice(0, 16) : normalized.slice(0, 10)
}

async function loadSignupState() {
  if (!userStore.isLoggedIn || !item.value?.id) {
    signupRow.value = null
    return
  }
  try {
    const rows = await getMyEventSignups()
    const list = Array.isArray(rows) ? rows : []
    signupRow.value = list.find((r) => r.eventId === item.value.id) || null
  } catch {
    signupRow.value = null
  }
}

async function loadReviewCandidates() {
  if (!showReviewSection.value || !item.value?.id) {
    reviewCandidates.value = []
    return
  }
  reviewLoading.value = true
  try {
    const rows = await fetchEventReviewCandidates(item.value.id)
    reviewCandidates.value = Array.isArray(rows) ? rows : []
    reviewCandidates.value.forEach((user) => {
      if (!reviewDraft[user.userId]) {
        reviewDraft[user.userId] = { rating: 5, comment: '' }
      }
    })
  } catch {
    reviewCandidates.value = []
  } finally {
    reviewLoading.value = false
  }
}

onMounted(async () => {
  try {
    item.value = await fetchEventDetail(route.params.id)
    await loadSignupState()
    await loadReviewCandidates()
  } catch {
    item.value = null
  }
})

watch(showReviewSection, (val) => {
  if (val) loadReviewCandidates()
})

async function copyOnsiteLink() {
  const url = onsiteEntryUrl.value
  if (!url) return
  onsiteCopyHint.value = ''
  try {
    await navigator.clipboard.writeText(url)
    onsiteCopyHint.value = '现场链接已复制'
  } catch {
    onsiteCopyHint.value = '复制失败，请手动复制上方链接'
  }
}

async function handleSignup() {
  if (!item.value || signingUp.value || signedUp.value || !userStore.isLoggedIn) return
  signingUp.value = true
  signupMessage.value = ''

  try {
    const result = await signupEvent(item.value.id)
    item.value.signupCount = result.signupCount ?? item.value.signupCount
    signupMessage.value = result.message || '报名成功'
    await loadSignupState()
  } catch (error) {
    signupMessage.value = error.message || '报名失败，请稍后再试'
  } finally {
    signingUp.value = false
  }
}

async function handleCheckin() {
  if (!item.value || !checkinCode.value.trim()) {
    checkinMessage.value = '请输入签到码'
    checkinError.value = true
    return
  }
  checkinSaving.value = true
  checkinMessage.value = ''
  checkinError.value = false
  try {
    const res = await checkinEvent(item.value.id, checkinCode.value.trim())
    const badge = res?.datingIdentity?.badgeLabel
    checkinMessage.value = badge
      ? `${res?.message || '签到成功'}，你的活动编号：${badge}`
      : (res?.message || '签到成功')
    checkinCode.value = ''
    await loadSignupState()
  } catch (error) {
    checkinError.value = true
    checkinMessage.value = error.message || '签到码不正确或活动尚未开启签到，请向组织者确认'
  } finally {
    checkinSaving.value = false
  }
}

function setRating(userId, rating) {
  if (!reviewDraft[userId]) reviewDraft[userId] = { rating: 5, comment: '' }
  reviewDraft[userId].rating = rating
}

async function handleSubmitReview(targetUserId) {
  const draft = reviewDraft[targetUserId]
  if (!draft?.rating || !item.value) return
  reviewSubmitting.value = targetUserId
  try {
    await submitEventReview(item.value.id, {
      targetUserId,
      rating: draft.rating,
      comment: draft.comment || ''
    })
    await loadSignupState()
    await loadReviewCandidates()
  } catch (error) {
    checkinMessage.value = error.message || '提交失败'
    checkinError.value = true
  } finally {
    reviewSubmitting.value = null
  }
}
</script>

<style scoped>
.detail-content {
  margin: 0;
  white-space: pre-line;
}

.detail-head {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--lc-space-3);
  margin-bottom: var(--lc-space-2);
}

.section-subtitle {
  font-size: 1rem;
  margin: 0 0 var(--lc-space-2);
}

.organizer-onsite-card {
  margin: var(--lc-space-4) 0;
  padding: var(--lc-space-4);
  border: 1px dashed var(--lc-pink-border, #f5c2d6);
  border-radius: var(--lc-radius);
  background: var(--lc-pink-light, #fff5f9);
}

.organizer-onsite-link {
  margin: 0 0 var(--lc-space-3);
  font-size: 12px;
  word-break: break-all;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  color: var(--lc-text);
}

.organizer-onsite-hint {
  margin: var(--lc-space-2) 0 0;
  font-size: var(--lc-text-sm);
  color: #15803d;
}

.participation-card {
  margin: var(--lc-space-6) 0;
  padding: var(--lc-space-4);
  border: 1px solid var(--lc-blue-border);
  border-radius: var(--lc-radius);
  background: linear-gradient(135deg, rgba(239, 246, 255, 0.6), rgba(255, 255, 255, 0.9));
}

.participation-status {
  font-size: 1.1rem;
  font-weight: 700;
  margin: 0;
}

.participation-status.info { color: var(--lc-blue-dark); }
.participation-status.success { color: #15803d; }
.participation-status.warning { color: #b45309; }
.participation-status.danger { color: #b91c1c; }
.participation-status.neutral { color: var(--lc-muted); }

.on-site-card {
  padding: var(--lc-space-4);
  border: 1px solid var(--lc-blue-border);
  border-radius: var(--lc-radius);
  background: linear-gradient(135deg, rgba(240, 253, 244, 0.5), rgba(255, 255, 255, 0.95));
}

.on-site-text {
  margin: 0;
  color: var(--lc-text);
  line-height: 1.7;
  font-size: var(--lc-text-sm);
}

.participation-meta {
  margin: var(--lc-space-2) 0 0;
  color: var(--lc-muted);
  font-size: var(--lc-text-sm);
}

.participation-meta.warn { color: #b45309; }

.participation-meta.dating-badge {
  color: #be185d;
  font-weight: 700;
}

.dating-entry {
  padding: var(--lc-space-4);
  border: 1px solid #fbcfe8;
  border-radius: var(--lc-radius);
  background: linear-gradient(135deg, rgba(255, 241, 246, 0.8), rgba(255, 255, 255, 0.95));
}

.event-signup-panel {
  display: flex;
  align-items: center;
  gap: var(--lc-space-4);
  flex-wrap: wrap;
  margin-top: var(--lc-space-6);
}

.engagement-section {
  margin-top: var(--lc-space-6);
  padding-top: var(--lc-space-4);
  border-top: 1px solid var(--lc-soft-alt);
}

.muted-box {
  padding: var(--lc-space-4);
  background: var(--lc-bg);
  border-radius: var(--lc-radius);
  color: var(--lc-muted);
}

.section-hint {
  color: var(--lc-muted);
  font-size: var(--lc-text-sm);
  margin: 0 0 var(--lc-space-3);
}

.checkin-row {
  display: flex;
  flex-wrap: wrap;
  gap: var(--lc-space-3);
  align-items: center;
}

.checkin-input {
  flex: 1;
  min-width: 140px;
  padding: 10px 12px;
  border: 1px solid var(--lc-soft-alt);
  border-radius: 8px;
  font-size: 1rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.review-row {
  padding: var(--lc-space-3) 0;
  border-bottom: 1px solid var(--lc-soft-alt);
  display: grid;
  gap: var(--lc-space-2);
}

.review-head {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: center;
  gap: var(--lc-space-2);
}

.star-row { display: flex; gap: 2px; }

.star-btn {
  border: 0;
  background: none;
  color: #d1d5db;
  cursor: pointer;
  font-size: 1.25rem;
  padding: 0;
}

.star-btn.on { color: #f59e0b; }
.star-btn:disabled { cursor: default; }

.platform-btn.sm {
  padding: 6px 14px;
  font-size: var(--lc-text-sm);
  width: fit-content;
}

.feedback-msg {
  margin-top: var(--lc-space-3);
  color: var(--lc-blue-dark);
  font-size: var(--lc-text-sm);
  font-weight: 600;
}

.feedback-msg.error { color: #b91c1c; }
.feedback-msg.success { color: #15803d; }

.event-signup-btn:disabled {
  cursor: not-allowed;
  opacity: 0.68;
}

.event-signup-hint,
.event-signup-message {
  color: var(--lc-muted);
  font-size: var(--lc-text-sm);
}

.event-signup-message {
  margin: var(--lc-space-3) 0 0;
  color: var(--lc-blue-dark);
  font-weight: 700;
}

.inline-state {
  color: var(--lc-muted);
  font-size: var(--lc-text-sm);
}
</style>
