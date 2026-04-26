import request from './request.js'

export const getNotifications     = (limit = 50)  => request.get('/notifications', { params: { limit } })
export const getNotifUnreadCount  = ()             => request.get('/notifications/unread-count')
export const markNotifRead        = (id)           => request.patch(`/notifications/${id}/read`)
export const markAllNotifRead     = ()             => request.post('/notifications/read-all')
