<template>
  <div class="me-page">
    <header class="page-header">
      <h1 class="page-title">个人中心</h1>
      <p class="header-tip">完善资料提升匹配率</p>
      <div class="header-actions">
        <button class="header-icon-btn" @click="router.push('/fellowship/settings')">
          <van-icon name="setting-o" size="20" />
        </button>
        <button class="header-icon-btn" @click="router.push('/fellowship/notifications')">
          <van-icon name="bell" size="20" />
          <span v-if="notificationCount > 0" class="badge-dot">{{ notificationCount }}</span>
        </button>
      </div>
    </header>

    <main class="me-content">
      <div v-if="loadingPage" class="me-skeleton" aria-hidden="true">
        <div class="skeleton-block tall"></div>
        <div class="skeleton-block"></div>
        <div class="skeleton-block"></div>
      </div>
      <section v-show="!loadingPage" class="profile-card">
        <div class="profile-main">
          <div class="avatar-wrap" :class="{ disabled: avatarUploading }" @click="chooseAvatar">
            <img class="avatar" :src="displayAvatar" alt="头像" />
            <div class="avatar-camera">
              <van-icon name="photo-o" size="14" />
            </div>
          </div>

          <div class="profile-info">
            <div class="name-block">
              <h2 class="nickname">{{ displayName }}</h2>
              <div class="badge-row">
                <button class="verified-badge" :class="verificationLevel" type="button" @click="router.push('/fellowship/verify')">
                  <span class="verified-mark">V</span>
                  <span>{{ verificationLabel }}</span>
                </button>
                <button class="vip-medal" :class="{ active: vipActive }" type="button" @click="router.push('/fellowship/vip')">
                  <span class="vip-crown">VIP</span>
                  <span>{{ vipActive ? '已开通' : '尊享' }}</span>
                </button>
              </div>
            </div>
            <p class="base-info">{{ displayAge }}岁 · {{ displayCity }} · {{ displayJob }}</p>
            <p class="intro">{{ displayIntro }}</p>
            <div class="identity-strip" @click="router.push('/fellowship/verify')">
              <span v-for="item in identityPerks" :key="item">{{ item }}</span>
            </div>
            <div class="tags-row">
              <span class="tag-chip">{{ profileMeta[0] }}</span>
              <span class="tag-chip">{{ profileMeta[1] }}</span>
              <span class="tag-chip">{{ profileMeta[2] }}</span>
              <span class="tag-chip">{{ profileMeta[3] }}</span>
              <span class="tag-chip">{{ profileMeta[4] }}</span>
            </div>
          </div>

          <button class="edit-link" @click="router.push('/fellowship/profile/edit')">编辑资料</button>
          <button class="message-link" @click="router.push('/fellowship/messages')">消息</button>
          <div class="premium-seal" @click="router.push('/fellowship/vip')">
            <span>LOVE</span>
            <strong>PREMIUM</strong>
          </div>
          <div class="heart-glow" />
        </div>
      </section>

      <section v-show="!loadingPage" v-if="swipeQuota && !swipeQuota.unlimited" class="swipe-quota-banner" @click="router.push('/fellowship/vip')">
        <div>
          <p class="sq-title">今日滑卡 {{ swipeQuota.used }}/{{ swipeQuota.limit }}</p>
          <p class="sq-sub">开通 VIP 无限滑 · 解锁访客与喜欢名单</p>
        </div>
        <van-icon name="arrow" size="16" />
      </section>

      <section v-show="!loadingPage" class="play-hub-banner" @click="router.push('/m/platform')">
        <div>
          <p class="sq-title">成长玩法中心</p>
          <p class="sq-sub">任务 · 签到 · 心声 · 互助</p>
        </div>
        <van-icon name="arrow" size="16" />
      </section>

      <section v-show="!loadingPage" class="status-panel">
        <div class="completion-cell">
          <p class="cell-title">资料完整度 {{ completionPercent }}%</p>
          <div class="progress-track">
            <div class="progress-bar" :style="{ width: `${completionPercent}%` }" />
          </div>
          <p class="cell-sub">{{ completionHint }}</p>
          <button class="minor-link" @click="router.push('/fellowship/profile/edit')">去完善</button>
        </div>
        <div class="status-cell">
          <div class="metric-icon metric-trend">
            <van-icon name="chart-trending-o" size="22" />
          </div>
          <p class="status-title">推荐指数</p>
          <p class="status-value">B+</p>
        </div>
        <div class="status-cell">
          <div class="metric-icon metric-fire">
            <van-icon name="fire" size="22" />
          </div>
          <p class="status-title">活跃等级</p>
          <p class="status-value">Lv.4</p>
        </div>
        <div class="status-cell">
          <div class="metric-icon metric-eye">
            <van-icon name="eye" size="22" />
          </div>
          <p class="status-title">曝光增加</p>
          <p class="status-value">+30%</p>
        </div>
      </section>

      <section v-show="!loadingPage" class="menu-grid-card">
        <button v-for="item in menuItems" :key="item.key" class="menu-item" @click="goTo(item.to)">
          <div class="menu-icon" :class="`menu-${item.theme}`">
            <van-icon :name="item.icon" size="22" />
          </div>
          <p class="menu-title">{{ item.title }}</p>
          <p class="menu-sub">{{ item.sub }}</p>
        </button>
      </section>

      <section v-show="!loadingPage" class="photos-card">
        <div class="block-header">
          <h3>我的照片</h3>
          <button class="block-link" @click="router.push('/fellowship/profile-edit')">全部照片 ></button>
        </div>
        <div class="photos-row">
          <div v-for="(photo, index) in displayPhotos" :key="photo.id || index" class="photo-item">
            <img :src="photo.url" alt="照片" />
            <span v-if="index === 0" class="main-tag">主照</span>
            <button
              class="remove-photo-btn"
              type="button"
              :disabled="photoDeleting"
              @click.stop="removePhoto(photo, index)"
            >
              删除
            </button>
          </div>
          <button class="upload-item" :disabled="photoUploading || photoDeleting" @click="choosePhoto">
            <van-icon name="plus" size="22" />
            <span>上传照片</span>
            <small>({{ displayPhotos.length }}/9)</small>
          </button>
        </div>
      </section>

      <section v-show="!loadingPage" class="activity-card">
        <div class="block-header">
          <h3>平台公告</h3>
          <button class="block-link" @click="router.push('/fellowship/messages')">全部 ></button>
        </div>
        <ul class="activity-list">
          <li v-for="item in activityItems" :key="item.title" class="activity-item">
            <div class="activity-dot" :class="`dot-${item.theme}`">
              <van-icon :name="item.icon" size="12" />
            </div>
            <div class="activity-text">
              <p>{{ item.title }}</p>
              <span>{{ item.time }}</span>
            </div>
            <button v-if="item.action" class="activity-action" @click="goTo(item.action.to)">{{ item.action.label }}</button>
          </li>
        </ul>
      </section>

      <section v-show="!loadingPage" class="vip-banner" @click="router.push('/fellowship/vip')">
        <div class="vip-banner-main">
          <span class="vip-banner-kicker">Love Cube Premium</span>
          <p class="vip-title">开通会员 · 让优质身份被优先看见</p>
          <span class="vip-sub">专属金标、优先曝光、访客解锁，喜欢你的人一键查</span>
        </div>
        <button type="button">升级身份</button>
      </section>
    </main>

    <AppTabBar />

    <input ref="avatarInput" type="file" accept="image/*" class="hidden-input" @change="onAvatarSelected" />
    <input ref="photoInput" type="file" accept="image/*" multiple class="hidden-input" @change="onPhotoSelected" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import AppTabBar from '@/components/AppTabBar.vue'
import { useUserStore } from '@/stores/user.js'
import {
  deleteFellowshipPhoto,
  getFellowshipPhotos,
  getFellowshipProfileCompletion,
  getMyFellowshipProfile,
  saveFellowshipPhotos,
  updateMyFellowshipProfile,
  uploadFellowshipAvatar,
  uploadFellowshipPhoto
} from '@/api/fellowshipProfile.js'
import { getFellowshipMeStatsCached } from '@/api/fellowship.js'
import { getNotifUnreadCountCached } from '@/api/notification.js'
import { userHasVerificationBadge } from '@/utils/displayFields.js'

const router = useRouter()
const userStore = useUserStore()
const avatarInput = ref(null)
const photoInput = ref(null)

const userInfo = computed(() => userStore.userInfo || {})
const profile = ref({
  nickname: '',
  avatarUrl: '',
  age: '',
  city: '',
  reviewStatus: 'none',
  profileCompletion: 0
})
const completion = ref({ percent: 0, missingFields: [] })
const photoList = ref([])
const photoDeleting = ref(false)
const avatarUploading = ref(false)
const photoUploading = ref(false)
const loadingPage = ref(true)
const notificationCount = ref(0)
const fellowshipStats = ref({
  todayVisitorCount: 0,
  totalVisitorCount: 0,
  likesReceived: 0,
  mutualMatchCount: 0,
  followingCount: 0,
  blacklistCount: 0,
  eventSignupCount: 0,
  vipActive: false,
  swipeQuota: null
})

const vipActive = computed(() => Boolean(fellowshipStats.value.vipActive))
const swipeQuota = computed(() => fellowshipStats.value.swipeQuota || null)

const displayName = computed(() => profile.value.nickname || userInfo.value?.username || 'Love Cube 用户')
const displayAvatar = computed(
  () => profile.value.avatarUrl || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'
)
const displayAge = computed(() => profile.value.age || 28)
const displayCity = computed(() => profile.value.city || '保定市')
const displayJob = computed(() => profile.value.job || '产品经理')
const displayIntro = computed(() => profile.value.bio || '认真生活、真诚交友，期待遇见对的人。')
const displayPhotos = computed(() => photoList.value.slice(0, 4))
const completionPercent = computed(() => {
  const raw = completion.value || {}
  const candidates = [raw.percent, raw.completionPercent, raw.completionRate, raw.profileCompletion]
  const firstValid = candidates.find((item) => Number.isFinite(Number(item)))
  const numeric = Number(firstValid || 0)
  const percent = numeric > 0 && numeric <= 1 ? numeric * 100 : numeric
  return Math.min(100, Math.max(0, Math.round(percent)))
})
const completionMissingCount = computed(() => {
  const raw = completion.value || {}
  if (Array.isArray(raw.missingFields) && raw.missingFields.length > 0) return raw.missingFields.length
  if (Array.isArray(raw.unfinishedFields) && raw.unfinishedFields.length > 0) return raw.unfinishedFields.length
  const total = Number(raw.totalFields ?? raw.totalCount ?? raw.totalItems ?? 0)
  if (total > 0) {
    const completed = Number(raw.completedFields ?? raw.completedCount ?? raw.completedItems ?? 0)
    return Math.max(0, total - completed)
  }
  return completionPercent.value >= 100 ? 0 : null
})
const completionHint = computed(() => {
  if (completionMissingCount.value === 0) return '资料已完善，匹配展示更充分'
  if (completionMissingCount.value && completionMissingCount.value > 0) return `再完善 ${completionMissingCount.value} 项，匹配率可继续提升`
  return '完善更多资料，可提升匹配率'
})
const effectiveVerificationRow = computed(() => ({
  verificationStatus:
    profile.value.verificationStatus ??
    userInfo.value?.verificationStatus ??
    profile.value.reviewStatus ??
    'none',
  photoVerified: profile.value.photoVerified ?? userInfo.value?.photoVerified,
  realnameVerified: profile.value.realnameVerified ?? userInfo.value?.realnameVerified
}))
const isUserCertified = computed(() => userHasVerificationBadge(effectiveVerificationRow.value))
const verificationStatus = computed(() => String(effectiveVerificationRow.value.verificationStatus || 'none'))
const verificationLabel = computed(() => {
  if (isUserCertified.value) return '已认证'
  if (verificationStatus.value === 'pending') return '认证审核中'
  if (verificationStatus.value === 'rejected') return '未通过'
  return '待认证'
})
const verificationLevel = computed(() => {
  if (isUserCertified.value) return 'is-approved'
  if (verificationStatus.value === 'pending') return 'is-pending'
  if (verificationStatus.value === 'rejected') return 'is-rejected'
  return 'is-none'
})
const profileMeta = computed(() => [
  profile.value.height ? `${profile.value.height}cm` : '--',
  profile.value.weight ? `${profile.value.weight}kg` : '--',
  profile.value.education || '--',
  profile.value.houseStatus || '--',
  profile.value.smokeStatus || '--'
])

const identityPerks = ['真人核验', '资料可信', '优先推荐']

const menuItems = computed(() => {
  const s = fellowshipStats.value
  return [
    {
      key: 'notifications',
      title: '消息中心',
      sub: notificationCount.value > 0 ? `${notificationCount.value}条未读` : '系统与互动通知',
      icon: 'bell',
      to: '/fellowship/notifications',
      theme: 'rose'
    },
    { key: 'match', title: '我的匹配', sub: s.mutualMatchCount > 0 ? `${s.mutualMatchCount}人与你匹配` : '查看匹配', icon: 'like', to: '/fellowship/my-likes?tab=mutual', theme: 'pink' },
    { key: 'visitor', title: '谁看过我', sub: s.todayVisitorCount > 0 ? `今日+${s.todayVisitorCount}` : '查看访客', icon: 'eye', to: '/fellowship/messages?tab=visitor', theme: 'blue' },
    { key: 'likes', title: '喜欢我的人', sub: s.likesReceived > 0 ? `${s.likesReceived}人喜欢你` : '查看喜欢', icon: 'good-job', to: '/fellowship/liked-me', theme: 'yellow' },
    { key: 'play', title: '成长玩法', sub: '任务签到心声', icon: 'gem-o', to: '/m/platform', theme: 'teal' },
    { key: 'invite', title: '邀请码', sub: '邀请好友加入', icon: 'friends', to: '/fellowship/invite', theme: 'purple' },
    { key: 'signup', title: '我的报名', sub: s.eventSignupCount > 0 ? `${s.eventSignupCount}个报名中` : '查看报名', icon: 'calendar', to: '/fellowship/event-signups', theme: 'green' },
    { key: 'collect', title: '我的收藏', sub: s.followingCount > 0 ? `${s.followingCount}人` : '查看收藏', icon: 'star', to: '/fellowship/following', theme: 'orange' },
    { key: 'blacklist', title: '黑名单', sub: s.blacklistCount > 0 ? `${s.blacklistCount}人` : '管理黑名单', icon: 'shield', to: '/fellowship/blacklist', theme: 'indigo' },
    { key: 'privacy', title: '隐私设置', sub: '隐私与权限', icon: 'setting', to: '/fellowship/privacy', theme: 'gray' }
  ]
})

const activityItems = computed(() => {
  const s = fellowshipStats.value
  const items = []
  if (s.todayVisitorCount > 0) {
    items.push({ icon: 'eye-o', title: `今日有 ${s.todayVisitorCount} 人浏览了你`, time: '今日', theme: 'pink' })
  }
  if (s.likesReceived > 0) {
    items.push({ icon: 'good-job-o', title: `共有 ${s.likesReceived} 人对你感兴趣`, time: '累计', theme: 'yellow' })
  }
  return items
})

function normalizeProfile(data) {
  if (!data) return
  profile.value = {
    ...profile.value,
    ...data,
    avatarUrl: data.avatarUrl || data.avatar || profile.value.avatarUrl,
    nickname: data.nickname || data.userName || profile.value.nickname
  }
}

async function loadPageData() {
  try {
    const [p, c, photosRes] = await Promise.allSettled([
      getMyFellowshipProfile(),
      getFellowshipProfileCompletion(),
      getFellowshipPhotos()
    ])

    if (p.status === 'fulfilled') {
      normalizeProfile(p.value)
      userStore.refreshCurrentUser().catch(() => null)
    }
    if (c.status === 'fulfilled') completion.value = c.value || completion.value
    if (photosRes.status === 'fulfilled') {
      const photos = Array.isArray(photosRes.value?.photos)
        ? photosRes.value.photos
        : Array.isArray(photosRes.value)
          ? photosRes.value
          : []
      photoList.value = photos.map((url, idx) => ({ id: `${idx}-${url}`, url }))
    }
  } catch (err) {
    showToast({ type: 'fail', message: err.message || '个人中心加载失败，请稍后重试' })
  }
}

function chooseAvatar() {
  if (avatarUploading.value) return
  avatarInput.value?.click()
}

function choosePhoto() {
  if (photoUploading.value || photoDeleting.value) return
  photoInput.value?.click()
}

function parseUploadUrl(res) {
  return res?.url || res?.data?.url || res?.photoUrl || ''
}

async function onAvatarSelected(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file || avatarUploading.value) return
  avatarUploading.value = true
  try {
    const uploadRes = await uploadFellowshipAvatar(file)
    const url = parseUploadUrl(uploadRes)
    if (!url) throw new Error('头像上传返回为空')
    await updateMyFellowshipProfile({ avatarUrl: url })
    profile.value.avatarUrl = url
    showToast({ type: 'success', message: '头像已更新' })
  } catch (err) {
    showToast({ type: 'fail', message: err.message || '头像上传失败' })
  } finally {
    avatarUploading.value = false
  }
}

async function onPhotoSelected(event) {
  const files = Array.from(event.target.files || [])
  event.target.value = ''
  if (!files.length || photoUploading.value) return
  photoUploading.value = true
  try {
    const uploadResults = await Promise.all(files.map((file) => uploadFellowshipPhoto(file)))
    const uploadedUrls = uploadResults.map(parseUploadUrl).filter(Boolean)
    if (!uploadedUrls.length) throw new Error('照片上传返回为空')
    const next = [...uploadedUrls, ...photoList.value.map((item) => item.url)].slice(0, 9)
    await saveFellowshipPhotos(next)
    photoList.value = next.map((item, idx) => ({ id: `${idx}-${item}`, url: item }))
    showToast({ type: 'success', message: `成功上传 ${uploadedUrls.length} 张照片` })
  } catch (err) {
    showToast({ type: 'fail', message: err.message || '照片上传失败' })
  } finally {
    photoUploading.value = false
  }
}

async function removePhoto(photo, index) {
  const targetUrl = photo?.url || ''
  if (!targetUrl || photoDeleting.value) return

  try {
    await showConfirmDialog({
      title: '删除照片',
      message: '删除后不可恢复，确定继续吗？',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }

  photoDeleting.value = true
  try {
    await deleteFellowshipPhoto(targetUrl)
    photoList.value = photoList.value.filter((item, itemIndex) => !(item.url === targetUrl && itemIndex === index))
    showToast({ type: 'success', message: '照片已删除' })
  } catch (err) {
    showToast({ type: 'fail', message: err.message || '照片删除失败' })
  } finally {
    photoDeleting.value = false
  }
}

function goTo(path) {
  router.push(path)
}

onMounted(async () => {
  try {
    if (!userStore.userInfo) {
      await userStore.refreshCurrentUser().catch(() => null)
    }
    const [pageDataRes, notifRes, statsRes] = await Promise.allSettled([
      loadPageData(),
      getNotifUnreadCountCached(),
      getFellowshipMeStatsCached()
    ])
    if (pageDataRes.status === 'rejected') {
      console.warn('[me-page] loadPageData failed:', pageDataRes.reason)
    }
    if (notifRes.status === 'fulfilled') {
      notificationCount.value = Number(notifRes.value?.count ?? notifRes.value?.unreadCount ?? 0)
    }
    if (statsRes.status === 'fulfilled' && statsRes.value) {
      fellowshipStats.value = statsRes.value
    }
  } finally {
    loadingPage.value = false
  }
})
</script>

<style scoped>
.me-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f7f7fc 0%, #f4f5fb 210px, #f4f5fb 100%);
  padding-bottom: 84px;
}

.page-header {
  position: relative;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 13px 132px 9px 16px;
}

.page-title {
  margin: 0 6px 0 0;
  font-size: 22px;
  line-height: 1;
  font-weight: 800;
  color: #212334;
}

.header-tip {
  margin: 4px auto 0 0;
  font-size: 11px;
  color: #9497ab;
  background: rgba(255, 255, 255, 0.72);
  border-radius: 12px;
  padding: 2px 9px;
}

.header-actions {
  display: flex;
  gap: 6px;
}

.header-icon-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: none;
  color: #2f3347;
  background: transparent;
  box-shadow: none;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.badge-dot {
  position: absolute;
  right: -3px;
  top: -5px;
  min-width: 16px;
  height: 16px;
  border-radius: 8px;
  background: #ff4f78;
  color: #fff;
  border: 2px solid #fff;
  font-size: 10px;
  line-height: 12px;
  text-align: center;
  padding: 0 2px;
}

.me-content {
  padding: 6px 12px 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.me-skeleton {
  display: grid;
  gap: 10px;
}

.skeleton-block {
  position: relative;
  height: 100px;
  border-radius: 14px;
  overflow: hidden;
  background: #e9edf6;
}

.skeleton-block.tall {
  height: 180px;
}

.skeleton-block::after {
  content: '';
  position: absolute;
  inset: 0;
  transform: translateX(-100%);
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.84), transparent);
  animation: skeleton-slide 1.2s ease infinite;
}

.profile-card,
.status-panel,
.menu-grid-card,
.photos-card,
.activity-card {
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 1px 8px rgba(45, 57, 98, 0.05);
}

.profile-main {
  border-radius: 14px;
  background: linear-gradient(120deg, #fdf7fc 0%, #fff 58%, #ffeef6 100%);
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 13px 12px;
  position: relative;
  overflow: hidden;
}

.avatar-wrap {
  width: 74px;
  height: 74px;
  border-radius: 50%;
  overflow: hidden;
  flex: 0 0 auto;
  position: relative;
  border: 3px solid #fff;
  box-shadow: 0 7px 16px rgba(232, 82, 145, 0.16);
}

.avatar-wrap.disabled {
  pointer-events: none;
  opacity: 0.68;
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
  filter: saturate(1.08) contrast(1.02);
}

.avatar-camera {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(23, 27, 40, 0.68);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.profile-info {
  min-width: 0;
  flex: 1;
}

.name-block {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  padding-right: 96px;
  min-width: 0;
  align-self: stretch;
}

.badge-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
}

.nickname {
  margin: 0;
  align-self: stretch;
  min-width: 0;
  font-size: 29px;
  line-height: 1.15;
  font-weight: 700;
  color: #222334;
  white-space: normal;
  overflow-wrap: break-word;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.verified-badge {
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 999px;
  background: linear-gradient(135deg, #2774ff 0%, #5a8cff 46%, #9ac1ff 100%);
  color: #fff;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.2px;
  padding: 2px 7px 2px 3px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 3px;
  box-shadow: 0 5px 12px rgba(52, 112, 255, 0.24), inset 0 1px 1px rgba(255, 255, 255, 0.52);
  position: relative;
  z-index: 2;
}

.verified-badge.is-pending {
  background: linear-gradient(135deg, #d99b28 0%, #f8c660 100%);
  box-shadow: 0 5px 12px rgba(217, 151, 33, 0.2), inset 0 1px 1px rgba(255, 255, 255, 0.5);
}

.verified-badge.is-none {
  background: linear-gradient(135deg, #9aa2b6 0%, #c2c8d6 100%);
  box-shadow: 0 5px 12px rgba(112, 123, 150, 0.16), inset 0 1px 1px rgba(255, 255, 255, 0.5);
}

.verified-badge.is-approved {
  background: linear-gradient(135deg, #0d9f6e 0%, #3ecf9a 48%, #7fe8c3 100%);
  box-shadow: 0 5px 12px rgba(14, 142, 102, 0.28), inset 0 1px 1px rgba(255, 255, 255, 0.45);
}

.verified-badge.is-rejected {
  background: linear-gradient(135deg, #c24141 0%, #f08080 100%);
  box-shadow: 0 5px 12px rgba(180, 60, 60, 0.22), inset 0 1px 1px rgba(255, 255, 255, 0.45);
}

.verified-mark {
  width: 15px;
  height: 15px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.18);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.34);
}

.vip-medal {
  border: 1px solid rgba(122, 83, 20, 0.18);
  border-radius: 999px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.86), rgba(255, 248, 222, 0.42)),
    linear-gradient(135deg, #fff2bd 0%, #f4c466 47%, #d6942e 100%);
  color: #6b4210;
  padding: 2px 8px;
  font-size: 10px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  box-shadow: 0 6px 16px rgba(208, 145, 45, 0.24), inset 0 1px 1px rgba(255, 255, 255, 0.66);
  position: relative;
  z-index: 2;
}

.vip-crown {
  color: #4c2d0a;
  font-weight: 900;
  letter-spacing: 0.4px;
}

.base-info,
.intro {
  margin: 5px 0 0;
  font-size: 12px;
  color: #656b84;
  line-height: 1.45;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.identity-strip {
  margin-top: 6px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.62);
  border: 1px solid rgba(255, 204, 224, 0.72);
  padding: 3px 5px;
  box-shadow: 0 4px 12px rgba(255, 97, 159, 0.08);
  position: relative;
  z-index: 2;
}

.identity-strip span {
  border-radius: 999px;
  background: linear-gradient(135deg, #fff 0%, #fff4f9 100%);
  color: #c24174;
  font-size: 10px;
  font-weight: 700;
  padding: 2px 6px;
}

.tags-row {
  margin-top: 6px;
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.tag-chip {
  font-size: 11px;
  color: #6e738c;
  background: #f2f3f8;
  border-radius: 10px;
  padding: 1px 8px;
}

.edit-link {
  position: absolute;
  right: 10px;
  top: 12px;
  border: none;
  background: rgba(255, 84, 145, 0.1);
  color: #de4b88;
  border-radius: 12px;
  font-size: 11px;
  padding: 4px 9px;
}

.message-link {
  position: absolute;
  right: 10px;
  top: 44px;
  border: none;
  background: rgba(255, 84, 145, 0.1);
  color: #de4b88;
  border-radius: 12px;
  font-size: 11px;
  padding: 4px 14px;
}

.heart-glow {
  position: absolute;
  right: 6px;
  bottom: 0;
  width: 106px;
  height: 78px;
  background: linear-gradient(180deg, rgba(255, 246, 251, 0.2), rgba(255, 232, 244, 0.5));
  border-radius: 14px;
  opacity: 1;
  pointer-events: none;
}

.premium-seal {
  position: absolute;
  right: 13px;
  bottom: 12px;
  width: 82px;
  height: 38px;
  border-radius: 18px;
  background:
    radial-gradient(circle at 22% 18%, rgba(255, 255, 255, 0.76), rgba(255, 255, 255, 0) 34%),
    linear-gradient(135deg, rgba(40, 27, 15, 0.92) 0%, rgba(96, 61, 21, 0.9) 46%, rgba(220, 156, 55, 0.9) 100%);
  color: #ffe8b3;
  border: 1px solid rgba(255, 228, 178, 0.48);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 22px rgba(119, 73, 20, 0.18), inset 0 1px 1px rgba(255, 255, 255, 0.22);
  z-index: 1;
}

.premium-seal span {
  font-size: 9px;
  letter-spacing: 1.4px;
}

.premium-seal strong {
  margin-top: 1px;
  font-size: 11px;
  letter-spacing: 0.7px;
}

.heart-glow::before,
.heart-glow::after {
  content: '';
  position: absolute;
  border-radius: 50%;
}

.heart-glow::before {
  width: 39px;
  height: 39px;
  right: 40px;
  top: 30px;
  background: rgba(255, 146, 194, 0.78);
  box-shadow:
    14px -11px 0 rgba(255, 118, 178, 0.78),
    28px -4px 0 rgba(255, 86, 160, 0.84);
}

.heart-glow::after {
  width: 47px;
  height: 47px;
  right: 25px;
  top: 26px;
  background: rgba(255, 82, 158, 0.74);
  transform: rotate(45deg);
  border-radius: 15px 50% 50% 50%;
  box-shadow:
    -14px 12px 0 rgba(255, 171, 207, 0.66),
    12px -13px 0 rgba(255, 94, 166, 0.82);
}

.status-panel {
  padding: 10px;
  display: grid;
  grid-template-columns: 1.7fr 1fr 1fr 1fr;
  gap: 6px;
}

.completion-cell {
  border-radius: 12px;
  background: #fbfcff;
  padding: 9px 10px 8px;
  min-height: 96px;
}

.cell-title {
  margin: 0;
  font-size: 16px;
  color: #2a2d42;
  font-weight: 800;
}

.progress-track {
  margin-top: 9px;
  height: 5px;
  border-radius: 5px;
  background: #edf0fb;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  width: 80%;
  border-radius: inherit;
  background: linear-gradient(90deg, #ff5a8d 0%, #ff8c5f 34%, #c95cff 68%, #6ea8ff 100%);
  box-shadow: 0 0 8px rgba(255, 96, 160, 0.28);
}

.cell-sub {
  margin: 9px 0 0;
  font-size: 11px;
  color: #8a8ea3;
}

.minor-link {
  margin-top: 8px;
  border: none;
  background: none;
  color: #f24b7d;
  font-size: 12px;
  font-weight: 600;
  padding: 0;
}

.status-cell {
  border-radius: 12px;
  background: #fafbff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 9px 4px;
}

.metric-icon {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 18px rgba(90, 103, 216, 0.16);
}

.metric-trend {
  background: linear-gradient(135deg, #d84bff 0%, #7d5cff 100%);
}

.metric-fire {
  background: linear-gradient(135deg, #ffb12e 0%, #ff7a1a 100%);
}

.metric-eye {
  background: linear-gradient(135deg, #6ab7ff 0%, #3989ff 100%);
}

.status-title {
  margin: 0;
  font-size: 11px;
  color: #8b90a8;
  margin-top: 6px;
}

.status-value {
  margin: 2px 0 0;
  font-size: 22px;
  font-weight: 700;
  color: #2a2d42;
}

.menu-grid-card {
  padding: 6px 5px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 3px;
}

.menu-item {
  border: none;
  background: transparent;
  border-radius: 12px;
  padding: 8px 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
}

.menu-icon {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  box-shadow: inset 0 1px 1px rgba(255, 255, 255, 0.78);
}

.menu-icon::after {
  content: '';
  position: absolute;
  inset: 2px auto auto 3px;
  width: 12px;
  height: 8px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.55);
  transform: rotate(-22deg);
}

.menu-icon :deep(.van-icon) {
  position: relative;
  z-index: 1;
}

.menu-rose { background: linear-gradient(135deg, #fff0f3, #ffd6e5); color: #e91e8c; }
.menu-pink { background: linear-gradient(135deg, #ffe5ef, #ffd0e2); color: #ff3f8f; }
.menu-blue { background: linear-gradient(135deg, #edf3ff, #dce8ff); color: #4c87ff; }
.menu-yellow { background: linear-gradient(135deg, #fff4dc, #ffe3ad); color: #f0a11c; }
.menu-purple { background: linear-gradient(135deg, #f0e8ff, #e0d2ff); color: #8152f5; }
.menu-green { background: linear-gradient(135deg, #e6fbf1, #c8f2df); color: #23b979; }
.menu-orange { background: linear-gradient(135deg, #fff0e7, #ffd6c4); color: #ff8740; }
.menu-indigo { background: linear-gradient(135deg, #eaf0ff, #d6e0ff); color: #5274ef; }
.menu-gray { background: linear-gradient(135deg, #f0f2f8, #e3e6ef); color: #7a8194; }

.menu-title {
  margin: 0;
  font-size: 14px;
  color: #2c3048;
  font-weight: 600;
}

.menu-sub {
  margin: 0;
  font-size: 11px;
  color: #9ba0b6;
}

.photos-card,
.activity-card {
  padding: 12px;
}

.block-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.block-header h3 {
  margin: 0;
  font-size: 23px;
  line-height: 1;
  color: #23253b;
}

.block-link {
  border: none;
  background: none;
  font-size: 13px;
  color: #9ca1b7;
}

.photos-row {
  margin-top: 9px;
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 7px;
}

.photo-item {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  aspect-ratio: 1 / 1.12;
  background: #f1f3fa;
  box-shadow: 0 5px 12px rgba(42, 50, 92, 0.1);
}

.photo-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center top;
  filter: saturate(1.12) contrast(1.04);
}

.remove-photo-btn {
  position: absolute;
  right: 4px;
  bottom: 4px;
  border: none;
  border-radius: 999px;
  padding: 2px 7px;
  font-size: 10px;
  color: #fff;
  background: rgba(255, 75, 111, 0.9);
  box-shadow: 0 3px 8px rgba(255, 75, 111, 0.28);
}

.remove-photo-btn:disabled {
  opacity: 0.6;
}

.main-tag {
  position: absolute;
  left: 4px;
  top: 4px;
  border-radius: 9px;
  background: linear-gradient(90deg, #ff4c83, #ff62b2);
  color: #fff;
  font-size: 10px;
  padding: 1px 6px;
  box-shadow: 0 3px 8px rgba(255, 74, 132, 0.25);
}

.upload-item {
  border: 1px dashed #d8ddeb;
  border-radius: 12px;
  background: linear-gradient(180deg, #fbfcff 0%, #f5f7fd 100%);
  color: #868da7;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  aspect-ratio: 1 / 1.12;
}

.upload-item:disabled {
  opacity: 0.55;
}

.upload-item span {
  font-size: 12px;
}

.upload-item small {
  font-size: 10px;
}

.activity-list {
  margin: 10px 0 0;
  padding: 0;
  list-style: none;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 8px 0;
}

.activity-dot {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dot-pink { background: #ffe8f1; color: #ff5f98; }
.dot-orange { background: #fff3df; color: #f3aa2f; }
.dot-purple { background: #f1ebff; color: #8256f7; }

.activity-text {
  min-width: 0;
  flex: 1;
}

.activity-text p {
  margin: 0;
  font-size: 14px;
  color: #2f334a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.activity-text span {
  font-size: 12px;
  color: #9aa0b7;
}

.activity-action {
  border: none;
  background: none;
  color: #f14f80;
  font-size: 13px;
}

.vip-banner {
  border-radius: 16px;
  background:
    radial-gradient(circle at 90% 18%, rgba(255, 229, 166, 0.34) 0, rgba(255, 229, 166, 0) 28%),
    linear-gradient(118deg, #18111f 0%, #3b2134 45%, #b6752b 100%);
  padding: 13px 14px;
  color: #fff4cf;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  border: 1px solid rgba(255, 225, 167, 0.28);
  box-shadow: 0 10px 28px rgba(64, 33, 19, 0.16);
  overflow: hidden;
  position: relative;
}

.vip-banner::after {
  content: '';
  position: absolute;
  right: -16px;
  top: -28px;
  width: 96px;
  height: 96px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 242, 196, 0.2), rgba(255, 242, 196, 0));
}

.vip-banner-main {
  position: relative;
  z-index: 1;
  min-width: 0;
}

.vip-banner-kicker {
  display: inline-flex;
  margin-bottom: 4px;
  border-radius: 999px;
  background: rgba(255, 232, 179, 0.14);
  border: 1px solid rgba(255, 232, 179, 0.24);
  color: #ffe4a6;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 0.7px;
  padding: 2px 7px;
}

.vip-title {
  margin: 0;
  font-size: 19px;
  line-height: 1.08;
  font-weight: 700;
  color: #fff7dd;
}

.vip-sub {
  display: block;
  margin-top: 5px;
  font-size: 11px;
  color: rgba(255, 241, 208, 0.78);
  line-height: 1.35;
}

.vip-banner button {
  position: relative;
  z-index: 1;
  flex: 0 0 auto;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #fff4c7 0%, #f0c06b 50%, #d58a2b 100%);
  color: #3a250d;
  font-size: 13px;
  font-weight: 700;
  padding: 8px 12px;
  box-shadow: 0 7px 16px rgba(0, 0, 0, 0.22), inset 0 1px 1px rgba(255, 255, 255, 0.68);
}

.hidden-input {
  display: none;
}

.swipe-quota-banner,
.play-hub-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin: 0 14px 10px;
  padding: 12px 14px;
  border-radius: 14px;
  cursor: pointer;
}

.swipe-quota-banner {
  background: linear-gradient(135deg, #fff1f5, #ffe4ec);
  border: 1px solid #fbcfe8;
}

.play-hub-banner {
  background: linear-gradient(135deg, #f5f3ff, #ede9fe);
  border: 1px solid #ddd6fe;
}

.sq-title {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: #1f2937;
}

.sq-sub {
  margin: 4px 0 0;
  font-size: 11px;
  color: #6b7280;
}

.vip-medal.active {
  background: linear-gradient(135deg, #fde68a, #f59e0b);
  color: #78350f;
}

@keyframes skeleton-slide {
  to {
    transform: translateX(100%);
  }
}

@media (max-width: 420px) {
  .photos-row {
    grid-template-columns: repeat(5, minmax(0, 1fr));
    gap: 6px;
  }

  .profile-card,
  .status-panel,
  .menu-grid-card,
  .photos-card,
  .activity-card,
  .photo-item,
  .vip-banner {
    box-shadow: none;
  }
}
</style>



