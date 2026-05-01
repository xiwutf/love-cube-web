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
              <linearGradient :id="`${uid}-st-${index}`" x1="5" y1="4" x2="19" y2="20" gradientUnits="userSpaceOnUse">
                <stop offset="0%" stop-color="#FEF9C3" />
                <stop offset="40%" stop-color="#FACC15" />
                <stop offset="100%" stop-color="#B45309" />
              </linearGradient>
            </defs>
            <path
              fill="#713F12"
              opacity="0.35"
              d="M12 2.2l2.95 6.9 7.45.65-5.7 5.1 1.7 7.25L12 18.05 5.6 22.1l1.7-7.25-5.7-5.1 7.45-.65L12 2.2z"
            />
            <path
              :fill="`url(#${uid}-st-${index})`"
              d="M12 4.1l2.38 5.55 6.05.52-4.6 4.14 1.39 5.92L12 16.65 6.38 20.23l1.39-5.92-4.6-4.14 6.05-.52L12 4.1z"
            />
            <path
              fill="#FFFBEB"
              opacity="0.55"
              d="M12 5.2l-.35 3.2-2.9.25 2.2 1.95-.65 2.85L12 11.9l2.85 1.75-.65-2.85 2.2-1.95-2.9-.25L12 5.2z"
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
              <linearGradient :id="`${uid}-sh-${index}`" x1="4" y1="3" x2="20" y2="22" gradientUnits="userSpaceOnUse">
                <stop offset="0%" stop-color="#BFDBFE" />
                <stop offset="45%" stop-color="#3B82F6" />
                <stop offset="100%" stop-color="#1E3A8A" />
              </linearGradient>
            </defs>
            <path
              fill="#172554"
              opacity="0.35"
              d="M12 2.4L20.2 5.4v6.6c0 5.1-3.4 9.35-8.2 11.1-4.8-1.75-8.2-6-8.2-11.1V5.4L12 2.4z"
            />
            <path
              :fill="`url(#${uid}-sh-${index})`"
              d="M12 3.6L19 6.05v5.85c0 4.35-2.85 8-7 9.65-4.15-1.65-7-5.3-7-9.65V6.05L12 3.6z"
            />
            <path
              fill="#DBEAFE"
              opacity="0.5"
              d="M12 6.2c1.65 1.1 2.8 2.85 2.95 4.9h-5.9c.15-2.05 1.3-3.8 2.95-4.9z"
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
              <linearGradient :id="`${uid}-ht-${index}`" x1="3" y1="4" x2="21" y2="20" gradientUnits="userSpaceOnUse">
                <stop offset="0%" stop-color="#FBCFE8" />
                <stop offset="40%" stop-color="#EC4899" />
                <stop offset="100%" stop-color="#9D174D" />
              </linearGradient>
            </defs>
            <path
              fill="#831843"
              opacity="0.3"
              d="M12 21.2S3.2 14.4 3.2 8.6c0-3.35 2.55-6.05 5.8-6.05 1.85 0 3.55.9 4.5 2.35.95-1.45 2.65-2.35 4.5-2.35 3.25 0 5.8 2.7 5.8 6.05 0 5.8-8.8 12.6-8.8 12.6z"
            />
            <path
              :fill="`url(#${uid}-ht-${index})`"
              d="M12 19.8S4.4 13.6 4.4 8.6c0-2.75 2.1-4.95 4.75-4.95 1.5 0 2.9.75 3.85 1.95.95-1.2 2.35-1.95 3.85-1.95 2.65 0 4.75 2.2 4.75 4.95 0 5-7.6 11.2-7.6 11.2H12z"
            />
            <path
              fill="#FDF2F8"
              opacity="0.45"
              d="M8.8 7.1c-.9.35-1.55 1.15-1.55 2.15 0 2.2 2.35 5.15 4.75 7.35-.5-3.6-1.2-7.35-3.2-9.5z"
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
              <linearGradient :id="`${uid}-md-${index}`" x1="8" y1="4" x2="16" y2="14" gradientUnits="userSpaceOnUse">
                <stop offset="0%" stop-color="#FEF08A" />
                <stop offset="50%" stop-color="#EAB308" />
                <stop offset="100%" stop-color="#A16207" />
              </linearGradient>
            </defs>
            <path fill="#1E40AF" d="M8.5 14.5l-2.2 7.5 3.4-2.4 2.3 2.8-.5-4.6-3-3.3z" opacity="0.9" />
            <path fill="#B91C1C" d="M15.5 14.5l2.2 7.5-3.4-2.4-2.3 2.8.5-4.6 3-3.3z" opacity="0.9" />
            <circle cx="12" cy="9.5" r="7" fill="#713F12" opacity="0.35" />
            <circle cx="12" cy="9.5" r="6" :fill="`url(#${uid}-md-${index})`" />
            <circle cx="12" cy="9.5" r="4.2" fill="#CA8A04" opacity="0.35" />
            <path
              fill="#FFFBEB"
              opacity="0.85"
              d="M12 6.8l.95 2.05 2.2.3-1.6 1.55.4 2.2L12 11.9l-1.95 1 .4-2.2-1.6-1.55 2.2-.3L12 6.8z"
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
  padding: 4px 10px 4px 6px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.22);
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
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.35);
}

.mh-badge-svg {
  width: 20px;
  height: 20px;
  display: block;
  filter: drop-shadow(0 1px 1px rgba(15, 23, 42, 0.25));
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
