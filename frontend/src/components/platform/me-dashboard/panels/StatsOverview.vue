<template>
  <section class="bar" aria-label="数据概览">
    <header class="head">
      <h2>数据概览</h2>
      <router-link to="/modules">全部数据 ></router-link>
    </header>
    <div class="bar-grid" :style="{ '--stat-count': Math.max(1, items.length) }">
      <div
        v-for="item in items"
        :key="item.label"
        class="cell"
        role="link"
        tabindex="0"
        @click="go(item.to)"
        @keydown.enter="go(item.to)"
      >
        <div class="icon" :class="`tone-${item.tone || 'violet'}`" aria-hidden="true">{{ item.icon }}</div>
        <div class="value">{{ item.value }}</div>
        <div class="label">{{ item.label }}</div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
  items: { type: Array, required: true }
})

const router = useRouter()

function go(to) {
  if (!to) return
  router.push(to)
}
</script>

<style scoped>
.bar {
  background: #ffffff;
  border: 1px solid #e7ebf2;
  border-radius: 12px;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.055);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 20px 24px 4px;
}

.head h2 {
  margin: 0;
  color: #0f172a;
  font-size: 16px;
  font-weight: 900;
}

.head a {
  color: #7b8498;
  font-size: 12px;
  font-weight: 700;
  text-decoration: none;
  white-space: nowrap;
}

.bar-grid {
  display: grid;
  grid-template-columns: repeat(var(--stat-count), minmax(0, 1fr));
  flex: 1;
}

.cell {
  padding: 22px 12px 24px;
  display: grid;
  justify-items: center;
  align-content: center;
  gap: 6px;
  cursor: pointer;
  user-select: none;
  transition: background 0.15s;
}

.cell:hover {
  background: rgba(79, 70, 229, 0.03);
}

.cell + .cell {
  border-left: 1px solid rgba(229, 231, 235, 0.9);
}

.icon {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-size: 14px;
  font-weight: 900;
  opacity: 0.62;
}

.value {
  font-size: 26px;
  line-height: 1;
  font-weight: 900;
  color: #0f172a;
}

.label {
  font-size: 12px;
  color: #7a8699;
  font-weight: 600;
}

.tone-violet { color: #4f46e5; background: rgba(79, 70, 229, 0.10); }
.tone-rose   { color: #db2777; background: rgba(219, 39, 119, 0.10); }
.tone-amber  { color: #d97706; background: rgba(217, 119, 6, 0.12); }
.tone-blue   { color: #2563eb; background: rgba(37, 99, 235, 0.10); }
.tone-green  { color: #059669; background: rgba(5, 150, 105, 0.10); }
</style>
