import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

function getLayerFallback(path) {
  if (path.startsWith('/fellowship')) return '/fellowship/discover'
  if (path.startsWith('/admin')) return '/admin'
  return '/'
}

export function useBackNavigation(defaultTo = '') {
  const route = useRoute()
  const router = useRouter()

  const fallbackPath = computed(() => defaultTo || getLayerFallback(route.path))
  const previousPath = computed(() => window.history.state?.back || '')
  const hasPreviousRoute = computed(() => Boolean(previousPath.value))

  function goBack(to = '') {
    const target = to || defaultTo
    if (target) {
      router.push(target)
      return
    }

    if (hasPreviousRoute.value) {
      router.back()
      return
    }

    if (route.path !== fallbackPath.value) {
      router.push(fallbackPath.value)
    }
  }

  return {
    fallbackPath,
    hasPreviousRoute,
    goBack
  }
}
