const BASE = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/api$/, '')

export function toFullUrl(url) {
  if (!url) return ''
  if (/^https?:\/\//.test(url)) return url
  if (url.startsWith('/')) return BASE + url
  return BASE + '/' + url
}

export const DEFAULT_AVATAR = '/default-avatar.svg'

export function getAvatar(user) {
  const raw = user?.profilePhoto || user?.profile_photo || user?.avatar || user?.avatarUrl || ''
  return toFullUrl(raw) || DEFAULT_AVATAR
}
