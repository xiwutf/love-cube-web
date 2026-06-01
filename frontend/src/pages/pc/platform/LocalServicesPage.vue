<template>
  <section class="pc-feature">
    <PcPlayBackBar title="本地服务" />
    <LocalResourcesDesktop />
  </section>
</template>

<script setup>
import PcPlayBackBar from '@/components/pc/platform/PcPlayBackBar.vue'
import LocalResourcesDesktop from '@/components/platform/local/LocalResources.desktop.vue'
</script>

<style scoped>
.pc-feature {
  padding-bottom: 48px;
}
</style>
