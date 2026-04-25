import request from './request.js'

export const getDynamics   = (page = 1, size = 10) => request.get('/dynamics', { params: { page, size } })
export const likeDynamic   = (id) => request.post(`/dynamics/${id}/like`)
export const unlikeDynamic = (id) => request.delete(`/dynamics/${id}/like`)
export const deleteDynamic = (id) => request.delete(`/dynamics/${id}`)
