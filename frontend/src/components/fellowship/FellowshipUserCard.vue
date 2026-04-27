<template>
  <section class="user-card">
    <div class="header-row">
      <button class="avatar-btn" type="button" @click="$emit('avatar-click')">
        <van-image v-if="avatar" round width="74" height="74" :src="avatar" fit="cover" />
        <div v-else class="avatar-fallback">{{ avatarInitial }}</div>
      </button>

      <div class="main-info">
        <p class="name-row">
          <span class="nickname">{{ nickname || '未设置昵称' }}</span>
          <span class="id">ID {{ userId || '-' }}</span>
        </p>
        <p class="meta">{{ ageText }} · {{ cityText }}</p>
        <div class="tag-row">
          <span class="verify-tag" :class="`v-${verifyStatus}`">{{ verifyText }}</span>
          <span class="online-tag">{{ online ? '在线' : '离线' }}</span>
        </div>
      </div>

      <van-button size="small" round plain color="#ff6b9b" @click="$emit('edit')">编辑资料</van-button>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  avatar: { type: String, default: '' },
  nickname: { type: String, default: '' },
  userId: { type: [String, Number], default: '' },
  age: { type: [String, Number], default: '' },
  city: { type: String, default: '' },
  verified: { type: [Boolean, String], default: false },
  online: { type: Boolean, default: true }
})

defineEmits(['avatar-click', 'edit'])

const avatarInitial = computed(() => String(props.nickname || '我').trim().slice(0, 1).toUpperCase())
const ageText = computed(() => (props.age ? `${props.age}岁` : '年龄未填'))
const cityText = computed(() => (props.city || '城市未填'))
const verifyStatus = computed(() => {
  if (props.verified === true || props.verified === 'APPROVED' || props.verified === 'approved') return 'approved'
  if (props.verified === 'PENDING' || props.verified === 'pending') return 'pending'
  return 'none'
})
const verifyText = computed(() => {
  if (verifyStatus.value === 'approved') return '已认证'
  if (verifyStatus.value === 'pending') return '审核中'
  return '未认证'
})
</script>

<style scoped>
.user-card {
  background: #fff;
  border-radius: 16px;
  padding: 14px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08);
}

.header-row {
  display: grid;
  grid-template-columns: 74px 1fr auto;
  gap: 12px;
  align-items: center;
}

.avatar-btn {
  border: 0;
  background: transparent;
  padding: 0;
  width: 74px;
  height: 74px;
}

.avatar-fallback {
  width: 74px;
  height: 74px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff7aa8, #ffb4cd);
  color: #fff;
  font-size: 30px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.main-info {
  min-width: 0;
}

.name-row {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.nickname {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
}

.id {
  font-size: 12px;
  color: #8b93a6;
}

.meta {
  margin: 4px 0 0;
  font-size: 13px;
  color: #666;
}

.tag-row {
  margin-top: 8px;
  display: flex;
  gap: 8px;
}

.verify-tag,
.online-tag {
  font-size: 12px;
  border-radius: 999px;
  padding: 2px 10px;
}

.verify-tag {
  border: 1px solid #e5e7eb;
  color: #6b7280;
  background: #f9fafb;
}

.v-approved {
  border-color: #bbf7d0;
  background: #ecfdf3;
  color: #15803d;
}

.v-pending {
  border-color: #fed7aa;
  background: #fff7ed;
  color: #c2410c;
}

.online-tag {
  border: 1px solid #ffd0df;
  background: #fff3f8;
  color: #ff4d8d;
}
</style>

