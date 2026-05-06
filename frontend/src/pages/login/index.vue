<template>
  <div :class="['login-page', isFellowshipRoute ? 'is-fellowship' : 'is-platform']">
    <div class="login-shell">
      <section class="login-card">
        <div class="logo-area">
          <img class="logo-mark" :src="loveCubeIcon" alt="Love Cube">
          <h1 class="logo-title">欢迎来到 Love Cube</h1>
          <p class="logo-sub">登录后即可继续你的平台体验</p>
        </div>

        <van-tabs v-model:active="activeTab" class="login-tabs" color="#ff6b8a" title-active-color="#ff6b8a">
          <van-tab title="登录">
            <van-form class="form-wrap" @submit="handleLogin">
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
                  :rules="[{ required: true, message: '请输入密码' }]"
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
            <van-form class="form-wrap" @submit="handleRegister">
              <van-cell-group inset>
                <van-field
                  v-model="regForm.username"
                  :maxlength="USERNAME_MAX_LENGTH"
                  name="username"
                  label="昵称"
                  placeholder="给自己起一个昵称"
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
                  :rules="[{ required: true, message: '请输入密码' }, { min: 6, message: '密码至少 6 位' }]"
                />
                <van-field
                  v-model="regForm.inviteCode"
                  name="inviteCode"
                  label="邀请码"
                  placeholder="可选，扫码会自动带入"
                />
                <van-field
                  v-model="regForm.gender"
                  name="gender"
                  label="性别"
                  placeholder="请选择性别"
                  :rules="[{ required: true, message: '请选择性别' }]"
                >
                  <template #input>
                    <select v-model="regForm.gender" class="gender-select">
                      <option value="" disabled>请选择性别</option>
                      <option value="male">男</option>
                      <option value="female">女</option>
                    </select>
                  </template>
                </van-field>
              </van-cell-group>
              <p class="register-tip">注册后默认仅开通平台账号；联谊功能需本人手动开启。</p>
              <p class="invite-tip">如果你通过邀请二维码进入，邀请码会自动填入，也可以手动修改。</p>
              <div class="btn-wrap">
                <van-button round block type="primary" native-type="submit" :loading="loading" loading-text="注册中...">
                  注册
                </van-button>
              </div>
            </van-form>
          </van-tab>
        </van-tabs>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import { useUserStore } from '@/stores/user.js'
import loveCubeIcon from '@/assets/brand/love-cube-icon.svg'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeTab = ref(0)
const loading = ref(false)
const USERNAME_MAX_LENGTH = 20
const loginForm = reactive({ phone: '', password: '' })
const regForm = reactive({ username: '', phone: '', password: '', inviteCode: '', gender: '' })
const isFellowshipRoute = computed(() => route.path.startsWith('/fellowship'))

function applyInviteCodeFromRoute() {
  const code = typeof route.query.inviteCode === 'string' ? route.query.inviteCode.trim() : ''
  if (code) {
    regForm.inviteCode = code
    activeTab.value = 1
  } else if (route.path === '/register') {
    activeTab.value = 1
  }
}

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
    showToast({ message: err.message || '登录失败，请稍后重试', type: 'fail' })
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  const username = regForm.username.trim()
  if (username.length > USERNAME_MAX_LENGTH) {
    showToast({ message: `昵称最多 ${USERNAME_MAX_LENGTH} 个字符`, type: 'fail' })
    return
  }
  loading.value = true
  try {
    await userStore.register({
      username,
      phone: regForm.phone,
      password: regForm.password,
      inviteCode: regForm.inviteCode.trim(),
      gender: regForm.gender
    })
    showToast({ message: '注册成功，欢迎来到 Love Cube', type: 'success' })
    await showConfirmDialog({
      title: '注册完成',
      message: '是否现在去上传生活照并开通联谊？（开通前至少需要一张生活照）',
      confirmButtonText: '去开通',
      cancelButtonText: '稍后再说'
    })
    router.replace('/fellowship')
  } catch (err) {
    // Vant showConfirmDialog：点取消会 reject 字符串 'cancel'，不是 Error#name === 'Cancel'
    if (err === 'cancel' || err?.name === 'Cancel') {
      router.replace('/')
      return
    }
    showToast({ message: (typeof err?.message === 'string' && err.message) || '注册失败，请稍后重试', type: 'fail' })
  } finally {
    loading.value = false
  }
}

watch(
  () => [route.path, route.query.inviteCode],
  applyInviteCodeFromRoute
)

onMounted(applyInviteCodeFromRoute)
</script>

<style scoped>
.login-page {
  min-height: 100vh;
}

.login-shell {
  width: min(100%, 520px);
  margin: 0 auto;
  padding: 18px 0 36px;
}

.login-card {
  background: transparent;
}

.logo-area {
  text-align: center;
  padding: 18px 0 24px;
}

.logo-mark {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: inline-block;
  object-fit: cover;
  box-shadow: 0 14px 26px rgba(232, 79, 115, 0.32);
}

.logo-title {
  margin-top: 14px;
  font-size: 30px;
  font-weight: 800;
  color: #1f2a44;
  letter-spacing: 0;
}

.logo-sub {
  margin-top: 8px;
  font-size: 14px;
  color: #64748b;
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

.register-tip,
.invite-tip {
  margin: 12px 18px 0;
  color: #475569;
  font-size: 12px;
  line-height: 1.6;
}

.invite-tip {
  margin-top: 8px;
  color: #f43f5e;
}

:deep(.van-button--primary) {
  font-weight: 700;
  box-shadow: 0 14px 24px rgba(255, 107, 138, 0.28);
}

:deep(.van-cell-group--inset) {
  border-radius: 16px;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.08);
  border: 1px solid #ecf0f4;
}

.gender-select {
  width: 100%;
  height: 32px;
  border: 0;
  background: transparent;
  color: #323233;
  font-size: 14px;
  outline: none;
}

.is-platform {
  background:
    radial-gradient(circle at 12% 4%, rgba(255, 111, 146, 0.18) 0, rgba(255, 111, 146, 0) 34%),
    linear-gradient(165deg, #f6f8fd 0%, #ffffff 48%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 36px 12px;
}

.is-platform .login-shell {
  width: min(100%, 560px);
  padding: 0;
}

.is-platform .login-card {
  border: 1px solid #e8edf3;
  border-radius: 22px;
  background: #ffffff;
  box-shadow: 0 24px 48px rgba(15, 23, 42, 0.12);
  padding: 18px 18px 24px;
}

.is-fellowship {
  background: linear-gradient(160deg, #fff0f3 0%, #ffffff 40%);
  padding-bottom: 40px;
}

@media (max-width: 767px) {
  .is-platform {
    display: block;
    padding: 0 0 22px;
    background: linear-gradient(160deg, #fff0f3 0%, #ffffff 40%);
  }

  .is-platform .login-shell {
    width: 100%;
    padding: 14px 0 24px;
  }

  .is-platform .login-card {
    border: 0;
    border-radius: 0;
    box-shadow: none;
    background: transparent;
    padding: 0;
  }

  .logo-mark {
    width: 52px;
    height: 52px;
  }

  .logo-title {
    font-size: 26px;
  }

  .logo-sub {
    font-size: 13px;
  }
}
</style>
