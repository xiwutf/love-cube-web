<template>
  <div class="fellowship-layout fellowship-shell">
    <FellowshipLifePhotosBanner />
    <router-view />
    <router-link to="/fellowship-intro" class="back-platform">返回平台官网</router-link>
  </div>
</template>

<script setup>
import FellowshipLifePhotosBanner from '@/components/fellowship/FellowshipLifePhotosBanner.vue'
</script>

<style scoped>
.fellowship-layout {
  position: relative;
}
</style>
