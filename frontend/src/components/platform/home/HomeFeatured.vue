<template>
  <section class="featured-card">
    <h3>今日推荐</h3>
    <article
      v-for="item in items"
      :key="item.id"
      class="featured-item"
      @click="$emit('open', item)"
    >
      <img :src="coverFor(item)" :alt="item.title" loading="lazy" @error="onCoverError($event, item)">
      <div class="featured-copy">
        <h4>{{ item.title }}</h4>
        <p>{{ item.summary }}</p>
      </div>
    </article>
  </section>
</template>

<script setup>
import { resolveUploadUrl } from '@/utils/image.js'

defineProps({ items: Array })
defineEmits(['open'])

const FALLBACK_COVER = 'https://images.unsplash.com/photo-1529156069898-49953e39b3ac?auto=format&fit=crop&w=320&q=80'

function coverFor(item) {
  const raw = String(item?.cover || item?.coverUrl || '').trim()
  return resolveUploadUrl(raw) || raw || FALLBACK_COVER
}

function onCoverError(event) {
  const img = event?.target
  if (!img || img.dataset.fallbackApplied === '1') return
  img.dataset.fallbackApplied = '1'
  img.src = FALLBACK_COVER
}
</script>

<style scoped>
.featured-card {
  padding: 14px 16px;
  background: var(--lc-surface);
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius);
}

.featured-card h3 {
  margin: 0;
  font-size: 18px;
  color: var(--lc-text);
}

.featured-item {
  display: grid;
  grid-template-columns: 72px minmax(0, 1fr);
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid var(--lc-soft-alt);
  cursor: pointer;
}

.featured-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.featured-item img {
  width: 72px;
  height: 54px;
  object-fit: cover;
  border-radius: 8px;
  background: var(--lc-soft);
}

.featured-copy {
  min-width: 0;
}

.featured-copy h4 {
  margin: 0;
  font-size: 14px;
  line-height: 1.35;
  color: var(--lc-text);
  word-break: break-word;
}

.featured-copy p {
  margin: 4px 0 0;
  color: var(--lc-muted);
  font-size: 12px;
  line-height: 1.45;
  display: -webkit-box;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

@media (max-width: 360px) {
  .featured-item {
    grid-template-columns: 60px minmax(0, 1fr);
  }

  .featured-item img {
    width: 60px;
    height: 48px;
  }
}
</style>
