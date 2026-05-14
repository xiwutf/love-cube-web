<template>
  <router-link class="mh-badge-row" to="/me/badges" aria-label="我的徽章">
    <div class="mh-badge-icons">
      <span
        v-for="(key, index) in iconKeys"
        :key="`${key}-${index}`"
        class="mh-badge-icon"
      >
          <!-- 星形 -->
          <svg
            v-if="key === 'star'"
            class="mh-badge-svg"
            viewBox="0 0 24 24"
            aria-hidden="true"
          >
            <defs>
              <linearGradient :id="`${uid}-st-${index}`" x1="5" y1="3" x2="19" y2="21" gradientUnits="userSpaceOnUse">
                <stop offset="0%" stop-color="#FDE047" />
                <stop offset="45%" stop-color="#EAB308" />
                <stop offset="100%" stop-color="#A16207" />
              </linearGradient>
            </defs>
            <path
              fill="#422006"
              d="M12 2.2l2.95 6.9 7.45.65-5.7 5.1 1.7 7.25L12 18.05 5.6 22.1l1.7-7.25-5.7-5.1 7.45-.65L12 2.2z"
            />
            <path
              :fill="`url(#${uid}-st-${index})`"
              stroke="#854D0E"
              stroke-width="0.35"
              stroke-linejoin="round"
              d="M12 4.1l2.38 5.55 6.05.52-4.6 4.14 1.39 5.92L12 16.65 6.38 20.23l1.39-5.92-4.6-4.14 6.05-.52L12 4.1z"
            />
            <path
              fill="#FFFBEB"
              d="M12 5.4l-.28 2.55-2.35.2 1.78 1.6-.52 2.3L12 11.35l2.04 1.25-.52-2.3 1.78-1.6-2.35-.2L12 5.4z"
            />
          </svg>
          <!-- 盾牌 -->
          <svg
            v-else-if="key === 'shield'"
            class="mh-badge-svg"
            viewBox="0 0 24 24"
            aria-hidden="true"
          >
            <defs>
              <linearGradient :id="`${uid}-sh-${index}`" x1="4" y1="2" x2="20" y2="22" gradientUnits="userSpaceOnUse">
                <stop offset="0%" stop-color="#93C5FD" />
                <stop offset="42%" stop-color="#2563EB" />
                <stop offset="100%" stop-color="#1E3A8A" />
              </linearGradient>
            </defs>
            <path
              fill="#0F172A"
              d="M12 2.4L20.2 5.4v6.6c0 5.1-3.4 9.35-8.2 11.1-4.8-1.75-8.2-6-8.2-11.1V5.4L12 2.4z"
            />
            <path
              :fill="`url(#${uid}-sh-${index})`"
              stroke="#1E40AF"
              stroke-width="0.35"
              stroke-linejoin="round"
              d="M12 3.6L19 6.05v5.85c0 4.35-2.85 8-7 9.65-4.15-1.65-7-5.3-7-9.65V6.05L12 3.6z"
            />
            <path
              fill="#EFF6FF"
              d="M12 6.35c1.45.95 2.5 2.55 2.72 4.45H9.28c.22-1.9 1.27-3.5 2.72-4.45z"
            />
          </svg>
          <!-- 心形 -->
          <svg
            v-else-if="key === 'heart'"
            class="mh-badge-svg"
            viewBox="0 0 24 24"
            aria-hidden="true"
          >
            <defs>
              <linearGradient :id="`${uid}-ht-${index}`" x1="3" y1="3" x2="21" y2="21" gradientUnits="userSpaceOnUse">
                <stop offset="0%" stop-color="#F9A8D4" />
                <stop offset="45%" stop-color="#DB2777" />
                <stop offset="100%" stop-color="#831843" />
              </linearGradient>
            </defs>
            <path
              fill="#500724"
              d="M12 21.2S3.2 14.4 3.2 8.6c0-3.35 2.55-6.05 5.8-6.05 1.85 0 3.55.9 4.5 2.35.95-1.45 2.65-2.35 4.5-2.35 3.25 0 5.8 2.7 5.8 6.05 0 5.8-8.8 12.6-8.8 12.6z"
            />
            <path
              :fill="`url(#${uid}-ht-${index})`"
              stroke="#9D174D"
              stroke-width="0.35"
              stroke-linejoin="round"
              d="M12 19.8S4.4 13.6 4.4 8.6c0-2.75 2.1-4.95 4.75-4.95 1.5 0 2.9.75 3.85 1.95.95-1.2 2.35-1.95 3.85-1.95 2.65 0 4.75 2.2 4.75 4.95 0 5-7.6 11.2-7.6 11.2H12z"
            />
            <path
              fill="#FCE7F3"
              d="M9.05 7.25c-.75.3-1.3.95-1.3 1.85 0 1.85 1.95 4.35 4.25 6.25-.45-3-.95-6.1-3-8.1z"
            />
          </svg>
          <!-- 奖章 -->
          <svg
            v-else
            class="mh-badge-svg"
            viewBox="0 0 24 24"
            aria-hidden="true"
          >
            <defs>
              <linearGradient :id="`${uid}-md-${index}`" x1="7" y1="3" x2="17" y2="15" gradientUnits="userSpaceOnUse">
                <stop offset="0%" stop-color="#FDE047" />
                <stop offset="48%" stop-color="#EAB308" />
                <stop offset="100%" stop-color="#854D0E" />
              </linearGradient>
            </defs>
            <path fill="#1E3A8A" d="M8.5 14.5l-2.2 7.5 3.4-2.4 2.3 2.8-.5-4.6-3-3.3z" />
            <path fill="#991B1B" d="M15.5 14.5l2.2 7.5-3.4-2.4-2.3 2.8.5-4.6 3-3.3z" />
            <circle cx="12" cy="9.5" r="7" fill="#422006" />
            <circle
              cx="12"
              cy="9.5"
              r="6"
              :fill="`url(#${uid}-md-${index})`"
              stroke="#A16207"
              stroke-width="0.4"
            />
            <circle cx="12" cy="9.5" r="4.15" fill="#B45309" />
            <path
              fill="#FFFBEB"
              d="M12 6.85l.85 1.85 2.05.28-1.48 1.45.35 2.05L12 11.65l-1.77 1.08.35-2.05-1.48-1.45 2.05-.28L12 6.85z"
            />
          </svg>
      </span>
    </div>
    <span class="mh-badge-text">我的徽章</span>
    <span class="mh-badge-arrow">›</span>
  </router-link>
</template>

<script setup>
import { computed, useId } from 'vue'

const props = defineProps({
  badges: {
    type: Array,
    default: () => []
  }
})

const PRESET_KEYS = ['star', 'shield', 'heart', 'medal']
const NAME_MAP = {
  新手上路: 'star',
  活跃达人: 'heart',
  热心帮助: 'shield',
  官方认证: 'medal'
}
const EMOJI_MAP = {
  '⭐': 'star',
  '🛡️': 'shield',
  '💗': 'heart',
  '💖': 'heart',
  '❤️': 'heart',
  '🏅': 'medal',
  '🎖️': 'medal'
}
const ALLOWED = new Set(PRESET_KEYS)

const uid = useId().replace(/[^a-zA-Z0-9_-]/g, '_')

const iconKeys = computed(() => {
  const list = Array.isArray(props.badges) ? props.badges : []
  const pick = (item, index) => {
    const raw = String(item?.icon || '').trim()
    if (raw && ALLOWED.has(raw)) return raw
    if (raw && EMOJI_MAP[raw]) return EMOJI_MAP[raw]
    const byName = NAME_MAP[String(item?.name || '').trim()]
    if (byName) return byName
    return PRESET_KEYS[index % PRESET_KEYS.length]
  }
  if (list.length) return list.slice(0, 4).map(pick)
  return [...PRESET_KEYS]
})
</script>

<style scoped>
.mh-badge-row {
  margin-top: 8px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 5px 12px 5px 7px;
  border-radius: 999px;
  background: rgba(24, 12, 52, 0.88);
  border: 1px solid rgba(255, 255, 255, 0.28);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.35);
  text-decoration: none;
  max-width: 100%;
}

.mh-badge-icons {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  flex: 0 0 auto;
}

.mh-badge-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(165deg, #ffffff 0%, #f1f5f9 55%, #e2e8f0 100%);
  border: 1px solid rgba(15, 23, 42, 0.12);
  box-shadow:
    0 2px 0 rgba(15, 23, 42, 0.06),
    0 4px 8px rgba(15, 23, 42, 0.14);
}

.mh-badge-svg {
  width: 20px;
  height: 20px;
  display: block;
  filter: drop-shadow(0 1px 0 rgba(255, 255, 255, 0.65)) drop-shadow(0 2px 2px rgba(15, 23, 42, 0.28));
}

.mh-badge-text {
  font-size: 12px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.94);
  line-height: 1;
}

.mh-badge-arrow {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.65);
  line-height: 1;
}
</style>
