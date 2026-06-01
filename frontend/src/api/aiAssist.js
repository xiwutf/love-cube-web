import request from './request.js'

export function polishProfile(payload) {
  return request.post('/ai-assist/profile-polish', payload)
}

export function fetchIcebreakerHints(scene = 'match') {
  return request.get('/ai-assist/icebreaker', { params: { scene } })
}
