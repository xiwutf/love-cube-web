<template>
  <section class="onsite-page">
    <div class="onsite-shell">
      <header class="onsite-head">
        <p class="onsite-kicker">现场快速加入</p>
        <h1 class="onsite-title">扫码加入本场活动</h1>
        <p v-if="context.title" class="onsite-event">{{ context.title }}</p>
        <p v-if="context.location || context.eventTime" class="onsite-meta">
          <span v-if="context.eventTime">{{ formatTime(context.eventTime) }}</span>
          <span v-if="context.location"> · {{ context.location }}</span>
        </p>
      </header>

      <div v-if="loading" class="onsite-state">加载中…</div>
      <div v-else-if="loadError" class="onsite-state error">{{ loadError }}</div>

      <div v-else-if="guestMode === 'choice'" class="onsite-body">
        <div class="status-card info">
          <p class="status-label">未登录</p>
          <p class="status-message">选择参加方式</p>
        </div>
        <div class="action-stack">
          <button
            v-if="isDatingEventFromContext"
            class="onsite-btn primary"
            type="button"
            @click="guestMode = 'form'"
          >
            快速参加本场活动
          </button>
          <button class="onsite-btn ghost" type="button" @click="goLogin">登录已有账号</button>
        </div>
      </div>

      <div v-else-if="guestMode === 'form'" class="onsite-body">
        <div class="status-card info">
          <p class="status-label">活动游客</p>
          <p class="status-message">填写最少信息即可参加本场活动</p>
        </div>
        <form class="guest-form" @submit.prevent="handleGuestStart">
          <label class="guest-field">
            <span>昵称</span>
            <input v-model.trim="guestForm.nickname" type="text" maxlength="20" placeholder="如何称呼你" required>
          </label>
          <label class="guest-field">
            <span>性别</span>
            <select v-model="guestForm.genderSide" required>
              <option disabled value="">请选择</option>
              <option value="MALE">男</option>
              <option value="FEMALE">女</option>
              <option value="OTHER">其他</option>
            </select>
          </label>
          <label class="guest-field">
            <span>手机号（选填）</span>
            <input v-model.trim="guestForm.mobile" type="tel" maxlength="11" placeholder="便于找回本场身份">
          </label>
          <button class="onsite-btn primary" type="submit" :disabled="guestStarting">
            {{ guestStarting ? '创建中…' : '开始参加' }}
          </button>
          <button class="onsite-btn ghost" type="button" @click="guestMode = 'choice'">返回</button>
        </form>
        <p v-if="actionMessage" class="feedback" :class="{ error: actionError }">{{ actionMessage }}</p>
      </div>

      <div v-else class="onsite-body">
        <div class="status-card" :class="statusTone">
          <p class="status-label">{{ statusTitle }}</p>
          <p class="status-message">{{ context.statusMessage || '请按提示继续' }}</p>
          <p v-if="context.badgeLabel" class="status-badge">你的编号：{{ context.badgeLabel }}</p>
          <p v-if="isGuestSession && context.nickname" class="status-guest">游客昵称：{{ context.nickname }}</p>
        </div>

        <div v-if="!userStore.isLoggedIn && !isGuestSession" class="action-stack">
          <button class="onsite-btn primary" type="button" @click="guestMode = 'choice'">选择参加方式</button>
        </div>

        <div v-else-if="context.eventStatus === 'CLOSED'" class="action-stack">
          <button class="onsite-btn ghost" type="button" @click="goEventDetail">返回活动详情</button>
        </div>

        <div v-else-if="context.eventStatus === 'ENDED'" class="action-stack">
          <button class="onsite-btn ghost" type="button" @click="goEventDetail">返回活动详情</button>
        </div>

        <div v-else-if="context.eventStatus === 'FULL' && !context.registered" class="action-stack">
          <button class="onsite-btn ghost" type="button" @click="goEventDetail">返回活动详情</button>
        </div>

        <div v-else-if="context.canJoin" class="action-stack">
          <button class="onsite-btn primary" type="button" :disabled="joining" @click="handleJoin">
            {{ joining ? '加入中…' : '确认加入本场活动' }}
          </button>
        </div>

        <div v-else-if="context.canCheckin" class="action-stack">
          <label class="checkin-field">
            <span>现场签到码</span>
            <input
              v-model.trim="checkinCode"
              type="text"
              maxlength="6"
              class="checkin-input"
              placeholder="输入 6 位签到码"
              :disabled="checkingIn"
            >
          </label>
          <button
            class="onsite-btn primary"
            type="button"
            :disabled="checkingIn || !checkinCode"
            @click="handleCheckin"
          >
            {{ checkingIn ? '签到中…' : '确认签到' }}
          </button>
        </div>

        <div v-else-if="context.checkedIn" class="action-stack">
          <button
            v-if="isDatingEvent"
            class="onsite-btn primary"
            type="button"
            @click="goTarget(context.targetUrl)"
          >
            进入联谊专场
          </button>
          <button v-if="!isGuestSession" class="onsite-btn ghost" type="button" @click="goEventDetail">返回活动详情</button>
        </div>

        <div v-else class="action-stack">
          <button v-if="!isGuestSession" class="onsite-btn ghost" type="button" @click="goEventDetail">返回活动详情</button>
        </div>

        <p v-if="actionMessage" class="feedback" :class="{ error: actionError }">{{ actionMessage }}</p>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchOnsiteContext, onsiteCheckin, onsiteJoin } from '@/api/activityOnsite.js'
import {
  createGuestSession,
  fetchGuestContext,
  guestCheckin,
  guestJoin
} from '@/api/eventGuest.js'
import { useUserStore } from '@/stores/user.js'
import { hasEventGuestToken, saveEventGuestSession } from '@/utils/eventGuestToken.js'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const eventId = computed(() => route.params.id)
const loading = ref(true)
const loadError = ref('')
const context = ref({})
const checkinCode = ref('')
const joining = ref(false)
const checkingIn = ref(false)
const actionMessage = ref('')
const actionError = ref(false)
const guestMode = ref(null)
const guestStarting = ref(false)
const guestForm = reactive({
  nickname: '',
  genderSide: '',
  mobile: ''
})

const isGuestSession = computed(() => !userStore.isLoggedIn && hasEventGuestToken(eventId.value))
const isDatingEvent = computed(() => String(context.value.templateType || '').toUpperCase() === 'DATING')
const isDatingEventFromContext = computed(() => isDatingEvent.value)

const statusTitle = computed(() => {
  if (isGuestSession.value) return '活动游客'
  if (!userStore.isLoggedIn) return '未登录'
  if (context.value.checkedIn) return '已签到'
  if (context.value.canCheckin) return '待签到'
  if (context.value.registered) return '已加入'
  if (context.value.canJoin) return '待加入'
  if (context.value.eventStatus === 'FULL') return '名额已满'
  if (context.value.eventStatus === 'ENDED') return '活动已结束'
  if (context.value.eventStatus === 'CLOSED') return '活动未开放'
  return '活动状态'
})

const statusTone = computed(() => {
  if (context.value.checkedIn) return 'success'
  if (context.value.eventStatus === 'ENDED' || context.value.eventStatus === 'CLOSED') return 'neutral'
  if (context.value.eventStatus === 'FULL' && !context.value.registered) return 'danger'
  return 'info'
})

function formatTime(value) {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(0, 16)
}

async function loadContext() {
  loading.value = true
  loadError.value = ''
  try {
    if (userStore.isLoggedIn) {
      guestMode.value = null
      context.value = await fetchOnsiteContext(eventId.value)
      return
    }
    if (hasEventGuestToken(eventId.value)) {
      guestMode.value = null
      context.value = await fetchGuestContext(eventId.value)
      return
    }
    context.value = await fetchOnsiteContext(eventId.value)
    guestMode.value = 'choice'
  } catch (e) {
    if (!userStore.isLoggedIn && hasEventGuestToken(eventId.value)) {
      loadError.value = e.message || '游客身份加载失败'
    } else if (!userStore.isLoggedIn) {
      guestMode.value = 'choice'
      context.value = {}
    } else {
      loadError.value = e.message || '活动信息加载失败'
    }
  } finally {
    loading.value = false
  }
}

function goLogin() {
  router.push({
    path: '/login',
    query: { redirect: encodeURIComponent(route.fullPath) }
  })
}

function goEventDetail() {
  router.push(`/events/${eventId.value}`)
}

function goTarget(path) {
  if (!path) return
  router.push(path)
}

async function handleGuestStart() {
  if (!guestForm.nickname || !guestForm.genderSide) return
  guestStarting.value = true
  actionMessage.value = ''
  actionError.value = false
  try {
    const session = await createGuestSession(eventId.value, {
      nickname: guestForm.nickname,
      genderSide: guestForm.genderSide,
      mobile: guestForm.mobile || undefined
    })
    saveEventGuestSession(eventId.value, session)
    guestMode.value = null
    const joinRes = await guestJoin(eventId.value)
    actionMessage.value = joinRes?.message || '已加入本场活动'
    await loadContext()
  } catch (e) {
    actionError.value = true
    actionMessage.value = e.message || '创建游客身份失败'
  } finally {
    guestStarting.value = false
  }
}

async function handleJoin() {
  joining.value = true
  actionMessage.value = ''
  actionError.value = false
  try {
    const res = isGuestSession.value
      ? await guestJoin(eventId.value)
      : await onsiteJoin(eventId.value)
    actionMessage.value = res?.message || '已加入本场活动'
    await loadContext()
  } catch (e) {
    actionError.value = true
    actionMessage.value = e.message || '加入失败'
  } finally {
    joining.value = false
  }
}

async function handleCheckin() {
  if (!checkinCode.value.trim()) return
  checkingIn.value = true
  actionMessage.value = ''
  actionError.value = false
  try {
    const res = isGuestSession.value
      ? await guestCheckin(eventId.value, checkinCode.value.trim())
      : await onsiteCheckin(eventId.value, checkinCode.value.trim())
    const badge = res?.datingIdentity?.badgeLabel
    actionMessage.value = badge
      ? `${res?.message || '签到成功'}，你的编号：${badge}`
      : (res?.message || '签到成功')
    await loadContext()
    if (res?.targetUrl) {
      setTimeout(() => router.replace(res.targetUrl), 600)
    }
  } catch (e) {
    actionError.value = true
    actionMessage.value = e.message || '签到码不正确，请向组织者确认'
  } finally {
    checkingIn.value = false
  }
}

onMounted(loadContext)

watch(() => userStore.token, () => {
  if (userStore.token) {
    guestMode.value = null
    loadContext()
  }
})
</script>

<style scoped>
.onsite-page {
  min-height: calc(100vh - 80px);
  display: flex;
  justify-content: center;
  padding: 20px 16px 40px;
  background: linear-gradient(180deg, #eff6ff 0%, #fff 45%);
}

.onsite-shell {
  width: 100%;
  max-width: 480px;
}

.onsite-head {
  text-align: center;
  margin-bottom: 20px;
}

.onsite-kicker {
  margin: 0;
  font-size: 13px;
  color: var(--lc-blue-dark, #1d4ed8);
  font-weight: 700;
}

.onsite-title {
  margin: 8px 0 0;
  font-size: 28px;
  line-height: 1.2;
  color: var(--lc-text, #0f172a);
}

.onsite-event {
  margin: 10px 0 0;
  font-size: 16px;
  font-weight: 700;
  color: #334155;
}

.onsite-meta {
  margin: 8px 0 0;
  font-size: 13px;
  color: var(--lc-muted, #64748b);
}

.onsite-state {
  text-align: center;
  padding: 40px 0;
  color: var(--lc-muted, #64748b);
}

.onsite-state.error {
  color: #b91c1c;
}

.status-card {
  border-radius: 16px;
  padding: 18px 16px;
  margin-bottom: 18px;
  border: 1px solid #dbeafe;
  background: #fff;
}

.status-card.success {
  border-color: #bbf7d0;
  background: #f0fdf4;
}

.status-card.danger {
  border-color: #fecaca;
  background: #fef2f2;
}

.status-card.neutral {
  border-color: #e2e8f0;
  background: #f8fafc;
}

.status-label {
  margin: 0;
  font-size: 12px;
  font-weight: 700;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.status-message {
  margin: 8px 0 0;
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
  line-height: 1.5;
}

.status-badge {
  margin: 10px 0 0;
  font-size: 22px;
  font-weight: 800;
  color: #be185d;
}

.status-guest {
  margin: 8px 0 0;
  font-size: 14px;
  color: #64748b;
}

.action-stack {
  display: grid;
  gap: 12px;
}

.guest-form {
  display: grid;
  gap: 12px;
}

.guest-field {
  display: grid;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #475569;
}

.guest-field input,
.guest-field select {
  min-height: 48px;
  border: 1px solid #cbd5e1;
  border-radius: 12px;
  padding: 0 14px;
  font-size: 16px;
  background: #fff;
}

.onsite-btn {
  width: 100%;
  min-height: 52px;
  border-radius: 14px;
  border: 0;
  font-size: 17px;
  font-weight: 700;
  cursor: pointer;
}

.onsite-btn.primary {
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: #fff;
}

.onsite-btn.ghost {
  background: #fff;
  color: #334155;
  border: 1px solid #cbd5e1;
}

.onsite-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.checkin-field {
  display: grid;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #475569;
}

.checkin-input {
  min-height: 52px;
  border: 1px solid #cbd5e1;
  border-radius: 12px;
  padding: 0 14px;
  font-size: 20px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  text-align: center;
}

.feedback {
  margin: 14px 0 0;
  text-align: center;
  font-size: 14px;
  font-weight: 600;
  color: #1d4ed8;
}

.feedback.error {
  color: #b91c1c;
}
</style>
