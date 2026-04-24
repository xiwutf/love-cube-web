<template>
  <div class="settings-page">
    <NavBar title="设置" />

    <van-cell-group inset title="账号" class="group">
      <van-cell title="退出登录" is-link @click="handleLogout" title-class="danger-text" />
    </van-cell-group>

    <van-cell-group inset title="存储" class="group">
      <van-cell title="清理缓存" is-link :value="cacheSize" @click="handleClearCache" />
    </van-cell-group>

    <van-cell-group inset title="关于" class="group">
      <van-cell title="版本号" value="v0.1.0" />
      <van-cell title="Love Cube" value="遇见你，是最好的事" />
    </van-cell-group>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import NavBar from '@/components/NavBar.vue'
import { useUserStore } from '@/stores/user.js'
import { storage } from '@/utils/storage.js'

const router    = useRouter()
const userStore = useUserStore()
const cacheSize = ref('已优化')

function handleLogout() {
  showConfirmDialog({ title: '退出登录', message: '确认退出当前账号？' })
    .then(() => {
      userStore.logout()
      router.replace('/login')
    })
    .catch(() => {})
}

function handleClearCache() {
  showConfirmDialog({ title: '清理缓存', message: '确认清除本地缓存数据？' })
    .then(() => {
      // 只清除非鉴权缓存（保留 token/userId）
      const token  = storage.get('token')
      const userId = storage.get('userId')
      storage.clear()
      if (token)  storage.set('token',  token)
      if (userId) storage.set('userId', userId)
      cacheSize.value = '已清理'
      showToast({ message: '缓存已清理', type: 'success' })
    })
    .catch(() => {})
}
</script>

<style scoped>
.settings-page { min-height: 100vh; background: #f8f8f8; }
.group { margin-top: 12px; }
:deep(.danger-text) { color: #ee0a24; }
</style>
