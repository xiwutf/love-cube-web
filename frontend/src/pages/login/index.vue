<template>
  <div class="login-page">
    <div class="logo-area">
      <div class="logo-icon">💕</div>
      <h1 class="logo-title">Love Cube</h1>
      <p class="logo-sub">统一用户登录中心</p>
    </div>

    <van-tabs v-model:active="activeTab" class="login-tabs" color="#FF6B8A" title-active-color="#FF6B8A">
      <van-tab title="登录">
        <van-form @submit="handleLogin" class="form-wrap">
          <van-cell-group inset>
            <van-field
              v-model="loginForm.phone"
              name="phone"
              label="手机号"
              placeholder="请输入手机号"
              type="tel"
              maxlength="11"
              :rules="[{ required: true, message: '请填写手机号' }]"
            />
            <van-field
              v-model="loginForm.password"
              name="password"
              label="密码"
              placeholder="请输入密码"
              type="password"
              :rules="[{ required: true, message: '请填写密码' }]"
            />
          </van-cell-group>
          <div class="btn-wrap">
            <van-button round block type="primary" native-type="submit" :loading="loading" loading-text="登录中...">
              登录
            </van-button>
          </div>
        </van-form>
      </van-tab>

      <van-tab title="注册">
        <van-form @submit="handleRegister" class="form-wrap">
          <van-cell-group inset>
            <van-field
              v-model="regForm.username"
              name="username"
              label="昵称"
              placeholder="给自己起个名字"
            />
            <van-field
              v-model="regForm.phone"
              name="phone"
              label="手机号"
              placeholder="请输入手机号"
              type="tel"
              maxlength="11"
              :rules="[{ required: true, message: '请填写手机号' }]"
            />
            <van-field
              v-model="regForm.password"
              name="password"
              label="密码"
              placeholder="至少 6 位"
              type="password"
              :rules="[{ required: true, message: '请填写密码' }, { min: 6, message: '密码至少 6 位' }]"
            />
          </van-cell-group>
          <div class="btn-wrap">
            <van-button round block type="primary" native-type="submit" :loading="loading" loading-text="注册中...">
              注册
            </van-button>
          </div>
        </van-form>
      </van-tab>
    </van-tabs>

    <p class="hint">演示账号：admin 13800000000 / 123456，普通用户 13900000000 / 123456</p>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useUserStore } from '@/stores/user.js'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeTab = ref(0)
const loading = ref(false)
const loginForm = reactive({ phone: '', password: '' })
const regForm = reactive({ username: '', phone: '', password: '' })

function resolveRedirect() {
  const fromQuery = typeof route.query.redirect === 'string' ? route.query.redirect : ''
  const fromStore = userStore.consumePostLoginRedirect()
  const target = fromQuery || fromStore
  if (target) return decodeURIComponent(target)
  return route.path.startsWith('/fellowship') ? '/fellowship/discover' : '/account'
}

async function handleLogin() {
  loading.value = true
  try {
    await userStore.login({ phone: loginForm.phone, password: loginForm.password })
    showToast({ message: '登录成功', type: 'success' })
    router.replace(resolveRedirect())
  } catch (err) {
    showToast({ message: err.message || '登录失败', type: 'fail' })
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  loading.value = true
  try {
    await userStore.register({
      username: regForm.username,
      phone: regForm.phone,
      password: regForm.password
    })
    showToast({ message: '注册成功', type: 'success' })
    router.replace(resolveRedirect())
  } catch (err) {
    showToast({ message: err.message || '注册失败', type: 'fail' })
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(160deg, #fff0f3 0%, #ffffff 40%);
  padding-bottom: 40px;
}

.logo-area {
  text-align: center;
  padding: 56px 0 28px;
}

.logo-icon {
  font-size: 56px;
  line-height: 1;
  margin-bottom: 10px;
}

.logo-title {
  font-size: 28px;
  font-weight: 700;
  color: #ff6b8a;
  letter-spacing: 2px;
}

.logo-sub {
  margin-top: 8px;
  font-size: 13px;
  color: #7b889b;
}

.login-tabs {
  padding: 0 4px;
}

.form-wrap {
  padding: 16px 0;
}

.btn-wrap {
  padding: 24px 16px 0;
}

.hint {
  margin: 18px 18px 0;
  font-size: 12px;
  color: #94a3b8;
  line-height: 1.6;
}
</style>
