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

/**
 * 将头像/相册/认证图地址转为当前页面可加载的 URL。
 * 库中常存完整 http(s) 指向 :8090、localhost 或 OSS；HTTPS 管理页引用 HTTP 资源时需同域 /admin/uploads/...。
 */
export function resolveUploadUrl(url) {
  const value = String(url || '').trim()
  if (!value) return ''
  const normalized = value.replace(/\\/g, '/')

  if (/^https?:\/\//i.test(normalized)) {
    try {
      const u = new URL(normalized)
      const p = u.pathname
      const q = u.search || ''
      const isLocalHost = ['localhost', '127.0.0.1'].includes((u.hostname || '').toLowerCase())
      const isHttpOnHttpsPage =
        typeof window !== 'undefined' &&
        window.location.protocol === 'https:' &&
        u.protocol === 'http:'
      if (p.startsWith('/admin/uploads/')) {
        if (isLocalHost || isHttpOnHttpsPage) return `${p}${q}`
        return normalized
      }
      if (p.startsWith('/admin/api/uploads/')) {
        const fixed = p.replace('/admin/api/uploads/', '/admin/uploads/')
        if (isLocalHost || isHttpOnHttpsPage) return `${fixed}${q}`
        return `${u.origin}${fixed}${q}`
      }
      if (p.startsWith('/uploads/')) {
        if (isLocalHost || isHttpOnHttpsPage) return `/admin${p}${q}`
        return normalized
      }
    } catch {
      /* ignore */
    }
    return normalized
  }

  if (normalized.startsWith('/admin/uploads/')) return normalized
  if (normalized.startsWith('/admin/api/uploads/')) return normalized.replace('/admin/api/uploads/', '/admin/uploads/')
  if (normalized.startsWith('/uploads/')) return `/admin${normalized}`
  if (normalized.startsWith('uploads/')) return `/admin/${normalized}`
  if (normalized.startsWith('admin/uploads/')) return `/${normalized}`
  if (normalized.startsWith('admin/api/uploads/')) return `/${normalized.replace('admin/api/uploads/', 'admin/uploads/')}`
  if (normalized.startsWith('/')) return normalized
  if (normalized.includes('/')) return `/admin/${normalized.replace(/^\//, '')}`
  return `/admin/uploads/photos/${normalized}`
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
  return toFullUrl(raw) || DEFAULT_AVATAR
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
