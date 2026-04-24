<template>
  <div class="home-page">
    <!-- 顶部栏 -->
    <div class="top-bar">
      <span class="top-title">💝 Love Cube</span>
      <van-icon name="contact" size="24" color="#FF6B8A" @click="router.push('/profile')" />
    </div>

    <!-- 加载中 -->
    <div v-if="pageLoading" class="page-loading">
      <van-loading type="spinner" color="#FF6B8A" />
    </div>

    <template v-else>
      <!-- Banner 轮播 -->
      <div class="section banner-section">
        <van-swipe :autoplay="3000" indicator-color="#FF6B8A" class="banner-swipe">
          <van-swipe-item v-for="item in banners" :key="item.id">
            <img
              :src="item.imageUrl || item.image_url || item.imgUrl"
              class="banner-img"
              alt="banner"
              @error="onImgError"
            />
          </van-swipe-item>
          <!-- 无 banner 时占位 -->
          <van-swipe-item v-if="!banners.length">
            <div class="banner-placeholder">
              <span>遇见你，是最好的事 💝</span>
            </div>
          </van-swipe-item>
        </van-swipe>
      </div>

      <!-- 推荐用户 -->
      <div class="section">
        <div class="section-title">
          <span>推荐</span>
          <span class="section-more">查看更多</span>
        </div>
        <div v-if="recommends.length" class="user-scroll">
          <div
            v-for="user in recommends"
            :key="user.userId || user.userid"
            class="user-card"
          >
            <van-image
              round
              width="60"
              height="60"
              :src="user.profilePhoto || user.profile_photo"
              fit="cover"
              class="user-avatar"
            >
              <template #error>
                <div class="avatar-fallback">{{ (user.nickname || user.username || '?')[0] }}</div>
              </template>
            </van-image>
            <p class="user-name">{{ user.nickname || user.username }}</p>
            <p class="user-meta">{{ user.age }}岁 · {{ user.location || '未知' }}</p>
          </div>
        </div>
        <van-empty v-else description="暂无推荐" image-size="80" />
      </div>

      <!-- 新人推荐 -->
      <div class="section">
        <div class="section-title">
          <span>新人</span>
          <span class="section-more">查看更多</span>
        </div>
        <div v-if="newcomers.length" class="newcomer-list">
          <div
            v-for="user in newcomers"
            :key="user.userId || user.userid"
            class="newcomer-card"
          >
            <van-image
              round
              width="48"
              height="48"
              :src="user.profilePhoto || user.profile_photo"
              fit="cover"
            >
              <template #error>
                <div class="avatar-fallback small">{{ (user.nickname || user.username || '?')[0] }}</div>
              </template>
            </van-image>
            <div class="newcomer-info">
              <p class="user-name">{{ user.nickname || user.username }}</p>
              <p class="user-meta">{{ user.age }}岁 · {{ user.location || '未知' }}</p>
            </div>
            <van-tag plain type="primary" class="newcomer-tag">打招呼</van-tag>
          </div>
        </div>
        <van-empty v-else description="暂无新人" image-size="80" />
      </div>
    </template>

    <!-- 底部导航 -->
    <van-tabbar v-model="tabbarActive" active-color="#FF6B8A">
      <van-tabbar-item icon="home-o" @click="router.push('/')">首页</van-tabbar-item>
      <van-tabbar-item icon="friends-o">发现</van-tabbar-item>
      <van-tabbar-item icon="contact-o" @click="router.push('/profile')">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getBanners, getRecommends, getNewcomers } from '@/api/home.js'

const router = useRouter()
const pageLoading  = ref(true)
const tabbarActive = ref(0)
const banners      = ref([])
const recommends   = ref([])
const newcomers    = ref([])

onMounted(async () => {
  try {
    const [b, r, n] = await Promise.allSettled([
      getBanners(),
      getRecommends(),
      getNewcomers()
    ])
    if (b.status === 'fulfilled') banners.value    = b.value || []
    if (r.status === 'fulfilled') recommends.value = r.value || []
    if (n.status === 'fulfilled') newcomers.value  = n.value || []
  } catch {
    showToast({ message: '数据加载失败', type: 'fail' })
  } finally {
    pageLoading.value = false
  }
})

function onImgError(e) {
  e.target.style.display = 'none'
}
</script>

<style scoped>
.home-page {
  padding-bottom: 60px;
  min-height: 100vh;
  background: #f8f8f8;
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
  font-size: 18px;
  font-weight: 700;
  color: #FF6B8A;
}

.page-loading {
  display: flex;
  justify-content: center;
  padding-top: 120px;
}

.section {
  background: #fff;
  margin: 8px 0;
  padding: 14px 0 14px;
}

.banner-section {
  padding: 0;
  margin-top: 0;
}

.banner-swipe {
  height: 180px;
}

.banner-img {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.banner-placeholder {
  height: 180px;
  background: linear-gradient(135deg, #FFE8EE, #FFD0DC);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: #FF6B8A;
  font-weight: 500;
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px 12px;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.section-more {
  font-size: 12px;
  color: #aaa;
  font-weight: 400;
}

/* 推荐：横向滚动卡片 */
.user-scroll {
  display: flex;
  gap: 12px;
  padding: 0 16px;
  overflow-x: auto;
  scrollbar-width: none;
}

.user-scroll::-webkit-scrollbar { display: none; }

.user-card {
  flex-shrink: 0;
  width: 80px;
  text-align: center;
}

.user-avatar {
  margin-bottom: 6px;
}

/* 新人：竖向列表 */
.newcomer-list {
  padding: 0 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.newcomer-card {
  display: flex;
  align-items: center;
  gap: 12px;
}

.newcomer-info {
  flex: 1;
}

.newcomer-tag {
  flex-shrink: 0;
}

.user-name {
  font-size: 13px;
  font-weight: 500;
  color: #333;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.user-meta {
  font-size: 11px;
  color: #999;
  margin-top: 2px;
}

.avatar-fallback {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF6B8A, #FFB3C1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #fff;
  font-weight: 600;
}

.avatar-fallback.small {
  width: 48px;
  height: 48px;
  font-size: 16px;
}
</style>
