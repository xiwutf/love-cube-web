<template>
  <div class="navbar">
    <div class="navbar-left" @click="handleBack">
      <van-icon v-if="back" name="arrow-left" size="22" />
    </div>
    <div class="navbar-title">{{ title }}</div>
    <div class="navbar-right">
      <slot name="right" />
    </div>
  </div>
</template>

<script setup>
import { useBackNavigation } from '@/composables/useBackNavigation.js'

const props = defineProps({
  title: { type: String, default: '' },
  back:  { type: Boolean, default: true },
  to:    { type: String, default: '' },   // 指定返回路径（可选）
})

const { goBack } = useBackNavigation()

function handleBack() {
  if (!props.back) return
  goBack(props.to)
}
</script>

<style scoped>
.navbar {
  display: flex;
  align-items: center;
  min-height: calc(48px + env(safe-area-inset-top, 0px));
  padding: env(safe-area-inset-top, 0px) 12px 0;
  box-sizing: border-box;
  background: #fff;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 1px 4px rgba(0,0,0,.06);
}
.navbar-left  { width: 40px; display: flex; align-items: center; cursor: pointer; }
.navbar-title { flex: 1; text-align: center; font-size: 17px; font-weight: 600; color: #333; }
.navbar-right { width: 40px; display: flex; align-items: center; justify-content: flex-end; }
</style>
