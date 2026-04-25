<template>
  <section class="settings-card">
    <button
      v-for="item in items"
      :key="item.key"
      type="button"
      class="setting-item"
      :class="{ danger: item.danger }"
      @click="$emit('action', item)"
    >
      <span>{{ item.title }}</span>
      <div class="right">
        <span v-if="item.value" class="value">{{ item.value }}</span>
        <van-icon name="arrow" size="14" />
      </div>
    </button>
  </section>
</template>

<script setup>
defineProps({
  items: { type: Array, default: () => [] }
})

defineEmits(['action'])
</script>

<style scoped>
.settings-card {
  margin-top: 12px;
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08);
}

.setting-item {
  width: 100%;
  border: 0;
  border-bottom: 1px solid #f2f3f7;
  background: #fff;
  padding: 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #374151;
  font-size: 14px;
}

.setting-item:last-child {
  border-bottom: none;
}

.setting-item.danger {
  color: #ef4444;
}

.right {
  display: flex;
  align-items: center;
  gap: 6px;
}

.value {
  color: #9ca3af;
  font-size: 12px;
}
</style>

