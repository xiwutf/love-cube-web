import request from './request.js'

let unreadCountCache = null
let unreadCountCacheAt = 0
let unreadCountPending = null

export const getNotifications = (limit = 10) => request.get('/notifications', { params: { limit } })
export const getNotificationsByType = (type, limit = 20) => request.get('/notifications', { params: { type, limit } })
export const getNotifUnreadCount = () => request.get('/notifications/unread-count')
export async function markNotifRead(id) {
  const res = await request.patch(`/notifications/${id}/read`)
  clearNotifUnreadCache()
  return res
}
export async function markAllNotifRead() {
  const res = await request.post('/notifications/read-all')
  clearNotifUnreadCache()
  return res
}

export async function getNotifUnreadCountCached(maxAgeMs = 15000) {
  const now = Date.now()
  if (unreadCountCache && now - unreadCountCacheAt < maxAgeMs) return unreadCountCache
  if (unreadCountPending) return unreadCountPending
  unreadCountPending = getNotifUnreadCount()
    .then((res) => {
      unreadCountCache = res
      unreadCountCacheAt = Date.now()
      return res
    })
    .finally(() => {
      unreadCountPending = null
    })
  return unreadCountPending
}

export function clearNotifUnreadCache() {
  unreadCountCache = null
  unreadCountCacheAt = 0
  unreadCountPending = null
}
