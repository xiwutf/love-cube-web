<template>
  <article class="share-card section-card reflection-card">
    <header class="share-head">
      <div class="author">
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
        <div class="author-info">
          <div class="name-row">
            <span class="name">{{ displayName }}</span>
            <span class="status-badge info">{{ item.category || '成长记录' }}</span>
          </div>
          <span class="time">{{ formatTime(item.createdAt) }}</span>
        </div>
      </div>
      <span class="status-badge neutral">{{ item.status || '已发布' }}</span>
    </header>

    <p class="content">{{ item.content }}</p>

    <div class="share-meta-row">
      <span class="status-badge success">连续 {{ streakLabel }} 天</span>
      <span v-if="item.tags" class="status-badge neutral">{{ item.tags }}</span>
    </div>

    <footer class="actions">
      <button type="button" :class="['platform-btn', 'action-btn', 'like-btn', { liked: item.liked }]" @click="$emit('like', item)">
        {{ item.liked ? '已点赞' : '点赞' }}
        <span class="action-count">{{ item.encourageCount || 0 }}</span>
      </button>
      <button
        type="button"
        :class="['platform-btn', 'platform-btn-ghost', 'action-btn', 'bookmark-btn', { bookmarked: item.bookmarked }]"
        @click="$emit('bookmark', item)"
      >
        {{ item.bookmarked ? '已收藏' : '收藏' }}
      </button>
      <button type="button" class="platform-btn platform-btn-ghost action-btn comment-btn" @click="$emit('comment', item)">
        评论
        <span class="action-count">{{ item.commentCount || 0 }}</span>
      </button>
      <button type="button" class="platform-btn platform-btn-ghost action-btn share-btn">
        分享
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
  }
})

defineEmits(['like', 'bookmark', 'comment'])

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

const CAT_CONFIG = {
  '感恩': { bg: '#fee2e2', color: '#ef4444' },
  '鼓励': { bg: '#f3e8ff', color: '#a855f7' },
  '成长': { bg: '#dcfce7', color: '#16a34a' },
  '理想': { bg: '#dbeafe', color: '#2563eb' },
  '生活': { bg: '#fce7f3', color: '#db2777' },
  '学习': { bg: '#ffedd5', color: '#ea580c' },
  '工作': { bg: '#f1f5f9', color: '#475569' },
  '人际': { bg: '#ede9fe', color: '#7c3aed' },
  '正能量': { bg: '#fef9c3', color: '#ca8a04' },
  '其他': { bg: '#f1f5f9', color: '#64748b' },
}

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

const avatarStyle = computed(() => {
  if (props.item.anonymous) {
    return { background: '#f1f5f9', color: '#94a3b8' }
  }
  const palette = AVATAR_PALETTES[nameHash(displayName.value) % AVATAR_PALETTES.length]
  return { background: palette.bg, color: palette.text }
})

const catTagStyle = computed(() => {
  const cfg = CAT_CONFIG[props.item.category] || CAT_CONFIG['其他']
  return { background: cfg.bg, color: cfg.color }
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
.share-card {
  border: 1px solid var(--lc-border);
  border-radius: 12px;
  background: var(--lc-surface);
  padding: 16px;
  box-shadow: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.share-card:hover {
  border-color: var(--lc-blue-border);
  box-shadow: 0 10px 24px rgb(15 23 42 / 6%);
}

.share-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.author {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  flex: 1;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 700;
  flex-shrink: 0;
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.author-info {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.name {
  color: var(--lc-text);
  font-size: 14px;
  font-weight: 700;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.time {
  color: var(--lc-subtle);
  font-size: 12px;
}

.content {
  margin: 12px 0 0;
  color: var(--lc-slate);
  line-height: 1.75;
  white-space: pre-wrap;
  overflow-wrap: break-word;
  font-size: 14px;
}

.share-meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.actions {
  margin-top: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.action-btn {
  min-height: 32px;
  padding: 0 10px;
  font-size: 12px;
}

.like-btn.liked {
  border-color: var(--lc-green);
  background: var(--lc-green-light);
  color: var(--lc-green);
}

.share-btn {
  margin-left: auto;
}

.action-count {
  color: var(--lc-subtle);
  font-size: 11px;
}

.bookmark-btn.bookmarked {
  border-color: var(--lc-amber);
  background: var(--lc-amber-light);
  color: var(--lc-amber);
}
</style>
