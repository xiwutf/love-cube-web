<template>
  <section class="official-grid">
    <div class="info-panel">
      <header class="panel-head">
        <h2>官方动态</h2>
        <router-link to="/announcements">查看全部 →</router-link>
      </header>
      <div class="official-list">
        <router-link v-for="item in officialItems" :key="item.id" :to="item.to" class="official-row">
          <span>{{ item.tag }}</span>
          <strong>{{ item.title }}</strong>
          <time>{{ item.date }}</time>
        </router-link>
      </div>
    </div>

    <div class="info-panel">
      <header class="panel-head">
        <h2>更新日志</h2>
        <router-link to="/announcements">查看全部 →</router-link>
      </header>
      <div class="log-list">
        <article v-for="item in changelogItems" :key="`${item.version}-${item.title}`" class="log-row">
          <span>{{ item.version }}</span>
          <strong>{{ item.title }}</strong>
          <time>{{ item.date }}</time>
        </article>
      </div>
    </div>
  </section>
</template>

<script setup>
defineProps({
  officialItems: {
    type: Array,
    required: true
  },
  changelogItems: {
    type: Array,
    required: true
  }
})
</script>

<style scoped>
.official-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--lc-space-5);
  margin-top: var(--lc-space-5);
}

.info-panel {
  min-width: 0;
  padding: var(--lc-space-6);
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius);
  background: var(--lc-surface);
  box-shadow: var(--lc-shadow-sm);
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--lc-space-4);
  margin-bottom: var(--lc-space-5);
}

.panel-head h2 {
  margin: 0;
  color: var(--lc-text);
  font-size: 24px;
}

.panel-head a {
  flex: 0 0 auto;
  color: var(--lc-blue);
  font-size: var(--lc-text-sm);
  font-weight: 900;
  text-decoration: none;
}

.official-list,
.log-list {
  display: grid;
  gap: var(--lc-space-3);
}

.official-row,
.log-row {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  gap: var(--lc-space-3);
  align-items: center;
  min-height: 34px;
  color: inherit;
  text-decoration: none;
}

.official-row span {
  padding: 4px 8px;
  border-radius: 6px;
  color: var(--lc-surface);
  background: var(--lc-blue);
  font-size: var(--lc-text-xs);
  font-weight: 900;
}

.official-row:nth-child(1) span {
  background: var(--lc-pink);
}

.official-row:nth-child(2) span {
  background: var(--lc-amber);
}

.official-row strong,
.log-row strong {
  overflow: hidden;
  color: var(--lc-text);
  font-size: 15px;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.official-row time,
.log-row time {
  color: var(--lc-subtle);
  font-size: var(--lc-text-sm);
}

.log-row span {
  color: var(--lc-green);
  font-size: var(--lc-text-sm);
  font-weight: 950;
}

@media (max-width: 900px) {
  .official-grid {
    grid-template-columns: 1fr;
    gap: var(--lc-space-4);
  }
}

@media (max-width: 560px) {
  .info-panel {
    padding: var(--lc-space-5);
  }

  .official-row,
  .log-row {
    grid-template-columns: auto minmax(0, 1fr);
  }

  .official-row time,
  .log-row time {
    display: none;
  }
}
</style>
