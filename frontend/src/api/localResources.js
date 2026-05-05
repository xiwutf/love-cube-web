import request from './request.js'

export function getLocalResources(params = {}) {
  return request.get('/local-resources', { params })
}

export function getLocalResourceDetail(id) {
  return request.get(`/local-resources/${id}`)
}

export function markLocalResourceInterest(id) {
  return request.post(`/local-resources/${id}/interest`)
}
