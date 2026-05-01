<template><div><input type="file" accept="image/*" multiple @change="onPick"><div class="grid"><img v-for="img in previews" :key="img" :src="img" alt=""></div></div></template>
<script setup>
import { ref } from 'vue'
const emit = defineEmits(['change'])
const previews = ref([])
function onPick(e){
  const files = Array.from(e.target.files || []).slice(0, 9)
  previews.value = files.map(file => URL.createObjectURL(file))
  emit('change', files)
}
</script>
<style scoped>.grid{display:grid;grid-template-columns:repeat(3,1fr);gap:8px;margin-top:8px}img{width:100%;height:88px;object-fit:cover;border-radius:8px;border:1px solid var(--lc-border)}</style>
