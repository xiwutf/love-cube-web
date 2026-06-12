import request from './request.js'

export function fetchOnsiteContext(eventId) {
  return request.get(`/events/${eventId}/onsite/context`)
}

export function onsiteJoin(eventId) {
  return request.post(`/events/${eventId}/onsite/join`)
}

export function onsiteCheckin(eventId, code) {
  return request.post(`/events/${eventId}/onsite/checkin`, { code })
}
