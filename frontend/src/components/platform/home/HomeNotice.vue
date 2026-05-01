<template><section class="card"><h3>平台公告</h3><p>{{ notice?.title }}</p><small>{{ notice?.summary }}</small></section></template>
<script setup>defineProps({ notice: Object })</script>
<style scoped>.card{padding:14px 16px;background:var(--lc-surface);border:1px solid var(--lc-border);border-radius:var(--lc-radius)}h3{margin:0 0 8px}p{margin:0;font-weight:700}small{display:block;margin-top:6px;color:var(--lc-muted)}</style>
