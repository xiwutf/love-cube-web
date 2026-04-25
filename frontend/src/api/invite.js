import request from './request.js'

export function getMyInviteCode() {
  return request.get('/fellowship/invite/my-code')
}

export function getMyInvitees() {
  return request.get('/fellowship/invite/my-invitees')
}

export function getAdminInvites(params = {}) {
  return request.get('/admin/invites', { params })
}

