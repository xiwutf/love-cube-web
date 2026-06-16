<template>
  <PlatformSocialListPage
    title="我的粉丝"
    subtitle="关注你的人会显示在这里"
    empty-text="还没有粉丝，多参与互动更容易被关注"
    :loading="loading"
    :error="error"
    :users="list"
    :action-resolver="actionLabelFor"
    :action-busy-id="busyId"
    @action="followBack"
  />
</template>

<script setup>
import { onMounted, ref } from 'vue'
import PlatformSocialListPage from '@/components/platform/me/PlatformSocialListPage.vue'
import { getFansUsers, getFollowingUsers, toggleFollowUser } from '@/api/personal.js'
import { normalizeUser } from '@/utils/normalizeUser.js'

const loading = ref(false)
const error = ref('')
const list = ref([])
const busyId = ref('')
const followingIds = ref(new Set())

function actionLabelFor(user) {
  if (!user?.userId) return ''
  if (followingIds.value.has(String(user.userId))) return ''
  return '回关'
}

async function loadList() {
  loading.value = true
  error.value = ''
  try {
    const [fansData, followingData] = await Promise.all([getFansUsers(), getFollowingUsers()])
    followingIds.value = new Set(
      (Array.isArray(followingData) ? followingData : [])
        .map((item) => String(item?.userId ?? item?.userid ?? item?.id ?? ''))
        .filter(Boolean)
    )
    list.value = (Array.isArray(fansData) ? fansData : []).map((item) => normalizeUser(item)).filter(Boolean)
  } catch (e) {
    error.value = e?.message || '加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

async function followBack(user) {
  if (!user?.userId) return
  if (followingIds.value.has(String(user.userId))) return
  busyId.value = user.userId
  try {
    await toggleFollowUser(user.userId)
    followingIds.value = new Set([...followingIds.value, String(user.userId)])
    error.value = ''
  } catch (e) {
    error.value = e?.message || '关注失败'
  } finally {
    busyId.value = ''
  }
}

onMounted(loadList)
</script>
