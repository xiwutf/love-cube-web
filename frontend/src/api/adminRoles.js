import request from './request.js'

// 角色管理（仅 SUPER_ADMIN）
export function getUserAdminRoles(userId) {
  return request.get(`/admin/roles/users/${userId}`)
}

export function assignAdminRole(userId, roleCode) {
  return request.post(`/admin/roles/users/${userId}`, { roleCode })
}

export function revokeAdminRole(userId, roleCode) {
  return request.delete(`/admin/roles/users/${userId}/${roleCode}`)
}

export function assignGroupAdmin(userId, groupId, role = 'OWNER') {
  return request.post('/admin/roles/group-admins', { userId, groupId, role })
}

export function revokeGroupAdmin(groupId, userId) {
  return request.delete(`/admin/roles/group-admins/${groupId}/${userId}`)
}
