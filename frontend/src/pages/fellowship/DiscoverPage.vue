<template>
  <div class="home-page">
    <header class="home-header">
      <div class="home-logo">
        <img class="home-logo-icon" :src="loveCubeIcon" alt="">
        <span class="home-logo-text">Love Cube</span>
      </div>
      <button class="home-avatar-btn" @click="router.push(fellowshipPath('/me'))">
        <van-icon name="contact" size="22" color="#ff5f84" />
      </button>
    </header>

    <template v-if="!renderFallback">
      <div v-if="loadingHome" class="home-skeleton" aria-hidden="true">
        <div class="skeleton-banner"></div>
        <div class="skeleton-card"></div>
        <div class="skeleton-card"></div>
      </div>

      <template v-else>
      <div v-if="showProfileReminder" class="tip-card">
        <div class="tip-left">
          <div class="tip-icon">!</div>
          <div>
            <p class="tip-title">完善资料可提升推荐效</p>
            <p class="tip-desc">当前资料完成度 {{ completion.percent || 0 }}%，建议先补充关键信息</p>
          </div>
        </div>
        <van-button round size="small" color="#ff5f84" @click="router.push(fellowshipPath('/profile/edit'))">
          完善资料
        </van-button>
      </div>

      <div v-if="showVerifyReminder" class="tip-card tip-card--blue">
        <div class="tip-left">
          <div class="tip-icon tip-icon--blue">✓</div>
          <div>
            <p class="tip-title">完成真人认证，提高信任度</p>
            <p class="tip-desc">认证后更容易获得回应与推荐</p>
          </div>
        </div>
        <van-button round size="small" plain color="#2563eb" @click="router.push(fellowshipPath('/verify'))">
          去认证
        </van-button>
      </div>

      <div class="banner-wrap">
        <van-swipe :autoplay="3000" indicator-color="#ff5f84" class="banner-swipe">
          <van-swipe-item v-for="(item, index) in displayBanners" :key="item.id">
            <img
              :src="item.imageUrl"
              class="banner-img"
              alt="banner"
              :loading="index === 0 ? 'eager' : 'lazy'"
              :fetchpriority="index === 0 ? 'high' : 'auto'"
              decoding="async"
              @error="onImgError($event, index)"
            />
          </van-swipe-item>
          <van-swipe-item v-if="!displayBanners.length">
            <div class="banner-fallback">
              <p class="banner-fallback-kicker">Love Cube Fellowship</p>
              <p class="banner-fallback-title">立即开始匹配</p>
            </div>
          </van-swipe-item>
        </van-swipe>
      </div>

      <div class="section-card">
        <div class="section-head">
          <span class="section-title">为你推荐</span>
          <span class="section-more" @click="router.push(fellowshipPath('/match'))">查看更多</span>
        </div>
        <div v-if="safeRecommends.length" class="rec-scroll">
          <div
            v-for="user in safeRecommends"
            :key="user.userId || user.userid"
            class="rec-item"
            @click="router.push(fellowshipPath(`/user-profile/${user.userId || user.userid}`))"
          >
            <div class="rec-avatar-ring">
              <van-image
                round
                width="60"
                height="60"
                :src="getAvatar(user)"
                fit="cover"
                class="rec-avatar"
              >
                <template #error>
                  <div class="avatar-fb size60">{{ (user.nickname || user.username || '?')[0] }}</div>
                </template>
              </van-image>
            </div>
            <p class="rec-name">{{ user.nickname || user.username }}</p>
            <p class="rec-meta">{{ user.age || '--' }} 岁</p>
          </div>
        </div>
        <div v-else class="empty-guide">
          <van-empty description="当前推荐较少，去完善资料试试" image-size="64" />
          <van-button round type="primary" size="small" color="#ff5f84" @click="router.push(fellowshipPath('/profile/edit'))">
            去完善资料
          </van-button>
        </div>
      </div>

      <div class="section-card">
        <div class="section-head">
          <span class="section-title">新人</span>
          <span class="section-more" @click="router.push(fellowshipPath('/newcomers'))">查看更多</span>
        </div>
        <div v-if="safeNewcomers.length" class="nc-list">
          <div
            v-for="user in safeNewcomers"
            :key="user.userId || user.userid"
            class="nc-item"
            @click="router.push(fellowshipPath(`/user-profile/${user.userId || user.userid}`))"
          >
            <van-image round width="48" height="48" :src="getAvatar(user)" fit="cover">
              <template #error>
                <div class="avatar-fb size48">{{ (user.nickname || user.username || '?')[0] }}</div>
              </template>
            </van-image>
            <div class="nc-info">
              <p class="nc-name">{{ user.nickname || user.username }}</p>
              <p class="nc-meta">{{ user.age || '--' }} 岁 · {{ user.location || '未知' }}</p>
            </div>
            <button class="nc-greet-btn" @click.stop="router.push(fellowshipPath(`/user-profile/${user.userId || user.userid}`))">
              打招呼
            </button>
          </div>
        </div>
        <van-empty v-else description="暂无新人，稍后再来看看" image-size="60" />
      </div>
      </template>
    </template>
    <div v-else class="section-card fallback-shell">
      <div class="section-head">
        <span class="section-title">首页内容加载</span>
      </div>
      <div class="fallback-body">
        <p>当前网络或数据格式异常，已切换到安全模式</p>
        <van-button round type="primary" size="small" color="#ff5f84" @click="router.go(0)">
          重新加载
        </van-button>
      </div>
    </div>

    <AppTabBar />
  </div>
</template>

<script setup>
import { computed, onErrorCaptured, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppTabBar from '@/components/AppTabBar.vue'
import { getHomeInit } from '@/api/home.js'
import { useFellowshipProfileStore } from '@/stores/fellowshipProfile.js'
import banner1 from '@/assets/fellowship/home-banners/fellowship-home-banner-1.webp'
import banner2 from '@/assets/fellowship/home-banners/fellowship-home-banner-2.webp'
import banner3 from '@/assets/fellowship/home-banners/fellowship-home-banner-3.webp'
import loveCubeIcon from '@/assets/brand/love-cube-icon.svg'
import { getAvatar } from '@/utils/image.js'
import { useFellowshipNavBase } from '@/composables/useFellowshipNavBase.js'

const router = useRouter()
const { fellowshipPath } = useFellowshipNavBase()
const profileStore = useFellowshipProfileStore()

// Module-level cache: stale-while-revalidate, 5-min TTL
const CACHE_TTL_MS = 5 * 60 * 1000
const _cache = { banners: null, recommends: null, newcomers: null, ts: 0 }

const banners = ref([])
const recommends = ref([])
const newcomers = ref([])
const completion = ref({ completed: false, percent: 0, missingFields: [] })
const verificationStatus = ref('none')
const renderFallback = ref(false)
const loadingHome = ref(true)

const localBanners = [
  { id: 'local-banner-1', imageUrl: banner1 },
  { id: 'local-banner-2', imageUrl: banner2 },
  { id: 'local-banner-3', imageUrl: banner3 }
]

const remoteBanners = computed(() => {
  const list = Array.isArray(banners.value) ? banners.value : []
  return list
    .map((item, index) => ({
      id: item?.id || `remote-banner-${index}`,
      imageUrl: resolveBannerImage(item)
    }))
    .filter((item) => item.imageUrl)
})

const displayBanners = computed(() => (remoteBanners.value.length ? remoteBanners.value : localBanners))
const safeRecommends = computed(() => (Array.isArray(recommends.value) ? recommends.value.slice(0, 3) : []))
const safeNewcomers = computed(() => (Array.isArray(newcomers.value) ? newcomers.value.slice(0, 3) : []))

const showProfileReminder = computed(() => {
  if (!completion.value) return false
  if (completion.value.completed) return false
  return Number(completion.value.percent || 0) < 80
})

const showVerifyReminder = computed(() => {
  const status = String(verificationStatus.value || '').toLowerCase()
  return !['approved', 'verified'].includes(status)
})

onErrorCaptured((err) => {
  console.error('[fellowship-home] render error:', err)
  renderFallback.value = true
  return false
})

onMounted(async () => {
  // 有未过期缓存时立即展示，同时在后台刷新（stale-while-revalidate）
  const isFresh = _cache.banners !== null && (Date.now() - _cache.ts) < CACHE_TTL_MS
  if (isFresh) {
    banners.value = _cache.banners
    recommends.value = _cache.recommends
    newcomers.value = _cache.newcomers
    loadingHome.value = false
  }

  try {
    // 单次请求获取全部首页数据，避免 5 个串行 RTT
    const res = await getHomeInit()

    if (Array.isArray(res?.banners)) {
      _cache.banners = res.banners
      banners.value = res.banners
    }
    if (Array.isArray(res?.recommends)) {
      _cache.recommends = res.recommends
      recommends.value = res.recommends
    }
    if (Array.isArray(res?.newcomers)) {
      _cache.newcomers = res.newcomers
      newcomers.value = res.newcomers
    }
    _cache.ts = Date.now()

    if (res?.completion) {
      completion.value = res.completion
      profileStore.completion = res.completion
    }
    if (res?.profile) {
      profileStore.profile = res.profile
      const raw = res.profile.reviewStatus || res.profile.verificationStatus || res.profile.verified
      verificationStatus.value = raw === true ? 'approved' : String(raw || 'none')
    }
  } catch {
    // init 失败时缓存内容已展示，不阻塞页面
  } finally {
    loadingHome.value = false
  }
})

function resolveBannerImage(item) {
  if (!item) return ''
  const raw = typeof item === 'string'
    ? item
    : (
    item.imageUrl ||
    item.image_url ||
    item.imgUrl ||
    item.img_url ||
    item.image ||
    item.url ||
    item.coverUrl ||
    item.cover_url ||
    item.cover ||
    item.picUrl ||
    item.pic_url ||
    ''
  )
  return normalizeBannerUrl(raw)
}

function normalizeBannerUrl(rawUrl) {
  const value = String(rawUrl || '').trim()
  if (!value) return ''
  if (value.startsWith('data:') || value.startsWith('blob:')) return value

  const apiBase = String(import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '')
  const toBaseUrl = (pathWithQuery = '') => {
    if (!apiBase) return pathWithQuery
    if (apiBase.endsWith('/admin') && pathWithQuery.startsWith('/admin/')) {
      return `${apiBase}${pathWithQuery.slice('/admin'.length)}`
    }
    if (pathWithQuery.startsWith('/')) return `${apiBase}${pathWithQuery}`
    return `${apiBase}/${pathWithQuery}`
  }

  if (value.startsWith('/')) return toBaseUrl(value)
  if (!/^https?:\/\//i.test(value)) return toBaseUrl(value)

  try {
    const parsed = new URL(value)
    const fullPath = `${parsed.pathname || ''}${parsed.search || ''}${parsed.hash || ''}`
    if (fullPath.startsWith('/admin/')) {
      return toBaseUrl(fullPath)
    }
    return value
  } catch {
    return value
  }
}

function onImgError(event, index) {
  const img = event.target
  const fallback = localBanners[index % localBanners.length]?.imageUrl
  if (fallback && img.dataset.fallbackApplied !== 'true') {
    img.dataset.fallbackApplied = 'true'
    img.src = fallback
    return
  }
  img.style.display = 'none'
}
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: #f4f6fb;
  padding-bottom: 72px;
}

.home-skeleton {
  margin: 12px 12px 0;
  display: grid;
  gap: 12px;
}

.skeleton-banner,
.skeleton-card {
  position: relative;
  overflow: hidden;
  border-radius: 14px;
  background: #edf2f9;
}

.skeleton-banner::after,
.skeleton-card::after {
  content: '';
  position: absolute;
  inset: 0;
  transform: translateX(-100%);
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.84), transparent);
  animation: skeleton-slide 1.2s ease infinite;
}

.skeleton-banner {
  height: 190px;
}

.skeleton-card {
  height: 136px;
}

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
  width: 24px;
  height: 24px;
  border-radius: 7px;
  display: block;
}

.home-logo-text {
  font-size: 18px;
  font-weight: 800;
  color: #1a2236;
  letter-spacing: 0;
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
}

.tip-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin: 12px 12px 0;
  padding: 12px 14px;
  background: #fff6f8;
  border: 1px solid #ffd8e4;
  border-radius: 12px;
}

.tip-card--blue {
  background: #f4f8ff;
  border-color: #dbeafe;
}

.tip-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 0;
}

.tip-icon {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: #ff5f84;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.tip-icon--blue {
  background: #2563eb;
}

.tip-title {
  font-size: 13px;
  font-weight: 700;
  color: #1a2236;
}

.tip-desc {
  margin-top: 2px;
  font-size: 12px;
  color: #64748b;
}

.banner-wrap {
  margin: 12px 12px 0;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(15, 23, 42, 0.08);
  background: linear-gradient(135deg, #fff0f4, #eff6ff);
}

.banner-swipe {
  height: 190px;
}

.banner-img {
  width: 100%;
  height: 190px;
  object-fit: cover;
  display: block;
}

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
}

.section-card {
  margin: 12px 12px 0;
  padding: 16px 0 12px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(15, 23, 42, 0.05);
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
  color: #ff5f84;
  cursor: pointer;
  font-weight: 500;
}

.rec-scroll {
  display: flex;
  gap: 8px;
  padding: 0 16px 4px;
  overflow-x: auto;
  scrollbar-width: none;
}

.rec-scroll::-webkit-scrollbar {
  display: none;
}

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
  background: linear-gradient(135deg, #ff5f84, #ffb3c4);
  margin: 0 auto 6px;
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

.empty-guide {
  padding: 0 16px 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.fallback-shell {
  margin-top: 12px;
}

.fallback-body {
  padding: 4px 16px 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  color: #64748b;
  font-size: 13px;
}

.nc-list {
  display: flex;
  flex-direction: column;
  padding: 0 16px;
}

.nc-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 11px 0;
  cursor: pointer;
  border-bottom: 1px solid #f4f6fb;
}

.nc-item:last-child {
  border-bottom: none;
}

.nc-info {
  flex: 1;
  min-width: 0;
}

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
  height: 30px;
  padding: 0 14px;
  border-radius: 999px;
  border: 1px solid #ff5f84;
  background: transparent;
  color: #ff5f84;
  font-size: 12px;
  font-weight: 600;
}

.avatar-fb {
  border-radius: 50%;
  background: linear-gradient(135deg, #ff5f84, #ffb3c4);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 700;
}

.size60 {
  width: 60px;
  height: 60px;
  font-size: 20px;
}

.size48 {
  width: 48px;
  height: 48px;
  font-size: 16px;
}

@keyframes skeleton-slide {
  to {
    transform: translateX(100%);
  }
}

@media (max-width: 375px) {
  .tip-card {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>


