<template>
  <section class="camp-manage operation-shell">
    <div class="section-head operation-section-head">
      <div>
        <p class="section-kicker">Campaign Ops</p>
        <h2>打卡营管理</h2>
        <p class="hint">用项目面板跟进营期状态、完成进度和掉队风险。</p>
      </div>
      <button
        v-if="!showCreateForm && !selected"
        type="button"
        class="btn primary"
        @click="openCreateForm"
      >
        新建打卡营
      </button>
    </div>

    <form v-if="showCreateForm" class="create-form" @submit.prevent="submitCreate">
      <h3>创建 7 天打卡营</h3>
      <label class="field">
        <span>标题</span>
        <input v-model.trim="form.title" type="text" maxlength="200" required>
      </label>
      <label class="field">
        <span>描述</span>
        <textarea v-model.trim="form.description" rows="3" maxlength="2000" />
      </label>
      <label class="field">
        <span>开始日期</span>
        <input v-model="form.startDate" type="date" required>
      </label>

      <div class="days-grid">
        <article v-for="(day, idx) in form.days" :key="idx" class="day-card">
          <h4>第 {{ idx + 1 }} 天</h4>
          <label class="field">
            <span>任务标题</span>
            <input v-model.trim="day.taskTitle" type="text" maxlength="200" required>
          </label>
          <label class="field">
            <span>任务说明</span>
            <textarea v-model.trim="day.taskDescription" rows="2" maxlength="1000" />
          </label>
        </article>
      </div>

      <div class="form-actions">
        <button type="submit" class="btn primary" :disabled="saving">{{ saving ? '创建中…' : '创建打卡营' }}</button>
      </div>
    </form>

    <div v-if="loading" class="inline-state">加载中…</div>
    <div v-else-if="selected" class="campaign-dashboard">
      <div class="metric-grid">
        <article class="metric-card">
          <span class="metric-label">营期状态</span>
          <strong>{{ statusText(selected.status) }}</strong>
          <p>{{ selected.startDate }} 起</p>
        </article>
        <article class="metric-card accent">
          <span class="metric-label">参与人数</span>
          <strong>{{ selected.stats?.participantCount ?? 0 }}</strong>
          <p>当前营期参与规模</p>
        </article>
        <article class="metric-card">
          <span class="metric-label">完成率</span>
          <strong>{{ selected.stats?.totalCompletionRate ?? 0 }}%</strong>
          <p>累计完成情况</p>
        </article>
        <article class="metric-card warn">
          <span class="metric-label">掉队人数</span>
          <strong>{{ fallenBehindMembers.length }}</strong>
          <p>需要运营跟进</p>
        </article>
      </div>

      <div class="campaign-layout">
        <section class="section-card campaign-current">
          <div class="section-card-head">
            <div>
              <p class="section-kicker">Current Campaign</p>
              <h3>{{ selected.title }}</h3>
              <p class="meta-line">{{ selected.startDate }} 起 · {{ statusText(selected.status) }}</p>
            </div>
            <span class="status-badge" :class="campaignStatusTone(selected.status)">{{ statusText(selected.status) }}</span>
          </div>

          <div class="completion-block">
            <div class="progress-label">
              <span>总完成率</span>
              <strong>{{ selected.stats?.totalCompletionRate ?? 0 }}%</strong>
            </div>
            <div class="completion-meter">
              <span :style="{ width: `${safeRate(selected.stats?.totalCompletionRate)}%` }"></span>
            </div>
            <p>今日完成 {{ selected.stats?.todayCompletedCount ?? 0 }} 人，未完成 {{ selected.stats?.todayNotCompletedCount ?? 0 }} 人。</p>
          </div>

          <div v-if="selected.stats?.dailyCompletions?.length" class="daily-table">
            <h4>每日完成情况</h4>
            <ul>
              <li v-for="row in selected.stats.dailyCompletions" :key="row.dayNumber">
                <span>第 {{ row.dayNumber }} 天</span>
                <strong>{{ row.completedCount }} 人完成</strong>
              </li>
            </ul>
          </div>
        </section>

        <aside class="section-card risk-card danger">
          <div class="section-card-head compact">
            <div>
              <p class="section-kicker">Risks</p>
              <h3>风险成员</h3>
            </div>
            <span class="status-badge danger">{{ fallenBehindMembers.length }} 人</span>
          </div>
          <ul v-if="fallenBehindMembers.length" class="risk-member-list">
            <li v-for="m in fallenBehindMembers" :key="m.userId">
              <strong>{{ m.username }}</strong>
              <span>缺 {{ m.missedCount }} 天 · 第 {{ (m.missedDays || []).join('、') }} 天</span>
            </li>
          </ul>
          <p v-else class="hint">暂无掉队成员，当前营期风险稳定。</p>
        </aside>
      </div>

      <section class="section-card action-section">
        <div class="section-card-head">
          <div>
            <p class="section-kicker">Actions</p>
            <h3>运营动作</h3>
          </div>
        </div>
        <div class="action-grid">
          <article class="action-card warning">
            <span class="status-badge warning">提醒</span>
            <h4>提醒未打卡成员</h4>
            <p>针对今日未完成成员发起提醒，降低当天流失。</p>
            <button type="button" class="btn secondary sm" :disabled="notifying" @click="notifyMembers">
              {{ notifying ? '通知中…' : '通知成员' }}
            </button>
          </article>
          <article class="action-card danger">
            <span class="status-badge danger">风险</span>
            <h4>查看掉队名单</h4>
            <p>优先处理连续缺卡成员，必要时单独沟通。</p>
            <button type="button" class="btn secondary sm" @click="notifyMembers">提醒掉队成员</button>
          </article>
          <article class="action-card">
            <span class="status-badge neutral">管理</span>
            <h4>结束或切换营期</h4>
            <p>当前版本保留原业务流程，可返回列表查看其他营期。</p>
            <button type="button" class="btn ghost sm" @click="selected = null">返回列表</button>
          </article>
        </div>
      </section>
    </div>

    <div v-else-if="items.length" class="campaign-list">
      <article v-for="item in items" :key="item.id" class="campaign-row section-card">
        <div>
          <span class="status-badge" :class="campaignStatusTone(item.status)">{{ statusText(item.status) }}</span>
          <strong>{{ item.title }}</strong>
          <p class="meta-line">{{ item.startDate }} · {{ statusText(item.status) }}</p>
        </div>
        <button type="button" class="btn secondary sm" @click="openDetail(item.id)">查看数据</button>
      </article>
    </div>
    <p v-else-if="!showCreateForm" class="inline-state">暂无打卡营，创建第一个吧</p>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import {
  createSpaceCampaign,
  fetchSpaceCampaignDetail,
  fetchSpaceCampaigns,
  notifySpaceCampaignMembers
} from '@/api/spaceCampaign.js'

const props = defineProps({
  groupId: { type: [String, Number], required: true }
})

const emit = defineEmits(['flash'])

const loading = ref(false)
const saving = ref(false)
const notifying = ref(false)
const items = ref([])
const selected = ref(null)
const showCreateForm = ref(false)

const emptyDay = () => ({ taskTitle: '', taskDescription: '' })
const form = reactive({
  title: '',
  description: '',
  startDate: new Date().toISOString().slice(0, 10),
  days: Array.from({ length: 7 }, emptyDay)
})

const fallenBehindMembers = computed(() => selected.value?.stats?.fallenBehindMembers ?? [])

function statusText(status) {
  if (status === 'active') return '进行中'
  if (status === 'scheduled') return '未开始'
  if (status === 'ended') return '已结束'
  return status || '—'
}

function campaignStatusTone(status) {
  if (status === 'active') return 'success'
  if (status === 'scheduled') return 'info'
  if (status === 'ended') return 'neutral'
  return 'neutral'
}

function safeRate(value) {
  const n = Number(value) || 0
  return Math.max(0, Math.min(100, n))
}

function openCreateForm() {
  showCreateForm.value = true
}

async function loadList() {
  loading.value = true
  try {
    const res = await fetchSpaceCampaigns(props.groupId)
    items.value = Array.isArray(res) ? res : res?.data ?? []
  } catch (err) {
    emit('flash', err?.message || '加载打卡营失败', 'error')
    items.value = []
  } finally {
    loading.value = false
  }
}

async function openDetail(campaignId) {
  loading.value = true
  try {
    selected.value = await fetchSpaceCampaignDetail(props.groupId, campaignId)
  } catch (err) {
    emit('flash', err?.message || '加载详情失败', 'error')
  } finally {
    loading.value = false
  }
}

async function submitCreate() {
  saving.value = true
  try {
    await createSpaceCampaign(props.groupId, {
      title: form.title,
      description: form.description,
      startDate: form.startDate,
      days: form.days.map((d) => ({
        taskTitle: d.taskTitle,
        taskDescription: d.taskDescription
      }))
    })
    form.title = ''
    form.description = ''
    form.startDate = new Date().toISOString().slice(0, 10)
    form.days = Array.from({ length: 7 }, emptyDay)
    showCreateForm.value = false
    emit('flash', '打卡营已创建')
    await loadList()
  } catch (err) {
    emit('flash', err?.message || '创建失败', 'error')
  } finally {
    saving.value = false
  }
}

async function notifyMembers() {
  if (!selected.value?.id) return
  notifying.value = true
  try {
    const res = await notifySpaceCampaignMembers(props.groupId, selected.value.id)
    emit('flash', res?.message || `已通知 ${res?.sent ?? 0} 位成员`)
  } catch (err) {
    emit('flash', err?.message || '通知失败', 'error')
  } finally {
    notifying.value = false
  }
}

onMounted(loadList)
</script>

<style scoped>
.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--lc-space-3);
  flex-wrap: wrap;
}

.section-head h2 {
  margin: 0;
  color: var(--lc-text);
}

.hint,
.meta-line {
  margin: var(--lc-space-2) 0 0;
  color: var(--lc-muted);
  font-weight: 600;
}

.create-form {
  display: grid;
  gap: var(--lc-space-3);
  padding: var(--lc-space-4);
  border: 1px solid var(--lc-border);
  border-radius: 12px;
}

.create-form h3 {
  margin: 0;
}

.days-grid {
  display: grid;
  gap: var(--lc-space-3);
}

.day-card {
  padding: var(--lc-space-3);
  border: 1px solid var(--lc-border);
  border-radius: 10px;
  background: var(--lc-bg);
}

.day-card h4 {
  margin: 0 0 var(--lc-space-2);
}

.field {
  display: grid;
  gap: 6px;
  margin-bottom: var(--lc-space-2);
}

.field span {
  font-weight: 800;
}

.field input,
.field textarea {
  width: 100%;
  border: 1px solid var(--lc-border);
  border-radius: 8px;
  padding: 8px 10px;
  font: inherit;
}

.campaign-dashboard {
  display: grid;
  gap: var(--lc-space-4);
}

.campaign-dashboard > .metric-grid {
  margin-top: 0;
  margin-bottom: var(--lc-space-4);
}

.campaign-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: var(--lc-space-2);
}

.campaign-current {
  display: grid;
  gap: var(--lc-space-3);
}

.completion-block {
  display: grid;
  gap: var(--lc-space-2);
  padding: var(--lc-space-3);
  border: 1px solid var(--lc-blue-border);
  border-radius: 10px;
  background: color-mix(in srgb, var(--lc-blue-light) 58%, var(--lc-surface));
}

.progress-label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--lc-space-2);
  color: var(--lc-muted);
  font-size: 12px;
  font-weight: 900;
}

.progress-label strong {
  color: var(--lc-blue);
  font-size: 22px;
}

.completion-meter {
  height: 8px;
  overflow: hidden;
  border-radius: 999px;
  background: color-mix(in srgb, var(--lc-blue-border) 48%, var(--lc-surface));
}

.completion-meter span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: var(--lc-blue);
}

.risk-member-list {
  list-style: none;
  display: grid;
  gap: var(--lc-space-2);
  margin: var(--lc-space-3) 0 0;
  padding: 0;
}

.risk-member-list li {
  display: grid;
  gap: 4px;
  padding: 9px 10px;
  border: 1px solid color-mix(in srgb, var(--lc-red) 22%, var(--lc-border));
  border-radius: 8px;
  background: var(--lc-surface);
}

.risk-member-list strong {
  color: var(--lc-text);
}

.risk-member-list span {
  color: var(--lc-red);
  font-size: 12px;
  font-weight: 800;
}

.daily-table ul {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: 8px;
}

.daily-table li {
  display: flex;
  justify-content: space-between;
  padding: 8px 10px;
  border: 1px solid var(--lc-border);
  border-radius: 8px;
}

.action-card h4 {
  margin: 0;
  color: var(--lc-text);
}

.campaign-list {
  display: grid;
  gap: var(--lc-space-2);
}

.campaign-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--lc-space-3);
}

.campaign-row strong {
  display: block;
  margin-top: var(--lc-space-2);
  color: var(--lc-text);
}

.btn {
  border-radius: 10px;
  padding: 10px 14px;
  font-weight: 800;
  border: 1px solid transparent;
  cursor: pointer;
}

.btn.primary {
  background: var(--lc-blue);
  color: #fff;
}

.btn.secondary {
  background: #fff;
  border-color: var(--lc-border);
}

.btn.ghost {
  background: #fff;
  border-color: var(--lc-border);
  color: var(--lc-muted);
}

.btn.sm {
  padding: 6px 10px;
  font-size: 13px;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 980px) {
  .campaign-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .campaign-row {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
