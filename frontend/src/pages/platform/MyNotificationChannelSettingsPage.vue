<template>
  <section class="sp-page">
    <header class="sp-head">
      <button type="button" class="sp-back" aria-label="返回" @click="$router.back()">‹</button>
      <h1 class="sp-title">通知渠道设置</h1>
    </header>

    <div class="sp-body">
      <p class="nc-hint">
        开启后，站内消息将同步投递至邮件或 PushPlus（微信）。PushPlus 需在
        <a href="https://www.pushplus.plus/" target="_blank" rel="noopener noreferrer">pushplus.plus</a>
        获取 Token，本功能不接微信公众号/服务号/企业微信。
      </p>

      <div class="sp-card">
        <div class="sp-card-title">邮件通知</div>
        <div class="nc-switch-row">
          <div>
            <div class="nc-label">开启邮件通知</div>
            <div class="nc-sub">
              {{ prefs.hasEmail ? `将发送至 ${prefs.emailMasked}` : '请先在账号中绑定邮箱' }}
            </div>
          </div>
          <button
            type="button"
            class="nc-switch"
            :class="{ on: prefs.emailEnabled }"
            :disabled="!prefs.hasEmail"
            @click="prefs.emailEnabled = !prefs.emailEnabled"
          >
            <span class="nc-switch-thumb"></span>
          </button>
        </div>
      </div>

      <div class="sp-card">
        <div class="sp-card-title">PushPlus 微信推送</div>
        <div class="nc-switch-row">
          <div>
            <div class="nc-label">开启 PushPlus</div>
            <div class="nc-sub">通过 PushPlus 将通知推送到微信</div>
          </div>
          <button
            type="button"
            class="nc-switch"
            :class="{ on: prefs.pushplusEnabled }"
            @click="prefs.pushplusEnabled = !prefs.pushplusEnabled"
          >
            <span class="nc-switch-thumb"></span>
          </button>
        </div>
        <label class="nc-field">
          <span class="nc-field-label">PushPlus Token</span>
          <input
            v-model="prefs.pushplusToken"
            type="text"
            class="nc-input"
            placeholder="在 pushplus.plus 个人中心复制"
            autocomplete="off"
          />
        </label>
      </div>

      <p v-if="message" class="nc-msg" :class="{ error: messageError }">{{ message }}</p>

      <button type="button" class="sp-primary-btn" :disabled="saving" @click="save">
        {{ saving ? '保存中…' : '保存设置' }}
      </button>
      <button type="button" class="sp-secondary-btn" :disabled="testing" @click="sendTest">
        {{ testing ? '发送中…' : '发送测试通知' }}
      </button>
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import {
  getNotificationChannelPrefs,
  putNotificationChannelPrefs,
  postNotificationChannelTest
} from '@/api/notificationChannelPrefs.js'

const prefs = reactive({
  emailEnabled: false,
  pushplusEnabled: false,
  pushplusToken: '',
  hasEmail: false,
  emailMasked: ''
})

const saving = ref(false)
const testing = ref(false)
const message = ref('')
const messageError = ref(false)

function flash(text, isError = false) {
  message.value = text
  messageError.value = isError
  setTimeout(() => { message.value = '' }, 4000)
}

async function load() {
  try {
    const data = await getNotificationChannelPrefs()
    prefs.emailEnabled = !!data.emailEnabled
    prefs.pushplusEnabled = !!data.pushplusEnabled
    prefs.pushplusToken = data.pushplusToken || ''
    prefs.hasEmail = !!data.hasEmail
    prefs.emailMasked = data.emailMasked || ''
  } catch (e) {
    flash(e.message || '加载失败', true)
  }
}

async function save() {
  if (prefs.pushplusEnabled && !prefs.pushplusToken.trim()) {
    flash('开启 PushPlus 前请填写 Token', true)
    return
  }
  saving.value = true
  try {
    await putNotificationChannelPrefs({
      emailEnabled: prefs.emailEnabled,
      pushplusEnabled: prefs.pushplusEnabled,
      pushplusToken: prefs.pushplusToken.trim()
    })
    flash('已保存')
  } catch (e) {
    flash(e.message || '保存失败', true)
  } finally {
    saving.value = false
  }
}

async function sendTest() {
  testing.value = true
  try {
    const res = await postNotificationChannelTest()
    flash(res.message || '测试通知已发送')
  } catch (e) {
    flash(e.message || '发送失败', true)
  } finally {
    testing.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.nc-hint {
  margin: 0 0 12px;
  font-size: 13px;
  line-height: 1.55;
  color: var(--lc-text-secondary, #646566);
}
.nc-hint a {
  color: var(--lc-blue, #1989fa);
}
.nc-switch-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 4px 0 12px;
}
.nc-label {
  font-size: 15px;
  color: var(--lc-text, #323233);
}
.nc-sub {
  margin-top: 4px;
  font-size: 12px;
  color: var(--lc-text-secondary, #969799);
}
.nc-switch {
  position: relative;
  width: 44px;
  height: 26px;
  border: none;
  border-radius: 13px;
  background: #dcdee0;
  cursor: pointer;
  flex-shrink: 0;
  transition: background 0.2s;
}
.nc-switch:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}
.nc-switch.on {
  background: var(--lc-pink, #ee5a8f);
}
.nc-switch-thumb {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #fff;
  transition: transform 0.2s;
}
.nc-switch.on .nc-switch-thumb {
  transform: translateX(18px);
}
.nc-field {
  display: block;
  margin-top: 8px;
}
.nc-field-label {
  display: block;
  margin-bottom: 6px;
  font-size: 13px;
  color: var(--lc-text-secondary, #646566);
}
.nc-input {
  width: 100%;
  box-sizing: border-box;
  padding: 10px 12px;
  border: 1px solid var(--lc-border, #ebedf0);
  border-radius: 8px;
  font-size: 14px;
}
.nc-msg {
  margin: 8px 0;
  font-size: 13px;
  color: var(--lc-green, #07c160);
  text-align: center;
}
.nc-msg.error {
  color: var(--lc-red, #ee0a24);
}
.sp-secondary-btn {
  display: block;
  width: 100%;
  margin-top: 10px;
  padding: 12px;
  border: 1px solid var(--lc-border, #ebedf0);
  border-radius: 10px;
  background: #fff;
  font-size: 15px;
  color: var(--lc-text, #323233);
  cursor: pointer;
}
.sp-secondary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
