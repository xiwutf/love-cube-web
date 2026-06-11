<template>
  <div class="msg-m-wrap">
    <header class="msg-m-head">
      <button type="button" class="msg-m-back" @click="goBack()">‹ 玩法</button>
      <h1>消息中心</h1>
    </header>
    <MessagesCenterPage class="msg-m-body" />
  </div>
</template>

<script setup>
import MessagesCenterPage from '@/pages/platform/MessagesCenterPage.vue'
import { useBackNavigation } from '@/composables/useBackNavigation.js'

const { goBack } = useBackNavigation('/m/platform')
</script>

<style scoped>
.msg-m-wrap {
  min-height: 100vh;
  background: var(--lc-bg, #f4f5fb);
}

.msg-m-head {
  padding: 14px 14px 0;
  background: #fff;
  border-bottom: 1px solid var(--lc-soft, #e8ecf4);
}

.msg-m-back {
  font-size: 14px;
  color: var(--lc-blue, #2563eb);
  text-decoration: none;
  border: none;
  background: transparent;
  padding: 0;
  cursor: pointer;
}

.msg-m-head h1 {
  margin: 6px 0 12px;
  font-size: 20px;
  font-weight: 800;
}

.msg-m-body :deep(.messages-header) {
  display: none;
}

.msg-m-body :deep(.platform-page) {
  padding: 0 12px 24px;
}
</style>
