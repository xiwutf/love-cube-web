<template>
  <div class="chat-page">
    <NavBar :title="partnerName">
      <template #right>
        <van-icon name="ellipsis" size="22" color="#666" @click="showChatMenu" />
      </template>
    </NavBar>

    <!-- 连接状态提示 -->
    <div v-if="wsStatus !== 'open'" class="ws-status">
      <van-loading v-if="wsStatus === 'connecting'" size="14" />
      <span>{{ statusLabel }}</span>
    </div>

    <!-- 消息列表 -->
    <div class="msg-list" ref="msgListRef">
      <div v-if="historyLoading" class="page-loading">
        <van-loading type="spinner" color="#FF6B8A" />
      </div>

      <template v-for="(msg, i) in allMessages" :key="msg.id ?? i">
        <!-- 时间分割线 -->
        <div v-if="showTimeDivider(msg, allMessages[i - 1])" class="time-divider">
          {{ formatTime(msg.timestamp) }}
        </div>
        <!-- 消息气泡 -->
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
      </template>
    </div>

    <!-- 输入区 -->
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
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
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

const route       = useRoute()
const receiverId  = route.params.receiverId
const myId        = storage.get('userId')

// 对方信息
const partnerName   = ref('聊天')
const partnerAvatar = ref(DEFAULT_AVATAR)
const myName        = ref('我')
const myAvatar      = ref(DEFAULT_AVATAR)

// 历史消息
const historyLoading = ref(true)
const historyMessages = ref([])

// WebSocket
const { messages: wsMessages, errors: wsErrors, status: wsStatus, connect, send } = useWebSocket(myId)
const { openReport } = useReport()
const inputText  = ref('')
const msgListRef = ref(null)

// 合并历史 + 实时消息
const allMessages = computed(() => {
  const all = [...historyMessages.value, ...wsMessages.value]
  // 去重（历史消息和 WS 可能重叠）
  const seen = new Set()
  return all.filter(m => {
    const key = m.id ?? `${m.senderId}-${m.timestamp}`
    if (seen.has(key)) return false
    seen.add(key)
    return true
  }).sort((a, b) => a.timestamp - b.timestamp)
})

const statusLabel = computed(() => {
  const map = { connecting: '连接中...', closed: '已断开', error: '连接失败，正在重试' }
  return map[wsStatus.value] ?? ''
})

// 加载历史消息和对方信息
onMounted(async () => {
  try {
    const [hist, partner] = await Promise.allSettled([
      getChatHistory(myId, receiverId),
      request.get(`/users/${receiverId}`)
    ])
    if (hist.status === 'fulfilled') {
      const msgs = Array.isArray(hist.value) ? hist.value : (hist.value?.messages ?? [])
      historyMessages.value = msgs.map(m => ({
        id:         m.id,
        senderId:   m.senderId  ?? m.sender_id,
        receiverId: m.receiverId ?? m.receiver_id,
        content:    m.content   ?? '',
        timestamp:  m.timestamp ?? new Date(m.createdAt).getTime(),
        isSelf:     String(m.senderId ?? m.sender_id) === String(myId),
      }))
    }
    if (partner.status === 'fulfilled') {
      const u = normalizeUser(partner.value)
      partnerName.value   = u.nickname
      partnerAvatar.value = u.avatar
    }
    await markChatRead(myId, receiverId)
  } finally {
    historyLoading.value = false
    scrollToBottom()
  }
  connect()
})

// 新消息到来自动滚动
watch(allMessages, () => nextTick(scrollToBottom))

// 服务端错误（如被拉黑）
watch(wsErrors, (errs) => {
  const last = errs[errs.length - 1]
  if (!last) return
  if (last.code === 'BLOCKED') {
    showToast({ message: '无法发送消息：双方关系受限', type: 'fail', duration: 3000 })
  } else {
    showToast({ message: last.message || '发送失败', type: 'fail' })
  }
}, { deep: true })

function handleSend() {
  const text = inputText.value.trim()
  if (!text) return
  send(text, receiverId)
  inputText.value = ''
  nextTick(scrollToBottom)
}

function scrollToBottom() {
  const el = msgListRef.value
  if (el) el.scrollTop = el.scrollHeight
}

async function showChatMenu() {
  try {
    const action = await showActionSheet({
      actions: [{ name: '举报该用户', color: '#ee0a24' }],
    })
    if (action.name === '举报该用户') {
      await openReport({ targetType: 'USER', targetUserId: receiverId })
    }
  } catch {
    // dismissed
  }
}

// 相邻消息时间差 > 5 分钟则显示时间分割线
function showTimeDivider(curr, prev) {
  if (!prev) return true
  return curr.timestamp - prev.timestamp > 5 * 60 * 1000
}
</script>

<style scoped>
.chat-page { display: flex; flex-direction: column; height: 100vh; background: #f0f0f0; }

.ws-status {
  display: flex; align-items: center; justify-content: center; gap: 6px;
  padding: 6px; font-size: 12px; color: #999; background: #fff8f8;
}

.msg-list {
  flex: 1; overflow-y: auto; padding: 12px 12px 8px;
  display: flex; flex-direction: column; gap: 12px;
}

.page-loading { display: flex; justify-content: center; padding: 40px 0; }

.time-divider {
  text-align: center; font-size: 11px; color: #bbb;
  padding: 4px 12px; background: rgba(0,0,0,.04); border-radius: 10px;
  align-self: center;
}

.msg-row { display: flex; align-items: flex-end; gap: 8px; }
.msg-row.self { flex-direction: row-reverse; }

.bubble {
  max-width: 68%; padding: 10px 14px; border-radius: 18px;
  font-size: 15px; line-height: 1.5; word-break: break-word;
}
.bubble-other { background: #fff; color: #333; border-bottom-left-radius: 4px; }
.bubble-self  { background: #FF6B8A; color: #fff; border-bottom-right-radius: 4px; }

.bubble-avatar {
  width: 36px; height: 36px; border-radius: 50%; flex-shrink: 0;
  background: linear-gradient(135deg, #FF6B8A, #FFB3C1);
  display: flex; align-items: center; justify-content: center;
  font-size: 14px; color: #fff; font-weight: 700;
}
.self-avatar { background: linear-gradient(135deg, #FFB3C1, #FF6B8A); }

.input-bar {
  display: flex; align-items: center; gap: 8px; padding: 8px 12px;
  background: #fff; box-shadow: 0 -1px 6px rgba(0,0,0,.06);
  /* 避免被软键盘遮挡 */
  padding-bottom: max(8px, env(safe-area-inset-bottom));
}
.input-field { flex: 1; background: #f5f5f5; border-radius: 20px; padding: 0 12px; }
</style>
