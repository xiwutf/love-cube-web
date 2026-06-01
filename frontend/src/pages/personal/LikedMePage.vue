<template>
  <div class="list-page">
    <NavBar title="喜欢我的人" />
    <div v-if="loading" class="loading-wrap"><van-loading size="20" /></div>
    <template v-else>
      <div v-if="locked" class="vip-banner" @click="router.push('/fellowship/vip')">
        <p>有 {{ totalCount }} 人喜欢你</p>
        <span>开通 VIP 查看完整名单</span>
      </div>
      <UserCard
        v-for="u in list"
        :key="u.userId || u.createdAt"
        :user="u"
        @click="onProfile(u)"
      >
        <template #action>
          <van-button v-if="!u.locked && u.userId" size="small" type="primary" @click.stop="goChat(u.userId)">去聊天</van-button>
        </template>
      </UserCard>
      <van-empty v-if="!list.length && !locked" description="暂时还没有人喜欢你" />
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
const locked = ref(false)
const totalCount = ref(0)

async function loadList() {
  loading.value = true
  try {
    const data = await getLikedMeUsers()
    if (Array.isArray(data)) {
      list.value = data.map((item) => normalizeUser(item)).filter(Boolean)
      locked.value = false
      totalCount.value = list.value.length
      return
    }
    locked.value = Boolean(data?.locked && !data?.vipActive)
    totalCount.value = Number(data?.totalCount || 0)
    const items = Array.isArray(data?.items) ? data.items : []
    list.value = items.map((item) => normalizeUser(item)).filter(Boolean)
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '加载失败' })
  } finally {
    loading.value = false
  }
}

function onProfile(user) {
  if (user?.locked || !user?.userId) {
    showToast('开通 VIP 后可查看资料')
    router.push('/fellowship/vip')
    return
  }
  goProfile(user.userId)
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

.vip-banner {
  margin: 12px;
  padding: 14px 16px;
  border-radius: 12px;
  background: linear-gradient(135deg, #fff7ed, #ffedd5);
  border: 1px solid #fdba74;
}

.vip-banner p {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: #9a3412;
}

.vip-banner span {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #c2410c;
}
</style>
