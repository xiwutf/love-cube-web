<template>
  <div class="change-password-page">
    <NavBar title="修改密码" />

    <section class="panel">
      <van-field
        v-model.trim="form.oldPassword"
        type="password"
        clearable
        label="旧密码"
        placeholder="请输入当前密码"
      />
      <van-field
        v-model.trim="form.newPassword"
        type="password"
        clearable
        label="新密码"
        placeholder="请输入新密码（至少 6 位）"
      />
      <van-field
        v-model.trim="form.confirmPassword"
        type="password"
        clearable
        label="确认密码"
        placeholder="请再次输入新密码"
      />

      <p v-if="message" class="message" :class="{ 'is-error': isError }">{{ message }}</p>

      <van-button
        block
        type="primary"
        :loading="submitting"
        :disabled="submitting"
        @click="handleSubmit"
      >
        确认修改
      </van-button>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import NavBar from '@/components/NavBar.vue'
import { changePassword } from '@/api/auth.js'

const router = useRouter()
const submitting = ref(false)
const message = ref('')
const isError = ref(false)
const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

function validate() {
  if (!form.oldPassword) return '请输入旧密码'
  if (!form.newPassword) return '请输入新密码'
  if (form.newPassword.length < 6) return '新密码至少 6 位'
  if (!form.confirmPassword) return '请确认新密码'
  if (form.newPassword !== form.confirmPassword) return '两次输入的新密码不一致'
  if (form.oldPassword === form.newPassword) return '新密码不能与旧密码相同'
  return ''
}

async function handleSubmit() {
  message.value = ''
  isError.value = false
  const validationError = validate()
  if (validationError) {
    isError.value = true
    message.value = validationError
    return
  }

  submitting.value = true
  try {
    const res = await changePassword({
      oldPassword: form.oldPassword,
      newPassword: form.newPassword,
      confirmPassword: form.confirmPassword
    })
    showToast({ type: 'success', message: res?.message || '密码修改成功' })
    form.oldPassword = ''
    form.newPassword = ''
    form.confirmPassword = ''
    router.back()
  } catch (error) {
    isError.value = true
    message.value = error?.message || '修改失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.change-password-page {
  min-height: 100vh;
  background: #f8f8f8;
}

.panel {
  margin: 12px;
  padding: 12px;
  border-radius: 12px;
  background: #fff;
  display: grid;
  gap: 10px;
}

.message {
  margin: 4px 4px 0;
  font-size: 12px;
  color: #07c160;
}

.message.is-error {
  color: #ee0a24;
}
</style>
