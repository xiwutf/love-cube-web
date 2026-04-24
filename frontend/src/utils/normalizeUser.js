/**
 * 归一化后端返回的用户对象
 * 后端各接口字段命名不统一（驼峰/下划线/别名），统一为前端约定字段。
 */
import { getAvatar } from './image.js'
import { formatAge } from './format.js'

export function normalizeUser(raw) {
  if (!raw) return null
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
    photos:      Array.isArray(raw.photos) ? raw.photos : [],
    completionRate: raw.completionRate ?? 0,
    statistics:  raw.statistics ?? null,
  }
}
