<template><div class="bar"><button v-for="item in sorts" :key="item.key" :class="{ active: modelValue===item.key }" @click="$emit('update:modelValue', item.key)">{{ item.label }}</button></div></template>
<script setup>defineProps({ sorts:Array, modelValue:String });defineEmits(['update:modelValue'])</script>
<style scoped>.bar{display:flex;gap:8px}.bar button{height:32px;padding:0 12px;border:1px solid var(--lc-border);border-radius:8px;background:var(--lc-surface)}.active{border-color:var(--lc-blue);color:var(--lc-blue)}</style>
