import request from './request.js'

export function createPositiveShare(payload) {
  return request.post('/positive-shares', payload)
}

export function fetchPositiveShares(params = {}) {
  return request.get('/positive-shares', { params })
}

export function fetchMyPositiveShares(params = {}) {
  return request.get('/positive-shares/my', { params })
}

export function fetchMyFavoritePositiveShares(params = {}) {
  return request.get('/positive-shares/my/favorites', { params })
}

export function fetchMyLikedPositiveShares(params = {}) {
  return request.get('/positive-shares/my/likes', { params })
}

export function fetchMyPositiveShareDrafts(params = {}) {
  return request.get('/positive-shares/my/drafts', { params })
}

export function likePositiveShare(id) {
  return request.post(`/positive-shares/${id}/like`)
}

export function unlikePositiveShare(id) {
  return request.delete(`/positive-shares/${id}/like`)
}

export function bookmarkPositiveShare(id) {
  return request.post(`/positive-shares/${id}/bookmark`)
}

export function unbookmarkPositiveShare(id) {
  return request.delete(`/positive-shares/${id}/bookmark`)
}

export function commentPositiveShare(id, payload) {
  return request.post(`/positive-shares/${id}/comments`, payload)
}

export function fetchPositiveShareComments(id, params = {}) {
  return request.get(`/positive-shares/${id}/comments`, { params })
}
