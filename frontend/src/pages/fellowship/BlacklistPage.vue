<template>
  <div class="blacklist-page">
    <NavBar title="榛戝悕鍗" />

    <div v-if="loading" class="page-loading">
      <van-loading type="spinner" color="#FF6B8A" />
    </div>

    <section v-else class="content">
      <template v-if="list.length">
        <article v-for="item in list" :key="item.blockedUserId" class="row">
          <div class="user-info">
            <van-image
              v-if="item.avatar"
              round width="40" height="40"
              :src="item.avatar" fit="cover"
              class="avatar"
            />
            <div class="avatar-fallback" v-else>
              {{ (item.nickname || '?')[0] }}
            </div>
            <div>
              <p class="name">{{ item.nickname || `用户${item.blockedUserId}` }}</p>
              <p class="meta">ID {{ item.blockedUserId }}</p>
            </div>
          </div>
          <van-button
            size="small"
            plain
            type="primary"
            :loading="removing === item.blockedUserId"
            @click="remove(item.blockedUserId)"
          >
            瑙ｉ櫎鎷夐粦
          </van-button>
        </article>
      </template>
      <van-empty v-else description="榛戝悕鍗曚负绌" />
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { showToast } from 'vant'
import NavBar from '@/components/NavBar.vue'
import { getMyBlacklist, unblockUser } from '@/api/fellowship.js'

const loading  = ref(true)
const removing = ref(null)
const list     = ref([])

onMounted(async () => {
  try {
    list.value = await getMyBlacklist()
  } catch {
    showToast({ message: '加载失败', type: 'fail' })
  } finally {
    loading.value = false
  }
})

async function remove(userId) {
  removing.value = userId
  try {
    await unblockUser(userId)
    list.value = list.value.filter(item => item.blockedUserId !== userId)
    showToast({ type: 'success', message: '已解除拉黑' })
  } catch {
    showToast({ message: 'ʧܣ', type: 'fail' })
  } finally {
    removing.value = null
  }
}
</script>

<style scoped>
.blacklist-page {
  min-height: 100vh;
  background: #f7f9fc;
}

.page-loading {
  display: flex;
  justify-content: center;
  padding-top: 80px;
}

.content {
  padding: 12px;
}

.row {
  background: #fff;
  border-radius: 14px;
  padding: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar { flex-shrink: 0; }

.avatar-fallback {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF6B8A, #FFB3C1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: #fff;
  font-weight: 700;
  flex-shrink: 0;
}

.name {
  margin: 0;
  font-size: 15px;
  color: #111827;
}

.meta {
  margin: 4px 0 0;
  font-size: 12px;
  color: #9ca3af;
}
</style>

