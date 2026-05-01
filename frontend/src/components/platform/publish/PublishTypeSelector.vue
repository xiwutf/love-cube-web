<template><div class="types"><button v-for="item in types" :key="item.key" :class="{active:modelValue===item.key}" @click="$emit('update:modelValue',item.key)">{{ item.label }}</button></div></template>
<script setup>defineProps({ types:Array, modelValue:String });defineEmits(['update:modelValue'])</script>
<style scoped>.types{display:grid;grid-template-columns:1fr 1fr;gap:8px}button{height:38px;border:1px solid var(--lc-border);border-radius:10px;background:#fff}.active{border-color:var(--lc-blue);background:var(--lc-blue-light);color:var(--lc-blue)}</style>
