export const storage = {
  get:    (key)        => localStorage.getItem(key),
  set:    (key, value) => localStorage.setItem(key, String(value)),
  remove: (key)        => localStorage.removeItem(key),
  clear:  ()           => localStorage.clear()
}

export function toast(options) {
  const { showToast } = window.__vant__ || {}
  if (!showToast) {
    const msg = typeof options === 'string' ? options : options?.message
    alert(msg); return
  }
  if (typeof options === 'string') showToast({ message: options, duration: 2000 })
  else showToast({ duration: 2000, ...options })
}
