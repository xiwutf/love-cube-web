/** 徽章图标类型与后端字段映射 */
export const BADGE_ICON_KEYS = ['star', 'shield', 'heart', 'medal']

export const BADGE_NAME_ICON_MAP = {
  新手上路: 'star',
  活跃达人: 'heart',
  热心帮助: 'shield',
  官方认证: 'medal',
  初来乍到: 'star',
  坚持七天: 'star',
  分享达人: 'medal',
  暖心使者: 'heart',
  成长先锋: 'medal',
  团体成员: 'shield'
}

export const BADGE_EMOJI_ICON_MAP = {
  '⭐': 'star',
  '🌟': 'star',
  '🛡️': 'shield',
  '👥': 'shield',
  '💗': 'heart',
  '💖': 'heart',
  '❤️': 'heart',
  '🏅': 'medal',
  '🎖️': 'medal',
  '🏆': 'medal',
  '🔥': 'star',
  '🌱': 'heart',
  '🎉': 'medal',
  '📝': 'shield',
  '🌱': 'heart',
  '🎤': 'medal',
  '📅': 'star'
}

const ALLOWED = new Set(BADGE_ICON_KEYS)

export function resolveBadgeIconKey(item, index = 0) {
  const raw = String(item?.icon || '').trim()
  if (raw && ALLOWED.has(raw)) return raw
  if (raw && BADGE_EMOJI_ICON_MAP[raw]) return BADGE_EMOJI_ICON_MAP[raw]
  const byName = BADGE_NAME_ICON_MAP[String(item?.name || '').trim()]
  if (byName) return byName
  return BADGE_ICON_KEYS[index % BADGE_ICON_KEYS.length]
}
