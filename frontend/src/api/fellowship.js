import request from './request.js'

export function getMyFellowshipProfile() {
  return request.get('/fellowship/profile/me')
}

export function updateMyFellowshipProfile(data) {
  return request.put('/fellowship/profile/me', data)
}

export function fetchFellowshipUsers(params = {}) {
  return request.get('/fellowship/users', { params })
}

export function fetchFellowshipUserDetail(userId) {
  return request.get(`/fellowship/users/${userId}`)
}

export function sendFellowshipInterest(targetUserId) {
  return request.post(`/fellowship/interests/${targetUserId}`)
}

export function fetchFellowshipMatches() {
  return request.get('/fellowship/matches')
}

export function fetchFellowshipHome() {
  return request.get('/fellowship/home')
}

export function submitFellowshipReport(payload) {
  return request.post('/fellowship/reports', payload)
}

export function submitReport(payload) {
  return request.post('/reports', payload)
}

export function getMyReports() {
  return request.get('/reports/my')
}

// Blacklist
export function getMyBlacklist() {
  return request.get('/blacklist')
}

export function blockUser(userId, reason = '') {
  return request.post(`/blacklist/${userId}`, { reason })
}

export function unblockUser(userId) {
  return request.delete(`/blacklist/${userId}`)
}

export function getBlockStatus(userId) {
  return request.get(`/blacklist/status/${userId}`)
}

export function getFellowshipMeStats() {
  return request.get('/fellowship/users/me/stats')
}

let meStatsCache = null
let meStatsCacheAt = 0
let meStatsPending = null

export async function getFellowshipMeStatsCached(maxAgeMs = 15000) {
  const now = Date.now()
  if (meStatsCache && now - meStatsCacheAt < maxAgeMs) return meStatsCache
  if (meStatsPending) return meStatsPending
  meStatsPending = getFellowshipMeStats()
    .then((res) => {
      meStatsCache = res
      meStatsCacheAt = Date.now()
      return res
    })
    .finally(() => {
      meStatsPending = null
    })
  return meStatsPending
}

