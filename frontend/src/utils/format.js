/**
 * 时间/日期/距离/年龄 格式化工具
 * 迁移自小程序 utils/util.js
 */

/** 格式化时间戳为 HH:mm 或 昨天/日期 */
export function formatTime(timestamp) {
  if (!timestamp) return ''
  const date = new Date(typeof timestamp === 'number' && timestamp < 1e12 ? timestamp * 1000 : timestamp)
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const target = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  const diffDays = Math.round((today - target) / 86400000)

  const hh = String(date.getHours()).padStart(2, '0')
  const mm = String(date.getMinutes()).padStart(2, '0')

  if (diffDays === 0) return `${hh}:${mm}`
  if (diffDays === 1) return `昨天 ${hh}:${mm}`
  if (diffDays < 7)  return `${diffDays}天前`
  // 超过一周：同年仍用月-日；跨年或往年必须带年份，避免误以为是「今年」
  if (date.getFullYear() === now.getFullYear()) {
    return formatDate(date, 'MM-DD')
  }
  return formatDate(date, 'YYYY-MM-DD')
}

/** 格式化日期 */
export function formatDate(date, fmt = 'YYYY-MM-DD') {
  const d = date instanceof Date ? date : new Date(date)
  if (isNaN(d)) return ''
  const map = {
    'YYYY': d.getFullYear(),
    'MM':   String(d.getMonth() + 1).padStart(2, '0'),
    'DD':   String(d.getDate()).padStart(2, '0'),
    'HH':   String(d.getHours()).padStart(2, '0'),
    'mm':   String(d.getMinutes()).padStart(2, '0'),
    'ss':   String(d.getSeconds()).padStart(2, '0'),
  }
  return fmt.replace(/YYYY|MM|DD|HH|mm|ss/g, k => map[k])
}

/** 根据生日字符串计算年龄 */
export function formatAge(birthday) {
  if (!birthday) return ''
  const birth = new Date(birthday)
  const now = new Date()
  let age = now.getFullYear() - birth.getFullYear()
  const m = now.getMonth() - birth.getMonth()
  if (m < 0 || (m === 0 && now.getDate() < birth.getDate())) age--
  return age > 0 ? age : ''
}

/** 格式化距离 */
export function formatDistance(meters) {
  if (meters == null) return ''
  return meters < 1000 ? `${meters}m` : `${(meters / 1000).toFixed(1)}km`
}

/** 防抖 */
export function debounce(fn, delay = 300) {
  let timer = null
  return function (...args) {
    clearTimeout(timer)
    timer = setTimeout(() => fn.apply(this, args), delay)
  }
}

/** 节流 */
export function throttle(fn, interval = 300) {
  let last = 0
  return function (...args) {
    const now = Date.now()
    if (now - last >= interval) {
      last = now
      fn.apply(this, args)
    }
  }
}
