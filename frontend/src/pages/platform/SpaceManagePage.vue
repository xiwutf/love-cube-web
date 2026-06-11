<template>
  <section class="space-manage-page">
    <div v-if="redirecting" class="state-card">正在跳转兼容管理页…</div>

    <template v-else-if="loading">
      <div class="state-card">加载社区空间中…</div>
    </template>

    <template v-else-if="forbidden">
      <div class="state-card err">
        <h2>无权访问运营台</h2>
        <p>仅社区负责人或管理员可进入运营台。</p>
        <router-link class="btn ghost" :to="groupsPath(String(spaceId))">返回社区主页</router-link>
      </div>
    </template>

    <template v-else-if="loadError">
      <div class="state-card err">
        <h2>加载失败</h2>
        <p>{{ loadError }}</p>
        <button type="button" class="btn primary" @click="bootstrap">重试</button>
      </div>
    </template>

    <template v-else-if="group">
      <div v-if="onboardingActive" class="onboarding-panel platform-card">
        <div class="onboarding-head">
          <strong>欢迎，开始运营你的社区空间</strong>
          <span class="step-label">步骤 {{ onboardingStep }} / 3</span>
        </div>
        <p class="onboarding-hint">{{ onboardingHints[onboardingStep - 1] }}</p>

        <div v-if="onboardingStep === 1" class="onboarding-body">
          <label class="field">
            <span>社区名称</span>
            <input v-model.trim="settingsForm.name" type="text" maxlength="100">
          </label>
          <label class="field">
            <span>简介</span>
            <textarea v-model.trim="settingsForm.description" rows="4" maxlength="2000" />
          </label>
          <label class="field">
            <span>地区（可选）</span>
            <input v-model.trim="settingsForm.region" type="text" maxlength="50">
          </label>
          <button type="button" class="btn primary" :disabled="saving" @click="saveSettingsAndNext">
            {{ saving ? '保存中…' : '保存并继续' }}
          </button>
        </div>

        <div v-else-if="onboardingStep === 2" class="onboarding-body">
          <label class="field">
            <span>公告标题</span>
            <input v-model.trim="noticeForm.title" type="text" maxlength="120" placeholder="例如：欢迎加入本社区">
          </label>
          <label class="field">
            <span>公告内容</span>
            <textarea v-model.trim="noticeForm.content" rows="5" maxlength="5000" placeholder="介绍社区规则与近期安排" />
          </label>
          <div class="onboarding-actions">
            <button type="button" class="btn ghost" @click="onboardingStep = 3">稍后发布</button>
            <button type="button" class="btn primary" :disabled="saving" @click="publishFirstNotice">
              {{ saving ? '发布中…' : '发布公告并继续' }}
            </button>
          </div>
        </div>

        <div v-else class="onboarding-body">
          <p class="share-hint">把下面的链接发给伙伴，邀请他们加入社区。</p>
          <div class="share-box">
            <code>{{ inviteShareUrl }}</code>
            <button type="button" class="btn secondary" @click="copyInviteLink">复制链接</button>
          </div>
          <p v-if="inviteCode" class="meta-line">邀请码：<strong>{{ inviteCode }}</strong></p>
          <button type="button" class="btn primary" @click="finishOnboarding">进入运营台</button>
        </div>
      </div>

      <header class="page-head">
        <div class="title-block">
          <router-link class="back-link" :to="groupsPath()">← 我的社区</router-link>
          <div class="title-row">
            <img v-if="group.coverUrl" :src="group.coverUrl" :alt="group.name" class="cover-thumb">
            <div>
              <h1>{{ group.name }}</h1>
              <p class="subtitle">社区运营台 · {{ roleLabel }} · {{ group.memberCount ?? 0 }} 位成员</p>
            </div>
          </div>
        </div>
        <div class="head-actions">
          <router-link class="btn ghost" :to="groupsPath(String(spaceId))">查看主页</router-link>
        </div>
      </header>

      <p v-if="flash" class="flash" :class="{ err: flashType === 'error' }">{{ flash }}</p>

      <nav class="manage-tabs" aria-label="运营分区">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          type="button"
          :class="{ active: activeTab === tab.key }"
          @click="switchTab(tab.key)"
        >
          {{ tab.label }}
          <span v-if="tab.key === 'members' && pendingCount" class="badge">{{ pendingCount }}</span>
        </button>
      </nav>

      <!-- 概览 -->
      <section v-if="activeTab === 'overview'" class="platform-card tab-panel">
        <SpaceManageOverviewPanel
          :stats="spaceStats"
          :loading="loadingSpaceStats"
          :error="spaceStatsError"
          @refresh="loadSpaceStats"
          @navigate="switchTab"
        />
      </section>

      <!-- 成员 -->
      <section v-else-if="activeTab === 'members'" class="platform-card tab-panel">
        <div class="section-head">
          <h2>成员管理</h2>
          <div class="filter-row">
            <button
              v-for="opt in memberFilters"
              :key="opt.value"
              type="button"
              :class="{ active: memberStatus === opt.value }"
              @click="loadMembers(opt.value)"
            >
              {{ opt.label }}
            </button>
          </div>
        </div>
        <div v-if="loadingMembers" class="inline-state">加载中…</div>
        <div v-else-if="members.length" class="member-list">
          <article v-for="m in members" :key="m.id" class="member-row">
            <img :src="m.avatarUrl || defaultAvatar" :alt="m.username">
            <div class="member-main">
              <strong>{{ displayMemberName(m) }}</strong>
              <p>{{ formatDate(m.joinedAt || m.requestedAt) }}</p>
              <p v-if="m.status === 'pending' && m.applyReason" class="apply-reason">申请说明：{{ m.applyReason }}</p>
            </div>
            <div class="member-tags">
              <span class="tag" :class="m.role">{{ roleTag(m.role) }}</span>
              <span class="tag status">{{ memberStatusLabel(m.status) }}</span>
            </div>
            <div class="member-actions">
              <template v-if="m.status === 'pending'">
                <button type="button" class="btn primary sm" :disabled="saving" @click="approve(m)">通过</button>
                <button type="button" class="btn danger sm" :disabled="saving" @click="reject(m)">拒绝</button>
              </template>
              <button
                v-else-if="m.role !== 'owner' && m.status === 'approved'"
                type="button"
                class="btn danger sm"
                :disabled="saving"
                @click="removeMember(m)"
              >
                移除
              </button>
            </div>
          </article>
        </div>
        <p v-else class="inline-state">暂无成员记录</p>
      </section>

      <!-- 公告 -->
      <section v-else-if="activeTab === 'notices'" class="platform-card tab-panel">
        <div class="section-head">
          <h2>公告</h2>
        </div>
        <form class="inline-form" @submit.prevent="submitNotice">
          <label class="field">
            <span>标题</span>
            <input v-model.trim="noticeForm.title" type="text" maxlength="120" required>
          </label>
          <label class="field">
            <span>内容</span>
            <textarea v-model.trim="noticeForm.content" rows="4" maxlength="5000" required />
          </label>
          <button type="submit" class="btn primary" :disabled="saving">{{ saving ? '发布中…' : '发布公告' }}</button>
        </form>
        <div v-if="loadingNotices" class="inline-state">加载中…</div>
        <div v-else-if="notices.length" class="content-list">
          <article v-for="n in notices" :key="n.id" class="content-row">
            <header>
              <strong>{{ n.title }}</strong>
              <time>{{ formatDate(n.createdAt) }}</time>
            </header>
            <p>{{ n.content }}</p>
          </article>
        </div>
        <p v-else class="inline-state">暂无公告，发布第一条吧</p>
      </section>

      <!-- 活动 -->
      <section v-else-if="activeTab === 'activities'" class="platform-card tab-panel">
        <div class="section-head">
          <h2>活动</h2>
        </div>
        <form class="inline-form" @submit.prevent="submitActivity">
          <label class="field">
            <span>活动标题</span>
            <input v-model.trim="activityForm.title" type="text" maxlength="200" required>
          </label>
          <label class="field">
            <span>简介</span>
            <textarea v-model.trim="activityForm.description" rows="3" maxlength="2000" />
          </label>
          <div class="field-row">
            <label class="field">
              <span>开始时间</span>
              <input v-model="activityForm.startTime" type="datetime-local" required>
            </label>
            <label class="field">
              <span>结束时间</span>
              <input v-model="activityForm.endTime" type="datetime-local" required>
            </label>
          </div>
          <label class="field">
            <span>地点（可选）</span>
            <input v-model.trim="activityForm.location" type="text" maxlength="200">
          </label>
          <button type="submit" class="btn primary" :disabled="saving">{{ saving ? '创建中…' : '创建活动' }}</button>
        </form>
        <div v-if="loadingActivities" class="inline-state">加载中…</div>
        <div v-else-if="activities.length" class="content-list">
          <article v-for="a in activities" :key="a.id" class="content-row">
            <header>
              <strong>{{ a.title }}</strong>
              <time>{{ formatDate(a.startTime) }}</time>
            </header>
            <p>{{ a.description || '暂无简介' }}</p>
            <p class="meta-line">{{ a.participantCount ?? 0 }} 人报名 · {{ a.location || '线上/待定' }}</p>
          </article>
        </div>
        <p v-else class="inline-state">暂无活动</p>
      </section>

      <!-- 打卡营 -->
      <section v-else-if="activeTab === 'camp'" class="tab-panel">
        <SpaceCampaignManagePanel :group-id="spaceId" @flash="onCampFlash" />
      </section>

      <!-- 任务 -->
      <section v-else-if="activeTab === 'tasks'" class="platform-card tab-panel">
        <h2>每日任务</h2>
        <p class="hint">以下为平台预设的社区每日任务，成员完成后可获得成长值。任务规则配置将在后续版本开放。</p>
        <ul class="task-list">
          <li v-for="task in taskCatalog" :key="task.code">
            <div>
              <strong>{{ task.name }}</strong>
              <span class="meta-line">奖励 {{ task.rewardExp }} 成长值</span>
            </div>
            <span class="tag">系统预设</span>
          </li>
        </ul>
      </section>

      <!-- 设置 -->
      <section v-else-if="activeTab === 'settings'" class="platform-card tab-panel">
        <h2>社区设置</h2>
        <form class="settings-form" @submit.prevent="saveSettings">
          <label class="field">
            <span>社区名称</span>
            <input v-model.trim="settingsForm.name" type="text" maxlength="100" required>
          </label>
          <label class="field">
            <span>分类</span>
            <select v-model="settingsForm.type">
              <option v-for="c in categoryOptions" :key="c.value" :value="c.value">{{ c.label }}</option>
            </select>
          </label>
          <label class="field">
            <span>地区</span>
            <input v-model.trim="settingsForm.region" type="text" maxlength="50">
          </label>
          <label class="field">
            <span>加入方式</span>
            <select v-model="settingsForm.joinMode">
              <option value="open">公开加入</option>
              <option value="audit">审核加入</option>
              <option value="invite">仅限邀请</option>
            </select>
          </label>
          <label class="field">
            <span>封面图 URL</span>
            <input v-model.trim="settingsForm.coverUrl" type="url" maxlength="512" placeholder="可选">
          </label>
          <label class="field">
            <span>简介</span>
            <textarea v-model.trim="settingsForm.description" rows="5" maxlength="2000" />
          </label>
          <label class="field">
            <span>标签（逗号分隔，可选）</span>
            <input v-model.trim="settingsForm.tags" type="text" maxlength="120">
          </label>
          <button type="submit" class="btn primary" :disabled="saving">{{ saving ? '保存中…' : '保存设置' }}</button>
        </form>
        <div v-if="inviteCode" class="invite-block">
          <h3>邀请信息</h3>
          <p class="meta-line">邀请码：<strong>{{ inviteCode }}</strong></p>
          <div class="share-box">
            <code>{{ inviteShareUrl }}</code>
            <button type="button" class="btn secondary sm" @click="copyInviteLink">复制链接</button>
          </div>
        </div>
      </section>
    </template>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  approveMember,
  createGroupActivity,
  createGroupNotice,
  fetchGroupActivities,
  fetchGroupDetail,
  fetchGroupMembers,
  fetchGroupNotices,
  rejectMember,
  resolveSpaceManageEntry,
  removeGroupMember,
  updateGroup
} from '@/api/groups.js'
import { fetchSpaceStats } from '@/api/spaceStats.js'
import SpaceCampaignManagePanel from '@/components/platform/spaces/SpaceCampaignManagePanel.vue'
import SpaceManageOverviewPanel from '@/components/platform/spaces/SpaceManageOverviewPanel.vue'
import { PLATFORM_GROUP_TASK_CATALOG } from '@/constants/platformGroupTasks.js'
import { usePlatformPath } from '@/composables/usePlatformPath.js'
import { useUserStore } from '@/stores/user.js'
import { PLATFORM_GROUP_CATEGORY_OPTIONS } from '@/utils/groupCategories.js'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { groupsPath } = usePlatformPath()

const spaceId = computed(() => String(route.params.id || ''))
const defaultAvatar = 'https://api.dicebear.com/7.x/initials/svg?seed=LC&backgroundColor=eff6ff'

const loading = ref(true)
const loadError = ref('')
const forbidden = ref(false)
const redirecting = ref(false)
const group = ref(null)
const saving = ref(false)
const flash = ref('')
const flashType = ref('success')

const activeTab = ref(normalizeTab(route.query.tab))
const pendingCount = ref(0)
const spaceStats = ref(null)
const loadingSpaceStats = ref(false)
const spaceStatsError = ref('')

const members = ref([])
const memberStatus = ref('approved')
const loadingMembers = ref(false)

const notices = ref([])
const loadingNotices = ref(false)
const noticeForm = reactive({ title: '', content: '' })

const activities = ref([])
const loadingActivities = ref(false)
const activityForm = reactive({
  title: '',
  description: '',
  startTime: '',
  endTime: '',
  location: ''
})

const settingsForm = reactive({
  name: '',
  type: 'interest',
  region: '',
  joinMode: 'audit',
  coverUrl: '',
  description: '',
  tags: ''
})

const inviteCode = ref('')
const onboardingStep = ref(1)
const taskCatalog = PLATFORM_GROUP_TASK_CATALOG
const categoryOptions = PLATFORM_GROUP_CATEGORY_OPTIONS

const onboardingActive = computed(() => route.query.onboarding === '1')
const onboardingHints = [
  '先完善社区名称与简介，让成员一眼看懂你的社区定位。',
  '发布第一条公告，告诉大家社区要做什么。',
  '复制邀请链接，把第一批成员拉进来。'
]

const tabs = [
  { key: 'overview', label: '概览' },
  { key: 'members', label: '成员' },
  { key: 'notices', label: '公告' },
  { key: 'activities', label: '活动' },
  { key: 'camp', label: '打卡营' },
  { key: 'tasks', label: '任务' },
  { key: 'settings', label: '设置' }
]

const memberFilters = [
  { label: '已加入', value: 'approved' },
  { label: '待审核', value: 'pending' },
  { label: '全部', value: 'all' }
]

const roleLabel = computed(() => {
  const r = group.value?.myRole
  if (r === 'owner') return '负责人'
  if (r === 'admin') return '管理员'
  return '运营者'
})

const inviteShareUrl = computed(() => {
  const base = typeof window !== 'undefined' ? window.location.origin : ''
  const path = groupsPath(String(spaceId.value))
  if (inviteCode.value) {
    return `${base}/#${path}?invite=${inviteCode.value}`
  }
  return `${base}/#${path}`
})

function normalizeTab(raw) {
  const key = typeof raw === 'string' ? raw : 'overview'
  return tabs.some((t) => t.key === key) ? key : 'overview'
}

function switchTab(key) {
  activeTab.value = key
  router.replace({ query: { ...route.query, tab: key } })
  if (key === 'overview') loadSpaceStats()
  if (key === 'members') loadMembers(memberStatus.value)
  if (key === 'notices') loadNotices()
  if (key === 'activities') loadActivities()
}

function showFlash(msg, type = 'success') {
  flash.value = msg
  flashType.value = type
  window.setTimeout(() => { flash.value = '' }, 4000)
}

function onCampFlash(msg, type = 'success') {
  showFlash(msg, type)
}

function formatDate(v) {
  if (!v) return '—'
  const d = new Date(v)
  if (Number.isNaN(d.getTime())) return String(v)
  return d.toLocaleString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

function displayMemberName(m) {
  return m.memberRealName || m.username || `用户 ${m.userId}`
}

function roleTag(role) {
  if (role === 'owner') return '负责人'
  if (role === 'admin') return '管理员'
  return '成员'
}

function memberStatusLabel(status) {
  if (status === 'pending') return '待审核'
  if (status === 'approved') return '已加入'
  if (status === 'rejected') return '已拒绝'
  return status || '—'
}

function fillSettingsFromGroup(g) {
  settingsForm.name = g.name || ''
  settingsForm.type = g.type || g.category || 'interest'
  settingsForm.region = g.region || g.location || ''
  const jm = g.joinModeKey || g.joinMode || 'audit'
  settingsForm.joinMode = jm === 'open' ? 'open' : jm === 'invite' ? 'invite' : 'audit'
  settingsForm.coverUrl = g.coverUrl || ''
  settingsForm.description = g.description || ''
  settingsForm.tags = g.tags || ''
  inviteCode.value = g.inviteCode || ''
}

async function ensureAuth() {
  if (!userStore.isLoggedIn) {
    userStore.setPostLoginRedirect(route.fullPath)
    await router.push('/login')
    return false
  }
  await userStore.loadAdminContext().catch(() => null)
  return true
}

async function bootstrap() {
  const legacyEntry = resolveSpaceManageEntry(spaceId.value, {
    tab: typeof route.query.tab === 'string' ? route.query.tab : 'info'
  })
  if (legacyEntry?.path?.startsWith('/admin/my-groups')) {
    redirecting.value = true
    await router.replace(legacyEntry)
    return
  }

  loading.value = true
  loadError.value = ''
  forbidden.value = false

  try {
    const ok = await ensureAuth()
    if (!ok) return

    const detail = await fetchGroupDetail(spaceId.value)
    const data = detail?.data ?? detail
    group.value = data

    const canManage = Boolean(data?.managed) || userStore.hasPermission('group.manage.all')
    if (!canManage) {
      forbidden.value = true
      return
    }

    fillSettingsFromGroup(data)
    pendingCount.value = Number(data?.pendingMemberCount ?? 0)
    inviteCode.value = data?.inviteCode || inviteCode.value

    await loadSpaceStats()
    if (activeTab.value === 'members') await loadMembers(memberStatus.value)
    if (activeTab.value === 'notices') await loadNotices()
    if (activeTab.value === 'activities') await loadActivities()
  } catch (err) {
    loadError.value = err?.message || '无法加载社区空间'
  } finally {
    loading.value = false
  }
}

async function loadSpaceStats() {
  loadingSpaceStats.value = true
  spaceStatsError.value = ''
  try {
    const res = await fetchSpaceStats(spaceId.value)
    spaceStats.value = res?.data ?? res
    const pending = Number(spaceStats.value?.memberGrowth?.pendingMembers)
    if (!Number.isNaN(pending)) {
      pendingCount.value = pending
    }
  } catch (err) {
    spaceStatsError.value = err?.message || '数据加载失败'
  } finally {
    loadingSpaceStats.value = false
  }
}

async function loadMembers(status = 'approved') {
  memberStatus.value = status
  loadingMembers.value = true
  try {
    const res = await fetchGroupMembers(spaceId.value, { status })
    members.value = Array.isArray(res) ? res : res?.data ?? []
    if (status === 'pending') {
      pendingCount.value = members.value.length
    }
  } catch (err) {
    showFlash(err?.message || '成员加载失败', 'error')
    members.value = []
  } finally {
    loadingMembers.value = false
  }
}

async function loadNotices() {
  loadingNotices.value = true
  try {
    const res = await fetchGroupNotices(spaceId.value)
    notices.value = Array.isArray(res) ? res : res?.data ?? []
  } catch (err) {
    showFlash(err?.message || '公告加载失败', 'error')
    notices.value = []
  } finally {
    loadingNotices.value = false
  }
}

async function loadActivities() {
  loadingActivities.value = true
  try {
    const res = await fetchGroupActivities(spaceId.value, { page: 1, pageSize: 50 })
    activities.value = res?.items ?? (Array.isArray(res) ? res : [])
  } catch (err) {
    showFlash(err?.message || '活动加载失败', 'error')
    activities.value = []
  } finally {
    loadingActivities.value = false
  }
}

function buildSettingsPayload() {
  return {
    name: settingsForm.name,
    type: settingsForm.type,
    region: settingsForm.region,
    joinMode: settingsForm.joinMode,
    coverUrl: settingsForm.coverUrl || undefined,
    description: settingsForm.description,
    tags: settingsForm.tags || undefined
  }
}

async function saveSettings() {
  saving.value = true
  try {
    await updateGroup(spaceId.value, buildSettingsPayload())
    const detail = await fetchGroupDetail(spaceId.value)
    group.value = detail?.data ?? detail
    fillSettingsFromGroup(group.value)
    showFlash('设置已保存')
    return true
  } catch (err) {
    showFlash(err?.message || '保存失败', 'error')
    return false
  } finally {
    saving.value = false
  }
}

async function saveSettingsAndNext() {
  const ok = await saveSettings()
  if (ok) onboardingStep.value = 2
}

async function submitNotice() {
  if (!noticeForm.title.trim() || !noticeForm.content.trim()) {
    showFlash('请填写公告标题与内容', 'error')
    return false
  }
  saving.value = true
  try {
    await createGroupNotice(spaceId.value, {
      title: noticeForm.title,
      content: noticeForm.content
    })
    noticeForm.title = ''
    noticeForm.content = ''
    await loadNotices()
    showFlash('公告已发布')
    return true
  } catch (err) {
    showFlash(err?.message || '发布失败', 'error')
    return false
  } finally {
    saving.value = false
  }
}

async function publishFirstNotice() {
  const ok = await submitNotice()
  if (ok) onboardingStep.value = 3
}

function toIsoLocal(dtLocal) {
  if (!dtLocal) return ''
  const d = new Date(dtLocal)
  if (Number.isNaN(d.getTime())) return ''
  return d.toISOString().slice(0, 19)
}

async function submitActivity() {
  const startTime = toIsoLocal(activityForm.startTime)
  const endTime = toIsoLocal(activityForm.endTime)
  if (!startTime || !endTime) {
    showFlash('请填写有效的活动时间', 'error')
    return
  }
  saving.value = true
  try {
    await createGroupActivity(spaceId.value, {
      title: activityForm.title,
      description: activityForm.description,
      startTime,
      endTime,
      location: activityForm.location || undefined,
      maxParticipants: 0
    })
    activityForm.title = ''
    activityForm.description = ''
    activityForm.startTime = ''
    activityForm.endTime = ''
    activityForm.location = ''
    await loadActivities()
    showFlash('活动已创建')
  } catch (err) {
    showFlash(err?.message || '创建失败', 'error')
  } finally {
    saving.value = false
  }
}

async function approve(member) {
  saving.value = true
  try {
    await approveMember(spaceId.value, member.id)
    await loadMembers(memberStatus.value)
    const detail = await fetchGroupDetail(spaceId.value)
    group.value = detail?.data ?? detail
    pendingCount.value = Number(group.value?.pendingMemberCount ?? pendingCount.value)
    showFlash('已通过入团申请')
  } catch (err) {
    showFlash(err?.message || '操作失败', 'error')
  } finally {
    saving.value = false
  }
}

async function reject(member) {
  saving.value = true
  try {
    await rejectMember(spaceId.value, member.id)
    await loadMembers(memberStatus.value)
    pendingCount.value = Math.max(0, pendingCount.value - 1)
    showFlash('已拒绝入团申请')
  } catch (err) {
    showFlash(err?.message || '操作失败', 'error')
  } finally {
    saving.value = false
  }
}

async function removeMember(member) {
  if (!window.confirm(`确定移除成员「${displayMemberName(member)}」？`)) return
  saving.value = true
  try {
    await removeGroupMember(spaceId.value, member.id)
    await loadMembers(memberStatus.value)
    showFlash('成员已移除')
  } catch (err) {
    showFlash(err?.message || '移除失败', 'error')
  } finally {
    saving.value = false
  }
}

async function copyInviteLink() {
  try {
    await navigator.clipboard.writeText(inviteShareUrl.value)
    showFlash('链接已复制')
  } catch {
    showFlash('复制失败，请手动选择链接', 'error')
  }
}

function finishOnboarding() {
  const q = { ...route.query }
  delete q.onboarding
  router.replace({ path: route.path, query: q })
}

watch(
  () => route.params.id,
  () => bootstrap()
)

watch(
  () => route.query.tab,
  (tab) => {
    const next = normalizeTab(tab)
    if (next !== activeTab.value) {
      switchTab(next)
    }
  }
)

onMounted(() => {
  bootstrap()
})
</script>

<style scoped>
.space-manage-page {
  width: calc(100% - var(--lc-space-8));
  max-width: 960px;
  margin: var(--lc-space-4) auto var(--lc-space-8);
  padding: var(--lc-space-6);
}

.state-card {
  padding: var(--lc-space-8);
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius);
  background: var(--lc-surface);
  text-align: center;
  color: var(--lc-muted);
}

.state-card.err h2 {
  margin: 0 0 var(--lc-space-2);
  color: var(--lc-text);
}

.page-head {
  display: flex;
  justify-content: space-between;
  gap: var(--lc-space-4);
  flex-wrap: wrap;
  margin-bottom: var(--lc-space-4);
}

.back-link {
  display: inline-block;
  margin-bottom: var(--lc-space-2);
  color: var(--lc-blue);
  font-weight: 700;
  text-decoration: none;
}

.title-row {
  display: flex;
  gap: var(--lc-space-3);
  align-items: center;
}

.cover-thumb {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  object-fit: cover;
  border: 1px solid var(--lc-border);
}

.page-head h1 {
  margin: 0;
  font-size: 26px;
  color: var(--lc-text);
}

.subtitle {
  margin: var(--lc-space-1) 0 0;
  color: var(--lc-muted);
  font-weight: 600;
}

.manage-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: var(--lc-space-2);
  margin-bottom: var(--lc-space-4);
}

.manage-tabs button {
  border: 1px solid var(--lc-border);
  background: var(--lc-surface);
  color: var(--lc-muted);
  border-radius: 999px;
  padding: 8px 14px;
  font-weight: 800;
  cursor: pointer;
}

.manage-tabs button.active {
  border-color: var(--lc-blue);
  color: var(--lc-blue);
  background: color-mix(in srgb, var(--lc-blue) 8%, white);
}

.badge {
  display: inline-flex;
  min-width: 18px;
  height: 18px;
  margin-left: 6px;
  padding: 0 5px;
  border-radius: 999px;
  background: var(--lc-red);
  color: #fff;
  font-size: 11px;
  align-items: center;
  justify-content: center;
}

.tab-panel {
  padding: var(--lc-space-5);
}

.tab-panel h2 {
  margin: 0 0 var(--lc-space-4);
  font-size: 20px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: var(--lc-space-3);
  margin-bottom: var(--lc-space-4);
}

.stat-card {
  padding: var(--lc-space-4);
  border-radius: 12px;
  border: 1px solid var(--lc-border);
  background: var(--lc-bg);
  display: grid;
  gap: 4px;
}

.stat-card strong {
  font-size: 24px;
  color: var(--lc-text);
}

.stat-card span {
  color: var(--lc-muted);
  font-weight: 700;
  font-size: 13px;
}

.stat-card.warn strong {
  color: var(--lc-red);
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: var(--lc-space-2);
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--lc-space-3);
  flex-wrap: wrap;
  margin-bottom: var(--lc-space-4);
}

.section-head h2 {
  margin: 0;
}

.filter-row {
  display: flex;
  gap: var(--lc-space-2);
  flex-wrap: wrap;
}

.filter-row button {
  border: 1px solid var(--lc-border);
  background: #fff;
  border-radius: 8px;
  padding: 6px 10px;
  font-weight: 700;
  cursor: pointer;
}

.filter-row button.active {
  border-color: var(--lc-blue);
  color: var(--lc-blue);
}

.member-list {
  display: grid;
  gap: var(--lc-space-3);
}

.member-row {
  display: grid;
  grid-template-columns: 40px 1fr auto auto;
  gap: var(--lc-space-3);
  align-items: center;
  padding: var(--lc-space-3);
  border: 1px solid var(--lc-border);
  border-radius: 12px;
}

.member-row img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.member-main p {
  margin: 2px 0 0;
  color: var(--lc-muted);
  font-size: 13px;
}

.apply-reason {
  color: var(--lc-text);
}

.member-tags {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 999px;
  background: var(--lc-bg);
  font-size: 12px;
  font-weight: 700;
  color: var(--lc-muted);
}

.tag.owner {
  color: var(--lc-blue);
}

.tag.admin {
  color: var(--lc-indigo);
}

.member-actions {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.inline-form,
.settings-form {
  display: grid;
  gap: var(--lc-space-3);
  margin-bottom: var(--lc-space-5);
  padding-bottom: var(--lc-space-4);
  border-bottom: 1px solid var(--lc-border);
}

.field-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--lc-space-3);
}

.field {
  display: grid;
  gap: 6px;
}

.field span {
  font-weight: 800;
  color: var(--lc-text);
}

.field input,
.field textarea,
.field select {
  width: 100%;
  border: 1px solid var(--lc-border);
  border-radius: 10px;
  padding: 10px 12px;
  font: inherit;
}

.content-list {
  display: grid;
  gap: var(--lc-space-3);
}

.content-row {
  padding: var(--lc-space-3);
  border: 1px solid var(--lc-border);
  border-radius: 12px;
}

.content-row header {
  display: flex;
  justify-content: space-between;
  gap: var(--lc-space-2);
  margin-bottom: var(--lc-space-2);
}

.content-row time {
  color: var(--lc-muted);
  font-size: 13px;
}

.task-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: var(--lc-space-3);
}

.task-list li {
  display: flex;
  justify-content: space-between;
  gap: var(--lc-space-3);
  align-items: center;
  padding: var(--lc-space-3);
  border: 1px solid var(--lc-border);
  border-radius: 12px;
}

.hint,
.meta-line,
.share-hint {
  color: var(--lc-muted);
  font-weight: 600;
  line-height: 1.5;
}

.flash {
  margin: 0 0 var(--lc-space-3);
  padding: 10px 12px;
  border-radius: 10px;
  background: color-mix(in srgb, var(--lc-green) 12%, white);
  color: var(--lc-green);
  font-weight: 700;
}

.flash.err {
  background: color-mix(in srgb, var(--lc-red) 10%, white);
  color: var(--lc-red);
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  padding: 10px 14px;
  font-weight: 800;
  border: 1px solid transparent;
  cursor: pointer;
  text-decoration: none;
}

.btn.primary {
  background: var(--lc-blue);
  color: #fff;
}

.btn.secondary {
  background: #fff;
  border-color: var(--lc-border);
  color: var(--lc-text);
}

.btn.ghost {
  background: #fff;
  border-color: var(--lc-border);
  color: var(--lc-muted);
}

.btn.danger {
  background: #fff;
  border-color: color-mix(in srgb, var(--lc-red) 40%, white);
  color: var(--lc-red);
}

.btn.sm {
  padding: 6px 10px;
  font-size: 13px;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.onboarding-panel {
  margin-bottom: var(--lc-space-4);
  padding: var(--lc-space-5);
  border: 1px solid color-mix(in srgb, var(--lc-blue) 25%, white);
  background: color-mix(in srgb, var(--lc-blue) 4%, white);
}

.onboarding-head {
  display: flex;
  justify-content: space-between;
  gap: var(--lc-space-2);
  align-items: center;
}

.step-label {
  color: var(--lc-blue);
  font-weight: 800;
}

.onboarding-body {
  margin-top: var(--lc-space-3);
  display: grid;
  gap: var(--lc-space-3);
}

.onboarding-actions {
  display: flex;
  gap: var(--lc-space-2);
  flex-wrap: wrap;
}

.share-box {
  display: flex;
  gap: var(--lc-space-2);
  align-items: center;
  flex-wrap: wrap;
}

.share-box code {
  flex: 1;
  min-width: 200px;
  padding: 8px 10px;
  border-radius: 8px;
  background: var(--lc-bg);
  word-break: break-all;
  font-size: 12px;
}

.invite-block {
  margin-top: var(--lc-space-5);
  padding-top: var(--lc-space-4);
  border-top: 1px solid var(--lc-border);
}

.inline-state {
  color: var(--lc-muted);
  font-weight: 700;
}

@media (max-width: 720px) {
  .member-row {
    grid-template-columns: 40px 1fr;
  }

  .member-tags,
  .member-actions {
    grid-column: 2;
  }

  .field-row {
    grid-template-columns: 1fr;
  }
}
</style>
