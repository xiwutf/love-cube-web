import request from './request.js'

export function getMyFellowshipProfile() {
  return request.get('/fellowship/profile/me')
}

export function updateMyFellowshipProfile(data) {
  return request.put('/fellowship/profile/me', data)
}

export function getFellowshipProfileCompletion() {
  return request.get('/fellowship/profile/completion')
}

