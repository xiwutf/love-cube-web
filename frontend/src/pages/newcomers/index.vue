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
import { getNewcomers } from '@/api/home.js'
import { useInfiniteScroll } from '@/composables/useInfiniteScroll.js'
import { normalizeUser } from '@/utils/normalizeUser.js'
import NavBar   from '@/components/NavBar.vue'
import UserCard from '@/components/UserCard.vue'

const router     = useRouter()
const refreshing = ref(false)

const { list: rawList, loading, noMore, load, reload } = useInfiniteScroll(
  async (page, size) => {
    const data = await getNewcomers()
    // 后端目前不分页，直接返回数组
    return (Array.isArray(data) ? data : []).map(normalizeUser)
  }
)

// 使用 rawList 直接作为 list（已经是 normalized）
const list = rawList

async function onRefresh() {
  await reload()
  refreshing.value = false
}

function goProfile(id) {
  router.push(`/user-profile/${id}`)
}
</script>

<style scoped>
.newcomers-page { min-height: 100vh; background: #f8f8f8; }
</style>
