<template>
  <div class="sp-body">
    <p v-if="loading" class="sp-status">加载中...</p>
    <template v-else>
      <p v-if="accountHint" class="sp-hint">{{ accountHint }}</p>

      <div v-if="newcomerPack.eligible && newcomerTasks.length" class="sp-card account-block">
        <div class="sp-card-title">新人 7 日任务 · 第 {{ newcomerPack.currentDay }} 天</div>
        <p class="sp-sub">注册后 7 天内可完成，奖励需手动领取。</p>
        <div class="task-list">
          <div
            v-for="task in newcomerTasks"
            :key="task.code"
            class="task-row account"
            :class="{ done: task.completed, locked: !task.unlocked }"
          >
            <div class="task-check">{{ task.completed ? '✓' : '' }}</div>
            <div class="task-body">
              <div class="task-name">{{ task.title }}</div>
              <div class="task-exp">+{{ task.exp }} 经验</div>
            </div>
            <button
              v-if="task.completed && !task.claimed"
              type="button"
              class="task-btn claim"
              :disabled="claimingNewcomer === task.code"
              @click="claimOneNewcomer(task.code)"
            >
              {{ claimingNewcomer === task.code ? '…' : '领取' }}
            </button>
            <span v-else-if="task.claimed" class="task-btn done">已领取</span>
            <span v-else-if="!task.unlocked" class="task-btn locked">第 {{ task.unlockDay }} 天解锁</span>
            <router-link v-else class="task-btn" :to="newcomerTaskRoute(task.code)">去完成</router-link>
          </div>
        </div>
      </div>

      <div v-if="weeklyTasks.length" class="sp-card account-block">
        <div class="sp-card-title">本周挑战</div>
        <p class="sp-sub">每周一刷新，完成后点「领取」。</p>
        <div class="task-list">
          <div v-for="task in weeklyTasks" :key="task.code" class="task-row account" :class="{ done: task.completed }">
            <div class="task-check">{{ task.completed ? '✓' : '' }}</div>
            <div class="task-body">
              <div class="task-name">{{ task.title }}</div>
              <div class="task-exp">+{{ task.exp }} 经验 · {{ task.current }}/{{ task.total }}</div>
            </div>
            <button
              v-if="task.completed && !task.claimed"
              type="button"
              class="task-btn claim"
              :disabled="claimingWeekly === task.code"
              @click="claimOneWeekly(task.code)"
            >
              {{ claimingWeekly === task.code ? '…' : '领取' }}
            </button>
            <span v-else-if="task.claimed" class="task-btn done">已领取</span>
            <router-link v-else class="task-btn" :to="weeklyTaskRoute(task.code)">去完成</router-link>
          </div>
        </div>
      </div>

      <div v-if="accountTasks.length" class="sp-card account-block">
        <div class="sp-card-title">账号成长任务</div>
        <p class="sp-sub">一次性任务，经验更多；完成后请先点「领取」。</p>
        <div class="task-list">
          <div v-for="task in accountTasks" :key="task.code" class="task-row account" :class="{ done: task.completed }">
            <div class="task-check">{{ task.completed ? '✓' : '' }}</div>
            <div class="task-body">
              <div class="task-name">{{ task.title }}</div>
              <div class="task-exp">+{{ task.exp }} 经验</div>
            </div>
            <button
              v-if="task.completed"
              type="button"
              class="task-btn claim"
              :disabled="claimingAccount === task.code"
              @click="claimOneAccount(task.code)"
            >
              {{ claimingAccount === task.code ? '…' : '领取' }}
            </button>
            <router-link v-else class="task-btn" :to="task.to">去完成</router-link>
          </div>
        </div>
      </div>

      <div class="sp-card task-summary">
        <div class="ts-level">
          <div class="ts-lv-badge">Lv.{{ growthLevel }}</div>
          <div class="ts-lv-info">
            <div class="ts-lv-name">{{ growthName }}</div>
            <div class="ts-lv-hint">{{ completedCount }}/{{ tasks.length }} 任务完成 · 每日 0 点刷新</div>
          </div>
        </div>
        <div class="ts-progress-bar">
          <div class="ts-progress-fill" :style="{ width: `${(completedCount / Math.max(1, tasks.length)) * 100}%` }"></div>
        </div>
      </div>

      <p v-if="!tasks.length" class="sp-status sp-empty">今日任务加载失败，请稍后再试。</p>
      <div v-else class="task-list">
        <div v-for="task in tasks" :key="task.title" class="sp-card task-row" :class="{ done: task.done }">
          <div class="task-check">{{ task.done ? '✓' : '' }}</div>
          <div class="task-body">
            <div class="task-name">{{ task.title }}</div>
            <div class="task-exp">+{{ task.exp }} 经验</div>
            <div v-if="!task.done" class="task-progress-bar">
              <div
                class="task-progress-fill"
                :style="{ width: `${Math.min(100, (task.current / Math.max(1, task.total)) * 100)}%` }"
              ></div>
            </div>
            <div class="task-sub">{{ task.done ? '已完成' : `${task.current}/${task.total}` }}</div>
          </div>
          <router-link :to="task.to" class="task-btn" :class="{ done: task.done }">
            {{ task.done ? '已完成' : '去完成' }}
          </router-link>
        </div>
      </div>

      <div class="sp-card">
        <div class="sp-card-title">经验获取方式</div>
        <div v-for="s in expSources" :key="s.label" class="exp-row">
          <span class="exp-icon">{{ s.icon }}</span>
          <span class="exp-label">{{ s.label }}</span>
          <span class="exp-val">+{{ s.exp }} 经验/次</span>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { claimAccountTask, claimNewcomerTask, claimWeeklyTask, getMyGrowth } from '@/api/growth.js'
import { usePlayTaskRoutes } from '@/composables/usePlayTaskRoutes.js'

const {
  dailyTaskRoute,
  weeklyTaskRoute,
  newcomerTaskRoute,
  accountTaskRoute,
  mockTasks
} = usePlayTaskRoutes()

const loading = ref(false)
const growthLevel = ref(1)
const growthName = ref('新手用户')
const tasks = ref([])
const accountTasks = ref([])
const weeklyTasks = ref([])
const newcomerTasks = ref([])
const newcomerPack = ref({ eligible: false, currentDay: 0 })
const claimingAccount = ref('')
const claimingWeekly = ref('')
const claimingNewcomer = ref('')
const accountHint = ref('')

function mapWeeklyTasks(rows) {
  return Array.isArray(rows)
    ? rows.map(item => ({
        code: item.code,
        title: item.name || item.code,
        exp: Number(item.rewardExp ?? 0),
        current: Number(item.progress ?? 0),
        total: Number(item.targetCount ?? 1),
        completed: Boolean(item.completed),
        claimed: Boolean(item.claimed)
      }))
    : []
}

function mapNewcomerTasks(rows) {
  return Array.isArray(rows)
    ? rows.map(item => ({
        code: item.code,
        title: item.name || item.code,
        exp: Number(item.rewardExp ?? 0),
        unlockDay: Number(item.unlockDay ?? 1),
        unlocked: Boolean(item.unlocked),
        completed: Boolean(item.completed),
        claimed: Boolean(item.claimed)
      }))
    : []
}

async function reloadGrowth() {
  const data = await getMyGrowth()
  growthLevel.value = Number(data?.level ?? 1)
  growthName.value = data?.title || '新手用户'
  tasks.value = Array.isArray(data?.dailyTasks) && data.dailyTasks.length
    ? data.dailyTasks.map(item => ({
        title: item.name || item.code,
        exp: Number(item.rewardExp ?? 0),
        current: Number(item.progress ?? 0),
        total: Number(item.targetCount ?? 1),
        done: Boolean(item.completed),
        to: dailyTaskRoute(item.code)
      }))
    : tasks.value
  accountTasks.value = Array.isArray(data?.accountTasks)
    ? data.accountTasks.map(item => ({
        code: item.code,
        title: item.name || item.code,
        exp: Number(item.rewardExp ?? 0),
        completed: Boolean(item.completed),
        to: accountTaskRoute(item.code)
      }))
    : []
  weeklyTasks.value = mapWeeklyTasks(data?.weeklyTasks)
  const pack = data?.newcomerPack || {}
  newcomerPack.value = {
    eligible: Boolean(pack.eligible),
    currentDay: Number(pack.currentDay ?? 0)
  }
  newcomerTasks.value = mapNewcomerTasks(pack.tasks)
}

const completedCount = computed(() => tasks.value.filter(t => t.done).length)

async function claimOneWeekly(code) {
  if (!code || claimingWeekly.value) return
  claimingWeekly.value = code
  accountHint.value = ''
  try {
    const res = await claimWeeklyTask(code)
    if (res?.claimed) {
      accountHint.value = `本周挑战已领取 +${Number(res.rewardExp ?? 0)} 经验`
      await reloadGrowth()
    }
  } catch (e) {
    accountHint.value = e?.response?.data?.message || '领取失败'
  } finally {
    claimingWeekly.value = ''
    window.setTimeout(() => { accountHint.value = '' }, 4000)
  }
}

async function claimOneNewcomer(code) {
  if (!code || claimingNewcomer.value) return
  claimingNewcomer.value = code
  accountHint.value = ''
  try {
    const res = await claimNewcomerTask(code)
    if (res?.claimed) {
      accountHint.value = `新人任务已领取 +${Number(res.rewardExp ?? 0)} 经验`
      await reloadGrowth()
    }
  } catch (e) {
    accountHint.value = e?.response?.data?.message || '领取失败'
  } finally {
    claimingNewcomer.value = ''
    window.setTimeout(() => { accountHint.value = '' }, 4000)
  }
}

async function claimOneAccount(code) {
  if (!code || claimingAccount.value) return
  claimingAccount.value = code
  accountHint.value = ''
  try {
    const res = await claimAccountTask(code)
    if (res?.claimed) {
      accountHint.value = `已领取 +${Number(res.rewardExp ?? 0)} 经验`
      await reloadGrowth()
    } else {
      accountHint.value = String(res?.message || '暂不可领取')
    }
  } catch (e) {
    accountHint.value = e?.response?.data?.message || '领取失败，请稍后重试'
  } finally {
    claimingAccount.value = ''
    window.setTimeout(() => { accountHint.value = '' }, 4000)
  }
}

const expSources = [
  { icon: '📅', label: '每日登录', exp: 2 },
  { icon: '📝', label: '发布动态', exp: 10 },
  { icon: '👁', label: '浏览内容', exp: 1 },
  { icon: '👍', label: '点赞内容', exp: 2 },
  { icon: '👤', label: '完善资料', exp: 5 }
]

onMounted(async () => {
  loading.value = true
  try {
    await reloadGrowth()
    if (!tasks.value.length) tasks.value = mockTasks()
  } catch {
    tasks.value = mockTasks()
    accountTasks.value = []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.sp-body {
  padding: 16px 14px calc(80px + env(safe-area-inset-bottom));
  max-width: 680px;
  margin: 0 auto;
}
.sp-status { text-align: center; padding: 40px 0; color: var(--lc-subtle); font-size: 14px; }
.sp-empty::before { display: block; font-size: 32px; margin-bottom: 10px; content: "✅"; }

.sp-hint {
  text-align: center;
  font-size: 13px;
  font-weight: 700;
  color: var(--lc-emerald);
  margin: 0 0 10px;
}

.account-block .sp-sub {
  margin: -4px 0 12px;
  font-size: 12px;
  color: var(--lc-subtle);
  line-height: 1.45;
}

.task-btn.claim {
  border-color: var(--lc-violet);
  color: var(--lc-surface);
  background: linear-gradient(135deg, var(--lc-violet), var(--lc-indigo));
  cursor: pointer;
}

.task-btn.claim:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.sp-card {
  background: var(--lc-surface);
  border: 1px solid var(--lc-soft-alt);
  border-radius: 16px;
  box-shadow: 0 3px 12px rgba(15, 23, 42, 0.04);
  margin-bottom: 12px;
  padding: 16px;
}
.sp-card-title { font-size: 15px; font-weight: 800; margin-bottom: 12px; color: var(--lc-text); }

.task-summary { padding: 16px; }
.ts-level { display: flex; align-items: center; gap: 14px; margin-bottom: 14px; }
.ts-lv-badge {
  width: 52px;
  height: 52px;
  flex: 0 0 auto;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, var(--lc-violet), var(--lc-indigo));
  clip-path: polygon(25% 5%, 75% 5%, 100% 50%, 75% 95%, 25% 95%, 0 50%);
  color: var(--lc-surface);
  font-size: 13px;
  font-weight: 900;
}
.ts-lv-name { font-size: 15px; font-weight: 800; }
.ts-lv-hint { font-size: 12px; color: var(--lc-subtle); margin-top: 3px; }
.ts-progress-bar { height: 8px; border-radius: 999px; background: var(--lc-soft-alt); overflow: hidden; }
.ts-progress-fill {
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, var(--lc-violet), var(--lc-indigo));
  transition: width 0.5s;
}

.task-list { display: flex; flex-direction: column; gap: 0; }
.task-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 0;
  border-left: 0;
  border-right: 0;
  box-shadow: none;
}
.task-row:first-child { border-radius: 16px 16px 0 0; }
.task-row:last-child { border-radius: 0 0 16px 16px; border-bottom: 1px solid var(--lc-soft-alt); }
.task-row:not(:last-child) { border-bottom: 1px solid var(--lc-soft); }
.task-row.done { background: var(--lc-emerald-light); }

.task-check {
  width: 28px;
  height: 28px;
  flex: 0 0 auto;
  display: grid;
  place-items: center;
  border-radius: 50%;
  border: 2px solid var(--lc-border);
  font-size: 14px;
  font-weight: 800;
  color: transparent;
}
.task-row.done .task-check {
  background: var(--lc-emerald);
  border-color: var(--lc-emerald);
  color: var(--lc-surface);
}

.task-body { flex: 1; min-width: 0; }
.task-name { font-size: 14px; font-weight: 600; color: var(--lc-text); }
.task-exp { font-size: 11px; color: var(--lc-subtle); margin-top: 2px; }
.task-progress-bar {
  height: 4px;
  border-radius: 999px;
  background: var(--lc-soft-alt);
  overflow: hidden;
  margin: 6px 0 4px;
}
.task-progress-fill { height: 100%; border-radius: inherit; background: var(--lc-violet); }
.task-sub { font-size: 11px; color: var(--lc-subtle); }

.task-btn {
  flex: 0 0 auto;
  border: 1px solid var(--lc-blue-border);
  border-radius: 999px;
  background: var(--lc-surface);
  color: var(--lc-indigo);
  font-size: 12px;
  font-weight: 700;
  padding: 6px 14px;
  text-decoration: none;
}
.task-btn.done {
  border-color: var(--lc-green-light);
  color: var(--lc-emerald);
  background: var(--lc-emerald-light);
}
.task-btn.locked {
  border-color: var(--lc-soft);
  color: var(--lc-subtle);
  background: var(--lc-soft-alt);
  cursor: default;
}

.exp-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 0;
  border-bottom: 1px solid var(--lc-soft);
}
.exp-row:last-child { border-bottom: 0; padding-bottom: 0; }
.exp-icon { font-size: 16px; width: 24px; text-align: center; flex: 0 0 auto; }
.exp-label { flex: 1; font-size: 13px; color: var(--lc-slate); }
.exp-val { font-size: 12px; color: var(--lc-violet); font-weight: 700; }
</style>
