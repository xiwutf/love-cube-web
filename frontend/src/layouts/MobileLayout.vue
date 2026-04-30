<template>
  <div class="mobile-layout">
    <router-view />
  </div>
</template>

<script setup>
</script>

<style scoped>
.mobile-layout {
  max-width: 480px;
  margin: 0 auto;
  min-height: 100vh;
  position: relative;
  background: #f6f8ff;
  overflow-x: hidden;
}
</style>
