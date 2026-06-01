<template>
  <section class="ck-page">
    <header class="ck-head">
      <button type="button" class="ck-back" aria-label="返回" @click="$router.back()">‹</button>
      <h1 class="ck-title">每日签到</h1>
    </header>
    <CheckinPanel />
  </section>
</template>

<script setup>
import CheckinPanel from '@/components/platform/CheckinPanel.vue'
</script>

<style scoped>
.ck-page {
  min-height: 100vh;
  background: var(--lc-bg);
  color: var(--lc-text);
}

.ck-head {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  background: var(--lc-surface);
  border-bottom: 1px solid var(--lc-soft-alt);
}
.ck-back {
  width: 48px;
  height: 52px;
  flex: 0 0 auto;
  display: grid;
  place-items: center;
  border: 0;
  background: none;
  font-size: 22px;
  color: var(--lc-indigo);
  cursor: pointer;
}
.ck-title {
  flex: 1;
  margin: 0;
  font-size: 17px;
  font-weight: 800;
}

@media (max-width: 767px) {
  :global(.platform-header),
  :global(.platform-footer),
  :global(.co-creation-toolbar) {
    display: none !important;
  }
}
</style>
