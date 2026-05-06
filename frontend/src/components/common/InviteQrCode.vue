<template>
  <div class="invite-qr" aria-label="invite QR code">
    <img v-if="qrSrc" :src="qrSrc" alt="Invite registration QR code" />
    <span v-else>{{ loadingText }}</span>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import QRCode from 'qrcode'
import { createInviteUrl } from '@/utils/inviteUrl.js'

const props = defineProps({
  inviteCode: { type: String, default: '' },
  size: { type: Number, default: 168 },
  loadingText: { type: String, default: 'Generating...' }
})

const qrSrc = ref('')

watch(
  () => [props.inviteCode, props.size],
  async ([inviteCode, size]) => {
    const value = createInviteUrl(inviteCode)
    if (!value) {
      qrSrc.value = ''
      return
    }
    qrSrc.value = await QRCode.toDataURL(value, {
      width: size,
      margin: 1,
      errorCorrectionLevel: 'M',
      color: {
        dark: '#0f172a',
        light: '#ffffff'
      }
    })
  },
  { immediate: true }
)
</script>

<style scoped>
.invite-qr {
  display: grid;
  place-items: center;
  width: 100%;
  aspect-ratio: 1;
  border: 1px solid var(--lc-border);
  border-radius: 8px;
  background: var(--lc-surface);
  overflow: hidden;
  color: var(--lc-muted);
  font-size: 12px;
}

.invite-qr img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: contain;
}
</style>
