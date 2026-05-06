/**
 * API 字段治理：同一语义只保留一个「主字段」，其余仅作兼容入参。
 * 规范见 docs/field-governance.md
 */

/** 用户头像 URL（主字段 avatarUrl） */
export function userAvatarUrlFromApi(item) {
  if (item == null || typeof item !== 'object') return ''
  const v = item.avatarUrl ?? item.avatar ?? item.profilePhoto ?? item.profile_photo
  return typeof v === 'string' ? v.trim() : String(v ?? '').trim()
}

/** 团体动态流封面（主字段 coverUrl；历史误用 avatarUrl） */
export function groupFeedCoverUrlFromApi(item) {
  if (item == null || typeof item !== 'object') return ''
  const v = item.coverUrl ?? item.avatarUrl
  return typeof v === 'string' ? v.trim() : String(v ?? '').trim()
}
