<template>
  <div class="onsite-qr-frame">
    <div class="onsite-qr">
      <img v-if="qrSrc" :src="qrSrc" alt="现场加入活动二维码" />
      <span v-else>{{ loadingText }}</span>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import QRCode from 'qrcode'

const props = defineProps({
  url: { type: String, default: '' },
  size: { type: Number, default: 180 },
  loadingText: { type: String, default: '生成中…' }
})

const qrSrc = ref('')

watch(
  () => [props.url, props.size],
  async ([url, size]) => {
    if (!url) {
      qrSrc.value = ''
      return
    }
    qrSrc.value = await QRCode.toDataURL(url, {
      width: size,
      margin: 2,
      errorCorrectionLevel: 'H',
      color: { dark: '#0f172a', light: '#ffffff' }
    })
  },
  { immediate: true }
)
</script>

<style scoped>
.onsite-qr-frame {
  display: grid;
  place-items: center;
  padding: 12px;
  border: 1px solid var(--lc-border, #e2e8f0);
  border-radius: 12px;
  background: #fff;
}

.onsite-qr img {
  display: block;
  width: 100%;
  max-width: 200px;
  height: auto;
}
</style>
