<template>
  <section class="governance">
    <header class="bar-head">
      <h3>平台治理</h3>
      <p>弱化展示，聚焦待处理风险与审核流。</p>
    </header>

    <div class="bar-grid">
      <article v-for="item in items" :key="item.label" class="govern-item">
        <div>
          <p class="label">{{ item.label }}</p>
          <p class="value">{{ loading ? '...' : item.value }}</p>
        </div>
        <router-link :to="item.to" class="mini-btn">{{ item.actionText }}</router-link>
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
.bar-head {
  margin-bottom: 16px;
}

.bar-head h3 {
  margin: 0;
  font-size: 20px;
}

.bar-head p {
  margin: 6px 0 0;
  color: var(--lc-subtle);
  font-size: 13px;
}

.bar-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.govern-item {
  border: 1px solid #eee;
  background: #f8fafc;
  border-radius: 12px;
  padding: 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  box-shadow: var(--lc-shadow-sm);
}

.label {
  margin: 0;
  font-size: 12px;
  color: var(--lc-muted);
}

.value {
  margin: 6px 0 0;
  font-size: 24px;
  font-weight: 700;
}

.mini-btn {
  text-decoration: none;
  border: 1px solid #eee;
  border-radius: 10px;
  padding: 6px 10px;
  font-size: 12px;
  color: var(--lc-blue-dark);
  background: #fff;
}
</style>
