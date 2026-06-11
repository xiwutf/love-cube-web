<template>
  <section class="overview-panel">
    <div class="panel-head">
      <h2>数据概览</h2>
      <button
        type="button"
        class="btn secondary sm"
        :disabled="loading"
        @click="$emit('refresh')"
      >
        {{ loading ? '刷新中…' : '刷新' }}
      </button>
    </div>

    <div v-if="loading && !stats" class="inline-state">加载数据中…</div>
    <div v-else-if="error" class="inline-state err">{{ error }}</div>
    <template v-else-if="stats">
      <!-- 核心指标 -->
      <div class="stat-grid">
        <article class="stat-card">
          <strong>{{ mg.totalMembers ?? 0 }}</strong>
          <span>总成员</span>
        </article>
        <article class="stat-card">
          <strong>+{{ mg.newMembers7d ?? 0 }}</strong>
          <span>7 日新增</span>
        </article>
        <article class="stat-card accent">
          <strong>{{ act.activeMembers7d ?? 0 }}</strong>
          <span>7 日活跃</span>
        </article>
        <article class="stat-card accent">
          <strong>{{ act.activeRate7d ?? 0 }}%</strong>
          <span>活跃率</span>
        </article>
        <article class="stat-card warn">
          <strong>{{ mg.pendingMembers ?? 0 }}</strong>
          <span>待审核</span>
        </article>
      </div>

      <!-- 打卡营表现 -->
      <div class="block">
        <h3>当前打卡营</h3>
        <template v-if="camp.activeCampaignTitle">
          <p class="camp-title">{{ camp.activeCampaignTitle }}</p>
          <div class="stat-grid compact">
            <article class="stat-card">
              <strong>{{ camp.campaignParticipants ?? 0 }}</strong>
              <span>参与人数</span>
            </article>
            <article class="stat-card">
              <strong>{{ camp.campaignTodayCompleted ?? 0 }}</strong>
              <span>今日完成</span>
            </article>
            <article class="stat-card">
              <strong>{{ camp.campaignCompletionRate ?? 0 }}%</strong>
              <span>总完成率</span>
            </article>
            <article class="stat-card warn">
              <strong>{{ camp.fallenBehindCount ?? 0 }}</strong>
              <span>掉队人数</span>
            </article>
          </div>
        </template>
        <p v-else class="hint">暂无进行中的打卡营</p>
      </div>

      <!-- 内容数据 -->
      <div class="block">
        <h3>内容概况</h3>
        <div class="stat-grid compact">
          <article class="stat-card">
            <strong>{{ content.posts7d ?? 0 }}</strong>
            <span>7 日动态</span>
          </article>
          <article class="stat-card">
            <strong>{{ content.noticesCount ?? 0 }}</strong>
            <span>公告数</span>
          </article>
          <article class="stat-card">
            <strong>{{ content.activitiesCount ?? 0 }}</strong>
            <span>活动数</span>
          </article>
        </div>
      </div>

      <!-- 风险成员 -->
      <div class="block">
        <h3>风险成员</h3>

        <article class="risk-block">
          <h4>7 日未活跃 <span class="count">{{ risks.inactiveMembers7d ?? 0 }} 人</span></h4>
          <p v-if="!(risks.inactiveMembers7d > 0)" class="hint">暂无</p>
        </article>

        <article class="risk-block">
          <h4>
            加入后无活动
            <span class="count">{{ noActivityTotal }} 人</span>
          </h4>
          <ul v-if="noActivityList.length" class="member-list">
            <li v-for="m in noActivityList" :key="`na-${m.userId}`">
              <strong>{{ m.username || `成员 ${m.userId}` }}</strong>
              <span class="meta">{{ formatDate(m.joinedAt) }}</span>
            </li>
          </ul>
          <p v-else class="hint">暂无</p>
          <p v-if="noActivityTruncated" class="truncate-hint">仅展示前 20 个</p>
        </article>

        <article class="risk-block">
          <h4>
            打卡营掉队
            <span class="count">{{ fallenBehindTotal }} 人</span>
          </h4>
          <ul v-if="fallenBehindList.length" class="member-list">
            <li v-for="m in fallenBehindList" :key="`fb-${m.userId}`">
              <strong>{{ m.username || `成员 ${m.userId}` }}</strong>
              <span class="tag warn">缺 {{ m.missedCount ?? 0 }} 天</span>
            </li>
          </ul>
          <p v-else class="hint">暂无</p>
          <p v-if="fallenBehindTruncated" class="truncate-hint">仅展示前 20 个</p>
        </article>
      </div>

      <div class="quick-actions">
        <button type="button" class="btn secondary" @click="$emit('navigate', 'members')">审核成员</button>
        <button type="button" class="btn secondary" @click="$emit('navigate', 'notices')">发公告</button>
        <button type="button" class="btn secondary" @click="$emit('navigate', 'activities')">建活动</button>
        <button type="button" class="btn secondary" @click="$emit('navigate', 'camp')">打卡营</button>
      </div>
    </template>
    <p v-else class="hint">暂无统计数据</p>
  </section>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  stats: { type: Object, default: null },
  loading: { type: Boolean, default: false },
  error: { type: String, default: '' }
})

defineEmits(['refresh', 'navigate'])

const mg = computed(() => props.stats?.memberGrowth ?? {})
const act = computed(() => props.stats?.activity ?? {})
const camp = computed(() => props.stats?.campaign ?? {})
const content = computed(() => props.stats?.content ?? {})
const risks = computed(() => props.stats?.risks ?? {})

const noActivityList = computed(() => risks.value.joinedButNoActivityMembers ?? [])
const fallenBehindList = computed(() => risks.value.fallenBehindMembers ?? [])

const noActivityTotal = computed(() =>
  risks.value.joinedButNoActivityCount ?? noActivityList.value.length
)

const fallenBehindTotal = computed(() => camp.value.fallenBehindCount ?? 0)

const noActivityTruncated = computed(() =>
  noActivityTotal.value > noActivityList.value.length
)

const fallenBehindTruncated = computed(() =>
  fallenBehindTotal.value > fallenBehindList.value.length
)

function formatDate(v) {
  if (!v) return '—'
  const d = new Date(v)
  if (Number.isNaN(d.getTime())) return String(v)
  return d.toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.overview-panel {
  display: grid;
  gap: var(--lc-space-4);
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--lc-space-3);
}

.panel-head h2 {
  margin: 0;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: var(--lc-space-3);
}

.stat-grid.compact {
  grid-template-columns: repeat(auto-fill, minmax(110px, 1fr));
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
  font-size: 1.5rem;
  line-height: 1.1;
}

.stat-card span {
  color: var(--lc-muted);
  font-size: 13px;
  font-weight: 700;
}

.stat-card.warn strong {
  color: var(--lc-warning, #d97706);
}

.stat-card.accent strong {
  color: var(--lc-blue);
}

.block {
  padding-top: var(--lc-space-2);
  border-top: 1px solid var(--lc-border);
}

.block h3 {
  margin: 0 0 var(--lc-space-3);
  font-size: 1rem;
}

.hint {
  margin: 0;
  color: var(--lc-muted);
  font-size: 14px;
}

.camp-title {
  margin: 0 0 var(--lc-space-3);
  font-weight: 800;
}

.risk-block {
  margin-bottom: var(--lc-space-4);
}

.risk-block h4 {
  margin: 0 0 var(--lc-space-2);
  font-size: 14px;
  font-weight: 800;
}

.risk-block .count {
  color: var(--lc-muted);
  font-weight: 700;
}

.member-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: var(--lc-space-2);
}

.member-list li {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--lc-space-2);
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid var(--lc-border);
  background: var(--lc-bg);
}

.meta {
  font-size: 12px;
  color: var(--lc-muted);
  font-weight: 700;
}

.tag {
  font-size: 12px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 999px;
  white-space: nowrap;
}

.tag.warn {
  background: color-mix(in srgb, #d97706 15%, white);
  color: #b45309;
}

.truncate-hint {
  margin: var(--lc-space-2) 0 0;
  font-size: 12px;
  color: var(--lc-muted);
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: var(--lc-space-2);
  padding-top: var(--lc-space-2);
}

.btn {
  border: 1px solid var(--lc-border);
  border-radius: 10px;
  padding: 8px 14px;
  font-weight: 700;
  cursor: pointer;
  background: white;
}

.btn.secondary {
  background: var(--lc-bg);
}

.btn.sm {
  padding: 6px 10px;
  font-size: 13px;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.inline-state {
  color: var(--lc-muted);
  font-weight: 700;
}

.inline-state.err {
  color: #b91c1c;
}
</style>
