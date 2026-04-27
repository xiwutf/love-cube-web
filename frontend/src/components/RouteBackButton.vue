<template>
  <button class="route-back-button" type="button" :aria-label="label" :title="label" @click="goBack()">
    <svg viewBox="0 0 24 24" aria-hidden="true">
      <path d="M15 18 9 12l6-6" />
    </svg>
    <span>{{ label }}</span>
  </button>
</template>

<script setup>
import { useBackNavigation } from '@/composables/useBackNavigation.js'

defineProps({
  label: { type: String, default: '返回' }
})

const { goBack } = useBackNavigation()
</script>

<style scoped>
.route-back-button {
  border: 1px solid var(--lc-border);
  background: var(--lc-surface);
  color: var(--lc-muted);
  min-width: 40px;
  height: 40px;
  padding: 0 var(--lc-space-3);
  border-radius: var(--lc-radius-sm);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--lc-space-1);
  font-size: var(--lc-text-sm);
  font-weight: 700;
  line-height: 1;
  box-shadow: var(--lc-shadow-sm);
  cursor: pointer;
  transition: var(--lc-transition);
}

.route-back-button:hover {
  border-color: var(--lc-pink-border);
  color: var(--lc-pink);
}

.route-back-button svg {
  width: 18px;
  height: 18px;
  fill: none;
  stroke: currentColor;
  stroke-width: 2.2;
  stroke-linecap: round;
  stroke-linejoin: round;
  flex: 0 0 auto;
}
</style>
