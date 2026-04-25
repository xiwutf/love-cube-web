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

