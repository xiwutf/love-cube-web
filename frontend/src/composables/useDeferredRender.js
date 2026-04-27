import { onBeforeUnmount, onMounted, ref } from 'vue'

export function useDeferredRender(options = {}) {
  const {
    root = null,
    rootMargin = '200px 0px',
    threshold = 0
  } = options

  const mountRef = ref(null)
  const shouldRender = ref(false)
  let observer = null

  onMounted(() => {
    const target = mountRef.value
    if (!target) {
      shouldRender.value = true
      return
    }

    if (!('IntersectionObserver' in window)) {
      shouldRender.value = true
      return
    }

    observer = new IntersectionObserver((entries) => {
      const entry = entries[0]
      if (!entry?.isIntersecting) return
      shouldRender.value = true
      observer?.disconnect()
      observer = null
    }, { root, rootMargin, threshold })

    observer.observe(target)
  })

  onBeforeUnmount(() => {
    observer?.disconnect()
    observer = null
  })

  return { mountRef, shouldRender }
}
