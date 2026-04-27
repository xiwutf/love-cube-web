<template>
  <div class="profile-page">
    <!-- 顶部信息 -->
    <div class="top-bar">
      <van-icon name="arrow-left" size="22" @click="router.back()" />
      <span class="top-title">个人中心</span>
      <div style="width:22px" />
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="page-loading">
      <van-loading type="spinner" color="#FF6B8A" />
    </div>

    <template v-else-if="userInfo">
      <!-- 澶撮儴淇℃伅 -->
      <div class="profile-header">
        <van-image
          round
          width="80"
          height="80"
          :src="userInfo.profilePhoto"
          fit="cover"
          class="avatar"
        >
          <template #error>
            <div class="avatar-fallback">{{ (userInfo.nickname || '?')[0] }}</div>
          </template>
        </van-image>
        <div class="header-info">
          <h2 class="nickname">{{ userInfo.nickname || '未设置昵称' }}</h2>
          <p class="bio">{{ userInfo.signature || '这个人很懒，什么都没留下~' }}</p>
          <div class="tags">
            <van-tag v-if="userInfo.gender"    plain type="primary">{{ userInfo.gender }}</van-tag>
            <van-tag v-if="userInfo.age" plain type="success">{{ userInfo.age }}岁</van-tag>
            <van-tag v-if="userInfo.constellation" plain type="warning">{{ userInfo.constellation }}</van-tag>
          </div>
        </div>
      </div>

      <!-- 资料完整度 -->
      <div class="completion-bar">
        <div class="completion-label">
          <span>资料完整</span>
          <span class="completion-value">{{ userInfo.completionRate }}%</span>
        </div>
        <van-progress :percentage="userInfo.completionRate" color="#FF6B8A" :show-pivot="false" />
      </div>

      <!-- 基本信息 -->
      <van-cell-group inset title="基本信息" class="info-group">
        <van-cell title="手机号" :value="userInfo.phoneNumber || '未绑定'" />
        <van-cell title="所在地" :value="userInfo.location || '未填写'" />
        <van-cell title="职业" :value="userInfo.occupation || '未填写'" />
        <van-cell title="身高" :value="userInfo.height ? userInfo.height + 'cm' : '未填写'" />
        <van-cell title="生日" :value="userInfo.birthday || '未填写'" />
      </van-cell-group>

      <!-- 统计数据 -->
      <div class="stats-row" v-if="userInfo.statistics">
        <div class="stat-item">
          <span class="stat-num">{{ userInfo.statistics.likeCount ? 0 }}</span>
          <span class="stat-label">获赞</span>
        </div>
        <div class="stat-divider" />
        <div class="stat-item">
          <span class="stat-num">{{ userInfo.statistics.visitorCount ? 0 }}</span>
          <span class="stat-label">访客</span>
        </div>
        <div class="stat-divider" />
        <div class="stat-item">
          <span class="stat-num">{{ userInfo.statistics.matchCount ? 0 }}</span>
          <span class="stat-label">配对</span>
        </div>
      </div>

      <!-- 退出登录 -->
      <div class="logout-wrap">
        <van-button
          round
          block
          plain
          type="danger"
          @click="handleLogout"
        >
          退出登录
        </van-button>
      </div>
    </template>

    <!-- 加载失败 -->
    <van-empty v-else description="加载失败，请重试" image="error">
      <van-button round type="primary" size="small" @click="loadUserInfo">重新加载</van-button>
    </van-empty>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import { getMe } from '@/api/user.js'
import { useUserStore } from '@/stores/user.js'

const router    = useRouter()
const userStore = useUserStore()
const loading   = ref(true)
const userInfo  = ref(null)

async function loadUserInfo() {
  loading.value = true
  try {
    userInfo.value = await getMe()
  } catch (err) {
    showToast({ message: err.message || '加载失败', type: 'fail' })
    userInfo.value = null
  } finally {
    loading.value = false
  }
}

function handleLogout() {
  showConfirmDialog({
    title: '退出登录',
    message: '确认退出当前账号？'
  }).then(() => {
    userStore.logout()
    router.replace('/login')
  }).catch(() => {})
}

onMounted(loadUserInfo)
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: #f8f8f8;
  padding-bottom: 32px;
}

.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  background: #fff;
  position: sticky;
  top: 0;
  z-index: 10;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
}

.top-title {
  font-size: 17px;
  font-weight: 600;
}

.page-loading {
  display: flex;
  justify-content: center;
  padding-top: 120px;
}

.profile-header {
  display: flex;
  gap: 16px;
  padding: 20px 16px;
  background: #fff;
  margin-bottom: 8px;
  align-items: flex-start;
}

.avatar-fallback {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF6B8A, #FFB3C1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
  font-weight: 700;
}

.header-info {
  flex: 1;
}

.nickname {
  font-size: 18px;
  font-weight: 700;
  color: #333;
  margin-bottom: 4px;
}

.bio {
  font-size: 13px;
  color: #999;
  margin-bottom: 8px;
  line-height: 1.4;
}

.tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.completion-bar {
  background: #fff;
  padding: 12px 16px;
  margin-bottom: 8px;
}

.completion-label {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 13px;
  color: #666;
}

.completion-value {
  color: #FF6B8A;
  font-weight: 600;
}

.info-group {
  margin-bottom: 8px;
}

.stats-row {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 16px 0;
  margin-bottom: 16px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-num {
  font-size: 20px;
  font-weight: 700;
  color: #FF6B8A;
}

.stat-label {
  font-size: 12px;
  color: #999;
}

.stat-divider {
  width: 1px;
  height: 30px;
  background: #f0f0f0;
}

.logout-wrap {
  padding: 0 16px;
}
</style>

