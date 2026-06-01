<template>
  <section class="pc-feature">
    <PcPlayBackBar title="消息中心" />
    <MessagesCenterPage class="pc-msg-body" />
  </section>
</template>

<script setup>
import PcPlayBackBar from '@/components/pc/platform/PcPlayBackBar.vue'
import MessagesCenterPage from '@/pages/platform/MessagesCenterPage.vue'
</script>

<style scoped>
.pc-feature {
  min-height: 60vh;
  padding-bottom: 48px;
}

.pc-msg-body :deep(.messages-header) {
  display: none;
}

.pc-msg-body :deep(.platform-page) {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 24px 24px;
}
</style>
