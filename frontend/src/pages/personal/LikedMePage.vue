<template>
  <div class="list-page">
    <NavBar title="喜欢我的人" />
    <div v-if="loading" class="loading-wrap"><van-loading size="20" /></div>
    <template v-else>
      <UserCard
        v-for="u in list"
        :key="u.userId"
        :user="u"
        @click="goProfile(u.userId)"
      >
        <template #action>
          <van-button size="small" type="primary" @click.stop="goChat(u.userId)">去聊天</van-button>
        </template>
      </UserCard>
      <van-empty v-if="!list.length" description="暂时还没有人喜欢你" />
    </template>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import NavBar from '@/components/NavBar.vue'
import UserCard from '@/components/UserCard.vue'
import { getLikedMeUsers } from '@/api/personal.js'
import { normalizeUser } from '@/utils/normalizeUser.js'

const router = useRouter()
const loading = ref(false)
const list = ref([])

async function loadList() {
  loading.value = true
  try {
    const data = await getLikedMeUsers()
    list.value = (Array.isArray(data) ? data : []).map((item) => normalizeUser(item)).filter(Boolean)
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '加载失败' })
  } finally {
    loading.value = false
  }
}

function goProfile(userId) {
  router.push(`/fellowship/user-profile/${userId}`)
}

function goChat(userId) {
  router.push(`/fellowship/chat/${userId}`)
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

