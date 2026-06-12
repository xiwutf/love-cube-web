import request from './request.js'
import { getEventGuestToken } from '@/utils/eventGuestToken.js'
import { storage } from '@/utils/storage.js'

function datingConfig(eventId, extra = {}) {
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

function datingGet(eventId, url) {
  return request.get(url, datingConfig(eventId))
}

function datingPut(eventId, url, payload) {
  return request.put(url, payload, datingConfig(eventId))
}

function datingPost(eventId, url, payload) {
  return request.post(url, payload, datingConfig(eventId))
}

function datingDelete(eventId, url) {
  return request.delete(url, datingConfig(eventId))
}

export function fetchDatingContext(eventId) {
  return datingGet(eventId, `/events/${eventId}/dating/context`)
}

export function fetchDatingIdentity(eventId) {
  return datingGet(eventId, `/events/${eventId}/dating/me/identity`)
}

export function fetchDatingProfilePrefill(eventId) {
  return datingGet(eventId, `/events/${eventId}/dating/me/profile/prefill`)
}

export function fetchDatingProfile(eventId) {
  return datingGet(eventId, `/events/${eventId}/dating/me/profile`)
}

export function saveDatingProfile(eventId, payload) {
  return datingPut(eventId, `/events/${eventId}/dating/me/profile`, payload)
}

export function fetchDatingMyCard(eventId) {
  return datingGet(eventId, `/events/${eventId}/dating/me/card`)
}

export function fetchDatingRoster(eventId) {
  return datingGet(eventId, `/events/${eventId}/dating/roster`)
}

export function fetchDatingUserCard(eventId, userId) {
  return fetchDatingParticipantCard(eventId, `REGISTERED:${userId}`)
}

export function fetchDatingParticipantCard(eventId, participantKey) {
  const encoded = encodeURIComponent(participantKey)
  return datingGet(eventId, `/events/${eventId}/dating/roster/participant/${encoded}/card`)
}

export function createDatingConnection(eventId, payload) {
  return datingPost(eventId, `/events/${eventId}/dating/connections`, payload)
}

export function deleteDatingConnection(eventId, connectionId) {
  return datingDelete(eventId, `/events/${eventId}/dating/connections/${connectionId}`)
}

export function fetchDatingMyConnections(eventId) {
  return datingGet(eventId, `/events/${eventId}/dating/my-connections`)
}

export function fetchDatingConnectionStats(eventId) {
  return datingGet(eventId, `/events/${eventId}/dating/my-connections/stats`)
}

export function createDatingLike(eventId, payload) {
  return datingPost(eventId, `/events/${eventId}/dating/likes`, payload)
}

export function deleteDatingLike(eventId, likeId) {
  return datingDelete(eventId, `/events/${eventId}/dating/likes/${likeId}`)
}

export function fetchDatingMyLikes(eventId) {
  return datingGet(eventId, `/events/${eventId}/dating/my-likes`)
}

export function fetchDatingLikeStats(eventId) {
  return datingGet(eventId, `/events/${eventId}/dating/my-likes/stats`)
}

export function fetchDatingMutualMatches(eventId) {
  return datingGet(eventId, `/events/${eventId}/dating/my-mutual-matches`)
}
