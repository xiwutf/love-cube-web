import { defineStore } from 'pinia'
import { ref } from 'vue'
import { storage } from '@f/utils/storage.js'

export const useUserStore = defineStore('fellowship-user', () => {
  const token    = ref(storage.get('token') || '')
  const userId   = ref(storage.get('userId') || '')
  const userInfo = ref(null)

  function setAuth(newToken, newUserId) {
    token.value  = newToken
    userId.value = String(newUserId)
    storage.set('token',  newToken)
    storage.set('userId', String(newUserId))
  }

  function setUserInfo(info) {
    userInfo.value = info
  }

  function logout() {
    token.value    = ''
    userId.value   = ''
    userInfo.value = null
    storage.remove('token')
    storage.remove('userId')
  }

  const isLoggedIn = () => !!token.value

  return { token, userId, userInfo, setAuth, setUserInfo, logout, isLoggedIn }
})
