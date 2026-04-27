import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { storage } from '@/utils/storage.js'
import { login as loginApi, register as registerApi } from '@/api/auth.js'
import { clearMeCache, getMeCached } from '@/api/user.js'

const REDIRECT_KEY = 'postLoginRedirect'

function normalizeUser(raw) {
  if (!raw) return null
  const id = String(raw.userId ?? raw.id ?? '')
  return {
    id,
    userId: id,
    username: raw.username ?? raw.nickname ?? '',
    nickname: raw.nickname ?? raw.username ?? '',
    phone: raw.phone ?? raw.phoneNumber ?? '',
    role: raw.role ?? 'user',
    status: raw.status ?? 'active',
    verificationStatus: raw.verificationStatus ?? 'none',
    verificationRejectReason: raw.verificationRejectReason ?? '',
    avatar: raw.profilePhoto ?? '',
    bio: raw.bio ?? raw.signature ?? '',
    location: raw.location ?? '',
    gender: raw.gender ?? '',
    birthday: raw.birthday ?? '',
    height: raw.height ?? ''
  }
}

export const useUserStore = defineStore('user', () => {
  const token = ref(storage.get('token') || '')
  const userId = ref(storage.get('userId') || '')
  const userInfo = ref(null)

  function setAuth(newToken, newUserId) {
    token.value = newToken
    userId.value = String(newUserId)
    storage.set('token', newToken)
    storage.set('userId', String(newUserId))
  }

  function setUserInfo(info) {
    userInfo.value = info
  }

  async function refreshCurrentUser() {
    if (!token.value) {
      userInfo.value = null
      return null
    }
    const me = await getMeCached()
    const normalized = normalizeUser(me)
    if (normalized?.userId) {
      userId.value = normalized.userId
      storage.set('userId', normalized.userId)
    }
    userInfo.value = normalized
    return userInfo.value
  }

  function syncCurrentUser() {
    if (!token.value) {
      userInfo.value = null
      return null
    }
    if (!userInfo.value) {
      refreshCurrentUser().catch(() => {
        logout()
      })
    }
    return userInfo.value
  }

  async function login(form) {
    const res = await loginApi(form)
    setAuth(res.token, res.userId)
    await refreshCurrentUser()
    return res
  }

  async function register(form) {
    const res = await registerApi(form)
    setAuth(res.token, res.userId)
    await refreshCurrentUser()
    return res
  }

  function logout() {
    token.value = ''
    userId.value = ''
    userInfo.value = null
    storage.remove('token')
    storage.remove('userId')
    storage.remove(REDIRECT_KEY)
    clearMeCache()
  }

  function setPostLoginRedirect(path) {
    if (!path) return
    storage.set(REDIRECT_KEY, path)
  }

  function consumePostLoginRedirect() {
    const path = storage.get(REDIRECT_KEY)
    storage.remove(REDIRECT_KEY)
    return path || ''
  }

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => ['admin', 'super_admin', 'root'].includes(String(userInfo.value?.role || '').toLowerCase()))

  if (token.value) {
    refreshCurrentUser().catch(() => {
      logout()
    })
  }

  return {
    token,
    userId,
    userInfo,
    isLoggedIn,
    isAdmin,
    setAuth,
    setUserInfo,
    syncCurrentUser,
    refreshCurrentUser,
    login,
    register,
    logout,
    setPostLoginRedirect,
    consumePostLoginRedirect
  }
})
