<template>
  <section class="panel platform-card">
    <header class="panel-head">
      <div>
        <p class="kicker">业务运营</p>
        <h3>平台内容运营</h3>
        <p class="desc">管理平台内容生态，追踪发布状态。</p>
      </div>
      <router-link to="/admin/announcements" class="entry-btn">进入内容管理</router-link>
    </header>

    <div class="panel-visual">
      <div class="visual-block"></div>
    </div>

    <footer class="metrics-row">
      <div v-for="item in items" :key="item.label" class="metric">
        <p class="label">{{ item.label }}</p>
        <p class="value">{{ loading ? '...' : item.value }}</p>
      </div>
    </footer>
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
.panel {
  padding: 24px;
  border: 1px solid var(--lc-surface);
  border-radius: 12px;
}

.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.kicker {
  margin: 0;
  color: var(--lc-subtle);
  font-size: 12px;
}

h3 {
  margin: 8px 0;
  font-size: 24px;
}

.desc {
  margin: 0;
  font-size: 13px;
  color: var(--lc-muted);
}

.entry-btn {
  border: 1px solid var(--lc-surface);
  border-radius: 12px;
  padding: 10px 16px;
  text-decoration: none;
  color: var(--lc-blue-dark);
  font-size: 13px;
  background: var(--lc-surface);
}

.panel-visual {
  margin: 22px 0;
  min-height: 140px;
  border-radius: 12px;
  border: 1px solid var(--lc-surface);
  background: linear-gradient(135deg, var(--lc-blue-light), var(--lc-bg));
  padding: 18px;
}

.visual-block {
  width: 70%;
  height: 100%;
  border-radius: 10px;
  border: 1px dashed var(--lc-blue-border);
  background: rgba(255, 255, 255, 0.8);
}

.metrics-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.metric {
  padding-top: 10px;
}

.label {
  margin: 0;
  color: var(--lc-muted);
  font-size: 13px;
}

.value {
  margin: 8px 0 0;
  font-size: 28px;
  font-weight: 800;
}
</style>
