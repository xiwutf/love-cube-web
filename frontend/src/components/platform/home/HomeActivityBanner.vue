<template><section class="banner">{{ text }}</section></template>
<script setup>defineProps({ text: String })</script>
<style scoped>.banner{padding:14px 16px;border-radius:var(--lc-radius);background:var(--lc-blue-light);border:1px solid var(--lc-blue-border);color:var(--lc-blue-dark);font-weight:700}</style>
