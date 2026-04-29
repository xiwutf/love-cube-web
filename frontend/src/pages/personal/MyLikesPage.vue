<template>
  <div class="list-page">
    <NavBar title="我的喜欢" />

    <van-tabs v-model:active="activeTab" color="#ff6b9b" title-active-color="#ff6b9b">
      <van-tab title="我的喜欢">
        <div v-if="loadingSent" class="loading-wrap"><van-loading size="20" /></div>
        <template v-else>
          <UserCard
            v-for="u in sentLikes"
            :key="u.userId"
            :user="u"
            @click="goProfile(u.userId)"
          >
            <template #action>
              <van-button size="small" plain type="primary" @click.stop="cancelLike(u)">取消喜欢</van-button>
            </template>
          </UserCard>
          <van-empty v-if="!sentLikes.length" description="还没有喜欢的" />
        </template>
      </van-tab>

      <van-tab title="互相喜欢">
        <div v-if="loadingMutual" class="loading-wrap"><van-loading size="20" /></div>
        <template v-else>
          <UserCard
            v-for="u in mutualLikes"
            :key="u.userId"
            :user="u"
            @click="goProfile(u.userId)"
          >
            <template #action>
              <van-button size="small" type="primary" @click.stop="goChat(u.userId)">去聊天</van-button>
            </template>
          </UserCard>
          <van-empty v-if="!mutualLikes.length" description="暂时还没有互相喜欢" />
        </template>
      </van-tab>
    </van-tabs>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import NavBar from '@/components/NavBar.vue'
import UserCard from '@/components/UserCard.vue'
import { likeUser } from '@/api/match.js'
import { getMyLikeUsers, getMutualLikeUsers } from '@/api/personal.js'
import { normalizeUser } from '@/utils/normalizeUser.js'

const route = useRoute()
const router = useRouter()
const activeTab = ref(route.query.tab === 'mutual' ? 1 : 0)

const loadingSent = ref(false)
const loadingMutual = ref(false)
const sentLikes = ref([])
const mutualLikes = ref([])

function mapUsers(list) {
  return (Array.isArray(list) ? list : []).map((item) => normalizeUser(item)).filter(Boolean)
}

async function loadSent() {
  loadingSent.value = true
  try {
    sentLikes.value = mapUsers(await getMyLikeUsers())
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '加载失败' })
  } finally {
    loadingSent.value = false
  }
}

async function loadMutual() {
  loadingMutual.value = true
  try {
    mutualLikes.value = mapUsers(await getMutualLikeUsers())
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '加载失败' })
  } finally {
    loadingMutual.value = false
  }
}

async function cancelLike(user) {
  try {
    await likeUser(user.userId)
    sentLikes.value = sentLikes.value.filter((u) => String(u.userId) !== String(user.userId))
    mutualLikes.value = mutualLikes.value.filter((u) => String(u.userId) !== String(user.userId))
    showToast({ type: 'success', message: '已取消喜欢' })
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '操作失败' })
  }
}

function goProfile(userId) {
  router.push(`/fellowship/user-profile/${userId}`)
}

function goChat(userId) {
  router.push(`/fellowship/chat/${userId}`)
}

onMounted(async () => {
  await Promise.allSettled([loadSent(), loadMutual()])
})
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

