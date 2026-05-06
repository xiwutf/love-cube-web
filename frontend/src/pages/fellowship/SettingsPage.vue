<template>
  <div class="settings-page">
    <NavBar title="设置" />

    <van-cell-group inset title="账号" class="group">
      <van-cell title="统一账号中心" is-link @click="router.push('/account')" />
      <van-cell title="修改密码" is-link @click="router.push('/fellowship/change-password')" />
      <van-cell
        title="关闭联谊功能"
        is-link
        :value="deactivating ? '处理中' : '手动关闭'"
        @click="handleDeactivateFellowship"
      />
      <van-cell title="退出登录" is-link @click="handleLogout" title-class="danger-text" />
    </van-cell-group>

    <van-cell-group inset title="隐私" class="group">
      <van-cell title="黑名单" is-link @click="router.push('/fellowship/blacklist')" />
      <van-cell
        title="进入匹配列表"
        :value="matchVisible ? '已开启' : '未开启'"
        is-link
        @click="handleToggleMatchVisible"
      />
    </van-cell-group>

    <van-cell-group inset title="存储" class="group">
      <van-cell title="清理缓存" is-link :value="cacheSize" @click="handleClearCache" />
    </van-cell-group>

    <van-cell-group inset title="关于" class="group">
      <van-cell title="版本号" value="v0.2.0" />
      <van-cell title="Love Cube" value="遇见你，是最好的" />
    </van-cell-group>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import NavBar from '@/components/NavBar.vue'
import { useUserStore } from '@/stores/user.js'
import { storage } from '@/utils/storage.js'

const router = useRouter()
const userStore = useUserStore()
const cacheSize = ref('已优化')
const deactivating = ref(false)
const savingMatchVisible = ref(false)
const isFellowshipEnabled = computed(() => userStore.isFellowshipEnabled)
const matchVisible = computed(() => userStore.isFellowshipMatchVisible)

function handleLogout() {
  showConfirmDialog({ title: '退出登录', message: '确认退出当前账号吗？' })
    .then(() => {
      userStore.logout()
      router.replace('/login')
    })
    .catch(() => {})
}

function handleClearCache() {
  showConfirmDialog({ title: '清理缓存', message: '确认清除本地缓存数据吗？' })
    .then(() => {
      const token = storage.get('token')
      const userId = storage.get('userId')
      storage.clear()
      if (token) storage.set('token', token)
      if (userId) storage.set('userId', userId)
      cacheSize.value = '已清理'
      showToast({ message: '缓存已清理', type: 'success' })
    })
    .catch(() => {})
}

function handleDeactivateFellowship() {
  if (!isFellowshipEnabled.value) {
    showToast({ message: '联谊功能当前已关闭', type: 'fail' })
    return
  }
  showConfirmDialog({
    title: '关闭联谊功能',
    message: '关闭后你将无法使用联谊功能，可随时在联谊首页重新开通。确认关闭吗？'
  })
    .then(async () => {
      deactivating.value = true
      try {
        await userStore.deactivateFellowship()
        showToast({ message: '联谊功能已关闭', type: 'success' })
        router.replace('/fellowship')
      } catch (error) {
        showToast({ message: error?.message || '关闭失败，请稍后重试', type: 'fail' })
      } finally {
        deactivating.value = false
      }
    })
    .catch(() => {})
}

function handleToggleMatchVisible() {
  if (!isFellowshipEnabled.value) {
    showToast({ message: '请先开通联谊功能', type: 'fail' })
    return
  }
  if (savingMatchVisible.value) {
    return
  }
  const nextVisible = !matchVisible.value
  const actionText = nextVisible ? '开启' : '关闭'
  showConfirmDialog({
    title: `${actionText}匹配列表`,
    message: nextVisible
      ? '开启后你会出现在推荐/匹配列表中，其他用户可看到你。确认开启吗？'
      : '关闭后你将不会出现在推荐/匹配列表中，但仍可浏览联谊功能。确认关闭吗？'
  })
    .then(async () => {
      savingMatchVisible.value = true
      try {
        await userStore.updateFellowshipMatchVisibility(nextVisible)
        showToast({ message: `已${actionText}匹配列表`, type: 'success' })
      } catch (error) {
        showToast({ message: error?.message || '设置失败，请稍后重试', type: 'fail' })
      } finally {
        savingMatchVisible.value = false
      }
    })
    .catch(() => {})
}
</script>

<style scoped>
.settings-page { min-height: 100vh; background: #f8f8f8; }
.group { margin-top: 12px; }
:deep(.danger-text) { color: #ee0a24; }
</style>


