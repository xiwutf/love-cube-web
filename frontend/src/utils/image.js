/**
 * 图片 URL 处理工具
 * 后端返回的图片路径可能是相对路径（/uploads/avatar/xxx.jpg 或 /admin/uploads/...），
 * 浏览器应请求 /admin/uploads/...（由 Nginx 反代到 Spring context-path=/admin）。
 */

import { toBrowserUploadUrl } from '@/utils/uploadUrl.js'

const BASE = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/api$/, '')

/** 将后端返回的图片路径转为当前页面可加载的地址（不拼接生产域名）。 */
export function toFullUrl(url) {
  if (!url) return ''
  const resolved = resolveUploadUrl(url)
  if (!resolved) return ''
  if (/^https?:\/\//i.test(resolved)) return resolved
  if (resolved.startsWith('/admin/')) return resolved
  if (resolved.startsWith('/')) return `${BASE}${resolved}`
  return `${BASE}/${resolved}`
}

/**
 * 将头像/相册/认证图/公告附件地址转为当前页面可加载的 URL。
 */
export function resolveUploadUrl(url) {
  return toBrowserUploadUrl(url)
}

/** 默认头像（本地占位） */
export const DEFAULT_AVATAR = '/default-avatar.svg'

/** 安全取头像 URL */
export function getAvatar(user) {
  const raw =
    user?.userAvatar ||
    user?.avatarUrl ||
    user?.profilePhoto ||
    user?.profile_photo ||
    user?.avatar ||
    resolveFirstPhoto(user) ||
    ''
  const resolved = resolveUploadUrl(raw)
  return resolved || DEFAULT_AVATAR
}

function resolveFirstPhoto(user) {
  if (!user) return ''
  const candidates = [
    user?.photos,
    user?.photoUrls,
    user?.photo_urls,
    user?.images,
    user?.imageUrls
  ]
  for (const candidate of candidates) {
    if (Array.isArray(candidate) && candidate.length > 0) {
      const first = candidate.find((item) => typeof item === 'string' && item.trim())
      if (first) return first
    }
  }
  return ''
}
