<template>
  <section class="pc-me-page">
    <DesktopDashboard
      :user="user"
      :display-name="displayName"
      :user-id-display="userIdDisplay"
      :location-display="locationDisplay"
      :invite-code-display="inviteCodeDisplay"
      :invite-count="inviteCount"
      :unread-count="unreadCount"
      :joined-space-count="1"
      :copy-feedback="copyFeedback"
      :copy-feedback-error="copyFeedbackError"
      :profile-light-stats="profileLightStats"
      :growth-level="growthLevel"
      :growth-progress="growthProgress"
      :completed-task-count="completedTaskCount"
      :daily-tasks="dailyTasks"
      :account-tasks="dashboardAccountTasks"
      :claiming-account-code="claimingAccountCode"
      :overview-items="overviewItems"
      :group-info="groupInfo"
      :group-ranking="groupRanking"
      :quick-actions="quickActions"
      :on-open-settings="openSettingsPanel"
      :on-open-edit="openEditPanel"
      :on-copy-invite="copyInviteCode"
      @claim-account-task="onClaimAccountTask"
    />
    <section class="badge-panel" aria-label="我的徽章">
      <h3>我的徽章</h3>
      <div class="badge-grid">
        <article v-for="badge in badges" :key="badge.code" :class="{ locked: !badge.unlocked }">
          <strong>{{ badge.name }}</strong>
          <small>{{ badge.progress }}/{{ badge.conditionValue }}</small>
        </article>
      </div>
    </section>

    <div v-if="editOpen" class="me-modal-backdrop" @click.self="closeEditPanel">
      <section class="me-modal" role="dialog" aria-modal="true" aria-label="编辑个人资料">
        <div class="me-modal-head">
          <h3>编辑个人资料</h3>
          <button type="button" aria-label="关闭" @click="closeEditPanel">×</button>
        </div>
        <form class="me-edit-form" @submit.prevent="handleSaveProfile">
          <label>
            <span>昵称</span>
            <input v-model.trim="editForm.username" type="text" maxlength="20" placeholder="请输入昵称" />
          </label>
          <label>
            <span>头像</span>
            <div class="avatar-uploader">
              <img v-if="editForm.avatar" :src="editForm.avatar" alt="头像预览" />
              <div v-else>{{ avatarFallback }}</div>
              <button type="button" :disabled="uploading || saving" @click="handlePickAvatar">
                {{ uploading ? '上传中...' : '选择图片' }}
              </button>
            </div>
          </label>
          <label>
            <span>所在地/城市</span>
            <input v-model.trim="editForm.location" type="text" maxlength="60" placeholder="例如：河北省 保定市" />
          </label>
          <label>
            <span>个人简介</span>
            <textarea v-model.trim="editForm.bio" rows="3" maxlength="200" placeholder="简单介绍一下你自己" />
          </label>
          <div class="me-edit-subtitle">联谊档案（可与联谊端同步编辑）</div>
          <label>
            <span>性别</span>
            <select v-model="editForm.gender">
              <option value="">未填写</option>
              <option value="male">男</option>
              <option value="female">女</option>
            </select>
          </label>
          <label>
            <span>年龄</span>
            <input v-model.trim="editForm.age" type="number" min="1" placeholder="例如：24（不填则保持）" />
          </label>
          <label>
            <span>出生年份</span>
            <input v-model.trim="editForm.birthYear" type="number" min="1" placeholder="例如：1998（不填则保持）" />
          </label>
          <label>
            <span>婚姻状况</span>
            <select v-model="editForm.maritalStatus">
              <option value="">未填写</option>
              <option value="单身">单身</option>
              <option value="已婚">已婚</option>
              <option value="离异">离异</option>
            </select>
          </label>
          <label>
            <span>职业</span>
            <input v-model.trim="editForm.occupation" type="text" maxlength="64" placeholder="例如：工程师（不填则保持）" />
          </label>
          <label>
            <span>学历</span>
            <select v-model="editForm.education">
              <option value="">未填写</option>
              <option value="高中及以下">高中及以下</option>
              <option value="大专">大专</option>
              <option value="本科">本科</option>
              <option value="硕士">硕士</option>
              <option value="博士">博士</option>
            </select>
          </label>
          <label>
            <span>身高(cm)</span>
            <input v-model.trim="editForm.height" type="number" min="1" placeholder="例如：170（不填则保持）" />
          </label>
          <label>
            <span>交友意向</span>
            <textarea v-model.trim="editForm.intention" rows="2" maxlength="500" placeholder="例如：希望共同成长、生活有趣..." />
          </label>
          <label>
            <span>标签</span>
            <input v-model.trim="editForm.tags" type="text" maxlength="500" placeholder="多个标签用逗号分隔（不填则保持）" />
          </label>
          <p v-if="saveMessage" class="save-message" :class="{ 'is-error': saveError }">{{ saveMessage }}</p>
          <div class="modal-actions">
            <button type="button" class="outline-action" :disabled="saving" @click="resetEditForm">重置</button>
            <button type="submit" class="primary-action" :disabled="saving">{{ saving ? '保存中...' : '保存资料' }}</button>
          </div>
        </form>
      </section>
    </div>

    <div v-if="settingsOpen" class="me-modal-backdrop" @click.self="closeSettingsPanel">
      <section class="me-modal" role="dialog" aria-modal="true" aria-label="账号设置">
        <div class="me-modal-head">
          <h3>账号设置</h3>
          <button type="button" aria-label="关闭" @click="closeSettingsPanel">×</button>
        </div>
        <div class="settings-summary">
          <div class="profile-avatar avatar-fallback">{{ avatarFallback }}</div>
          <div>
            <strong>{{ displayName }}</strong>
            <span>{{ roleLabel }}</span>
          </div>
        </div>
        <div class="settings-list">
          <div><span>账号 ID</span><strong>{{ userIdDisplay }}</strong></div>
          <div><span>手机号</span><strong>{{ user?.phone || '未绑定' }}</strong></div>
          <div><span>认证状态</span><strong>{{ verifyLabel }}</strong></div>
          <div><span>注册时间</span><strong>{{ registerDate }}</strong></div>
        </div>
        <div class="modal-actions">
          <button type="button" class="primary-action" @click="openEditPanel">编辑个人资料</button>
          <button type="button" class="outline-action" @click="goChangePhonePage">换绑手机号</button>
          <button type="button" class="outline-action" @click="goChangePasswordPage">修改密码</button>
          <button type="button" class="danger-action" @click="handleLogout">退出登录</button>
        </div>
      </section>
    </div>
  </section>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user.js'
import { getNotifUnreadCountCached } from '@/api/notification.js'
import { getUserStatsCached, updateProfile } from '@/api/user.js'
import { getMyFellowshipProfile, updateMyFellowshipProfile } from '@/api/fellowshipProfile.js'
import { getInviteInfo } from '@/api/invite.js'
import { claimAccountTask, getMyGrowth } from '@/api/growth.js'
import { PC_EVENTS, PC_ME, pcPath } from '@/constants/pcPaths.js'
import { usePlayTaskRoutes } from '@/composables/usePlayTaskRoutes.js'
import { useImageUpload } from '@/composables/useImageUpload.js'
import DesktopDashboard from '@/components/platform/me-dashboard/DesktopDashboard.vue'

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const { accountTaskRoute, dailyTaskRoute } = usePlayTaskRoutes()
const user = computed(() => userStore.userInfo)
const unreadCount = ref(0)
const editOpen = ref(false)
const settingsOpen = ref(false)
const saving = ref(false)
const saveMessage = ref('')
const saveError = ref(false)
const { pickAndUpload, uploading } = useImageUpload()
const inviteCode = ref('')
const inviteCount = ref(0)
const copyFeedback = ref('')
const copyFeedbackError = ref(false)
const editForm = reactive({
  username: '',
  avatar: '',
  location: '',
  bio: '',
  // 联谊档案字段：用于联谊匹配展示，可与联谊端双向编辑
  gender: '',
  age: '',
  birthYear: '',
  maritalStatus: '',
  occupation: '',
  education: '',
  height: '',
  intention: '',
  tags: ''
})
const editFormSnapshot = ref(null)

const myContentCount = ref(0)
const myEventCount = ref(0)
const myFavoriteCount = ref(0)
const myPositiveShareLikeCount = ref(0)
const growthInfo = ref(null)
const badges = ref([])
const claimingAccountCode = ref('')

function accountGrowthTaskRoutePc(code) {
  return accountTaskRoute(code)
}

const dashboardAccountTasks = computed(() => {
  const rows = growthInfo.value?.accountTasks
  if (!Array.isArray(rows) || !rows.length) return []
  return rows.map((item) => ({
    code: item.code,
    title: item.name || item.code,
    exp: Number(item.rewardExp ?? 0),
    completed: Boolean(item.completed),
    to: accountGrowthTaskRoutePc(item.code)
  }))
})

async function onClaimAccountTask(code) {
  if (!code || claimingAccountCode.value) return
  claimingAccountCode.value = code
  try {
    const res = await claimAccountTask(code)
    if (res?.claimed) {
      const g = await getMyGrowth()
      growthInfo.value = g || null
      badges.value = Array.isArray(g?.badges) ? g.badges : []
    }
  } catch {
    /* 静默失败，工作台以重新进入页面为准 */
  } finally {
    claimingAccountCode.value = ''
  }
}

const growthLevel = computed(() => {
  const data = growthInfo.value
  return {
    level: Number(data?.level ?? 1),
    name: data?.title || '新手用户',
    currentExp: Number(data?.exp ?? 0),
    nextExp: Number(data?.nextLevelExp ?? 100),
    sources: [
      { label: '登录', exp: 2 },
      { label: '发布内容', exp: 10 },
      { label: '浏览内容', exp: 1 },
      { label: '点赞内容', exp: 2 }
    ]
  }
})

const dailyTasks = computed(() => {
  const rows = growthInfo.value?.dailyTasks
  if (!Array.isArray(rows)) return []
  return rows.map(item => ({
    title: item.name || item.code,
    exp: Number(item.rewardExp ?? 0),
    current: Number(item.progress ?? 0),
    total: Number(item.targetCount ?? 1),
    done: Boolean(item.completed),
    to: dailyTaskRoute(item.code)
  }))
})

const groupInfo = {
  name: 'LoveCube 官方团队',
  role: '管理员',
  members: 23,
  activity: '中等',
  weekExp: 120
}

const groupRanking = [
  { rank: 1, name: 'LoveCube 官方团队', activity: 320 },
  { rank: 2, name: '星空缘分社', activity: 280 },
  { rank: 3, name: '绿来是你', activity: 210 }
]

const displayName = computed(() => user.value?.username || user.value?.nickname || 'LoveCube 官方团队')
const userIdDisplay = computed(() => user.value?.id || user.value?.userId || '1')
const locationDisplay = computed(() => user.value?.location || '河北省 保定市')
const avatarFallback = computed(() => String(displayName.value || 'L').slice(0, 1).toUpperCase())
const inviteCodeDisplay = computed(() => inviteCode.value || '')
const growthProgress = computed(() => {
  const nextExp = Math.max(1, Number(growthLevel.value.nextExp))
  const currentExp = Number(growthLevel.value.currentExp)
  return `${Math.min(100, Math.round((currentExp / nextExp) * 100))}%`
})
const completedTaskCount = computed(() => dailyTasks.value.filter(item => item.done).length)

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

const registerDate = computed(() => {
  const raw = user.value?.createdAt
  if (!raw) return '--'
  return String(raw).replace('T', ' ').slice(0, 10)
})

const profileLightStats = [
  { label: '关注', value: '12' },
  { label: '粉丝', value: '236' },
  { label: '获赞', value: '1,234' }
]

const workspaceItems = computed(() => [
  { title: '我的内容', desc: '发布、管理文章', value: `${myContentCount.value} 篇内容`, icon: '▤', tone: 'violet', to: pcPath('positive-share') },
  { title: '每日心声', desc: '记录每日想法', value: `${Math.min(myContentCount.value, 7)} 条心声`, icon: '♡', tone: 'rose', to: pcPath('positive-share') },
  { title: '我的活动', desc: '报名、签到与互评', value: `${myEventCount.value} 个活动`, icon: '▣', tone: 'amber', to: '/platform/my-activities' },
  { title: '消息中心', desc: '系统通知与互动', value: unreadCount.value > 0 ? `${unreadCount.value} 条未读` : '暂无未读', icon: '●', tone: 'blue', to: pcPath('messages') }
])

const overviewItems = computed(() => [
  { label: '发布内容', value: myContentCount.value, icon: '↗', tone: 'violet', to: pcPath('positive-share') },
  { label: '活动参与', value: myEventCount.value, icon: '✦', tone: 'rose', to: '/platform/my-activities' },
  { label: '心声收藏', value: myFavoriteCount.value, icon: '☆', tone: 'amber', to: pcPath('me/favorites') },
  { label: '点赞心声', value: myPositiveShareLikeCount.value, icon: '♥', tone: 'rose', to: pcPath('positive-share') },
  { label: '互动热度', value: '--', icon: '♨', tone: 'green', to: pcPath('positive-share') },
  { label: '当前等级', value: `Lv.${growthLevel.value.level}`, icon: '◇', tone: 'blue', to: pcPath('modules') }
])

const quickActions = computed(() => [
  { title: '我的活动', desc: '报名、签到与互评', icon: '▣', tone: 'amber', to: '/platform/my-activities' },
  { title: '内容中心', desc: '管理文章和内容', icon: '▤', tone: 'violet', to: pcPath('positive-share') },
  { title: '模块中心', desc: '管理平台模块', icon: '▦', tone: 'green', to: pcPath('modules') },
  { title: '通知中心', desc: '查看系统通知', icon: '●', tone: 'rose', to: pcPath('messages') },
  { title: '账号设置', desc: '账号与安全', icon: '◇', tone: 'green', to: { path: PC_ME, query: { panel: 'settings' } } },
  { title: '邀请码', desc: '邀请好友加入', icon: '▭', tone: 'amber', to: '/fellowship/invite' },
  { title: '编辑资料', desc: '修改个人资料', icon: '✎', tone: 'blue', to: { path: PC_ME, query: { panel: 'edit' } } }
])

async function refreshUnreadCount() {
  const notifRes = await getNotifUnreadCountCached().catch(() => null)
  unreadCount.value = Number(notifRes?.count ?? notifRes?.unreadCount ?? 0)
}

function handleNotifReadAll() {
  unreadCount.value = 0
}

watch(
  () => user.value,
  (value) => {
    // 编辑弹窗打开时，以联谊资料加载结果为准；避免用户刷新覆盖正在编辑的表单
    if (editOpen.value) return
    editForm.username = value?.username || ''
    editForm.avatar = value?.avatar || ''
    editForm.location = value?.location || ''
    editForm.bio = value?.bio || ''
  },
  { immediate: true }
)

async function openEditPanel() {
  editOpen.value = true
  settingsOpen.value = false
  saveMessage.value = ''
  saveError.value = false

  await loadFellowshipProfileForEdit()
  editFormSnapshot.value = JSON.parse(JSON.stringify(editForm))
}

async function loadFellowshipProfileForEdit() {
  const d = await getMyFellowshipProfile().catch(() => null)
  if (!d) return

  editForm.username = d.nickname || editForm.username
  editForm.avatar = d.avatarUrl || editForm.avatar
  editForm.location = d.city || editForm.location
  editForm.bio = d.bio || editForm.bio

  editForm.gender = d.gender || ''
  editForm.age = d.age ?? ''
  editForm.birthYear = d.birthYear ?? ''
  editForm.maritalStatus = d.maritalStatus || ''
  editForm.occupation = d.occupation || ''
  editForm.education = d.education || ''
  editForm.height = d.height ?? ''
  editForm.intention = d.intention || ''
  editForm.tags = d.tags || ''
}

function closeEditPanel() {
  editOpen.value = false
}

function openSettingsPanel() {
  settingsOpen.value = true
  editOpen.value = false
  if (route.query?.panel !== 'settings') {
    router.replace({ path: '/pc/platform/me', query: { ...route.query, panel: 'settings' } })
  }
}

function closeSettingsPanel() {
  settingsOpen.value = false
}

function resetEditForm() {
  if (editFormSnapshot.value) {
    Object.assign(editForm, JSON.parse(JSON.stringify(editFormSnapshot.value)))
  } else {
    editForm.username = user.value?.username || ''
    editForm.avatar = user.value?.avatar || ''
    editForm.location = user.value?.location || ''
    editForm.bio = user.value?.bio || ''
  }
  saveMessage.value = ''
  saveError.value = false
}

async function handlePickAvatar() {
  saveMessage.value = ''
  saveError.value = false
  try {
    const avatarUrl = await pickAndUpload({ quality: 0.8 })
    if (!avatarUrl) throw new Error('上传失败，请重试')
    editForm.avatar = avatarUrl
  } catch (error) {
    saveError.value = true
    saveMessage.value = error?.message || '头像上传失败，请稍后重试'
  }
}

async function handleSaveProfile() {
  saveMessage.value = ''
  saveError.value = false
  const username = editForm.username.trim()
  if (!username) {
    saveError.value = true
    saveMessage.value = '昵称不能为空'
    return
  }
  if (username.length > 20) {
    saveError.value = true
    saveMessage.value = '昵称最多 20 个字符'
    return
  }

  const parsedAge = editForm.age === '' || editForm.age == null ? null : Number(editForm.age)
  if (parsedAge !== null && (!Number.isInteger(parsedAge) || parsedAge <= 0)) {
    saveError.value = true
    saveMessage.value = '年龄必须是大于 0 的整数'
    return
  }

  const parsedBirthYear = editForm.birthYear === '' || editForm.birthYear == null ? null : Number(editForm.birthYear)
  if (parsedBirthYear !== null && (!Number.isInteger(parsedBirthYear) || parsedBirthYear <= 0)) {
    saveError.value = true
    saveMessage.value = '出生年份必须是大于 0 的整数'
    return
  }

  const parsedHeight = editForm.height === '' || editForm.height == null ? null : Number(editForm.height)
  if (parsedHeight !== null && (!Number.isInteger(parsedHeight) || parsedHeight <= 0)) {
    saveError.value = true
    saveMessage.value = '身高必须是大于 0 的整数'
    return
  }
  saving.value = true
  try {
    await updateProfile({
      username,
      profilePhoto: editForm.avatar,
      location: editForm.location,
      bio: editForm.bio
    })

    const fellowshipPayload = {}
    if (editForm.gender) fellowshipPayload.gender = editForm.gender
    if (parsedAge !== null) fellowshipPayload.age = parsedAge
    if (parsedBirthYear !== null) fellowshipPayload.birthYear = parsedBirthYear
    if (editForm.maritalStatus) fellowshipPayload.maritalStatus = editForm.maritalStatus
    if (editForm.occupation) fellowshipPayload.occupation = editForm.occupation
    if (editForm.education) fellowshipPayload.education = editForm.education
    if (parsedHeight !== null) fellowshipPayload.height = parsedHeight
    if (editForm.intention) fellowshipPayload.intention = editForm.intention
    if (editForm.tags) fellowshipPayload.tags = editForm.tags

    try {
      await updateMyFellowshipProfile(fellowshipPayload)
    } catch (e) {
      saveError.value = true
      saveMessage.value = e?.response?.data?.message || e?.message || '联谊档案保存失败，请稍后重试'
    }

    await userStore.refreshCurrentUser().catch(() => {})
    if (!saveError.value) saveMessage.value = '资料已更新'
    window.setTimeout(() => {
      if (!saveError.value) closeEditPanel()
    }, 600)
  } catch (error) {
    saveError.value = true
    saveMessage.value = error?.message || '保存失败，请稍后重试'
  } finally {
    saving.value = false
  }
}

function handleLogout() {
  userStore.logout()
  router.replace('/login')
}

function goChangePasswordPage() {
  closeSettingsPanel()
  router.push('/fellowship/change-password')
}

function goChangePhonePage() {
  closeSettingsPanel()
  router.push('/fellowship/change-phone')
}

async function copyInviteCode() {
  if (!inviteCodeDisplay.value) return
  copyFeedback.value = ''
  copyFeedbackError.value = false
  try {
    if (navigator?.clipboard?.writeText) {
      await navigator.clipboard.writeText(inviteCodeDisplay.value)
    } else {
      const input = document.createElement('input')
      input.value = inviteCodeDisplay.value
      document.body.appendChild(input)
      input.select()
      document.execCommand('copy')
      document.body.removeChild(input)
    }
    copyFeedback.value = '已复制'
    window.setTimeout(() => { copyFeedback.value = '' }, 2000)
  } catch {
    copyFeedbackError.value = true
    copyFeedback.value = '复制失败'
    window.setTimeout(() => { copyFeedback.value = '' }, 2000)
  }
}

onMounted(async () => {
  window.addEventListener('platform-notif-read-all', handleNotifReadAll)
  if (route.query?.panel === 'settings') openSettingsPanel()
  if (route.query?.panel === 'edit') openEditPanel()
  if (!user.value) await userStore.refreshCurrentUser().catch(() => {})
  await refreshUnreadCount()
  const [statsRes, inviteRes, growthRes] = await Promise.allSettled([getUserStatsCached(), getInviteInfo(), getMyGrowth()])
  if (statsRes.status === 'fulfilled' && statsRes.value) {
    myContentCount.value = Number(statsRes.value.contentCount ?? 0)
    myEventCount.value = Number(statsRes.value.eventCount ?? 0)
    myFavoriteCount.value = Number(statsRes.value.positiveShareBookmarkCount ?? 0)
    myPositiveShareLikeCount.value = Number(statsRes.value.positiveShareLikeCount ?? 0)
  }
  if (inviteRes.status === 'fulfilled') {
    inviteCode.value = String(inviteRes.value?.inviteCode || inviteRes.value?.code || '').trim()
    inviteCount.value = Number(inviteRes.value?.inviteCount ?? 0)
  }
  if (growthRes.status === 'fulfilled') {
    growthInfo.value = growthRes.value || null
    badges.value = Array.isArray(growthRes.value?.badges) ? growthRes.value.badges : []
  }
})

watch(
  () => route.query?.panel,
  (panel) => {
    if (panel === 'settings') openSettingsPanel()
    if (panel === 'edit') openEditPanel()
  }
)

onBeforeUnmount(() => {
  window.removeEventListener('platform-notif-read-all', handleNotifReadAll)
})
</script>

<style scoped>
.pc-me-page {
  min-height: calc(100vh - 60px);
  background: #f6f7fb;
}

.badge-panel {
  width: min(100% - 40px, 1720px);
  margin: 12px auto 0;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 14px 16px;
}

.badge-panel h3 {
  margin: 0 0 10px;
  font-size: 16px;
}

.badge-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.badge-grid article {
  border: 1px solid #dbeafe;
  border-radius: 10px;
  padding: 10px;
  background: #eff6ff;
}

.badge-grid article.locked {
  border-color: #e2e8f0;
  background: #f8fafc;
  color: #94a3b8;
}

.me-modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 400;
  background: rgba(15, 23, 42, 0.42);
  display: grid;
  place-items: center;
  padding: 24px;
}

.me-modal {
  background: #fff;
  border-radius: 20px;
  width: min(100%, 480px);
  padding: 28px 28px 24px;
  box-shadow: 0 24px 56px rgba(15, 23, 42, 0.18);
}

.me-modal-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.me-modal-head h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
}

.me-modal-head button {
  width: 32px;
  height: 32px;
  border: 0;
  border-radius: 50%;
  background: #f1f5f9;
  font-size: 20px;
  cursor: pointer;
  line-height: 1;
}

.me-edit-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.me-edit-form label {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.me-edit-form label span {
  font-size: 13px;
  font-weight: 600;
  color: #334155;
}

.me-edit-form input,
.me-edit-form textarea {
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 10px 12px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.15s;
  font-family: inherit;
  resize: vertical;
}

.me-edit-form select {
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 10px 12px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.15s;
  font-family: inherit;
  background: #fff;
}

.me-edit-subtitle {
  margin-top: -4px;
  font-size: 13px;
  font-weight: 700;
  color: #334155;
}

.me-edit-form input:focus,
.me-edit-form textarea:focus {
  border-color: #6d5dfb;
}

.avatar-uploader {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar-uploader img {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-uploader div {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6d5dfb, #4f46e5);
  color: #fff;
  display: grid;
  place-items: center;
  font-size: 22px;
  font-weight: 800;
}

.avatar-uploader button {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #f8fafc;
  padding: 7px 14px;
  font-size: 13px;
  cursor: pointer;
}

.save-message {
  margin: 0;
  font-size: 13px;
  color: #059669;
}

.save-message.is-error {
  color: #dc2626;
}

.modal-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 4px;
}

.primary-action {
  padding: 9px 20px;
  border: 0;
  border-radius: 10px;
  background: #6d5dfb;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.15s;
}

.primary-action:hover {
  opacity: 0.88;
}

.primary-action:disabled {
  opacity: 0.5;
}

.outline-action {
  padding: 9px 20px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: transparent;
  font-size: 14px;
  cursor: pointer;
}

.danger-action {
  padding: 9px 20px;
  border: 0;
  border-radius: 10px;
  background: #fee2e2;
  color: #dc2626;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.settings-summary {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 0;
  border-bottom: 1px solid #f1f5f9;
  margin-bottom: 16px;
}

.profile-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
}

.avatar-fallback {
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #6d5dfb, #4f46e5);
  color: #fff;
  font-size: 18px;
  font-weight: 800;
}

.settings-summary strong {
  display: block;
  font-size: 15px;
  font-weight: 700;
}

.settings-summary span {
  font-size: 12px;
  color: #64748b;
}

.settings-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 20px;
}

.settings-list div {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.settings-list span {
  color: #64748b;
}

.settings-list strong {
  font-weight: 600;
}
</style>
