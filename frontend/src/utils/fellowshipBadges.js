/** 联谊用户卡片徽章展示工具 */

export function pickDisplayBadges(user = {}) {
  const verified = Array.isArray(user.verifiedBadges) ? user.verifiedBadges : []
  const normal = Array.isArray(user.badges) ? user.badges : []
  const identity = verified.slice(0, 2)
  const remaining = Math.max(0, 3 - identity.length)
  return {
    identityBadges: identity,
    normalBadges: normal.slice(0, remaining)
  }
}

export function formatBadgeLabel(badge) {
  if (!badge) return ''
  return badge.name || badge.title || badge.code || ''
}
