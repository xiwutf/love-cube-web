<template>
  <section class="sp-page">
    <header class="sp-head">
      <router-link to="/m/platform" class="sp-back" aria-label="返回玩法中心">‹</router-link>
      <h1 class="sp-title">今日任务</h1>
    </header>
    <MyTasksPanel />
  </section>
</template>

<script setup>
import MyTasksPanel from '@/components/platform/MyTasksPanel.vue'
</script>

<style scoped>
.sp-page {
  min-height: 100vh;
  background: var(--lc-bg);
  color: var(--lc-text);
}

.sp-head {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  background: var(--lc-surface);
  border-bottom: 1px solid var(--lc-soft-alt);
}
.sp-back {
  width: 48px;
  height: 52px;
  flex: 0 0 auto;
  display: grid;
  place-items: center;
  font-size: 22px;
  color: var(--lc-indigo);
  text-decoration: none;
}
.sp-title {
  flex: 1;
  margin: 0;
  font-size: 17px;
  font-weight: 800;
}
</style>
