<template>
  <div class="user-card" @click="$emit('click', user)">
    <van-image round :width="avatarSize" :height="avatarSize" :src="user.avatar" fit="cover">
      <template #error>
        <div class="avatar-fallback" :style="{ width: avatarSize + 'px', height: avatarSize + 'px' }">
          {{ (user.nickname || '?')[0] }}
        </div>
      </template>
    </van-image>
    <div class="card-info">
      <p class="name">{{ user.nickname }}</p>
      <p class="meta">
        <span v-if="user.age">{{ user.age }}岁</span>
        <span v-if="user.age && user.location"> · </span>
        <span v-if="user.location">{{ user.location }}</span>
      </p>
    </div>
    <slot name="action" />
  </div>
</template>

<script setup>
defineProps({
  user:       { type: Object,  required: true },
  avatarSize: { type: Number,  default: 48 },
})
defineEmits(['click'])
</script>

<style scoped>
.user-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  background: #fff;
  cursor: pointer;
}
.card-info { flex: 1; overflow: hidden; }
.name { font-size: 14px; font-weight: 500; color: #333; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.meta { font-size: 12px; color: #999; margin-top: 3px; }
.avatar-fallback {
  border-radius: 50%;
  background: linear-gradient(135deg, #FF6B8A, #FFB3C1);
  display: flex; align-items: center; justify-content: center;
  font-size: 16px; color: #fff; font-weight: 700;
}
</style>
