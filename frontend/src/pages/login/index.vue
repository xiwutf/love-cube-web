<template>
  <div class="login-page">
    <!-- Logo 区 -->
    <div class="logo-area">
      <div class="logo-icon">💝</div>
      <h1 class="logo-title">Love Cube</h1>
      <p class="logo-sub">遇见你，是最好的事</p>
    </div>

    <!-- Tab 切换：登录 / 注册 -->
    <van-tabs v-model:active="activeTab" class="login-tabs" color="#FF6B8A" title-active-color="#FF6B8A">
      <!-- 登录 -->
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
            <van-button
              round
              block
              type="primary"
              native-type="submit"
              :loading="loading"
              loading-text="登录中..."
            >
              登录
            </van-button>
          </div>
        </van-form>
      </van-tab>

      <!-- 注册 -->
      <van-tab title="注册">
        <van-form @submit="handleRegister" class="form-wrap">
          <van-cell-group inset>
            <van-field
              v-model="regForm.username"
              name="username"
              label="昵称"
              placeholder="给自己起个名字吧"
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
            <van-button
              round
              block
              type="primary"
              native-type="submit"
              :loading="loading"
              loading-text="注册中..."
            >
              注册
            </van-button>
          </div>
        </van-form>
      </van-tab>
    </van-tabs>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { login, register } from '@/api/auth.js'
import { useUserStore } from '@/stores/user.js'

const router    = useRouter()
const userStore = useUserStore()

const activeTab = ref(0)
const loading   = ref(false)

const loginForm = reactive({ phone: '', password: '' })
const regForm   = reactive({ username: '', phone: '', password: '' })

async function handleLogin() {
  loading.value = true
  try {
    const res = await login({ phone: loginForm.phone, password: loginForm.password })
    userStore.setAuth(res.token, res.userId)
    showToast({ message: '登录成功', type: 'success' })
    router.replace('/')
  } catch (err) {
    showToast({ message: err.message || '登录失败', type: 'fail' })
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  loading.value = true
  try {
    const res = await register({
      phone:    regForm.phone,
      password: regForm.password,
      username: regForm.username
    })
    userStore.setAuth(res.token, res.userId)
    showToast({ message: '注册成功', type: 'success' })
    router.replace('/')
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
  padding: 60px 0 32px;
}

.logo-icon {
  font-size: 60px;
  line-height: 1;
  margin-bottom: 12px;
}

.logo-title {
  font-size: 28px;
  font-weight: 700;
  color: #FF6B8A;
  letter-spacing: 2px;
}

.logo-sub {
  margin-top: 8px;
  font-size: 13px;
  color: #aaa;
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
</style>
