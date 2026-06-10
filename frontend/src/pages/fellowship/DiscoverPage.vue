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
            <p class="tip-title">完善资料可提升推荐效果</p>
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

      <div v-if="!onboardingAllDone" class="onboard-card">
        <div class="onboard-head">
          <p class="onboard-title">新手引导 · {{ onboardingProgress }}%</p>
          <span class="onboard-more" @click="router.push(fellowshipPath('/tasks'))">任务中心</span>
        </div>
        <div class="onboard-steps">
          <button
            v-for="step in onboardingSteps"
            :key="step.key"
            type="button"
            class="onboard-step"
            :class="{ done: step.done, current: !step.done && currentOnboardStep?.key === step.key }"
            @click="router.push(fellowshipPath(step.to))"
          >
            <span class="step-dot">{{ step.done ? '✓' : '' }}</span>
            <span class="step-label">{{ step.label }}</span>
          </button>
        </div>
        <p v-if="currentOnboardStep" class="onboard-hint">下一步：{{ currentOnboardStep.label }}</p>
      </div>

      <div
        v-if="newcomerPack.eligible && newcomerPendingCount > 0"
        class="tip-card tip-card--purple"
        @click="router.push(fellowshipPath('/tasks'))"
      >
        <div class="tip-left">
          <div class="tip-icon tip-icon--purple">7</div>
          <div>
            <p class="tip-title">新人 7 日任务 · 第 {{ newcomerPack.currentDay }} 天</p>
            <p class="tip-desc">还有 {{ newcomerPendingCount }} 项待完成，点我查看</p>
          </div>
        </div>
        <van-icon name="arrow" size="16" color="#7c3aed" />
      </div>

      <div
        v-if="pendingEventReviewCount > 0"
        class="tip-card tip-card--amber"
        @click="router.push(fellowshipPath('/event-signups'))"
      >
        <div class="tip-left">
          <div class="tip-icon tip-icon--amber">★</div>
          <div>
            <p class="tip-title">活动互评待完成</p>
            <p class="tip-desc">还有 {{ pendingEventReviewCount }} 位伙伴等你评价</p>
          </div>
        </div>
        <van-icon name="arrow" size="16" color="#d97706" />
      </div>

      <div class="quick-grid">
        <button type="button" class="quick-item" @click="router.push(fellowshipPath('/match'))">
          <van-icon name="like-o" size="22" color="#ff5f84" />
          <span>开始滑卡</span>
        </button>
        <button type="button" class="quick-item" @click="router.push('/m/platform/checkin')">
          <van-icon name="calendar-o" size="22" color="#2563eb" />
          <span>每日签到</span>
        </button>
        <button type="button" class="quick-item" @click="router.push(fellowshipPath('/tasks'))">
          <van-icon name="todo-list-o" size="22" color="#059669" />
          <span>今日任务</span>
          <em v-if="dailyTaskSummary" class="quick-badge">{{ dailyTaskSummary }}</em>
        </button>
        <button type="button" class="quick-item" @click="router.push(fellowshipPath('/event-signups'))">
          <van-icon name="flag-o" size="22" color="#d97706" />
          <span>我的活动</span>
        </button>
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

      <div v-if="upcomingEvents.length" class="section-card">
        <div class="section-head">
          <span class="section-title">近期活动</span>
          <span class="section-more" @click="router.push('/events')">全部活动</span>
        </div>
        <div class="event-list">
          <div
            v-for="ev in upcomingEvents"
            :key="ev.id"
            class="event-item"
            @click="router.push(`/events/${ev.id}`)"
          >
            <div class="event-date">{{ formatEventDate(ev) }}</div>
            <div class="event-body">
              <p class="event-title">{{ ev.title || ev.name }}</p>
              <p class="event-meta">{{ ev.location || '线上/线下' }}</p>
            </div>
            <van-icon name="arrow" size="14" color="#a0abbe" />
          </div>
        </div>
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
              <span v-if="completionBadge(user)" class="rec-complete-badge">{{ completionBadge(user) }}</span>
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
import { getMyGrowth } from '@/api/growth.js'
import { fetchEvents, getMyEventSignups } from '@/api/platformContent.js'
import { useFellowshipProfileStore } from '@/stores/fellowshipProfile.js'
import { useFellowshipOnboarding } from '@/composables/useFellowshipOnboarding.js'
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
const upcomingEvents = ref([])
const newcomerPack = ref({ eligible: false, currentDay: 0 })
const newcomerTasks = ref([])
const dailyTasks = ref([])
const pendingEventReviewCount = ref(0)

const isVerified = computed(() => {
  const status = String(verificationStatus.value || '').toLowerCase()
  return ['approved', 'verified'].includes(status)
})

const completionPercent = computed(() => Number(completion.value?.percent || 0))

const {
  steps: onboardingSteps,
  allDone: onboardingAllDone,
  currentStep: currentOnboardStep,
  progressPercent: onboardingProgress
} = useFellowshipOnboarding({
  completionPercent,
  verified: isVerified
})

const newcomerPendingCount = computed(() =>
  newcomerTasks.value.filter((t) => t.unlocked && (!t.completed || !t.claimed)).length
)

const dailyTaskSummary = computed(() => {
  if (!dailyTasks.value.length) return ''
  const done = dailyTasks.value.filter((t) => t.completed).length
  return `${done}/${dailyTasks.value.length}`
})

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
    const [initRes, growthRes, eventsRes, signupsRes] = await Promise.allSettled([
      getHomeInit(),
      getMyGrowth().catch(() => null),
      fetchEvents({ status: 'published' }).catch(() => []),
      getMyEventSignups().catch(() => [])
    ])

    const res = initRes.status === 'fulfilled' ? initRes.value : null

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

    if (growthRes.status === 'fulfilled' && growthRes.value) {
      const g = growthRes.value
      dailyTasks.value = Array.isArray(g.dailyTasks) ? g.dailyTasks : []
      const pack = g.newcomerPack || {}
      newcomerPack.value = {
        eligible: Boolean(pack.eligible),
        currentDay: Number(pack.currentDay ?? 0)
      }
      newcomerTasks.value = Array.isArray(pack.tasks) ? pack.tasks : []
    }

    if (eventsRes.status === 'fulfilled') {
      const list = Array.isArray(eventsRes.value) ? eventsRes.value : []
      upcomingEvents.value = list.slice(0, 3)
    }

    if (signupsRes.status === 'fulfilled') {
      const signups = Array.isArray(signupsRes.value) ? signupsRes.value : []
      pendingEventReviewCount.value = signups.reduce(
        (sum, item) => sum + Number(item.pendingReviewCount || 0),
        0
      )
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

function completionBadge(user) {
  const rate = Number(user?.completionRate ?? 0)
  if (rate >= 80) return '完善'
  if (rate >= 50) return '较完整'
  return ''
}

function formatEventDate(ev) {
  const raw = ev?.startTime || ev?.start_time || ev?.eventDate || ev?.date || ''
  if (!raw) return '近期'
  try {
    const d = new Date(raw)
    if (Number.isNaN(d.getTime())) return '近期'
    return `${d.getMonth() + 1}/${d.getDate()}`
  } catch {
    return '近期'
  }
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

.tip-card--purple {
  background: #faf5ff;
  border-color: #e9d5ff;
  cursor: pointer;
}

.tip-card--amber {
  background: #fffbeb;
  border-color: #fde68a;
  cursor: pointer;
}

.tip-icon--purple {
  background: #7c3aed;
}

.tip-icon--amber {
  background: #d97706;
}

.onboard-card {
  margin: 12px 12px 0;
  padding: 14px 16px;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 2px 12px rgba(15, 23, 42, 0.05);
}

.onboard-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.onboard-title {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: #1a2236;
}

.onboard-more {
  font-size: 12px;
  color: #ff5f84;
  font-weight: 600;
}

.onboard-steps {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
}

.onboard-step {
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: #f8fafc;
  padding: 8px 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.onboard-step.done {
  border-color: #bbf7d0;
  background: #f0fdf4;
}

.onboard-step.current {
  border-color: #ffb3c4;
  background: #fff5f8;
}

.step-dot {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #e2e8f0;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.onboard-step.done .step-dot {
  background: #10b981;
}

.onboard-step.current .step-dot {
  background: #ff5f84;
}

.step-label {
  font-size: 10px;
  color: #64748b;
  line-height: 1.2;
}

.onboard-hint {
  margin: 10px 0 0;
  font-size: 12px;
  color: #64748b;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
  margin: 12px 12px 0;
}

.quick-item {
  position: relative;
  border: none;
  border-radius: 14px;
  background: #fff;
  padding: 12px 6px 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  box-shadow: 0 2px 10px rgba(15, 23, 42, 0.05);
  cursor: pointer;
}

.quick-item span {
  font-size: 11px;
  font-weight: 600;
  color: #475569;
}

.quick-badge {
  position: absolute;
  top: 6px;
  right: 6px;
  font-style: normal;
  font-size: 10px;
  font-weight: 700;
  color: #fff;
  background: #ff5f84;
  border-radius: 999px;
  padding: 1px 5px;
}

.event-list {
  padding: 0 16px 4px;
}

.event-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f1f5f9;
  cursor: pointer;
}

.event-item:last-child {
  border-bottom: none;
}

.event-date {
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  border-radius: 10px;
  background: #fff0f4;
  color: #ff5f84;
  font-size: 13px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
}

.event-title {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #1a2236;
}

.event-meta {
  margin: 4px 0 0;
  font-size: 12px;
  color: #8898aa;
}

.event-body {
  flex: 1;
  min-width: 0;
}

.rec-complete-badge {
  position: absolute;
  top: -2px;
  right: -2px;
  z-index: 2;
  font-size: 9px;
  font-weight: 700;
  color: #fff;
  background: #10b981;
  border-radius: 999px;
  padding: 2px 5px;
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
  position: relative;
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


