import request from './request.js'

export function fetchPlatformMemberStatus() {
  return request.get('/platform/member/status')
}

export function subscribePlatformMember() {
  return request.post('/platform/member/subscribe')
}
