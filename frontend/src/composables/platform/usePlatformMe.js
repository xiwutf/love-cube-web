import { computed, ref } from 'vue'
import { getMyGrowth } from '@/api/growth.js'
import { getMyInviteCode } from '@/api/invite.js'
import { getNotifUnreadCountCached } from '@/api/notification.js'
import { getUserStatsCached } from '@/api/user.js'
import { useUserStore } from '@/stores/user.js'

export function usePlatformMe() {
  const userStore = useUserStore()
  const loading = ref(false)
  const inviteCode = ref('')
  const stats = ref({ contentCount: 0, eventCount: 0, favoriteCount: 0 })
  const growth = ref(null)
  const unread = ref(0)

  const user = computed(() => userStore.userInfo)
  const growthLevel = computed(() => ({
    level: Number(growth.value?.level ?? 1),
    name: growth.value?.title || '新手用户',
    exp: Number(growth.value?.exp ?? 0),
    nextExp: Number(growth.value?.nextLevelExp ?? 100)
  }))

  async function refresh() {
    loading.value = true
    if (!user.value) {
      await userStore.refreshCurrentUser().catch(() => null)
    }

    const [inviteRes, statsRes, growthRes, unreadRes] = await Promise.allSettled([
      getMyInviteCode(),
      getUserStatsCached(),
      getMyGrowth(),
      getNotifUnreadCountCached()
    ])

    inviteCode.value = inviteRes.status === 'fulfilled' ? String(inviteRes.value?.inviteCode || inviteRes.value?.code || '') : ''
    stats.value = statsRes.status === 'fulfilled' ? (statsRes.value || stats.value) : stats.value
    growth.value = growthRes.status === 'fulfilled' ? (growthRes.value || null) : null
    unread.value = unreadRes.status === 'fulfilled' ? Number(unreadRes.value?.count ?? unreadRes.value?.unreadCount ?? 0) : 0
    loading.value = false
  }

  return { loading, user, inviteCode, stats, growth, growthLevel, unread, refresh }
}
