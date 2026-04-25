import request from './request.js'

export function getAdminUsers() {
  return request.get('/admin/users')
}

