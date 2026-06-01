<template>
  <section class="pc-feature">
    <PcPlayBackBar title="今日任务" />
    <div class="pc-feature-body">
      <MyTasksPanel />
    </div>
  </section>
</template>

<script setup>
import PcPlayBackBar from '@/components/pc/platform/PcPlayBackBar.vue'
import MyTasksPanel from '@/components/platform/MyTasksPanel.vue'
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
