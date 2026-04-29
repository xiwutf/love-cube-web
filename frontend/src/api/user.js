import request from './request.js'
import { storage } from '@/utils/storage.js'

let meCache = null
let meCacheAt = 0
let mePending = null
let meCacheToken = ''

let userStatsCache = null
let userStatsCacheAt = 0
let userStatsPending = null

export function getMe() {
  return request.get('/users/me')
}

export function activateFellowship() {
  return request.post('/users/me/fellowship/activate')
}

export function deactivateFellowship() {
  return request.post('/users/me/fellowship/deactivate')
}

export function updateFellowshipMatchVisibility(visible) {
  return request.post('/users/me/fellowship/match-visibility', { visible })
}

export async function getMeCached(maxAgeMs = 15000) {
  const now = Date.now()
  const currentToken = storage.get('token') || ''
  if (meCache && meCacheToken === currentToken && now - meCacheAt < maxAgeMs) return meCache
  if (mePending) return mePending
  mePending = getMe()
    .then((res) => {
      meCache = res
      meCacheAt = Date.now()
      meCacheToken = currentToken
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
  meCacheToken = ''
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
