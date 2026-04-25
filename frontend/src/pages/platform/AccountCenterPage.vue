<template>
  <section class="platform-page">
    <section class="platform-card platform-soft-card">
      <p class="platform-kicker">Account Center</p>
      <h1 class="platform-title">我的账号</h1>
      <p class="platform-subtitle">统一用户体系下，官网与联谊模块共享同一登录状态和认证状态。</p>
    </section>

    <section v-if="user" class="platform-card platform-block">
      <h3 class="platform-heading">基础信息</h3>
      <p class="platform-text">昵称：{{ user.username }}</p>
      <p class="platform-text">手机号：{{ user.phone || '未设置' }}</p>
      <p class="platform-text">账号角色：{{ user.role === 'admin' ? '管理员' : '普通用户' }}</p>
      <p class="platform-text">账号状态：{{ user.status === 'banned' ? '已封禁' : '正常' }}</p>
      <p class="platform-text">认证状态：{{ statusText }}</p>
      <p v-if="user.verificationRejectReason" class="platform-text">驳回原因：{{ user.verificationRejectReason }}</p>
      <div class="platform-actions">
        <router-link to="/fellowship/me" class="platform-btn platform-btn-ghost">进入联谊-我的</router-link>
      </div>
    </section>

    <section v-if="canSubmit" class="platform-card platform-block">
      <h3 class="platform-heading">实名认证提交（MVP）</h3>
      <div class="submit-form">
        <input v-model="form.realName" class="admin-input" placeholder="真实姓名" />
        <input v-model="form.idNumber" class="admin-input" placeholder="证件号（示例）" />
        <textarea v-model="form.note" class="admin-textarea" placeholder="补充说明（可选）" />
      </div>
      <div class="platform-actions">
        <button class="platform-btn platform-btn-primary" type="button" @click="submit">提交认证</button>
      </div>
    </section>

    <section v-else class="platform-card platform-block">
      <h3 class="platform-heading">实名认证状态</h3>
      <p class="platform-text">当前状态：{{ statusText }}</p>
      <p v-if="verification?.submittedAt" class="platform-text">提交时间：{{ verification.submittedAt }}</p>
      <p v-if="verification?.reviewedAt" class="platform-text">审核时间：{{ verification.reviewedAt }}</p>
      <p v-if="verification?.rejectReason" class="platform-text">驳回原因：{{ verification.rejectReason }}</p>
      <p class="platform-text">如被驳回可重新提交。</p>
    </section>
  </section>
</template>

<script setup>
import { computed, reactive } from 'vue'
import { showToast } from 'vant'
import { useUserStore } from '@/stores/user.js'
import { usePlatformState } from '@/mock/platformState.js'

const userStore = useUserStore()
const { submitVerification, getVerificationByUser } = usePlatformState()

const form = reactive({
  realName: '',
  idNumber: '',
  note: ''
})

const user = computed(() => userStore.syncCurrentUser())
const verification = computed(() => getVerificationByUser(userStore.userId))
const canSubmit = computed(() => ['none', 'rejected'].includes(user.value?.verificationStatus || 'none'))

const statusText = computed(() => {
  const status = user.value?.verificationStatus || 'none'
  if (status === 'approved') return '已通过'
  if (status === 'pending') return '待审核'
  if (status === 'rejected') return '已驳回'
  return '未提交'
})

function submit() {
  if (!form.realName || !form.idNumber) {
    showToast({ message: '请填写完整认证信息', type: 'fail' })
    return
  }
  submitVerification(userStore.userId, form)
  userStore.syncCurrentUser()
  showToast({ message: '提交成功，等待审核', type: 'success' })
}
</script>

<style scoped>
.submit-form {
  margin-top: 12px;
  display: grid;
  gap: 8px;
}
</style>
