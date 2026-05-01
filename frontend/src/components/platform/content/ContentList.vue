<template>
  <section class="list">
    <ContentCard
      v-for="item in items"
      :key="item.id"
      :item="item"
      @like="$emit('like', item)"
      @comment="$emit('comment', item)"
    />
  </section>
</template>

<script setup>
import ContentCard from './ContentCard.vue'

defineProps({ items: Array })
defineEmits(['like', 'comment'])
</script>

<style scoped>
.list{display:grid;gap:10px}
</style>
