<template>
  <div class="me-page">
    <header class="me-header">
      <h1 class="me-header-title">个人中心</h1>
      <div class="me-header-actions">
        <button class="me-icon-btn" @click="onMore">
          <van-icon name="ellipsis" size="18" />
        </button>
        <button class="me-icon-btn" @click="router.push('/fellowship/settings')">
          <van-icon name="setting-o" size="18" />
        </button>
      </div>
    </header>

    <div class="me-bg-strip" />

    <div class="me-content">
      <UserCard
        :avatar="profile.avatarUrl"
        :nickname="profile.nickname || userInfo?.username || ''"
        :user-id="userInfo?.userId || userInfo?.id || ''"
        :age="profile.age"
        :city="profile.city"
        :verified="profile.reviewStatus || profile.verified"
        :online="true"
        @avatar-click="chooseAvatar"
        @edit="router.push('/fellowship/profile/edit')"
      />

      <div class="next-step-card">
        <p class="next-step-title">下一步建议</p>
        <div class="next-step-grid">
          <router-link v-for="item in nextStepItems" :key="item.key" :to="item.to" class="next-step-item">
            <p>{{ item.title }}</p>
            <span>{{ item.desc }}</span>
          </router-link>
        </div>
      </div>

      <div v-if="showCertifiedBadge" class="achievement-card">
        <p class="achievement-title">已认证成就</p>
        <p class="achievement-desc">你已通过认证，当前账号在推荐与互动中会显示更高可信度。</p>
      </div>

      <ProfileProgress
        :percent="completion.percent || profile.profileCompletion || 0"
        :missing-fields="completion.missingFields || []"
        @improve="router.push('/fellowship/profile/edit')"
      />

      <PhotoWall
        :photos="photoList"
        @upload-photo="choosePhoto"
        @delete-photo="removePhoto"
        @set-primary="setPrimaryPhoto"
      />

      <QuickGrid :items="quickItems" @action="onQuickAction" />
      <SettingsList :items="settingItems" @action="onSettingAction" />
    </div>

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
import UserCard from '@/components/fellowship/UserCard.vue'
import ProfileProgress from '@/components/fellowship/ProfileProgress.vue'
import PhotoWall from '@/components/fellowship/PhotoWall.vue'
import QuickGrid from '@/components/fellowship/QuickGrid.vue'
import SettingsList from '@/components/fellowship/SettingsList.vue'
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

const router = useRouter()
const userStore = useUserStore()
const avatarInput = ref(null)
const photoInput = ref(null)

const userInfo = computed(() => userStore.syncCurrentUser() || {})
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

const showCertifiedBadge = computed(() => {
  const status = String(profile.value.reviewStatus || profile.value.verificationStatus || '').toLowerCase()
  return status === 'approved' || status === 'verified'
})

const nextStepItems = computed(() => {
  const list = []
  if ((completion.value.percent || 0) < 80) {
    list.push({ key: 'profile', title: '去完善资料', desc: '提升推荐效果', to: '/fellowship/profile/edit' })
  }
  if (!showCertifiedBadge.value) {
    list.push({ key: 'verify', title: '去真人认证', desc: '提升信任度', to: '/fellowship/verify' })
  }
  list.push({ key: 'discover', title: '去查看推荐用户', desc: '开始互动', to: '/fellowship/discover' })
  list.push({ key: 'notice', title: '查看新通知', desc: '不错过消息', to: '/fellowship/messages?tab=notification' })
  return list.slice(0, 4)
})

const quickItems = [
  { key: 'likes', title: '我的喜欢', icon: 'like-o', to: '/fellowship/my-likes' },
  { key: 'visitor', title: '谁看过我', icon: 'eye-o', to: '/fellowship/messages?tab=visitor' },
  { key: 'match', title: '我的匹配', icon: 'friends-o', to: '/fellowship/my-likes?tab=mutual' },
  { key: 'message', title: '消息中心', icon: 'chat-o', to: '/fellowship/messages' },
  { key: 'verify', title: '认证中心', icon: 'certificate', to: '/fellowship/verify' },
  { key: 'invite', title: '我的邀请码', icon: 'gift-o', to: '/fellowship/invite' },
  { key: 'dynamic', title: '我的动态', icon: 'records', to: '/fellowship/dynamic' },
  { key: 'blacklist', title: '黑名单', icon: 'warning-o', to: '/fellowship/blacklist' }
]

const settingItems = [
  { key: 'security', title: '账号安全' },
  { key: 'privacy', title: '隐私设置' },
  { key: 'feedback', title: '帮助反馈' },
  { key: 'service', title: '联系客服', value: '在线客服' },
  { key: 'about', title: '关于我们', value: 'Love Cube 1.0.0' },
  { key: 'logout', title: '退出登录', danger: true }
]

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

    if (p.status === 'fulfilled') normalizeProfile(p.value)
    if (c.status === 'fulfilled') completion.value = c.value || completion.value
    if (photosRes.status === 'fulfilled') {
      const photos = Array.isArray(photosRes.value?.photos) ? photosRes.value.photos : []
      photoList.value = photos.map((url, idx) => ({ id: `${idx}-${url}`, url }))
    }
  } catch (err) {
    showToast({ type: 'fail', message: err.message || '个人中心加载失败，请稍后重试' })
  }
}

function chooseAvatar() {
  avatarInput.value?.click()
}

function choosePhoto() {
  photoInput.value?.click()
}

function parseUploadUrl(res) {
  return res?.url || res?.data?.url || res?.photoUrl || ''
}

async function onAvatarSelected(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return
  try {
    const uploadRes = await uploadFellowshipAvatar(file)
    const url = parseUploadUrl(uploadRes)
    if (!url) throw new Error('头像上传返回为空')
    await updateMyFellowshipProfile({ avatarUrl: url })
    profile.value.avatarUrl = url
    showToast({ type: 'success', message: '头像已更新' })
  } catch (err) {
    showToast({ type: 'fail', message: err.message || '头像上传失败' })
  }
}

async function onPhotoSelected(event) {
  const files = Array.from(event.target.files || [])
  event.target.value = ''
  if (!files.length) return
  try {
    const uploadResults = await Promise.all(files.map((file) => uploadFellowshipPhoto(file)))
    const uploadedUrls = uploadResults.map(parseUploadUrl).filter(Boolean)
    if (!uploadedUrls.length) throw new Error('照片上传返回为空')
    const next = [...uploadedUrls, ...photoList.value.map((item) => item.url)]
    await saveFellowshipPhotos(next)
    photoList.value = next.map((item, idx) => ({ id: `${idx}-${item}`, url: item }))
    showToast({ type: 'success', message: `成功上传 ${uploadedUrls.length} 张照片` })
  } catch (err) {
    showToast({ type: 'fail', message: err.message || '照片上传失败' })
  }
}

async function removePhoto(photo, idx) {
  try {
    await showConfirmDialog({ title: '删除照片', message: '确认删除这张照片吗？' })
  } catch {
    return
  }
  try {
    await deleteFellowshipPhoto(photo.id || photo.url)
  } catch {
    // ignore and fallback save list
  }
  const next = photoList.value.filter((_, i) => i !== idx).map((item) => item.url)
  try {
    await saveFellowshipPhotos(next)
  } catch {
    // ignore
  }
  photoList.value = next.map((item, i) => ({ id: `${i}-${item}`, url: item }))
  showToast({ type: 'success', message: '已删除' })
}

async function setPrimaryPhoto(index) {
  if (index <= 0) return
  const list = [...photoList.value]
  const [first] = list.splice(index, 1)
  list.unshift(first)
  const urls = list.map((item) => item.url)
  try {
    await saveFellowshipPhotos(urls)
  } catch {
    // ignore
  }
  photoList.value = list
  showToast({ type: 'success', message: '已设为主图' })
}

function onQuickAction(item) {
  router.push(item.to)
}

function onSettingAction(item) {
  if (item.key === 'security') return router.push('/account')
  if (item.key === 'privacy') return router.push('/fellowship/privacy')
  if (item.key === 'feedback') return router.push('/fellowship/help')
  if (item.key === 'service') return showToast('联系客服：400-800-5200')
  if (item.key === 'about') return router.push('/about')
  if (item.key === 'logout') {
    showConfirmDialog({ title: '退出登录', message: '确认退出当前账号？' })
      .then(() => {
        userStore.logout()
        router.replace('/fellowship/login')
      })
      .catch(() => {})
  }
}

function onMore() {
  showToast('更多功能即将开放')
}

onMounted(loadPageData)
</script>

<style scoped>
.me-page {
  min-height: 100vh;
  background: #f4f6fb;
  padding-bottom: 84px;
  position: relative;
}

.me-header {
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

.me-header-title {
  margin: 0;
  font-size: 18px;
  font-weight: 800;
  color: #1a2236;
}

.me-header-actions {
  display: flex;
  gap: 4px;
}

.me-icon-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: #f8f9fd;
  color: #5b6b8a;
  display: flex;
  align-items: center;
  justify-content: center;
}

.me-bg-strip {
  height: 80px;
  background: linear-gradient(135deg, #ff5f84 0%, #ff8faa 100%);
  margin-top: -1px;
}

.me-content {
  position: relative;
  margin-top: -40px;
  padding: 0 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.next-step-card,
.achievement-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 14px;
}

.next-step-title,
.achievement-title {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: #1f2937;
}

.next-step-grid {
  margin-top: 10px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.next-step-item {
  display: block;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 10px;
  text-decoration: none;
  background: #f8fafc;
}

.next-step-item p {
  margin: 0;
  font-size: 13px;
  font-weight: 700;
  color: #1f2937;
}

.next-step-item span {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #64748b;
}

.achievement-desc {
  margin: 6px 0 0;
  font-size: 13px;
  color: #64748b;
  line-height: 1.6;
}

.hidden-input {
  display: none;
}

@media (max-width: 360px) {
  .next-step-grid {
    grid-template-columns: 1fr;
  }
}
</style>
