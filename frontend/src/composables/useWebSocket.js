/**
 * WebSocket 封装 composable
 * 替换小程序 wx.connectSocket + socketTask.*
 * 支持：心跳、指数退避重连、离线消息队列、自动关闭
 */
import { ref, onUnmounted } from 'vue'
import { storage } from '@/utils/storage.js'

const WS_BASE = (import.meta.env.VITE_API_BASE_URL || '')
  .replace(/^http/, 'ws')        // http → ws，https → wss
  .replace(/\/admin$/, '/admin') // 保留 /admin 前缀

const MAX_RECONNECT   = 5
const HEARTBEAT_MS    = 30_000
const BASE_DELAY_MS   = 1_000

export function useWebSocket(userId) {
  const messages      = ref([])   // 渲染用消息列表
  const status        = ref('closed')  // closed | connecting | open | error
  const reconnectCount = ref(0)

  let ws             = null
  let heartbeatTimer = null
  let reconnectTimer = null
  let msgQueue       = []         // 连接建立前的待发消息

  function connect() {
    if (ws && ws.readyState < 2) return  // 已连接或连接中
    status.value = 'connecting'
    ws = new WebSocket(`${WS_BASE}/ws/chat/${userId}`)

    ws.onopen = () => {
      status.value     = 'open'
      reconnectCount.value = 0
      startHeartbeat()
      flushQueue()
    }

    ws.onmessage = ({ data }) => {
      try {
        const msg = JSON.parse(data)
        if (msg.type === 'pong') return  // 心跳响应，忽略
        messages.value.push(normalizeMsg(msg))
      } catch {
        // 非 JSON 消息，忽略
      }
    }

    ws.onerror = () => {
      status.value = 'error'
      stopHeartbeat()
    }

    ws.onclose = () => {
      status.value = 'closed'
      stopHeartbeat()
      if (reconnectCount.value < MAX_RECONNECT) {
        const delay = Math.min(BASE_DELAY_MS * 2 ** reconnectCount.value, 10_000)
        reconnectCount.value++
        reconnectTimer = setTimeout(connect, delay)
      }
    }
  }

  function send(content, receiverId) {
    const msg = { type: 'chat', senderId: Number(userId), receiverId: Number(receiverId), content, timestamp: Date.now() }
    if (ws?.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify(msg))
    } else {
      msgQueue.push(msg)
    }
    // 本地先显示（乐观更新）
    messages.value.push({ ...normalizeMsg(msg), isSelf: true, pending: true })
  }

  function flushQueue() {
    while (msgQueue.length && ws?.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify(msgQueue.shift()))
    }
  }

  function startHeartbeat() {
    heartbeatTimer = setInterval(() => {
      if (ws?.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({ type: 'ping' }))
      }
    }, HEARTBEAT_MS)
  }

  function stopHeartbeat() {
    clearInterval(heartbeatTimer)
    heartbeatTimer = null
  }

  function disconnect() {
    clearTimeout(reconnectTimer)
    stopHeartbeat()
    reconnectCount.value = MAX_RECONNECT  // 阻止自动重连
    ws?.close()
    ws = null
  }

  function normalizeMsg(raw) {
    const myId = Number(storage.get('userId'))
    return {
      id:          raw.id        ?? Date.now(),
      senderId:    raw.senderId  ?? raw.sender_id,
      receiverId:  raw.receiverId ?? raw.receiver_id,
      content:     raw.content   ?? '',
      timestamp:   raw.timestamp ?? Date.now(),
      isSelf:      Number(raw.senderId ?? raw.sender_id) === myId,
      type:        raw.type      ?? 'chat',
    }
  }

  onUnmounted(disconnect)

  return { messages, status, reconnectCount, connect, send, disconnect }
}
