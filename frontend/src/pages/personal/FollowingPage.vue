<template>
  <div class="list-page">
    <NavBar title="关注列表" />
    <div v-if="loading" class="loading-wrap"><van-loading size="20" /></div>
    <template v-else>
      <UserCard
        v-for="u in list"
        :key="u.userId"
        :user="u"
        @click="goProfile(u.userId)"
      >
        <template #action>
          <van-button size="small" plain type="primary" @click.stop="toggleFollow(u)">取消关注</van-button>
        </template>
      </UserCard>
      <van-empty v-if="!list.length" description="还没有关注任何人" />
    </template>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import NavBar from '@/components/NavBar.vue'
import UserCard from '@/components/UserCard.vue'
import { getFollowingUsers, toggleFollowUser } from '@/api/personal.js'
import { normalizeUser } from '@/utils/normalizeUser.js'

const router = useRouter()
const loading = ref(false)
const list = ref([])

async function loadList() {
  loading.value = true
  try {
    const data = await getFollowingUsers()
    list.value = (Array.isArray(data) ? data : []).map((item) => normalizeUser(item)).filter(Boolean)
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '加载失败' })
  } finally {
    loading.value = false
  }
}

async function toggleFollow(user) {
  try {
    await toggleFollowUser(user.userId)
    list.value = list.value.filter((u) => String(u.userId) !== String(user.userId))
    showToast({ type: 'success', message: '已取消关注' })
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '操作失败' })
  }
}

function goProfile(userId) {
  router.push(`/fellowship/user-profile/${userId}`)
}

onMounted(loadList)
</script>

<style scoped>
.list-page {
  min-height: 100vh;
  background: #f8f9fb;
}

.loading-wrap {
  display: flex;
  justify-content: center;
  padding: 32px 0;
}
</style>

