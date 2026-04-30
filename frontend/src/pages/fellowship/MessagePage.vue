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
      offset-top="52"
      :border="false"
      class="msg-tabs"
    >
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

              <div v-if="!loadingChat && !chatList.length" class="empty-wrap">
                <van-empty description="暂无聊天消息" image-size="70" />
                <van-button round type="primary" size="small" color="#ff5f84" @click="router.push('/fellowship/discover')">
                  去看看推荐用户吧
                </van-button>
              </div>
            </van-list>
          </div>
        </van-pull-refresh>
      </van-tab>

      <van-tab title="互动" :badge="msgStore.unreadInteract || ''">
        <van-pull-refresh v-model="refreshingInteract" @refresh="loadInteract">
          <div class="tab-content">
            <van-list v-model:loading="loadingInteract" :finished="true" finished-text="">
              <div
                v-for="item in interactList"
                :key="item.id"
                class="interact-item"
                role="button"
                tabindex="0"
                @click="goInteractProfile(item)"
              >
                <van-image round width="46" height="46" :src="getAvatar(interactFromUser(item))" fit="cover">
                  <template #error>
                    <div class="avatar-fb size46">{{ interactInitial(item) }}</div>
                  </template>
                </van-image>
                <div class="interact-info">
                  <div class="interact-row">
                    <span class="interact-name">{{ getInteractName(item) }}</span>
                    <span class="interact-time">{{ formatTime(item.createdAt || item.time) }}</span>
                  </div>
                  <p class="interact-sub">
                    <span class="interact-action">{{ interactLabel(item.type) }}</span>
                    <span v-if="interactPreview(item)" class="interact-preview">{{ interactPreview(item) }}</span>
                  </p>
                </div>
                <div class="interact-type-icon">{{ interactIcon(item.type) }}</div>
                <van-icon name="arrow" class="interact-chevron" />
              </div>
              <van-empty v-if="!loadingInteract && !interactList.length" description="暂无互动消息" image-size="70" />
            </van-list>
          </div>
        </van-pull-refresh>
      </van-tab>

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
                    <span class="chat-name">{{ getVisitorName(item) }}</span>
                    <span class="chat-time">{{ formatTime(item.visitTime) }}</span>
                  </div>
                  <p class="chat-last visitor-label">来查看了你的主页</p>
                </div>
              </div>
              <van-empty v-if="!loadingVisitor && !visitorList.length" description="暂无访客记录" image-size="70" />
            </van-list>
          </div>
        </van-pull-refresh>
      </van-tab>

      <van-tab title="通知" :badge="msgStore.unreadNotification || ''">
        <van-pull-refresh v-model="refreshingNotif" @refresh="loadNotifications">
          <div class="tab-content">
            <div class="notif-toolbar" v-if="notifList.length">
              <span class="notif-count">共 {{ notifList.length }} 条通知</span>
              <van-button size="mini" plain type="primary" @click="readAll">全部已读</van-button>
            </div>

            <div
              v-for="item in notifList"
              :key="item.id"
              class="notif-item"
              :class="{ unread: !item.isRead }"
              @click="handleNotifClick(item)"
            >
              <div class="notif-icon">{{ notifIcon(item.type) }}</div>
              <div class="notif-body">
                <p class="notif-title">{{ item.title }}</p>
                <p class="notif-content">{{ item.content }}</p>
                <p class="notif-time">{{ formatTime(item.createdAt) }}</p>
              </div>
              <div v-if="!item.isRead" class="notif-dot" />
            </div>

            <van-empty v-if="!loadingNotif && !notifList.length" description="暂无通知消息" image-size="70" />
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
import { showConfirmDialog, showToast } from 'vant'
import AppTabBar from '@/components/AppTabBar.vue'
import { useMessageStore } from '@/stores/message.js'
import {
  getChatList,
  getInteractList,
  getVisitorList,
  getUnreadCount,
  markInteractRead,
  markVisitorRead
} from '@/api/message.js'
import {
  getNotifications,
  getNotifUnreadCountCached,
  markNotifRead,
  markAllNotifRead
} from '@/api/notification.js'
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
    // 进入聊天列表即认为已查看消息中心入口，先清掉底部“消息”角标
    msgStore.clearChat()
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
  if (item.targetType === 'USER' && item.targetId) {
    router.push(`/fellowship/user-profile/${item.targetId}`)
  } else if (item.targetType === 'CHAT' && item.targetId) {
    router.push(`/fellowship/chat/${item.targetId}`)
  }
}

async function readAll() {
  try {
    await markAllNotifRead()
    notifList.value.forEach((item) => {
      item.isRead = true
    })
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

watch(
  () => route.query.tab,
  (tab) => {
    const nextTab = TAB_MAP[tab] ?? 0
    if (activeTab.value !== nextTab) activeTab.value = nextTab
  }
)

onMounted(async () => {
  await fetchUnread()
  // 进入消息中心后先清空底栏入口角标，避免已进入页面仍显示“未读”。
  msgStore.clearChat()
  msgStore.clearInteract()
  msgStore.clearVisitor()
  if (activeTab.value !== 3) msgStore.clearNotification()
  if (activeTab.value === 0) loadChat()
  if (activeTab.value === 1) loadInteract()
  if (activeTab.value === 2) loadVisitor()
  if (activeTab.value === 3) loadNotifications()
})

function goChat(item) {
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

function interactPreview(item) {
  const text = (item.content || item.message || '').trim()
  if (!text) return ''
  return text.length > 36 ? `${text.slice(0, 36)}…` : text
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
    await showConfirmDialog({
      title: '删除聊天',
      message: `确认删除与 ${item.nickname} 的全部聊天记录吗？`
    })
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

function notifIcon(type) {
  const map = {
    LIKE: '❤',
    MESSAGE: '💬',
    SYSTEM: '📢',
    REPORT_HANDLED: '🛡',
    BANNED: '⛔'
  }
  return map[type] || '🔔'
}

function interactLabel(type) {
  const map = {
    like: '喜欢了你',
    super_like: '超级喜欢了你',
    follow: '关注了你',
    greet: '向你打了招呼',
    match: '和你匹配成功',
    gift: '给你送了礼物',
    comment: '给你留了言',
    skip: '在推荐中跳过了你'
  }
  return map[type] || (typeof type === 'string' && type ? `互动：${type}` : '与你有新的互动')
}

function interactIcon(type) {
  const map = {
    like: '❤',
    super_like: '💖',
    follow: '⭐',
    greet: '👋',
    match: '🎉',
    gift: '🎁',
    comment: '💬',
    skip: '⏭'
  }
  return map[type] || '💬'
}
</script>

<style scoped>
.message-page {
  min-height: 100dvh;
  background: #f2f4f8;
  padding-bottom: 72px;
}

.msg-header {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 52px;
  padding: 0 16px;
  background: #fff;
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid #edf1f6;
}

.msg-header-title {
  font-size: 20px;
  font-weight: 800;
  color: #1a2236;
}

.msg-header-badge {
  transform: translateY(-8px);
}

.msg-tabs :deep(.van-tabs__wrap) {
  background: #fff;
  border-bottom: 1px solid #edf1f6;
}

.msg-tabs :deep(.van-tabs__nav) {
  background: transparent;
  padding: 0 8px;
}

.msg-tabs :deep(.van-tab) {
  font-size: 16px;
  font-weight: 700;
  color: #7e8ea4;
}

.msg-tabs :deep(.van-tab--active) {
  color: #ff5f84;
  font-weight: 800;
}

.msg-tabs :deep(.van-tabs__line) {
  height: 3px;
  border-radius: 2px;
  width: 24px !important;
}

.tab-content {
  background: #fff;
  min-height: calc(100dvh - 52px - 44px - 72px);
}

.chat-cell :deep(.van-swipe-cell__wrapper) {
  background: #fff;
}

.chat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 13px 16px;
  background: #fff;
  border-bottom: 1px solid #f1f4f8;
}

.chat-avatar-wrap {
  position: relative;
  flex-shrink: 0;
}

.unread-dot {
  position: absolute;
  top: -5px;
  right: -5px;
  min-width: 20px;
  height: 20px;
  border-radius: 10px;
  background: #f43f5e;
  color: #fff;
  font-size: 10px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 5px;
  border: 2px solid #fff;
  box-shadow: 0 4px 10px rgba(244, 63, 94, 0.3);
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
  color: #94a3b8;
}

.empty-wrap {
  min-height: 300px;
  padding: 18px 0 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  justify-content: center;
}

.empty-wrap :deep(.van-button) {
  min-width: 148px;
  font-size: 15px;
  font-weight: 600;
  height: 38px;
}

.interact-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 12px 12px 16px;
  background: #fff;
  border-bottom: 1px solid #f1f4f8;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
}

.interact-item:active {
  background: #f8fafc;
}

.interact-info {
  flex: 1;
  min-width: 0;
}

.interact-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 4px;
}

.interact-name {
  font-size: 15px;
  font-weight: 700;
  color: #1a2236;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  min-width: 0;
}

.interact-sub {
  margin: 0;
  font-size: 13px;
  line-height: 1.45;
  color: #5a6a80;
}

.interact-action {
  font-weight: 500;
  color: #ff5f84;
}

.interact-preview {
  color: #8898aa;
  margin-left: 4px;
}

.interact-time {
  font-size: 11px;
  color: #c0cad8;
  flex-shrink: 0;
}

.interact-type-icon {
  font-size: 22px;
  flex-shrink: 0;
}

.interact-chevron {
  font-size: 14px;
  color: #c9d4e3;
  flex-shrink: 0;
}

.notif-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px;
  border-bottom: 1px solid #edf1f6;
}

.notif-count {
  font-size: 12px;
  color: #8898aa;
}

.notif-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  border-bottom: 1px solid #f1f4f8;
  position: relative;
}

.notif-item.unread {
  background: #fff8fa;
}

.notif-icon {
  font-size: 24px;
  flex-shrink: 0;
  margin-top: 2px;
}

.notif-body {
  flex: 1;
  min-width: 0;
}

.notif-title {
  font-size: 14px;
  font-weight: 600;
  color: #1a2236;
  margin-bottom: 4px;
}

.notif-content {
  font-size: 13px;
  color: #5a6a80;
  line-height: 1.5;
  margin-bottom: 6px;
}

.notif-time {
  font-size: 11px;
  color: #c0cad8;
}

.notif-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #f43f5e;
  margin-top: 4px;
}

.avatar-fb {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff5f84, #ffb3c4);
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
