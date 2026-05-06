<template>
  <section class="activity-panel">
    <img class="activity-art" :src="activityImage" alt="" aria-hidden="true">
    <header class="panel-head">
      <div>
        <h2>活动专区</h2>
        <p>发现精彩活动，参与有趣体验</p>
      </div>
      <router-link to="/events">查看活动 →</router-link>
    </header>

    <dl class="activity-stats">
      <div v-for="item in stats" :key="item.label">
        <dt>{{ item.value }}</dt>
        <dd>{{ item.label }}</dd>
      </div>
    </dl>

    <div v-if="events.length" class="event-list">
      <router-link v-for="item in events" :key="item.id" :to="item.to" class="event-row">
        <span>{{ item.status }}</span>
        <div>
          <strong>{{ item.title }}</strong>
          <p>{{ item.summary }}</p>
        </div>
        <time>{{ item.date }}</time>
      </router-link>
    </div>
    <p v-else class="event-empty">暂无活动数据</p>
  </section>
</template>

<script setup>
import activityImage from '@/assets/活动模块.webp'

defineProps({
  events: {
    type: Array,
    required: true
  },
  stats: {
    type: Array,
    required: true
  }
})
</script>

<style scoped>
.activity-panel {
  position: relative;
  min-height: 210px;
  padding: var(--lc-space-8) var(--lc-space-8) var(--lc-space-6);
  border: 1px solid var(--lc-blue-border);
  border-radius: var(--lc-radius);
  background:
    linear-gradient(135deg, rgba(239, 246, 255, 0.95), rgba(255, 255, 255, 0.98));
  box-shadow: var(--lc-shadow-sm);
  overflow: hidden;
}

.activity-panel > *:not(.activity-art) {
  position: relative;
  z-index: 1;
}

.activity-art {
  position: absolute;
  right: 22px;
  bottom: -18px;
  width: 250px;
  opacity: 0.9;
  pointer-events: none;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  gap: var(--lc-space-4);
}

.panel-head h2 {
  margin: 0;
  color: var(--lc-blue);
  font-size: 28px;
}

.panel-head p {
  margin: var(--lc-space-2) 0 0;
  color: var(--lc-muted);
  font-weight: 800;
}

.panel-head a {
  color: var(--lc-blue);
  flex: 0 0 auto;
  font-size: var(--lc-text-sm);
  font-weight: 950;
  text-decoration: none;
}

.activity-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  margin: var(--lc-space-6) 0 0;
  gap: var(--lc-space-3);
}

.activity-stats div {
  padding: var(--lc-space-3);
  border-radius: var(--lc-radius-sm);
  background: var(--lc-surface);
  text-align: center;
}

.activity-stats dt {
  color: var(--lc-text);
  font-size: 23px;
  font-weight: 950;
}

.activity-stats dd {
  margin: var(--lc-space-1) 0 0;
  color: var(--lc-muted);
  font-size: var(--lc-text-sm);
  font-weight: 800;
}

.event-list {
  display: grid;
  gap: var(--lc-space-3);
  width: min(100%, 520px);
  margin-top: var(--lc-space-5);
}

.event-empty {
  width: min(100%, 520px);
  margin-top: var(--lc-space-5);
  padding: var(--lc-space-4);
  border-radius: var(--lc-radius-sm);
  border: 1px dashed rgba(191, 219, 254, 0.9);
  color: var(--lc-subtle);
  font-size: var(--lc-text-sm);
  font-weight: 800;
  background: rgba(255, 255, 255, 0.72);
}

.event-row {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  gap: var(--lc-space-3);
  align-items: center;
  padding: var(--lc-space-3);
  border: 1px solid rgba(191, 219, 254, 0.78);
  border-radius: var(--lc-radius-sm);
  color: inherit;
  background: rgba(255, 255, 255, 0.82);
  text-decoration: none;
}

.event-row span {
  padding: 5px 9px;
  border-radius: 999px;
  color: var(--lc-blue);
  background: var(--lc-blue-light);
  font-size: var(--lc-text-xs);
  font-weight: 950;
}

.event-row strong {
  color: var(--lc-text);
}

.event-row p {
  overflow: hidden;
  margin: var(--lc-space-1) 0 0;
  color: var(--lc-muted);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.event-row time {
  color: var(--lc-subtle);
  font-size: var(--lc-text-sm);
}

@media (max-width: 680px) {
  .activity-panel {
    padding: var(--lc-space-5);
  }

  .activity-art {
    display: none;
  }

  .panel-head {
    display: grid;
  }

  .activity-stats {
    grid-template-columns: 1fr 1fr 1fr;
  }

  .event-row {
    grid-template-columns: 1fr;
  }

  .event-row p {
    white-space: normal;
  }
}
</style>
