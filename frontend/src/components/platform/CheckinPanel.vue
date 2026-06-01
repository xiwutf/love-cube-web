<template>
  <div class="ck-body">
    <div class="ck-hero">
      <div class="ck-hero-top">
        <div>
          <p class="ck-kicker">TODAY</p>
          <p class="ck-date">{{ todayText }}</p>
        </div>
        <span class="ck-status" :class="{ ok: isCheckedInToday }">
          {{ isCheckedInToday ? '今日已签到' : '今日未签到' }}
        </span>
      </div>

      <div class="ck-actions">
        <button
          type="button"
          class="ck-btn ck-btn-primary"
          :disabled="loading || isCheckedInToday"
          @click="handleCheckin"
        >
          {{ isCheckedInToday ? '已签到' : loading ? '签到中…' : '立即签到' }}
        </button>

        <button
          v-if="claimableDailyLoginTask"
          type="button"
          class="ck-btn"
          :disabled="claiming"
          @click="handleClaim"
        >
          {{ claiming ? '领取中…' : `领取 +${Number(claimableDailyLoginTask.rewardExp ?? 0)} 经验` }}
        </button>
      </div>

      <p class="ck-tip">签到会记录一次「登录」成长行为，并同步更新今日任务进度。</p>
      <div v-if="loginStreak" class="ck-streak">
        <div class="ck-streak-row">
          <span>连续签到</span>
          <strong>{{ loginStreak.currentStreak ?? 0 }} 天</strong>
        </div>
        <div class="ck-streak-row">
          <span>最长记录</span>
          <strong>{{ loginStreak.longestStreak ?? 0 }} 天</strong>
        </div>
        <p v-if="loginStreak.nextMilestone && (loginStreak.daysToNextMilestone ?? 0) > 0" class="ck-streak-hint">
          再签 {{ loginStreak.daysToNextMilestone }} 天，额外奖励 +{{ loginStreak.nextMilestoneBonus ?? 0 }} 经验
        </p>
      </div>
    </div>

    <div class="ck-card">
      <div class="ck-card-title">我的成长</div>
      <div v-if="overview" class="ck-growth">
        <div class="ck-growth-row">
          <span>当前等级</span>
          <strong>Lv.{{ overview.level }}</strong>
        </div>
        <div class="ck-growth-row">
          <span>称号</span>
          <strong>{{ overview.title }}</strong>
        </div>
        <div class="ck-growth-row">
          <span>经验</span>
          <strong>{{ overview.exp }}/{{ overview.nextLevelExp }}</strong>
        </div>
        <div class="ck-progress" role="progressbar" :aria-valuenow="expProgressValue" aria-valuemin="0" aria-valuemax="100">
          <div class="ck-progress-fill" :style="{ width: expProgress }"></div>
        </div>
      </div>
      <p v-else class="ck-muted">加载中…</p>
    </div>

    <div class="ck-card">
      <div class="ck-card-title">今日任务 · 登录</div>
      <div v-if="dailyLoginTask" class="ck-task">
        <div class="ck-task-main">
          <div>
            <div class="ck-task-name">{{ dailyLoginTask.name || '每日登录' }}</div>
            <div class="ck-task-sub">
              {{ dailyLoginTask.progress }}/{{ dailyLoginTask.targetCount }}
              <span class="ck-task-sep" aria-hidden="true">·</span>
              奖励 +{{ Number(dailyLoginTask.rewardExp ?? 0) }} 经验
            </div>
          </div>
          <span class="ck-pill" :class="{ ok: Boolean(dailyLoginTask.completed) }">
            {{ dailyLoginTask.completed ? (dailyLoginTask.claimed ? '已领取' : '可领取') : '进行中' }}
          </span>
        </div>
        <div class="ck-task-progress">
          <div class="ck-task-progress-fill" :style="{ width: taskProgress }"></div>
        </div>
      </div>
      <p v-else class="ck-muted">暂无登录类任务配置（请检查后台 daily_tasks 表）。</p>
    </div>

    <p class="ck-more">
      <router-link :to="platformPath('tasks')">查看全部今日任务 →</router-link>
    </p>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { showToast } from 'vant'
import { claimDailyTask, getMyGrowth, recordGrowthAction } from '@/api/growth.js'
import { usePlatformPath } from '@/composables/usePlatformPath.js'

const { platformPath } = usePlatformPath()

const loading = ref(false)
const claiming = ref(false)
const overview = ref(null)

const loginStreak = computed(() => overview.value?.loginStreak || null)

const todayKey = computed(() => {
  const d = new Date()
  const yyyy = d.getFullYear()
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd}`
})

const todayText = computed(() => {
  const d = new Date()
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${mm}.${dd}`
})

const dailyLoginTask = computed(() => {
  const tasks = overview.value?.dailyTasks
  if (!Array.isArray(tasks)) return null
  return tasks.find(t => String(t?.actionType || '').toUpperCase() === 'LOGIN') || null
})

const isCheckedInToday = computed(() => Boolean(dailyLoginTask.value?.completed))

const claimableDailyLoginTask = computed(() => {
  const t = dailyLoginTask.value
  if (!t || !t.completed || t.claimed) return null
  return t
})

const expProgressValue = computed(() => {
  const cur = Number(overview.value?.exp ?? 0)
  const next = Math.max(1, Number(overview.value?.nextLevelExp ?? 1))
  return Math.min(100, Math.round((cur / next) * 100))
})
const expProgress = computed(() => `${expProgressValue.value}%`)

const taskProgress = computed(() => {
  const t = dailyLoginTask.value
  if (!t) return '0%'
  const cur = Number(t.progress ?? 0)
  const total = Math.max(1, Number(t.targetCount ?? 1))
  return `${Math.min(100, Math.round((cur / total) * 100))}%`
})

async function refresh() {
  overview.value = await getMyGrowth()
}

async function handleCheckin() {
  if (loading.value || isCheckedInToday.value) return
  loading.value = true
  try {
    const res = await recordGrowthAction({
      actionType: 'LOGIN',
      bizId: `LOGIN_${todayKey.value}`
    })
    await refresh()
    const bonus = res?.loginStreak?.streakBonusExp
    const msg = bonus ? `签到成功！连续签到奖励 +${bonus} 经验` : '签到成功'
    showToast({ type: 'success', message: msg })
  } catch (error) {
    await refresh().catch(() => null)
    showToast({
      type: 'success',
      message: isCheckedInToday.value ? '今日已签到' : (error?.message || '签到失败')
    })
  } finally {
    loading.value = false
  }
}

async function handleClaim() {
  const t = claimableDailyLoginTask.value
  if (!t || claiming.value) return
  claiming.value = true
  try {
    const res = await claimDailyTask(String(t.code || ''))
    await refresh()
    const bonus = Number(res?.bonusExp ?? 0)
    const msg = bonus > 0 ? `已领取，额外奖励 +${bonus} 经验` : '已领取'
    showToast({ type: 'success', message: msg })
  } catch (error) {
    showToast({ type: 'fail', message: error?.response?.data?.message || error?.message || '领取失败' })
  } finally {
    claiming.value = false
  }
}

onMounted(async () => {
  try {
    await refresh()
  } catch {
    overview.value = null
  }
})
</script>

<style scoped>
.ck-body {
  max-width: 720px;
  margin: 0 auto;
  padding: 16px 14px calc(80px + env(safe-area-inset-bottom));
  display: grid;
  gap: 12px;
}

.ck-hero {
  border-radius: 18px;
  border: 1px solid var(--lc-blue-border);
  background:
    radial-gradient(circle at 12% 0%, rgba(191, 219, 254, 0.7), transparent 40%),
    radial-gradient(circle at 92% 22%, rgba(236, 72, 153, 0.16), transparent 38%),
    var(--lc-surface);
  box-shadow: 0 14px 34px rgba(37, 99, 235, 0.08);
  padding: 16px;
}

.ck-hero-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.ck-kicker {
  margin: 0;
  font-size: 11px;
  letter-spacing: 0.18em;
  color: var(--lc-subtle);
  font-weight: 800;
}
.ck-date {
  margin: 6px 0 0;
  font-size: 28px;
  font-weight: 950;
  color: var(--lc-text);
}

.ck-status {
  flex: 0 0 auto;
  border-radius: 999px;
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 800;
  color: var(--lc-indigo);
  background: var(--lc-indigo-light);
  border: 1px solid var(--lc-blue-border);
}
.ck-status.ok {
  color: var(--lc-emerald);
  background: var(--lc-emerald-light);
  border-color: var(--lc-green-light);
}

.ck-actions {
  margin-top: 12px;
  display: grid;
  grid-template-columns: 1fr;
  gap: 10px;
}

.ck-btn {
  height: 44px;
  border-radius: 12px;
  border: 1px solid var(--lc-border);
  background: var(--lc-surface);
  color: var(--lc-text);
  font-size: 14px;
  font-weight: 800;
  cursor: pointer;
}
.ck-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.ck-btn-primary {
  border-color: var(--lc-blue);
  background: linear-gradient(135deg, var(--lc-blue), var(--lc-indigo));
  color: var(--lc-surface);
  box-shadow: 0 14px 28px rgba(37, 99, 235, 0.2);
}

.ck-tip {
  margin: 10px 0 0;
  font-size: 12px;
  color: var(--lc-subtle);
  line-height: 1.5;
}

.ck-card {
  border-radius: 16px;
  border: 1px solid var(--lc-soft-alt);
  background: var(--lc-surface);
  box-shadow: 0 3px 12px rgba(15, 23, 42, 0.04);
  padding: 16px;
}
.ck-card-title {
  font-size: 15px;
  font-weight: 900;
  margin-bottom: 12px;
}
.ck-muted {
  margin: 0;
  color: var(--lc-subtle);
  font-size: 13px;
}

.ck-growth {
  display: grid;
  gap: 10px;
}
.ck-growth-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}
.ck-growth-row span {
  color: var(--lc-subtle);
  font-size: 12px;
  font-weight: 700;
}
.ck-growth-row strong {
  color: var(--lc-text);
  font-size: 13px;
  font-weight: 900;
}

.ck-progress {
  height: 8px;
  border-radius: 999px;
  background: var(--lc-soft-alt);
  overflow: hidden;
}
.ck-progress-fill {
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, var(--lc-violet), var(--lc-indigo));
  transition: width 0.45s ease;
}

.ck-task {
  display: grid;
  gap: 10px;
}
.ck-task-main {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
}
.ck-task-name {
  font-size: 14px;
  font-weight: 800;
  color: var(--lc-text);
}
.ck-task-sub {
  margin-top: 3px;
  font-size: 12px;
  color: var(--lc-subtle);
  font-weight: 700;
}
.ck-task-sep {
  margin: 0 6px;
  opacity: 0.5;
}
.ck-pill {
  flex: 0 0 auto;
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 11px;
  font-weight: 800;
  color: var(--lc-indigo);
  background: var(--lc-indigo-light);
  border: 1px solid var(--lc-blue-border);
}
.ck-pill.ok {
  color: var(--lc-emerald);
  background: var(--lc-emerald-light);
  border-color: var(--lc-green-light);
}
.ck-task-progress {
  height: 6px;
  border-radius: 999px;
  background: var(--lc-soft-alt);
  overflow: hidden;
}
.ck-task-progress-fill {
  height: 100%;
  border-radius: inherit;
  background: var(--lc-blue);
  transition: width 0.45s ease;
}

.ck-streak {
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px dashed var(--lc-soft-alt);
}

.ck-streak-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  padding: 4px 0;
}

.ck-streak-row strong {
  color: var(--lc-indigo);
}

.ck-streak-hint {
  margin: 8px 0 0;
  font-size: 12px;
  color: var(--lc-subtle);
}

.ck-more {
  margin: 0;
  text-align: center;
  font-size: 13px;
}

.ck-more a {
  color: var(--lc-blue);
  font-weight: 700;
  text-decoration: none;
}
</style>
