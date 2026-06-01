<template>
  <main class="page">
    <h1>提交资源线索</h1>
    <p class="fixed-type">提交后将进入待处理队列，由运营审核发布</p>
    <form class="clue-form" @submit.prevent="submitClue">
      <label>
        资源标题
        <input v-model.trim="clueForm.title" type="text" maxlength="100" placeholder="请输入标题" />
      </label>
      <label>
        资源分类
        <select v-model="clueForm.type">
          <option value="people">找人</option>
          <option value="activity">找活动</option>
          <option value="group">找圈子</option>
          <option value="resource">实用资源</option>
        </select>
      </label>
      <label>
        联系方式
        <input v-model.trim="clueForm.contact" type="text" maxlength="120" placeholder="手机号/微信/链接" />
      </label>
      <label>
        地区（选填）
        <input v-model.trim="clueForm.location" type="text" maxlength="100" placeholder="例如：河北省 保定市" />
      </label>
      <label>
        简述（选填）
        <textarea v-model.trim="clueForm.summary" rows="3" maxlength="500" placeholder="简要说明资源信息"></textarea>
      </label>
      <button type="submit" class="submit">提交线索</button>
    </form>
    <button type="button" class="back" @click="goBackLocal">返回本地资源</button>

    <p v-if="message" class="msg">{{ message }}</p>
  </main>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { submitLocalResourceClue } from '@/api/localResources.js'

const router = useRouter()
const message = ref('')
const clueForm = reactive({ title: '', type: 'people', contact: '', location: '', summary: '' })

function goBackLocal() {
  router.push('/platform/local')
}

async function submitClue() {
  if (!clueForm.title) {
    message.value = '资源标题不能为空'
    return
  }
  if (!clueForm.contact) {
    message.value = '联系方式不能为空'
    return
  }
  try {
    await submitLocalResourceClue({
      title: clueForm.title,
      type: clueForm.type,
      contact: clueForm.contact,
      location: clueForm.location,
      summary: clueForm.summary
    })
    message.value = '线索提交成功，已进入待处理队列'
    clueForm.title = ''
    clueForm.type = 'people'
    clueForm.contact = ''
    clueForm.location = ''
    clueForm.summary = ''
  } catch (error) {
    message.value = error?.response?.data?.message || '线索提交失败，请稍后重试'
  }
}
</script>

<style scoped>
.page{width:min(100% - 24px,560px);margin:16px auto;display:grid;gap:12px;padding-bottom:calc(72px + env(safe-area-inset-bottom));}
h1{margin:0;font-size:20px}
.fixed-type{margin:0;color:var(--lc-muted);font-size:14px}
.submit{height:44px;border:0;border-radius:10px;background:var(--lc-blue);color:var(--lc-surface);font-weight:700}
.msg{margin:0;color:var(--lc-muted);font-size:13px}
.clue-form{display:grid;gap:10px}
.clue-form label{display:grid;gap:6px;font-size:13px;color:var(--lc-muted)}
.clue-form input,.clue-form select,.clue-form textarea{width:100%;border:1px solid var(--lc-border);border-radius:10px;padding:10px;font:inherit;background:var(--lc-surface)}
.back{height:40px;border:1px solid var(--lc-border);border-radius:10px;background:var(--lc-surface);color:var(--lc-text);font-weight:600}
</style>
