import request from './request.js'

export function getAdminUsers() {
  return request.get('/admin/users')
}

export function updateAdminUserRole(userId, role) {
  return request.put(`/admin/users/${userId}/role`, { role })
}

