<template>
  <section class="sp-page">
    <header class="sp-head">
      <button type="button" class="sp-back" aria-label="返回" @click="goBack()">‹</button>
      <h1 class="sp-title">今日任务</h1>
    </header>
    <MyTasksPanel />
  </section>
</template>

<script setup>
import MyTasksPanel from '@/components/platform/MyTasksPanel.vue'
import { useBackNavigation } from '@/composables/useBackNavigation.js'

const { goBack } = useBackNavigation('/m/platform')
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
  border: none;
  background: transparent;
  padding: 0;
  cursor: pointer;
}
.sp-title {
  flex: 1;
  margin: 0;
  font-size: 17px;
  font-weight: 800;
}
</style>
