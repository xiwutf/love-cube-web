<template>
  <section class="platform-page">
    <section class="platform-card platform-soft-card">
      <p class="platform-kicker">Account Center</p>
      <h1 class="platform-title">我的账号</h1>
      <p class="platform-subtitle">统一用户体系下，官网与联谊模块共享同一登录状态和个人资料。</p>
    </section>

    <section v-if="user" class="platform-card platform-block">
      <h3 class="platform-heading">基础信息</h3>
      <p class="platform-text">昵称：{{ user.username || '未设置' }}</p>
      <p class="platform-text">手机号：{{ user.phone || '未设置' }}</p>
      <p class="platform-text">账号角色：{{ user.role === 'admin' ? '管理员' : '普通用户' }}</p>
      <p class="platform-text">账号状态：{{ user.status === 'banned' ? '已封禁' : '正常' }}</p>
      <p class="platform-text">认证状态：{{ statusText }}</p>
      <p v-if="user.verificationRejectReason" class="platform-text">驳回原因：{{ user.verificationRejectReason }}</p>
    </section>

    <section v-if="user" class="platform-card platform-block">
      <h3 class="platform-heading">编辑个人资料</h3>
      <div class="submit-form">
        <input v-model="form.username" class="admin-input" placeholder="昵称" />
        <input v-model="form.location" class="admin-input" placeholder="所在地" />
        <input v-model="form.occupation" class="admin-input" placeholder="职业" />
        <input v-model="form.height" class="admin-input" placeholder="身高（cm）" />
        <textarea v-model="form.bio" class="admin-textarea" placeholder="个性签名 / 个人简介" />
      </div>
      <div class="platform-actions">
        <button class="platform-btn platform-btn-primary" type="button" @click="saveProfile">保存资料</button>
        <router-link to="/fellowship/me" class="platform-btn platform-btn-ghost">进入联谊-我的</router-link>
      </div>
    </section>
  </section>
</template>

<script setup>
import { computed, reactive, watch } from 'vue'
import { showToast } from 'vant'
import { useUserStore } from '@/stores/user.js'
import { updateProfile } from '@/api/user.js'

const userStore = useUserStore()
const user = computed(() => userStore.userInfo || userStore.syncCurrentUser())

const form = reactive({
  username: '',
  location: '',
  occupation: '',
  height: '',
  bio: ''
})

watch(user, (val) => {
  form.username = val?.username || ''
  form.location = val?.location || ''
  form.occupation = val?.occupation || ''
  form.height = val?.height || ''
  form.bio = val?.bio || ''
}, { immediate: true })

const statusText = computed(() => {
  const status = user.value?.verificationStatus || 'none'
  if (status === 'approved') return '已通过'
  if (status === 'pending') return '待审核'
  if (status === 'rejected') return '已驳回'
  return '未提交'
})

async function saveProfile() {
  try {
    await updateProfile({
      username: form.username,
      location: form.location,
      occupation: form.occupation,
      height: form.height,
      bio: form.bio,
      signature: form.bio,
      nickname: form.username
    })
    await userStore.refreshCurrentUser()
    showToast({ message: '资料已保存', type: 'success' })
  } catch (err) {
    showToast({ message: err.message || '保存失败', type: 'fail' })
  }
}
</script>

<style scoped>
.submit-form {
  margin-top: 12px;
  display: grid;
  gap: 8px;
}
</style>

