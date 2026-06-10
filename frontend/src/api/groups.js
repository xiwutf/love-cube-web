import request from './request.js'

// ── Platform groups (public module) ──────────────────────────────────────────

/** 数字编号团体（platform_group 表），支持邀请码、赛季、互评、周报等 */
export function isNumericPlatformGroupId(id) {
  if (id === null || id === undefined) return false
  return /^\d+$/.test(String(id))
}

/** @deprecated 历史命名：与 isNumericPlatformGroupId 相同，true = 数字 ID */
export function isLegacyPlatformGroupId(id) {
  return isNumericPlatformGroupId(id)
}

/** 团体动态列表：数组或 { items, total, page, pageSize } */
export function unwrapGroupPostsList(res) {
  if (Array.isArray(res)) return res
  if (Array.isArray(res?.items)) return res.items
  if (Array.isArray(res?.data?.items)) return res.data.items
  if (Array.isArray(res?.data)) return res.data
  return []
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

export function fetchGroupPosts(id, params = {}) {
  if (isLegacyPlatformGroupId(id)) {
    return request.get(`/platform/groups/${id}/posts`, { params })
  }
  return request.get(`/groups/${id}/posts`, { params })
}

/** 切换团体动态点赞（plat：Long postId；platform_groups：需带 groupId） */
export function toggleGroupPostLike(groupId, postId) {
  if (isLegacyPlatformGroupId(groupId)) {
    return request.post(`/platform/groups/posts/${postId}/like`)
  }
  return request.post(`/groups/${groupId}/posts/${postId}/like`)
}

export function fetchGroupPostComments(groupId, postId, params = {}) {
  if (isLegacyPlatformGroupId(groupId)) {
    return request.get(`/platform/groups/posts/${postId}/comments`, { params })
  }
  return request.get(`/groups/${groupId}/posts/${postId}/comments`, { params })
}

export function createGroupPostComment(groupId, postId, payload) {
  if (isLegacyPlatformGroupId(groupId)) {
    return request.post(`/platform/groups/posts/${postId}/comments`, payload)
  }
  return request.post(`/groups/${groupId}/posts/${postId}/comments`, payload)
}

export function deletePlatformGroupPost(groupId, postId) {
  if (isLegacyPlatformGroupId(groupId)) {
    return request.delete(`/platform/groups/${groupId}/posts/${postId}`)
  }
  return request.delete(`/groups/${groupId}/posts/${postId}`)
}

export function deletePlatformGroupPostComment(groupId, postId, commentId) {
  if (isLegacyPlatformGroupId(groupId)) {
    return request.delete(`/platform/groups/posts/${postId}/comments/${commentId}`)
  }
  return request.delete(`/groups/${groupId}/posts/${postId}/comments/${commentId}`)
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

export function joinGroup(id, opts = {}) {
  let message = ''
  let memberRealName = ''
  let inviteCode = ''
  if (typeof opts === 'string') {
    message = opts
  } else if (opts && typeof opts === 'object') {
    message = String(opts.message ?? '')
    memberRealName = String(opts.memberRealName ?? '').trim()
    inviteCode = String(opts.inviteCode ?? '').trim()
  }
  const body = { message, memberRealName }
  if (inviteCode) body.inviteCode = inviteCode
  if (isLegacyPlatformGroupId(id)) {
    return request.post(`/platform/groups/${id}/join`, body)
  }
  return request.post(`/groups/${id}/join`, body)
}

export function fetchGroupByInviteCode(code) {
  const normalized = String(code || '').trim()
  return request.get(`/platform/groups/by-invite-code/${encodeURIComponent(normalized)}`)
}

export function fetchGroupInviteInfo(groupId) {
  return request.get(`/platform/groups/${groupId}/invite-info`)
}

export function refreshGroupInviteCode(groupId) {
  return request.post(`/platform/groups/${groupId}/invite-code/refresh`)
}

export function leaveGroup(id) {
  if (isLegacyPlatformGroupId(id)) {
    return request.post(`/platform/groups/${id}/leave`)
  }
  return request.delete(`/groups/${id}/leave`)
}

/** 已加入成员自助补全或修改本团展示姓名 */
export function patchMyGroupMemberRealName(groupId, memberRealName) {
  const body = { memberRealName: String(memberRealName ?? '').trim() }
  if (isLegacyPlatformGroupId(groupId)) {
    return request.patch(`/platform/groups/${groupId}/me/member-real-name`, body)
  }
  return request.patch(`/groups/${groupId}/me/member-real-name`, body)
}

/** 登录用户创建 platform_groups，创建者写入 platform_group_admin(OWNER) */
export function createGroup(payload) {
  return request.post('/groups', payload)
}

export function updateGroup(id, payload) {
  return request.put(`/platform/groups/${id}`, payload)
}

export function createGroupPost(id, payload) {
  if (isLegacyPlatformGroupId(id)) {
    return request.post(`/platform/groups/${id}/posts`, payload)
  }
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
  if (isLegacyPlatformGroupId(groupId)) {
    return request.delete(`/platform/groups/${groupId}/members/${memberId}`)
  }
  return request.delete(`/groups/${groupId}/members/${memberId}`)
}

/** 团长调整成员角色：admin | member（数字 ID→plat_groups；字符串 ID→platform_groups/group_members） */
export function patchPlatformGroupMemberRole(groupId, memberId, payload) {
  if (isLegacyPlatformGroupId(groupId)) {
    return request.patch(`/platform/groups/${groupId}/members/${memberId}/role`, payload)
  }
  return request.patch(`/groups/${groupId}/members/${memberId}/role`, payload)
}

// ── 打卡 ─────────────────────────────────────────────────────────────────────

export function fetchCheckinSummary(groupId) {
  if (isLegacyPlatformGroupId(groupId)) {
    return request.get(`/platform/groups/${groupId}/checkins/summary`)
  }
  return request.get(`/groups/${groupId}/checkins/summary`)
}

export function createCheckin(groupId, payload) {
  if (isLegacyPlatformGroupId(groupId)) {
    return request.post(`/platform/groups/${groupId}/checkins`, payload)
  }
  return request.post(`/groups/${groupId}/checkins`, payload)
}

/** 团体打卡排行榜 type: daily | streak（需登录且为团体成员） */
export function fetchCheckinRankings(groupId, type = 'daily') {
  if (isLegacyPlatformGroupId(groupId)) {
    return request.get(`/platform/groups/${groupId}/checkins/rankings`, { params: { type } })
  }
  return request.get(`/groups/${groupId}/checkins/rankings`, { params: { type } })
}

export function likePlatformCheckin(checkinId) {
  return request.post(`/platform/checkins/${checkinId}/like`)
}

export function unlikePlatformCheckin(checkinId) {
  return request.delete(`/platform/checkins/${checkinId}/like`)
}

export function fetchPlatformCheckinComments(checkinId, params = {}) {
  return request.get(`/platform/checkins/${checkinId}/comments`, { params })
}

export function createPlatformCheckinComment(checkinId, payload) {
  return request.post(`/platform/checkins/${checkinId}/comments`, payload)
}

export function deletePlatformCheckinComment(commentId) {
  return request.delete(`/platform/checkins/comments/${commentId}`)
}

// ── 团体任务 ──────────────────────────────────────────────────────────────────

export function fetchTodayTasks(groupId) {
  if (isLegacyPlatformGroupId(groupId)) {
    return request.get(`/platform/groups/${groupId}/tasks/today`)
  }
  return request.get(`/groups/${groupId}/tasks/today`)
}

export function claimGroupTask(groupId, taskCode) {
  if (isLegacyPlatformGroupId(groupId)) {
    return request.post(`/platform/groups/${groupId}/tasks/${taskCode}/claim`)
  }
  return request.post(`/groups/${groupId}/tasks/${taskCode}/claim`)
}

// ── 团体活动 ──────────────────────────────────────────────────────────────────

export function fetchGroupActivities(groupId, params = {}) {
  if (isLegacyPlatformGroupId(groupId)) {
    return request.get(`/platform/groups/${groupId}/activities`, { params })
  }
  return request.get(`/groups/${groupId}/activities`, { params })
}

export function createGroupActivity(groupId, payload) {
  if (isLegacyPlatformGroupId(groupId)) {
    return request.post(`/platform/groups/${groupId}/activities`, payload)
  }
  return request.post(`/groups/${groupId}/activities`, payload)
}

export function fetchGroupActivity(groupId, activityId) {
  if (isLegacyPlatformGroupId(groupId)) {
    return request.get(`/platform/groups/${groupId}/activities/${activityId}`)
  }
  return request.get(`/groups/${groupId}/activities/${activityId}`)
}

export function signUpGroupActivity(groupId, activityId) {
  if (isLegacyPlatformGroupId(groupId)) {
    return request.post(`/platform/groups/${groupId}/activities/${activityId}/signup`)
  }
  return request.post(`/groups/${groupId}/activities/${activityId}/signup`)
}

export function cancelGroupActivitySignup(groupId, activityId) {
  if (isLegacyPlatformGroupId(groupId)) {
    return request.post(`/platform/groups/${groupId}/activities/${activityId}/cancel-signup`)
  }
  return request.post(`/groups/${groupId}/activities/${activityId}/cancel-signup`)
}

export function updateGroupActivity(groupId, activityId, payload) {
  if (isLegacyPlatformGroupId(groupId)) {
    return request.patch(`/platform/groups/${groupId}/activities/${activityId}`, payload)
  }
  return request.patch(`/groups/${groupId}/activities/${activityId}`, payload)
}

// ── 团体投票（仅 platform_groups 字符串 ID；纯数字 legacy 团体无此能力）────────

export function fetchGroupPolls(groupId, params = {}) {
  if (isLegacyPlatformGroupId(groupId)) {
    return Promise.resolve({ items: [], total: 0, page: 1, pageSize: 20 })
  }
  return request.get(`/groups/${groupId}/polls`, { params })
}

export function createGroupPoll(groupId, payload) {
  if (isLegacyPlatformGroupId(groupId)) {
    return Promise.reject(new Error('当前团体类型不支持线上投票'))
  }
  return request.post(`/groups/${groupId}/polls`, payload)
}

export function fetchGroupPoll(groupId, pollId) {
  if (isLegacyPlatformGroupId(groupId)) {
    return Promise.reject(new Error('当前团体类型不支持线上投票'))
  }
  return request.get(`/groups/${groupId}/polls/${pollId}`)
}

export function submitGroupPollVotes(groupId, pollId, payload) {
  if (isLegacyPlatformGroupId(groupId)) {
    return Promise.reject(new Error('当前团体类型不支持线上投票'))
  }
  return request.post(`/groups/${groupId}/polls/${pollId}/votes`, payload)
}

export function revealGroupPollResults(groupId, pollId) {
  if (isLegacyPlatformGroupId(groupId)) {
    return Promise.reject(new Error('当前团体类型不支持线上投票'))
  }
  return request.post(`/groups/${groupId}/polls/${pollId}/reveal-results`)
}

export function fetchGroupSeasonRankings(params = {}) {
  return request.get('/groups/season/rankings', { params })
}

export function fetchGroupSeasonRank(groupId) {
  return request.get(`/groups/season/${groupId}/rank`)
}

export function generateGroupActivityCheckinCode(groupId, activityId) {
  if (!isLegacyPlatformGroupId(groupId)) {
    return Promise.reject(new Error('仅数字 ID 团体支持活动签到'))
  }
  return request.post(`/platform/groups/${groupId}/activities/${activityId}/checkin-code`)
}

export function checkinGroupActivity(groupId, activityId, code) {
  if (!isLegacyPlatformGroupId(groupId)) {
    return Promise.reject(new Error('仅数字 ID 团体支持活动签到'))
  }
  return request.post(`/platform/groups/${groupId}/activities/${activityId}/checkin`, { code })
}

export function fetchGroupActivityReviewCandidates(groupId, activityId) {
  if (!isLegacyPlatformGroupId(groupId)) {
    return Promise.reject(new Error('仅数字 ID 团体支持活动互评'))
  }
  return request.get(`/platform/groups/${groupId}/activities/${activityId}/review-candidates`)
}

export function submitGroupActivityReview(groupId, activityId, payload) {
  if (!isLegacyPlatformGroupId(groupId)) {
    return Promise.reject(new Error('仅数字 ID 团体支持活动互评'))
  }
  return request.post(`/platform/groups/${groupId}/activities/${activityId}/reviews`, payload)
}

export function fetchGroupWeeklyDigest(groupId) {
  if (!isLegacyPlatformGroupId(groupId)) {
    return Promise.reject(new Error('仅数字 ID 团体支持周报'))
  }
  return request.get(`/platform/groups/${groupId}/weekly-digest`)
}

export function sendGroupWeeklyDigest(groupId) {
  if (!isLegacyPlatformGroupId(groupId)) {
    return Promise.reject(new Error('仅数字 ID 团体支持周报'))
  }
  return request.post(`/platform/groups/${groupId}/weekly-digest/send`)
}

export function fetchAdminPlatformGroupStats() {
  return request.get('/admin/platform-groups/stats')
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
