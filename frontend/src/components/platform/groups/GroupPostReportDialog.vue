<template>
  <div v-if="open" class="report-overlay" role="dialog" aria-modal="true" @click.self="close">
    <div class="report-dialog">
      <h2>举报动态</h2>
      <p class="hint">处理结果将通过消息中心通知你</p>
      <label>
        <span>举报原因</span>
        <select v-model="reasonType" class="report-select">
          <option v-for="r in REASONS" :key="r" :value="r">{{ r }}</option>
        </select>
      </label>
      <label>
        <span>补充说明（可选）</span>
        <textarea v-model.trim="content" rows="3" maxlength="500" placeholder="简要描述问题…" />
      </label>
      <div class="actions">
        <button type="button" class="btn ghost" @click="close">取消</button>
        <button type="button" class="btn primary" :disabled="submitting" @click="submit">
          {{ submitting ? '提交中…' : '提交举报' }}
        </button>
      </div>
      <p v-if="error" class="err">{{ error }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { submitReport } from '@/api/reports.js'

const REASONS = ['骚扰', '虚假资料', '广告推销', '诈骗', '色情内容', '其他']

const props = defineProps({
  open: { type: Boolean, default: false },
  postId: { type: [String, Number], default: null },
  targetUserId: { type: [String, Number], default: null }
})

const emit = defineEmits(['close', 'submitted'])

const reasonType = ref('其他')
const content = ref('')
const submitting = ref(false)
const error = ref('')

watch(() => props.open, (v) => {
  if (v) {
    reasonType.value = '其他'
    content.value = ''
    error.value = ''
  }
})

function close() {
  emit('close')
}

async function submit() {
  if (!props.postId) return
  submitting.value = true
  error.value = ''
  try {
    await submitReport({
      targetType: 'GROUP_POST',
      targetId: String(props.postId),
      targetUserId: props.targetUserId ? Number(props.targetUserId) : null,
      reasonType: reasonType.value,
      content: content.value || null,
      alsoBlock: false
    })
    emit('submitted')
    close()
  } catch (e) {
    error.value = e?.message || '提交失败，请重试'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.report-overlay {
  position: fixed;
  inset: 0;
  z-index: var(--lc-z-modal, 1000);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--lc-space-4);
  background: rgb(0 0 0 / 45%);
}

.report-dialog {
  width: min(420px, 100%);
  padding: var(--lc-space-4);
  border-radius: var(--lc-radius-lg);
  background: var(--lc-surface);
  box-shadow: var(--lc-shadow-lg);
}

.report-dialog h2 {
  margin: 0 0 var(--lc-space-2);
  font-size: 1.1rem;
}

.hint {
  margin: 0 0 var(--lc-space-3);
  font-size: 0.85rem;
  color: var(--lc-text-muted);
}

label {
  display: flex;
  flex-direction: column;
  gap: var(--lc-space-1);
  margin-bottom: var(--lc-space-3);
  font-size: 0.85rem;
}

.report-select,
textarea {
  padding: var(--lc-space-2);
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius-md);
  font: inherit;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--lc-space-2);
}

.btn {
  padding: var(--lc-space-2) var(--lc-space-3);
  border-radius: var(--lc-radius-md);
  border: 1px solid var(--lc-border);
  cursor: pointer;
  font: inherit;
}

.btn.primary {
  background: var(--lc-blue);
  border-color: var(--lc-blue);
  color: #fff;
}

.btn.ghost {
  background: transparent;
}

.err {
  margin: var(--lc-space-2) 0 0;
  color: var(--lc-danger, #c0392b);
  font-size: 0.85rem;
}
</style>
