<template>
  <div class="newcomers-page">
    <NavBar title="新人推荐" />

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="noMore"
        finished-text="没有更多了"
        @load="load"
      >
        <UserCard
          v-for="u in list"
          :key="u.userId"
          :user="u"
          @click="goProfile(u.userId)"
        >
          <template #action>
            <van-tag plain type="primary" @click.stop="goProfile(u.userId)">查看</van-tag>
          </template>
        </UserCard>
        <van-empty v-if="!loading && !list.length" description="暂无新人" />
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { getNewcomers } from '@f/api/home.js'
import { useInfiniteScroll } from '@f/composables/useInfiniteScroll.js'
import { normalizeUser } from '@f/utils/normalizeUser.js'
import NavBar   from '@f/components/NavBar.vue'
import UserCard from '@f/components/UserCard.vue'

const router     = useRouter()
const refreshing = ref(false)

const { list: rawList, loading, noMore, load, reload } = useInfiniteScroll(
  async () => {
    const data = await getNewcomers()
    return (Array.isArray(data) ? data : []).map(normalizeUser)
  }
)

const list = rawList

async function onRefresh() {
  await reload()
  refreshing.value = false
}

function goProfile(id) {
  router.push(`/fellowship/user-profile/${id}`)
}
</script>

<style scoped>
.newcomers-page { min-height: 100vh; background: #f8f8f8; }
</style>
