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

  async function fetchProfile() {
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

  async function fetchCompletion() {
    const data = await getFellowshipProfileCompletion()
    completion.value = data
    return data
  }

  return {
    profile,
    completion,
    loading,
    saving,
    fetchProfile,
    saveProfile,
    fetchCompletion
  }
})

