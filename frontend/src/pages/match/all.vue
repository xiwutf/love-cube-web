<template>
  <div class="all-users-page">
    <NavBar title="全部异性" />
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="loadMore"
      >
        <UserCard
          v-for="user in users"
          :key="user.userId"
          :user="user"
          @click="goProfile(user.userId)"
        >
          <template #action>
            <van-tag plain type="primary" @click.stop="goProfile(user.userId)">查看</van-tag>
          </template>
        </UserCard>
        <van-empty v-if="!loading && !users.length" description="暂无可查看用户" />
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import NavBar from '@/components/NavBar.vue'
import UserCard from '@/components/UserCard.vue'
import { getOppositeGenderUsers } from '@/api/match.js'
import { normalizeUser } from '@/utils/normalizeUser.js'

const router = useRouter()
const users = ref([])
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)
const pager = ref({ page: 1, size: 20 })
/** van-list 触发 @load 前会把 loading 置为 true，不能用 loading 做「正在请求」守卫，否则会直接 return 且永远不执行 finally */
const fetching = ref(false)

async function loadMore() {
  if (finished.value || fetching.value) return
  fetching.value = true
  loading.value = true
  try {
    const res = await getOppositeGenderUsers({
      page: pager.value.page,
      size: pager.value.size
    })
    const list = (Array.isArray(res?.list) ? res.list : []).map(normalizeUser)
    const exists = new Set(users.value.map((item) => item.userId))
    users.value.push(...list.filter((item) => !exists.has(item.userId)))
    finished.value = !res?.hasMore || list.length === 0
    pager.value.page += 1
  } catch (error) {
    showToast({ type: 'fail', message: error?.message || '加载失败，请稍后重试' })
    finished.value = true
  } finally {
    fetching.value = false
    loading.value = false
  }
}

async function onRefresh() {
  users.value = []
  finished.value = false
  pager.value.page = 1
  await loadMore()
  refreshing.value = false
}

function goProfile(id) {
  router.push(`/fellowship/user-profile/${id}`)
}
</script>

<style scoped>
.all-users-page {
  min-height: 100vh;
  background: #f8f8f8;
}
</style>
