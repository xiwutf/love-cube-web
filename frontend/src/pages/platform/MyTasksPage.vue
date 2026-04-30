<template>
  <section class="sp-page">
    <header class="sp-head">
      <button type="button" class="sp-back" aria-label="返回" @click="$router.back()">‹</button>
      <h1 class="sp-title">今日任务</h1>
    </header>

    <div class="sp-body">
      <p v-if="loading" class="sp-status">加载中...</p>
      <template v-else>
        <!-- 进度汇总 -->
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

        <!-- 任务列表 -->
        <p v-if="!tasks.length" class="sp-status sp-empty">今日任务加载失败，请稍后再试。</p>
        <div v-else class="task-list">
          <div v-for="task in tasks" :key="task.title" class="sp-card task-row" :class="{ done: task.done }">
            <div class="task-check">{{ task.done ? '✓' : '' }}</div>
            <div class="task-body">
              <div class="task-name">{{ task.title }}</div>
              <div class="task-exp">+{{ task.exp }} 经验</div>
              <div v-if="!task.done" class="task-progress-bar">
                <div class="task-progress-fill"
                     :style="{ width: `${Math.min(100, (task.current / Math.max(1, task.total)) * 100)}%` }"></div>
              </div>
              <div class="task-sub">{{ task.done ? '已完成' : `${task.current}/${task.total}` }}</div>
            </div>
            <router-link :to="task.to" class="task-btn" :class="{ done: task.done }">
              {{ task.done ? '已完成' : '去完成' }}
            </router-link>
          </div>
        </div>

        <!-- 经验来源说明 -->
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
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getMyGrowth } from '@/api/growth.js'

const loading = ref(false)
const growthLevel = ref(1)
const growthName = ref('新手用户')
const tasks = ref([])

const codeToRoute = {
  DAILY_LOGIN: '/me',
  DAILY_POST: '/platform/positive-share',
  DAILY_VIEW: '/articles',
  DAILY_LIKE: '/platform/positive-share'
}

const completedCount = computed(() => tasks.value.filter(t => t.done).length)

const expSources = [
  { icon: '📅', label: '每日登录', exp: 2 },
  { icon: '📝', label: '发布动态', exp: 10 },
  { icon: '👁', label: '浏览内容', exp: 1 },
  { icon: '👍', label: '点赞内容', exp: 2 },
  { icon: '👤', label: '完善资料', exp: 5 }
]

const mockTasks = [
  { title: '每日签到', exp: 2, current: 0, total: 1, done: false, to: '/me' },
  { title: '完善资料', exp: 5, current: 0, total: 1, done: false, to: { path: '/me', query: { panel: 'edit' } } },
  { title: '发布动态', exp: 10, current: 0, total: 1, done: false, to: '/platform/positive-share' },
  { title: '浏览内容', exp: 1, current: 0, total: 3, done: false, to: '/articles' }
]

onMounted(async () => {
  loading.value = true
  try {
    const res = await getMyGrowth()
    growthLevel.value = Number(res?.level ?? 1)
    growthName.value = res?.title || '新手用户'
    const rows = res?.dailyTasks
    if (Array.isArray(rows) && rows.length) {
      tasks.value = rows.map(item => ({
        title: item.name || item.code,
        exp: Number(item.rewardExp ?? 0),
        current: Number(item.progress ?? 0),
        total: Number(item.targetCount ?? 1),
        done: Boolean(item.completed),
        to: codeToRoute[item.code] || '/me'
      }))
    } else {
      tasks.value = mockTasks
    }
  } catch {
    tasks.value = mockTasks
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.sp-page { min-height: 100vh; background: #f6f7fb; color: #0f172a; }

.sp-head {
  position: sticky; top: 0; z-index: 10;
  display: flex; align-items: center;
  background: #fff; border-bottom: 1px solid #eef0f4;
}
.sp-back {
  width: 48px; height: 52px; flex: 0 0 auto;
  display: grid; place-items: center;
  border: 0; background: none; font-size: 22px; color: #4f46e5; cursor: pointer;
}
.sp-title { flex: 1; margin: 0; font-size: 17px; font-weight: 800; }

.sp-body {
  padding: 16px 14px calc(80px + env(safe-area-inset-bottom));
  max-width: 680px; margin: 0 auto;
}
.sp-status { text-align: center; padding: 40px 0; color: #94a3b8; font-size: 14px; }
.sp-empty::before { display: block; font-size: 32px; margin-bottom: 10px; content: "✅"; }

.sp-card {
  background: #fff; border: 1px solid #eef0f4; border-radius: 16px;
  box-shadow: 0 3px 12px rgba(15,23,42,0.04); margin-bottom: 12px; padding: 16px;
}
.sp-card-title { font-size: 15px; font-weight: 800; margin-bottom: 12px; color: #0f172a; }

.task-summary { padding: 16px; }
.ts-level { display: flex; align-items: center; gap: 14px; margin-bottom: 14px; }
.ts-lv-badge {
  width: 52px; height: 52px; flex: 0 0 auto;
  display: grid; place-items: center;
  background: linear-gradient(135deg, #7c6aff, #4f46e5);
  clip-path: polygon(25% 5%, 75% 5%, 100% 50%, 75% 95%, 25% 95%, 0 50%);
  color: #fff; font-size: 13px; font-weight: 900;
}
.ts-lv-name { font-size: 15px; font-weight: 800; }
.ts-lv-hint { font-size: 12px; color: #94a3b8; margin-top: 3px; }
.ts-progress-bar { height: 8px; border-radius: 999px; background: #edf0f5; overflow: hidden; }
.ts-progress-fill { height: 100%; border-radius: inherit; background: linear-gradient(90deg, #7c6aff, #4f46e5); transition: width 0.5s; }

.task-list { display: flex; flex-direction: column; gap: 0; }
.task-row {
  display: flex; align-items: center; gap: 12px; padding: 14px 16px;
  border-radius: 0; border-left: 0; border-right: 0; box-shadow: none;
}
.task-row:first-child { border-radius: 16px 16px 0 0; }
.task-row:last-child { border-radius: 0 0 16px 16px; border-bottom: 1px solid #eef0f4; }
.task-row:not(:last-child) { border-bottom: 1px solid #f1f5f9; }
.task-row.done { background: #f0fdf4; }

.task-check {
  width: 28px; height: 28px; flex: 0 0 auto;
  display: grid; place-items: center; border-radius: 50%;
  border: 2px solid #dfe4ee;
  font-size: 14px; font-weight: 800; color: transparent;
}
.task-row.done .task-check { background: #059669; border-color: #059669; color: #fff; }

.task-body { flex: 1; min-width: 0; }
.task-name { font-size: 14px; font-weight: 600; color: #0f172a; }
.task-exp { font-size: 11px; color: #94a3b8; margin-top: 2px; }
.task-progress-bar { height: 4px; border-radius: 999px; background: #edf0f5; overflow: hidden; margin: 6px 0 4px; }
.task-progress-fill { height: 100%; border-radius: inherit; background: #6d5dfb; }
.task-sub { font-size: 11px; color: #94a3b8; }

.task-btn {
  flex: 0 0 auto; border: 1px solid #cfd4ff; border-radius: 999px;
  background: #fff; color: #4f46e5;
  font-size: 12px; font-weight: 700; padding: 6px 14px; text-decoration: none;
}
.task-btn.done { border-color: #d1fae5; color: #059669; background: #f0fdf4; }

.exp-row {
  display: flex; align-items: center; gap: 10px; padding: 9px 0;
  border-bottom: 1px solid #f1f5f9;
}
.exp-row:last-child { border-bottom: 0; padding-bottom: 0; }
.exp-icon { font-size: 16px; width: 24px; text-align: center; flex: 0 0 auto; }
.exp-label { flex: 1; font-size: 13px; color: #334155; }
.exp-val { font-size: 12px; color: #6d5dfb; font-weight: 700; }

@media (max-width: 767px) {
  :global(.platform-header), :global(.platform-footer), :global(.co-creation-toolbar) {
    display: none !important;
  }
}
@media (min-width: 768px) {
  .sp-body { padding-top: 24px; padding-bottom: 48px; }
}
</style>
