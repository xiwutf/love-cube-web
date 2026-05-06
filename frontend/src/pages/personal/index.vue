<template>
  <div class="personal-page">
    <section class="profile-section">
      <div class="section-head">
        <h1>个人中心</h1>
        <div class="head-actions">
          <van-icon name="scan" size="22" @click="handleScan" />
          <van-icon name="setting-o" size="22" @click="router.push('/fellowship/settings')" />
        </div>
      </div>

      <div class="profile-card">
        <div class="profile-main">
          <div class="avatar-wrap">
            <van-image v-if="showPersonalAvatar" round width="88" height="88" :src="personalAvatarSrc" fit="cover" />
            <div v-else class="avatar-fallback">{{ userInitial }}</div>
          </div>
          <div class="profile-info">
            <div class="name-line">
              <h2>{{ displayName }}</h2>
              <span class="role-tag">{{ user.role === 'admin' ? '管理员' : '用户' }}</span>
              <span class="verify-tag" :class="`verify-${verifyLevel}`">
                <van-icon v-if="verifyLevel === 'approved'" name="passed" class="verify-tag-icon" />
                {{ statusText }}
              </span>
            </div>
            <button class="edit-btn" type="button" @click="router.push('/fellowship/profile/edit')">
              <van-icon name="edit" size="16" />
              编辑资料
            </button>
          </div>
        </div>

        <div class="stats-row">
          <button class="stat-item" type="button" @click="router.push('/fellowship/my-likes')">
            <p class="num">{{ stats.myLikes }}</p>
            <p class="label">我的喜欢</p>
          </button>
          <button class="stat-item" type="button" @click="router.push('/fellowship/my-likes?tab=mutual')">
            <p class="num">{{ stats.mutualLikes }}</p>
            <p class="label">互相喜欢</p>
          </button>
          <button class="stat-item" type="button" @click="router.push('/fellowship/messages?tab=visitor')">
            <p class="num">{{ stats.visitors }}</p>
            <p class="label">我的访客</p>
          </button>
          <button class="stat-item" type="button" @click="router.push('/fellowship/dynamic')">
            <p class="num">{{ stats.dynamicCount }}</p>
            <p class="label">我的动态</p>
          </button>
        </div>
      </div>

      <div class="vip-card" @click="handleVip">
        <div class="vip-left">
          <van-icon name="diamond-o" size="22" />
          <div>
            <h3>开通VIP会员</h3>
            <p>享受更多特权，助力脱单</p>
          </div>
        </div>
        <button class="vip-btn" type="button">立即开通</button>
      </div>
    </section>

    <section class="feature-card">
      <h3>常用功能</h3>
      <div class="feature-grid">
        <button v-for="item in featureItems" :key="item.title" type="button" class="feature-item" @click="router.push(item.to)">
          <div class="icon-wrap">
            <van-icon :name="item.icon" size="24" />
          </div>
          <span>{{ item.title }}</span>
        </button>
      </div>
    </section>

    <section class="settings-card">
      <button v-for="item in settingItems" :key="item.title" type="button" class="setting-row" @click="item.onClick">
        <div class="setting-left">
          <van-icon :name="item.icon" size="20" />
          <span>{{ item.title }}</span>
        </div>
        <div class="setting-right">
          <span v-if="item.value" class="value-text">{{ item.value }}</span>
          <van-icon name="arrow" size="16" />
        </div>
      </button>
      <button type="button" class="setting-row logout-row" @click="handleLogout">
        <div class="setting-left">
          <van-icon name="warning-o" size="20" />
          <span>退出登录</span>
        </div>
        <van-icon name="arrow" size="16" />
      </button>
    </section>

    <AppTabBar />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import AppTabBar from '@/components/AppTabBar.vue'
import { useUserStore } from '@/stores/user.js'
import { getVisitorList } from '@/api/message.js'
import { getMyDynamicCount, getMyLikeUsers, getMutualLikeUsers } from '@/api/personal.js'
import { getAvatar } from '@/utils/image.js'
import { userAvatarUrlFromApi, userHasVerificationBadge } from '@/utils/displayFields.js'

const router = useRouter()
const userStore = useUserStore()

const user = computed(() => userStore.syncCurrentUser() || { userId: '', username: '未登录', role: 'user', verificationStatus: 'none' })

const displayName = computed(() => user.value.username || user.value.nickname || user.value.userId || 'Love Cube 用户')
const showPersonalAvatar = computed(() => Boolean(userAvatarUrlFromApi(user.value)))
const personalAvatarSrc = computed(() => getAvatar(user.value))
const userInitial = computed(() => String(displayName.value || '我').trim().slice(0, 1).toUpperCase())

const statusText = computed(() => {
  if (userHasVerificationBadge(user.value)) return '已认证'
  const status = user.value.verificationStatus || 'none'
  if (status === 'pending') return '审核中'
  if (status === 'rejected') return '未通过'
  return '未认证'
})

const verifyLevel = computed(() => {
  if (userHasVerificationBadge(user.value)) return 'approved'
  const status = user.value.verificationStatus || 'none'
  if (status === 'pending') return 'pending'
  if (status === 'rejected') return 'rejected'
  return 'none'
})

const stats = reactive({
  myLikes: 0,
  mutualLikes: 0,
  visitors: 0,
  dynamicCount: 0
})

const featureItems = [
  { title: '平台个人中心', icon: 'manager-o', to: '/me' },
  { title: '认证状态', icon: 'certificate', to: '/me' },
  { title: '我的动态', icon: 'records', to: '/fellowship/dynamic' },
  { title: '消息中心', icon: 'chat-o', to: '/fellowship/messages' },
  { title: '我的喜欢', icon: 'like-o', to: '/fellowship/my-likes' },
  { title: '我的访客', icon: 'eye-o', to: '/fellowship/messages?tab=visitor' },
  { title: '最近来访', icon: 'clock-o', to: '/fellowship/messages?tab=visitor' },
  { title: '关注列表', icon: 'star-o', to: '/fellowship/following' }
]

const settingItems = computed(() => [
  { title: '通用设置', icon: 'setting-o', onClick: () => router.push('/fellowship/settings') },
  { title: '隐私设置', icon: 'shield-o', onClick: () => router.push('/fellowship/privacy') },
  { title: '帮助与反馈', icon: 'question-o', onClick: () => router.push('/fellowship/help') },
  { title: '建议反馈', icon: 'info-o', value: 'Love Cube 1.0.0', onClick: () => router.push('/feedback') }
])

async function loadStats() {
  const [likesRes, mutualRes, visitorsRes, dynamicsRes] = await Promise.allSettled([
    getMyLikeUsers(),
    getMutualLikeUsers(),
    getVisitorList(),
    getMyDynamicCount()
  ])

  if (likesRes.status === 'fulfilled') stats.myLikes = Array.isArray(likesRes.value) ? likesRes.value.length : 0
  if (mutualRes.status === 'fulfilled') stats.mutualLikes = Array.isArray(mutualRes.value) ? mutualRes.value.length : 0
  if (visitorsRes.status === 'fulfilled') stats.visitors = Array.isArray(visitorsRes.value) ? visitorsRes.value.length : 0
  if (dynamicsRes.status === 'fulfilled') stats.dynamicCount = Number(dynamicsRes.value?.count || 0)
}

function handleVip() {
  showToast('VIP 功能即将上线')
}

function handleScan() {
  showToast('扫码功能开发中')
}

function handleLogout() {
  showConfirmDialog({ title: '退出登录', message: '确认退出当前账号？' })
    .then(() => {
      userStore.logout()
      router.replace('/fellowship/login')
    })
    .catch(() => {})
}

onMounted(() => {
  loadStats().catch(() => {})
  userStore.refreshCurrentUser().catch(() => {})
})
</script>

<style scoped>
.personal-page {
  min-height: 100vh;
  padding: 12px 12px 84px;
  background: #f8f9fb;
  color: #333;
}

.profile-section {
  padding: 14px;
  border-radius: 20px;
  background:
    radial-gradient(circle at 86% 10%, rgba(255, 255, 255, 0.48) 0, rgba(255, 255, 255, 0) 43%),
    linear-gradient(140deg, #ffd8e2 0%, #ffe7ef 58%, #fff4f7 100%);
  border: 1px solid #ffd9e4;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-head h1 {
  font-size: 30px;
  line-height: 1;
  margin: 0;
  font-weight: 800;
  letter-spacing: 1px;
}

.head-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #222;
}

.profile-card {
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.96);
  padding: 14px 14px 8px;
  box-shadow: 0 8px 22px rgba(255, 107, 155, 0.12);
}

.profile-main {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar-fallback {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff6b9b, #ff9ab6);
  color: #fff;
  font-size: 34px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.profile-info {
  flex: 1;
  min-width: 0;
}

.name-line {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 8px;
}

.name-line h2 {
  margin: 0;
  font-size: 42px;
  line-height: 1;
  font-weight: 700;
}

.role-tag,
.verify-tag {
  border-radius: 8px;
  border: 1px solid #ffc5d6;
  padding: 2px 8px;
  font-size: 18px;
  line-height: 1.2;
  color: #ff6b9b;
  background: #fff3f8;
}

.verify-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #666;
  border-color: #d8dbe3;
  background: #f6f7fa;
}

.verify-tag-icon {
  flex-shrink: 0;
}

.verify-approved {
  color: #16a34a;
  border-color: #a7f3d0;
  background: #ecfdf3;
}

.verify-pending {
  color: #d97706;
  border-color: #fed7aa;
  background: #fff7ed;
}

.verify-rejected {
  color: #dc2626;
  border-color: #fecaca;
  background: #fef2f2;
}

.edit-btn {
  border: 0;
  background: transparent;
  color: #222;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 17px;
  padding: 0;
}

.stats-row {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  border-top: 1px solid #f0f1f5;
}

.stat-item {
  text-align: center;
  padding: 10px 4px 6px;
  border: 0;
  background: transparent;
}

.stat-item + .stat-item {
  border-left: 1px solid #f0f1f5;
}

.num {
  margin: 0 0 3px;
  font-size: 36px;
  font-weight: 700;
}

.label {
  margin: 0;
  font-size: 14px;
  color: #666;
}

.vip-card {
  margin-top: 10px;
  border-radius: 16px;
  background: linear-gradient(115deg, #161b2d 0%, #2f3546 55%, #121620 100%);
  color: #f6dfb4;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  cursor: pointer;
}

.vip-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.vip-left h3 {
  margin: 0 0 2px;
  color: #ffe8b3;
  font-size: 18px;
}

.vip-left p {
  margin: 0;
  color: #d9be8b;
  font-size: 13px;
}

.vip-btn {
  border: 0;
  border-radius: 999px;
  background: linear-gradient(130deg, #fff0c7, #e7bf73);
  color: #2c220f;
  padding: 10px 14px;
  font-size: 14px;
  font-weight: 700;
}

.feature-card,
.settings-card {
  margin-top: 12px;
  border-radius: 16px;
  background: #fff;
  border: 1px solid #eff0f4;
}

.feature-card {
  padding: 14px 12px 4px;
}

.feature-card h3 {
  margin: 0 0 12px;
  font-size: 20px;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px 6px;
}

.feature-item {
  border: 0;
  background: transparent;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  color: #333;
}

.icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  background: #fff4f8;
  color: #ff6b9b;
  display: flex;
  align-items: center;
  justify-content: center;
}

.feature-item span {
  font-size: 13px;
}

.settings-card {
  overflow: hidden;
}

.setting-row {
  width: 100%;
  border: 0;
  background: #fff;
  border-bottom: 1px solid #f0f1f5;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 14px;
  color: #333;
}

.setting-left,
.setting-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.value-text {
  color: #999;
  font-size: 14px;
}

.logout-row {
  color: #ff3b62;
  border-bottom: 0;
}

@media (max-width: 360px) {
  .name-line h2 {
    font-size: 34px;
  }

  .num {
    font-size: 28px;
  }
}
</style>
