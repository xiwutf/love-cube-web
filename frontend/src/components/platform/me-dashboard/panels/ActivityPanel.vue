<template>
  <section class="card" aria-label="最近动态">
    <header class="head">
      <h2>最近动态</h2>
      <router-link class="more" :to="modulesPath()">查看全部 ></router-link>
    </header>

    <ul class="list">
      <li v-for="item in items" :key="item.key" class="row">
        <span class="icon" aria-hidden="true">{{ item.icon }}</span>
        <div class="main">
          <strong>{{ item.title }}</strong>
        </div>
        <small class="time">{{ item.time }}</small>
      </li>
    </ul>
  </section>
</template>

<script setup>
import { usePlatformPath } from '@/composables/usePlatformPath.js'

defineProps({
  items: { type: Array, required: true }
})

const { modulesPath } = usePlatformPath()
</script>

<style scoped>
.card {
  background: var(--lc-surface);
  border: 1px solid var(--lc-soft-alt);
  border-radius: 12px;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.055);
  padding: 20px 24px;
  min-height: 100%;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 4px;
}

.head h2 {
  margin: 0;
  font-size: 15px;
  font-weight: 900;
  color: var(--lc-text);
}

.more {
  color: var(--lc-muted-light);
  font-size: 12px;
  font-weight: 700;
  text-decoration: none;
  white-space: nowrap;
}

.list {
  margin: 0;
  padding: 0;
  list-style: none;
  flex: 1;
  display: grid;
  align-content: center;
}

.row {
  display: grid;
  grid-template-columns: 28px minmax(0, 1fr) auto;
  gap: 10px;
  align-items: center;
  border-bottom: 1px solid rgba(229, 231, 235, 0.75);
  padding: 0 2px;
  min-height: 46px;
}

.row:last-child {
  border-bottom: 0;
}

.icon {
  width: 28px;
  height: 28px;
  border-radius: 9px;
  display: grid;
  place-items: center;
  background: rgba(79, 70, 229, 0.08);
  color: var(--lc-indigo);
  font-weight: 800;
  font-size: 13px;
  flex: 0 0 auto;
}

.main {
  min-width: 0;
}

.main strong {
  display: block;
  font-size: 12px;
  font-weight: 700;
  color: var(--lc-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.4;
}

.time {
  font-size: 11px;
  color: var(--lc-muted);
  justify-self: end;
  white-space: nowrap;
}
</style>
