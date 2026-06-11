<template>
  <section v-if="visible && (loading || error || camp)" class="camp-member-panel platform-card">
    <div class="camp-head">
      <h3>打卡营</h3>
      <span class="camp-status" :class="camp?.status">{{ statusLabel }}</span>
    </div>

    <div v-if="loading" class="camp-state">加载中…</div>
    <div v-else-if="error" class="camp-state err">{{ error }}</div>

    <template v-else-if="camp">
      <p class="camp-title">{{ camp.title }}</p>
      <p v-if="camp.description" class="camp-desc">{{ camp.description }}</p>
      <p class="camp-meta">
        第 {{ camp.currentDayNumber || 0 }} / {{ camp.durationDays }} 天
        · 我已完成 {{ camp.myCompletedDays || 0 }} 天
        <template v-if="camp.allowMakeup"> · 可补最近 {{ camp.makeupDaysLimit }} 天</template>
      </p>

      <div v-if="camp.todayTask" class="today-task">
        <strong>今日任务：{{ camp.todayTask.taskTitle }}</strong>
        <p>{{ camp.todayTask.taskDescription || '按任务说明完成今日打卡' }}</p>
        <button
          type="button"
          class="btn primary"
          :disabled="completing || camp.todayTask.completedByMe || !camp.todayTask.canComplete"
          @click="completeDay(camp.currentDayNumber)"
        >
          {{
            camp.todayTask.completedByMe
              ? '今日已完成'
              : completing
                ? '提交中…'
                : '完成今日打卡'
          }}
        </button>
      </div>
      <p v-else-if="camp.status === 'scheduled'" class="camp-hint">打卡营将于 {{ camp.startDate }} 开始</p>
      <p v-else class="camp-hint">今日暂无任务</p>

      <div v-if="dayStates.length" class="day-grid">
        <button
          v-for="day in dayStates"
          :key="day.dayNumber"
          type="button"
          class="day-chip"
          :class="{
            done: day.completedByMe,
            makeup: day.canComplete && day.makeup,
            today: day.dayNumber === camp.currentDayNumber,
            disabled: !day.canComplete && !day.completedByMe
          }"
          :disabled="completing || day.completedByMe || !day.canComplete"
          :title="day.taskTitle"
          @click="completeDay(day.dayNumber)"
        >
          <span class="dn">D{{ day.dayNumber }}</span>
          <span class="state">{{ dayLabel(day) }}</span>
        </button>
      </div>
    </template>
  </section>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { completeSpaceCampaignDay, fetchActiveSpaceCampaign } from '@/api/spaceCampaign.js'
import { isLegacyPlatformGroupId } from '@/api/groups.js'
import { useUserStore } from '@/stores/user.js'

const props = defineProps({
  groupId: { type: [String, Number], required: true },
  isMember: { type: Boolean, default: false }
})

const userStore = useUserStore()
const loading = ref(false)
const error = ref('')
const camp = ref(null)
const completing = ref(false)

const visible = computed(() =>
  isLegacyPlatformGroupId(props.groupId) && userStore.isLoggedIn && props.isMember
)

const dayStates = computed(() => camp.value?.dayStates || camp.value?.days || [])

const statusLabel = computed(() => {
  const s = camp.value?.status
  if (s === 'active') return '进行中'
  if (s === 'scheduled') return '未开始'
  if (s === 'ended') return '已结束'
  return s || ''
})

function dayLabel(day) {
  if (day.completedByMe) return '已完成'
  if (day.canComplete && day.makeup) return '可补卡'
  if (day.canComplete) return '可打卡'
  return '—'
}

async function load() {
  if (!visible.value) return
  loading.value = true
  error.value = ''
  try {
    camp.value = await fetchActiveSpaceCampaign(props.groupId)
  } catch (err) {
    if (err?.response?.status === 404 || /没有进行中/.test(err?.message || '')) {
      camp.value = null
      error.value = ''
    } else {
      error.value = err?.message || '加载打卡营失败'
    }
  } finally {
    loading.value = false
  }
}

async function completeDay(dayNumber) {
  if (!camp.value?.id || !dayNumber) return
  completing.value = true
  error.value = ''
  try {
    await completeSpaceCampaignDay(props.groupId, camp.value.id, dayNumber)
    await load()
  } catch (err) {
    error.value = err?.message || '打卡失败'
  } finally {
    completing.value = false
  }
}

watch(() => [props.groupId, props.isMember], load)
onMounted(load)
</script>

<style scoped>
.camp-member-panel {
  margin-bottom: var(--lc-space-4);
  padding: var(--lc-space-4);
}

.camp-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--lc-space-2);
  margin-bottom: var(--lc-space-2);
}

.camp-head h3 {
  margin: 0;
  font-size: 18px;
}

.camp-status {
  font-size: 12px;
  font-weight: 800;
  padding: 2px 8px;
  border-radius: 999px;
  background: var(--lc-bg);
  color: var(--lc-muted);
}

.camp-status.active {
  color: var(--lc-green);
}

.camp-title {
  margin: 0;
  font-weight: 900;
  color: var(--lc-text);
}

.camp-desc,
.camp-meta,
.camp-hint {
  margin: var(--lc-space-2) 0 0;
  color: var(--lc-muted);
  font-weight: 600;
  line-height: 1.5;
}

.today-task {
  margin-top: var(--lc-space-3);
  padding: var(--lc-space-3);
  border: 1px solid var(--lc-border);
  border-radius: 12px;
  background: var(--lc-bg);
}

.today-task p {
  margin: var(--lc-space-2) 0;
  color: var(--lc-text);
}

.btn.primary {
  border: none;
  border-radius: 10px;
  padding: 10px 14px;
  background: var(--lc-blue);
  color: #fff;
  font-weight: 800;
  cursor: pointer;
}

.btn.primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.day-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(72px, 1fr));
  gap: 8px;
  margin-top: var(--lc-space-3);
}

.day-chip {
  display: grid;
  gap: 2px;
  padding: 8px 6px;
  border-radius: 10px;
  border: 1px solid var(--lc-border);
  background: #fff;
  cursor: pointer;
  text-align: center;
}

.day-chip .dn {
  font-weight: 900;
  font-size: 13px;
  color: var(--lc-text);
}

.day-chip .state {
  font-size: 11px;
  font-weight: 700;
  color: var(--lc-muted);
}

.day-chip.done {
  border-color: var(--lc-green);
  background: color-mix(in srgb, var(--lc-green) 10%, white);
}

.day-chip.makeup {
  border-color: var(--lc-amber, #d97706);
}

.day-chip.today {
  box-shadow: 0 0 0 2px color-mix(in srgb, var(--lc-blue) 25%, transparent);
}

.day-chip.disabled:not(.done) {
  opacity: 0.45;
  cursor: not-allowed;
}

.camp-state {
  color: var(--lc-muted);
  font-weight: 700;
}

.camp-state.err {
  color: var(--lc-red);
}
</style>
