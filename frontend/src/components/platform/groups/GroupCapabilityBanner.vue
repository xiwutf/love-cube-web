<template>
  <div v-if="show" class="group-capability-banner" role="status">
    <p>
      此团体为文字编号（历史数据），线上投票可用；邀请码、赛季榜、活动签到互评、周报等功能仅支持数字编号团体。
    </p>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { isNumericPlatformGroupId } from '@/api/groups.js'

const props = defineProps({
  groupId: { type: [String, Number], default: null }
})

const show = computed(() => {
  const id = props.groupId
  if (id === null || id === undefined || id === '') return false
  return !isNumericPlatformGroupId(id)
})
</script>

<style scoped>
.group-capability-banner {
  margin: var(--lc-space-3) 0;
  padding: var(--lc-space-3) var(--lc-space-4);
  border-radius: var(--lc-radius-md);
  background: var(--lc-surface-muted, #f8fafc);
  border: 1px solid var(--lc-border, #e2e8f0);
}

.group-capability-banner p {
  margin: 0;
  font-size: 0.9rem;
  line-height: 1.5;
  color: var(--lc-text-muted, #64748b);
}
</style>
