<template>
  <div class="dating-likes-page">
    <NavBar title="我的喜欢" left-arrow @click-left="router.back()" />

    <div v-if="loading" class="loading-wrap"><van-loading size="24" /></div>
    <section v-else>
      <p class="hint">你在本场活动中喜欢的人，对方不会收到通知</p>

      <div class="list">
        <article
          v-for="item in likes"
          :key="item.likeId || item.participantKey"
          class="like-item"
          @click="openCard(item)"
        >
          <div class="avatar-wrap">
            <img v-if="item.avatar" :src="item.avatar" alt="" class="avatar" />
            <span v-else class="avatar placeholder">{{ badgeInitial(item.badgeLabel) }}</span>
          </div>
          <div class="meta">
            <p class="badge">{{ item.badgeLabel }}</p>
            <p class="line">
              <span v-if="item.age">{{ item.age }} 岁</span>
              <span v-if="item.city"> · {{ item.city }}</span>
              <span v-if="item.occupation"> · {{ item.occupation }}</span>
            </p>
            <p class="time">喜欢于 {{ formatTime(item.createdAt) }}</p>
          </div>
          <van-icon name="arrow" class="arrow" />
        </article>
        <van-empty v-if="!likes.length" description="还没有喜欢记录，去花名册看看吧" />
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import NavBar from '@/components/NavBar.vue'
import { fetchDatingMyLikes } from '@/api/datingEvent.js'

const route = useRoute()
const router = useRouter()
const eventId = computed(() => route.params.eventId)

const loading = ref(true)
const likes = ref([])

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
    likes.value = await fetchDatingMyLikes(eventId.value)
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
.dating-likes-page {
  min-height: 100vh;
  background: #fff;
}

.loading-wrap {
  padding: 48px 20px;
  text-align: center;
}

.hint {
  margin: 0;
  padding: 12px 16px;
  font-size: 13px;
  color: #64748b;
  background: #fff5f8;
}

.list {
  padding: 8px 0 24px;
}

.like-item {
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
