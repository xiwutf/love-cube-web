import { reactive } from 'vue'
import { announcements as seedAnnouncements, articles as seedArticles, events as seedEvents, policyPages } from '@/pages/platform/mockData.js'
import { storage } from '@/utils/storage.js'

const STORAGE_KEY = 'platform-mock-state-v2'

function nowString() {
  return new Date().toISOString().slice(0, 19).replace('T', ' ')
}

function uid(prefix) {
  return `${prefix}-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
}

function buildSeed() {
  return {
    announcements: seedAnnouncements.map((item) => ({ ...item, status: 'published', updatedAt: item.date })),
    articles: seedArticles.map((item) => ({ ...item, status: 'published', updatedAt: '2026-04-20' })),
    events: seedEvents.map((item, idx) => ({ ...item, status: 'published', signupCount: 16 + idx * 7, updatedAt: '2026-04-18' })),
    users: [
      {
        id: '1',
        username: '平台管理员',
        phone: '13800000000',
        password: '123456',
        role: 'admin',
        status: 'active',
        avatar: '',
        bio: '负责平台运营和内容审核',
        verificationStatus: 'approved',
        verificationRejectReason: '',
        createdAt: '2026-01-15 09:00:00'
      },
      {
        id: '2',
        username: '演示用户',
        phone: '13900000000',
        password: '123456',
        role: 'user',
        status: 'active',
        avatar: '',
        bio: '正在完善个人资料',
        verificationStatus: 'none',
        verificationRejectReason: '',
        createdAt: '2026-02-20 12:20:00'
      }
    ],
    verifications: [
      {
        id: 'verify-seed-001',
        userId: '2',
        realName: '演示用户',
        idNumber: '310101199001011234',
        note: '演示提交记录（可重新提交）',
        status: 'rejected',
        rejectReason: '证件照片不清晰，请重新上传完整正反面。',
        submittedAt: '2026-04-02 19:30:00',
        reviewedAt: '2026-04-03 10:20:00'
      }
    ],
    reports: [
      {
        id: 'report-seed-001',
        reporterId: '2',
        targetUserId: '1',
        type: '不当言论',
        content: '演示举报内容：用于后台流程展示。',
        status: 'pending',
        note: '',
        createdAt: '2026-04-19 14:30:00'
      }
    ],
    policyPages
  }
}

function readState() {
  const raw = storage.get(STORAGE_KEY)
  if (!raw) return buildSeed()
  try {
    const parsed = JSON.parse(raw)
    return {
      ...buildSeed(),
      ...parsed,
      policyPages
    }
  } catch {
    return buildSeed()
  }
}

const state = reactive(readState())

function persist() {
  storage.set(STORAGE_KEY, JSON.stringify(state))
}

function getUserById(userId) {
  return state.users.find((user) => String(user.id) === String(userId)) || null
}

function sanitizeUser(user) {
  if (!user) return null
  const { password, ...rest } = user
  return { ...rest }
}

function loginWithPassword({ phone, password }) {
  const user = state.users.find((item) => item.phone === phone)
  if (!user) throw new Error('账号不存在，请先注册')
  if (user.status === 'banned') throw new Error('账号已被封禁，请联系平台处理')
  if (user.password !== password) throw new Error('密码错误，请重试')
  return {
    token: `mock-token-${user.id}-${Date.now()}`,
    userId: String(user.id),
    user: sanitizeUser(user)
  }
}

function registerAccount({ username, phone, password }) {
  if (!phone || !password) throw new Error('手机号和密码不能为空')
  if (state.users.some((item) => item.phone === phone)) throw new Error('该手机号已注册')
  const user = {
    id: uid('user'),
    username: username || `用户${phone.slice(-4)}`,
    phone,
    password,
    role: 'user',
    status: 'active',
    avatar: '',
    bio: '',
    verificationStatus: 'none',
    verificationRejectReason: '',
    createdAt: nowString()
  }
  state.users.unshift(user)
  persist()
  return {
    token: `mock-token-${user.id}-${Date.now()}`,
    userId: String(user.id),
    user: sanitizeUser(user)
  }
}

function ensureUserById(userId) {
  const hit = getUserById(userId)
  if (hit) return hit
  const fallback = {
    id: String(userId),
    username: `用户${String(userId).slice(-4)}`,
    phone: '',
    password: '',
    role: 'user',
    status: 'active',
    avatar: '',
    bio: '',
    verificationStatus: 'none',
    verificationRejectReason: '',
    createdAt: nowString()
  }
  state.users.unshift(fallback)
  persist()
  return fallback
}

function submitVerification(userId, payload) {
  const user = ensureUserById(userId)
  const activeRequest = state.verifications.find(
    (item) => String(item.userId) === String(userId) && (item.status === 'pending' || item.status === 'rejected')
  )
  const data = {
    realName: payload.realName,
    idNumber: payload.idNumber,
    note: payload.note || ''
  }

  if (activeRequest) {
    Object.assign(activeRequest, data, {
      status: 'pending',
      rejectReason: '',
      submittedAt: nowString(),
      reviewedAt: ''
    })
  } else {
    state.verifications.unshift({
      id: uid('verify'),
      userId: String(userId),
      ...data,
      status: 'pending',
      rejectReason: '',
      submittedAt: nowString(),
      reviewedAt: ''
    })
  }

  user.verificationStatus = 'pending'
  user.verificationRejectReason = ''
  persist()
}

function reviewVerification(verificationId, nextStatus, rejectReason = '') {
  const item = state.verifications.find((entry) => entry.id === verificationId)
  if (!item) return
  const user = ensureUserById(item.userId)
  item.status = nextStatus
  item.rejectReason = nextStatus === 'rejected' ? rejectReason : ''
  item.reviewedAt = nowString()
  user.verificationStatus = nextStatus
  user.verificationRejectReason = nextStatus === 'rejected' ? rejectReason : ''
  persist()
}

function updateContent(type, payload) {
  const list = state[type]
  const idx = list.findIndex((item) => item.id === payload.id)
  if (idx < 0) return
  list[idx] = { ...list[idx], ...payload, updatedAt: nowString() }
  persist()
}

function createContent(type, payload) {
  const list = state[type]
  list.unshift({
    id: uid(type.slice(0, -1)),
    status: 'draft',
    updatedAt: nowString(),
    ...payload
  })
  persist()
}

function toggleContentStatus(type, id) {
  const item = state[type].find((entry) => entry.id === id)
  if (!item) return
  item.status = item.status === 'published' ? 'offline' : 'published'
  item.updatedAt = nowString()
  persist()
}

function updateUserStatus(userId, status) {
  const user = getUserById(userId)
  if (!user) return
  user.status = status
  persist()
}

function updateReport(reportId, payload) {
  const item = state.reports.find((entry) => entry.id === reportId)
  if (!item) return
  Object.assign(item, payload)
  persist()
}

function getVerificationByUser(userId) {
  return state.verifications.find((item) => String(item.userId) === String(userId)) || null
}

export function usePlatformState() {
  return {
    state,
    persist,
    getUserById,
    sanitizeUser,
    loginWithPassword,
    registerAccount,
    ensureUserById,
    submitVerification,
    reviewVerification,
    updateContent,
    createContent,
    toggleContentStatus,
    updateUserStatus,
    updateReport,
    getVerificationByUser
  }
}
