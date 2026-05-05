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

    <div class="info-stack">
      <div class="info-panel info-panel-compact">
        <header class="panel-head">
          <h2>更新日志</h2>
          <router-link to="/platform/changelog">查看全部 →</router-link>
        </header>
        <div class="log-list">
          <article v-for="item in changelogItems" :key="`${item.version}-${item.title}`" class="log-row">
            <span>{{ item.version }}</span>
            <strong>{{ item.title }}</strong>
            <time>{{ item.date }}</time>
          </article>
        </div>
      </div>

      <div class="info-panel info-panel-compact">
        <header class="panel-head">
          <h2>待更新</h2>
          <router-link to="/platform/pending-updates">查看全部 →</router-link>
        </header>
        <div class="pending-list">
          <article v-for="item in pendingItems" :key="item.id || item.title" class="pending-row">
            <div class="pending-headline">
              <strong>{{ item.title }}</strong>
              <em>{{ item.status || '处理中' }}</em>
            </div>
            <p>{{ item.detail || item.title }}</p>
          </article>
        </div>
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
  },
  pendingItems: {
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

.info-stack {
  display: grid;
  gap: var(--lc-space-3);
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
  margin-bottom: var(--lc-space-4);
}

.panel-head h2 {
  margin: 0;
  color: var(--lc-text);
  font-size: 22px;
}

.panel-head a {
  flex: 0 0 auto;
  color: var(--lc-blue);
  font-size: var(--lc-text-sm);
  font-weight: 900;
  text-decoration: none;
}

.official-list,
.log-list,
.pending-list {
  display: grid;
  gap: 10px;
}

.official-row,
.log-row {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  gap: var(--lc-space-3);
  align-items: center;
  min-height: 30px;
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
  font-size: 14px;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.official-row time,
.log-row time {
  color: var(--lc-subtle);
  font-size: 12px;
}

.log-row span {
  color: var(--lc-green);
  font-size: 13px;
  font-weight: 950;
}

.info-panel-compact {
  padding: 14px 16px;
}

.info-panel-compact .panel-head {
  margin-bottom: 10px;
}

.info-panel-compact .panel-head h2 {
  font-size: 18px;
}

.pending-row {
  display: grid;
  gap: 4px;
  padding-bottom: 6px;
  border-bottom: 1px dashed var(--lc-border);
}

.pending-row:last-child {
  padding-bottom: 0;
  border-bottom: 0;
}

.pending-headline {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.pending-headline strong {
  color: var(--lc-text);
  font-size: 14px;
  font-weight: 800;
}

.pending-headline em {
  font-style: normal;
  font-size: 12px;
  color: var(--lc-surface);
  background: var(--lc-amber);
  border-radius: 999px;
  padding: 3px 10px;
  font-weight: 800;
}

.pending-row p {
  margin: 0;
  color: var(--lc-subtle);
  font-size: 12px;
  line-height: 1.35;
}

@media (max-width: 900px) {
  .official-grid {
    grid-template-columns: 1fr;
    gap: var(--lc-space-4);
  }
}

@media (max-width: 560px) {
  .info-panel {
    padding: 14px 12px;
    border-radius: 16px;
  }

  .official-grid .info-stack {
    display: none;
  }

  .official-row,
  .log-row {
    grid-template-columns: auto minmax(0, 1fr) auto;
    gap: 8px;
  }

  .panel-head h2 {
    font-size: 20px;
  }

  .official-row strong {
    font-size: 14px;
  }

  .official-row time {
    display: block;
    font-size: 12px;
  }
}
</style>
