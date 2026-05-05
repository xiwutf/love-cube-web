import request from './request.js'

export function fetchHelpRequests(params = {}) {
  return request.get('/help/requests', { params })
}

export function fetchHelpRequestDetail(id) {
  return request.get(`/help/requests/${id}`)
}

export function createHelpRequest(payload) {
  return request.post('/help/requests', payload)
}

export function createHelpReply(requestId, payload) {
  return request.post(`/help/requests/${requestId}/reply`, payload)
}

export function acceptHelpReply(replyId) {
  return request.post(`/help/replies/${replyId}/accept`)
}

export function rejectHelpReply(replyId) {
  return request.post(`/help/replies/${replyId}/reject`)
}

export function resolveHelpRequest(requestId, payload) {
  return request.post(`/help/requests/${requestId}/resolve`, payload)
}

export function closeHelpRequest(requestId) {
  return request.post(`/help/requests/${requestId}/close`)
}

export function fetchMyHelpAuthored(params = {}) {
  return request.get('/help/requests/mine', { params })
}

export function fetchMyHelpReplied(params = {}) {
  return request.get('/help/replies/mine', { params })
}

export function fetchHelpUserStats(userId) {
  return request.get(`/help/user/stats/${userId}`)
}
