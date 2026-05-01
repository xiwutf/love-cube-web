<template><section class="card"><h3>快捷入口</h3><div class="grid"><button v-for="item in entries" :key="item.type" type="button" @click="$emit('go', item.type)">{{ item.label }}</button></div></section></template>
<script setup>defineProps({ entries: Array });defineEmits(['go'])</script>
<style scoped>.card{padding:14px 16px;background:var(--lc-surface);border:1px solid var(--lc-border);border-radius:var(--lc-radius)}.grid{display:grid;grid-template-columns:1fr 1fr;gap:8px}button{height:38px;border:1px solid var(--lc-border);border-radius:8px;background:#fff;color:var(--lc-text)}</style>
