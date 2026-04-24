import axios from 'axios'
import { storage } from '@/utils/storage.js'

/**
 * 统一请求层
 * baseURL：由环境变量 VITE_API_BASE_URL 控制
 *   开发：/admin  （Vite proxy 转发到后端）
 *   生产：http://xifg.com.cn:8090/admin
 */
const request = axios.create({
  baseURL: (import.meta.env.VITE_API_BASE_URL || '') + '/api',
  timeout: 10000
})

// 请求拦截：自动注入 token
request.interceptors.request.use(config => {
  const token = storage.get('token')
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`
  }
  return config
})

// 响应拦截：统一错误处理
request.interceptors.response.use(
  res => res.data,
  err => {
    const status  = err.response?.status
    const message = err.response?.data?.message || err.message || '请求失败'

    if (status === 401) {
      storage.remove('token')
      storage.remove('userId')
      // 跳转登录页（延迟避免在请求回调中直接操作路由）
      setTimeout(() => { window.location.hash = '#/login' }, 100)
    }

    return Promise.reject(new Error(message))
  }
)

export default request
