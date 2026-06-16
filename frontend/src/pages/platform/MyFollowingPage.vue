<template>
  <PlatformSocialListPage
    title="我的关注"
    subtitle="你主动关注的人，可在联谊模块查看资料或发消息"
    empty-text="还没有关注任何人，去联谊发现页看看吧"
    :loading="loading"
    :error="error"
    :users="list"
    action-label="取消关注"
    :action-busy-id="busyId"
    @action="unfollow"
  />
</template>

<script setup>
import { onMounted, ref } from 'vue'
import PlatformSocialListPage from '@/components/platform/me/PlatformSocialListPage.vue'
import { getFollowingUsers, toggleFollowUser } from '@/api/personal.js'
import { normalizeUser } from '@/utils/normalizeUser.js'

const loading = ref(false)
const error = ref('')
const list = ref([])
const busyId = ref('')

async function loadList() {
  loading.value = true
  error.value = ''
  try {
    const data = await getFollowingUsers()
    list.value = (Array.isArray(data) ? data : []).map((item) => normalizeUser(item)).filter(Boolean)
  } catch (e) {
    error.value = e?.message || '加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

async function unfollow(user) {
  if (!user?.userId) return
  busyId.value = user.userId
  try {
    await toggleFollowUser(user.userId)
    list.value = list.value.filter((row) => String(row.userId) !== String(user.userId))
  } catch (e) {
    error.value = e?.message || '取消关注失败'
  } finally {
    busyId.value = ''
  }
}

onMounted(loadList)
</script>
