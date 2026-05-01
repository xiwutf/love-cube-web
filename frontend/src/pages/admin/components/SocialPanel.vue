<template>
  <section class="panel platform-card">
    <header class="panel-head">
      <div>
        <p class="kicker">数据区</p>
        <h3>联谊侧互动</h3>
        <p class="desc">点赞、私信等活跃度参考。处理认证与举报请用功能地图「审核与安全」。</p>
      </div>
      <router-link to="/admin/verifications" class="entry-btn">去认证审核</router-link>
    </header>

    <div class="panel-visual">
      <div class="heart"></div>
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
  color: var(--lc-pink);
  font-size: 13px;
  background: var(--lc-surface);
}

.panel-visual {
  margin: 22px 0;
  min-height: 140px;
  border-radius: 12px;
  border: 1px solid var(--lc-surface);
  background: linear-gradient(135deg, var(--lc-pink-light), var(--lc-pink-light));
  display: flex;
  align-items: center;
  justify-content: center;
}

.heart {
  width: 84px;
  height: 84px;
  background: radial-gradient(circle at 35% 30%, #fb7185, var(--lc-pink));
  transform: rotate(45deg);
  border-radius: 12px;
  position: relative;
}

.heart::before,
.heart::after {
  content: '';
  position: absolute;
  width: 84px;
  height: 84px;
  border-radius: 50%;
  background: inherit;
}

.heart::before {
  top: -42px;
  left: 0;
}

.heart::after {
  left: -42px;
  top: 0;
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
