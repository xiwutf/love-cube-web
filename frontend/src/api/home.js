import request from './request.js'

/** 首页轮播图 */
export function getBanners() {
  return request.get('/banners')
}

/** 推荐用户列表 */
export function getRecommends() {
  return request.get('/recommends')
}

/** 新人列表 */
export function getNewcomers() {
  return request.get('/newcomers')
}
