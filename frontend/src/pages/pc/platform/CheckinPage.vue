<template>
  <section class="pc-feature">
    <PcPlayBackBar title="每日签到" />
    <div class="pc-feature-body">
      <CheckinPanel />
    </div>
  </section>
</template>

<script setup>
import PcPlayBackBar from '@/components/pc/platform/PcPlayBackBar.vue'
import CheckinPanel from '@/components/platform/CheckinPanel.vue'
</script>

<style scoped>
.pc-feature {
  min-height: 60vh;
  padding-bottom: 48px;
}

.pc-feature-body {
  max-width: 900px;
  margin: 0 auto;
  padding: 16px 24px;
}
</style>
