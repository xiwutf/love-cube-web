/**
 * Programmatic showActionSheet for Vant 4.
 * Vant 4 dropped the imperative showActionSheet function; this shim
 * mounts a temporary ActionSheet component and returns a Promise.
 */
import { ActionSheet } from 'vant'
import { createApp, h, ref } from 'vue'

export function showActionSheet({ title = '', actions = [], cancelText = '取消' } = {}) {
  return new Promise((resolve, reject) => {
    const container = document.createElement('div')
    document.body.appendChild(container)

    let settled = false
    let app = null

    function cleanup() {
      setTimeout(() => {
        if (app) { app.unmount(); app = null }
        if (container.parentNode) container.parentNode.removeChild(container)
      }, 300)
    }

    function done(action) {
      if (settled) return
      settled = true
      cleanup()
      resolve(action)
    }

    function cancel() {
      if (settled) return
      settled = true
      cleanup()
      reject(new Error('cancel'))
    }

    app = createApp({
      setup() {
        const visible = ref(true)
        return () => h(ActionSheet, {
          show:        visible.value,
          title,
          actions,
          cancelText,
          onSelect:         (action) => { visible.value = false; done(action) },
          onCancel:         ()       => { visible.value = false; cancel() },
          onClickOverlay:   ()       => { visible.value = false; cancel() },
          'onUpdate:show':  (v)      => { if (!v && !settled) cancel() },
        })
      },
    })

    app.mount(container)
  })
}
