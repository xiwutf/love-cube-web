import request from './request.js'

export function fetchInterestTopics() {
  return request.get('/interest-topics')
}

export function fetchInterestTopicDetail(id, params = {}) {
  return request.get(`/interest-topics/${id}`, { params })
}

export function createInterestTopicPost(id, content) {
  return request.post(`/interest-topics/${id}/posts`, { content })
}
