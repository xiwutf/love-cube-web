<template>
  <section class="hero">
    <h1>{{ title }}</h1>
    <p>{{ subtitle }}</p>
    <div class="actions">
      <button type="button" @click="$emit('browse')">浏览内容</button>
      <button type="button" class="publish" @click="$emit('publish')">发布内容</button>
    </div>
  </section>
</template>

<script setup>
defineProps({ title: String, subtitle: String })
defineEmits(['browse', 'publish'])
</script>

<style scoped>
.hero { padding: 20px 16px; background: var(--lc-surface); border: 1px solid var(--lc-border); border-radius: var(--lc-radius); }
h1 { margin: 0; font-size: 22px; }
p { margin: 8px 0 0; color: var(--lc-muted); }
.actions { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-top: 14px; }
button { height: 40px; border: 1px solid var(--lc-blue-border); border-radius: 10px; background: var(--lc-surface); color: var(--lc-blue); font-weight: 700; }
.publish { background: var(--lc-blue); color: var(--lc-surface); border-color: var(--lc-blue); }
</style>
