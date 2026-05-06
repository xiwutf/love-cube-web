import axios from 'axios'
import { storage } from '@/utils/storage.js'

const apiBase = import.meta.env.VITE_API_BASE_URL || '/admin'

const request = axios.create({
  baseURL: `${apiBase}/api`,
  timeout: 10000
})

request.interceptors.request.use((config) => {
  const token = storage.get('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (res) => res.data,
  (err) => {
    const status = err.response?.status
    const message = err.response?.data?.message || err.message || '请求失败'

    // 注册/登录成功后立刻请求 /users/me 若遇 401（如库同步延迟），不应清掉刚写入的新 token
    const cfg = err.config || {}
    if (status === 401 && !cfg.skip401Redirect) {
      storage.remove('token')
      storage.remove('userId')
      const currentHash = window.location.hash?.replace(/^#/, '') || '/'
      const encoded = encodeURIComponent(currentHash)
      setTimeout(() => {
        const loginPath = currentHash.startsWith('/fellowship') ? '/fellowship/login' : '/login'
        window.location.hash = `#${loginPath}?redirect=${encoded}`
      }, 100)
    }

    const requestError = new Error(message)
    requestError.status = status
    requestError.data = err.response?.data
    return Promise.reject(requestError)
  }
)

export default request
