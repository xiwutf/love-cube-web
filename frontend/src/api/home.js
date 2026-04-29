import request from './request.js'

function unwrapList(res) {
  if (Array.isArray(res)) return res
  if (Array.isArray(res?.data)) return res.data
  if (Array.isArray(res?.data?.list)) return res.data.list
  if (Array.isArray(res?.data?.records)) return res.data.records
  if (Array.isArray(res?.list)) return res.list
  if (Array.isArray(res?.records)) return res.records
  if (Array.isArray(res?.content)) return res.content
  return []
}

/**
 * 首页一次性初始化：banners + recommends + newcomers + profile + completion 合并为单次请求。
 * 返回 { banners, recommends, newcomers, profile?, completion? }
 */
export async function getHomeInit() {
  return request.get('/home/init')
}

/** 首页轮播图（独立接口，供其他场景按需调用） */
export async function getBanners() {
  const res = await request.get('/banners')
  return unwrapList(res)
}

/** 推荐用户列表 */
export async function getRecommends() {
  const res = await request.get('/recommends')
  return unwrapList(res)
}

/** 新人列表 */
export async function getNewcomers() {
  const res = await request.get('/newcomers')
  return unwrapList(res)
}
