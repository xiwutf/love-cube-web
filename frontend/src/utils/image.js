/**
 * 图片 URL 处理工具
 * 后端返回的图片路径可能是相对路径（/uploads/avatar/xxx.jpg），
 * 需要拼接 baseUrl 才能正常显示。
 */

const BASE = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/api$/, '')

/** 将后端返回的图片路径转为可访问的完整 URL */
export function toFullUrl(url) {
  if (!url) return ''
  if (/^https?:\/\//.test(url)) return url   // 已是完整 URL
  if (url.startsWith('/')) return BASE + url   // 绝对路径
  return BASE + '/' + url                      // 相对路径
}

/** 默认头像（本地占位） */
export const DEFAULT_AVATAR = '/default-avatar.svg'

/** 安全取头像 URL */
export function getAvatar(user) {
  const raw = user?.profilePhoto || user?.profile_photo || user?.avatar || user?.avatarUrl || ''
  return toFullUrl(raw) || DEFAULT_AVATAR
}
