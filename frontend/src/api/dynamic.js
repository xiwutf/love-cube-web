import request from './request.js'

export const getDynamics     = (pageNum = 1, pageSize = 10) => request.get('/dynamics', { params: { pageNum, pageSize } })
export const publishDynamic  = (payload) => request.post('/dynamics', payload)
export const likeDynamic     = (id)  => request.post(`/dynamics/${id}/like`)
export const unlikeDynamic   = (id)  => request.delete(`/dynamics/${id}/like`)
export const deleteDynamic   = (id)  => request.delete(`/dynamics/${id}`)
