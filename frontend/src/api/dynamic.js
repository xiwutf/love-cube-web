import request from './request.js'

export const getDynamics     = (pageNum = 1, pageSize = 10) => request.get('/dynamics', { params: { pageNum, pageSize } })
export const publishDynamic  = (payload) => request.post('/dynamics', payload)
export const likeDynamic     = (id)  => request.post(`/dynamics/${id}/like`)
export const unlikeDynamic   = (id)  => request.delete(`/dynamics/${id}/like`)
export const deleteDynamic   = (id)  => request.delete(`/dynamics/${id}`)
export const getDynamicComments = (id, pageNum = 1, pageSize = 50, extra = {}) =>
  request.get(`/dynamics/${id}/comments`, { params: { pageNum, pageSize, ...extra } })
export const getDynamicLikes = (id, pageNum = 1, pageSize = 50) =>
  request.get(`/dynamics/${id}/likes`, { params: { pageNum, pageSize } })
export const postDynamicComment = (id, payload) => request.post(`/dynamics/${id}/comments`, payload)
export const deleteDynamicComment = (id, commentId) => request.delete(`/dynamics/${id}/comments/${commentId}`)
