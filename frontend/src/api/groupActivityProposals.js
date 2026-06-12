import request from './request.js'

export function submitActivityProposal(groupId, payload) {
  return request.post(`/platform/groups/${groupId}/activity-proposals`, payload)
}

export function fetchMyActivityProposals(groupId) {
  return request.get(`/platform/groups/${groupId}/my-activity-proposals`)
}

export function fetchPendingActivityProposals(groupId) {
  return request.get(`/platform/groups/${groupId}/activity-proposals/pending`)
}

export function reviewActivityProposal(groupId, activityId, payload) {
  return request.post(`/platform/groups/${groupId}/activity-proposals/${activityId}/review`, payload)
}
