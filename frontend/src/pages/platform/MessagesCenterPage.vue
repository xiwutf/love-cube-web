<template>
  <section class="platform-page messages-page">
    <div class="platform-card messages-header">
      <h1>平台消息中心</h1>
      <p>统一接收平台通知、系统公告、活动通知、内容互动，并提供联谊消息入口。</p>
    </div>

    <div class="platform-card">
      <van-tabs v-model:active="activeTab" class="messages-tabs">
        <van-tab title="全部">
          <div class="list-wrap">
            <article v-for="item in allMessages" :key="`all-${item.id}`" class="message-item">
              <div>
                <p class="message-title">{{ item.title }}</p>
                <p class="message-content">{{ item.content }}</p>
              </div>
              <small>{{ item.time }}</small>
            </article>
          </div>
        </van-tab>

        <van-tab title="系统通知">
          <div class="list-wrap">
            <article v-for="item in platformNotices" :key="`platform-${item.id}`" class="message-item">
              <div>
                <p class="message-title">{{ item.title }}</p>
                <p class="message-content">{{ item.content }}</p>
              </div>
              <small>{{ item.time }}</small>
            </article>
          </div>
        </van-tab>

        <van-tab title="活动通知">
          <div class="list-wrap">
            <article v-for="item in eventNotices" :key="`event-${item.id}`" class="message-item">
              <div>
                <p class="message-title">{{ item.title }}</p>
                <p class="message-content">{{ item.content }}</p>
              </div>
              <small>{{ item.time }}</small>
            </article>
          </div>
        </van-tab>

        <van-tab title="内容互动">
          <div class="list-wrap">
            <article v-for="item in contentInteractions" :key="`content-${item.id}`" class="message-item">
              <div>
                <p class="message-title">{{ item.title }}</p>
                <p class="message-content">{{ item.content }}</p>
              </div>
              <small>{{ item.time }}</small>
            </article>
          </div>
        </van-tab>

        <van-tab title="联谊消息">
          <div class="fellowship-entry">
            <p>联谊消息在业务模块内查看，平台消息中心仅提供入口。</p>
            <router-link class="platform-btn platform-btn-primary" to="/fellowship/messages">进入联谊消息</router-link>
          </div>
        </van-tab>

        <van-tab title="订单消息（预留）">
          <div class="fellowship-entry">
            <p>订单消息能力正在规划中，后续会在此频道统一承载。</p>
          </div>
        </van-tab>
      </van-tabs>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getNotifications } from '@/api/notification.js'

const activeTab = ref(0)
const platformNotices = ref([])

const eventNotices = ref([
  { id: 'event-1', title: '活动报名提醒', content: '你关注的活动将于今晚截止报名。', time: '今天 09:20' }
])

const contentInteractions = ref([
  { id: 'content-1', title: '内容互动提醒', content: '你发布的内容有新的点赞与评论。', time: '今天 08:10' }
])

const allMessages = computed(() => [
  ...platformNotices.value,
  ...eventNotices.value,
  ...contentInteractions.value
])

onMounted(async () => {
  const list = await getNotifications(30).catch(() => [])
  const rows = Array.isArray(list) ? list : []
  platformNotices.value = rows.map((item, index) => ({
    id: item.id || `notice-${index}`,
    title: item.title || '平台通知',
    content: item.content || '你有一条新的平台通知。',
    time: String(item.createdAt || item.publishDate || '').slice(0, 16) || '刚刚'
  }))
})
</script>

<style scoped>
.messages-page {
  display: grid;
  gap: 14px;
}

.messages-header h1 {
  margin: 0;
}

.messages-header p {
  margin: 8px 0 0;
  color: var(--lc-muted);
}

.messages-tabs {
  min-height: 380px;
}

.list-wrap {
  padding: 12px 0;
}

.message-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 2px;
  border-bottom: 1px solid var(--lc-border);
}

.message-title {
  margin: 0;
  font-weight: 700;
}

.message-content {
  margin: 6px 0 0;
  color: var(--lc-muted);
  font-size: 14px;
}

.message-item small {
  white-space: nowrap;
  color: var(--lc-muted);
  font-size: 12px;
}

.fellowship-entry {
  min-height: 220px;
  display: grid;
  place-content: center;
  gap: 12px;
  text-align: center;
}

.fellowship-entry p {
  margin: 0;
  color: var(--lc-muted);
}
</style>
