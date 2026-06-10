const STORAGE_KEY = 'fellowship-preferences'

export function readFellowshipPreferences() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}')
  } catch {
    return {}
  }
}

/** 根据择偶条件与匹配筛选，生成「条件过严」提示文案 */
export function buildPreferenceBiasHints(options = {}) {
  const prefs = readFellowshipPreferences()
  const hints = []

  const minAge = Number(prefs.minAge || options.minAge || 22)
  const maxAge = Number(prefs.maxAge || options.maxAge || 32)
  if (maxAge - minAge <= 4) {
    hints.push('年龄范围较窄，可放宽 3–5 岁')
  }

  const city = String(prefs.city || options.region || '').trim()
  if (city) {
    hints.push(`已限定城市「${city}」，可尝试周边城市或留空`)
  }

  const education = String(prefs.education || '').trim()
  if (education && education !== '不限') {
    hints.push(`学历要求「${education}」，可改为「不限」`)
  }

  const gender = prefs.gender || options.gender || ''
  if (gender && gender !== 'any' && gender !== '') {
    hints.push('性别偏好已锁定，可在筛选中改为「不限」')
  }

  if (options.verifiedOnly) {
    hints.push('已开启「仅看已认证」，关闭后可看到更多人')
  }

  return hints
}

export function isMatchFilterStrict(filter = {}) {
  const ageRange = filter.ageRange || [18, 40]
  const narrowAge = ageRange[1] - ageRange[0] <= 6
  return Boolean(filter.region?.trim() || filter.verifiedOnly || filter.gender || narrowAge)
}
