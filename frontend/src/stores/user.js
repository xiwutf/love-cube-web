import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { storage } from '@/utils/storage.js'
import { usePlatformState } from '@/mock/platformState.js'

const REDIRECT_KEY = 'postLoginRedirect'

export const useUserStore = defineStore('user', () => {
  const { getUserById, sanitizeUser, loginWithPassword, registerAccount, ensureUserById } = usePlatformState()

  const token = ref(storage.get('token') || '')
  const userId = ref(storage.get('userId') || '')
  const userInfo = ref(null)

  function setAuth(newToken, newUserId) {
    token.value = newToken
    userId.value = String(newUserId)
    storage.set('token', newToken)
    storage.set('userId', String(newUserId))
    syncCurrentUser()
  }

  function setUserInfo(info) {
    userInfo.value = info
  }

  function syncCurrentUser() {
    if (!userId.value) {
      userInfo.value = null
      return null
    }
    const user = getUserById(userId.value) || ensureUserById(userId.value)
    userInfo.value = sanitizeUser(user)
    return userInfo.value
  }

  async function login(form) {
    const res = loginWithPassword(form)
    setAuth(res.token, res.userId)
    return res
  }

  async function register(form) {
    const res = registerAccount(form)
    setAuth(res.token, res.userId)
    return res
  }

  function logout() {
    token.value = ''
    userId.value = ''
    userInfo.value = null
    storage.remove('token')
    storage.remove('userId')
    storage.remove(REDIRECT_KEY)
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
  const isAdmin = computed(() => userInfo.value?.role === 'admin')

  if (token.value && userId.value) {
    syncCurrentUser()
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
    login,
    register,
    logout,
    setPostLoginRedirect,
    consumePostLoginRedirect
  }
})
