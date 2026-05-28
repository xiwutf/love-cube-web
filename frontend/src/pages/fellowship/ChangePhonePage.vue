<template>
  <div class="change-phone-page">
    <NavBar title="换绑手机号" />

    <section class="panel">
      <p v-if="currentPhoneMasked" class="current-phone">
        当前手机号：<strong>{{ currentPhoneMasked }}</strong>
      </p>
      <p v-else class="current-phone">当前未绑定手机号，绑定后可使用新号码登录</p>

      <van-field
        v-model.trim="form.password"
        type="password"
        clearable
        label="登录密码"
        placeholder="请输入当前登录密码"
      />
      <van-field
        v-model.trim="form.newPhone"
        type="tel"
        maxlength="11"
        clearable
        label="新手机号"
        placeholder="请输入 11 位新手机号"
      />
      <van-field
        v-model.trim="form.confirmPhone"
        type="tel"
        maxlength="11"
        clearable
        label="确认手机号"
        placeholder="请再次输入新手机号"
      />

      <p class="hint">换绑成功后，请使用新手机号登录；旧手机号将无法登录本账号。</p>

      <p v-if="message" class="message" :class="{ 'is-error': isError }">{{ message }}</p>

      <van-button
        block
        type="primary"
        :loading="submitting"
        :disabled="submitting"
        @click="handleSubmit"
      >
        确认换绑
      </van-button>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import NavBar from '@/components/NavBar.vue'
import { changePhone } from '@/api/auth.js'
import { clearMeCache } from '@/api/user.js'
import { useUserStore } from '@/stores/user.js'

const PHONE_PATTERN = /^1[3-9]\d{9}$/

const router = useRouter()
const userStore = useUserStore()
const submitting = ref(false)
const message = ref('')
const isError = ref(false)
const form = reactive({
  password: '',
  newPhone: '',
  confirmPhone: ''
})

const currentPhoneMasked = computed(() => {
  const phone = String(userStore.userInfo?.phone || '').trim()
  if (!phone) return ''
  if (phone.length < 7) return phone
  return `${phone.slice(0, 3)}****${phone.slice(-4)}`
})

function validate() {
  if (!form.password) return '请输入登录密码'
  if (!form.newPhone) return '请输入新手机号'
  if (!PHONE_PATTERN.test(form.newPhone)) return '请输入有效的 11 位中国大陆手机号'
  if (!form.confirmPhone) return '请再次输入新手机号'
  if (form.newPhone !== form.confirmPhone) return '两次输入的新手机号不一致'
  const current = String(userStore.userInfo?.phone || '').trim()
  if (current && current === form.newPhone) return '新手机号不能与当前手机号相同'
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
    const res = await changePhone({
      password: form.password,
      newPhone: form.newPhone,
      confirmPhone: form.confirmPhone
    })
    clearMeCache()
    await userStore.refreshCurrentUser().catch(() => {})
    showToast({ type: 'success', message: res?.message || '手机号已更新' })
    form.password = ''
    form.newPhone = ''
    form.confirmPhone = ''
    router.back()
  } catch (error) {
    isError.value = true
    message.value = error?.message || '换绑失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  if (!userStore.userInfo) {
    await userStore.refreshCurrentUser().catch(() => {})
  }
})
</script>

<style scoped>
.change-phone-page {
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

.current-phone {
  margin: 0 4px 4px;
  font-size: 13px;
  color: #646566;
  line-height: 1.5;
}

.current-phone strong {
  color: #323233;
  font-weight: 700;
}

.hint {
  margin: 0 4px;
  font-size: 12px;
  line-height: 1.5;
  color: #969799;
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
