<template>
  <div class="gd-m-shell">
    <header class="gd-m-head">
      <router-link :to="groupsPath()" class="gd-m-back" aria-label="返回团体广场">‹</router-link>
      <span class="gd-m-title">团体详情</span>
    </header>
    <GroupDetailMobile />
  </div>
</template>

<script setup>
import GroupDetailMobile from '@/components/mobile/platform/GroupDetailMobile.vue'
import { usePlatformPath } from '@/composables/usePlatformPath.js'

const { groupsPath } = usePlatformPath()
</script>

<style scoped>
.gd-m-shell {
  min-height: 100vh;
  background: var(--lc-bg, #f4f5fb);
}

.gd-m-head {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: var(--lc-surface, #fff);
  border-bottom: 1px solid var(--lc-soft-alt, #e8ecf4);
}

.gd-m-back {
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: #fff;
  border: 1px solid var(--lc-soft, #e8ecf4);
  text-decoration: none;
  color: var(--lc-indigo, #4f46e5);
  font-size: 22px;
}

.gd-m-title {
  font-size: 15px;
  font-weight: 800;
  color: var(--lc-text, #1a2236);
}
</style>
