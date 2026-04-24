import request from './request.js'

/** 获取当前登录用户信息 */
export function getMe() {
  return request.get('/users/me')
}

/** 更新用户资料 */
export function updateProfile(data) {
  return request.put('/users/profile', data)
}
