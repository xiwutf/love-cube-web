import request from './request.js'

export function getMyGrowth() {
  return request.get('/users/me/growth')
}

export function getGrowthCenter() {
  return request.get('/growth/me')
}

export function getGrowthContributions(limit = 20) {
  return request.get('/growth/contributions/me', { params: { limit } })
}

export function getGrowthAchievements(limit = 20) {
  return request.get('/growth/achievements/me', { params: { limit } })
}

export function getGrowthEvents(limit = 20) {
  return request.get('/growth/events/me', { params: { limit } })
}

export function recordGrowthAction(payload) {
  return request.post('/growth/actions', payload)
}

export function claimDailyTask(taskCode) {
  return request.post(`/daily-tasks/${taskCode}/claim`)
}

export function claimAccountTask(taskCode) {
  return request.post(`/account-tasks/${taskCode}/claim`)
}

export function getMyBadges() {
  return request.get('/badges/me')
}
