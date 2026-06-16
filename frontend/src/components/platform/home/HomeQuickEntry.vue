<template>
  <section class="card">
    <h3>快捷入口</h3>
    <div class="grid">
      <button
        v-for="item in entries"
        :key="item.type"
        type="button"
        :class="{ 'is-featured': item.type === featuredType }"
        @click="$emit('go', item.type)"
      >
        {{ item.label }}
      </button>
    </div>
  </section>
</template>

<script setup>
defineProps({
  entries: Array,
  featuredType: {
    type: String,
    default: ''
  }
})
defineEmits(['go'])
</script>

<style scoped>
.card {
  padding: 14px 16px;
  background: var(--lc-surface);
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius);
}

.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

button {
  height: 38px;
  border: 1px solid var(--lc-border);
  border-radius: 8px;
  background: var(--lc-surface);
  color: var(--lc-text);
  font-size: 13px;
  cursor: pointer;
}

button.is-featured {
  border-color: var(--lc-blue-border, #93c5fd);
  background: linear-gradient(135deg, var(--lc-blue-light, #eff6ff), var(--lc-surface));
  color: var(--lc-blue, #2563eb);
  font-weight: 700;
}

@media (max-width: 360px) {
  .grid {
    gap: 6px;
  }

  button {
    height: 36px;
    font-size: 12px;
  }
}
</style>
