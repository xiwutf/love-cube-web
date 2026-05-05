<template>
  <div class="admin-brief-head">
    <div>
      <h3 class="admin-brief-title">{{ title }}</h3>
      <p class="admin-brief-summary">{{ summary || fallbackSummary }}</p>
    </div>
    <div class="admin-brief-status">
      <span class="admin-tag" :class="status">{{ status }}</span>
      <span class="admin-row-meta">{{ updatedLabel }}：{{ updatedAtText }}</span>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, default: '' },
  summary: { type: String, default: '' },
  fallbackSummary: { type: String, default: '暂无摘要' },
  status: { type: String, default: 'draft' },
  updatedAtText: { type: String, default: '-' },
  updatedLabel: { type: String, default: '更新' }
})
</script>

<style scoped>
.admin-brief-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.admin-brief-title {
  margin: 0;
  font-size: 16px;
  color: var(--lc-text);
}

.admin-brief-summary {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--lc-muted);
  line-height: 1.6;
}

.admin-brief-status {
  display: grid;
  justify-items: end;
  gap: 8px;
}
</style>
