/** @typedef {'DRAFT'|'REGISTRATION_OPEN'|'UPCOMING'|'ONGOING'|'ENDED'|'CANCELLED'} ActivityDisplayStatus */

export const DEFAULT_EVENT_DURATION_MS = 3 * 60 * 60 * 1000

/**
 * 平台活动有效结束时间：end_time 优先，否则 start + 3 小时
 * @param {object} activity
 * @returns {number|null}
 */
export function resolveEffectiveEndMs(activity) {
  if (!activity) return null
  const endMs = parseTime(activity.endTime || activity.effectiveEndTime)
  if (endMs != null) return endMs
  const startMs = parseTime(activity.startTime || activity.eventTime || activity.time)
  if (startMs == null) return null
  return startMs + DEFAULT_EVENT_DURATION_MS
}

/**
 * 统一活动展示状态（不改数据库，仅前端推导）
 * @param {object} activity
 * @returns {{ code: ActivityDisplayStatus, label: string, tone: string }}
 */
export function getActivityDisplayStatus(activity) {
  if (!activity) {
    return { code: 'ENDED', label: '已结束', tone: 'neutral' }
  }

  const rawStatus = String(activity.status || activity.activityStatus || 'published').toLowerCase()
  if (rawStatus === 'draft' || rawStatus === 'pending') {
    return { code: 'DRAFT', label: rawStatus === 'pending' ? '待审核' : '草稿', tone: 'neutral' }
  }
  if (rawStatus === 'cancelled' || rawStatus === 'rejected') {
    return { code: 'CANCELLED', label: rawStatus === 'rejected' ? '已拒绝' : '已取消', tone: 'danger' }
  }

  const now = Date.now()
  const startMs = parseTime(activity.startTime || activity.eventTime || activity.time)
  const endMs = resolveEffectiveEndMs(activity)

  if (endMs != null && endMs < now) {
    return { code: 'ENDED', label: '已结束', tone: 'neutral' }
  }
  if (startMs != null && startMs <= now && (endMs == null || endMs >= now)) {
    return { code: 'ONGOING', label: '进行中', tone: 'success' }
  }
  if (startMs != null && startMs > now) {
    const withinDay = startMs - now <= 24 * 60 * 60 * 1000
    return {
      code: withinDay ? 'UPCOMING' : 'REGISTRATION_OPEN',
      label: withinDay ? '即将开始' : '报名中',
      tone: withinDay ? 'warning' : 'info'
    }
  }

  return { code: 'REGISTRATION_OPEN', label: '报名中', tone: 'info' }
}

/** 用户参与视角的状态标签（与后端 status 码对齐） */
export function getParticipationStatusLabel(status) {
  const map = {
    waiting_start: '已报名，等待活动开始',
    checkin_pending: '活动进行中，待签到',
    checked_in_waiting: '已签到，等待活动开始',
    checked_in_ongoing: '已签到，活动进行中',
    review_pending: '活动已结束，待互评',
    completed: '已完成',
    missed: '未签到，活动已结束',
    // 兼容旧码
    upcoming: '已报名，等待活动开始',
    checked_in: '已签到，活动进行中'
  }
  return map[status] || '已报名'
}

export function getParticipationStatusTone(status) {
  const map = {
    waiting_start: 'info',
    checkin_pending: 'warning',
    checked_in_waiting: 'info',
    checked_in_ongoing: 'success',
    review_pending: 'warning',
    completed: 'neutral',
    missed: 'danger',
    upcoming: 'info',
    checked_in: 'success'
  }
  return map[status] || 'info'
}

/**
 * 签到后现场状态说明（活动详情页）
 * @param {string} status - 参与状态码
 * @returns {string|null}
 */
export function getOnSiteStatusMessage(status) {
  const map = {
    checked_in_waiting: '你已签到，活动尚未开始，请按活动时间到场参与。',
    checked_in_ongoing: '你已签到，活动正在进行中，结束后可为同行伙伴互评。',
    review_pending: '活动已结束，你可以为已签到的伙伴进行互评。',
    completed: '你已完成本场活动互评，感谢参与！'
  }
  return map[status] || null
}

function parseTime(value) {
  if (!value) return null
  const normalized = String(value).replace(' ', 'T')
  const ms = new Date(normalized).getTime()
  return Number.isNaN(ms) ? null : ms
}
