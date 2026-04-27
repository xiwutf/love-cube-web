<template>
  <div class="me-page">
    <header class="page-header">
      <h1 class="page-title">个人中心</h1>
      <p class="header-tip">完善资料提升匹配率 ></p>
      <div class="header-actions">
        <button class="header-icon-btn" @click="router.push('/fellowship/settings')">
          <van-icon name="setting-o" size="20" />
        </button>
        <button class="header-icon-btn" @click="router.push('/fellowship/messages?tab=notification')">
          <van-icon name="bell" size="20" />
          <span v-if="notificationCount > 0" class="badge-dot">{{ notificationCount }}</span>
        </button>
      </div>
    </header>

    <main class="me-content">
      <section class="profile-card">
        <div class="profile-main">
          <div class="avatar-wrap" @click="chooseAvatar">
            <img class="avatar" :src="displayAvatar" alt="头像" />
            <div class="avatar-camera">
              <van-icon name="photo-o" size="14" />
            </div>
          </div>

          <div class="profile-info">
            <div class="name-row">
              <h2 class="nickname">{{ displayName }}</h2>
              <span class="verified-badge">V</span>
              <span class="vip-pill">VIP</span>
            </div>
            <p class="base-info">{{ displayAge }}岁 · {{ displayCity }} · {{ displayJob }}</p>
            <p class="intro">{{ displayIntro }}</p>
            <div class="tags-row">
              <span class="tag-chip">{{ profileMeta[0] }}</span>
              <span class="tag-chip">{{ profileMeta[1] }}</span>
              <span class="tag-chip">{{ profileMeta[2] }}</span>
              <span class="tag-chip">{{ profileMeta[3] }}</span>
              <span class="tag-chip">{{ profileMeta[4] }}</span>
            </div>
          </div>

          <button class="edit-link" @click="router.push('/fellowship/profile/edit')">编辑资料</button>
          <div class="heart-glow" />
        </div>
      </section>

      <section class="status-panel">
        <div class="completion-cell">
          <p class="cell-title">资料完整度 80%</p>
          <div class="progress-track">
            <div class="progress-bar" />
          </div>
          <p class="cell-sub">再完善3项，匹配率提升+30%</p>
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

      <section class="menu-grid-card">
        <button v-for="item in menuItems" :key="item.key" class="menu-item" @click="goTo(item.to)">
          <div class="menu-icon" :class="`menu-${item.theme}`">
            <van-icon :name="item.icon" size="22" />
          </div>
          <p class="menu-title">{{ item.title }}</p>
          <p class="menu-sub">{{ item.sub }}</p>
        </button>
      </section>

      <section class="photos-card">
        <div class="block-header">
          <h3>我的照片</h3>
          <button class="block-link" @click="router.push('/fellowship/profile/edit')">全部照片 ></button>
        </div>
        <div class="photos-row">
          <div v-for="(photo, index) in displayPhotos" :key="photo.id || index" class="photo-item">
            <img :src="photo.url" alt="照片" />
            <span v-if="index === 0" class="main-tag">主照片</span>
          </div>
          <button class="upload-item" @click="choosePhoto">
            <van-icon name="plus" size="22" />
            <span>上传照片</span>
            <small>({{ displayPhotos.length }}/9)</small>
          </button>
        </div>
      </section>

      <section class="activity-card">
        <div class="block-header">
          <h3>平台动态</h3>
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

      <section class="vip-banner" @click="router.push('/fellowship/vip')">
        <div>
          <p class="vip-title">开通会员 · 解锁更多特权</p>
          <span class="vip-sub">查看谁喜欢我，无限次可聊天等</span>
        </div>
        <button>去开通</button>
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
import { showToast } from 'vant'
import AppTabBar from '@/components/AppTabBar.vue'
import { useUserStore } from '@/stores/user.js'
import {
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
const notificationCount = ref(5)

const displayName = computed(() => profile.value.nickname || userInfo.value?.username || 'Love Cube 用户')
const displayAvatar = computed(
  () => profile.value.avatarUrl || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'
)
const displayAge = computed(() => profile.value.age || 28)
const displayCity = computed(() => profile.value.city || '保定市')
const displayJob = computed(() => profile.value.job || '产品经理')
const displayIntro = computed(() => profile.value.bio || '认真生活、真诚交友，期待遇见对的人。')
const displayPhotos = computed(() => photoList.value.slice(0, 4))
const profileMeta = computed(() => [
  profile.value.height ? `${profile.value.height}cm` : '178cm',
  profile.value.weight ? `${profile.value.weight}kg` : '68kg',
  profile.value.education || '本科',
  profile.value.houseStatus || '有房',
  profile.value.smokeStatus || '不抽烟'
])

const menuItems = [
  { key: 'match', title: '我的匹配', sub: '12人与你匹配', icon: 'like', to: '/fellowship/my-likes?tab=mutual', theme: 'pink' },
  { key: 'visitor', title: '谁看过我', sub: '今日+3', icon: 'eye', to: '/fellowship/messages?tab=visitor', theme: 'blue' },
  { key: 'likes', title: '喜欢我的人', sub: '8人戳了你', icon: 'good-job', to: '/fellowship/my-likes', theme: 'yellow' },
  { key: 'chat', title: '我的聊天', sub: '12条未读', icon: 'chat', to: '/fellowship/messages', theme: 'purple' },
  { key: 'signup', title: '我的报名', sub: '3个活动报名中', icon: 'calendar', to: '/fellowship/messages?tab=event', theme: 'green' },
  { key: 'collect', title: '我的收藏', sub: '18人被收藏', icon: 'star', to: '/fellowship/following', theme: 'orange' },
  { key: 'blacklist', title: '黑名单', sub: '1人已加入', icon: 'shield', to: '/fellowship/blacklist', theme: 'indigo' },
  { key: 'privacy', title: '隐私设置', sub: '隐私与权限', icon: 'setting', to: '/fellowship/privacy', theme: 'gray' }
]

const activityItems = [
  { icon: 'eye-o', title: '今日有 3 人浏览了你', time: '2小时前', theme: 'pink' },
  { icon: 'volume-o', title: '资料完整度提升可增加曝光30%', time: '1天前', theme: 'orange' },
  {
    icon: 'gift-o',
    title: '本周线下活动报名中，快来参与吧～',
    time: '3天前',
    theme: 'purple',
    action: { label: '去看看', to: '/fellowship/messages?tab=event' }
  }
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
    await saveFellowshipPhotos(next.slice(0, 9))
    photoList.value = next.map((item, idx) => ({ id: `${idx}-${item}`, url: item }))
    showToast({ type: 'success', message: `成功上传 ${uploadedUrls.length} 张照片` })
  } catch (err) {
    showToast({ type: 'fail', message: err.message || '照片上传失败' })
  }
}

function goTo(path) {
  router.push(path)
}

onMounted(loadPageData)
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
  padding: 13px 16px 9px;
}

.page-title {
  margin: 0 6px 0 0;
  font-size: 31px;
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
  padding: 0 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
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

.name-row {
  display: flex;
  align-items: center;
  gap: 5px;
  padding-right: 72px;
}

.nickname {
  margin: 0;
  max-width: 130px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 29px;
  line-height: 1.05;
  font-weight: 700;
  color: #222334;
}

.verified-badge {
  width: 15px;
  height: 15px;
  border-radius: 50%;
  background: #518dff;
  color: #fff;
  font-size: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.vip-pill {
  border-radius: 10px;
  background: rgba(250, 202, 112, 0.28);
  color: #9a6a1b;
  padding: 1px 8px;
  font-size: 11px;
  font-weight: 700;
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
  border-radius: 12px;
  background: linear-gradient(90deg, #ff4a80 0%, #ff5aa0 100%);
  padding: 10px 14px;
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.vip-title {
  margin: 0;
  font-size: 21px;
  line-height: 1;
  font-weight: 700;
}

.vip-sub {
  display: block;
  margin-top: 1px;
  font-size: 12px;
  opacity: 0.9;
}

.vip-banner button {
  border: none;
  border-radius: 16px;
  background: #ffd8e9;
  color: #d93d7a;
  font-size: 15px;
  font-weight: 700;
  padding: 5px 14px;
}

.hidden-input {
  display: none;
}

@media (max-width: 420px) {
  .photos-row {
    grid-template-columns: repeat(5, minmax(0, 1fr));
    gap: 6px;
  }
}
</style>
