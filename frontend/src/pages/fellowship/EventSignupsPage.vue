<template>
  <div class="signups-page">
    <NavBar title="我的报名" />

    <div v-if="loading" class="loading-wrap">
      <van-loading size="20" />
    </div>

    <section v-else class="content">
      <article v-for="item in list" :key="item.eventId" class="event-item" @click="goDetail(item.eventId)">
        <div class="event-main">
          <p class="title">{{ item.title || '活动' }}</p>
          <p class="meta">{{ formatEventTime(item.eventTime) }} · {{ item.location || '线上/待定' }}</p>
          <p class="signup-time">报名时间：{{ formatTime(item.signupAt) }}</p>
        </div>
        <van-tag type="primary" plain>已报名</van-tag>
      </article>

      <van-empty v-if="!list.length" description="你还没有报名活动" />
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import NavBar from '@/components/NavBar.vue'
import { getMyEventSignups } from '@/api/platformContent.js'

const router = useRouter()
const loading = ref(false)
const list = ref([])

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
</style>
