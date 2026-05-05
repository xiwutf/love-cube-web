<template>
  <div class="admin-pagination">
    <label class="admin-page-size">
      每页
      <select
        :value="pageSize"
        class="admin-select"
        :disabled="disabled"
        @change="onPageSizeChange($event)"
      >
        <option v-for="size in pageSizeOptions" :key="size" :value="size">{{ size }}</option>
      </select>
      条
    </label>
    <div class="admin-page-actions">
      <button class="admin-btn" type="button" :disabled="disabled || currentPage <= 1" @click="goPrev">
        上一页
      </button>
      <span class="admin-page-indicator">{{ currentPage }} / {{ totalPages }}</span>
      <button class="admin-btn" type="button" :disabled="disabled || currentPage >= totalPages" @click="goNext">
        下一页
      </button>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  currentPage: { type: Number, default: 1 },
  totalPages: { type: Number, default: 1 },
  pageSize: { type: Number, default: 10 },
  pageSizeOptions: { type: Array, default: () => [5, 10, 20] },
  disabled: { type: Boolean, default: false }
})

const emit = defineEmits(['update:currentPage', 'update:pageSize'])

function onPageSizeChange(event) {
  const value = Number(event?.target?.value || props.pageSize)
  emit('update:pageSize', value)
}

function goPrev() {
  if (props.currentPage <= 1) return
  emit('update:currentPage', props.currentPage - 1)
}

function goNext() {
  if (props.currentPage >= props.totalPages) return
  emit('update:currentPage', props.currentPage + 1)
}
</script>

<style scoped>
.admin-pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.admin-page-size {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--lc-muted-light);
  font-size: 13px;
}

.admin-page-actions {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.admin-page-indicator {
  min-width: 56px;
  text-align: center;
  color: var(--lc-slate);
  font-weight: 600;
  font-size: 13px;
}
</style>
