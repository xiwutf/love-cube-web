<template>
  <section class="editor-card">
    <h2 class="editor-title">写下你的今日心声</h2>

    <div class="textarea-wrap">
      <textarea
        v-model.trim="form.content"
        maxlength="300"
        rows="4"
        placeholder="今天有什么值得感恩、被鼓励、想记录的事情？"
        class="editor-textarea"
      />
      <span class="char-counter">{{ form.content.length }}/300</span>
    </div>

    <div class="section-row">
      <span class="section-label">选择分类</span>
      <div class="category-chips">
        <button
          v-for="cat in categories"
          :key="cat.value"
          type="button"
          :class="['chip', { active: form.category === cat.value }]"
          :style="form.category === cat.value ? chipActiveStyle(cat) : {}"
          @click="form.category = cat.value"
        >
          <span class="chip-icon" aria-hidden="true">{{ cat.icon }}</span>{{ cat.label }}
        </button>
      </div>
    </div>

    <div class="anon-row">
      <label class="anon-toggle">
        <span class="toggle-track" :class="{ on: form.anonymous }" @click="form.anonymous = !form.anonymous">
          <span class="toggle-thumb"></span>
        </span>
        <span class="anon-label">匿名发布</span>
      </label>
      <span class="anon-hint">开启后将以"匿名用户"身份发布</span>
    </div>

    <p v-if="message" :class="['feedback', { error }]">{{ message }}</p>

    <button class="submit-btn" type="button" :disabled="submitting" @click="handleSubmit">
      <span class="submit-icon" aria-hidden="true">✈</span>
      {{ submitting ? '发布中...' : '发布心声' }}
    </button>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { createPositiveShare } from '@/api/positiveShare.js'

const emit = defineEmits(['published'])

const categories = [
  { value: '感恩', label: '感恩', icon: '♥', bgActive: '#fee2e2', colorActive: '#ef4444', borderActive: '#fca5a5' },
  { value: '鼓励', label: '鼓励', icon: '↑', bgActive: '#f3e8ff', colorActive: '#a855f7', borderActive: '#d8b4fe' },
  { value: '成长', label: '成长', icon: '🌿', bgActive: '#dcfce7', colorActive: '#16a34a', borderActive: '#86efac' },
  { value: '信仰', label: '信仰', icon: '✦', bgActive: '#dbeafe', colorActive: '#2563eb', borderActive: '#93c5fd' },
  { value: '生活', label: '生活', icon: '🏠', bgActive: '#fce7f3', colorActive: '#db2777', borderActive: '#f9a8d4' },
  { value: '学习', label: '学习', icon: '📖', bgActive: '#ffedd5', colorActive: '#ea580c', borderActive: '#fdba74' },
  { value: '工作', label: '工作', icon: '💼', bgActive: '#f1f5f9', colorActive: '#475569', borderActive: '#cbd5e1' },
  { value: '人际', label: '人际', icon: '👥', bgActive: '#ede9fe', colorActive: '#7c3aed', borderActive: '#c4b5fd' },
]

const submitting = ref(false)
const message = ref('')
const error = ref(false)
const form = reactive({
  content: '',
  category: '感恩',
  anonymous: false,
  publicVisible: true
})

function chipActiveStyle(cat) {
  return {
    background: cat.bgActive,
    color: cat.colorActive,
    borderColor: cat.borderActive,
  }
}

async function handleSubmit() {
  message.value = ''
  error.value = false
  if (!form.content) {
    error.value = true
    message.value = '请写下你的今日心声'
    return
  }
  submitting.value = true
  try {
    const created = await createPositiveShare({
      content: form.content,
      category: form.category,
      anonymous: form.anonymous,
      publicVisible: form.publicVisible
    })
    form.content = ''
    form.category = '感恩'
    form.anonymous = false
    message.value = created?.status === 'PENDING'
      ? '✓ 已提交审核，审核通过后将公开展示'
      : '✓ 发布成功，愿你的分享温暖更多人'
    emit('published', created)
  } catch (e) {
    error.value = true
    message.value = e?.message || '发布失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.editor-card {
  border: 1px solid #f0ebe5;
  border-radius: 16px;
  background: #fff;
  padding: 20px 22px;
}

.editor-title {
  margin: 0 0 14px;
  color: #1f2937;
  font-size: 17px;
  font-weight: 700;
}

.textarea-wrap {
  position: relative;
}

.editor-textarea {
  width: 100%;
  box-sizing: border-box;
  border: 1px solid #e5e0da;
  border-radius: 10px;
  padding: 12px 14px 28px;
  font: inherit;
  font-size: 14px;
  color: #374151;
  resize: none;
  line-height: 1.7;
  transition: border-color 0.15s;
  background: #fdfcfb;
}

.editor-textarea:focus {
  outline: none;
  border-color: #f97316;
  background: #fff;
}

.editor-textarea::placeholder {
  color: #b0a8a0;
}

.char-counter {
  position: absolute;
  bottom: 10px;
  right: 12px;
  color: #b0a8a0;
  font-size: 12px;
  pointer-events: none;
}

.section-row {
  margin-top: 14px;
}

.section-label {
  display: block;
  color: #374151;
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 8px;
}

.category-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: 1px solid #e5e0da;
  border-radius: 999px;
  background: #fafaf9;
  color: #6b7280;
  height: 28px;
  padding: 0 11px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
  white-space: nowrap;
}

.chip:hover {
  border-color: #d0c8c0;
  background: #f5f0eb;
  color: #374151;
}

.chip-icon {
  font-size: 13px;
  line-height: 1;
}

.anon-row {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.anon-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.toggle-track {
  position: relative;
  width: 36px;
  height: 20px;
  border-radius: 999px;
  background: #d1d5db;
  cursor: pointer;
  transition: background 0.2s;
  flex-shrink: 0;
}

.toggle-track.on {
  background: #f97316;
}

.toggle-thumb {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.15);
  transition: left 0.2s;
}

.toggle-track.on .toggle-thumb {
  left: 18px;
}

.anon-label {
  color: #374151;
  font-size: 13px;
  font-weight: 600;
}

.anon-hint {
  color: #9ca3af;
  font-size: 12px;
}

.feedback {
  margin: 10px 0 0;
  color: #16a34a;
  font-size: 13px;
  font-weight: 600;
}

.feedback.error {
  color: #dc2626;
}

.submit-btn {
  margin-top: 14px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 40px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #fb923c, #f97316);
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  padding: 0 22px;
  cursor: pointer;
  transition: opacity 0.15s, transform 0.1s;
  box-shadow: 0 2px 8px rgba(249, 115, 22, 0.35);
}

.submit-btn:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

.submit-btn:active {
  transform: translateY(0);
}

.submit-btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
  transform: none;
}

.submit-icon {
  font-size: 15px;
}

@media (max-width: 600px) {
  .editor-card {
    padding: 16px;
  }

  .submit-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
