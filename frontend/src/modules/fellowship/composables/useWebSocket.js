import { ref, onUnmounted } from 'vue'
import { storage } from '@f/utils/storage.js'

const WS_BASE = (import.meta.env.VITE_API_BASE_URL || '')
  .replace(/^http/, 'ws')
  .replace(/\/admin$/, '/admin')

const MAX_RECONNECT   = 5
const HEARTBEAT_MS    = 30_000
const BASE_DELAY_MS   = 1_000

export function useWebSocket(userId) {
  const messages       = ref([])
  const errors         = ref([])
  const status         = ref('closed')
  const reconnectCount = ref(0)

  let ws             = null
  let heartbeatTimer = null
  let reconnectTimer = null
  let msgQueue       = []

  function connect() {
    if (ws && ws.readyState < 2) return
    status.value = 'connecting'
    ws = new WebSocket(`${WS_BASE}/ws/chat/${userId}`)

    ws.onopen = () => {
      status.value        = 'open'
      reconnectCount.value = 0
      startHeartbeat()
      flushQueue()
    }

    ws.onmessage = ({ data }) => {
      try {
        const msg = JSON.parse(data)
        if (msg.type === 'pong' || msg.type === 'sent_confirm') return
        if (msg.type === 'error') {
          errors.value.push(msg)
          return
        }
        messages.value.push(normalizeMsg(msg))
      } catch {
        // non-JSON message
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
    reconnectCount.value = MAX_RECONNECT
    ws?.close()
    ws = null
  }

  function normalizeMsg(raw) {
    const myId = Number(storage.get('userId'))
    return {
      id:         raw.id        ?? Date.now(),
      senderId:   raw.senderId  ?? raw.sender_id,
      receiverId: raw.receiverId ?? raw.receiver_id,
      content:    raw.content   ?? '',
      timestamp:  raw.timestamp ?? Date.now(),
      isSelf:     Number(raw.senderId ?? raw.sender_id) === myId,
      type:       raw.type      ?? 'chat',
    }
  }

  onUnmounted(disconnect)

  return { messages, errors, status, reconnectCount, connect, send, disconnect }
}
