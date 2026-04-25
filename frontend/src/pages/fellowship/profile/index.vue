<template>
  <div class="fellowship-profile-page">
    <NavBar title="联谊资料" />

    <div v-if="store.loading" class="loading-wrap">
      <van-loading color="#ff6b8a" />
    </div>

    <template v-else>
      <div class="profile-card">
        <div class="row"><span>昵称</span><span>{{ profile.nickname || '-' }}</span></div>
        <div class="row"><span>性别</span><span>{{ profile.gender || '-' }}</span></div>
        <div class="row"><span>年龄</span><span>{{ profile.age || '-' }}</span></div>
        <div class="row"><span>城市</span><span>{{ profile.city || '-' }}</span></div>
        <div class="row"><span>职业</span><span>{{ profile.occupation || '-' }}</span></div>
        <div class="row"><span>学历</span><span>{{ profile.education || '-' }}</span></div>
        <div class="row"><span>身高</span><span>{{ profile.height ? `${profile.height}cm` : '-' }}</span></div>
        <div class="row"><span>交友意向</span><span>{{ profile.intention || '-' }}</span></div>
        <div class="row"><span>资料状态</span><span>{{ profile.profileStatus || 'INCOMPLETE' }}</span></div>
      </div>

      <div class="completion-card">
        <div class="completion-header">
          <span>完整度</span>
          <strong>{{ completion.percent || 0 }}%</strong>
        </div>
        <van-progress :percentage="completion.percent || 0" color="#ff6b8a" stroke-width="8" />
        <p v-if="completion.missingFields?.length" class="missing">
          缺少字段：{{ completion.missingFields.join('、') }}
        </p>
      </div>

      <div class="action-wrap">
        <van-button round block type="primary" @click="router.push('/fellowship/profile/edit')">
          编辑资料
        </van-button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import NavBar from '@/components/NavBar.vue'
import { useFellowshipProfileStore } from '@/stores/fellowshipProfile.js'

const router = useRouter()
const store = useFellowshipProfileStore()
const profile = computed(() => store.profile || {})
const completion = computed(() => store.completion || { percent: 0, missingFields: [] })

onMounted(async () => {
  await Promise.all([store.fetchProfile(), store.fetchCompletion()])
})
</script>

<style scoped>
.fellowship-profile-page { min-height: 100vh; background: #f8f8f8; padding-bottom: 24px; }
.loading-wrap { display: flex; justify-content: center; padding-top: 100px; }
.profile-card, .completion-card {
  margin: 12px;
  background: #fff;
  border-radius: 12px;
  padding: 14px;
}
.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f2f2f2;
  font-size: 14px;
}
.row:last-child { border-bottom: none; }
.completion-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 14px;
}
.missing {
  margin-top: 10px;
  font-size: 12px;
  color: #888;
}
.action-wrap { margin: 16px 12px; }
</style>

