import request from './request.js'
import { getEventGuestToken } from '@/utils/eventGuestToken.js'
import { storage } from '@/utils/storage.js'

function guestConfig(eventId, extra = {}) {
  const guestToken = getEventGuestToken(eventId)
  const headers = { ...(extra.headers || {}) }
  if (guestToken) {
    headers['X-Event-Guest-Token'] = guestToken
  }
  const hasAuth = Boolean(storage.get('token'))
  return {
    ...extra,
    headers,
    skip401Redirect: !hasAuth && Boolean(guestToken)
  }
}

export function createGuestSession(eventId, payload) {
  return request.post(`/events/${eventId}/guest/session`, payload, guestConfig(eventId))
}

export function fetchGuestContext(eventId) {
  return request.get(`/events/${eventId}/guest/context`, guestConfig(eventId))
}

export function guestJoin(eventId) {
  return request.post(`/events/${eventId}/guest/join`, {}, guestConfig(eventId))
}

export function guestCheckin(eventId, code) {
  return request.post(`/events/${eventId}/guest/checkin`, { code }, guestConfig(eventId))
}
