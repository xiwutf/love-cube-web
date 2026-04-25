import request from './request.js'

export function getMe() {
  return request.get('/users/me')
}

export function updateProfile(data) {
  return request.put('/users/profile', data)
}
