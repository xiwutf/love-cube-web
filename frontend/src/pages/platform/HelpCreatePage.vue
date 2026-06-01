<template>
  <section class="help-create">
    <header class="page-head">
      <h1>发布互助需求</h1>
      <p>提交后进入「待审核」，审核通过后将展示在互助广场。</p>
    </header>

    <form class="form" @submit.prevent="submit">
      <label class="field">
        <span>需求类型 <em>*</em></span>
        <select v-model="form.helpType" required>
          <option v-for="o in typeOptions" :key="o.value" :value="o.value">{{ o.label }}</option>
        </select>
      </label>
      <label class="field">
        <span>标题 <em>*</em></span>
        <input v-model.trim="form.title" maxlength="200" required placeholder="一句话说明你的需求">
      </label>
      <label class="field">
        <span>详细描述 <em>*</em></span>
        <textarea v-model.trim="form.content" rows="8" required maxlength="5000" placeholder="背景、期望、限制条件等"></textarea>
      </label>
      <label class="field">
        <span>地区</span>
        <input v-model.trim="form.region" maxlength="100" placeholder="选填，如：上海">
      </label>
      <div class="field grid2">
        <label>
          <span>联系方式类型</span>
          <input v-model.trim="form.contactType" maxlength="32" placeholder="如：微信 / 手机">
        </label>
        <label>
          <span>联系方式</span>
          <input v-model.trim="form.contactValue" maxlength="200" placeholder="选填">
        </label>
      </div>
      <label class="check">
        <input v-model="form.contactPublic" type="checkbox">
        在详情页向他人公开我的联系方式（默认仅对已确认互助对象展示）
      </label>
      <label class="field">
        <span>期望完成日期</span>
        <input v-model="form.deadline" type="date">
      </label>
      <p v-if="message" class="msg" :class="{ err: messageType === 'error' }">{{ message }}</p>
      <div class="actions">
        <button type="submit" class="btn primary" :disabled="saving">{{ saving ? '提交中…' : '提交审核' }}</button>
        <router-link :to="helpPath()" class="btn ghost">返回广场</router-link>
      </div>
    </form>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { createHelpRequest } from '@/api/help.js'
import { usePlatformPath } from '@/composables/usePlatformPath.js'

const router = useRouter()
const { helpPath } = usePlatformPath()
const saving = ref(false)
const message = ref('')
const messageType = ref('')

const typeOptions = [
  { value: 'JOB_SEEK', label: '找工作' },
  { value: 'RECRUIT', label: '招人' },
  { value: 'FIND_MATERIAL', label: '找资料' },
  { value: 'OFFER_RESOURCE', label: '提供资源' },
  { value: 'ASK_EXP', label: '求经验' },
  { value: 'OTHER', label: '其他' }
]

const form = reactive({
  helpType: 'OTHER',
  title: '',
  content: '',
  region: '',
  contactType: '',
  contactValue: '',
  contactPublic: false,
  deadline: ''
})

async function submit() {
  saving.value = true
  message.value = ''
  messageType.value = ''
  try {
    const payload = {
      helpType: form.helpType,
      title: form.title,
      content: form.content,
      region: form.region || undefined,
      contactType: form.contactType || undefined,
      contactValue: form.contactValue || undefined,
      contactPublic: form.contactPublic,
      deadline: form.deadline || undefined
    }
    const res = await createHelpRequest(payload)
    const id = res?.id
    if (id) {
      router.replace(helpPath(String(id)))
      return
    }
    message.value = '已提交'
    messageType.value = 'ok'
  } catch (e) {
    message.value = e.message || '提交失败'
    messageType.value = 'error'
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.help-create {
  max-width: 640px;
  margin: 0 auto;
  padding: 1.5rem 1rem 3rem;
  color: var(--lc-text);
}

.page-head h1 {
  margin: 0 0 0.35rem;
  font-size: 1.5rem;
}

.page-head p {
  margin: 0 0 1.25rem;
  color: var(--lc-muted);
  line-height: 1.5;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.field span {
  display: block;
  margin-bottom: 0.35rem;
  font-weight: 600;
  font-size: 0.9rem;
}

.field em {
  color: var(--lc-red);
}

.field input,
.field select,
.field textarea {
  width: 100%;
  box-sizing: border-box;
  padding: 0.55rem 0.65rem;
  border-radius: 8px;
  border: 1px solid var(--lc-border);
  font: inherit;
  background: var(--lc-surface);
}

.field textarea {
  resize: vertical;
  min-height: 140px;
}

.grid2 {
  display: grid;
  gap: 0.75rem;
}

@media (min-width: 560px) {
  .grid2 {
    grid-template-columns: 1fr 1fr;
  }
}

.check {
  font-size: 0.88rem;
  color: var(--lc-muted);
  display: flex;
  gap: 0.5rem;
  align-items: flex-start;
}

.msg {
  margin: 0;
  font-size: 0.9rem;
}

.msg.err {
  color: var(--lc-red);
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.55rem 1.1rem;
  border-radius: 8px;
  font-weight: 600;
  text-decoration: none;
  border: 1px solid transparent;
  cursor: pointer;
}

.btn.primary {
  background: var(--lc-blue);
  color: #fff;
  border: none;
}

.btn.primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn.ghost {
  background: var(--lc-surface);
  color: var(--lc-blue);
  border-color: var(--lc-blue-border);
}
</style>
