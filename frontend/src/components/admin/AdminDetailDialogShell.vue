<template>
  <div v-if="visible" class="detail-dialog-mask" @click.self="requestClose">
    <section class="detail-dialog-panel" :style="{ width: `min(${maxWidth}px, 100%)` }">
      <header class="detail-dialog-head">
        <h3>{{ title || '详情' }}</h3>
        <button class="admin-btn" type="button" :disabled="loading" @click="requestClose">关闭</button>
      </header>
      <slot />
    </section>
  </div>
</template>

<script setup>
const props = defineProps({
  visible: { type: Boolean, default: false },
  title: { type: String, default: '' },
  loading: { type: Boolean, default: false },
  maxWidth: { type: Number, default: 1100 }
})

const emit = defineEmits(['update:visible'])

function requestClose() {
  if (props.loading) return
  emit('update:visible', false)
}
</script>

<style scoped>
.detail-dialog-mask {
  position: fixed;
  inset: 0;
  z-index: 1100;
  background: rgba(15, 23, 42, 0.35);
  display: grid;
  place-items: center;
  padding: 20px;
}

.detail-dialog-panel {
  max-height: calc(100vh - 40px);
  overflow: auto;
  background: var(--lc-surface);
  border-radius: 12px;
  border: 1px solid var(--lc-border);
  padding: 16px;
  display: grid;
  gap: 12px;
}

.detail-dialog-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.detail-dialog-head h3 {
  margin: 0;
  color: var(--lc-text);
  font-size: 18px;
}
</style>
