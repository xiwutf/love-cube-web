<template>
  <div class="interest-filter-bar">
    <button
      v-for="option in options"
      :key="option.key"
      type="button"
      class="filter-chip"
      :class="{ active: modelValue === option.key }"
      @click="emit('update:modelValue', option.key)"
    >
      {{ option.label }}
    </button>
  </div>
</template>

<script setup>
defineProps({
  modelValue: { type: String, default: 'all' },
  options: { type: Array, default: () => [] }
})

const emit = defineEmits(['update:modelValue'])
</script>

<style scoped>
.interest-filter-bar {
  display: flex;
  gap: 8px;
  padding: 10px 12px 4px;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

.filter-chip {
  flex-shrink: 0;
  padding: 6px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 999px;
  background: #fff;
  color: #64748b;
  font-size: 13px;
  line-height: 1.2;
}

.filter-chip.active {
  border-color: #ff5f84;
  background: #fff1f5;
  color: #e11d48;
  font-weight: 600;
}
</style>
