// frontend/src/api/content.js
import request from './request.js'

export function checkContent(text, context = 'content-check') {
  return request.post('/content/check', { text, context })
}
