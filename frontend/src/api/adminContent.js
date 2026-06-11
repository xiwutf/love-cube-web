import request from './request.js'

// Users
export function getAdminUsers(params = {}) {
  return request.get('/admin/users', { params })
}

export function getAdminFellowshipGrowthDashboard() {
  return request.get('/admin/fellowship/growth/dashboard')
}

export function updateAdminUserRole(userId, role) {
  return request.put(`/admin/users/${userId}/role`, { role })
}

export function updateAdminUserStatus(userId, status) {
  return request.put(`/admin/users/${userId}/status`, { status })
}

export function updateAdminUserFellowshipStatus(userId, fellowshipEnabled) {
  return request.put(`/admin/users/${userId}/fellowship`, { fellowshipEnabled })
}

export function forceDeleteAdminUser(userId) {
  return request.delete(`/admin/users/${userId}/force`)
}

export function resetAdminUserPassword(userId) {
  return request.put(`/admin/users/${userId}/reset-password`)
}

export function getAdminUserPhotos(userId) {
  return request.get(`/admin/users/${userId}/photos`)
}

// Stats
export function getAdminStats(refresh = false) {
  return request.get('/admin/stats', { params: refresh ? { refresh: true } : {} })
}

export function getAdminAuthContext() {
  return request.get('/admin/auth-context')
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

// Local resources
export function getAdminLocalResources() {
  return request.get('/admin/local-resources')
}

export function createAdminLocalResource(payload) {
  return request.post('/admin/local-resources', payload)
}

export function updateAdminLocalResource(id, payload) {
  return request.put(`/admin/local-resources/${id}`, payload)
}

export function deleteAdminLocalResource(id) {
  return request.delete(`/admin/local-resources/${id}`)
}

export function offlineAdminLocalResource(id) {
  return request.post(`/admin/local-resources/${id}/offline`)
}

export function publishAdminLocalResource(id) {
  return request.post(`/admin/local-resources/${id}/publish`)
}

// Verifications
export function getVerifications() {
  return request.get('/admin/verifications')
}

export function reviewVerification(id, action, reason) {
  return request.post(`/admin/verifications/${id}/review`, { action, reason })
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

export function getFeedbackSummary() {
  return request.get('/admin/feedbacks/summary')
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

// Positive shares
export function getAdminPositiveShares(params = {}) {
  return request.get('/admin/positive-shares', { params })
}

export function reviewAdminPositiveShare(id, status) {
  return request.post(`/admin/positive-shares/${id}/review`, { status })
}

export function batchReviewAdminPositiveShare(ids, status) {
  return request.post('/admin/positive-shares/batch-review', { ids, status })
}

export function getAdminPositiveShareComments(id, params = {}) {
  return request.get(`/admin/positive-shares/${id}/comments`, { params })
}

export function getAdminPositiveShareReviewSwitch() {
  return request.get('/admin/positive-shares/review-switch')
}

export function updateAdminPositiveShareReviewSwitch(positiveShareReviewRequired) {
  return request.patch('/admin/positive-shares/review-switch', { positiveShareReviewRequired })
}
