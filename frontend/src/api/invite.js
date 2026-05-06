import request from './request.js'
import { createInviteUrl } from '@/utils/inviteUrl.js'

export { createInviteUrl }

export function getMyInviteCode() {
  return request.get('/fellowship/invite/my-code')
}

export function getInviteInfo() {
  return request.get('/user/invite-info').then((res) => {
    const inviteCode = String(res?.inviteCode || res?.code || '').trim()
    return {
      ...res,
      inviteCode,
      inviteLink: createInviteUrl(inviteCode)
    }
  })
}

export function getMyInvitees() {
  return request.get('/fellowship/invite/my-invitees')
}

export function getAdminInvites(params = {}) {
  return request.get('/admin/invites', { params })
}
