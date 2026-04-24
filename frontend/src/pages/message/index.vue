<template>
  <div class="message-page">
    <div class="top-bar">
      <span class="top-title">消息</span>
    </div>

    <van-tabs v-model:active="activeTab" color="#FF6B8A" title-active-color="#FF6B8A" sticky offset-top="48">
      <!-- Tab1: 聊天 -->
      <van-tab title="聊天" :badge="msgStore.unreadChat || ''">
        <van-pull-refresh v-model="refreshingChat" @refresh="loadChat">
          <van-list v-model:loading="loadingChat" :finished="true" finished-text="">
            <div v-for="item in chatList" :key="item.userId" class="chat-item" @click="goChat(item)">
              <van-image round width="46" height="46" :src="item.avatar" fit="cover">
                <template #error>
                  <div class="avatar-fallback sm">{{ (item.nickname || '?')[0] }}</div>
                </template>
              </van-image>
              <div class="chat-info">
                <div class="chat-top">
                  <span class="chat-name">{{ item.nickname }}</span>
                  <span class="chat-time">{{ formatTime(item.lastTime) }}</span>
                </div>
                <div class="chat-bottom">
                  <span class="chat-last">{{ item.lastMessage }}</span>
                  <van-badge v-if="item.unread" :content="item.unread" max="99" />
                </div>
              </div>
            </div>
            <van-empty v-if="!loadingChat && !chatList.length" description="暂无聊天" image-size="80" />
          </van-list>
        </van-pull-refresh>
      </van-tab>

      <!-- Tab2: 互动 -->
      <van-tab title="互动" :badge="msgStore.unreadInteract || ''">
        <van-pull-refresh v-model="refreshingInteract" @refresh="loadInteract">
          <van-list v-model:loading="loadingInteract" :finished="true" finished-text="">
            <div v-for="item in interactList" :key="item.id" class="interact-item">
              <van-image round width="42" height="42" :src="getAvatar(item.fromUser)" fit="cover">
                <template #error>
                  <div class="avatar-fallback sm">{{ (item.fromUser?.nickname || '?')[0] }}</div>
                </template>
              </van-image>
              <div class="interact-info">
                <p class="interact-name">{{ item.fromUser?.nickname || '用户' }}
                  <span class="interact-action">{{ interactLabel(item.type) }}</span>
                </p>
                <p class="interact-time">{{ formatTime(item.createdAt) }}</p>
              </div>
            </div>
            <van-empty v-if="!loadingInteract && !interactList.length" description="暂无互动消息" image-size="80" />
          </van-list>
        </van-pull-refresh>
      </van-tab>

      <!-- Tab3: 访客 -->
      <van-tab title="访客" :badge="msgStore.unreadVisitor || ''">
        <van-pull-refresh v-model="refreshingVisitor" @refresh="loadVisitor">
          <van-list v-model:loading="loadingVisitor" :finished="true" finished-text="">
            <div v-for="item in visitorList" :key="item.id" class="chat-item"
                 @click="router.push(`/user-profile/${item.visitorId}`)">
              <van-image round width="46" height="46" :src="getAvatar(item.visitor)" fit="cover">
                <template #error>
                  <div class="avatar-fallback sm">{{ (item.visitor?.nickname || '?')[0] }}</div>
                </template>
              </van-image>
              <div class="chat-info">
                <div class="chat-top">
                  <span class="chat-name">{{ item.visitor?.nickname || '神秘访客' }}</span>
                  <span class="chat-time">{{ formatTime(item.visitTime) }}</span>
                </div>
                <p class="chat-last">来看了你的主页</p>
              </div>
            </div>
            <van-empty v-if="!loadingVisitor && !visitorList.length" description="暂无访客" image-size="80" />
          </van-list>
        </van-pull-refresh>
      </van-tab>
    </van-tabs>

    <AppTabBar />
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppTabBar from '@/components/AppTabBar.vue'
import { useMessageStore } from '@/stores/message.js'
import { getChatList, getInteractList, getVisitorList, getUnreadCount,
         markInteractRead, markVisitorRead } from '@/api/message.js'
import { formatTime } from '@/utils/format.js'
import { getAvatar } from '@/utils/image.js'
import { normalizeUser } from '@/utils/normalizeUser.js'

const route    = useRoute()
const router   = useRouter()
const msgStore = useMessageStore()

// Tab 索引映射
const TAB_MAP = { chat: 0, interact: 1, visitor: 2 }
const activeTab = ref(TAB_MAP[route.query.tab] ?? 0)

// 聊天列表
const chatList      = ref([])
const loadingChat   = ref(false)
const refreshingChat = ref(false)

async function loadChat() {
  loadingChat.value = true
  try {
    const data = await getChatList()
    chatList.value = (Array.isArray(data) ? data : []).map(item => ({
      userId:      item.partnerId ?? item.userId,
      nickname:    item.nickname  ?? item.partnerName ?? '用户',
      avatar:      getAvatar(item),
      lastMessage: item.lastMessage ?? item.content ?? '',
      lastTime:    item.lastTime    ?? item.timestamp,
      unread:      item.unreadCount ?? 0,
    }))
  } finally {
    loadingChat.value    = false
    refreshingChat.value = false
  }
}

// 互动列表
const interactList      = ref([])
const loadingInteract   = ref(false)
const refreshingInteract = ref(false)

async function loadInteract() {
  loadingInteract.value = true
  try {
    const data = await getInteractList()
    interactList.value = Array.isArray(data) ? data : []
    await markInteractRead()
    msgStore.clearInteract()
  } finally {
    loadingInteract.value    = false
    refreshingInteract.value = false
  }
}

// 访客列表
const visitorList      = ref([])
const loadingVisitor   = ref(false)
const refreshingVisitor = ref(false)

async function loadVisitor() {
  loadingVisitor.value = true
  try {
    const data = await getVisitorList()
    visitorList.value = Array.isArray(data) ? data : []
    await markVisitorRead()
    msgStore.clearVisitor()
  } finally {
    loadingVisitor.value    = false
    refreshingVisitor.value = false
  }
}

// 拉取未读数
async function fetchUnread() {
  try {
    const data = await getUnreadCount()
    msgStore.setUnread({
      chat:     data.chat     ?? data.chatUnread     ?? 0,
      interact: data.interact ?? data.interactUnread ?? 0,
      visitor:  data.visitor  ?? data.visitorUnread  ?? 0,
    })
  } catch {}
}

// 切换 tab 时按需加载
watch(activeTab, (tab) => {
  if (tab === 0 && !chatList.value.length)    loadChat()
  if (tab === 1 && !interactList.value.length) loadInteract()
  if (tab === 2 && !visitorList.value.length)  loadVisitor()
})

onMounted(async () => {
  await fetchUnread()
  loadChat()
})

function goChat(item) {
  router.push(`/chat/${item.userId}`)
}

function interactLabel(type) {
  const map = { like: '喜欢了你', follow: '关注了你', greet: '向你打了招呼', match: '和你配对成功' }
  return map[type] || '与你互动'
}
</script>

<style scoped>
.message-page { min-height: 100vh; background: #f8f8f8; padding-bottom: 60px; }

.top-bar {
  display: flex; align-items: center; height: 48px; padding: 0 16px;
  background: #fff; position: sticky; top: 0; z-index: 100;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
}
.top-title { font-size: 18px; font-weight: 700; color: #333; }

.chat-item {
  display: flex; gap: 12px; align-items: center;
  padding: 12px 16px; background: #fff; border-bottom: 1px solid #f5f5f5; cursor: pointer;
}
.chat-info  { flex: 1; overflow: hidden; }
.chat-top   { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
.chat-name  { font-size: 15px; font-weight: 500; color: #333; }
.chat-time  { font-size: 11px; color: #bbb; }
.chat-bottom { display: flex; justify-content: space-between; align-items: center; }
.chat-last  { font-size: 13px; color: #999; overflow: hidden; white-space: nowrap; text-overflow: ellipsis; max-width: 70%; }

.interact-item {
  display: flex; gap: 12px; align-items: center;
  padding: 12px 16px; background: #fff; border-bottom: 1px solid #f5f5f5;
}
.interact-info  { flex: 1; }
.interact-name  { font-size: 14px; font-weight: 500; color: #333; }
.interact-action { font-weight: 400; color: #FF6B8A; margin-left: 4px; }
.interact-time  { font-size: 11px; color: #bbb; margin-top: 4px; }

.avatar-fallback {
  border-radius: 50%; background: linear-gradient(135deg, #FF6B8A, #FFB3C1);
  display: flex; align-items: center; justify-content: center; color: #fff; font-weight: 700;
}
.avatar-fallback.sm { width: 46px; height: 46px; font-size: 16px; }
</style>
