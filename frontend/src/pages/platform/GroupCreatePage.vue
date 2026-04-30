<template>
  <section class="create-page">
    <header class="page-head">
      <router-link class="back-link" to="/platform/groups">← 返回团体大厅</router-link>
      <h1>创建团体</h1>
      <p>填写资料后即可创建；你将自动成为团体所有者。</p>
    </header>

    <form class="create-form" @submit.prevent="submit">
      <label class="field">
        <span class="label-text">团体名称 <em>*</em></span>
        <input v-model.trim="form.name" type="text" required maxlength="40" placeholder="给你的团体起一个清晰的名字">
      </label>

      <label class="field">
        <span class="label-text">团体简介 <em>*</em></span>
        <textarea v-model.trim="form.description" required maxlength="500" rows="5" placeholder="介绍团体定位、适合人群与活动内容"></textarea>
      </label>

      <label class="field">
        <span class="label-text">分类 <em>*</em></span>
        <select v-model="form.type" required>
          <option v-for="c in categories" :key="c.value" :value="c.value">{{ c.label }}</option>
        </select>
      </label>

      <fieldset class="field join-field">
        <legend class="label-text">加入方式 <em>*</em></legend>
        <label><input v-model="form.joinMode" type="radio" value="open"> 公开加入</label>
        <label><input v-model="form.joinMode" type="radio" value="audit"> 审核加入</label>
        <label><input v-model="form.joinMode" type="radio" value="invite"> 仅限邀请</label>
      </fieldset>

      <label class="field">
        <span class="label-text">封面图</span>
        <input type="file" accept="image/*" class="file-input" @change="onCoverFile">
        <p v-if="uploadingCover" class="hint">上传中…</p>
        <p v-else-if="form.coverUrl" class="hint preview-row">
          <span>已选择封面</span>
          <img :src="form.coverUrl" alt="" class="thumb">
        </p>
        <p v-else class="hint">可选，支持 JPG / PNG；也可在提交后稍后在团体资料中修改。</p>
      </label>

      <label class="field">
        <span class="label-text">地区（可选）</span>
        <input v-model.trim="form.region" type="text" maxlength="50" placeholder="例如：北京">
      </label>

      <label class="field">
        <span class="label-text">标签（可选）</span>
        <input v-model.trim="form.tags" type="text" maxlength="120" placeholder="多个标签用逗号分隔">
      </label>

      <p v-if="errorMsg" class="err">{{ errorMsg }}</p>

      <div class="actions">
        <router-link class="btn ghost" to="/platform/groups">取消</router-link>
        <button type="submit" class="btn primary" :disabled="submitting">{{ submitting ? '创建中…' : '创建团体' }}</button>
      </div>
    </form>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { createGroup } from '@/api/groups.js'
import { uploadImage } from '@/api/upload.js'
import { useUserStore } from '@/stores/user.js'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  name: '',
  description: '',
  type: 'region',
  joinMode: 'audit',
  region: '',
  tags: '',
  coverUrl: ''
})

const categories = [
  { label: '地区团体', value: 'region' },
  { label: '教会团体', value: 'church' },
  { label: '学习小组', value: 'study' },
  { label: '兴趣团体', value: 'interest' },
  { label: '生活团契', value: 'family' },
  { label: '事工团队', value: 'service' }
]

const submitting = ref(false)
const uploadingCover = ref(false)
const errorMsg = ref('')

onMounted(() => {
  if (!userStore.isLoggedIn) {
    userStore.setPostLoginRedirect('/platform/groups/create')
    router.push('/login')
  }
})

async function onCoverFile(e) {
  const file = e.target?.files?.[0]
  if (!file) return
  uploadingCover.value = true
  errorMsg.value = ''
  try {
    const res = await uploadImage(file)
    const url = res?.url
    if (typeof url === 'string' && url.length > 0) {
      form.coverUrl = url
    } else {
      errorMsg.value = '上传成功但未返回图片地址，请稍后重试或使用下方手动填写封面 URL（若表单后续补充）。'
    }
  } catch (err) {
    errorMsg.value = err?.message || '封面上传失败'
  } finally {
    uploadingCover.value = false
  }
}

async function submit() {
  if (!userStore.isLoggedIn) {
    userStore.setPostLoginRedirect('/platform/groups/create')
    router.push('/login')
    return
  }
  submitting.value = true
  errorMsg.value = ''
  try {
    const payload = {
      name: form.name,
      description: form.description,
      type: form.type,
      joinMode: form.joinMode,
      region: form.region || undefined,
      tags: form.tags || undefined,
      coverUrl: form.coverUrl || undefined
    }
    const res = await createGroup(payload)
    const id = res?.id ?? res?.data?.id
    if (id) {
      router.replace(`/platform/groups/${id}`)
    } else {
      errorMsg.value = '创建成功但未返回团体 ID'
    }
  } catch (err) {
    errorMsg.value = err?.message || '创建失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.create-page {
  width: min(100%, 640px);
  margin: var(--lc-space-4) auto 0;
  padding: var(--lc-space-8);
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius);
  background: var(--lc-surface);
  box-shadow: var(--lc-shadow-sm);
}

.back-link {
  display: inline-block;
  margin-bottom: var(--lc-space-3);
  color: var(--lc-blue);
  font-weight: 800;
  text-decoration: none;
}

.page-head h1 {
  margin: 0;
  color: var(--lc-text);
  font-size: 28px;
}

.page-head p {
  margin: var(--lc-space-2) 0 0;
  color: var(--lc-muted);
  font-weight: 700;
}

.create-form {
  display: grid;
  gap: var(--lc-space-5);
  margin-top: var(--lc-space-6);
}

.field {
  display: grid;
  gap: var(--lc-space-2);
}

.label-text {
  color: var(--lc-text);
  font-weight: 900;
}

.label-text em {
  color: var(--lc-red);
  font-style: normal;
}

input[type='text'],
input:not([type]),
select,
textarea {
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius-xs);
  padding: var(--lc-space-3);
  font: inherit;
  font-weight: 700;
  color: var(--lc-text);
  background: var(--lc-surface);
}

textarea {
  resize: vertical;
  min-height: 120px;
}

.file-input {
  font-size: var(--lc-text-sm);
}

.hint {
  margin: 0;
  color: var(--lc-muted);
  font-size: var(--lc-text-sm);
  font-weight: 700;
}

.preview-row {
  display: flex;
  align-items: center;
  gap: var(--lc-space-3);
}

.thumb {
  width: 120px;
  height: 72px;
  border-radius: var(--lc-radius-xs);
  object-fit: cover;
  border: 1px solid var(--lc-border);
}

.join-field {
  display: flex;
  flex-wrap: wrap;
  gap: var(--lc-space-4);
  margin: 0;
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius-xs);
  padding: var(--lc-space-3) var(--lc-space-4);
}

.join-field label {
  display: flex;
  align-items: center;
  gap: var(--lc-space-2);
  color: var(--lc-muted);
  font-weight: 800;
}

.err {
  margin: 0;
  padding: var(--lc-space-2) var(--lc-space-3);
  border-radius: var(--lc-radius-xs);
  color: var(--lc-red);
  background: var(--lc-red-light);
  font-weight: 800;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--lc-space-3);
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 42px;
  padding: 0 var(--lc-space-5);
  border-radius: var(--lc-radius-xs);
  font-weight: 900;
  text-decoration: none;
  cursor: pointer;
}

.btn.primary {
  border: 0;
  color: var(--lc-surface);
  background: linear-gradient(135deg, var(--lc-pink), var(--lc-blue));
  box-shadow: var(--lc-shadow-blue);
}

.btn.primary:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.btn.ghost {
  border: 1px solid var(--lc-border);
  color: var(--lc-blue);
  background: var(--lc-surface);
}
</style>
