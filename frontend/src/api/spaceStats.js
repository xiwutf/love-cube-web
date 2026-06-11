import request from './request.js'

export function fetchSpaceStats(groupId) {
  return request.get(`/platform/groups/${groupId}/space-stats`)
}
