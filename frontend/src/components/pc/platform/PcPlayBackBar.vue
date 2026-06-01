<template>
  <header class="pc-play-back">
    <router-link :to="backTo" class="pc-play-back-link">{{ backLabel }}</router-link>
    <h1 v-if="title" class="pc-play-back-title">{{ title }}</h1>
  </header>
</template>

<script setup>
import { PC_PLAY } from '@/constants/pcPaths.js'

defineProps({
  title: { type: String, default: '' },
  backTo: { type: String, default: PC_PLAY },
  backLabel: { type: String, default: '← 成长玩法' }
})
</script>

<style scoped>
.pc-play-back {
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px 24px 0;
}

.pc-play-back-link {
  font-size: 14px;
  color: var(--lc-blue, #2563eb);
  text-decoration: none;
}

.pc-play-back-title {
  margin: 8px 0 0;
  font-size: 22px;
  font-weight: 800;
  color: var(--lc-text, #1a2236);
}
</style>
