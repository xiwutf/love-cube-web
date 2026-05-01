<template>
  <section class="today-wrap platform-card">
    <header class="section-head">
      <div>
        <p class="kicker">数据区 · 参考指标</p>
        <h2>今日运营快照</h2>
        <p class="section-note">以下为汇总数字，办理具体业务请用上方功能地图或左侧菜单。</p>
      </div>
    </header>

    <div class="stats-grid">
      <article v-for="item in items" :key="item.label" class="stat-item" :class="`tone-${item.tone}`">
        <div class="stat-top">
          <span class="icon">{{ item.icon }}</span>
          <p class="label">{{ item.label }}</p>
        </div>
        <p class="value">{{ loading ? '...' : item.value }}</p>
        <p class="trend" :class="item.trend">
          <span>{{ item.trend === 'down' ? '↓' : '↑' }}</span>
          {{ item.trendText }}
        </p>
      </article>
    </div>
  </section>
</template>

<script setup>
defineProps({
  loading: {
    type: Boolean,
    default: false
  },
  items: {
    type: Array,
    default: () => []
  }
})
</script>

<style scoped>
.today-wrap {
  padding: 24px;
}

.section-head h2 {
  margin: 8px 0 0;
  font-size: 24px;
}

.section-note {
  margin: 8px 0 0;
  font-size: 13px;
  line-height: 1.5;
  color: var(--lc-muted);
  max-width: 36rem;
}

.kicker {
  margin: 0;
  color: var(--lc-subtle);
  font-size: 13px;
}

.stats-grid {
  margin-top: 24px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 24px;
}

.stat-item {
  border: 1px solid var(--lc-surface);
  border-radius: 12px;
  padding: 18px;
  box-shadow: var(--lc-shadow-sm);
}

.stat-top {
  display: flex;
  align-items: center;
  gap: 8px;
}

.icon {
  font-size: 20px;
}

.label {
  margin: 0;
  color: var(--lc-muted);
  font-size: 13px;
}

.value {
  margin: 14px 0 8px;
  font-size: 30px;
  font-weight: 800;
  line-height: 1;
}

.trend {
  margin: 0;
  font-size: 12px;
  color: var(--lc-subtle);
}

.trend.up {
  color: var(--lc-green);
}

.trend.down {
  color: var(--lc-red);
}

.tone-blue {
  background: var(--lc-blue-light);
}

.tone-green {
  background: var(--lc-green-light);
}

.tone-purple {
  background: var(--lc-indigo-soft);
}

.tone-orange {
  background: var(--lc-orange-light);
}

@media (max-width: 1100px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
