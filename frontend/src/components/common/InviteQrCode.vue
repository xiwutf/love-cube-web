<template>
  <div class="invite-qr-frame" aria-label="invite QR code">
    <span class="corner corner-tl" aria-hidden="true"></span>
    <span class="corner corner-tr" aria-hidden="true"></span>
    <span class="corner corner-bl" aria-hidden="true"></span>
    <span class="corner corner-br" aria-hidden="true"></span>
    <div class="invite-qr">
      <img v-if="qrSrc" :src="qrSrc" alt="Invite registration QR code" />
      <span v-else>{{ loadingText }}</span>
    </div>
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
      margin: 2,
      errorCorrectionLevel: 'H',
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
.invite-qr-frame {
  position: relative;
  display: grid;
  place-items: center;
  width: 100%;
  aspect-ratio: 1;
  border: 1px solid rgba(99, 102, 241, 0.18);
  border-radius: 8px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(248, 250, 252, 0.92)),
    radial-gradient(circle at 50% 0%, rgba(139, 92, 246, 0.16), transparent 58%);
  box-shadow: 0 14px 32px rgba(15, 23, 42, 0.11);
  padding: 10px;
}

.invite-qr {
  position: relative;
  z-index: 1;
  display: grid;
  place-items: center;
  width: 100%;
  aspect-ratio: 1;
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 8px;
  background: #ffffff;
  overflow: hidden;
  color: var(--lc-muted);
  font-size: 12px;
  box-shadow: inset 0 0 0 6px #ffffff;
}

.invite-qr img {
  display: block;
  width: calc(100% - 12px);
  height: calc(100% - 12px);
  object-fit: contain;
}

.corner {
  position: absolute;
  z-index: 2;
  width: 22px;
  height: 22px;
  border-color: rgba(99, 102, 241, 0.72);
  pointer-events: none;
}

.corner-tl {
  top: 8px;
  left: 8px;
  border-top: 2px solid;
  border-left: 2px solid;
  border-top-left-radius: 7px;
}

.corner-tr {
  top: 8px;
  right: 8px;
  border-top: 2px solid;
  border-right: 2px solid;
  border-top-right-radius: 7px;
}

.corner-bl {
  bottom: 8px;
  left: 8px;
  border-bottom: 2px solid;
  border-left: 2px solid;
  border-bottom-left-radius: 7px;
}

.corner-br {
  right: 8px;
  bottom: 8px;
  border-right: 2px solid;
  border-bottom: 2px solid;
  border-bottom-right-radius: 7px;
}
</style>
