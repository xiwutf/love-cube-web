import { computed, ref, unref } from 'vue'
import { useUserStore } from '@/stores/user.js'

const RECENT_ACTIVE_DAYS = 7

export const INTEREST_FILTER_OPTIONS = [
  { key: 'all', label: '全部' },
  { key: 'sameCity', label: '同城' },
  { key: 'verified', label: '已认证' },
  { key: 'recentActive', label: '最近活跃' }
]

function parseTime(value) {
  if (!value) return null
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? null : date
}

function isSameCity(myLocation, theirLocation) {
  const mine = String(myLocation || '').trim()
  const theirs = String(theirLocation || '').trim()
  if (!mine || !theirs) return false
  if (mine === theirs) return true
  const myCity = mine.split(/[\s/·,，]/)[0]
  const theirCity = theirs.split(/[\s/·,，]/)[0]
  return myCity && theirCity && (mine.includes(theirCity) || theirs.includes(myCity))
}

function isVerifiedUser(item) {
  return Boolean(item?.photoVerified || item?.realnameVerified)
}

function isRecentlyActive(item) {
  const activeAt = parseTime(item?.lastActiveAt || item?.updatedAt)
  if (!activeAt) return false
  const cutoff = Date.now() - RECENT_ACTIVE_DAYS * 24 * 60 * 60 * 1000
  return activeAt.getTime() >= cutoff
}

function pickLocation(item) {
  return item?.location
    || item?.visitor?.location
    || ''
}

function normalizeInterestItem(item) {
  if (!item) return null
  return {
    ...item,
    location: pickLocation(item),
    photoVerified: Boolean(item.photoVerified ?? item.visitor?.photoVerified),
    realnameVerified: Boolean(item.realnameVerified ?? item.visitor?.realnameVerified),
    lastActiveAt: item.lastActiveAt || item.updatedAt || item.visitTime || item.createdAt || null
  }
}

export function useFellowshipInterestFilter(sourceList) {
  const userStore = useUserStore()
  const activeFilter = ref('all')

  const myLocation = computed(() => userStore.userInfo?.location || '')

  const filteredList = computed(() => {
    const raw = unref(sourceList)
    const list = Array.isArray(raw) ? raw.map(normalizeInterestItem).filter(Boolean) : []
    if (activeFilter.value === 'all') return list

    return list.filter((item) => {
      if (activeFilter.value === 'sameCity') {
        return isSameCity(myLocation.value, pickLocation(item))
      }
      if (activeFilter.value === 'verified') {
        return isVerifiedUser(item)
      }
      if (activeFilter.value === 'recentActive') {
        return isRecentlyActive(item)
      }
      return true
    })
  })

  const filterEmptyHint = computed(() => {
    if (activeFilter.value === 'all') return ''
    const option = INTEREST_FILTER_OPTIONS.find((o) => o.key === activeFilter.value)
    return `当前「${option?.label || ''}」条件下暂无匹配，可切换其他筛选`
  })

  return {
    activeFilter,
    filteredList,
    filterEmptyHint,
    INTEREST_FILTER_OPTIONS
  }
}
