<template>
  <div class="ai-tools">
    <NavBar title="AI 助手" />

    <section class="block">
      <h2>资料润色</h2>
      <p class="hint">根据你的简介生成 3 版不同风格文案（规则模板，无需外网）</p>
      <van-field v-model="bio" rows="3" type="textarea" maxlength="300" placeholder="粘贴或输入当前简介" />
      <van-field v-model="nickname" maxlength="30" placeholder="昵称（选填）" />
      <van-button round block type="primary" color="#ff5f84" :loading="polishLoading" @click="runPolish">
        生成建议
      </van-button>
      <div v-for="(line, idx) in suggestions" :key="idx" class="suggestion" @click="copyText(line)">
        <p>{{ line }}</p>
        <span>点击复制</span>
      </div>
      <ul v-if="tips.length" class="tips">
        <li v-for="(t, i) in tips" :key="i">{{ t }}</li>
      </ul>
    </section>

    <section class="block">
      <h2>破冰话术</h2>
      <van-tabs v-model:active="sceneTab" color="#ff5f84" @change="loadPhrases">
        <van-tab title="匹配" name="match" />
        <van-tab title="互助" name="help" />
        <van-tab title="活动" name="event" />
      </van-tabs>
      <div v-for="(p, idx) in phrases" :key="idx" class="suggestion" @click="copyText(p)">
        <p>{{ p }}</p>
        <span>点击复制</span>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { showToast } from 'vant'
import NavBar from '@/components/NavBar.vue'
import { fetchIcebreakerHints, polishProfile } from '@/api/aiAssist.js'

const bio = ref('')
const nickname = ref('')
const polishLoading = ref(false)
const suggestions = ref([])
const tips = ref([])
const sceneTab = ref('match')
const phrases = ref([])

async function runPolish() {
  polishLoading.value = true
  try {
    const res = await polishProfile({ bio: bio.value, nickname: nickname.value })
    suggestions.value = Array.isArray(res?.suggestions) ? res.suggestions : []
    tips.value = Array.isArray(res?.tips) ? res.tips : []
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '生成失败' })
  } finally {
    polishLoading.value = false
  }
}

async function loadPhrases() {
  try {
    const res = await fetchIcebreakerHints(sceneTab.value)
    phrases.value = Array.isArray(res?.phrases) ? res.phrases : []
  } catch {
    phrases.value = []
  }
}

async function copyText(text) {
  try {
    await navigator.clipboard.writeText(text)
    showToast({ type: 'success', message: '已复制' })
  } catch {
    showToast(text)
  }
}

onMounted(loadPhrases)
</script>

<style scoped>
.ai-tools {
  min-height: 100vh;
  background: #f8f9fb;
  padding-bottom: 24px;
}

.block {
  margin: 12px;
  padding: 14px;
  background: #fff;
  border-radius: 14px;
}

.block h2 {
  margin: 0 0 6px;
  font-size: 16px;
}

.hint {
  margin: 0 0 10px;
  font-size: 12px;
  color: #64748b;
}

.suggestion {
  margin-top: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  background: #fff7fb;
  border: 1px solid #fbcfe8;
}

.suggestion p {
  margin: 0;
  font-size: 13px;
  line-height: 1.55;
  color: #334155;
}

.suggestion span {
  display: block;
  margin-top: 6px;
  font-size: 11px;
  color: #ff5f84;
}

.tips {
  margin: 12px 0 0;
  padding-left: 18px;
  font-size: 12px;
  color: #64748b;
}
</style>
