<template>
  <section class="season-panel" :class="{ compact }">
    <div class="season-head">
      <h2>{{ title }}</h2>
      <p v-if="seasonRank?.seasonTitle" class="season-sub">
        {{ seasonRank.seasonTitle }}
        <template v-if="seasonRank.startDate && seasonRank.endDate">
          · {{ seasonRank.startDate }} 至 {{ seasonRank.endDate }}
        </template>
      </p>
    </div>

    <div class="season-stats">
      <div class="season-stat">
        <span class="val">{{ seasonRank?.score ?? 0 }}</span>
        <span class="lbl">赛季积分</span>
      </div>
      <div class="season-stat">
        <span class="val">{{ seasonRank?.rank ?? '—' }}</span>
        <span class="lbl">当前排名</span>
      </div>
      <div v-if="showBreakdown" class="season-stat">
        <span class="val">{{ seasonRank?.checkinCount ?? 0 }}</span>
        <span class="lbl">打卡次数</span>
      </div>
      <div v-if="showBreakdown" class="season-stat">
        <span class="val">{{ seasonRank?.taskCount ?? 0 }}</span>
        <span class="lbl">任务次数</span>
      </div>
      <div v-if="showBreakdown" class="season-stat">
        <span class="val">{{ seasonRank?.activityCount ?? 0 }}</span>
        <span class="lbl">活动次数</span>
      </div>
    </div>

    <div v-if="rules.length" class="season-rules">
      <h3>积分规则</h3>
      <ul>
        <li v-for="rule in rules" :key="rule.label">
          <strong>{{ rule.label }} ×{{ rule.points }}</strong>
          <span>{{ rule.description }}</span>
        </li>
      </ul>
    </div>

    <div v-if="seasonTop.length" class="season-top">
      <h3>季度榜 TOP</h3>
      <ol>
        <li v-for="item in seasonTop" :key="item.groupId">
          <span class="rank">#{{ item.rank }}</span>
          <span class="name">{{ item.groupName }}</span>
          <span class="score">{{ item.score }}</span>
        </li>
      </ol>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  seasonRank: { type: Object, default: null },
  seasonTop: { type: Array, default: () => [] },
  title: { type: String, default: '赛季排行' },
  compact: { type: Boolean, default: false },
  showBreakdown: { type: Boolean, default: true }
})

const rules = computed(() => {
  const fromRank = props.seasonRank?.scoringRules
  if (Array.isArray(fromRank) && fromRank.length) return fromRank
  return [
    { label: '打卡', points: 2, description: '成员完成每日打卡' },
    { label: '任务', points: 5, description: '领取团体每日任务奖励' },
    { label: '活动', points: 10, description: '成员报名团体活动' }
  ]
})
</script>

<style scoped>
.season-panel {
  display: flex;
  flex-direction: column;
  gap: var(--lc-space-3);
}

.season-head h2 {
  margin: 0;
  font-size: 1.05rem;
}

.season-sub {
  margin: var(--lc-space-1) 0 0;
  font-size: 0.85rem;
  color: var(--lc-text-muted);
}

.season-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(72px, 1fr));
  gap: var(--lc-space-2);
}

.season-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--lc-space-2);
  border-radius: var(--lc-radius-md);
  background: var(--lc-surface-muted);
}

.season-stat .val {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--lc-blue);
}

.season-stat .lbl {
  font-size: 0.75rem;
  color: var(--lc-text-muted);
}

.season-rules h3,
.season-top h3 {
  margin: 0 0 var(--lc-space-2);
  font-size: 0.9rem;
}

.season-rules ul {
  margin: 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: var(--lc-space-2);
}

.season-rules li {
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-size: 0.85rem;
}

.season-rules strong {
  color: var(--lc-text);
}

.season-rules span {
  color: var(--lc-text-muted);
}

.season-top ol {
  margin: 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: var(--lc-space-2);
}

.season-top li {
  display: grid;
  grid-template-columns: 2rem 1fr auto;
  gap: var(--lc-space-2);
  align-items: center;
  font-size: 0.85rem;
}

.season-top .rank {
  color: var(--lc-text-muted);
}

.season-top .score {
  font-weight: 600;
  color: var(--lc-pink);
}

.season-panel.compact .season-rules,
.season-panel.compact .season-stats .season-stat:nth-child(n+3) {
  display: none;
}
</style>
