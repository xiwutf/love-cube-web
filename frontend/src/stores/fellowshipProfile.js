import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  getFellowshipProfileCompletion,
  getMyFellowshipProfile,
  updateMyFellowshipProfile
} from '@/api/fellowshipProfile.js'

export const useFellowshipProfileStore = defineStore('fellowship-profile', () => {
  const profile = ref(null)
  const completion = ref({ completed: false, percent: 0, missingFields: [] })
  const loading = ref(false)
  const saving = ref(false)

  async function fetchProfile(force = false) {
    if (!force && profile.value) return profile.value
    loading.value = true
    try {
      const data = await getMyFellowshipProfile()
      profile.value = data
      return data
    } finally {
      loading.value = false
    }
  }

  async function saveProfile(data) {
    saving.value = true
    try {
      const res = await updateMyFellowshipProfile(data)
      profile.value = res
      return res
    } finally {
      saving.value = false
    }
  }

  async function fetchCompletion(force = false) {
    if (!force && completion.value.percent > 0) return completion.value
    const data = await getFellowshipProfileCompletion()
    completion.value = data
    return data
  }

  function clearProfile() {
    profile.value = null
    completion.value = { completed: false, percent: 0, missingFields: [] }
  }

  return {
    profile,
    completion,
    loading,
    saving,
    fetchProfile,
    saveProfile,
    fetchCompletion,
    clearProfile
  }
})

