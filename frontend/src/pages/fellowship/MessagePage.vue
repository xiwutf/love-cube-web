<template>
  <div class="message-page">
    <header class="msg-header">
      <span class="msg-header-title">消息中心</span>
      <van-badge v-if="msgStore.totalUnread" :content="msgStore.totalUnread" max="99" class="msg-header-badge" />
    </header>

    <van-tabs
      v-model:active="activeTab"
      color="#ff5f84"
      title-active-color="#ff5f84"
      title-inactive-color="#8898aa"
      sticky
      offset-top="0"
      :border="false"
      class="msg-tabs"
    >
      <van-tab title="聊天" :badge="msgStore.unreadChat || ''">
        <ChatTab
          :list="chatList"
          :loading="loadingChat"
          :refreshing="refreshingChat"
          :format-time="formatTime"
          @refresh="loadChat"
          @chat="goChat"
          @delete-chat="handleDeleteChat"
          @discover="router.push('/fellowship/discover')"
          @update:loading="loadingChat = $event"
          @update:refreshing="refreshingChat = $event"
        />
      </van-tab>

      <van-tab title="互动" :badge="msgStore.unreadInteract || ''">
        <InteractTab
          :list="interactList"
          :loading="loadingInteract"
          :refreshing="refreshingInteract"
          :format-time="formatTime"
          :get-avatar="getAvatar"
          :from-user="interactFromUser"
          :initial="interactInitial"
          :name="getInteractName"
          :label="interactLabel"
          :preview="interactPreview"
          :icon="interactIcon"
          @refresh="loadInteract"
          @profile="goInteractProfile"
          @update:loading="loadingInteract = $event"
          @update:refreshing="refreshingInteract = $event"
        />
      </van-tab>

      <van-tab title="访客" :badge="msgStore.unreadVisitor || ''">
        <VisitorTab
          :list="visitorList"
          :loading="loadingVisitor"
          :refreshing="refreshingVisitor"
          :format-time="formatTime"
          :get-avatar="getAvatar"
          :get-visitor-name="getVisitorName"
          @refresh="loadVisitor"
          @profile="goVisitorProfile"
          @update:loading="loadingVisitor = $event"
          @update:refreshing="refreshingVisitor = $event"
        />
      </van-tab>

      <van-tab title="通知" :badge="msgStore.unreadNotification || ''">
        <NotificationTab
          :list="notifList"
          :loading="loadingNotif"
          :refreshing="refreshingNotif"
          :format-time="formatTime"
          :icon="notifIcon"
          @refresh="loadNotifications"
          @open="handleNotifClick"
          @read-all="readAll"
          @update:refreshing="refreshingNotif = $event"
        />
      </van-tab>
    </van-tabs>

    <AppTabBar />
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import AppTabBar from '@/components/AppTabBar.vue'
import ChatTab from '@/components/fellowship/messages/ChatTab.vue'
import InteractTab from '@/components/fellowship/messages/InteractTab.vue'
import VisitorTab from '@/components/fellowship/messages/VisitorTab.vue'
import NotificationTab from '@/components/fellowship/messages/NotificationTab.vue'
import { useMessageStore } from '@/stores/message.js'
import { getChatList, getInteractList, getVisitorList, getUnreadCount, markInteractRead, markVisitorRead } from '@/api/message.js'
import { getNotifications, getNotifUnreadCountCached, markNotifRead, markAllNotifRead } from '@/api/notification.js'
import { deleteChat } from '@/api/chat.js'
import { useUserStore } from '@/stores/user.js'
import { formatTime } from '@/utils/format.js'
import { getAvatar } from '@/utils/image.js'

const route = useRoute()
const router = useRouter()
const msgStore = useMessageStore()
const userStore = useUserStore()

const TAB_MAP = { chat: 0, interact: 1, visitor: 2, notification: 3 }
const TAB_QUERY_MAP = ['chat', 'interact', 'visitor', 'notification']
const activeTab = ref(TAB_MAP[route.query.tab] ?? 0)

const chatList = ref([])
const loadingChat = ref(false)
const refreshingChat = ref(false)

const interactList = ref([])
const loadingInteract = ref(false)
const refreshingInteract = ref(false)

const visitorList = ref([])
const loadingVisitor = ref(false)
const refreshingVisitor = ref(false)

const notifList = ref([])
const loadingNotif = ref(false)
const refreshingNotif = ref(false)

function normalizeListPayload(payload) {
  if (Array.isArray(payload)) return payload
  if (!payload || typeof payload !== 'object') return []
  const candidates = [payload.records, payload.list, payload.items, payload.content, payload.data]
  for (const candidate of candidates) {
    if (Array.isArray(candidate)) return candidate
  }
  return []
}

async function loadChat() {
  loadingChat.value = true
  try {
    const data = normalizeListPayload(await getChatList())
    chatList.value = data.map((item) => ({
      userId: item.partnerId || item.userId,
      nickname: item.nickname || item.partnerName || '用户',
      avatar: getAvatar(item),
      lastMessage: item.lastMessage || item.content || '',
      lastTime: item.lastTime || item.timestamp || item.updatedAt || item.createdAt,
      unread: Number(item.unreadCount || item.unread || 0)
    }))
    msgStore.clearChat()
  } catch {
    showToast({ message: '聊天列表加载失败', type: 'fail' })
  } finally {
    loadingChat.value = false
    refreshingChat.value = false
  }
}

async function loadInteract() {
  loadingInteract.value = true
  try {
    const data = normalizeListPayload(await getInteractList())
    interactList.value = data.map((item) => {
      const uid = item.fromUser?.userId ?? item.userId ?? null
      const mergedFrom = {
        userId: uid,
        nickname: item.fromUser?.nickname || item.nickname || '',
        avatar: item.fromUser?.avatar || item.avatar || ''
      }
      return {
        ...item,
        fromUser: Object.keys(item.fromUser || {}).length ? { ...mergedFrom, ...item.fromUser } : mergedFrom,
        createdAt: item.createdAt || item.time || null
      }
    })
    await markInteractRead()
    msgStore.clearInteract()
  } catch {
    showToast({ message: '互动消息加载失败', type: 'fail' })
  } finally {
    loadingInteract.value = false
    refreshingInteract.value = false
  }
}

async function loadVisitor() {
  loadingVisitor.value = true
  try {
    const data = normalizeListPayload(await getVisitorList())
    visitorList.value = data.map((item) => ({
      ...item,
      visitorId: item.visitorId || item.userId || item.visitor?.userId || null,
      visitor: item.visitor || {
        nickname: item.nickname || '神秘访客',
        avatar: item.avatar || '',
        userId: item.userId || item.visitorId || null
      }
    }))
    await markVisitorRead()
    msgStore.clearVisitor()
  } catch {
    showToast({ message: '访客记录加载失败', type: 'fail' })
  } finally {
    loadingVisitor.value = false
    refreshingVisitor.value = false
  }
}

async function loadNotifications() {
  loadingNotif.value = true
  try {
    const data = normalizeListPayload(await getNotifications(10))
    notifList.value = data
    const unreadCount = notifList.value.filter((item) => !item.isRead).length
    msgStore.setUnreadNotification(unreadCount)
  } catch {
    showToast({ message: '通知加载失败', type: 'fail' })
  } finally {
    loadingNotif.value = false
    refreshingNotif.value = false
  }
}

async function handleNotifClick(item) {
  if (!item.isRead) {
    item.isRead = true
    markNotifRead(item.id).catch(() => {})
    msgStore.setUnreadNotification(Math.max(0, msgStore.unreadNotification - 1))
  }
  if (item.targetType === 'USER' && item.targetId) router.push(`/fellowship/user-profile/${item.targetId}`)
  else if (item.targetType === 'CHAT' && item.targetId) router.push(`/fellowship/chat/${item.targetId}`)
}

async function readAll() {
  try {
    await markAllNotifRead()
    notifList.value.forEach((item) => { item.isRead = true })
    msgStore.clearNotification()
    showToast({ message: '通知已全部设为已读', type: 'success' })
  } catch {
    showToast({ message: '操作失败，请稍后重试', type: 'fail' })
  }
}

async function fetchUnread() {
  const [msgData, notifData] = await Promise.allSettled([getUnreadCount(), getNotifUnreadCountCached()])
  const msg = msgData.status === 'fulfilled' ? msgData.value : {}
  const notifCount = notifData.status === 'fulfilled' ? Number(notifData.value?.count || 0) : 0
  msgStore.setUnread({
    chat: Number(msg.chatUnread || msg.chat || 0),
    interact: Number(msg.interactUnread || msg.interact || 0),
    visitor: Number(msg.visitorUnread || msg.visitor || 0),
    notification: notifCount
  })
}

function syncTabQuery(tab) {
  const tabKey = TAB_QUERY_MAP[tab] || 'chat'
  if (route.query.tab === tabKey) return
  router.replace({ path: route.path, query: { ...route.query, tab: tabKey } })
}

watch(activeTab, (tab) => {
  syncTabQuery(tab)
  if (tab === 0 && !chatList.value.length) loadChat()
  if (tab === 1 && !interactList.value.length) loadInteract()
  if (tab === 2 && !visitorList.value.length) loadVisitor()
  if (tab === 3 && !notifList.value.length) loadNotifications()
})

watch(() => route.query.tab, (tab) => {
  const nextTab = TAB_MAP[tab] ?? 0
  if (activeTab.value !== nextTab) activeTab.value = nextTab
})

onMounted(async () => {
  await fetchUnread()
  if (activeTab.value === 0) loadChat()
  if (activeTab.value === 1) loadInteract()
  if (activeTab.value === 2) loadVisitor()
  if (activeTab.value === 3) loadNotifications()
})

function goChat(item) {
  if (!item?.userId) {
    showToast('该聊天对象不存在')
    return
  }
  router.push(`/fellowship/chat/${item.userId}`)
}

function interactFromUser(item) {
  return item?.fromUser || { nickname: item?.nickname, avatar: item?.avatar, userId: item?.userId }
}

function interactUserId(item) {
  const u = interactFromUser(item)
  return u?.userId ?? item?.userId ?? null
}

function interactInitial(item) {
  const n = getInteractName(item)
  return (n && n !== '用户' ? n : '?')[0]
}

function goInteractProfile(item) {
  const id = interactUserId(item)
  if (!id) {
    showToast('暂时无法查看该用户')
    return
  }
  router.push(`/fellowship/user-profile/${id}`)
}

function goVisitorProfile(item) {
  if (!item?.visitorId) {
    showToast('暂时无法查看该访客')
    return
  }
  router.push(`/fellowship/user-profile/${item.visitorId}`)
}

function interactPreview(item) {
  const text = (item.content || item.message || '').trim()
  if (!text) return ''
  return text.length > 36 ? `${text.slice(0, 36)}...` : text
}

function getInteractName(item) {
  const n = (item?.fromUser?.nickname || item?.nickname || '').trim()
  return n || '用户'
}

function getVisitorName(item) {
  return item?.visitor?.nickname || item?.nickname || '神秘访客'
}

async function handleDeleteChat(item) {
  try {
    await showConfirmDialog({ title: '删除聊天', message: `确认删除与 ${item.nickname} 的全部聊天记录吗？` })
  } catch {
    return
  }
  try {
    await deleteChat(userStore.userId, item.userId)
    chatList.value = chatList.value.filter((chat) => chat.userId !== item.userId)
    showToast({ type: 'success', message: '已删除' })
  } catch {
    showToast({ type: 'fail', message: '删除失败，请稍后重试' })
  }
}

function normalizeType(type) {
  return typeof type === 'string' ? type.toLowerCase() : ''
}

function notifIcon(type) {
  const map = { LIKE: '❤', MESSAGE: '💬', SYSTEM: '📢', REPORT_HANDLED: '🛡', BANNED: '⛔' }
  return map[type] || '🔔'
}

function interactLabel(type) {
  const map = {
    like: '喜欢了你', super_like: '超级喜欢了你', follow: '关注了你', greet: '向你打了招呼',
    match: '和你匹配成功', gift: '给你送了礼物', comment: '给你留了言', skip: '在推荐中跳过了你'
  }
  const key = normalizeType(type)
  return map[key] || (key ? `互动：${key}` : '与你有新的互动')
}

function interactIcon(type) {
  const map = { like: '❤', super_like: '💖', follow: '⭐', greet: '👋', match: '🎉', gift: '🎁', comment: '💬', skip: '⏭' }
  return map[normalizeType(type)] || '💬'
}
</script>

<style src="./MessagePage.css"></style>
