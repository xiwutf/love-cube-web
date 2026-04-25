<template>
  <div class="personal-page">
    <div class="top-bar">
      <span class="top-title">我的</span>
      <van-icon name="setting-o" size="22" color="#666" @click="router.push('/fellowship/settings')" />
    </div>

    <div class="profile-header">
      <div class="avatar-fallback">{{ (user.username || '?')[0] }}</div>
      <div class="header-info">
        <h2 class="nickname">{{ user.username }}</h2>
        <div class="tags">
          <van-tag plain type="primary">{{ user.role === 'admin' ? '管理员' : '普通用户' }}</van-tag>
          <van-tag plain :type="statusTagType">{{ statusText }}</van-tag>
        </div>
        <p class="signature">{{ user.bio || '完善资料可提高匹配质量' }}</p>
      </div>
      <van-icon name="edit" size="20" color="#FF6B8A" @click="router.push('/account')" />
    </div>

    <van-cell-group inset class="menu-group">
      <van-cell title="账号中心" icon="manager-o" is-link @click="router.push('/account')" />
      <van-cell title="我的动态" icon="records" is-link @click="router.push('/fellowship/dynamic')" />
      <van-cell title="消息中心" icon="chat-o" is-link @click="router.push('/fellowship/messages')" />
      <van-cell
        title="开通 VIP"
        icon="gold-coin-o"
        :value="canUseVip ? '' : '需完成认证'"
        is-link
        @click="handleVip"
      />
    </van-cell-group>

    <van-cell-group inset class="menu-group">
      <van-cell title="认证状态" icon="certificate" :value="statusText" is-link @click="router.push('/account')" />
      <van-cell title="退出登录" icon="warning-o" is-link title-class="danger-text" @click="handleLogout" />
    </van-cell-group>

    <AppTabBar />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import AppTabBar from '@/components/AppTabBar.vue'
import { useUserStore } from '@/stores/user.js'

const router = useRouter()
const userStore = useUserStore()

const user = computed(() => userStore.syncCurrentUser() || { username: '未登录', role: 'user', verificationStatus: 'none' })

const statusText = computed(() => {
  const status = user.value.verificationStatus || 'none'
  if (status === 'approved') return '已认证'
  if (status === 'pending') return '待审核'
  if (status === 'rejected') return '已驳回'
  return '未认证'
})

const statusTagType = computed(() => {
  const status = user.value.verificationStatus || 'none'
  if (status === 'approved') return 'success'
  if (status === 'pending') return 'warning'
  if (status === 'rejected') return 'danger'
  return 'default'
})

const canUseVip = computed(() => user.value.verificationStatus === 'approved')

function handleVip() {
  if (!canUseVip.value) {
    showToast({ message: '请先完成实名认证', type: 'fail' })
    router.push('/account')
    return
  }
  router.push('/fellowship/vip')
}

function handleLogout() {
  showConfirmDialog({ title: '退出登录', message: '确认退出当前账号？' })
    .then(() => {
      userStore.logout()
      router.replace('/login')
    })
    .catch(() => {})
}
</script>

<style scoped>
.personal-page { min-height: 100vh; background: #f8f8f8; padding-bottom: 60px; }
.top-bar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 14px 16px; background: #fff;
  position: sticky; top: 0; z-index: 10;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
}
.top-title { font-size: 18px; font-weight: 700; color: #333; }
.profile-header {
  display: flex; gap: 14px; align-items: flex-start;
  padding: 18px 16px; background: #fff; margin-bottom: 8px;
}
.avatar-fallback {
  width: 72px; height: 72px; border-radius: 50%;
  background: linear-gradient(135deg, #ff6b8a, #ffb3c1);
  display: flex; align-items: center; justify-content: center;
  font-size: 24px; color: #fff; font-weight: 700;
}
.header-info { flex: 1; min-width: 0; }
.nickname { font-size: 17px; font-weight: 700; color: #333; margin-bottom: 6px; }
.tags { display: flex; gap: 5px; flex-wrap: wrap; margin-bottom: 6px; }
.signature { font-size: 12px; color: #999; }
.menu-group { margin-bottom: 8px; }
:deep(.danger-text) { color: #ee0a24; }
</style>
