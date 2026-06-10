/**
 * 归一化后端返回的用户对象
 * 后端各接口字段命名不统一（驼峰/下划线/别名），统一为前端约定字段。
 * identityRole 缺失时默认 'self'，保证旧用户数据兼容。
 */
import { getAvatar } from './image.js'
import { formatAge } from './format.js'

export function normalizeUser(raw) {
  if (!raw) return null
  const normalizedPhotos = normalizePhotos(raw)
  return {
    userId:      raw.userId   ?? raw.userid   ?? raw.id,
    nickname:    raw.nickname ?? raw.username  ?? raw.name ?? '',
    avatar:      getAvatar(raw),
    gender:      raw.gender   ?? '',
    age:         raw.age      ?? (raw.birthday ? formatAge(raw.birthday) : '') ?? '',
    birthday:    raw.birthday ?? raw.birthDate ?? '',
    constellation: raw.constellation ?? '',
    location:    raw.location ?? '',
    occupation:  raw.occupation ?? '',
    height:      raw.height   ?? '',
    signature:   raw.signature ?? raw.bio ?? '',
    photos:      normalizedPhotos,
    completionRate: raw.completionRate ?? 0,
    recommendReasons: Array.isArray(raw.recommendReasons) ? raw.recommendReasons : [],
    photoVerified: Boolean(raw.photoVerified),
    realnameVerified: Boolean(raw.realnameVerified),
    statistics:  raw.statistics ?? null,

    // Identity role — determines whether this is a self account or guardian
    identityRole: raw.identityRole ?? 'self',
    guardianRole: raw.guardianRole ?? '',

    // Guardian child fields (populated when identityRole != 'self')
    childGender:             raw.childGender             ?? '',
    childAge:                raw.childAge                ?? null,
    childHeight:             raw.childHeight             ?? null,
    childEducation:          raw.childEducation          ?? '',
    childJob:                raw.childJob                ?? '',
    childCity:               raw.childCity               ?? '',
    childHouseCarStatus:     raw.childHouseCarStatus     ?? '',
    childMarriageIntention:  raw.childMarriageIntention  ?? '',
    childPartnerRequirements:raw.childPartnerRequirements?? '',
    guardianContactVisible:  raw.guardianContactVisible  ?? true,
  }
}

function normalizePhotos(raw) {
  const sources = [raw?.photos, raw?.photoUrls, raw?.photo_urls, raw?.images, raw?.imageUrls]
  for (const source of sources) {
    if (Array.isArray(source)) {
      return source.filter((item) => typeof item === 'string' && item.trim())
    }
  }
  return []
}

export function isGuardian(user) {
  return user?.identityRole === 'guardian_son' || user?.identityRole === 'guardian_daughter'
}

export function guardianLabel(identityRole, guardianRole) {
  const roleMap = { father: '父亲', mother: '母亲', family: '家人' }
  const childMap = { guardian_son: '儿子', guardian_daughter: '女儿' }
  const gLabel = roleMap[guardianRole] || '家长'
  const cLabel = childMap[identityRole] || '孩子'
  return `${gLabel}代${cLabel}征婚`
}
