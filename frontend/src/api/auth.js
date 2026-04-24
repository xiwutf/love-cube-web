import request from './request.js'

/** H5 账号密码登录 */
export function login(data) {
  return request.post('/auth/login', data)
}

/** H5 注册 */
export function register(data) {
  return request.post('/auth/register', data)
}
