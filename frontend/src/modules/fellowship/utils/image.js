const BASE = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/api$/, '')

export function toFullUrl(url) {
  if (!url) return ''
  if (/^https?:\/\//.test(url)) return url
  if (url.startsWith('/')) return BASE + url
  return BASE + '/' + url
}

export const DEFAULT_AVATAR = '/default-avatar.svg'

export function getAvatar(user) {
  const raw = user?.avatarUrl || user?.profilePhoto || user?.profile_photo || user?.avatar || ''
  if (/^https?:\/\/(localhost|127\.0\.0\.1)(:\d+)?\//i.test(raw)) {
    try {
      const u = new URL(raw)
      if (u.pathname.startsWith('/admin/uploads/')) return u.pathname
      if (u.pathname.startsWith('/uploads/')) return '/admin' + u.pathname
    } catch {
      /* ignore */
    }
  }
  return toFullUrl(raw) || DEFAULT_AVATAR
}
