<template>
  <section class="quick-card">
    <h3>快捷功能</h3>
    <div class="grid">
      <button
        v-for="item in items"
        :key="item.key"
        type="button"
        class="quick-item"
        @click="$emit('action', item)"
      >
        <div class="icon-box">
          <van-icon :name="item.icon" size="22" />
        </div>
        <span>{{ item.title }}</span>
      </button>
    </div>
  </section>
</template>

<script setup>
defineProps({
  items: { type: Array, default: () => [] }
})

defineEmits(['action'])
</script>

<style scoped>
.quick-card {
  margin-top: 12px;
  background: #fff;
  border-radius: 16px;
  padding: 14px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08);
}

.quick-card h3 {
  margin: 0 0 12px;
  font-size: 16px;
  color: #111827;
}

.grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.quick-item {
  border: 1px solid #f0f3f8;
  border-radius: 12px;
  background: #fff;
  padding: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
  text-align: left;
}

.icon-box {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: #fff3f8;
  color: #ff5d91;
  display: flex;
  align-items: center;
  justify-content: center;
}

.quick-item span {
  font-size: 14px;
  color: #374151;
}
</style>

