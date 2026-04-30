import request from './request.js'

// ── Platform groups (public module) ──────────────────────────────────────────

function isLegacyPlatformGroupId(id) {
  if (id === null || id === undefined) return false
  return /^\d+$/.test(String(id))
}

/** Normalizes list API: plain array or paginated { items, total, page, pageSize }. */
export function unwrapPlatformGroupList(res) {
  if (Array.isArray(res)) return res
  if (Array.isArray(res?.items)) return res.items
  if (Array.isArray(res?.data?.items)) return res.data.items
  if (Array.isArray(res?.data)) return res.data
  return []
}

/** 团体大厅列表（platform_groups + 分页结构） */
export async function fetchGroups(params = {}) {
  const normalizedParams = { ...params }

  // 新表接口默认按 active 查询；兼容旧表时改为 published。
  if (!normalizedParams.status) normalizedParams.status = 'active'
  const modern = await request.get('/groups', { params: normalizedParams })
  const modernItems = unwrapPlatformGroupList(modern)
  if (modernItems.length > 0) return modern

  const legacyParams = { ...normalizedParams }
  if (legacyParams.status === 'active') legacyParams.status = 'published'
  return request.get('/platform/groups', { params: legacyParams })
}

export function fetchMeGroupsBuckets() {
  return request.get('/platform/me/groups')
}

export function fetchMyGroups() {
  return request.get('/platform/groups/my')
}

export function fetchGroupDetail(id) {
  if (isLegacyPlatformGroupId(id)) {
    return request.get(`/platform/groups/${id}`)
  }
  return request.get(`/groups/${id}`)
}

export function fetchGroupMembers(id, params = {}) {
  if (isLegacyPlatformGroupId(id)) {
    return request.get(`/platform/groups/${id}/members`, { params })
  }
  return request.get(`/groups/${id}/members`, { params })
}

export function fetchGroupPosts(id) {
  if (isLegacyPlatformGroupId(id)) {
    return request.get(`/platform/groups/${id}/posts`)
  }
  return request.get(`/groups/${id}/posts`)
}

/** 从 group_posts 中筛公告类 type */
export async function fetchGroupNotices(id) {
  const path = isLegacyPlatformGroupId(id)
    ? `/platform/groups/${id}/notices`
    : `/groups/${id}/posts`
  const res = await request.get(path)
  const raw = Array.isArray(res) ? res : res?.data ?? []
  if (isLegacyPlatformGroupId(id)) return raw
  return raw.filter((p) => {
    const t = String(p.type || '').toLowerCase()
    return t === 'notice' || t === 'announcement' || t === 'bulletin'
  })
}

export function fetchHotGroups() {
  return request.get('/groups/hot')
}

export function fetchGroupFeed() {
  return request.get('/platform/groups/feed')
}

export function joinGroup(id, message = '') {
  if (isLegacyPlatformGroupId(id)) {
    return request.post(`/platform/groups/${id}/join`, { message })
  }
  return request.post(`/groups/${id}/join`, { message })
}

export function leaveGroup(id) {
  if (isLegacyPlatformGroupId(id)) {
    return request.post(`/platform/groups/${id}/leave`)
  }
  return request.delete(`/groups/${id}/leave`)
}

/** 登录用户创建 platform_groups，创建者写入 platform_group_admin(OWNER) */
export function createGroup(payload) {
  return request.post('/groups', payload)
}

export function updateGroup(id, payload) {
  return request.put(`/platform/groups/${id}`, payload)
}

export function createGroupPost(id, payload) {
  return request.post(`/groups/${id}/posts`, payload)
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

export function auditMember(groupId, memberId, action = 'approve') {
  return request.put(`/platform/groups/${groupId}/members/${memberId}/audit`, { action })
}

export function removeGroupMember(groupId, memberId) {
  return request.delete(`/platform/groups/${groupId}/members/${memberId}`)
}

// ── Admin groups (back-office) ────────────────────────────────────────────────

export function getAdminGroups() {
  return request.get('/admin/groups')
}

/** 后台团体详情（含 userRole / userPermissions，platform_groups） */
export function fetchAdminGroupDetail(id) {
  return request.get(`/admin/groups/${id}`)
}

export function getAdminGroupDetail(id) {
  return fetchAdminGroupDetail(id)
}

/** 公共 API：platform_groups 的团体资料（无角色信息） */
export function fetchLegacyGroupDetail(id) {
  return request.get(`/groups/${id}`)
}

/** 公共 API：团体动态列表（group_posts） */
export function fetchLegacyGroupPosts(id) {
  return request.get(`/groups/${id}/posts`)
}

/** 后台：已加入成员（需 OWNER/ADMIN） */
export function fetchAdminGroupMembers(id) {
  return request.get(`/admin/groups/${id}/members`)
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

export function getAdminGroupAdmins(groupId) {
  return request.get(`/admin/groups/${groupId}/admins`)
}

export function addAdminGroupAdmin(groupId, payload) {
  return request.post(`/admin/groups/${groupId}/admins`, payload)
}

export function removeAdminGroupAdmin(groupId, userId) {
  return request.delete(`/admin/groups/${groupId}/admins/${userId}`)
}
