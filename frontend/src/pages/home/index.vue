<template>
  <div class="home-page">
    <div class="top-bar">
      <span class="top-title">Love Cube</span>
      <van-icon name="contact" size="24" color="#FF6B8A" @click="router.push('/fellowship/me')" />
    </div>

    <div v-if="pageLoading" class="page-loading">
      <van-loading type="spinner" color="#FF6B8A" />
    </div>

    <template v-else>
      <div v-if="!completion.completed" class="section completion-tip">
        <div class="section-title">
          <span>资料完善提醒</span>
        </div>
        <p class="tip-text">完善资料后，别人才能更好地了解你</p>
        <div class="tip-action">
          <van-button round type="primary" size="small" @click="router.push('/fellowship/profile/edit')">
            去完善资料
          </van-button>
        </div>
      </div>

      <div class="section banner-section">
        <van-swipe :autoplay="3000" indicator-color="#FF6B8A" class="banner-swipe">
          <van-swipe-item v-for="item in banners" :key="item.id">
            <img :src="item.imageUrl || item.image_url || item.imgUrl" class="banner-img" alt="banner" @error="onImgError" />
          </van-swipe-item>
          <van-swipe-item v-if="!banners.length">
            <div class="banner-placeholder"><span>欢迎来到 Love Cube</span></div>
          </van-swipe-item>
        </van-swipe>
      </div>

      <div class="section">
        <div class="section-title">
          <span>推荐</span>
          <span class="section-more" @click="router.push('/fellowship/match')">查看更多</span>
        </div>
        <div v-if="recommends.length" class="user-scroll">
          <div
            v-for="user in recommends"
            :key="user.userId || user.userid"
            class="user-card"
            @click="router.push(`/fellowship/user-profile/${user.userId || user.userid}`)"
          >
            <van-image round width="60" height="60" :src="user.profilePhoto || user.profile_photo" fit="cover" class="user-avatar">
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

      <div class="section">
        <div class="section-title">
          <span>新人</span>
          <span class="section-more" @click="router.push('/fellowship/newcomers')">查看更多</span>
        </div>
        <div v-if="newcomers.length" class="newcomer-list">
          <div
            v-for="user in newcomers"
            :key="user.userId || user.userid"
            class="newcomer-card"
            @click="router.push(`/fellowship/user-profile/${user.userId || user.userid}`)"
          >
            <van-image round width="48" height="48" :src="user.profilePhoto || user.profile_photo" fit="cover">
              <template #error>
                <div class="avatar-fallback small">{{ (user.nickname || user.username || '?')[0] }}</div>
              </template>
            </van-image>
            <div class="newcomer-info">
              <p class="user-name">{{ user.nickname || user.username }}</p>
              <p class="user-meta">{{ user.age }}岁 · {{ user.location || '未知' }}</p>
            </div>
            <van-tag plain type="primary" class="newcomer-tag" @click.stop="router.push(`/fellowship/user-profile/${user.userId || user.userid}`)">
              打招呼
            </van-tag>
          </div>
        </div>
        <van-empty v-else description="暂无新人" image-size="80" />
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
.home-page { padding-bottom: 60px; min-height: 100vh; background: #f8f8f8; }
.top-bar {
  display: flex; justify-content: space-between; align-items: center;
  padding: 14px 16px; background: #fff;
  position: sticky; top: 0; z-index: 10;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
}
.top-title { font-size: 18px; font-weight: 700; color: #ff6b8a; }
.page-loading { display: flex; justify-content: center; padding-top: 120px; }
.section { background: #fff; margin: 8px 0; padding: 14px 0 14px; }
.banner-section { padding: 0; margin-top: 0; }
.banner-swipe { height: 180px; }
.banner-img { width: 100%; height: 180px; object-fit: cover; }
.banner-placeholder {
  height: 180px; background: linear-gradient(135deg, #ffe8ee, #ffd0dc);
  display: flex; align-items: center; justify-content: center;
  font-size: 16px; color: #ff6b8a; font-weight: 500;
}
.completion-tip { padding-top: 8px; }
.tip-text { padding: 0 16px; font-size: 13px; color: #666; }
.tip-action { padding: 10px 16px 0; }
.section-title {
  display: flex; justify-content: space-between; align-items: center;
  padding: 0 16px 12px; font-size: 16px; font-weight: 600; color: #333;
}
.section-more { font-size: 12px; color: #aaa; font-weight: 400; cursor: pointer; }
.user-scroll {
  display: flex; gap: 12px; padding: 0 16px; overflow-x: auto; scrollbar-width: none;
}
.user-scroll::-webkit-scrollbar { display: none; }
.user-card { flex-shrink: 0; width: 80px; text-align: center; cursor: pointer; }
.user-avatar { margin-bottom: 6px; }
.newcomer-list { padding: 0 16px; display: flex; flex-direction: column; gap: 12px; }
.newcomer-card { display: flex; align-items: center; gap: 12px; cursor: pointer; }
.newcomer-info { flex: 1; }
.newcomer-tag { flex-shrink: 0; }
.user-name {
  font-size: 13px; font-weight: 500; color: #333;
  overflow: hidden; white-space: nowrap; text-overflow: ellipsis;
}
.user-meta { font-size: 11px; color: #999; margin-top: 2px; }
.avatar-fallback {
  width: 60px; height: 60px; border-radius: 50%;
  background: linear-gradient(135deg, #ff6b8a, #ffb3c1);
  display: flex; align-items: center; justify-content: center;
  font-size: 20px; color: #fff; font-weight: 600;
}
.avatar-fallback.small { width: 48px; height: 48px; font-size: 16px; }
</style>
