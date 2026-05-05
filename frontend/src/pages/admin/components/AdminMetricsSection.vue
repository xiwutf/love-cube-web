<template>
  <section class="section platform-card">
    <header class="head">
      <div>
        <p class="kicker">数据区</p>
        <h3>{{ title }}</h3>
        <p v-if="description" class="desc">{{ description }}</p>
      </div>
      <router-link v-if="linkTo" :to="linkTo" class="link">{{ linkText || '前往' }}</router-link>
    </header>
    <div class="metrics">
      <div v-for="item in items" :key="item.label" class="metric">
        <p class="label">{{ item.label }}</p>
        <p class="value">{{ loading ? '…' : format(item.value) }}</p>
      </div>
    </div>
  </section>
</template>

<script setup>
defineProps({
  title: { type: String, required: true },
  description: { type: String, default: '' },
  linkTo: { type: String, default: '' },
  linkText: { type: String, default: '' },
  loading: { type: Boolean, default: false },
  items: { type: Array, default: () => [] }
})

function format(v) {
  if (v == null || Number.isNaN(v)) return '0'
  if (typeof v === 'object') return JSON.stringify(v)
  return v
}
</script>

<style scoped>
.section {
  padding: 20px 22px;
  border: 1px solid var(--lc-surface);
  border-radius: 12px;
}

.head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.kicker {
  margin: 0;
  font-size: 12px;
  color: var(--lc-subtle);
}

h3 {
  margin: 6px 0 0;
  font-size: 18px;
  font-weight: 800;
  color: var(--lc-text);
}

.desc {
  margin: 6px 0 0;
  font-size: 13px;
  line-height: 1.45;
  color: var(--lc-muted);
  max-width: 40rem;
}

.link {
  flex-shrink: 0;
  border: 1px solid var(--lc-surface);
  border-radius: 10px;
  padding: 8px 14px;
  text-decoration: none;
  color: var(--lc-blue-dark);
  font-size: 13px;
  background: var(--lc-surface);
}

.metrics {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(132px, 1fr));
  gap: 12px 16px;
}

.metric {
  padding: 10px 0 0;
  border-top: 1px solid var(--lc-border);
}

.label {
  margin: 0;
  font-size: 12px;
  color: var(--lc-muted);
  line-height: 1.35;
}

.value {
  margin: 6px 0 0;
  font-size: 22px;
  font-weight: 800;
  color: var(--lc-text);
  word-break: break-all;
}
</style>
