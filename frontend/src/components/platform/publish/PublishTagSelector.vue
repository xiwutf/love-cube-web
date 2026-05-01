<template><div class="tags"><button v-for="tag in tags" :key="tag" :class="{active:modelValue.includes(tag)}" @click="toggle(tag)">{{ tag }}</button></div></template>
<script setup>
const props = defineProps({ tags:Array, modelValue:Array })
const emit = defineEmits(['update:modelValue'])
function toggle(tag){
  const set = new Set(props.modelValue || [])
  set.has(tag) ? set.delete(tag) : set.add(tag)
  emit('update:modelValue', Array.from(set))
}
</script>
<style scoped>.tags{display:flex;flex-wrap:wrap;gap:8px}button{height:32px;padding:0 10px;border:1px solid var(--lc-border);border-radius:16px;background:#fff}.active{border-color:var(--lc-blue);color:var(--lc-blue);background:var(--lc-blue-light)}</style>
