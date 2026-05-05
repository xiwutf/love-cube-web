<template>
  <div class="copy-editor-field">
    <label class="copy-field">
      <span class="copy-label">{{ summaryLabel }}</span>
      <input
        :value="summary"
        class="admin-input summary-input"
        :placeholder="summaryPlaceholder"
        @input="$emit('update:summary', $event?.target?.value || '')"
      />
    </label>
    <ContentPreviewField
      :label="previewLabel"
      :preview="preview"
      :button-text="buttonText"
      :disabled="disabled"
      @edit="$emit('edit')"
    />
  </div>
</template>

<script setup>
import ContentPreviewField from '@/components/admin/ContentPreviewField.vue'

defineProps({
  summary: { type: String, default: '' },
  summaryLabel: { type: String, default: '摘要' },
  summaryPlaceholder: { type: String, default: '请输入摘要' },
  preview: { type: String, default: '' },
  previewLabel: { type: String, default: '正文预览' },
  buttonText: { type: String, default: '编辑正文' },
  disabled: { type: Boolean, default: false }
})

defineEmits(['update:summary', 'edit'])
</script>

<style scoped>
.copy-editor-field {
  display: grid;
  gap: 10px;
}

.copy-field {
  display: grid;
  gap: 6px;
}

.copy-label {
  font-size: 12px;
  font-weight: 700;
  color: var(--lc-muted);
}

.summary-input {
  min-width: 260px;
}
</style>
