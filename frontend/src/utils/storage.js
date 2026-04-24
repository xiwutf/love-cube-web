/**
 * 平台适配层：替换微信小程序 wx.setStorageSync / wx.getStorageSync 等能力
 * H5 端统一使用 localStorage
 */
export const storage = {
  get:    (key)        => localStorage.getItem(key),
  set:    (key, value) => localStorage.setItem(key, String(value)),
  remove: (key)        => localStorage.removeItem(key),
  clear:  ()           => localStorage.clear()
}

/**
 * 简易 toast 替代 wx.showToast（依赖 Vant showToast，在 main.js 已全量注册）
 * 使用方式：toast('提示文字') 或 toast({ message: '...', type: 'fail' })
 */
export function toast(options) {
  const { showToast } = window.__vant__ || {}
  if (!showToast) {
    // 未挂载时降级为 alert
    const msg = typeof options === 'string' ? options : options?.message
    alert(msg)
    return
  }
  if (typeof options === 'string') {
    showToast({ message: options, duration: 2000 })
  } else {
    showToast({ duration: 2000, ...options })
  }
}
