const PREFIX = 'lc-event-guest:'

function storageKey(eventId) {
  return `${PREFIX}${eventId}`
}

export function saveEventGuestSession(eventId, { guestToken, guestParticipantId, nickname }) {
  if (!eventId || !guestToken) return
  localStorage.setItem(storageKey(eventId), JSON.stringify({
    guestToken,
    guestParticipantId: guestParticipantId || null,
    nickname: nickname || ''
  }))
}

export function getEventGuestSession(eventId) {
  if (!eventId) return null
  const raw = localStorage.getItem(storageKey(eventId))
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    return null
  }
}

export function getEventGuestToken(eventId) {
  return getEventGuestSession(eventId)?.guestToken || ''
}

export function hasEventGuestToken(eventId) {
  return Boolean(getEventGuestToken(eventId))
}

export function clearEventGuestSession(eventId) {
  if (!eventId) return
  localStorage.removeItem(storageKey(eventId))
}
