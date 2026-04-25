<template>
  <section class="wall-card">
    <div class="wall-header">
      <div>
        <h3>我的生活照</h3>
        <p>真实生活照更受欢迎</p>
      </div>
      <span class="count">{{ photos.length }}/9</span>
    </div>

    <div class="grid">
      <div v-for="(photo, idx) in photos" :key="photo.id || photo.url || idx" class="photo-item">
        <van-image :src="photo.url || photo" width="100%" height="100%" fit="cover" radius="12" />
        <div class="mask-actions">
          <button type="button" class="mini-btn" @click="$emit('set-primary', idx)">设为主图</button>
          <button type="button" class="mini-btn danger" @click="$emit('delete-photo', photo, idx)">删除</button>
        </div>
      </div>

      <button
        v-for="i in Math.max(0, 9 - photos.length)"
        :key="`empty-${i}`"
        type="button"
        class="photo-item empty-item"
        @click="$emit('upload-photo')"
      >
        <van-icon name="plus" size="24" />
        <span>上传照片</span>
      </button>
    </div>
  </section>
</template>

<script setup>
defineProps({
  photos: { type: Array, default: () => [] }
})

defineEmits(['upload-photo', 'delete-photo', 'set-primary'])
</script>

<style scoped>
.wall-card {
  margin-top: 12px;
  background: #fff;
  border-radius: 16px;
  padding: 14px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08);
}

.wall-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 12px;
}

.wall-header h3 {
  margin: 0;
  font-size: 16px;
  color: #111827;
}

.wall-header p {
  margin: 4px 0 0;
  font-size: 12px;
  color: #8b93a6;
}

.count {
  font-size: 12px;
  color: #ff5d91;
}

.grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.photo-item {
  position: relative;
  aspect-ratio: 1 / 1;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #f1f4f9;
}

.mask-actions {
  position: absolute;
  inset: auto 0 0 0;
  padding: 6px;
  display: flex;
  gap: 6px;
  background: linear-gradient(180deg, transparent, rgba(0, 0, 0, 0.65));
}

.mini-btn {
  border: 0;
  border-radius: 999px;
  padding: 2px 8px;
  font-size: 11px;
  color: #fff;
  background: rgba(255, 255, 255, 0.28);
}

.mini-btn.danger {
  background: rgba(239, 68, 68, 0.7);
}

.empty-item {
  border: 1px dashed #ffc6d9;
  color: #ff5d91;
  background: #fff7fb;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.empty-item span {
  font-size: 12px;
}
</style>

