<template>
  <article class="feed-card" :class="{ 'feed-card--campaign': isCampaignPost }">
    <header class="feed-head">
      <div class="avatar" :style="avatarStyle">
        <img
          v-if="showAvatarImage"
          :src="resolvedAvatarUrl"
          :alt="`${displayName}头像`"
          class="avatar-img"
          @error="handleAvatarError"
        >
        <span v-else>{{ avatarText }}</span>
      </div>
      <div class="feed-meta">
        <div class="feed-name-line">
          <span class="name">{{ displayName }}</span>
          <span v-if="item.category" class="cat-tag">{{ item.category }}</span>
          <span v-if="statusLabel" class="status-tag">{{ statusLabel }}</span>
        </div>
        <p class="feed-subline">
          <span v-if="streakLabel > 0" class="streak">连续 {{ streakLabel }} 天</span>
          <time>{{ formatTime(item.createdAt) }}</time>
        </p>
      </div>
    </header>

    <p class="feed-body">{{ item.content }}</p>

    <footer class="feed-toolbar">
      <button
        type="button"
        class="tool"
        :class="{ active: commentsOpen }"
        :aria-expanded="commentsOpen"
        @click="$emit('toggle-comments', item)"
      >
        <span class="tool-icon" aria-hidden="true">💬</span>
        <span class="tool-label">{{ item.commentCount ? item.commentCount : '评论' }}</span>
      </button>
      <button
        type="button"
        class="tool"
        :class="{ active: item.liked }"
        @click="$emit('like', item)"
      >
        <span class="tool-icon" aria-hidden="true">{{ item.liked ? '♥' : '♡' }}</span>
        <span class="tool-label">{{ item.encourageCount ? item.encourageCount : '赞' }}</span>
      </button>
      <button
        type="button"
        class="tool"
        :class="{ active: item.bookmarked }"
        @click="$emit('bookmark', item)"
      >
        <span class="tool-icon" aria-hidden="true">{{ item.bookmarked ? '★' : '☆' }}</span>
        <span class="tool-label">{{ item.bookmarked ? '已藏' : '收藏' }}</span>
      </button>
      <button type="button" class="tool tool-compose" @click="$emit('comment', item)">
        写评论
      </button>
    </footer>
  </article>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { toFullUrl } from '@/utils/image.js'
import { userAvatarUrlFromApi } from '@/utils/displayFields.js'

const props = defineProps({
  item: {
    type: Object,
    required: true
  },
  commentsOpen: {
    type: Boolean,
    default: false
  }
})

defineEmits(['like', 'bookmark', 'comment', 'toggle-comments'])

const avatarLoadFailed = ref(false)

const AVATAR_PALETTES = [
  { bg: '#ffdde1', text: '#e05c7c' },
  { bg: '#fde8d0', text: '#e07030' },
  { bg: '#d0f0e8', text: '#2a9d7a' },
  { bg: '#d4e8fd', text: '#2a6db5' },
  { bg: '#e8d8fd', text: '#7c3aed' },
  { bg: '#fdf0d0', text: '#b07820' },
  { bg: '#f0d8e8', text: '#c0507a' },
  { bg: '#d8eef0', text: '#1a8090' },
]

function nameHash(str) {
  let h = 0
  for (const c of str) h = (h * 31 + c.charCodeAt(0)) & 0xffff
  return h
}

const displayName = computed(() => {
  if (props.item.anonymous) return '匿名用户'
  return props.item.nickname || props.item.username || '平台用户'
})

const resolvedAvatarUrl = computed(() => {
  const raw = userAvatarUrlFromApi(props.item)
  return raw ? toFullUrl(raw) : ''
})

const avatarText = computed(() => {
  const name = displayName.value
  if (name === '匿名用户') return '匿'
  return String(name).slice(0, 1)
})

const showAvatarImage = computed(() =>
  !props.item.anonymous && Boolean(resolvedAvatarUrl.value) && !avatarLoadFailed.value
)

const streakLabel = computed(() =>
  Number(props.item.streakDays || props.item.continuousDays || props.item.currentStreak || 0)
)

const statusLabel = computed(() => {
  const status = String(props.item.status || 'PUBLISHED').toUpperCase()
  if (status === 'PENDING') return '审核中'
  if (status === 'REJECTED') return '未通过'
  return ''
})

const isCampaignPost = computed(() => {
  const text = String(props.item.content || '')
  return text.includes('#端午') || text.includes('端午')
})

const avatarStyle = computed(() => {
  if (props.item.anonymous) {
    return { background: '#f1f5f9', color: '#94a3b8' }
  }
  const palette = AVATAR_PALETTES[nameHash(displayName.value) % AVATAR_PALETTES.length]
  return { background: palette.bg, color: palette.text }
})

function formatTime(value) {
  if (!value) return '刚刚'
  const d = new Date(String(value).replace('T', ' '))
  const now = new Date()
  const today = now.toISOString().slice(0, 10)
  const yesterday = new Date(now - 86400000).toISOString().slice(0, 10)
  const dateStr = d.toISOString().slice(0, 10)
  const timeStr = `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
  if (dateStr === today) return `今天 ${timeStr}`
  if (dateStr === yesterday) return `昨天 ${timeStr}`
  return `${dateStr.slice(5).replace('-', '/')} ${timeStr}`
}

function handleAvatarError() {
  avatarLoadFailed.value = true
}

watch(
  () => resolvedAvatarUrl.value,
  () => {
    avatarLoadFailed.value = false
  }
)
</script>

<style scoped>
.feed-card {
  padding: 12px 0;
  border-bottom: 1px solid var(--lc-border, #eef2f7);
}

.feed-card:last-child {
  border-bottom: none;
}

.feed-card--campaign {
  background: linear-gradient(180deg, rgba(240, 253, 244, 0.45) 0%, transparent 72%);
  margin: 0 -4px;
  padding-left: 4px;
  padding-right: 4px;
  border-radius: 8px;
}

.feed-head {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  flex-shrink: 0;
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.feed-meta {
  min-width: 0;
  flex: 1;
}

.feed-name-line {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.name {
  color: var(--lc-text);
  font-size: 14px;
  font-weight: 700;
}

.cat-tag,
.status-tag {
  display: inline-flex;
  align-items: center;
  min-height: 18px;
  padding: 0 6px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
}

.cat-tag {
  background: var(--lc-blue-light, #eff6ff);
  color: var(--lc-blue, #2563eb);
}

.status-tag {
  background: var(--lc-amber-light, #fffbeb);
  color: var(--lc-amber, #d97706);
}

.feed-subline {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 2px 0 0;
  color: var(--lc-subtle);
  font-size: 12px;
}

.streak::after {
  content: '·';
  margin-left: 6px;
  color: var(--lc-muted, #cbd5e1);
}

.feed-body {
  margin: 8px 0 0 46px;
  color: var(--lc-slate);
  line-height: 1.65;
  white-space: pre-wrap;
  overflow-wrap: break-word;
  font-size: 15px;
}

.feed-toolbar {
  display: flex;
  align-items: center;
  gap: 4px;
  margin: 8px 0 0 46px;
}

.tool {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  min-height: 28px;
  padding: 0 8px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: var(--lc-subtle);
  font-size: 12px;
  cursor: pointer;
}

.tool:hover {
  background: var(--lc-soft, #f1f5f9);
  color: var(--lc-text);
}

.tool.active {
  color: var(--lc-pink, #ec4899);
}

.tool.active .tool-icon {
  color: var(--lc-pink, #ec4899);
}

.tool-compose {
  margin-left: auto;
  color: var(--lc-blue, #3b82f6);
  font-weight: 600;
}

.tool-icon {
  font-size: 14px;
  line-height: 1;
}

.tool-label {
  min-width: 0;
}
</style>
