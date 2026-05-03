import request from './request.js'

export function getMyGrowth() {
  return request.get('/users/me/growth')
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
