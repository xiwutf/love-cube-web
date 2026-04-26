<template>
  <div class="home-page">
    <!-- Header -->
    <header class="home-header">
      <div class="home-logo">
        <span class="home-logo-icon">♡</span>
        <span class="home-logo-text">Love Cube</span>
      </div>
      <button class="home-avatar-btn" @click="router.push('/fellowship/me')">
        <van-icon name="contact" size="22" color="#FF5F84" />
      </button>
    </header>

    <div v-if="pageLoading" class="page-loading">
      <van-loading type="spinner" color="#FF5F84" size="32" />
    </div>

    <template v-else>
      <!-- Completion tip -->
      <div v-if="!completion.completed" class="tip-card">
        <div class="tip-left">
          <div class="tip-icon">✦</div>
          <div>
            <p class="tip-title">完善你的资料</p>
            <p class="tip-desc">完善资料后，别人才能更好地了解你</p>
          </div>
        </div>
        <van-button round size="mini" color="#FF5F84" @click="router.push('/fellowship/profile/edit')">
          去完善
        </van-button>
      </div>

      <!-- Banner -->
      <div class="banner-wrap">
        <van-swipe :autoplay="3000" indicator-color="#FF5F84" class="banner-swipe">
          <van-swipe-item v-for="item in banners" :key="item.id">
            <img :src="item.imageUrl || item.image_url || item.imgUrl" class="banner-img" alt="banner" @error="onImgError" />
          </van-swipe-item>
          <van-swipe-item v-if="!banners.length">
            <div class="banner-fallback">
              <p class="banner-fallback-kicker">Love Cube Fellowship</p>
              <p class="banner-fallback-title">遇见对的人</p>
            </div>
          </van-swipe-item>
        </van-swipe>
      </div>

      <!-- Recommends -->
      <div class="section-card">
        <div class="section-head">
          <span class="section-title">为你推荐</span>
          <span class="section-more" @click="router.push('/fellowship/match')">查看更多</span>
        </div>
        <div v-if="recommends.length" class="rec-scroll">
          <div
            v-for="user in recommends"
            :key="user.userId || user.userid"
            class="rec-item"
            @click="router.push(`/fellowship/user-profile/${user.userId || user.userid}`)"
          >
            <div class="rec-avatar-ring">
              <van-image
                round
                width="60"
                height="60"
                :src="user.profilePhoto || user.profile_photo"
                fit="cover"
                class="rec-avatar"
              >
                <template #error>
                  <div class="avatar-fb size60">{{ (user.nickname || user.username || '?')[0] }}</div>
                </template>
              </van-image>
            </div>
            <p class="rec-name">{{ user.nickname || user.username }}</p>
            <p class="rec-meta">{{ user.age }}岁</p>
          </div>
        </div>
        <van-empty v-else description="暂无推荐" image-size="60" />
      </div>

      <!-- Newcomers -->
      <div class="section-card">
        <div class="section-head">
          <span class="section-title">新人</span>
          <span class="section-more" @click="router.push('/fellowship/newcomers')">查看更多</span>
        </div>
        <div v-if="newcomers.length" class="nc-list">
          <div
            v-for="user in newcomers"
            :key="user.userId || user.userid"
            class="nc-item"
            @click="router.push(`/fellowship/user-profile/${user.userId || user.userid}`)"
          >
            <van-image round width="48" height="48" :src="user.profilePhoto || user.profile_photo" fit="cover">
              <template #error>
                <div class="avatar-fb size48">{{ (user.nickname || user.username || '?')[0] }}</div>
              </template>
            </van-image>
            <div class="nc-info">
              <p class="nc-name">{{ user.nickname || user.username }}</p>
              <p class="nc-meta">{{ user.age }}岁 · {{ user.location || '未知' }}</p>
            </div>
            <button class="nc-greet-btn" @click.stop="router.push(`/fellowship/user-profile/${user.userId || user.userid}`)">
              打招呼
            </button>
          </div>
        </div>
        <van-empty v-else description="暂无新人" image-size="60" />
      </div>
    </template>

    <AppTabBar />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import AppTabBar from '@/components/AppTabBar.vue'
import { getBanners, getRecommends, getNewcomers } from '@/api/home.js'
import { useFellowshipProfileStore } from '@/stores/fellowshipProfile.js'

const router = useRouter()
const profileStore = useFellowshipProfileStore()
const pageLoading = ref(true)
const banners = ref([])
const recommends = ref([])
const newcomers = ref([])
const completion = ref({ completed: false, percent: 0, missingFields: [] })

onMounted(async () => {
  try {
    const [b, r, n, c] = await Promise.allSettled([
      getBanners(),
      getRecommends(),
      getNewcomers(),
      profileStore.fetchCompletion()
    ])
    if (b.status === 'fulfilled') banners.value = b.value || []
    if (r.status === 'fulfilled') recommends.value = r.value || []
    if (n.status === 'fulfilled') newcomers.value = n.value || []
    if (c.status === 'fulfilled') completion.value = c.value || completion.value
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
/* ── Layout ── */
.home-page {
  min-height: 100vh;
  background: #f4f6fb;
  padding-bottom: 72px;
}

/* ── Header ── */
.home-header {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  height: 52px;
  background: #fff;
  box-shadow: 0 1px 0 #f0f2f8;
}

.home-logo {
  display: flex;
  align-items: center;
  gap: 6px;
}
.home-logo-icon {
  font-size: 20px;
  color: #FF5F84;
  line-height: 1;
}
.home-logo-text {
  font-size: 18px;
  font-weight: 800;
  color: #1a2236;
  letter-spacing: -0.02em;
}

.home-avatar-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: #fff5f8;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

/* ── Loading ── */
.page-loading {
  display: flex;
  justify-content: center;
  padding-top: 100px;
}

/* ── Tip card ── */
.tip-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin: 12px 12px 0;
  padding: 12px 14px;
  background: linear-gradient(135deg, #fff5f8, #fff8fa);
  border: 1px solid #ffdde7;
  border-radius: 14px;
}
.tip-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 0;
}
.tip-icon {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF5F84, #FF8FAA);
  color: #fff;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.tip-title {
  font-size: 13px;
  font-weight: 600;
  color: #1a2236;
}
.tip-desc {
  font-size: 12px;
  color: #8898aa;
  margin-top: 2px;
}

/* ── Banner ── */
.banner-wrap {
  margin: 12px 12px 0;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(15, 23, 42, 0.08);
}
.banner-swipe { height: 190px; }
.banner-img { width: 100%; height: 190px; object-fit: cover; display: block; }
.banner-fallback {
  height: 190px;
  background: linear-gradient(135deg, #1a1038 0%, #3d1a52 40%, #8b1a4a 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.banner-fallback-kicker {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: rgba(255, 160, 185, 0.8);
}
.banner-fallback-title {
  font-size: 26px;
  font-weight: 800;
  color: #fff;
  letter-spacing: -0.02em;
}

/* ── Section card ── */
.section-card {
  margin: 12px 12px 0;
  padding: 16px 0 12px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(15, 23, 42, 0.05);
  overflow: hidden;
}
.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px 12px;
}
.section-title {
  font-size: 16px;
  font-weight: 700;
  color: #1a2236;
}
.section-more {
  font-size: 12px;
  color: #FF5F84;
  cursor: pointer;
  font-weight: 500;
}

/* ── Recommends ── */
.rec-scroll {
  display: flex;
  gap: 8px;
  padding: 0 16px 4px;
  overflow-x: auto;
  scrollbar-width: none;
}
.rec-scroll::-webkit-scrollbar { display: none; }

.rec-item {
  flex-shrink: 0;
  width: 72px;
  text-align: center;
  cursor: pointer;
}
.rec-avatar-ring {
  width: 66px;
  height: 66px;
  border-radius: 50%;
  padding: 2px;
  background: linear-gradient(135deg, #FF5F84, #FFB3C4);
  margin: 0 auto 6px;
}
.rec-avatar {
  display: block;
}
.rec-name {
  font-size: 12px;
  font-weight: 500;
  color: #1a2236;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.rec-meta {
  font-size: 11px;
  color: #a0abbe;
  margin-top: 2px;
}

/* ── Newcomers ── */
.nc-list {
  display: flex;
  flex-direction: column;
  padding: 0 16px;
  gap: 0;
}
.nc-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  cursor: pointer;
  border-bottom: 1px solid #f4f6fb;
}
.nc-item:last-child { border-bottom: none; }
.nc-info { flex: 1; min-width: 0; }
.nc-name {
  font-size: 14px;
  font-weight: 600;
  color: #1a2236;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.nc-meta {
  font-size: 12px;
  color: #8898aa;
  margin-top: 2px;
}
.nc-greet-btn {
  flex-shrink: 0;
  height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  border: 1.5px solid #FF5F84;
  background: transparent;
  color: #FF5F84;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
}
.nc-greet-btn:active {
  background: #FF5F84;
  color: #fff;
}

/* ── Avatar fallback ── */
.avatar-fb {
  border-radius: 50%;
  background: linear-gradient(135deg, #FF5F84, #FFB3C4);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 700;
}
.size60 { width: 60px; height: 60px; font-size: 20px; }
.size48 { width: 48px; height: 48px; font-size: 16px; }
</style>
