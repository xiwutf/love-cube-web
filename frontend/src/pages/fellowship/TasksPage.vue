<template>
  <div class="tasks-page">
    <NavBar title="成长任务" />

    <div v-if="loading" class="page-loading">
      <van-loading color="#ff5f84" />
    </div>

    <template v-else>
      <section v-if="newcomerPack.eligible && pendingNewcomerCount > 0" class="banner banner-newcomer">
        <div>
          <p class="banner-title">新人 7 日任务 · 第 {{ newcomerPack.currentDay }} 天</p>
          <p class="banner-sub">还有 {{ pendingNewcomerCount }} 项待完成，经验奖励需手动领取</p>
        </div>
        <van-icon name="arrow" size="16" color="#ff5f84" />
      </section>

      <section class="summary-card">
        <div class="summary-level">Lv.{{ growthLevel }}</div>
        <div class="summary-body">
          <p class="summary-title">{{ growthName }}</p>
          <p class="summary-sub">今日任务 {{ completedDailyCount }}/{{ dailyTasks.length }}</p>
        </div>
      </section>

      <section v-if="newcomerTasks.length" class="block">
        <h2 class="block-title">新人任务</h2>
        <div v-for="task in newcomerTasks" :key="task.code" class="task-row" :class="{ done: task.completed, locked: !task.unlocked }">
          <div class="task-main">
            <p class="task-name">{{ task.title }}</p>
            <p class="task-meta">+{{ task.exp }} 经验</p>
          </div>
          <van-button
            v-if="task.completed && !task.claimed"
            size="small"
            round
            color="#ff5f84"
            :loading="claimingNewcomer === task.code"
            @click="claimNewcomer(task.code)"
          >
            领取
          </van-button>
          <span v-else-if="task.claimed" class="task-tag done">已领取</span>
          <span v-else-if="!task.unlocked" class="task-tag">第 {{ task.unlockDay }} 天</span>
          <van-button v-else size="small" round plain color="#ff5f84" @click="goTask(newcomerTaskRoute(task.code))">
            去完成
          </van-button>
        </div>
      </section>

      <section class="block">
        <h2 class="block-title">今日任务</h2>
        <div v-for="task in dailyTasks" :key="task.code" class="task-row" :class="{ done: task.completed }">
          <div class="task-main">
            <p class="task-name">{{ task.title }}</p>
            <p class="task-meta">+{{ task.exp }} 经验 · {{ task.completed ? '已完成' : `${task.current}/${task.total}` }}</p>
            <div v-if="!task.completed" class="progress-track">
              <div class="progress-fill" :style="{ width: `${Math.min(100, (task.current / Math.max(1, task.total)) * 100)}%` }" />
            </div>
          </div>
          <van-button
            v-if="task.completed && !task.claimed"
            size="small"
            round
            color="#ff5f84"
            :loading="claimingDaily === task.code"
            @click="claimDaily(task.code)"
          >
            领取
          </van-button>
          <span v-else-if="task.claimed" class="task-tag done">已领取</span>
          <van-button v-else-if="!task.completed" size="small" round plain color="#ff5f84" @click="goTask(task.to)">
            去完成
          </van-button>
          <span v-else class="task-tag done">已完成</span>
        </div>
      </section>

      <section v-if="weeklyTasks.length" class="block">
        <h2 class="block-title">本周挑战</h2>
        <div v-for="task in weeklyTasks" :key="task.code" class="task-row" :class="{ done: task.completed }">
          <div class="task-main">
            <p class="task-name">{{ task.title }}</p>
            <p class="task-meta">+{{ task.exp }} 经验 · {{ task.current }}/{{ task.total }}</p>
          </div>
          <van-button
            v-if="task.completed && !task.claimed"
            size="small"
            round
            color="#ff5f84"
            :loading="claimingWeekly === task.code"
            @click="claimWeekly(task.code)"
          >
            领取
          </van-button>
          <span v-else-if="task.claimed" class="task-tag done">已领取</span>
          <van-button v-else size="small" round plain color="#ff5f84" @click="goTask(weeklyTaskRoute(task.code))">
            去完成
          </van-button>
        </div>
      </section>

      <section class="play-links">
        <button type="button" class="play-link" @click="goTask('/m/platform/checkin')">平台签到</button>
        <button type="button" class="play-link" @click="goTask('/m/platform/positive-share')">每日心声</button>
        <button type="button" class="play-link" @click="goTask(fellowshipPath('/match'))">去滑卡</button>
      </section>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import NavBar from '@/components/NavBar.vue'
import {
  claimDailyTask,
  claimNewcomerTask,
  claimWeeklyTask,
  getMyGrowth
} from '@/api/growth.js'
import { useFellowshipTaskRoutes } from '@/composables/useFellowshipTaskRoutes.js'
import { useFellowshipNavBase } from '@/composables/useFellowshipNavBase.js'

const router = useRouter()
const { fellowshipPath } = useFellowshipNavBase()
const { dailyTaskRoute, weeklyTaskRoute, newcomerTaskRoute } = useFellowshipTaskRoutes()

const loading = ref(true)
const growthLevel = ref(1)
const growthName = ref('新手用户')
const dailyTasks = ref([])
const weeklyTasks = ref([])
const newcomerTasks = ref([])
const newcomerPack = ref({ eligible: false, currentDay: 0 })
const claimingDaily = ref('')
const claimingWeekly = ref('')
const claimingNewcomer = ref('')

const completedDailyCount = computed(() => dailyTasks.value.filter((t) => t.completed).length)
const pendingNewcomerCount = computed(() =>
  newcomerTasks.value.filter((t) => t.unlocked && (!t.completed || !t.claimed)).length
)

function mapDaily(rows) {
  return Array.isArray(rows)
    ? rows.map((item) => ({
        code: item.code,
        title: item.name || item.code,
        exp: Number(item.rewardExp ?? 0),
        current: Number(item.progress ?? 0),
        total: Number(item.targetCount ?? 1),
        completed: Boolean(item.completed),
        claimed: Boolean(item.claimed),
        to: dailyTaskRoute(item.code)
      }))
    : []
}

function mapWeekly(rows) {
  return Array.isArray(rows)
    ? rows.map((item) => ({
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

function mapNewcomer(rows) {
  return Array.isArray(rows)
    ? rows.map((item) => ({
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

async function loadGrowth() {
  loading.value = true
  try {
    const data = await getMyGrowth()
    growthLevel.value = Number(data?.level ?? 1)
    growthName.value = data?.title || '新手用户'
    dailyTasks.value = mapDaily(data?.dailyTasks)
    weeklyTasks.value = mapWeekly(data?.weeklyTasks)
    const pack = data?.newcomerPack || {}
    newcomerPack.value = {
      eligible: Boolean(pack.eligible),
      currentDay: Number(pack.currentDay ?? 0)
    }
    newcomerTasks.value = mapNewcomer(pack.tasks)
  } catch (e) {
    showToast({ type: 'fail', message: e?.message || '加载失败' })
  } finally {
    loading.value = false
  }
}

function goTask(path) {
  if (!path) return
  router.push(path)
}

async function claimDaily(code) {
  if (!code || claimingDaily.value) return
  claimingDaily.value = code
  try {
    const res = await claimDailyTask(code)
    if (res?.claimed) {
      showToast({ type: 'success', message: `已领取 +${Number(res.rewardExp ?? 0)} 经验` })
      await loadGrowth()
    }
  } catch (e) {
    showToast({ type: 'fail', message: e?.message || '领取失败' })
  } finally {
    claimingDaily.value = ''
  }
}

async function claimWeekly(code) {
  if (!code || claimingWeekly.value) return
  claimingWeekly.value = code
  try {
    const res = await claimWeeklyTask(code)
    if (res?.claimed) {
      showToast({ type: 'success', message: `已领取 +${Number(res.rewardExp ?? 0)} 经验` })
      await loadGrowth()
    }
  } catch (e) {
    showToast({ type: 'fail', message: e?.message || '领取失败' })
  } finally {
    claimingWeekly.value = ''
  }
}

async function claimNewcomer(code) {
  if (!code || claimingNewcomer.value) return
  claimingNewcomer.value = code
  try {
    const res = await claimNewcomerTask(code)
    if (res?.claimed) {
      showToast({ type: 'success', message: `已领取 +${Number(res.rewardExp ?? 0)} 经验` })
      await loadGrowth()
    }
  } catch (e) {
    showToast({ type: 'fail', message: e?.message || '领取失败' })
  } finally {
    claimingNewcomer.value = ''
  }
}

onMounted(loadGrowth)
</script>

<style scoped>
.tasks-page {
  min-height: 100vh;
  background: #f4f6fb;
  padding-bottom: 24px;
}

.page-loading {
  display: flex;
  justify-content: center;
  padding: 48px 0;
}

.banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin: 12px 12px 0;
  padding: 14px 16px;
  border-radius: 14px;
  background: linear-gradient(135deg, #fff0f4, #f5f3ff);
  border: 1px solid #ffd8e4;
}

.banner-title {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: #1a2236;
}

.banner-sub {
  margin: 4px 0 0;
  font-size: 12px;
  color: #64748b;
}

.summary-card {
  display: flex;
  align-items: center;
  gap: 14px;
  margin: 12px;
  padding: 16px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(15, 23, 42, 0.05);
}

.summary-level {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: linear-gradient(135deg, #ff5f84, #ff3366);
  color: #fff;
  font-size: 14px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
}

.summary-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #1a2236;
}

.summary-sub {
  margin: 4px 0 0;
  font-size: 12px;
  color: #8898aa;
}

.block {
  margin: 12px;
  padding: 14px 16px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(15, 23, 42, 0.05);
}

.block-title {
  margin: 0 0 12px;
  font-size: 15px;
  font-weight: 700;
  color: #1a2236;
}

.task-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f1f5f9;
}

.task-row:last-child {
  border-bottom: none;
}

.task-row.locked {
  opacity: 0.55;
}

.task-main {
  flex: 1;
  min-width: 0;
}

.task-name {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #1a2236;
}

.task-meta {
  margin: 4px 0 0;
  font-size: 12px;
  color: #8898aa;
}

.progress-track {
  margin-top: 8px;
  height: 4px;
  border-radius: 999px;
  background: #eef2f7;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #ff5f84, #ff9bb5);
  border-radius: inherit;
}

.task-tag {
  font-size: 12px;
  color: #8898aa;
  white-space: nowrap;
}

.task-tag.done {
  color: #10b981;
}

.play-links {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 12px;
}

.play-link {
  flex: 1;
  min-width: 96px;
  padding: 10px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: #fff;
  color: #475569;
  font-size: 13px;
  font-weight: 600;
}
</style>
