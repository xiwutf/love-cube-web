<template>
  <article class="card" :class="{ 'no-cover': !item.cover }">
    <img v-if="item.cover" :src="item.cover" alt="">
    <div class="body">
      <p v-if="item.pinned" class="pinned">置顶</p>
      <h4>{{ item.title }}</h4>
      <p>{{ item.summary }}</p>
      <div v-if="displayCategory" class="category">{{ displayCategory }}</div>
      <small class="meta">
        <span class="author">
          <img v-if="item.avatar" :src="item.avatar" alt="" class="meta-avatar">
          <span v-else class="meta-avatar-fallback">{{ authorInitial }}</span>
          <span>{{ item.authorName }}</span>
        </span>
        <span>·</span>
        <span>{{ item.createdAt }}</span>
        <span>·</span>
        <span>👍 {{ item.likeCount }}</span>
        <span>·</span>
        <span>💬 {{ item.commentCount }}</span>
      </small>
      <div class="actions">
        <button type="button" class="action-btn" @click="$emit('like')">点赞</button>
        <button type="button" class="action-btn" @click="$emit('comment')">评论</button>
      </div>
    </div>
  </article>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({ item: Object })
defineEmits(['like', 'comment'])

const authorInitial = computed(() => String(props.item?.authorName || '用户').slice(0, 1))
const displayCategory = computed(() => {
  const tags = Array.isArray(props.item?.tags) ? props.item.tags : []
  if (tags.length > 0 && tags[0]) return tags[0]
  if (props.item?.type === 'mood') return '每日心声'
  return ''
})
</script>

<style scoped>
.card{display:grid;grid-template-columns:96px 1fr;gap:10px;padding:12px;background:var(--lc-surface);border:1px solid var(--lc-border);border-radius:12px}
.card.no-cover{grid-template-columns:1fr}
img{width:96px;height:72px;object-fit:cover;border-radius:8px}
h4{margin:0;font-size:15px}
p{margin:4px 0;color:var(--lc-muted);font-size:13px}
.pinned{margin:0;color:var(--lc-red);font-size:12px}
.category{display:inline-flex;align-items:center;height:24px;padding:0 10px;border:1px solid var(--lc-blue);border-radius:12px;background:var(--lc-blue-light);color:var(--lc-blue);font-size:12px;margin:4px 0}
.meta{display:flex;align-items:center;gap:6px;color:var(--lc-subtle);flex-wrap:wrap}
.author{display:inline-flex;align-items:center;gap:6px}
.meta-avatar,.meta-avatar-fallback{width:18px;height:18px;flex:0 0 18px;border-radius:50%;display:inline-flex;align-items:center;justify-content:center;overflow:hidden}
.meta-avatar{object-fit:cover}
.meta-avatar-fallback{background:#e2e8f0;color:#475569;font-size:11px}
.actions{display:flex;gap:8px;margin-top:8px}
.action-btn{height:28px;padding:0 10px;border:1px solid var(--lc-border);border-radius:14px;background:#fff;color:var(--lc-muted);font-size:12px}
</style>
