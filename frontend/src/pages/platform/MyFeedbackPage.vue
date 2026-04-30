<template>
  <section class="sp-page">
    <header class="sp-head">
      <button type="button" class="sp-back" aria-label="返回" @click="$router.back()">‹</button>
      <h1 class="sp-title">意见反馈</h1>
    </header>

    <div class="sp-body">
      <div v-if="submitted" class="sp-card fb-success">
        <div class="fb-success-icon">✅</div>
        <div class="fb-success-title">感谢您的反馈！</div>
        <div class="fb-success-sub">我们会认真阅读每一条建议，尽快改进产品体验。</div>
        <button type="button" class="fb-again-btn" @click="submitted = false">再次反馈</button>
      </div>

      <template v-else>
        <div class="sp-card">
          <div class="sp-card-title">反馈类型</div>
          <div class="fb-type-grid">
            <button
              v-for="t in typeOptions"
              :key="t.value"
              type="button"
              class="fb-type-btn"
              :class="{ active: form.type === t.value }"
              @click="form.type = t.value"
            >
              <span>{{ t.icon }}</span>
              <span>{{ t.label }}</span>
            </button>
          </div>
        </div>

        <div class="sp-card">
          <div class="sp-card-title">反馈内容 <span class="fb-required">*</span></div>
          <textarea
            v-model.trim="form.content"
            class="fb-textarea"
            :placeholder="contentPlaceholder"
            maxlength="500"
            rows="5"
          ></textarea>
          <div class="fb-count">{{ form.content.length }}/500</div>
        </div>

        <div class="sp-card">
          <div class="sp-card-title">联系方式（选填）</div>
          <input
            v-model.trim="form.contact"
            class="fb-input"
            placeholder="手机号或邮箱，方便我们回复您"
            maxlength="100"
          />
        </div>

        <p v-if="errMsg" class="fb-err">{{ errMsg }}</p>

        <button
          type="button"
          class="sp-primary-btn"
          :disabled="submitting"
          @click="handleSubmit"
        >
          {{ submitting ? '提交中...' : '提交反馈' }}
        </button>
      </template>
    </div>
  </section>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { submitFeedback } from '@/api/feedback.js'

const submitted = ref(false)
const submitting = ref(false)
const errMsg = ref('')

const form = reactive({ type: 'suggestion', content: '', contact: '' })

const typeOptions = [
  { value: 'bug', icon: '🐛', label: '功能异常' },
  { value: 'suggestion', icon: '💡', label: '功能建议' },
  { value: 'content', icon: '📝', label: '内容问题' },
  { value: 'account', icon: '👤', label: '账号问题' },
  { value: 'other', icon: '💬', label: '其他' }
]

const contentPlaceholder = computed(() => {
  const map = {
    bug: '请描述遇到的问题，如操作步骤、出现的错误信息等...',
    suggestion: '请描述您希望增加或改进的功能...',
    content: '请描述遇到的内容问题...',
    account: '请描述账号相关的问题...',
    other: '请输入您的反馈内容...'
  }
  return map[form.type] || '请输入您的反馈内容...'
})

async function handleSubmit() {
  errMsg.value = ''
  if (!form.content) {
    errMsg.value = '请填写反馈内容'
    return
  }
  submitting.value = true
  try {
    await submitFeedback({ type: form.type, content: form.content, contact: form.contact })
    submitted.value = true
    form.content = ''
    form.contact = ''
  } catch (e) {
    errMsg.value = e?.message || '提交失败，请稍后再试'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.sp-page { min-height: 100vh; background: #f6f7fb; color: #0f172a; }

.sp-head {
  position: sticky; top: 0; z-index: 10;
  display: flex; align-items: center;
  background: #fff; border-bottom: 1px solid #eef0f4;
}
.sp-back {
  width: 48px; height: 52px; flex: 0 0 auto;
  display: grid; place-items: center;
  border: 0; background: none; font-size: 22px; color: #4f46e5; cursor: pointer;
}
.sp-title { flex: 1; margin: 0; font-size: 17px; font-weight: 800; }

.sp-body {
  padding: 16px 14px calc(80px + env(safe-area-inset-bottom));
  max-width: 680px; margin: 0 auto;
}
.sp-card {
  background: #fff; border: 1px solid #eef0f4; border-radius: 18px;
  box-shadow: 0 3px 12px rgba(15,23,42,0.04); margin-bottom: 14px; padding: 16px;
}
.sp-card-title { font-size: 15px; font-weight: 800; margin-bottom: 14px; color: #0f172a; }
.fb-required { color: #dc2626; }

.fb-type-grid {
  display: grid; grid-template-columns: repeat(5, 1fr); gap: 8px;
}
.fb-type-btn {
  display: flex; flex-direction: column; align-items: center; gap: 5px;
  padding: 10px 4px; border-radius: 12px;
  border: 1.5px solid #eef0f4; background: #f8fafc;
  font-size: 11px; color: #64748b; cursor: pointer; transition: all 0.15s;
}
.fb-type-btn span:first-child { font-size: 20px; }
.fb-type-btn.active { border-color: #6d5dfb; background: #f1efff; color: #4f46e5; font-weight: 700; }

.fb-textarea {
  width: 100%; box-sizing: border-box;
  border: 1px solid #eef0f4; border-radius: 12px;
  padding: 12px 14px; font: inherit; font-size: 14px;
  color: #0f172a; background: #f8fafc; resize: none; outline: none; line-height: 1.6;
}
.fb-textarea:focus { border-color: #a5b4fc; background: #fff; }
.fb-count { text-align: right; font-size: 11px; color: #94a3b8; margin-top: 6px; }

.fb-input {
  width: 100%; box-sizing: border-box;
  border: 1px solid #eef0f4; border-radius: 12px;
  padding: 12px 14px; font: inherit; font-size: 14px;
  color: #0f172a; background: #f8fafc; outline: none;
}
.fb-input:focus { border-color: #a5b4fc; background: #fff; }

.fb-err { color: #dc2626; font-size: 13px; margin: -6px 0 10px; text-align: center; }

.sp-primary-btn {
  display: block; width: 100%;
  padding: 14px; border: 0; border-radius: 14px;
  background: linear-gradient(135deg, #6d5dfb, #4f46e5);
  color: #fff; font-size: 15px; font-weight: 800; cursor: pointer;
  box-shadow: 0 8px 20px rgba(79,70,229,0.25);
}
.sp-primary-btn:disabled { opacity: 0.6; cursor: default; }

.fb-success {
  text-align: center; padding: 36px 24px;
}
.fb-success-icon { font-size: 48px; margin-bottom: 14px; }
.fb-success-title { font-size: 18px; font-weight: 800; margin-bottom: 8px; }
.fb-success-sub { font-size: 14px; color: #64748b; margin-bottom: 24px; line-height: 1.6; }
.fb-again-btn {
  border: 1.5px solid #6d5dfb; border-radius: 999px;
  background: #fff; color: #4f46e5;
  font-size: 14px; font-weight: 700; padding: 8px 24px; cursor: pointer;
}

@media (max-width: 767px) {
  :global(.platform-header), :global(.platform-footer), :global(.co-creation-toolbar) {
    display: none !important;
  }
}
@media (min-width: 768px) {
  .sp-body { padding-top: 24px; padding-bottom: 48px; }
}
</style>
