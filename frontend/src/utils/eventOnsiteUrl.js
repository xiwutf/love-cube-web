export function createEventOnsitePath(eventId) {
  if (!eventId) return ''
  return `/events/${eventId}/onsite`
}

export function createEventOnsiteUrl(eventId) {
  const path = createEventOnsitePath(eventId)
  if (!path) return ''
  if (typeof window === 'undefined') return `#${path}`
  return `${window.location.origin}${window.location.pathname}#${path}`
}
