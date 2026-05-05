<template>
  <div class="copy-field">
    <span class="copy-label">{{ label }}</span>
    <p class="content-preview">{{ preview }}</p>
    <button class="admin-btn" type="button" :disabled="disabled" @click="$emit('edit')">{{ buttonText }}</button>
  </div>
</template>

<script setup>
defineProps({
  label: { type: String, default: '正文预览' },
  preview: { type: String, default: '' },
  buttonText: { type: String, default: '编辑正文' },
  disabled: { type: Boolean, default: false }
})

defineEmits(['edit'])
</script>

<style scoped>
.copy-field {
  display: grid;
  gap: 6px;
}

.copy-label {
  font-size: 12px;
  font-weight: 700;
  color: var(--lc-muted);
}

.content-preview {
  margin: 0;
  min-height: 52px;
  border: 1px solid var(--lc-border);
  border-radius: 8px;
  padding: 8px 10px;
  background: var(--lc-surface);
  color: var(--lc-muted);
  line-height: 1.5;
  white-space: pre-wrap;
}
</style>
