<template>
  <section class="growth-page">
    <header class="growth-head">
      <button type="button" class="growth-back" aria-label="返回" @click="$router.back()">‹</button>
      <h1 class="growth-title">成长中心</h1>
    </header>

    <div class="growth-body">
      <p v-if="loading" class="growth-status">加载中...</p>
      <template v-else>

        <!-- 1. Identity Card -->
        <article class="growth-card identity-card">
          <div class="identity-level">
            <span class="lv-badge">Lv.{{ growth.level }}</span>
            <div class="identity-tags">
              <span v-if="growth.currentTitle" class="tag tag-title">{{ growth.currentTitle }}</span>
              <span v-if="growth.currentBadge" class="tag tag-badge">{{ growth.currentBadge }}</span>
            </div>
          </div>
          <p class="identity-contrib">总贡献值 <strong>{{ growth.totalContribution }}</strong></p>
          <div class="contrib-grid">
            <div class="contrib-item">
              <label>内容</label>
              <strong>{{ growth.contribContent }}</strong>
            </div>
            <div class="contrib-item">
              <label>组织</label>
              <strong>{{ growth.contribOrg }}</strong>
            </div>
            <div class="contrib-item">
              <label>传播</label>
              <strong>{{ growth.contribSpread }}</strong>
            </div>
            <div class="contrib-item">
              <label>城市</label>
              <strong>{{ growth.contribCity }}</strong>
            </div>
          </div>
        </article>

        <!-- 2. Next Goal Card -->
        <article class="growth-card next-goal-card">
          <h2 class="card-title">下一目标</h2>
          <div class="goal-header">
            <span>Lv.{{ growth.level }}</span>
            <span class="goal-label">
              距 Lv.{{ growth.nextLevel }} 还差
              <strong>{{ growth.contributionToNextLevel }}</strong> 贡献值
            </span>
            <span>Lv.{{ growth.nextLevel }}</span>
          </div>
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: levelProgressPct + '%' }"></div>
          </div>
          <p v-if="growth.nextLevelRewardDisplayText" class="goal-reward">
            升至 Lv.{{ growth.nextLevel }} 可获得：<strong>{{ growth.nextLevelRewardDisplayText }}</strong>
          </p>
          <p v-else-if="growth.level >= 10" class="goal-reward">已达到最高等级，感谢你的持续贡献！</p>
          <p v-else class="goal-reward">升至 Lv.{{ growth.nextLevel }} 继续前进！</p>
        </article>

        <!-- 3. Invite Milestone Card -->
        <article class="growth-card invite-card">
          <h2 class="card-title">邀请里程碑</h2>
          <div class="invite-progress">
            <span class="invite-count">有效邀请 <strong>{{ growth.inviteMilestoneProgress ?? 0 }}</strong> 人</span>
            <span v-if="growth.nextInviteRequiredCount" class="invite-next">
              再邀请
              <strong>{{ growth.nextInviteRequiredCount - (growth.inviteMilestoneProgress ?? 0) }}</strong>
              人，解锁「{{ growth.nextInviteRewardName }}」
            </span>
            <span v-else class="invite-next invite-max">已解锁全部邀请奖励！</span>
          </div>
          <div v-if="growth.nextInviteRequiredCount" class="progress-bar">
            <div class="progress-fill" :style="{ width: inviteProgressPct + '%' }"></div>
          </div>
        </article>

        <!-- 4. Achievement Wall -->
        <article class="growth-card achievement-card">
          <h2 class="card-title">我的成就</h2>
          <div v-if="achievements.length" class="achievement-grid">
            <div v-for="item in achievements" :key="item.id" class="achievement-item">
              <div class="achievement-icon">{{ achievementIcon(item.achievementType) }}</div>
              <p class="achievement-name">{{ item.achievementName || item.achievementCode }}</p>
              <p class="achievement-time">{{ formatDate(item.grantedAt) }}</p>
            </div>
          </div>
          <p v-else class="empty-state">继续发动态、加入团体、邀请朋友，即可解锁成长奖励。</p>
        </article>

        <!-- 5. Recent Contributions -->
        <article class="growth-card contrib-log-card">
          <h2 class="card-title">最近成长记录</h2>
          <ul v-if="contributions.length" class="contrib-list">
            <li v-for="log in contributions" :key="log.id" class="contrib-row">
              <div>
                <strong>{{ eventLabel(log.eventType) }}</strong>
                <p>{{ dimensionLabel(log.dimension) }}贡献 · {{ formatTime(log.occurredAt) }}</p>
              </div>
              <span class="contrib-delta">+{{ log.deltaFinal }}</span>
            </li>
          </ul>
          <p v-else class="empty-state">暂无贡献记录</p>
        </article>

      </template>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getGrowthAchievements, getGrowthCenter, getGrowthContributions } from '@/api/growth.js'

const loading = ref(false)
const growth = ref({
  level: 1,
  stage: 'normal',
  totalContribution: 0,
  contribContent: 0,
  contribOrg: 0,
  contribSpread: 0,
  contribCity: 0,
  nextLevel: 2,
  nextLevelThreshold: 50,
  contributionToNextLevel: 50,
  currentTitle: null,
  currentBadge: null,
  nextLevelRewardName: null,
  nextLevelRewardDisplayText: null,
  inviteMilestoneProgress: 0,
  nextInviteRewardName: null,
  nextInviteRequiredCount: null,
})
const contributions = ref([])
const achievements = ref([])

const levelProgressPct = computed(() => {
  const threshold = growth.value.nextLevelThreshold
  const toNext = growth.value.contributionToNextLevel
  if (threshold <= 0) return 100
  const earned = threshold - toNext
  return Math.min(100, Math.max(0, Math.round((earned / threshold) * 100)))
})

const inviteProgressPct = computed(() => {
  const required = growth.value.nextInviteRequiredCount
  const current = growth.value.inviteMilestoneProgress ?? 0
  if (!required) return 100
  const MILESTONES = [0, 1, 3, 5, 10]
  const prevMilestone = MILESTONES.filter(m => m < required).pop() ?? 0
  const span = required - prevMilestone
  const progress = current - prevMilestone
  return Math.min(100, Math.max(0, Math.round((progress / span) * 100)))
})

const EVENT_LABELS = {
  POST_CREATED: '发布动态',
  POST_LIKED: '收到点赞',
  POST_COMMENTED: '收到评论',
  GROUP_CREATED: '创建团体',
  GROUP_JOINED: '加入团体',
  USER_REGISTERED: '完成注册',
  USER_PROFILE_COMPLETED: '完善个人资料',
  USER_DAILY_ACTIVE: '每日活跃',
  USER_INVITED_REGISTERED: '邀请好友注册',
  USER_INVITED_EFFECTIVE: '有效邀请',
}

const DIMENSION_LABELS = {
  CONTENT: '内容',
  ORG: '组织',
  SPREAD: '传播',
  CITY: '城市',
}

const ACHIEVEMENT_ICONS = { BADGE: '🏅', TITLE: '🎖', CANDIDATE: '⭐' }

function eventLabel(type) { return EVENT_LABELS[type] || type }
function dimensionLabel(dim) { return DIMENSION_LABELS[dim] || dim }
function achievementIcon(type) { return ACHIEVEMENT_ICONS[type] || '🏆' }

function formatTime(value) {
  if (!value) return ''
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function formatDate(value) {
  if (!value) return ''
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

onMounted(async () => {
  loading.value = true
  try {
    const [centerRes, contribRes, achieveRes] = await Promise.allSettled([
      getGrowthCenter(),
      getGrowthContributions(20),
      getGrowthAchievements(20),
    ])
    if (centerRes.status === 'fulfilled' && centerRes.value) {
      growth.value = { ...growth.value, ...centerRes.value }
    }
    contributions.value = contribRes.status === 'fulfilled' && Array.isArray(contribRes.value)
      ? contribRes.value : []
    achievements.value = achieveRes.status === 'fulfilled' && Array.isArray(achieveRes.value)
      ? achieveRes.value : []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.growth-page {
  min-height: 100vh;
  background: var(--lc-bg);
  color: var(--lc-text);
}

.growth-head {
  display: flex;
  align-items: center;
  background: var(--lc-surface);
  border-bottom: 1px solid var(--lc-soft-alt);
  position: sticky;
  top: 0;
  z-index: 10;
}

.growth-back {
  width: 48px;
  height: 48px;
  border: 0;
  background: transparent;
  font-size: 22px;
  color: var(--lc-indigo);
  cursor: pointer;
}

.growth-title {
  margin: 0;
  font-size: 17px;
  font-weight: 800;
}

.growth-body {
  max-width: 720px;
  margin: 0 auto;
  padding: 14px 12px calc(90px + env(safe-area-inset-bottom));
}

.growth-status {
  text-align: center;
  padding: 36px 0;
  color: var(--lc-subtle);
}

.growth-card {
  background: var(--lc-surface);
  border: 1px solid var(--lc-soft-alt);
  border-radius: 14px;
  padding: 16px;
  margin-bottom: 12px;
}

.card-title {
  margin: 0 0 12px;
  font-size: 15px;
  font-weight: 700;
}

/* Identity Card */
.identity-level {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.lv-badge {
  background: linear-gradient(135deg, var(--lc-indigo), #2563eb);
  color: #fff;
  font-size: 15px;
  font-weight: 900;
  padding: 4px 14px;
  border-radius: 999px;
  white-space: nowrap;
}

.identity-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.tag {
  font-size: 12px;
  font-weight: 700;
  padding: 2px 10px;
  border-radius: 999px;
}

.tag-title {
  background: rgba(236, 72, 153, 0.1);
  color: var(--lc-pink);
  border: 1px solid rgba(236, 72, 153, 0.2);
}

.tag-badge {
  background: rgba(99, 102, 241, 0.1);
  color: var(--lc-indigo);
  border: 1px solid rgba(99, 102, 241, 0.2);
}

.identity-contrib {
  margin: 0 0 12px;
  font-size: 13px;
  color: var(--lc-subtle);
}

.identity-contrib strong {
  color: var(--lc-text);
  font-size: 20px;
  font-weight: 900;
}

.contrib-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
}

.contrib-item {
  background: var(--lc-bg);
  border-radius: 10px;
  padding: 8px;
  text-align: center;
}

.contrib-item label {
  display: block;
  color: var(--lc-subtle);
  font-size: 11px;
  margin-bottom: 4px;
}

.contrib-item strong {
  font-size: 16px;
  font-weight: 800;
}

/* Goal + Invite shared */
.progress-bar {
  height: 8px;
  background: var(--lc-bg);
  border-radius: 999px;
  overflow: hidden;
  margin-bottom: 10px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--lc-indigo), var(--lc-pink));
  border-radius: 999px;
  transition: width 0.4s ease;
}

/* Next Goal Card */
.goal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 13px;
  color: var(--lc-subtle);
}

.goal-label {
  text-align: center;
  color: var(--lc-slate);
}

.goal-label strong {
  color: var(--lc-text);
}

.goal-reward {
  margin: 0;
  font-size: 13px;
  color: var(--lc-subtle);
}

.goal-reward strong {
  color: var(--lc-violet);
}

/* Invite Milestone Card */
.invite-progress {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 10px;
}

.invite-count {
  font-size: 14px;
}

.invite-count strong {
  font-size: 22px;
  font-weight: 900;
  color: var(--lc-indigo);
}

.invite-next {
  font-size: 13px;
  color: var(--lc-subtle);
}

.invite-next strong {
  color: var(--lc-text);
}

.invite-max {
  color: var(--lc-emerald);
  font-weight: 700;
}

/* Achievement Wall */
.achievement-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.achievement-item {
  background: var(--lc-bg);
  border-radius: 10px;
  padding: 12px 8px;
  text-align: center;
}

.achievement-icon {
  font-size: 26px;
  margin-bottom: 6px;
}

.achievement-name {
  margin: 0 0 4px;
  font-size: 12px;
  font-weight: 700;
}

.achievement-time {
  margin: 0;
  font-size: 11px;
  color: var(--lc-subtle);
}

/* Contribution Log */
.contrib-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.contrib-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid var(--lc-soft);
}

.contrib-row:last-child {
  border-bottom: 0;
}

.contrib-row strong {
  font-size: 13px;
}

.contrib-row p {
  margin: 3px 0 0;
  font-size: 12px;
  color: var(--lc-subtle);
}

.contrib-delta {
  font-weight: 800;
  font-size: 15px;
  color: var(--lc-violet);
  white-space: nowrap;
}

/* Shared */
.empty-state {
  margin: 0;
  font-size: 13px;
  color: var(--lc-subtle);
  text-align: center;
  padding: 16px 0;
}
</style>
