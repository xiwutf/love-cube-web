/**
 * 纯函数：把后端/数据库里的上传地址收成浏览器可请求的站点相对路径。
 * 不依赖 Vite env，也不拼接生产域名。
 */
export const ADMIN_UPLOAD_PREFIX = '/admin/uploads/'

export function toBrowserUploadUrl(url) {
  const value = String(url || '').trim()
  if (!value) return ''
  const normalized = value.replace(/\\/g, '/')

  if (/^https?:\/\//i.test(normalized)) {
    try {
      const parsed = new URL(normalized)
      const path = parsed.pathname || ''
      const query = parsed.search || ''
      if (isUploadPath(path)) {
        return `${toBrowserUploadPath(path)}${query}`
      }
      return normalized
    } catch {
      return normalized
    }
  }

  return toBrowserUploadPath(normalized)
}

export function toBrowserUploadPath(path) {
  const raw = String(path || '').trim().replace(/\\/g, '/')
  if (!raw) return ''
  if (raw.startsWith(ADMIN_UPLOAD_PREFIX)) return raw
  if (raw.startsWith('/admin/api/uploads/')) return ADMIN_UPLOAD_PREFIX + raw.slice('/admin/api/uploads/'.length)
  if (raw.startsWith('admin/uploads/')) return `/${raw}`
  if (raw.startsWith('admin/api/uploads/')) return `/${raw.replace('admin/api/uploads/', 'admin/uploads/')}`
  if (raw.startsWith('/uploads/')) return `/admin${raw}`
  if (raw.startsWith('uploads/')) return `/admin/${raw}`
  return raw
}

function isUploadPath(path) {
  return path.startsWith(ADMIN_UPLOAD_PREFIX)
    || path.startsWith('/uploads/')
    || path.startsWith('/admin/api/uploads/')
}
