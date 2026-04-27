import request from './request.js'

let meCache = null
let meCacheAt = 0
let mePending = null

let userStatsCache = null
let userStatsCacheAt = 0
let userStatsPending = null

export function getMe() {
  return request.get('/users/me')
}

export async function getMeCached(maxAgeMs = 15000) {
  const now = Date.now()
  if (meCache && now - meCacheAt < maxAgeMs) return meCache
  if (mePending) return mePending
  mePending = getMe()
    .then((res) => {
      meCache = res
      meCacheAt = Date.now()
      return res
    })
    .finally(() => {
      mePending = null
    })
  return mePending
}

export function clearMeCache() {
  meCache = null
  meCacheAt = 0
  mePending = null
}

export function updateProfile(data) {
  return request.put('/users/profile', data)
}

export function getUserStats() {
  return request.get('/users/me/stats')
}

export async function getUserStatsCached(maxAgeMs = 15000) {
  const now = Date.now()
  if (userStatsCache && now - userStatsCacheAt < maxAgeMs) return userStatsCache
  if (userStatsPending) return userStatsPending
  userStatsPending = getUserStats()
    .then((res) => {
      userStatsCache = res
      userStatsCacheAt = Date.now()
      return res
    })
    .finally(() => {
      userStatsPending = null
    })
  return userStatsPending
}
