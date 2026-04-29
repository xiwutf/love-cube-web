import request from './request.js'

// ── Platform groups (public module) ──────────────────────────────────────────

export function fetchGroups(params = {}) {
  return request.get('/platform/groups', { params })
}

export function fetchMyGroups() {
  return request.get('/platform/groups')
}

export function fetchGroupDetail(id) {
  return request.get(`/platform/groups/${id}`)
}

export function fetchGroupMembers(id, params = {}) {
  return request.get(`/platform/groups/${id}/members`, { params })
}

export function fetchGroupPosts(id) {
  return request.get(`/platform/groups/${id}/posts`)
}

export function fetchGroupNotices(id) {
  return request.get(`/platform/groups/${id}/notices`)
}

export function fetchHotGroups() {
  return request.get('/platform/groups/hot')
}

export function fetchGroupFeed() {
  return request.get('/platform/groups/feed')
}

export function joinGroup(id, message = '') {
  return request.post(`/platform/groups/${id}/join`, { message })
}

export function leaveGroup(id) {
  return request.post(`/platform/groups/${id}/leave`)
}

export function createGroup(payload) {
  return request.post('/platform/groups', payload)
}

export function createGroupPost(id, payload) {
  return request.post(`/platform/groups/${id}/posts`, payload)
}

export function createGroupNotice(id, payload) {
  return request.post(`/platform/groups/${id}/notices`, payload)
}

export function approveMember(groupId, memberId) {
  return request.post(`/platform/groups/${groupId}/members/${memberId}/approve`)
}

export function rejectMember(groupId, memberId) {
  return request.post(`/platform/groups/${groupId}/members/${memberId}/reject`)
}

// ── Admin groups (back-office) ────────────────────────────────────────────────

export function getAdminGroups() {
  return request.get('/admin/groups')
}

export async function getAdminGroupDetail(id) {
  const groups = await getAdminGroups()
  const list = Array.isArray(groups) ? groups : Array.isArray(groups?.data) ? groups.data : []
  return list.find(item => String(item.id) === String(id)) || null
}

export function createAdminGroup(payload) {
  return request.post('/admin/groups', payload)
}

export function updateAdminGroup(id, payload) {
  return request.put(`/admin/groups/${id}`, payload)
}

export function deleteAdminGroup(id) {
  return request.delete(`/admin/groups/${id}`)
}

export function getAdminGroupJoinRequests(id, status = 'pending') {
  return request.get(`/admin/groups/${id}/join-requests`, { params: { status } })
}

export function approveAdminGroupRequest(groupId, requestId) {
  return request.patch(`/admin/groups/${groupId}/join-requests/${requestId}/approve`)
}

export function rejectAdminGroupRequest(groupId, requestId) {
  return request.patch(`/admin/groups/${groupId}/join-requests/${requestId}/reject`)
}

export function createAdminGroupPost(groupId, payload) {
  return request.post(`/admin/groups/${groupId}/posts`, payload)
}

export function deleteAdminGroupPost(groupId, postId) {
  return request.delete(`/admin/groups/${groupId}/posts/${postId}`)
}

export function removeAdminGroupMember(groupId, userId) {
  return request.delete(`/admin/groups/${groupId}/members/${userId}`)
}
