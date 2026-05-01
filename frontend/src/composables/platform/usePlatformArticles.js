import { computed, ref } from 'vue'
import { fetchArticles } from '@/api/platformContent.js'

export function usePlatformArticles() {
  const loading = ref(false)
  const list = ref([])
  const category = ref('all')

  const filtered = computed(() => {
    if (category.value === 'all') return list.value
    return list.value.filter((item) => (item.category || item.tag) === category.value)
  })

  async function refresh() {
    loading.value = true
    const res = await fetchArticles({ status: 'published' }).catch(() => [])
    list.value = Array.isArray(res) ? res : res?.data || []
    loading.value = false
  }

  return { loading, list, category, filtered, refresh }
}
