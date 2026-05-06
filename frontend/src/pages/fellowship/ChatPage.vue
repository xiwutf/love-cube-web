<template>
  <div class="chat-page">
    <NavBar :title="partnerName">
      <template #right>
        <van-icon name="ellipsis" size="22" color="#666" @click="showChatMenu" />
      </template>
    </NavBar>

    <div v-if="wsStatus !== 'open'" class="ws-status">
      <van-loading v-if="wsStatus === 'connecting'" size="14" />
      <span>{{ statusLabel }}</span>
    </div>

    <div class="msg-list" ref="msgListRef">
      <div v-if="historyLoading" class="page-loading">
        <van-loading type="spinner" color="#ff6b8a" />
      </div>

      <div v-for="(msg, i) in allMessages" :key="msg.id || i" class="msg-block">
        <div v-if="showTimeDivider(msg, allMessages[i - 1])" class="time-divider">
          {{ formatTime(msg.timestamp) }}
        </div>
        <div class="msg-row" :class="{ self: msg.isSelf }">
          <van-image v-if="!msg.isSelf" round width="36" height="36" :src="partnerAvatar" fit="cover">
            <template #error><div class="bubble-avatar">{{ partnerName[0] }}</div></template>
          </van-image>
          <div class="bubble" :class="{ 'bubble-self': msg.isSelf, 'bubble-other': !msg.isSelf }">
            {{ msg.content }}
          </div>
          <van-image v-if="msg.isSelf" round width="36" height="36" :src="myAvatar" fit="cover">
            <template #error><div class="bubble-avatar self-avatar">{{ myName[0] }}</div></template>
          </van-image>
        </div>
      </div>
    </div>

    <transition name="send-fade">
      <div v-if="sendSuccessTip" class="send-tip">已发</div>
    </transition>

    <div class="input-bar">
      <van-field
        v-model="inputText"
        placeholder="说点什么..."
        :border="false"
        class="input-field"
        @keyup.enter="handleSend"
      />
        <van-button type="primary" size="small" :disabled="!inputText.trim()" @click="handleSend">发送</van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { showActionSheet } from '@/utils/vantActionSheet.js'
import NavBar from '@/components/NavBar.vue'
import { useWebSocket } from '@/composables/useWebSocket.js'
import { getChatHistory, markChatRead } from '@/api/chat.js'
import { getAvatar, DEFAULT_AVATAR } from '@/utils/image.js'
import { formatTime } from '@/utils/format.js'
import { storage } from '@/utils/storage.js'
import request from '@/api/request.js'
import { normalizeUser } from '@/utils/normalizeUser.js'
import { useReport } from '@/composables/useReport.js'

const route = useRoute()
const router = useRouter()
const receiverId = route.params.receiverId
const myId = storage.get('userId')

const partnerName = ref('聊天')
const partnerAvatar = ref(DEFAULT_AVATAR)
const myName = ref('ME')
const myAvatar = ref(DEFAULT_AVATAR)

const historyLoading = ref(true)
const historyMessages = ref([])
const sendSuccessTip = ref(false)
let sendTipTimer = null

const { messages: wsMessages, errors: wsErrors, status: wsStatus, connect, send } = useWebSocket(myId)
const { openReport } = useReport()
const inputText = ref('')
const msgListRef = ref(null)

const allMessages = computed(() => {
  const all = [...historyMessages.value, ...wsMessages.value]
  const seen = new Set()
  return all
    .filter((item) => {
      const key = item.id || `${item.senderId}-${item.receiverId}-${item.timestamp}`
      if (seen.has(key)) return false
      seen.add(key)
      return true
    })
    .sort((a, b) => a.timestamp - b.timestamp)
})

const statusLabel = computed(() => {
  const map = {
    connecting: '连接中...',
    closed: '连接已断开',
    error: '连接异常，正在重试...'
  }
  return map[wsStatus.value] || ''
})

onMounted(async () => {
  if (!myId) {
    router.replace({ path: '/fellowship/login', query: { redirect: encodeURIComponent(route.fullPath) } })
    return
  }
  try {
    const [hist, partner, me] = await Promise.allSettled([
      getChatHistory(myId, receiverId),
      request.get(`/users/${receiverId}`),
      request.get('/users/me')
    ])
    if (hist.status === 'fulfilled') {
      const msgs = Array.isArray(hist.value) ? hist.value : (hist.value?.messages || [])
      historyMessages.value = msgs.map((item) => ({
        id: item.id,
        senderId: item.senderId ?? item.sender_id,
        receiverId: item.receiverId ?? item.receiver_id,
        content: item.content || '',
        timestamp: item.timestamp ?? new Date(item.createdAt).getTime(),
        isSelf: String(item.senderId ?? item.sender_id) === String(myId)
      }))
    }
    if (partner.status === 'fulfilled') {
      const raw = partner.value
      const user = normalizeUser(raw)
      partnerName.value = user.nickname
      partnerAvatar.value = getAvatar(raw)
    }
    if (me.status === 'fulfilled') {
      const rawMe = me.value
      const self = normalizeUser(rawMe)
      myName.value = self.nickname || myName.value
      myAvatar.value = getAvatar(rawMe)
    }
    await markChatRead(myId, receiverId).catch(() => {})
  } finally {
    historyLoading.value = false
    scrollToBottom()
  }
  connect()
})

watch(allMessages, () => nextTick(scrollToBottom))

watch(
  wsErrors,
  (errs) => {
    const last = errs[errs.length - 1]
    if (!last) return
    if (last.code === 'BLOCKED') {
      showToast({ message: '无法发消息：双方关系受限', type: 'fail', duration: 3000 })
    } else {
      showToast({ message: last.message || '发送失败，请稍后重试', type: 'fail' })
    }
  },
  { deep: true }
)

function handleSend() {
  const text = inputText.value.trim()
  if (!text) return
  send(text, receiverId)
  inputText.value = ''
  sendSuccessTip.value = true
  clearTimeout(sendTipTimer)
  sendTipTimer = setTimeout(() => {
    sendSuccessTip.value = false
  }, 900)
  nextTick(scrollToBottom)
}

onBeforeUnmount(() => {
  if (sendTipTimer) {
    clearTimeout(sendTipTimer)
    sendTipTimer = null
  }
})

function scrollToBottom() {
  const node = msgListRef.value
  if (node) node.scrollTop = node.scrollHeight
}

async function showChatMenu() {
  try {
    const action = await showActionSheet({
      actions: [{ name: '举报该用户', color: '#ee0a24' }]
    })
    if (action.name === '举报该用户') {
      await openReport({ targetType: 'USER', targetUserId: receiverId })
    }
  } catch {
    // dismissed
  }
}

function showTimeDivider(curr, prev) {
  if (!prev) return true
  return curr.timestamp - prev.timestamp > 5 * 60 * 1000
}
</script>

<style scoped>
.chat-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f0f0f0;
  position: relative;
}

.ws-status {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 6px;
  font-size: 12px;
  color: #64748b;
  background: #fff8f8;
}

.msg-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px 12px 8px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.msg-block {
  display: contents;
}

.page-loading {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.time-divider {
  text-align: center;
  font-size: 11px;
  color: #94a3b8;
  padding: 4px 12px;
  background: rgba(0, 0, 0, 0.04);
  border-radius: 10px;
  align-self: center;
}

.msg-row {
  display: flex;
  align-items: flex-end;
  gap: 8px;
}

.msg-row.self {
  flex-direction: row-reverse;
}

.bubble {
  max-width: 68%;
  padding: 10px 14px;
  border-radius: 18px;
  font-size: 15px;
  line-height: 1.5;
  overflow-wrap: break-word;
}

.bubble-other {
  background: #fff;
  color: #333;
  border-bottom-left-radius: 4px;
}

.bubble-self {
  background: #ff6b8a;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.bubble-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  flex-shrink: 0;
  background: linear-gradient(135deg, #ff6b8a, #ffb3c1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #fff;
  font-weight: 700;
}

.self-avatar {
  background: linear-gradient(135deg, #ffb3c1, #ff6b8a);
}

.send-tip {
  position: absolute;
  left: 50%;
  bottom: 62px;
  transform: translateX(-50%);
  background: rgba(17, 24, 39, 0.78);
  color: #fff;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
}

.send-fade-enter-active,
.send-fade-leave-active {
  transition: opacity 0.2s ease;
}

.send-fade-enter-from,
.send-fade-leave-to {
  opacity: 0;
}

.input-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #fff;
  box-shadow: 0 -1px 6px rgba(0, 0, 0, 0.06);
  padding-bottom: max(8px, env(safe-area-inset-bottom));
}

.input-field {
  flex: 1;
  background: #f5f5f5;
  border-radius: 20px;
  padding: 0 12px;
}
</style>

