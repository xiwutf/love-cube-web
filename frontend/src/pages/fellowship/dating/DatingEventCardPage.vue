<template>
  <div class="dating-card-page">
    <NavBar title="介绍卡" left-arrow @click-left="router.back()" />

    <div v-if="loading" class="loading-wrap"><van-loading size="24" /></div>
    <section v-else-if="error" class="state-box">
      <p>{{ error }}</p>
    </section>
    <section v-else class="content">
      <DatingIntroCard :card="card" />
      <van-button
        v-if="!card.isSelf"
        block
        round
        :type="card.connected ? 'default' : 'primary'"
        :color="card.connected ? undefined : 'linear-gradient(135deg, #ff5f84, #ff8fab)'"
        :disabled="card.connected || connecting"
        :loading="connecting"
        @click="markConnected"
      >
        {{ card.connected ? '已认识' : '认识了TA' }}
      </van-button>
      <van-button
        v-if="!card.isSelf"
        block
        round
        :type="card.liked ? 'default' : 'primary'"
        :color="card.liked ? undefined : 'linear-gradient(135deg, #f43f5e, #fb7185)'"
        :disabled="card.liked || liking"
        :loading="liking"
        @click="markLiked"
      >
        {{ card.liked ? '已喜欢' : '♡ 喜欢TA' }}
      </van-button>
      <van-button block round plain type="primary" @click="speakCard">朗读模式</van-button>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import NavBar from '@/components/NavBar.vue'
import DatingIntroCard from '@/components/fellowship/DatingIntroCard.vue'
import { createDatingConnection, createDatingLike, fetchDatingParticipantCard } from '@/api/datingEvent.js'

const route = useRoute()
const router = useRouter()
const eventId = computed(() => route.params.eventId)
const participantKey = computed(() => {
  const raw = route.params.participantKey || route.params.userId || ''
  const decoded = decodeURIComponent(raw)
  if (decoded.includes(':')) return decoded
  return `REGISTERED:${decoded}`
})

const loading = ref(true)
const connecting = ref(false)
const liking = ref(false)
const error = ref('')
const card = ref({})

function resolveTargetPayload(data) {
  if (data.participantType && (data.userId || data.guestParticipantId)) {
    return {
      targetParticipantType: data.participantType,
      targetParticipantId: data.guestParticipantId || data.userId
    }
  }
  const key = data.participantKey || participantKey.value
  const [type, id] = String(key).split(':')
  return {
    targetParticipantType: type,
    targetParticipantId: Number(id)
  }
}

function buildSpeechText(data) {
  const parts = [
    `活动编号 ${data.badgeLabel || ''}`,
    data.age ? `年龄 ${data.age} 岁` : '',
    data.city ? `来自 ${data.city}` : '',
    data.occupation ? `职业 ${data.occupation}` : '',
    data.selfIntro ? `自我介绍：${data.selfIntro}` : ''
  ]
  return parts.filter(Boolean).join('。')
}

function speakCard() {
  if (!window.speechSynthesis) {
    showToast('当前浏览器不支持朗读')
    return
  }
  const utter = new SpeechSynthesisUtterance(buildSpeechText(card.value))
  utter.lang = 'zh-CN'
  window.speechSynthesis.cancel()
  window.speechSynthesis.speak(utter)
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    card.value = await fetchDatingParticipantCard(eventId.value, participantKey.value)
  } catch (e) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

async function markConnected() {
  if (card.value.connected || card.value.isSelf) return
  connecting.value = true
  try {
    await createDatingConnection(eventId.value, resolveTargetPayload(card.value))
    card.value = { ...card.value, connected: true }
    showToast('已加入我的认识列表')
  } catch (e) {
    showToast(e.message || '操作失败')
  } finally {
    connecting.value = false
  }
}

async function markLiked() {
  if (card.value.liked || card.value.isSelf) return
  liking.value = true
  try {
    await createDatingLike(eventId.value, resolveTargetPayload(card.value))
    card.value = { ...card.value, liked: true }
    showToast('已加入我的喜欢列表')
  } catch (e) {
    showToast(e.message || '操作失败')
  } finally {
    liking.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.dating-card-page {
  min-height: 100vh;
  background: #f8fafc;
}

.loading-wrap,
.state-box {
  padding: 48px 20px;
  text-align: center;
  color: #64748b;
}

.content {
  padding: 16px;
  display: grid;
  gap: 16px;
}
</style>
