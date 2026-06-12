<template>
  <div class="dating-matches-page">
    <NavBar title="互选成功" left-arrow @click-left="router.back()" />

    <div v-if="loading" class="loading-wrap"><van-loading size="24" /></div>
    <section v-else-if="!eventEnded" class="state-box">
      <van-icon name="clock-o" size="48" color="#94a3b8" />
      <p class="state-title">活动结束后可查看互选结果</p>
      <p class="state-desc">{{ pendingMessage }}</p>
    </section>
    <section v-else>
      <p class="hint">双方互相喜欢后，活动结束将在此揭晓</p>
      <div class="list">
        <article
          v-for="item in matches"
          :key="item.matchId || item.participantKey"
          class="match-item"
          @click="openCard(item)"
        >
          <div class="avatar-wrap">
            <img v-if="item.avatar" :src="item.avatar" alt="" class="avatar" />
            <span v-else class="avatar placeholder">{{ badgeInitial(item.badgeLabel) }}</span>
          </div>
          <div class="meta">
            <p class="badge">
              <van-icon name="like" color="#ff5f84" />
              {{ item.badgeLabel }}
            </p>
            <p class="line">
              <span v-if="item.age">{{ item.age }} 岁</span>
              <span v-if="item.city"> · {{ item.city }}</span>
              <span v-if="item.occupation"> · {{ item.occupation }}</span>
            </p>
            <p class="time">互选于 {{ formatTime(item.matchedAt) }}</p>
          </div>
          <van-icon name="arrow" class="arrow" />
        </article>
        <van-empty
          v-if="!matches.length"
          description="暂无互选成功，可以继续关注后续活动"
        />
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import NavBar from '@/components/NavBar.vue'
import { fetchDatingMutualMatches } from '@/api/datingEvent.js'

const route = useRoute()
const router = useRouter()
const eventId = computed(() => route.params.eventId)

const loading = ref(true)
const eventEnded = ref(false)
const pendingMessage = ref('活动结束后可查看互选结果')
const matches = ref([])

function badgeInitial(label) {
  const text = String(label || '').trim()
  return text ? text.slice(-1) : '—'
}

function formatTime(value) {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  return `${month}月${day}日 ${hour}:${minute}`
}

async function load() {
  loading.value = true
  try {
    const res = await fetchDatingMutualMatches(eventId.value)
    if (res && res.eventEnded === false) {
      eventEnded.value = false
      pendingMessage.value = res.message || '活动结束后可查看互选结果'
      matches.value = []
      return
    }
    eventEnded.value = true
    matches.value = Array.isArray(res) ? res : []
  } catch (e) {
    showToast(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function openCard(item) {
  if (!item.participantKey) return
  router.push(`/fellowship/events/${eventId.value}/dating/card/${encodeURIComponent(item.participantKey)}`)
}

onMounted(load)
</script>

<style scoped>
.dating-matches-page {
  min-height: 100vh;
  background: #fff;
}

.loading-wrap,
.state-box {
  padding: 48px 20px;
  text-align: center;
  color: #64748b;
}

.state-title {
  margin: 16px 0 8px;
  font-size: 16px;
  font-weight: 700;
  color: #334155;
}

.state-desc {
  margin: 0;
  font-size: 14px;
  color: #94a3b8;
}

.hint {
  margin: 0;
  padding: 12px 16px;
  font-size: 13px;
  color: #64748b;
  background: #f0fdf4;
}

.list {
  padding: 8px 0 24px;
}

.match-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-bottom: 1px solid #f1f5f9;
}

.avatar-wrap {
  flex-shrink: 0;
}

.avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar.placeholder {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #ffe4ec;
  color: #ff5f84;
  font-weight: 700;
  font-size: 16px;
}

.meta {
  flex: 1;
  min-width: 0;
}

.badge {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 16px;
  font-weight: 700;
  color: #334155;
}

.line {
  margin: 4px 0 0;
  font-size: 13px;
  color: #64748b;
}

.time {
  margin: 4px 0 0;
  font-size: 12px;
  color: #94a3b8;
}

.arrow {
  color: #cbd5e1;
}
</style>
