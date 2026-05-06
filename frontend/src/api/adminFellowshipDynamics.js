import request from './request.js'

export function getAdminFellowshipDynamics(pageNum = 1, pageSize = 20) {
  return request.get('/admin/fellowship-dynamics', { params: { pageNum, pageSize } })
}

export function getAdminFellowshipDynamicLikes(dynamicId, pageNum = 1, pageSize = 50) {
  return request.get(`/admin/fellowship-dynamics/${dynamicId}/likes`, { params: { pageNum, pageSize } })
}
