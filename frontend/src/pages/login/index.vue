<template>
  <div :class="['login-page', isFellowshipRoute ? 'is-fellowship' : 'is-platform']">
    <div class="login-shell">
      <section class="login-card">
        <div class="logo-area">
          <span class="logo-mark" aria-hidden="true">LC</span>
          <h1 class="logo-title">Love Cube</h1>
          <p class="logo-sub">Í³Ò»ÓÃ»§µÇÂ¼ÖÐÐÄ</p>
        </div>

        <van-tabs v-model:active="activeTab" class="login-tabs" color="#FF6B8A" title-active-color="#FF6B8A">
          <van-tab title="µÇÂ¼">
            <van-form @submit="handleLogin" class="form-wrap">
              <van-cell-group inset>
                <van-field
                  v-model="loginForm.phone"
                  name="phone"
                  label="ÊÖ»úºÅ"
                  placeholder="ÇëÊäÈëÊÖ»úºÅ"
                  type="tel"
                  maxlength="11"
                  :rules="[{ required: true, message: 'ÇëÌîÐ´ÊÖ»úºÅ' }]"
                />
                <van-field
                  v-model="loginForm.password"
                  name="password"
                  label="ÃÜÂë"
                  placeholder="ÇëÊäÈëÃÜÂë"
                  type="password"
                  :rules="[{ required: true, message: 'ÇëÌîÐ´ÃÜÂë' }]"
                />
              </van-cell-group>
              <div class="btn-wrap">
                <van-button round block type="primary" native-type="submit" :loading="loading" loading-text="µÇÂ¼ÖÐ...">
                  µÇÂ¼
                </van-button>
              </div>
            </van-form>
          </van-tab>

          <van-tab title="×¢²á">
            <van-form @submit="handleRegister" class="form-wrap">
              <van-cell-group inset>
                <van-field
                  v-model="regForm.username"
                  name="username"
                  label="êÇ³Æ"
                  placeholder="¸ø×Ô¼ºÆð¸öÃû×Ö"
                />
                <van-field
                  v-model="regForm.phone"
                  name="phone"
                  label="ÊÖ»úºÅ"
                  placeholder="ÇëÊäÈëÊÖ»úºÅ"
                  type="tel"
                  maxlength="11"
                  :rules="[{ required: true, message: 'ÇëÌîÐ´ÊÖ»úºÅ' }]"
                />
                <van-field
                  v-model="regForm.password"
                  name="password"
                  label="ÃÜÂë"
                  placeholder="ÖÁÉÙ 6 Î»"
                  type="password"
                  :rules="[{ required: true, message: 'ÇëÌîÐ´ÃÜÂë' }, { min: 6, message: 'ÃÜÂëÖÁÉÙ 6 Î»' }]"
                />
              </van-cell-group>
              <div class="btn-wrap">
                <van-button round block type="primary" native-type="submit" :loading="loading" loading-text="×¢²áÖÐ...">
                  ×¢²á
                </van-button>
              </div>
            </van-form>
          </van-tab>
        </van-tabs>

        <p class="hint">ÑÝÊ¾ÕËºÅ£ºadmin 13800000000 / 123456£¬ÆÕÍ¨ÓÃ»§ 13900000000 / 123456</p>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
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
const isFellowshipRoute = computed(() => route.path.startsWith('/fellowship'))

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
    showToast({ message: 'µÇÂ¼³É¹¦', type: 'success' })
    router.replace(resolveRedirect())
  } catch (err) {
    showToast({ message: err.message || 'µÇÂ¼Ê§°Ü', type: 'fail' })
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
    showToast({ message: '×¢²á³É¹¦', type: 'success' })
    router.replace(resolveRedirect())
  } catch (err) {
    showToast({ message: err.message || '×¢²áÊ§°Ü', type: 'fail' })
  } finally {
    loading.value = false
  }
}
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
  border-radius: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(145deg, #ff6f92, #e84f73);
  color: #fff;
  font-size: 18px;
  font-weight: 800;
  letter-spacing: 0.08em;
  box-shadow: 0 14px 26px rgba(232, 79, 115, 0.32);
}

.logo-title {
  margin-top: 14px;
  font-size: 30px;
  font-weight: 800;
  color: #1f2a44;
  letter-spacing: -0.01em;
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

:deep(.van-button--primary) {
  font-weight: 700;
  box-shadow: 0 14px 24px rgba(255, 107, 138, 0.28);
}

:deep(.van-cell-group--inset) {
  border-radius: 16px;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.08);
  border: 1px solid #ecf0f4;
}

.hint {
  margin: 20px 18px 0;
  font-size: 12px;
  color: #94a3b8;
  line-height: 1.6;
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
    border-radius: 14px;
    font-size: 17px;
  }

  .logo-title {
    font-size: 28px;
  }

  .logo-sub {
    font-size: 13px;
  }
}
</style>
