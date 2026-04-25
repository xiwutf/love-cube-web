import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import {
  fetchFellowshipHome,
  fetchFellowshipMatches,
  fetchFellowshipUserDetail,
  fetchFellowshipUsers,
  getMyFellowshipProfile,
  sendFellowshipInterest,
  updateMyFellowshipProfile
} from '@/api/fellowship.js'
import { normalizeUser } from '@/utils/normalizeUser.js'

export const useFellowshipStore = defineStore('fellowship', () => {
  const myProfile = ref(null)
  const candidates = ref([])
  const matches = ref([])
  const announcements = ref([])
  const events = ref([])
  const loading = ref(false)

  async function refreshMyProfile() {
    const data = await getMyFellowshipProfile()
    myProfile.value = normalizeUser(data)
    return myProfile.value
  }

  async function saveMyProfile(payload) {
    const data = await updateMyFellowshipProfile(payload)
    myProfile.value = normalizeUser(data)
    return myProfile.value
  }

  async function loadUsers(params = {}) {
    const data = await fetchFellowshipUsers(params)
    candidates.value = (data?.list || []).map(normalizeUser)
    return candidates.value
  }

  async function loadUserDetail(userId) {
    const data = await fetchFellowshipUserDetail(userId)
    return normalizeUser(data)
  }

  async function interestUser(userId) {
    return sendFellowshipInterest(userId)
  }

  async function loadMatches() {
    const data = await fetchFellowshipMatches()
    matches.value = (Array.isArray(data) ? data : []).map(normalizeUser)
    return matches.value
  }

  async function loadHome() {
    loading.value = true
    try {
      const data = await fetchFellowshipHome()
      announcements.value = data?.announcements || []
      events.value = data?.events || []
      candidates.value = (data?.recommends || []).map(normalizeUser)
      return data
    } finally {
      loading.value = false
    }
  }

  const hasMyProfile = computed(() => !!myProfile.value?.userId)

  return {
    myProfile,
    candidates,
    matches,
    announcements,
    events,
    loading,
    hasMyProfile,
    refreshMyProfile,
    saveMyProfile,
    loadUsers,
    loadUserDetail,
    interestUser,
    loadMatches,
    loadHome
  }
})

