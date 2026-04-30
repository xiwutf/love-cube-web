<template>
  <section class="card" aria-label="我的成就">
    <header class="head">
      <h2>我的成就</h2>
      <router-link class="more" to="/modules">全部成就 ></router-link>
    </header>

    <div class="badges">
      <div
        v-for="item in items"
        :key="item.key"
        class="badge"
        :class="{ off: !item.achieved }"
        :title="item.desc"
      >
        <span class="icon" :class="item.achieved ? 'on' : 'dim'" aria-hidden="true">{{ item.icon }}</span>
        <span class="title">{{ item.title }}</span>
      </div>
    </div>
  </section>
</template>

<script setup>
defineProps({
  items: { type: Array, required: true }
})
</script>

<style scoped>
.card {
  background: #ffffff;
  border: 1px solid #e7ebf2;
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
  margin-bottom: 14px;
}

.head h2 {
  margin: 0;
  font-size: 15px;
  font-weight: 900;
  color: #0f172a;
}

.more {
  color: #64748b;
  font-size: 12px;
  font-weight: 700;
  text-decoration: none;
  white-space: nowrap;
}

.badges {
  display: grid;
  grid-template-columns: repeat(4, minmax(64px, 1fr));
  flex: 1;
  align-content: center;
  gap: 8px;
}

.badge {
  display: grid;
  justify-items: center;
  align-items: center;
  gap: 8px;
  padding: 6px 4px 2px;
  border: 0;
  background: transparent;
  cursor: default;
  transition: background 0.12s;
}

.icon {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  display: grid;
  place-items: center;
  font-size: 24px;
  font-weight: 900;
  flex: 0 0 auto;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.9), 0 10px 18px rgba(15, 23, 42, 0.08);
}

.icon.on  { color: #ffffff; background: linear-gradient(145deg, #7c66ff, #4f46e5); }
.icon.dim { color: #94a3b8; background: linear-gradient(145deg, #f1f5f9, #d7dde7); }

.badge:nth-child(2) .icon.on { background: linear-gradient(145deg, #ffb35c, #f97316); }
.badge:nth-child(3) .icon.on { background: linear-gradient(145deg, #ff67b2, #db2777); }

.title {
  max-width: 70px;
  font-size: 11px;
  font-weight: 800;
  color: #0f172a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.off {
  opacity: 0.38;
  filter: grayscale(0.6);
}
</style>
