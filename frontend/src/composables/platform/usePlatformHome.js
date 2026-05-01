import { computed, ref } from 'vue'
import { fetchAnnouncements, fetchArticles, fetchEvents, fetchPlatformStats } from '@/api/platformContent.js'

export function usePlatformHome() {
  const loading = ref(false)
  const announcements = ref([])
  const articles = ref([])
  const events = ref([])
  const stats = ref({})

  const heroNotice = computed(() => announcements.value[0] || null)

  async function refresh() {
    loading.value = true
    const [annRes, artRes, eventRes, statsRes] = await Promise.allSettled([
      fetchAnnouncements({ status: 'published', limit: 6 }),
      fetchArticles({ status: 'published', limit: 8 }),
      fetchEvents({ status: 'published', limit: 6 }),
      fetchPlatformStats()
    ])

    announcements.value = annRes.status === 'fulfilled' ? (Array.isArray(annRes.value) ? annRes.value : annRes.value?.data || []) : []
    articles.value = artRes.status === 'fulfilled' ? (Array.isArray(artRes.value) ? artRes.value : artRes.value?.data || []) : []
    events.value = eventRes.status === 'fulfilled' ? (Array.isArray(eventRes.value) ? eventRes.value : eventRes.value?.data || []) : []
    stats.value = statsRes.status === 'fulfilled' ? (statsRes.value || {}) : {}
    loading.value = false
  }

  return { loading, announcements, articles, events, stats, heroNotice, refresh }
}
