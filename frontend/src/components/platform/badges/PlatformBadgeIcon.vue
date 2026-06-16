<template>
  <svg
    class="platform-badge-icon"
    :class="[`is-${type}`, { 'is-locked': locked }]"
    viewBox="0 0 40 40"
    aria-hidden="true"
  >
    <defs>
      <linearGradient :id="`${gid}-ring`" x1="6" y1="4" x2="34" y2="36" gradientUnits="userSpaceOnUse">
        <stop offset="0%" :stop-color="palette.ringHi" />
        <stop offset="52%" :stop-color="palette.ringMid" />
        <stop offset="100%" :stop-color="palette.ringLo" />
      </linearGradient>
      <linearGradient :id="`${gid}-face`" x1="10" y1="8" x2="30" y2="32" gradientUnits="userSpaceOnUse">
        <stop offset="0%" :stop-color="palette.faceHi" />
        <stop offset="100%" :stop-color="palette.faceLo" />
      </linearGradient>
      <radialGradient :id="`${gid}-glow`" cx="30%" cy="24%" r="68%">
        <stop offset="0%" stop-color="#ffffff" stop-opacity="0.55" />
        <stop offset="100%" stop-color="#ffffff" stop-opacity="0" />
      </radialGradient>
    </defs>

    <!-- 外圈 -->
    <circle cx="20" cy="20" r="18.5" :fill="`url(#${gid}-ring)`" />
    <circle cx="20" cy="20" r="15.5" fill="rgba(255,255,255,0.18)" />
    <!-- 内盘 -->
    <circle cx="20" cy="20" r="13.8" :fill="`url(#${gid}-face)`" />
    <circle cx="20" cy="20" r="13.8" :fill="`url(#${gid}-glow)`" />

    <!-- 符号 -->
    <g class="badge-glyph" :fill="palette.glyph">
      <path v-if="type === 'star'" d="M20 10.2l2.1 4.5 5 .45-3.8 3.4 1.1 4.9L20 20.8l-4.4 2.7 1.1-4.9-3.8-3.4 5-.45L20 10.2z" />
      <path
        v-else-if="type === 'shield'"
        d="M20 9.5L28.5 12.2v6.1c0 4.5-3.1 8.4-8.5 10-5.4-1.6-8.5-5.5-8.5-10v-6.1L20 9.5zm0 2.4-6.2 2v4.7c0 3.2 2.2 6 6.2 7.3 4-1.3 6.2-4.1 6.2-7.3v-4.7L20 11.9z"
      />
      <path
        v-else-if="type === 'heart'"
        d="M20 29.2S11.5 23.2 11.5 17c0-3.2 2.4-5.7 5.4-5.7 1.7 0 3.2.8 4.1 2.1.9-1.3 2.4-2.1 4.1-2.1 3 0 5.4 2.5 5.4 5.7 0 6.2-8.5 12.2-8.5 12.2H20z"
      />
      <g v-else>
        <path d="M13.5 24.8 12 30.5l4.8-3.2 3.2 3.8-.8-6.8z" opacity="0.9" />
        <path d="M26.5 24.8 28 30.5l-4.8-3.2-3.2 3.8.8-6.8z" opacity="0.9" />
        <circle cx="20" cy="17.8" r="6.6" />
        <path
          d="M20 13.8l1.1 2.4 2.6.3-1.9 1.7.5 2.6L20 19l-2.3 1.8.5-2.6-1.9-1.7 2.6-.3L20 13.8z"
          fill="#fff"
          opacity="0.95"
        />
      </g>
    </g>
  </svg>
</template>

<script setup>
import { computed, useId } from 'vue'

const props = defineProps({
  type: {
    type: String,
    default: 'star',
    validator: v => ['star', 'shield', 'heart', 'medal'].includes(v)
  },
  locked: {
    type: Boolean,
    default: false
  }
})

const gid = useId().replace(/[^a-zA-Z0-9_-]/g, '_')

const PALETTES = {
  star: {
    ringHi: '#FFE58A',
    ringMid: '#FBBF24',
    ringLo: '#B45309',
    faceHi: '#FFF7D6',
    faceLo: '#F59E0B',
    glyph: '#FFFFFF'
  },
  shield: {
    ringHi: '#BFDBFE',
    ringMid: '#3B82F6',
    ringLo: '#1D4ED8',
    faceHi: '#DBEAFE',
    faceLo: '#2563EB',
    glyph: '#FFFFFF'
  },
  heart: {
    ringHi: '#FBCFE8',
    ringMid: '#EC4899',
    ringLo: '#BE185D',
    faceHi: '#FCE7F3',
    faceLo: '#DB2777',
    glyph: '#FFFFFF'
  },
  medal: {
    ringHi: '#DDD6FE',
    ringMid: '#8B5CF6',
    ringLo: '#5B21B6',
    faceHi: '#EDE9FE',
    faceLo: '#7C3AED',
    glyph: '#FFFFFF'
  }
}

const palette = computed(() => PALETTES[props.type] || PALETTES.star)
</script>

<style scoped>
.platform-badge-icon {
  display: block;
  width: 100%;
  height: 100%;
  filter: drop-shadow(0 2px 4px rgba(15, 23, 42, 0.22));
}

.badge-glyph {
  filter: drop-shadow(0 1px 0 rgba(255, 255, 255, 0.35));
}

.platform-badge-icon.is-locked {
  filter: grayscale(0.85) saturate(0.5) drop-shadow(0 1px 2px rgba(15, 23, 42, 0.12));
  opacity: 0.55;
}
</style>
