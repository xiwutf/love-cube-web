import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { storage } from '@/utils/storage.js'
import { login as loginApi, register as registerApi } from '@/api/auth.js'
import {
  activateFellowship as activateFellowshipApi,
  clearMeCache,
  deactivateFellowship as deactivateFellowshipApi,
  getMeCached,
  updateFellowshipMatchVisibility as updateFellowshipMatchVisibilityApi
} from '@/api/user.js'
import { useFellowshipProfileStore } from '@/stores/fellowshipProfile.js'

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
    height: raw.height ?? '',
    fellowshipEnabled: Boolean(raw.fellowshipEnabled),
    fellowshipMatchVisible: Boolean(raw.fellowshipMatchVisible)
  }
}

export const useUserStore = defineStore('user', () => {
  const token = ref(storage.get('token') || '')
  const userId = ref(storage.get('userId') || '')
  const id = ref(storage.get('userId') || '')
  const nickname = ref(storage.get('nickname') || '')
  const avatar = ref(storage.get('avatar') || '')
  const userInfo = ref(null)

  function setAuth(newToken, newUserId) {
    clearMeCache()
    token.value = newToken
    userId.value = String(newUserId)
    storage.set('token', newToken)
    storage.set('userId', String(newUserId))
  }

  function setUserInfo(info) {
    userInfo.value = info
    id.value = String(info?.userId || info?.id || '')
    nickname.value = info?.nickname || info?.username || ''
    avatar.value = info?.avatar || ''
    storage.set('nickname', nickname.value)
    storage.set('avatar', avatar.value)
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
      id.value = normalized.userId
      storage.set('userId', normalized.userId)
    }
    setUserInfo(normalized)
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

  async function activateFellowship() {
    if (!token.value) {
      throw new Error('请先登录')
    }
    const res = await activateFellowshipApi()
    clearMeCache()
    const normalized = normalizeUser(res)
    userInfo.value = normalized
    return normalized
  }

  async function deactivateFellowship() {
    if (!token.value) {
      throw new Error('请先登录')
    }
    const res = await deactivateFellowshipApi()
    clearMeCache()
    const normalized = normalizeUser(res)
    userInfo.value = normalized
    return normalized
  }

  async function updateFellowshipMatchVisibility(visible) {
    if (!token.value) {
      throw new Error('请先登录')
    }
    const res = await updateFellowshipMatchVisibilityApi(Boolean(visible))
    clearMeCache()
    const normalized = normalizeUser(res)
    userInfo.value = normalized
    return normalized
  }

  function logout() {
    token.value = ''
    userId.value = ''
    id.value = ''
    nickname.value = ''
    avatar.value = ''
    userInfo.value = null
    storage.remove('token')
    storage.remove('userId')
    storage.remove('nickname')
    storage.remove('avatar')
    storage.remove(REDIRECT_KEY)
    clearMeCache()
    useFellowshipProfileStore().clearProfile()
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
  const isFellowshipEnabled = computed(() => Boolean(userInfo.value?.fellowshipEnabled))
  const isFellowshipMatchVisible = computed(() => Boolean(userInfo.value?.fellowshipMatchVisible))

  if (token.value) {
    refreshCurrentUser().catch(() => {
      logout()
    })
  }

  return {
    token,
    id,
    nickname,
    avatar,
    userId,
    userInfo,
    isLoggedIn,
    isAdmin,
    isFellowshipEnabled,
    isFellowshipMatchVisible,
    setAuth,
    setUserInfo,
    syncCurrentUser,
    refreshCurrentUser,
    login,
    register,
    activateFellowship,
    deactivateFellowship,
    updateFellowshipMatchVisibility,
    logout,
    setPostLoginRedirect,
    consumePostLoginRedirect
  }
})
