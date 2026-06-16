<template>
  <PlatformSocialListPage
    title="获赞记录"
    subtitle="向你表达喜欢的人（喜欢 / 超级喜欢）"
    empty-text="暂时还没有人向你表达喜欢"
    :loading="loading"
    :error="error"
    :users="list"
    :locked="locked"
    :locked-count="totalCount"
  />
</template>

<script setup>
import { onMounted, ref } from 'vue'
import PlatformSocialListPage from '@/components/platform/me/PlatformSocialListPage.vue'
import { getLikedMeUsers } from '@/api/personal.js'
import { normalizeUser } from '@/utils/normalizeUser.js'
import { FELLOWSHIP_VIP_COMMERCE_ENABLED } from '@/constants/fellowshipCommerce.js'

const showVipCommerce = FELLOWSHIP_VIP_COMMERCE_ENABLED
const loading = ref(false)
const error = ref('')
const list = ref([])
const locked = ref(false)
const totalCount = ref(0)

async function loadList() {
  loading.value = true
  error.value = ''
  try {
    const data = await getLikedMeUsers()
    if (Array.isArray(data)) {
      list.value = data.map((item) => normalizeUser(item)).filter(Boolean)
      locked.value = false
      totalCount.value = list.value.length
      return
    }
    locked.value = showVipCommerce && Boolean(data?.locked && !data?.vipActive)
    totalCount.value = Number(data?.totalCount || 0)
    const items = Array.isArray(data?.items) ? data.items : []
    list.value = items.map((item) => normalizeUser(item)).filter(Boolean)
  } catch (e) {
    error.value = e?.message || '加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

onMounted(loadList)
</script>
