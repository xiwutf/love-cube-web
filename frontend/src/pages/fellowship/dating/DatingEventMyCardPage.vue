<template>
  <div class="dating-card-page">
    <NavBar title="我的介绍卡" left-arrow @click-left="router.back()" />

    <div v-if="loading" class="loading-wrap"><van-loading size="24" /></div>
    <section v-else-if="error" class="state-box">
      <p>{{ error }}</p>
      <van-button size="small" round type="primary" @click="load">重试</van-button>
    </section>
    <section v-else class="content">
      <DatingIntroCard :card="card" />
      <div class="actions">
        <van-button block round plain type="primary" @click="speakCard">朗读模式</van-button>
        <van-button block round type="primary" color="#ff5f84" @click="goProfile">编辑资料</van-button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import NavBar from '@/components/NavBar.vue'
import DatingIntroCard from '@/components/fellowship/DatingIntroCard.vue'
import { fetchDatingMyCard } from '@/api/datingEvent.js'

const route = useRoute()
const router = useRouter()
const eventId = computed(() => route.params.eventId)

const loading = ref(true)
const error = ref('')
const card = ref({})

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
    card.value = await fetchDatingMyCard(eventId.value)
  } catch (e) {
    error.value = e.message || '加载失败'
  } finally {
    loading.value = false
  }
}

function goProfile() {
  router.push(`/fellowship/events/${eventId.value}/dating/profile`)
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

.actions {
  display: grid;
  gap: 10px;
}
</style>
