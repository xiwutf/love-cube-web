<template>
  <section class="platform-page me-layout">
    <aside class="platform-card me-sidebar">
      <h2>平台个人中心</h2>
      <nav class="me-menu">
        <router-link to="/me" class="me-menu-item is-active">我的平台首页</router-link>
        <router-link to="/messages" class="me-menu-item">
          平台消息中心
          <span v-if="unreadCount > 0" class="me-dot">{{ unreadCount }}</span>
        </router-link>
        <router-link to="/articles" class="me-menu-item">我的内容</router-link>
        <router-link to="/events" class="me-menu-item">我的活动</router-link>
        <router-link to="/modules" class="me-menu-item">模块入口</router-link>
        <router-link to="/about" class="me-menu-item">平台设置</router-link>
      </nav>
    </aside>

    <main class="me-main">
      <div class="platform-card me-hero">
        <div class="me-user">
          <img v-if="user?.avatar" :src="user.avatar" class="me-avatar" alt="头像" />
          <div v-else class="me-avatar me-avatar-fallback">{{ avatarFallback }}</div>
          <div class="me-meta">
            <h1>{{ user?.username || '平台用户' }}</h1>
            <p>账号ID：{{ user?.id || '--' }}</p>
            <div class="me-tags">
              <span class="me-tag">{{ roleLabel }}</span>
              <span class="me-tag">{{ verifyLabel }}</span>
            </div>
            <button type="button" class="me-edit-btn" @click="toggleEdit">
              {{ editOpen ? '收起编辑' : '编辑资料' }}
            </button>
          </div>
        </div>
        <div class="me-kpis">
          <article>
            <p>平台通知</p>
            <strong>{{ unreadCount }}</strong>
          </article>
          <article>
            <p>我的内容</p>
            <strong>{{ myContentCount }}</strong>
          </article>
          <article>
            <p>我的活动</p>
            <strong>{{ myEventCount }}</strong>
          </article>
          <article>
            <p>收藏记录</p>
            <strong>{{ myFavoriteCount }}</strong>
          </article>
        </div>
      </div>

      <div v-if="editOpen" class="platform-card me-edit-card">
        <h3>编辑个人资料</h3>
        <form class="me-edit-form" @submit.prevent="handleSaveProfile">
          <label>
            <span>昵称</span>
            <input v-model.trim="editForm.username" type="text" maxlength="30" placeholder="请输入昵称" />
          </label>
          <label>
            <span>头像</span>
            <div class="me-avatar-uploader">
              <img v-if="editForm.avatar" :src="editForm.avatar" class="me-avatar-upload-preview" alt="头像预览" />
              <div v-else class="me-avatar-upload-preview me-avatar-upload-fallback">{{ avatarFallback }}</div>
              <div class="me-avatar-upload-actions">
                <button type="button" class="me-avatar-upload-btn" :disabled="uploading || saving" @click="handlePickAvatar">
                  {{ uploading ? '上传中...' : '选择图片' }}
                </button>
                <p class="me-avatar-upload-tip">支持常见图片格式，选择后自动上传并填充头像</p>
              </div>
            </div>
          </label>
          <label>
            <span>所在地</span>
            <input v-model.trim="editForm.location" type="text" maxlength="60" placeholder="例如：上海·浦东" />
          </label>
          <label>
            <span>个人简介</span>
            <textarea v-model.trim="editForm.bio" rows="3" maxlength="200" placeholder="简单介绍一下你自己" />
          </label>
          <p v-if="saveMessage" :class="['me-save-message', { 'is-error': saveError }]">{{ saveMessage }}</p>
          <div class="me-edit-actions">
            <button type="button" class="me-edit-cancel" :disabled="saving" @click="resetEditForm">重置</button>
            <button type="submit" class="me-edit-submit" :disabled="saving">{{ saving ? '保存中...' : '保存资料' }}</button>
          </div>
        </form>
      </div>

      <div class="me-grid">
        <div class="platform-card">
          <h3>我的模块</h3>
          <div class="me-shortcuts">
            <router-link to="/modules">模块中心</router-link>
            <router-link to="/fellowship">联谊模块入口</router-link>
            <router-link to="/announcements">平台动</router-link>
            <router-link to="/articles">精内</router-link>
          </div>
        </div>

        <div class="platform-card">
          <h3>账号与设</h3>
          <div class="me-shortcuts">
            <router-link to="/me">平台个人中心</router-link>
            <router-link to="/messages">平台消息中心</router-link>
            <router-link to="/about">登录安全</router-link>
            <router-link to="/about">平台设置</router-link>
          </div>
        </div>
      </div>
    </main>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useUserStore } from '@/stores/user.js'
import { getNotifUnreadCountCached } from '@/api/notification.js'
import { getUserStatsCached, updateProfile } from '@/api/user.js'
import { useImageUpload } from '@/composables/useImageUpload.js'

const userStore = useUserStore()
const user = computed(() => userStore.userInfo)
const unreadCount = ref(0)
const editOpen = ref(false)
const saving = ref(false)
const saveMessage = ref('')
const saveError = ref(false)
const { pickAndUpload, uploading } = useImageUpload()
const editForm = reactive({
  username: '',
  avatar: '',
  location: '',
  bio: ''
})

const myContentCount = ref(0)
const myEventCount = ref(0)
const myFavoriteCount = ref(0)

const avatarFallback = computed(() => {
  const name = user.value?.username || ''
  return name.slice(0, 1).toUpperCase() || 'U'
})

const roleLabel = computed(() => {
  const role = String(user.value?.role || '').toLowerCase()
  if (role === 'admin' || role === 'super_admin' || role === 'root') return '平台管理员'
  return '普通用户'
})

const verifyLabel = computed(() => {
  const status = String(user.value?.verificationStatus || 'none')
  if (status === 'approved') return '账号已认证'
  if (status === 'pending') return '认证审核中'
  if (status === 'rejected') return '认证未过'
  return '账号未认证'
})

watch(
  () => user.value,
  (value) => {
    editForm.username = value?.username || ''
    editForm.avatar = value?.avatar || ''
    editForm.location = value?.location || ''
    editForm.bio = value?.bio || ''
  },
  { immediate: true }
)

function toggleEdit() {
  editOpen.value = !editOpen.value
  saveMessage.value = ''
  saveError.value = false
}

function resetEditForm() {
  editForm.username = user.value?.username || ''
  editForm.avatar = user.value?.avatar || ''
  editForm.location = user.value?.location || ''
  editForm.bio = user.value?.bio || ''
  saveMessage.value = ''
  saveError.value = false
}

async function handlePickAvatar() {
  saveMessage.value = ''
  saveError.value = false
  try {
    const avatarUrl = await pickAndUpload({ quality: 0.8 })
    if (!avatarUrl) {
      throw new Error('上传失败，请重试')
    }
    editForm.avatar = avatarUrl
  } catch (error) {
    saveError.value = true
    saveMessage.value = error?.message || '头像上传失败，请稍后重试'
  }
}

async function handleSaveProfile() {
  saveMessage.value = ''
  saveError.value = false
  if (!editForm.username.trim()) {
    saveError.value = true
    saveMessage.value = '昵称不能为空'
    return
  }
  saving.value = true
  try {
    await updateProfile({
      username: editForm.username,
      profilePhoto: editForm.avatar,
      location: editForm.location,
      bio: editForm.bio
    })
    await userStore.refreshCurrentUser().catch(() => {})
    saveMessage.value = '资料已更新'
  } catch (error) {
    saveError.value = true
    saveMessage.value = error?.message || '保存失败，请稍后重试'
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  if (!user.value) {
    await userStore.refreshCurrentUser().catch(() => {})
  }
  const [notifRes, statsRes] = await Promise.allSettled([
    getNotifUnreadCountCached(),
    getUserStatsCached()
  ])
  if (notifRes.status === 'fulfilled') {
    unreadCount.value = Number(notifRes.value?.count ?? notifRes.value?.unreadCount ?? 0)
  }
  if (statsRes.status === 'fulfilled' && statsRes.value) {
    myContentCount.value = Number(statsRes.value.contentCount ?? 0)
    myEventCount.value = Number(statsRes.value.eventCount ?? 0)
    myFavoriteCount.value = Number(statsRes.value.favoriteCount ?? 0)
  }
})
</script>

<style scoped>
.me-layout {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 20px;
}

.me-sidebar {
  position: sticky;
  top: 88px;
  height: fit-content;
}

.me-sidebar h2 {
  margin: 0;
  font-size: 22px;
}

.me-menu {
  margin-top: 14px;
  display: grid;
  gap: 6px;
}

.me-menu-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 10px;
  color: var(--lc-muted);
  text-decoration: none;
  font-weight: 600;
}

.me-menu-item.is-active,
.me-menu-item:hover {
  background: #feeef3;
  color: #e11d48;
}

.me-dot {
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: #ef4444;
  color: #fff;
  font-size: 11px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.me-main {
  display: grid;
  gap: 14px;
}

.me-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 420px;
  gap: 16px;
  background: linear-gradient(100deg, #f8a2ce, #5a8ff2);
}

.me-user {
  display: flex;
  gap: 12px;
  align-items: center;
}

.me-avatar {
  width: 84px;
  height: 84px;
  border-radius: 50%;
  object-fit: cover;
}

.me-avatar-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: rgba(255, 255, 255, 0.2);
  font-size: 28px;
  font-weight: 800;
}

.me-meta h1 {
  margin: 0;
  color: #fff;
}

.me-meta p {
  margin: 6px 0;
  color: rgba(255, 255, 255, 0.88);
}

.me-tags {
  display: flex;
  gap: 8px;
}

.me-tag {
  padding: 3px 10px;
  border-radius: 999px;
  color: #fff;
  background: rgba(255, 255, 255, 0.25);
  font-size: 12px;
}

.me-edit-btn {
  margin-top: 8px;
  border: 1px solid rgba(255, 255, 255, 0.52);
  background: rgba(255, 255, 255, 0.18);
  color: #fff;
  border-radius: 999px;
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
}

.me-kpis {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.me-kpis article {
  border-radius: 10px;
  padding: 10px 12px;
  background: rgba(255, 255, 255, 0.92);
}

.me-kpis p {
  margin: 0;
  color: var(--lc-muted);
  font-size: 12px;
}

.me-kpis strong {
  font-size: 26px;
}

.me-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.me-edit-card h3 {
  margin-top: 0;
}

.me-edit-form {
  display: grid;
  gap: 12px;
}

.me-edit-form label {
  display: grid;
  gap: 6px;
}

.me-edit-form span {
  font-size: 13px;
  font-weight: 700;
  color: #334155;
}

.me-edit-form input,
.me-edit-form textarea {
  width: 100%;
  border: 1px solid #d7e0ed;
  border-radius: 8px;
  padding: 10px;
  font: inherit;
  color: #0f172a;
  background: #fff;
}

.me-edit-form textarea {
  resize: vertical;
}

.me-avatar-uploader {
  display: flex;
  align-items: center;
  gap: 12px;
}

.me-avatar-upload-preview {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #d7e0ed;
}

.me-avatar-upload-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
  background: #f8fafc;
  font-size: 22px;
  font-weight: 800;
}

.me-avatar-upload-actions {
  display: grid;
  gap: 6px;
}

.me-avatar-upload-btn {
  width: fit-content;
  border: 1px solid #d7e0ed;
  border-radius: 8px;
  background: #fff;
  color: #334155;
  padding: 7px 12px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}

.me-avatar-upload-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.me-avatar-upload-tip {
  margin: 0;
  font-size: 12px;
  color: #64748b;
}

.me-save-message {
  margin: 0;
  color: #16a34a;
  font-size: 13px;
  font-weight: 700;
}

.me-save-message.is-error {
  color: #dc2626;
}

.me-edit-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.me-edit-cancel,
.me-edit-submit {
  border: 1px solid #d7e0ed;
  border-radius: 8px;
  padding: 8px 14px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}

.me-edit-cancel {
  background: #fff;
  color: #334155;
}

.me-edit-submit {
  border-color: #2563eb;
  background: #2563eb;
  color: #fff;
}

.me-shortcuts {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.me-shortcuts a {
  border: 1px solid var(--lc-border);
  border-radius: 10px;
  padding: 10px;
  color: var(--lc-text);
  text-decoration: none;
  text-align: center;
}

@media (max-width: 1023px) {
  .me-layout {
    grid-template-columns: 1fr;
  }

  .me-sidebar {
    position: static;
  }

  .me-hero,
  .me-grid {
    grid-template-columns: 1fr;
  }
}
</style>

