<template>
  <section class="card" aria-label="功能与服务">
    <header class="head">
      <h2>功能与服务</h2>
    </header>

    <div class="grid">
      <div
        v-for="item in items"
        :key="item.title"
        class="tile"
        role="link"
        tabindex="0"
        @click="go(item.to)"
      >
        <div class="icon" :class="`tone-${item.tone || 'violet'}`" aria-hidden="true">{{ item.icon }}</div>
        <strong>{{ item.title }}</strong>
        <small>{{ item.desc }}</small>
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
.card {
  background: #ffffff;
  border: 1px solid #e7ebf2;
  border-radius: 12px;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.055);
  padding: 18px 24px;
}

.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.head h2 {
  margin: 0;
  font-size: 16px;
  font-weight: 900;
  color: #0f172a;
}

.grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 12px;
}

.tile {
  border: 0;
  border-radius: 10px;
  background: #ffffff;
  padding: 10px 12px;
  display: grid;
  grid-template-columns: 34px minmax(0, 1fr);
  grid-template-rows: auto auto;
  column-gap: 10px;
  row-gap: 2px;
  align-items: center;
  text-align: left;
  cursor: pointer;
  user-select: none;
}

.tile:hover {
  background: rgba(79, 70, 229, 0.05);
}

.icon {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  font-weight: 800;
  font-size: 12px;
  grid-row: 1 / span 2;
}

.tile strong {
  font-size: 12px;
  font-weight: 800;
  color: #0f172a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.tile small {
  font-size: 11px;
  color: #64748b;
  min-height: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.tone-violet { color: #4f46e5; background: rgba(79, 70, 229, 0.10); }
.tone-rose { color: #db2777; background: rgba(219, 39, 119, 0.10); }
.tone-amber { color: #d97706; background: rgba(217, 119, 6, 0.12); }
.tone-blue { color: #2563eb; background: rgba(37, 99, 235, 0.10); }
.tone-green { color: #059669; background: rgba(5, 150, 105, 0.10); }
</style>
