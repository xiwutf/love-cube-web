<template>
  <section class="progress-card">
    <div class="title-row">
      <span>资料完整度</span>
      <strong>{{ safePercent }}%</strong>
    </div>
    <van-progress :percentage="safePercent" color="linear-gradient(90deg,#ff77a8,#ff5d91)" stroke-width="8" />
    <p class="tips">{{ tipText }}</p>
    <div class="action-row">
      <span class="hint">完善资料可提升匹配成功率</span>
      <van-button size="small" round color="#ff6b9b" @click="$emit('improve')">立即完善</van-button>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  percent: { type: Number, default: 0 },
  missingFields: { type: Array, default: () => [] }
})

defineEmits(['improve'])

const safePercent = computed(() => Math.max(0, Math.min(100, Number(props.percent || 0))))
const tipText = computed(() => {
  if (!props.missingFields?.length) return '资料信息完整，继续保持'
  return `还可完善 ${props.missingFields.length} 项关键信息`
})
</script>

<style scoped>
.progress-card {
  margin-top: 10px;
  background: #fff;
  border-radius: 16px;
  padding: 12px 14px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08);
}

.title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  color: #374151;
  font-size: 14px;
}

.title-row strong {
  font-size: 18px;
  color: #ff5d91;
}

.tips {
  margin: 8px 0 0;
  font-size: 12px;
  color: #8b93a6;
}

.action-row {
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.hint {
  font-size: 12px;
  color: #6b7280;
}
</style>

