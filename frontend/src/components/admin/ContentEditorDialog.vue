<template>
  <div v-if="visible" class="content-editor-mask" @click.self="handleClose">
    <section class="content-editor-panel">
      <header class="content-editor-head">
        <h3>{{ title || '正文编辑' }}</h3>
        <button class="admin-btn" type="button" :disabled="isBusy" @click="handleClose">关闭</button>
      </header>
      <textarea
        :value="draftContent"
        class="admin-textarea content-editor-textarea"
        :placeholder="placeholder || '请输入正文'"
        @input="updateDraft($event)"
      ></textarea>
      <p class="content-meta">字数：{{ contentLength }}<span v-if="dirty">（有未保存修改）</span></p>
      <p class="shortcut-meta">快捷键：Esc 关闭，Ctrl/Cmd + Enter 应用并保存</p>
      <div class="content-editor-actions">
        <button class="admin-btn" type="button" :disabled="isBusy" @click="handleClose">取消</button>
        <button class="admin-btn primary" type="button" :disabled="isBusy" @click="emitApply">应用</button>
        <button class="admin-btn primary" type="button" :disabled="isBusy" @click="emitSave">应用并保存</button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { showConfirmDialog } from 'vant'

const props = defineProps({
  visible: { type: Boolean, default: false },
  title: { type: String, default: '' },
  content: { type: String, default: '' },
  placeholder: { type: String, default: '' },
  loading: { type: Boolean, default: false }
})

const emit = defineEmits(['update:visible', 'apply', 'save'])
const draftContent = ref('')
const originalContent = ref('')
const submitting = ref(false)
const dirty = computed(() => draftContent.value !== originalContent.value)
const contentLength = computed(() => String(draftContent.value || '').length)
const isBusy = computed(() => props.loading || submitting.value)

watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      const text = String(props.content || '')
      draftContent.value = text
      originalContent.value = text
    }
  }
)

function requestClose() {
  if (isBusy.value) return
  emit('update:visible', false)
}

function updateDraft(event) {
  draftContent.value = event?.target?.value || ''
}

async function handleClose() {
  if (isBusy.value) return
  if (dirty.value) {
    try {
      await showConfirmDialog({
        title: '未保存修改',
        message: '当前有未保存修改，确认关闭吗？',
        confirmButtonText: '确认关闭',
        cancelButtonText: '继续编辑'
      })
    } catch {
      return
    }
  }
  requestClose()
}

function emitApply() {
  if (isBusy.value) return
  emit('apply', draftContent.value)
}

function emitSave() {
  if (isBusy.value) return
  submitting.value = true
  emit('save', draftContent.value)
}

function onKeydown(event) {
  if (!props.visible) return
  if (event.key === 'Escape') {
    event.preventDefault()
    void handleClose()
    return
  }
  if (event.key === 'Enter' && (event.ctrlKey || event.metaKey)) {
    event.preventDefault()
    emitSave()
  }
}

onMounted(() => window.addEventListener('keydown', onKeydown))
onBeforeUnmount(() => window.removeEventListener('keydown', onKeydown))

watch(
  () => props.loading,
  (loading) => {
    if (!loading) submitting.value = false
  }
)
</script>

<style scoped>
.content-editor-mask {
  position: fixed;
  inset: 0;
  z-index: 1200;
  background: rgba(15, 23, 42, 0.45);
  display: grid;
  place-items: center;
  padding: 24px;
}

.content-editor-panel {
  width: min(920px, 100%);
  max-height: calc(100vh - 48px);
  background: var(--lc-surface);
  border-radius: 12px;
  border: 1px solid var(--lc-border);
  padding: 16px;
  display: grid;
  gap: 12px;
}

.content-editor-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.content-editor-head h3 {
  margin: 0;
  font-size: 18px;
  color: var(--lc-text);
}

.content-editor-textarea {
  min-height: 380px;
  resize: vertical;
}

.content-editor-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.content-meta {
  margin: -4px 0 0;
  font-size: 12px;
  color: var(--lc-subtle);
}

.shortcut-meta {
  margin: -8px 0 0;
  font-size: 12px;
  color: var(--lc-muted);
}
</style>
