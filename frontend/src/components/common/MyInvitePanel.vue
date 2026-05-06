<template>
  <section class="my-invite-panel" aria-label="my invite">
    <div class="invite-main">
      <div>
        <p class="eyebrow">我的邀请</p>
        <h2>{{ inviteCode || '加载中...' }}</h2>
        <p class="count">已邀请 {{ inviteCount }} 人</p>
      </div>
      <div class="qr-wrap">
        <InviteQrCode :invite-code="inviteCode" :size="180" />
      </div>
    </div>

    <label class="link-row">
      <span>邀请链接</span>
      <input :value="resolvedInviteLink || '生成中...'" readonly>
    </label>

    <div class="actions">
      <button type="button" :disabled="!resolvedInviteLink" @click="copyLink">复制邀请链接</button>
      <slot name="extra" />
    </div>
    <p v-if="feedback" class="feedback" :class="{ 'is-error': feedbackError }">{{ feedback }}</p>
  </section>
</template>

<script setup>
import { computed, ref } from 'vue'
import InviteQrCode from './InviteQrCode.vue'
import { createInviteUrl } from '@/utils/inviteUrl.js'

const props = defineProps({
  inviteCode: { type: String, default: '' },
  inviteCount: { type: Number, default: 0 }
})

const resolvedInviteLink = computed(() => createInviteUrl(props.inviteCode))
const feedback = ref('')
const feedbackError = ref(false)

async function copyLink() {
  if (!resolvedInviteLink.value) return
  feedback.value = ''
  feedbackError.value = false
  try {
    if (navigator?.clipboard?.writeText) {
      await navigator.clipboard.writeText(resolvedInviteLink.value)
    } else {
      const input = document.createElement('input')
      input.value = resolvedInviteLink.value
      document.body.appendChild(input)
      input.select()
      document.execCommand('copy')
      document.body.removeChild(input)
    }
    feedback.value = '邀请链接已复制'
  } catch {
    feedback.value = '复制失败，请手动复制'
    feedbackError.value = true
  }
  window.setTimeout(() => {
    feedback.value = ''
  }, 2200)
}
</script>

<style scoped>
.my-invite-panel {
  display: grid;
  gap: 14px;
  border: 1px solid var(--lc-soft-alt);
  border-radius: 8px;
  background: var(--lc-surface);
  padding: 16px;
}

.invite-main {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 128px;
  gap: 16px;
  align-items: center;
}

.eyebrow,
.count,
.feedback {
  margin: 0;
  color: var(--lc-muted);
  font-size: 12px;
}

h2 {
  margin: 6px 0;
  color: var(--lc-text);
  font-size: 26px;
  font-weight: 900;
  letter-spacing: 0;
  word-break: break-all;
}

.qr-wrap {
  width: 128px;
}

.link-row {
  display: grid;
  gap: 7px;
  color: var(--lc-muted);
  font-size: 12px;
  font-weight: 700;
}

.link-row input {
  width: 100%;
  border: 1px solid var(--lc-border);
  border-radius: 8px;
  background: var(--lc-bg);
  color: var(--lc-text);
  font: inherit;
  padding: 10px 11px;
  outline: 0;
}

.actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.actions button {
  border: 0;
  border-radius: 8px;
  background: var(--lc-indigo);
  color: var(--lc-surface);
  cursor: pointer;
  font-size: 13px;
  font-weight: 800;
  padding: 10px 14px;
}

.actions button:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.feedback {
  color: var(--lc-emerald);
}

.feedback.is-error {
  color: var(--lc-red);
}
</style>
