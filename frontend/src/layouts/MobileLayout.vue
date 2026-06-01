<template>
  <div class="mobile-layout" :class="{ 'is-platform-child': isPlatformChild }">
    <router-view />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const isPlatformChild = computed(() => route.path.startsWith('/m/platform'))
</script>

<style scoped>
.mobile-layout {
  max-width: 480px;
  margin: 0 auto;
  min-height: 100vh;
  position: relative;
  background: #f6f8ff;
  overflow-x: hidden;
  padding-bottom: env(safe-area-inset-bottom, 0);
}

/* /m/platform/* 下挂载的平台页：隐藏 PlatformLayout 顶栏（若从旧链跳入则仍由页面自身 media 处理） */
.mobile-layout.is-platform-child :deep(.platform-header),
.mobile-layout.is-platform-child :global(.platform-header),
.mobile-layout.is-platform-child :deep(.platform-footer),
.mobile-layout.is-platform-child :global(.platform-footer),
.mobile-layout.is-platform-child :deep(.co-creation-toolbar),
.mobile-layout.is-platform-child :global(.co-creation-toolbar) {
  display: none !important;
}

.mobile-layout.is-platform-child :deep(.positive-page .hero) {
  grid-template-columns: 1fr;
  min-height: auto;
}

.mobile-layout.is-platform-child :deep(.positive-page .hero-visual) {
  display: none;
}

.mobile-layout.is-platform-child :deep(.help-square) {
  width: calc(100% - 24px);
}
</style>
