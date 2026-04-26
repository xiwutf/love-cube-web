<template>
  <section class="platform-page module-page">
    <section class="platform-card module-hero">
      <p class="platform-kicker">Account Center</p>
      <h1 class="platform-title">账号中心</h1>
      <p class="platform-subtitle">统一管理个人资料、账号状态和平台入口。</p>
    </section>

    <section v-if="user" class="detail-layout">
      <article class="platform-card detail-aside">
        <h3 class="platform-heading">账号概览</h3>
        <p class="module-card-meta">用户名</p>
        <p class="platform-text">{{ user.username || '未设置' }}</p>
        <p class="module-card-meta">手机号</p>
        <p class="platform-text">{{ user.phone || '未设置' }}</p>
        <p class="module-card-meta">角色</p>
        <p class="platform-text">{{ user.role === 'admin' ? '管理员' : '普通用户' }}</p>
        <p class="module-card-meta">账号状态</p>
        <p class="platform-text">{{ user.status === 'banned' ? '已封禁' : '正常' }}</p>
        <p class="module-card-meta">认证状态</p>
        <p class="platform-text">{{ statusText }}</p>
      </article>

      <article class="platform-card detail-content">
        <h3 class="platform-heading">编辑资料</h3>
        <div class="account-form-grid">
          <label class="account-label">
            昵称
            <input v-model="form.username" class="admin-input" placeholder="请输入昵称" />
          </label>
          <label class="account-label">
            城市
            <input v-model="form.location" class="admin-input" placeholder="请输入城市" />
          </label>
          <label class="account-label">
            职业
            <input v-model="form.occupation" class="admin-input" placeholder="请输入职业" />
          </label>
          <label class="account-label">
            身高(cm)
            <input v-model="form.height" class="admin-input" placeholder="例如 170" />
          </label>
        </div>
        <label class="account-label account-label-full">
          个人简介
          <textarea v-model="form.bio" class="admin-textarea" placeholder="介绍一下你自己"></textarea>
        </label>
        <div class="platform-actions">
          <button class="platform-btn platform-btn-primary" type="button" @click="saveProfile">保存资料</button>
          <router-link to="/fellowship/me" class="platform-btn platform-btn-ghost">进入联谊模块</router-link>
        </div>
      </article>
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

watch(
  user,
  (val) => {
    form.username = val?.username || ''
    form.location = val?.location || ''
    form.occupation = val?.occupation || ''
    form.height = val?.height || ''
    form.bio = val?.bio || ''
  },
  { immediate: true }
)

const statusText = computed(() => {
  const status = user.value?.verificationStatus || 'none'
  if (status === 'approved') return '已通过'
  if (status === 'pending') return '审核中'
  if (status === 'rejected') return '已驳回'
  return '未认证'
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
    showToast({ message: '资料已更新', type: 'success' })
  } catch (err) {
    showToast({ message: err.message || '保存失败', type: 'fail' })
  }
}
</script>
