<template><section class="empty"><p>暂无内容</p><button type="button" @click="$emit('publish')">去发布</button></section></template>
<script setup>defineEmits(['publish'])</script>
<style scoped>.empty{padding:30px 16px;text-align:center;color:var(--lc-muted)}button{margin-top:8px;height:36px;padding:0 14px;border-radius:8px;border:1px solid var(--lc-blue-border);background:#fff;color:var(--lc-blue)}</style>
