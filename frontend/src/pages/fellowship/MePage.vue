<template>
  <div class="me-page">
    <!-- Header -->
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

    <!-- Decorative background strip -->
    <div class="me-bg-strip" />

    <!-- Content -->
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
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '个人中心加载失败' })
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
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '头像上传失败' })
  }
}

async function onPhotoSelected(event) {
  const files = Array.from(event.target.files || [])
  event.target.value = ''
  if (!files.length) return
  try {
    const uploadResults = await Promise.all(files.map((file) => uploadFellowshipPhoto(file)))
    const uploadedUrls = uploadResults
      .map(parseUploadUrl)
      .filter((url) => !!url)
    if (!uploadedUrls.length) throw new Error('照片上传返回为空')
    const next = [...uploadedUrls, ...photoList.value.map((p) => p.url)]
    await saveFellowshipPhotos(next)
    photoList.value = next.map((item, idx) => ({ id: `${idx}-${item}`, url: item }))
    showToast({ type: 'success', message: `成功上传 ${uploadedUrls.length} 张` })
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '照片上传失败' })
  }
}

async function removePhoto(photo, idx) {
  try {
    await showConfirmDialog({ title: '删除照片', message: '确认删除这张生活照吗？' })
  } catch {
    return
  }
  try {
    await deleteFellowshipPhoto(photo.id || photo.url)
  } catch {
    // ignore and fallback to save list
  }
  const next = photoList.value.filter((_, i) => i !== idx).map((item) => item.url)
  try {
    await saveFellowshipPhotos(next)
  } catch {
    // ignore network fallback
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
  showToast('更多功能敬请期待')
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

/* ── Header ── */
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
  letter-spacing: -0.01em;
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
  cursor: pointer;
  transition: background 0.15s;
}
.me-icon-btn:active {
  background: #eef1f8;
}

/* Decorative strip behind the card hero */
.me-bg-strip {
  height: 80px;
  background: linear-gradient(135deg, #FF5F84 0%, #FF8FAA 100%);
  margin-top: -1px;
}

/* ── Content ── */
.me-content {
  position: relative;
  margin-top: -40px;
  padding: 0 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.hidden-input {
  display: none;
}
</style>
