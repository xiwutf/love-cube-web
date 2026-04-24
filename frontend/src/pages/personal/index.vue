<template>
  <div class="personal-page">
    <div class="top-bar">
      <span class="top-title">我的</span>
      <van-icon name="setting-o" size="22" color="#666" @click="router.push('/settings')" />
    </div>

    <div v-if="loading" class="page-loading">
      <van-loading type="spinner" color="#FF6B8A" />
    </div>

    <template v-else-if="user">
      <!-- 头部信息 -->
      <div class="profile-header">
        <van-image round width="72" height="72" :src="user.avatar" fit="cover" @click="router.push('/profile-edit')">
          <template #error>
            <div class="avatar-fallback">{{ (user.nickname || '?')[0] }}</div>
          </template>
        </van-image>
        <div class="header-info">
          <h2 class="nickname">{{ user.nickname || '未设置昵称' }}</h2>
          <div class="tags">
            <van-tag v-if="user.gender"        plain type="primary">{{ user.gender }}</van-tag>
            <van-tag v-if="user.age"           plain type="success">{{ user.age }}岁</van-tag>
            <van-tag v-if="user.constellation" plain type="warning">{{ user.constellation }}</van-tag>
            <van-tag v-if="user.location"      plain>{{ user.location }}</van-tag>
          </div>
          <p class="signature">{{ user.signature || '点击编辑，填写个人签名～' }}</p>
        </div>
        <van-icon name="edit" size="20" color="#FF6B8A" @click="router.push('/profile-edit')" />
      </div>

      <!-- 资料完整度 -->
      <div class="completion-bar">
        <div class="completion-label">
          <span>资料完整度</span>
          <span class="completion-value">{{ user.completionRate }}%</span>
        </div>
        <van-progress :percentage="user.completionRate" color="#FF6B8A" :show-pivot="false" stroke-width="6" />
        <p v-if="user.completionRate < 100" class="completion-hint">完善资料可提高被匹配几率</p>
      </div>

      <!-- 统计数据 -->
      <div class="stats-row">
        <div class="stat-item" @click="router.push('/message?tab=visitor')">
          <span class="stat-num">{{ user.statistics?.visitorCount ?? 0 }}</span>
          <span class="stat-label">访客</span>
        </div>
        <div class="stat-divider" />
        <div class="stat-item" @click="router.push('/message?tab=interact')">
          <span class="stat-num">{{ user.statistics?.likeCount ?? 0 }}</span>
          <span class="stat-label">获赞</span>
        </div>
        <div class="stat-divider" />
        <div class="stat-item">
          <span class="stat-num">{{ user.statistics?.matchCount ?? 0 }}</span>
          <span class="stat-label">配对</span>
        </div>
      </div>

      <!-- 生活照 -->
      <div v-if="user.photos?.length" class="photos-section">
        <p class="section-title">生活照</p>
        <div class="photos-grid">
          <van-image
            v-for="(src, i) in user.photos"
            :key="i"
            :src="toFullUrl(src)"
            width="100%"
            height="100"
            fit="cover"
            radius="8"
            @click="previewPhotos(i)"
          />
        </div>
      </div>

      <!-- 功能入口 -->
      <van-cell-group inset class="menu-group">
        <van-cell title="编辑资料"   icon="edit"       is-link @click="router.push('/profile-edit')" />
        <van-cell title="我的动态"   icon="records"    is-link @click="router.push('/dynamic')" />
        <van-cell title="消息中心"   icon="chat-o"     is-link @click="router.push('/message')" />
      </van-cell-group>

      <van-cell-group inset class="menu-group">
        <van-cell title="设置"       icon="setting-o"  is-link @click="router.push('/settings')" />
        <van-cell title="退出登录"   icon="warning-o"  is-link title-class="danger-text" @click="handleLogout" />
      </van-cell-group>
    </template>

    <van-empty v-else description="加载失败" image="error">
      <van-button round type="primary" size="small" @click="load">重新加载</van-button>
    </van-empty>

    <AppTabBar />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast, showImagePreview } from 'vant'
import NavBar    from '@/components/NavBar.vue'
import AppTabBar from '@/components/AppTabBar.vue'
import { getMe } from '@/api/user.js'
import { useUserStore } from '@/stores/user.js'
import { normalizeUser } from '@/utils/normalizeUser.js'
import { toFullUrl } from '@/utils/image.js'

const router    = useRouter()
const userStore = useUserStore()
const loading   = ref(true)
const user      = ref(null)

async function load() {
  loading.value = true
  try {
    const data = await getMe()
    user.value  = normalizeUser(data)
    // 同步统计字段（/users/me 直接返回，不走 normalizeUser 的 statistics）
    user.value.statistics   = data.statistics
    user.value.completionRate = data.completionRate ?? 0
  } catch {
    showToast({ message: '加载失败', type: 'fail' })
  } finally {
    loading.value = false
  }
}

function handleLogout() {
  showConfirmDialog({ title: '退出登录', message: '确认退出当前账号？' })
    .then(() => { userStore.logout(); router.replace('/login') })
    .catch(() => {})
}

function previewPhotos(startIndex) {
  showImagePreview({ images: user.value.photos.map(toFullUrl), startPosition: startIndex })
}

onMounted(load)
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

.page-loading { display: flex; justify-content: center; padding-top: 120px; }

.profile-header {
  display: flex; gap: 14px; align-items: flex-start;
  padding: 18px 16px; background: #fff; margin-bottom: 8px;
}
.avatar-fallback {
  width: 72px; height: 72px; border-radius: 50%;
  background: linear-gradient(135deg, #FF6B8A, #FFB3C1);
  display: flex; align-items: center; justify-content: center;
  font-size: 24px; color: #fff; font-weight: 700;
}
.header-info { flex: 1; min-width: 0; }
.nickname { font-size: 17px; font-weight: 700; color: #333; margin-bottom: 6px; }
.tags { display: flex; gap: 5px; flex-wrap: wrap; margin-bottom: 6px; }
.signature { font-size: 12px; color: #999; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

.completion-bar { background: #fff; padding: 12px 16px; margin-bottom: 8px; }
.completion-label { display: flex; justify-content: space-between; margin-bottom: 8px; font-size: 13px; color: #666; }
.completion-value { color: #FF6B8A; font-weight: 600; }
.completion-hint { font-size: 11px; color: #aaa; margin-top: 6px; }

.stats-row {
  background: #fff; display: flex; align-items: center;
  justify-content: space-around; padding: 16px 0; margin-bottom: 8px;
}
.stat-item { display: flex; flex-direction: column; align-items: center; gap: 4px; cursor: pointer; }
.stat-num  { font-size: 20px; font-weight: 700; color: #FF6B8A; }
.stat-label { font-size: 12px; color: #999; }
.stat-divider { width: 1px; height: 30px; background: #f0f0f0; }

.photos-section { background: #fff; padding: 14px 16px; margin-bottom: 8px; }
.section-title  { font-size: 14px; font-weight: 600; color: #333; margin-bottom: 10px; }
.photos-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 6px; }

.menu-group { margin-bottom: 8px; }
:deep(.danger-text) { color: #ee0a24; }
</style>
