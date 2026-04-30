import { trackVisit } from '@/api/analytics.js'
import { storage } from '@/utils/storage.js'

const VISITOR_ID_KEY = 'lc_visitor_id'
let lastTrackedPath = ''

function ensureVisitorId() {
  let visitorId = storage.get(VISITOR_ID_KEY)
  if (visitorId) return visitorId
  visitorId = `v_${Date.now()}_${Math.random().toString(36).slice(2, 10)}`
  storage.set(VISITOR_ID_KEY, visitorId)
  return visitorId
}

export async function trackCurrentVisit(path) {
  if (!path || path === lastTrackedPath) return
  if (path.startsWith('/admin')) return
  lastTrackedPath = path
  const visitorId = ensureVisitorId()
  try {
    await trackVisit({
      visitorId,
      path,
      referrer: document.referrer || ''
    })
  } catch {
    // 埋点失败不影响用户页面流程
  }
}
