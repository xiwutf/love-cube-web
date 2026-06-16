/**
 * API 字段治理：同一语义只保留一个「主字段」，其余仅作兼容入参。
 * 规范见 docs/field-governance.md
 */

import { resolveUploadUrl } from '@/utils/image.js'

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

/** 团体卡片封面：解析上传路径并支持默认图 */
export function groupCoverUrlFromApi(item, fallback = '') {
  const raw = groupFeedCoverUrlFromApi(item)
  if (!raw) return fallback
  const resolved = resolveUploadUrl(raw)
  if (resolved) return resolved
  if (/^https?:\/\//i.test(raw)) return raw
  return fallback
}

/** 与审核通过等状态对齐，用于展示「已认证」徽章（含大小写与历史别名） */
export function isVerifiedStatusString(v) {
  const raw = String(v ?? '').trim().toLowerCase()
  return ['verified', 'approved', 'passed', 'success', 'done', 'active', 'completed', '1', 'true'].includes(raw)
}

/** 综合最新审核状态与真人/实名通过位 */
export function userHasVerificationBadge(row) {
  if (row == null || typeof row !== 'object') return false
  if (isVerifiedStatusString(row.verificationStatus)) return true
  if (row.photoVerified === true) return true
  if (row.realnameVerified === true) return true
  return false
}
