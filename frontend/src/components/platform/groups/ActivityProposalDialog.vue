<template>
  <div v-if="open" class="proposal-dialog-backdrop" @click.self="onClose">
    <div class="proposal-dialog" role="dialog" aria-labelledby="proposal-dialog-title">
      <header class="proposal-dialog-head">
        <h3 id="proposal-dialog-title">发起活动投稿</h3>
        <button type="button" class="close-btn" aria-label="关闭" @click="onClose">×</button>
      </header>

      <p class="proposal-hint">提交后由社区运营者审核，通过后将出现在团体活动列表。</p>

      <form class="proposal-form" @submit.prevent="onSubmit">
        <label class="field">
          <span>活动标题 *</span>
          <input v-model.trim="form.title" type="text" maxlength="200" required>
        </label>
        <label class="field">
          <span>活动简介</span>
          <textarea v-model.trim="form.description" rows="3" maxlength="2000" />
        </label>
        <div class="field-row">
          <label class="field">
            <span>开始时间 *</span>
            <input v-model="form.startTime" type="datetime-local" required>
          </label>
          <label class="field">
            <span>结束时间 *</span>
            <input v-model="form.endTime" type="datetime-local" required>
          </label>
        </div>
        <label class="field">
          <span>地点（可选）</span>
          <input v-model.trim="form.location" type="text" maxlength="200" placeholder="线上 / 具体地址">
        </label>

        <p v-if="error" class="form-error">{{ error }}</p>

        <div class="form-actions">
          <button type="button" class="btn ghost" @click="onClose">取消</button>
          <button type="submit" class="btn primary" :disabled="submitting">
            {{ submitting ? '提交中…' : '提交投稿' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { submitActivityProposal } from '@/api/groupActivityProposals.js'

const props = defineProps({
  open: { type: Boolean, default: false },
  groupId: { type: [String, Number], required: true }
})

const emit = defineEmits(['close', 'submitted'])

const form = reactive({
  title: '',
  description: '',
  startTime: '',
  endTime: '',
  location: ''
})

const submitting = ref(false)
const error = ref('')

watch(() => props.open, (visible) => {
  if (!visible) return
  error.value = ''
  form.title = ''
  form.description = ''
  form.startTime = ''
  form.endTime = ''
  form.location = ''
})

function toIsoLocal(value) {
  if (!value) return ''
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return ''
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

function onClose() {
  if (submitting.value) return
  emit('close')
}

async function onSubmit() {
  const startTime = toIsoLocal(form.startTime)
  const endTime = toIsoLocal(form.endTime)
  if (!startTime || !endTime) {
    error.value = '请填写有效的活动时间'
    return
  }
  submitting.value = true
  error.value = ''
  try {
    await submitActivityProposal(props.groupId, {
      title: form.title,
      description: form.description,
      startTime,
      endTime,
      location: form.location || undefined
    })
    emit('submitted')
    emit('close')
  } catch (err) {
    error.value = err?.message || '提交失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.proposal-dialog-backdrop {
  position: fixed;
  inset: 0;
  z-index: var(--lc-z-modal, 1000);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--lc-space-4);
  background: rgb(15 23 42 / 45%);
}

.proposal-dialog {
  width: min(100%, 520px);
  max-height: 90vh;
  overflow: auto;
  padding: var(--lc-space-5);
  border-radius: var(--lc-radius-lg);
  background: var(--lc-surface);
  box-shadow: var(--lc-shadow-lg);
}

.proposal-dialog-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--lc-space-3);
  margin-bottom: var(--lc-space-3);
}

.proposal-dialog-head h3 {
  margin: 0;
  font-size: var(--lc-text-lg);
}

.close-btn {
  border: none;
  background: transparent;
  font-size: 1.5rem;
  line-height: 1;
  color: var(--lc-text-muted);
  cursor: pointer;
}

.proposal-hint {
  margin: 0 0 var(--lc-space-4);
  color: var(--lc-text-muted);
  font-size: var(--lc-text-sm);
}

.proposal-form {
  display: flex;
  flex-direction: column;
  gap: var(--lc-space-3);
}

.field {
  display: flex;
  flex-direction: column;
  gap: var(--lc-space-1);
}

.field span {
  font-size: var(--lc-text-sm);
  color: var(--lc-text-muted);
}

.field input,
.field textarea {
  width: 100%;
  padding: var(--lc-space-2) var(--lc-space-3);
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius-md);
  font: inherit;
}

.field-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--lc-space-3);
}

.form-error {
  margin: 0;
  color: var(--lc-danger);
  font-size: var(--lc-text-sm);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--lc-space-2);
  margin-top: var(--lc-space-2);
}

@media (max-width: 560px) {
  .field-row {
    grid-template-columns: 1fr;
  }
}
</style>
