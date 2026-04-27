import request from './request.js'

// Users
export function getAdminUsers() {
  return request.get('/admin/users')
}

export function updateAdminUserRole(userId, role) {
  return request.put(`/admin/users/${userId}/role`, { role })
}

export function updateAdminUserStatus(userId, status) {
  return request.put(`/admin/users/${userId}/status`, { status })
}

export function forceDeleteAdminUser(userId) {
  return request.delete(`/admin/users/${userId}/force`)
}

// Stats
export function getAdminStats() {
  return request.get('/admin/stats')
}

// Announcements
export function getAnnouncements() {
  return request.get('/admin/announcements')
}

export function saveAnnouncement(payload) {
  return request.post('/admin/announcements', payload)
}

export function deleteAnnouncement(id) {
  return request.delete(`/admin/announcements/${id}`)
}

// Articles
export function getArticles() {
  return request.get('/admin/articles')
}

export function saveArticle(payload) {
  return request.post('/admin/articles', payload)
}

export function deleteArticle(id) {
  return request.delete(`/admin/articles/${id}`)
}

// Events
export function getEvents() {
  return request.get('/admin/events')
}

export function saveEvent(payload) {
  return request.post('/admin/events', payload)
}

export function deleteEvent(id) {
  return request.delete(`/admin/events/${id}`)
}

// Verifications
export function getVerifications() {
  return request.get('/admin/verifications')
}

export function reviewVerification(id, action, reason) {
  return request.patch(`/admin/verifications/${id}/review`, { action, reason })
}

// Reports
export function getReports() {
  return request.get('/admin/reports')
}

export function updateReport(id, payload) {
  return request.patch(`/admin/reports/${id}`, payload)
}

// Feedbacks
export function getFeedbacks() {
  return request.get('/admin/feedbacks')
}

export function updateFeedback(id, payload) {
  return request.patch(`/admin/feedbacks/${id}`, payload)
}

// Invites
export function getInvites(params = {}) {
  return request.get('/admin/invites', { params })
}

export function reviewReport(id, action, note = '') {
  return request.patch(`/admin/reports/${id}/review`, { action, note })
}

// Home config
export function getAdminHomeConfig() {
  return request.get('/admin/home-config')
}

export function saveAdminHomeConfig(payload) {
  return request.put('/admin/home-config', payload)
}

export async function getAdminModuleConfig() {
  const config = await getAdminHomeConfig()
  return Array.isArray(config?.modules) ? config.modules : []
}

export async function saveAdminModuleConfig(modules) {
  const config = await getAdminHomeConfig()
  return saveAdminHomeConfig({
    ...config,
    modules: Array.isArray(modules) ? modules : []
  })
}
