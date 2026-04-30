<template>
  <section class="dash" aria-label="平台个人中心工作台（桌面端）">
    <header class="dash-header">
      <div>
        <h1>个人中心</h1>
        <p>管理你的内容、团队、通知与账号资料</p>
      </div>
      <button type="button" class="icon-btn" aria-label="账号设置" @click="onOpenSettings">⚙</button>
    </header>

    <div class="dash-grid dash-top-grid">
      <HeroUserCard
        class="profile-panel"
        :user="user"
        :display-name="displayName"
        :user-id-display="userIdDisplay"
        :location-display="locationDisplay"
        :invite-code-display="inviteCodeDisplay"
        :copy-feedback="copyFeedback"
        :copy-feedback-error="copyFeedbackError"
        :profile-light-stats="profileLightStats"
        @edit="onOpenEdit"
        @copy-invite="onCopyInvite"
        @open-fellowship="onOpenFellowship"
      />

      <GrowthPanel class="growth-panel" :growth-level="growthLevel" :growth-progress="growthProgress" :compact="true" />

      <section ref="tasksPanelRef" class="card top-task-card task-panel" aria-label="今日任务">
        <header class="panel-head">
          <div>
            <h2>今日任务 <span>每日 0 点刷新</span></h2>
          </div>
          <p>已完成 <b>{{ completedTaskCount }}</b>/<b>{{ dailyTaskTotal }}</b></p>
        </header>

        <div class="top-task-list">
          <div v-for="task in dailyTasks" :key="task.title" class="top-task-row" :class="{ done: task.done }">
            <span class="task-dot" :class="{ ok: task.done }" aria-hidden="true">{{ task.done ? '✓' : '' }}</span>
            <strong>{{ task.title }}</strong>
            <span class="exp">+{{ task.exp }} 经验</span>
            <span class="mini-track" aria-hidden="true">
              <span :style="{ width: taskProgress(task) }"></span>
            </span>
            <small>{{ task.done ? '已完成' : `${task.current}/${task.total}` }}</small>
          </div>
        </div>

        <button type="button" class="ghost-wide-btn" @click="scrollToTasks">查看所有任务</button>
      </section>
    </div>

    <div class="dash-grid dash-overview-row">
      <StatsOverview class="overview-panel" :items="coreOverviewItems" />
      <AchievementPanel class="achievement-panel" :items="achievementItems" />
      <ActivityPanel class="activity-panel" :items="activityItems" />
    </div>

    <GroupPanel :group-info="groupInfo" :group-ranking="groupRanking" />

    <QuickActions :items="quickActions" />
  </section>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import ActivityPanel from './panels/ActivityPanel.vue'
import AchievementPanel from './panels/AchievementPanel.vue'
import GroupPanel from './panels/GroupPanel.vue'
import GrowthPanel from './panels/GrowthPanel.vue'
import HeroUserCard from './panels/HeroUserCard.vue'
import QuickActions from './panels/QuickActions.vue'
import StatsOverview from './panels/StatsOverview.vue'

const props = defineProps({
  user: { type: Object, default: null },
  displayName: { type: String, required: true },
  userIdDisplay: { type: [String, Number], required: true },
  locationDisplay: { type: String, required: true },
  inviteCodeDisplay: { type: String, required: true },
  copyFeedback: { type: String, default: '' },
  copyFeedbackError: { type: Boolean, default: false },
  profileLightStats: { type: Array, required: true },
  growthLevel: { type: Object, required: true },
  growthProgress: { type: String, required: true },
  completedTaskCount: { type: Number, required: true },
  dailyTasks: { type: Array, required: true },
  overviewItems: { type: Array, required: true },
  groupInfo: { type: Object, required: true },
  groupRanking: { type: Array, required: true },
  quickActions: { type: Array, required: true },
  onOpenSettings: { type: Function, required: true },
  onOpenEdit: { type: Function, required: true },
  onCopyInvite: { type: Function, required: true },
})

const router = useRouter()
const tasksPanelRef = ref(null)
const dailyTaskTotal = computed(() => props.dailyTasks.length)
const coreOverviewItems = computed(() => props.overviewItems.slice(0, 3))

const achievementItems = computed(() => [
  { key: 'newbie', title: '新手创作者', desc: '完成首次创作', icon: '⬢', achieved: true },
  { key: 'group-admin', title: '团体管理员', desc: '管理一个团体', icon: '⬣', achieved: true },
  { key: 'daily-share', title: '每日心声', desc: '连续发布 7 天', icon: '⬡', achieved: false },
  { key: 'explorer', title: '内容探索者', desc: '阅读 100 篇内容', icon: '⬟', achieved: false }
])

const activityItems = computed(() => [
  { key: 'a1', icon: '✦', title: '发布了 1 条每日心声', time: '2 小时前' },
  { key: 'a2', icon: '♙', title: `加入了 ${props.groupInfo.name}`, time: '1 天前' },
  { key: 'a3', icon: '✓', title: '完成了今日任务：发布每日心声', time: '2 天前' },
  { key: 'a4', icon: '♡', title: '获得了 1 次点赞', time: '3 天前' }
])

function onOpenFellowship() {
  router.push('/fellowship')
}

function scrollToTasks() {
  const el = tasksPanelRef.value
  if (el && typeof el.scrollIntoView === 'function') {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

function taskProgress(task) {
  if (!task?.total) return '0%'
  return `${Math.min(100, Math.round((task.current / task.total) * 100))}%`
}
</script>

<style scoped>
.dash {
  --dash-card: var(--lc-surface);
  --dash-border: #e8ecf4;
  --dash-soft: #f7f8fc;
  --dash-text: var(--lc-text);
  --dash-muted: #7b8498;
  --dash-primary: #6d5dfb;
  --dash-primary-dark: #4f46e5;
  --dash-shadow: 0 12px 28px rgba(15, 23, 42, 0.055);
  background: transparent;
  color: var(--dash-text);
  width: min(100% - 40px, 1720px);
  margin: 0 auto;
  padding: 0 0 24px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dash-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.dash-header h1 {
  margin: 0;
  font-size: 33px;
  font-weight: 800;
  line-height: 1.12;
}

.dash-header p {
  margin: 6px 0 0;
  font-size: 14px;
  color: var(--dash-muted);
}

.icon-btn {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  border: 1px solid var(--dash-border);
  background: var(--dash-card);
  cursor: pointer;
  font-size: 18px;
  color: var(--lc-muted);
}

.card {
  background: var(--dash-card);
  border: 1px solid var(--dash-border);
  border-radius: 12px;
  box-shadow: var(--dash-shadow);
}

.dash-grid {
  display: grid;
  grid-template-columns: repeat(12, minmax(0, 1fr));
  gap: 16px;
  align-items: stretch;
}

.dash-top-grid {
  grid-auto-rows: minmax(272px, auto);
}

.dash-overview-row {
  grid-auto-rows: minmax(210px, auto);
}

.profile-panel {
  grid-column: span 5;
}

.growth-panel {
  grid-column: span 2;
}

.task-panel {
  grid-column: span 5;
}

.overview-panel {
  grid-column: span 3;
}

.achievement-panel {
  grid-column: span 4;
}

.activity-panel {
  grid-column: span 5;
}

.top-task-card {
  display: flex;
  flex-direction: column;
  padding: 22px 28px 20px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.panel-head h2 {
  margin: 0;
  font-size: 16px;
  font-weight: 900;
}

.panel-head h2 span {
  margin-left: 8px;
  color: var(--dash-muted);
  font-size: 12px;
  font-weight: 600;
}

.panel-head p {
  margin: 0;
  color: var(--dash-muted);
  font-size: 12px;
  font-weight: 700;
}

.panel-head b {
  color: var(--dash-text);
}

.top-task-list {
  display: grid;
  flex: 1;
  align-content: center;
  gap: 12px;
}

.top-task-row {
  display: grid;
  grid-template-columns: 22px minmax(0, 1fr) 68px 88px 46px;
  gap: 10px;
  align-items: center;
  min-height: 26px;
  font-size: 12px;
}

.top-task-row strong {
  min-width: 0;
  overflow: hidden;
  color: var(--dash-text);
  font-weight: 700;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.top-task-row.done strong {
  color: var(--lc-subtle);
}

.task-dot {
  display: grid;
  place-items: center;
  width: 20px;
  height: 20px;
  border: 1px solid #cfd6e3;
  border-radius: 50%;
  color: var(--dash-card);
  font-size: 11px;
  font-weight: 900;
}

.task-dot.ok {
  border-color: var(--dash-primary);
  background: var(--dash-primary);
}

.exp,
.top-task-row small {
  color: var(--dash-muted);
  font-size: 11px;
  white-space: nowrap;
}

.mini-track {
  overflow: hidden;
  height: 6px;
  border-radius: 999px;
  background: #e7eaf1;
}

.mini-track span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, var(--dash-primary), var(--dash-primary-dark));
}

.ghost-wide-btn {
  width: 100%;
  height: 38px;
  margin-top: 18px;
  border: 1px solid var(--dash-border);
  border-radius: 10px;
  color: var(--lc-muted);
  background: var(--dash-card);
  font-size: 12px;
  font-weight: 800;
  cursor: pointer;
}

.dash-top-grid > *,
.dash-overview-row > * {
  min-width: 0;
  height: 100%;
}

@media (max-width: 1180px) {
  .dash {
    width: min(100% - 24px, 1180px);
  }

  .dash-grid {
    grid-template-columns: 1fr;
  }

  .profile-panel,
  .overview-panel,
  .growth-panel,
  .achievement-panel,
  .task-panel,
  .activity-panel {
    grid-column: auto;
  }
}
</style>
