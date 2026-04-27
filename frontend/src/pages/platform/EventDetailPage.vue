<template>
  <section class="platform-page module-page">
    <div class="detail-nav-row">
      <router-link to="/events" class="platform-backlink">返回活动列表</router-link>
      <router-link to="/fellowship-intro" class="platform-link">查看联谊介绍</router-link>
    </div>

    <article v-if="item" class="platform-card detail-article">
      <p class="module-card-meta">
        活动时间：{{ formatDate(item.eventTime || item.time, true) }} · 地点：{{ item.location || '待定' }}
      </p>
      <h1 class="detail-title">{{ item.title || '未命名活动' }}</h1>
      <p class="detail-lead">{{ item.summary || '暂无活动说明' }}</p>
      <div class="detail-body">
        <p>{{ item.content || '暂无活动详情。' }}</p>
        <div class="detail-stat">当前报名：{{ item.signupCount || 0 }} 人</div>
        <div class="event-signup-panel">
          <button
            class="platform-btn platform-btn-primary event-signup-btn"
            :disabled="signingUp || signedUp"
            @click="handleSignup"
          >
            {{ signupButtonText }}
          </button>
          <span class="event-signup-hint">报名后请留意站内消息或活动通知。</span>
        </div>
        <p v-if="signupMessage" class="event-signup-message">{{ signupMessage }}</p>
      </div>
    </article>

    <article v-else class="platform-card module-empty">
      <h3 class="platform-heading">活动不存在</h3>
      <p class="platform-text">该活动可能已下线或链接错误，请返回活动中心查看。</p>
    </article>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { fetchEventDetail, signupEvent } from '@/api/platformContent.js'

const route = useRoute()
const item = ref(null)
const signedUp = ref(false)
const signingUp = ref(false)
const signupMessage = ref('')

const signupButtonText = computed(() => {
  if (signedUp.value) return '已报名'
  return signingUp.value ? '报名中...' : '报名参加'
})

function formatDate(value, withTime = false) {
  if (!value) return ''
  const normalized = String(value).replace('T', ' ')
  return withTime ? normalized.slice(0, 16) : normalized.slice(0, 10)
}

onMounted(async () => {
  try {
    item.value = await fetchEventDetail(route.params.id)
  } catch {
    item.value = null
  }
})

async function handleSignup() {
  if (!item.value || signingUp.value || signedUp.value) return
  signingUp.value = true
  signupMessage.value = ''

  try {
    const result = await signupEvent(item.value.id)
    item.value.signupCount = result.signupCount ?? item.value.signupCount
    signedUp.value = true
    signupMessage.value = result.message || '报名成功'
  } catch (error) {
    signupMessage.value = error.message || '报名失败，请稍后再试'
  } finally {
    signingUp.value = false
  }
}
</script>

<style scoped>
.event-signup-panel {
  display: flex;
  align-items: center;
  gap: var(--lc-space-4);
  flex-wrap: wrap;
  margin-top: var(--lc-space-6);
}

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
</style>
