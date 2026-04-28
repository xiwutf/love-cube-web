<template>
  <article class="share-card">
    <header class="share-head">
      <div class="author">
        <div class="avatar" :style="avatarStyle">
          <img
            v-if="showAvatarImage"
            :src="props.item.avatar"
            :alt="`${displayName}头像`"
            class="avatar-img"
            @error="handleAvatarError"
          >
          <span v-else>{{ avatarText }}</span>
        </div>
        <div class="author-info">
          <div class="name-row">
            <span class="name">{{ displayName }}</span>
            <span class="cat-tag" :style="catTagStyle">{{ item.category }}</span>
          </div>
          <span class="time">{{ formatTime(item.createdAt) }}</span>
        </div>
      </div>
      <button type="button" class="more-btn" aria-label="更多操作">···</button>
    </header>

    <p class="content">{{ item.content }}</p>

    <footer class="actions">
      <button type="button" :class="['action-btn', 'like-btn', { liked: item.liked }]" @click="$emit('like', item)">
        <span class="action-icon">{{ item.liked ? '❤' : '♡' }}</span>
        为他加油
        <span class="action-count">{{ item.encourageCount || 0 }}</span>
      </button>
      <button type="button" class="action-btn comment-btn" @click="$emit('comment', item)">
        <span class="action-icon">💬</span>
        留言
        <span class="action-count">{{ item.commentCount || 0 }}</span>
      </button>
      <button type="button" class="action-btn share-btn">
        <span class="action-icon">↗</span>
        分享
      </button>
    </footer>
  </article>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  item: {
    type: Object,
    required: true
  }
})

defineEmits(['like', 'comment'])

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
  '信仰': { bg: '#dbeafe', color: '#2563eb' },
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

const avatarText = computed(() => {
  const name = displayName.value
  if (name === '匿名用户') return '匿'
  return String(name).slice(0, 1)
})

const showAvatarImage = computed(() =>
  !props.item.anonymous && Boolean(props.item.avatar) && !avatarLoadFailed.value
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
  () => props.item.avatar,
  () => {
    avatarLoadFailed.value = false
  }
)
</script>

<style scoped>
.share-card {
  border: 1px solid #f1ece8;
  border-radius: 14px;
  background: #fff;
  padding: 16px;
  transition: box-shadow 0.2s;
}

.share-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.07);
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
  color: #1f2937;
  font-size: 14px;
  font-weight: 700;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.cat-tag {
  border-radius: 999px;
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 700;
  white-space: nowrap;
}

.time {
  color: #94a3b8;
  font-size: 12px;
}

.more-btn {
  border: none;
  background: transparent;
  color: #94a3b8;
  font-size: 18px;
  line-height: 1;
  cursor: pointer;
  padding: 2px 4px;
  letter-spacing: 1px;
  flex-shrink: 0;
}

.more-btn:hover {
  color: #64748b;
}

.content {
  margin: 12px 0 0;
  color: #374151;
  line-height: 1.75;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 14px;
}

.actions {
  margin-top: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: 1px solid #f1ece8;
  border-radius: 999px;
  background: #fafaf9;
  color: #64748b;
  height: 30px;
  padding: 0 10px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
}

.action-btn:hover {
  border-color: #e2d9d0;
  background: #f5f0eb;
}

.like-btn.liked {
  border-color: #fecdd3;
  background: #fff1f2;
  color: #e11d48;
}

.like-btn .action-icon {
  color: #e11d48;
}

.comment-btn .action-icon {
  font-size: 13px;
}

.share-btn {
  margin-left: auto;
}

.action-count {
  color: #94a3b8;
  font-size: 11px;
}

.like-btn.liked .action-count {
  color: #f87171;
}
</style>
