<template>
  <van-popup v-model:show="showIcebreaker" position="bottom" round :style="{ maxHeight: '85vh' }">
    <div class="icebreaker-popup">
      <h3 class="icebreaker-title">破冰小问答</h3>
      <p class="icebreaker-sub">回答 3 道题，对方也完成后可互相查看</p>
      <div v-if="icebreakerLoading" class="icebreaker-loading">加载中…</div>
      <template v-else>
        <div v-for="q in icebreakerQuestions" :key="q.id" class="icebreaker-q">
          <p class="icebreaker-q-text">{{ q.questionText }}</p>
          <van-field
            v-model="icebreakerDraft[q.id]"
            type="textarea"
            rows="2"
            maxlength="200"
            placeholder="写下你的回答…"
            :readonly="icebreakerSession?.myCompleted"
          />
          <p v-if="icebreakerSession?.canViewPeerAnswers && q.peerAnswer" class="icebreaker-peer">
            对方：{{ q.peerAnswer }}
          </p>
        </div>
        <van-button
          v-if="!icebreakerSession?.myCompleted"
          round
          block
          color="#FF5F84"
          :loading="icebreakerSubmitting"
          @click="submitIcebreaker"
        >
          提交回答
        </van-button>
        <van-button v-else round block plain hairline class="secondary-btn" @click="finishAndChat">
          去聊天
        </van-button>
      </template>
    </div>
  </van-popup>

  <van-popup v-model:show="showCompatibility" position="bottom" round :style="{ maxHeight: '85vh' }">
    <div class="icebreaker-popup">
      <h3 class="icebreaker-title">默契测试</h3>
      <p class="icebreaker-sub">选最符合你的选项，双方完成后显示匹配度</p>
      <div v-if="compatibilityLoading" class="icebreaker-loading">加载中…</div>
      <template v-else>
        <div v-if="compatibilitySession?.canViewResult" class="compat-score">
          匹配度 <strong>{{ compatibilitySession.compatibilityScore }}%</strong>
        </div>
        <div v-for="q in compatibilityQuestions" :key="q.id" class="icebreaker-q">
          <p class="icebreaker-q-text">{{ q.questionText }}</p>
          <van-radio-group
            v-model="compatibilityDraft[q.id]"
            direction="vertical"
            :disabled="compatibilitySession?.myCompleted"
          >
            <van-radio v-for="opt in q.options" :key="opt.key" :name="opt.key" icon-size="16">
              {{ opt.text }}
            </van-radio>
          </van-radio-group>
          <p v-if="compatibilitySession?.canViewResult && q.peerOption" class="icebreaker-peer">
            对方选了：{{ optionLabel(q, q.peerOption) }}
          </p>
        </div>
        <van-button
          v-if="!compatibilitySession?.myCompleted"
          round
          block
          color="#FF5F84"
          :loading="compatibilitySubmitting"
          @click="submitCompatibility"
        >
          提交答案
        </van-button>
        <van-button v-else round block plain hairline class="secondary-btn" @click="finishAndChat">
          去聊天
        </van-button>
      </template>
    </div>
  </van-popup>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { showToast } from 'vant'
import {
  getIcebreakerSession,
  submitIcebreakerAnswers,
  getCompatibilitySession,
  submitCompatibilityAnswers
} from '@/api/match.js'

const props = defineProps({
  peerUserId: { type: [String, Number], default: null }
})

const emit = defineEmits(['go-chat'])

const showIcebreaker = ref(false)
const showCompatibility = ref(false)
const icebreakerLoading = ref(false)
const icebreakerSubmitting = ref(false)
const icebreakerSession = ref(null)
const icebreakerQuestions = ref([])
const icebreakerDraft = reactive({})
const compatibilityLoading = ref(false)
const compatibilitySubmitting = ref(false)
const compatibilitySession = ref(null)
const compatibilityQuestions = ref([])
const compatibilityDraft = reactive({})

function optionLabel(q, key) {
  const opt = (q?.options || []).find((o) => o.key === key)
  return opt?.text || key
}

function finishAndChat() {
  showIcebreaker.value = false
  showCompatibility.value = false
  emit('go-chat')
}

async function openIcebreaker() {
  if (!props.peerUserId) return
  showIcebreaker.value = true
  icebreakerLoading.value = true
  try {
    const session = await getIcebreakerSession(props.peerUserId)
    icebreakerSession.value = session
    icebreakerQuestions.value = Array.isArray(session?.questions) ? session.questions : []
    icebreakerQuestions.value.forEach((q) => {
      if (q.myAnswer) icebreakerDraft[q.id] = q.myAnswer
    })
  } catch {
    showToast({ message: '加载破冰题失败', type: 'fail' })
    showIcebreaker.value = false
  } finally {
    icebreakerLoading.value = false
  }
}

async function submitIcebreaker() {
  if (!props.peerUserId || icebreakerSubmitting.value) return
  const answers = icebreakerQuestions.value
    .map((q) => ({
      questionId: q.id,
      answerText: String(icebreakerDraft[q.id] || '').trim()
    }))
    .filter((a) => a.answerText)
  if (!answers.length) {
    showToast('请至少回答一题')
    return
  }
  icebreakerSubmitting.value = true
  try {
    const res = await submitIcebreakerAnswers(props.peerUserId, answers)
    icebreakerSession.value = res?.session || icebreakerSession.value
    icebreakerQuestions.value = res?.session?.questions || icebreakerQuestions.value
    showToast({ type: 'success', message: '回答已保存' })
  } catch {
    showToast({ message: '提交失败', type: 'fail' })
  } finally {
    icebreakerSubmitting.value = false
  }
}

async function openCompatibility() {
  if (!props.peerUserId) return
  showCompatibility.value = true
  compatibilityLoading.value = true
  try {
    const session = await getCompatibilitySession(props.peerUserId)
    compatibilitySession.value = session
    compatibilityQuestions.value = Array.isArray(session?.questions) ? session.questions : []
    compatibilityQuestions.value.forEach((q) => {
      if (q.myOption) compatibilityDraft[q.id] = q.myOption
    })
  } catch {
    showToast({ message: '加载默契测试失败', type: 'fail' })
    showCompatibility.value = false
  } finally {
    compatibilityLoading.value = false
  }
}

async function submitCompatibility() {
  if (!props.peerUserId || compatibilitySubmitting.value) return
  const answers = compatibilityQuestions.value
    .map((q) => ({
      questionId: q.id,
      selectedOption: String(compatibilityDraft[q.id] || '').trim()
    }))
    .filter((a) => a.selectedOption)
  if (!answers.length) {
    showToast('请至少完成一题')
    return
  }
  compatibilitySubmitting.value = true
  try {
    const res = await submitCompatibilityAnswers(props.peerUserId, answers)
    compatibilitySession.value = res?.session || compatibilitySession.value
    compatibilityQuestions.value = res?.session?.questions || compatibilityQuestions.value
    showToast({ type: 'success', message: '答案已保存' })
  } catch {
    showToast({ message: '提交失败', type: 'fail' })
  } finally {
    compatibilitySubmitting.value = false
  }
}

defineExpose({ openIcebreaker, openCompatibility })
</script>

<style scoped>
.icebreaker-popup {
  padding: 20px 16px 28px;
  max-height: 85vh;
  overflow-y: auto;
}

.icebreaker-title {
  margin: 0 0 6px;
  font-size: 18px;
  font-weight: 800;
  color: #1a2236;
}

.icebreaker-sub {
  margin: 0 0 16px;
  font-size: 13px;
  color: #8898aa;
}

.icebreaker-q {
  margin-bottom: 14px;
}

.icebreaker-q-text {
  margin: 0 0 8px;
  font-size: 14px;
  font-weight: 600;
  color: #334155;
}

.icebreaker-peer {
  margin: 8px 0 0;
  padding: 8px 10px;
  background: #f8fafc;
  border-radius: 8px;
  font-size: 13px;
  color: #64748b;
}

.icebreaker-loading {
  text-align: center;
  padding: 24px;
  color: #94a3b8;
}

.compat-score {
  margin-bottom: 14px;
  padding: 12px;
  border-radius: 12px;
  background: linear-gradient(135deg, #fff0f5, #fff);
  text-align: center;
  font-size: 14px;
  color: #64748b;
}

.compat-score strong {
  font-size: 22px;
  color: #ff5f84;
}

.secondary-btn {
  margin-top: 10px;
}
</style>
