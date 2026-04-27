<template>
  <section class="stats-band" aria-label="平台数据">
    <article v-for="item in platformStats" :key="item.label" class="stat-card">
      <span class="stat-icon" :class="item.tone">{{ item.icon }}</span>
      <strong>{{ item.value }}</strong>
      <small>{{ item.label }}</small>
      <em>{{ item.delta }}</em>
    </article>
  </section>
</template>

<script setup>
defineProps({
  platformStats: {
    type: Array,
    default: () => []
  }
})
</script>

<style scoped>
.stats-band {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 1px;
  margin-top: 22px;
  padding: 16px;
  border-radius: 8px;
  background: linear-gradient(135deg, #eef5ff, #f8fbff);
}

.stat-card {
  display: grid;
  grid-template-columns: 46px 1fr;
  gap: 5px 12px;
  align-items: center;
  min-height: 88px;
  padding: 14px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #fff;
}

.stat-icon {
  grid-row: 1 / 4;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-weight: 900;
}

.stat-card strong {
  color: #0f172a;
  font-size: 19px;
}

.stat-card small {
  color: #475569;
  font-size: 12px;
}

.stat-card em {
  color: #16a34a;
  font-size: 12px;
  font-style: normal;
}

.tone-pink { --tone: #ec4899; --tone-bg: #fff1f5; }
.tone-violet { --tone: #6366f1; --tone-bg: #eef2ff; }
.tone-blue { --tone: #2563eb; --tone-bg: #eff6ff; }
.tone-orange { --tone: #f97316; --tone-bg: #fff7ed; }
.tone-green { --tone: #16a34a; --tone-bg: #f0fdf4; }
.tone-purple { --tone: #7c3aed; --tone-bg: #f5f3ff; }

.stat-icon {
  background: var(--tone-bg);
  color: var(--tone);
}

@media (max-width: 1180px) {
  .stats-band {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .stats-band {
    grid-template-columns: 1fr;
    margin-top: 16px;
    padding: 10px;
    gap: 8px;
  }
}
</style>
