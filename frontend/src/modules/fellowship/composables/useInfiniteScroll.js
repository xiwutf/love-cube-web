import { ref } from 'vue'

export function useInfiniteScroll(fetchFn, pageSize = 10) {
  const list    = ref([])
  const loading = ref(false)
  const noMore  = ref(false)
  let   page    = 1

  async function load() {
    if (loading.value || noMore.value) return
    loading.value = true
    try {
      const data = await fetchFn(page, pageSize)
      const items = Array.isArray(data) ? data : (data?.list ?? data?.content ?? [])
      list.value.push(...items)
      if (items.length < pageSize) noMore.value = true
      else page++
    } catch {
      // silent fail
    } finally {
      loading.value = false
    }
  }

  async function reload() {
    page          = 1
    list.value    = []
    noMore.value  = false
    await load()
  }

  reload()

  return { list, loading, noMore, load, reload }
}
