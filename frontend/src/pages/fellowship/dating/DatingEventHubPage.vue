<template>
  <div class="dating-hub">
    <NavBar :title="context.eventTitle || '联谊专场'" left-arrow @click-left="router.back()" />

    <div v-if="loading" class="loading-wrap">
      <van-loading size="24" />
    </div>

    <section v-else-if="error" class="state-box">
      <p>{{ error }}</p>
      <van-button size="small" type="primary" round @click="load">重试</van-button>
    </section>

    <section v-else-if="!context.checkedIn" class="state-box">
      <p>请先完成现场签到后再进入联谊专场。</p>
      <van-button size="small" type="primary" round @click="goOnsite">去现场签到</van-button>
    </section>

    <section v-else class="hub-body">
      <div class="badge-card">
        <p class="badge-label">你的活动编号</p>
        <p v-if="badgeLabel" class="badge-no">{{ badgeLabel }}</p>
        <p v-else class="badge-pending">待分配</p>
        <p class="badge-hint">编号仅在本场活动有效，便于现场交流</p>
      </div>

      <div class="progress-card">
        <p class="progress-title">专场进度</p>
        <div class="progress-item" :class="{ done: Boolean(badgeLabel) }">
          <span>① 获得活动编号</span>
          <van-tag v-if="badgeLabel" type="success" plain>已完成</van-tag>
          <van-tag v-else type="warning" plain>待完成</van-tag>
        </div>
        <div class="progress-item" :class="{ done: context.profileCompleted }">
          <span>② 完善本场活动资料</span>
          <van-tag v-if="context.profileCompleted" type="success" plain>已完成</van-tag>
          <van-tag v-else type="warning" plain>待完成</van-tag>
        </div>
        <div class="progress-item">
          <span>③ 浏览参与者花名册</span>
        </div>
      </div>

      <div class="action-grid">
        <van-button
          block
          round
          type="primary"
          color="linear-gradient(135deg, #ff5f84, #ff8fab)"
          @click="goProfile"
        >
          {{ context.profileCompleted ? '编辑活动资料' : '完善活动资料' }}
        </van-button>
        <van-button block round plain type="primary" @click="goMyCard">我的介绍卡</van-button>
        <van-button block round plain type="primary" @click="goRoster">参与者花名册</van-button>
        <van-button block round plain type="primary" @click="goConnections">
          我的认识的人
          <span v-if="connectionCount > 0" class="conn-count">已认识：{{ connectionCount }} 人</span>
        </van-button>
        <van-button block round plain type="primary" @click="goLikes">
          我的喜欢
          <span v-if="likeCount > 0" class="conn-count">已喜欢：{{ likeCount }} 人</span>
        </van-button>
        <van-button block round plain type="primary" @click="goMatches">
          互选结果
          <span class="conn-count">{{ matchHint }}</span>
        </van-button>
        <van-button v-if="isGuestMode" block round plain hairline type="default" @click="showRegisterHint">
          注册账号，保存你的活动记录
        </van-button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import NavBar from '@/components/NavBar.vue'
import { fetchDatingConnectionStats, fetchDatingContext, fetchDatingLikeStats } from '@/api/datingEvent.js'
import { useUserStore } from '@/stores/user.js'
import { hasEventGuestToken } from '@/utils/eventGuestToken.js'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const eventId = computed(() => route.params.eventId)
const isGuestMode = computed(() => !userStore.isLoggedIn && hasEventGuestToken(eventId.value))

const loading = ref(true)
const error = ref('')
const context = ref({})
const connectionCount = ref(0)
const likeCount = ref(0)

const badgeLabel = computed(() => context.value?.identity?.badgeLabel || '')
const matchHint = computed(() => {
  const endTime = context.value?.effectiveEndTime || context.value?.endTime
  if (!endTime) return '活动结束后查看'
  const ended = new Date(String(endTime).replace(' ', 'T')) < new Date()
  return ended ? '查看互选结果' : '活动结束后查看'
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [ctx, connStats, likeStats] = await Promise.all([
      fetchDatingContext(eventId.value),
      fetchDatingConnectionStats(eventId.value).catch(() => ({ totalConnections: 0 })),
      fetchDatingLikeStats(eventId.value).catch(() => ({ totalLikes: 0 }))
    ])
    context.value = ctx
    connectionCount.value = Number(connStats?.totalConnections || 0)
    likeCount.value = Number(likeStats?.totalLikes || 0)
  } catch (e) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

function goOnsite() {
  router.push(`/events/${eventId.value}/onsite`)
}

function showRegisterHint() {
  showToast('注册绑定功能即将上线，当前游客身份仅本场活动有效')
}

function goProfile() {
  router.push(`/fellowship/events/${eventId.value}/dating/profile`)
}

function goMyCard() {
  router.push(`/fellowship/events/${eventId.value}/dating/my-card`)
}

function goRoster() {
  router.push(`/fellowship/events/${eventId.value}/dating/roster`)
}

function goConnections() {
  router.push(`/fellowship/events/${eventId.value}/dating/connections`)
}

function goLikes() {
  router.push(`/fellowship/events/${eventId.value}/dating/likes`)
}

function goMatches() {
  router.push(`/fellowship/events/${eventId.value}/dating/matches`)
}

onMounted(load)
</script>

<style scoped>
.dating-hub {
  min-height: 100vh;
  background: linear-gradient(180deg, #fff5f8 0%, #fff 40%);
}

.loading-wrap,
.state-box {
  padding: 48px 20px;
  text-align: center;
  color: #64748b;
}

.hub-body {
  padding: 16px;
  display: grid;
  gap: 16px;
}

.badge-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px 20px;
  text-align: center;
  box-shadow: 0 8px 24px rgba(255, 95, 132, 0.12);
}

.badge-label {
  margin: 0;
  font-size: 14px;
  color: #64748b;
}

.badge-no {
  margin: 8px 0 0;
  font-size: 42px;
  font-weight: 800;
  letter-spacing: 0.08em;
  color: #ff5f84;
}

.badge-pending {
  margin: 8px 0 0;
  font-size: 24px;
  font-weight: 700;
  color: #b45309;
}

.badge-hint {
  margin: 12px 0 0;
  font-size: 12px;
  color: #94a3b8;
}

.progress-card {
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  border: 1px solid #fce7f3;
}

.progress-title {
  margin: 0 0 12px;
  font-weight: 700;
  color: #334155;
}

.progress-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f1f5f9;
  font-size: 14px;
  color: #475569;
}

.progress-item:last-child {
  border-bottom: 0;
}

.progress-item.done {
  color: #15803d;
}

.action-grid {
  display: grid;
  gap: 10px;
}

.conn-count {
  margin-left: 6px;
  font-size: 12px;
  opacity: 0.85;
}
</style>
