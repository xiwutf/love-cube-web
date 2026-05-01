<template><div class="editor"><input v-model="localTitle" placeholder="请输入标题（心声可选）" @input="emitChange"><textarea v-model="localContent" maxlength="2000" placeholder="写点什么..." @input="emitChange"></textarea><p>{{ localContent.length }}/2000</p></div></template>
<script setup>
import { ref, watch } from 'vue'
const props = defineProps({ title:String, content:String })
const emit = defineEmits(['update'])
const localTitle = ref(props.title || '')
const localContent = ref(props.content || '')
watch(() => props.title, v => { localTitle.value = v || '' })
watch(() => props.content, v => { localContent.value = v || '' })
function emitChange(){ emit('update',{ title: localTitle.value, content: localContent.value }) }
</script>
<style scoped>.editor{display:grid;gap:8px}input,textarea{width:100%;border:1px solid var(--lc-border);border-radius:10px;padding:10px;font:inherit}textarea{min-height:140px;resize:vertical}p{margin:0;text-align:right;color:var(--lc-subtle);font-size:12px}</style>
