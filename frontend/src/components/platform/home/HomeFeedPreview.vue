<template>
  <section class="feed-preview">
    <h3>内容预览</h3>
    <ul v-if="items?.length">
      <li v-for="item in items" :key="item.id">{{ item.title }}</li>
    </ul>
    <p v-else class="feed-empty">暂无内容</p>
    <button type="button" class="feed-more" @click="$emit('more')">查看更多</button>
  </section>
</template>

<script setup>
defineProps({ items: Array })
defineEmits(['more'])
</script>

<style scoped>
.feed-preview {
  padding: 14px 16px;
  background: var(--lc-surface);
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius);
}

.feed-preview h3 {
  margin: 0;
  font-size: 18px;
  color: var(--lc-text);
}

ul {
  margin: 10px 0 0;
  padding-left: 18px;
  color: var(--lc-muted);
}

li {
  margin: 6px 0;
  font-size: 14px;
  line-height: 1.45;
  overflow-wrap: anywhere;
}

.feed-empty {
  margin: 10px 0 0;
  font-size: 13px;
  color: var(--lc-subtle);
}

.feed-more {
  margin-top: 12px;
  width: 100%;
  min-height: 40px;
  border: 1px solid var(--lc-blue-border);
  background: var(--lc-blue-light);
  color: var(--lc-blue);
  border-radius: 10px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
}
</style>
