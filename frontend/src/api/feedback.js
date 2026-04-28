import request from './request.js'

export function submitFeedback(payload) {
  return request.post('/feedback', payload)
}

export function submitCoCreationFeedback(payload) {
  return request.post('/feedback/submit', payload)
}
