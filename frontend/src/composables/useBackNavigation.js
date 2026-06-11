import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

function getLayerFallback(path) {
  if (path.startsWith('/m/platform')) {
    if (path === '/m/platform' || path === '/m/platform/') {
      return '/fellowship/discover'
    }
    return '/m/platform'
  }
  if (path.startsWith('/pc/platform')) {
    if (path === '/pc/platform/play' || path === '/pc/platform/play/') {
      return '/pc/platform'
    }
    return '/pc/platform/play'
  }
  if (path.startsWith('/fellowship')) return '/fellowship/discover'
  if (path.startsWith('/admin')) return '/admin'
  return '/'
}

function canNavigateBack() {
  const back = window.history.state?.back
  if (typeof back === 'string' && back.length > 0) {
    return true
  }
  return window.history.length > 1
}

export function useBackNavigation(defaultTo = '') {
  const route = useRoute()
  const router = useRouter()

  const fallbackPath = computed(() => defaultTo || getLayerFallback(route.path))

  function goBack(explicitTo = '') {
    if (explicitTo) {
      router.push(explicitTo)
      return
    }

    if (canNavigateBack()) {
      router.back()
      return
    }

    const fallback = fallbackPath.value
    if (fallback && route.path !== fallback) {
      router.push(fallback)
    }
  }

  return {
    fallbackPath,
    canNavigateBack,
    goBack
  }
}
