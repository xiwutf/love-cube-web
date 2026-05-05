import request from './request.js'

export function getAdminHelpRequests(params = {}) {
  return request.get('/admin/help-requests', { params })
}

export function reviewAdminHelpRequest(id, status) {
  return request.patch(`/admin/help-requests/${id}/review`, { status })
}
