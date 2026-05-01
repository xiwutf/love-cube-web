import { computed, ref } from 'vue'
import { fetchEvents, fetchEventsStats } from '@/api/platformContent.js'

export function usePlatformEvents() {
  const loading = ref(false)
  const list = ref([])
  const stats = ref({})

  const upcoming = computed(() => list.value.filter((item) => {
    const t = new Date(String(item.eventTime || item.time || '').replace(' ', 'T'))
    return Number.isFinite(t.getTime()) ? t.getTime() >= Date.now() : true
  }))

  async function refresh() {
    loading.value = true
    const [eventRes, statsRes] = await Promise.allSettled([fetchEvents({ status: 'published' }), fetchEventsStats()])
    list.value = eventRes.status === 'fulfilled' ? (Array.isArray(eventRes.value) ? eventRes.value : eventRes.value?.data || []) : []
    stats.value = statsRes.status === 'fulfilled' ? (statsRes.value || {}) : {}
    loading.value = false
  }

  return { loading, list, stats, upcoming, refresh }
}
