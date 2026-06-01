import { computed } from 'vue'
import { useRoute } from 'vue-router'

/**
 * 联谊 Tab / 跳转：在 /m/fellowship/* 与 /fellowship/* 之间自动选前缀
 */
export function useFellowshipNavBase() {
  const route = useRoute()

  const base = computed(() =>
    route.path.startsWith('/m/fellowship') ? '/m/fellowship' : '/fellowship'
  )

  function fellowshipPath(suffix = '') {
    if (!suffix) return base.value
    const normalized = suffix.startsWith('/') ? suffix : `/${suffix}`
    return `${base.value}${normalized}`
  }

  return { base, fellowshipPath }
}
