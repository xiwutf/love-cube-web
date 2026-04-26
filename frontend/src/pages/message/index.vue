<template>
  <div class="message-page">
    <!-- Top bar -->
    <header class="msg-header">
      <span class="msg-header-title">消息</span>
      <van-badge v-if="msgStore.totalUnread" :content="msgStore.totalUnread" max="99" class="msg-header-badge" />
    </header>

    <van-tabs
      v-model:active="activeTab"
      color="#FF5F84"
      title-active-color="#FF5F84"
      title-inactive-color="#8898aa"
      sticky
      offset-top="52"
      :border="false"
      class="msg-tabs"
    >
      <!-- 聊天 -->
      <van-tab title="聊天" :badge="msgStore.unreadChat || ''">
        <van-pull-refresh v-model="refreshingChat" @refresh="loadChat">
          <div class="tab-content">
            <van-list v-model:loading="loadingChat" :finished="true" finished-text="">
              <van-swipe-cell v-for="item in chatList" :key="item.userId" class="chat-cell">
                <div class="chat-item" @click="goChat(item)">
                  <div class="chat-avatar-wrap">
                    <van-image round width="50" height="50" :src="item.avatar" fit="cover">
                      <template #error>
                        <div class="avatar-fb">{{ (item.nickname || '?')[0] }}</div>
                      </template>
                    </van-image>
                    <span v-if="item.unread" class="unread-dot">{{ item.unread > 99 ? '99+' : item.unread }}</span>
                  </div>
                  <div class="chat-info">
                    <div class="chat-row">
                      <span class="chat-name">{{ item.nickname }}</span>
                      <span class="chat-time">{{ formatTime(item.lastTime) }}</span>
                    </div>
                    <p class="chat-last">{{ item.lastMessage || '暂无消息' }}</p>
                  </div>
                </div>
                <template #right>
                  <van-button square type="danger" text="删除" style="height: 100%" @click="handleDeleteChat(item)" />
                </template>
              </van-swipe-cell>
              <van-empty v-if="!loadingChat && !chatList.length" description="暂无聊天" image-size="70" />
            </van-list>
          </div>
        </van-pull-refresh>
      </van-tab>

      <!-- 互动 -->
      <van-tab title="互动" :badge="msgStore.unreadInteract || ''">
        <van-pull-refresh v-model="refreshingInteract" @refresh="loadInteract">
          <div class="tab-content">
            <van-list v-model:loading="loadingInteract" :finished="true" finished-text="">
              <div v-for="item in interactList" :key="item.id" class="interact-item">
                <van-image round width="46" height="46" :src="getAvatar(item.fromUser)" fit="cover">
                  <template #error>
                    <div class="avatar-fb size46">{{ (item.fromUser?.nickname || '?')[0] }}</div>
                  </template>
                </van-image>
                <div class="interact-info">
                  <p class="interact-name">
                    {{ item.fromUser?.nickname || '用户' }}
                    <span class="interact-action">{{ interactLabel(item.type) }}</span>
                  </p>
                  <p class="interact-time">{{ formatTime(item.createdAt) }}</p>
                </div>
                <div class="interact-type-icon">{{ interactIcon(item.type) }}</div>
              </div>
              <van-empty v-if="!loadingInteract && !interactList.length" description="暂无互动消息" image-size="70" />
            </van-list>
          </div>
        </van-pull-refresh>
      </van-tab>

      <!-- 访客 -->
      <van-tab title="访客" :badge="msgStore.unreadVisitor || ''">
        <van-pull-refresh v-model="refreshingVisitor" @refresh="loadVisitor">
          <div class="tab-content">
            <van-list v-model:loading="loadingVisitor" :finished="true" finished-text="">
              <div
                v-for="item in visitorList"
                :key="item.id"
                class="chat-item visitor-item"
                @click="router.push(`/fellowship/user-profile/${item.visitorId}`)"
              >
                <div class="chat-avatar-wrap">
                  <van-image round width="50" height="50" :src="getAvatar(item.visitor)" fit="cover">
                    <template #error>
                      <div class="avatar-fb">{{ (item.visitor?.nickname || '?')[0] }}</div>
                    </template>
                  </van-image>
                </div>
                <div class="chat-info">
                  <div class="chat-row">
                    <span class="chat-name">{{ item.visitor?.nickname || '神秘访客' }}</span>
                    <span class="chat-time">{{ formatTime(item.visitTime) }}</span>
                  </div>
                  <p class="chat-last visitor-label">👀 来看了你的主页</p>
                </div>
              </div>
              <van-empty v-if="!loadingVisitor && !visitorList.length" description="暂无访客" image-size="70" />
            </van-list>
          </div>
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
import { deleteChat } from '@/api/chat.js'
import { useUserStore } from '@/stores/user.js'
import { showConfirmDialog, showToast } from 'vant'
import { formatTime } from '@/utils/format.js'
import { getAvatar } from '@/utils/image.js'
import { normalizeUser } from '@/utils/normalizeUser.js'

const route     = useRoute()
const router    = useRouter()
const msgStore  = useMessageStore()
const userStore = useUserStore()

const TAB_MAP = { chat: 0, interact: 1, visitor: 2 }
const activeTab = ref(TAB_MAP[route.query.tab] ?? 0)

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
  router.push(`/fellowship/chat/${item.userId}`)
}

async function handleDeleteChat(item) {
  try {
    await showConfirmDialog({ title: '删除聊天', message: `确认删除与 ${item.nickname} 的全部聊天记录？` })
  } catch {
    return
  }
  try {
    await deleteChat(userStore.userId, item.userId)
    chatList.value = chatList.value.filter(c => c.userId !== item.userId)
    showToast({ type: 'success', message: '已删除' })
  } catch {
    showToast({ type: 'fail', message: '删除失败' })
  }
}

function interactLabel(type) {
  const map = { like: '喜欢了你', follow: '关注了你', greet: '向你打了招呼', match: '和你配对成功' }
  return map[type] || '与你互动'
}

function interactIcon(type) {
  const map = { like: '❤️', follow: '⭐', greet: '👋', match: '🎉' }
  return map[type] || '💬'
}
</script>

<style scoped>
/* ── Page ── */
.message-page {
  min-height: 100vh;
  background: #f4f6fb;
  padding-bottom: 72px;
}

/* ── Header ── */
.msg-header {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 52px;
  padding: 0 16px;
  background: #fff;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 1px 0 #f0f2f8;
}
.msg-header-title {
  font-size: 18px;
  font-weight: 800;
  color: #1a2236;
  letter-spacing: -0.01em;
}
.msg-header-badge {
  flex-shrink: 0;
}

/* ── Tabs ── */
.msg-tabs :deep(.van-tabs__wrap) {
  background: #fff;
  box-shadow: 0 1px 0 #f0f2f8;
}
.msg-tabs :deep(.van-tabs__nav) {
  background: transparent;
  padding: 0 4px;
}
.msg-tabs :deep(.van-tab) {
  font-size: 14px;
  font-weight: 600;
}
.msg-tabs :deep(.van-tabs__line) {
  height: 3px;
  border-radius: 2px;
  width: 24px !important;
}

/* ── Tab content ── */
.tab-content {
  background: #fff;
  min-height: 200px;
}

/* ── Chat items ── */
.chat-cell :deep(.van-swipe-cell__wrapper) {
  background: #fff;
}

.chat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #fff;
  cursor: pointer;
  border-bottom: 1px solid #f4f6fb;
  transition: background 0.1s;
}
.chat-item:active {
  background: #fafbfc;
}

.chat-avatar-wrap {
  position: relative;
  flex-shrink: 0;
}
.unread-dot {
  position: absolute;
  top: -3px;
  right: -3px;
  min-width: 18px;
  height: 18px;
  border-radius: 9px;
  background: #FF5F84;
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
  border: 2px solid #fff;
}

.chat-info {
  flex: 1;
  min-width: 0;
}
.chat-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
}
.chat-name {
  font-size: 15px;
  font-weight: 600;
  color: #1a2236;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 60%;
}
.chat-time {
  font-size: 11px;
  color: #c0cad8;
  flex-shrink: 0;
}
.chat-last {
  font-size: 13px;
  color: #8898aa;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.visitor-label {
  color: #a0abbe;
}

/* ── Interact items ── */
.interact-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #f4f6fb;
}
.interact-info {
  flex: 1;
  min-width: 0;
}
.interact-name {
  font-size: 14px;
  font-weight: 600;
  color: #1a2236;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.interact-action {
  font-weight: 400;
  color: #FF5F84;
  margin-left: 4px;
}
.interact-time {
  font-size: 12px;
  color: #c0cad8;
  margin-top: 4px;
}
.interact-type-icon {
  font-size: 22px;
  flex-shrink: 0;
}

/* ── Avatar fallback ── */
.avatar-fb {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF5F84, #FFB3C4);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: 700;
}
.avatar-fb.size46 {
  width: 46px;
  height: 46px;
  font-size: 16px;
}
</style>
