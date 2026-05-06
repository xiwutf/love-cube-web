<template>
  <div class="help-page">
    <NavBar title="帮我们做得更好" />

    <div class="form-card">
      <p class="subtitle">你的建议，可能成为下一次更新内容</p>

      <div class="question-block">
        <h3>① 你最关注哪个模块？</h3>
        <van-radio-group v-model="focusModule" direction="vertical">
          <van-radio v-for="item in moduleOptions" :key="item" :name="item">{{ item }}</van-radio>
        </van-radio-group>
      </div>

      <div class="question-block">
        <h3>② 你来网站最想获得什么？（可多选）</h3>
        <van-checkbox-group v-model="goals" direction="vertical">
          <van-checkbox v-for="item in goalOptions" :key="item" :name="item">{{ item }}</van-checkbox>
        </van-checkbox-group>
      </div>

      <div class="question-block">
        <h3>③ 你觉得目前网站最缺什么？</h3>
        <van-radio-group v-model="missingPoint" direction="vertical">
          <van-radio v-for="item in missingOptions" :key="item" :name="item">{{ item }}</van-radio>
        </van-radio-group>
      </div>

      <div class="question-block">
        <h3>④ 你最希望新增什么功能？（必填）</h3>
        <van-field
          v-model="featureSuggestion"
          rows="4"
          autosize
          type="textarea"
          maxlength="300"
          show-word-limit
          placeholder="请输入你的建议，例如：私信聊天、同城匹配、AI助手..."
        />
      </div>

      <div class="question-block">
        <h3>⑤ 联系方式（选填）</h3>
        <van-field v-model="contact" placeholder="微信 / QQ / 手机号" />
        <p class="contact-tip">如建议被采纳，可能邀请你参与内测</p>
      </div>

      <van-field
        v-model="extraRemark"
        rows="2"
        autosize
        type="textarea"
        maxlength="200"
        show-word-limit
        placeholder="补充说明（选填）"
      />
      <van-button block type="primary" round :loading="submitting" @click="submit">提交建议</van-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { showToast } from 'vant'
import NavBar from '@/components/NavBar.vue'
import { submitFeedback } from '@/api/feedback.js'

const moduleOptions = ['联谊', '活动中心', '内容资讯', '平台公告', '本地服务', 'AI工具', '个人中心']
const goalOptions = ['认识新朋友', '参与联谊', '获取信息内容', '参加活动', '使用实用工具', '本地资源', '随便看看']
const missingOptions = ['用户太少', '功能太少', '页面不够好看', '不知道怎么玩', '内容太少', '信任感不足', '其他']

const focusModule = ref('')
const goals = ref([])
const missingPoint = ref('')
const featureSuggestion = ref('')
const extraRemark = ref('')
const contact = ref('')
const submitting = ref(false)

async function submit() {
  const suggestion = featureSuggestion.value.trim()
  if (!focusModule.value) {
    showToast({ type: 'fail', message: '请先选择你最关注的模块' })
    return
  }
  if (!goals.value.length) {
    showToast({ type: 'fail', message: '请至少选择一个来站目标' })
    return
  }
  if (!missingPoint.value) {
    showToast({ type: 'fail', message: '请先选择当前最缺项' })
    return
  }
  if (!suggestion) {
    showToast({ type: 'fail', message: '请填写最希望新增的功能' })
    return
  }

  submitting.value = true
  try {
    const detailLines = [
      `Q1-最关注模块：${focusModule.value}`,
      `Q2-来站目标：${goals.value.join('、')}`,
      `Q3-当前最缺：${missingPoint.value}`,
      `Q4-新增功能建议：${suggestion}`
    ]
    if (extraRemark.value.trim()) {
      detailLines.push(`补充说明：${extraRemark.value.trim()}`)
    }
    await submitFeedback({
      module: focusModule.value,
      goals: goals.value,
      missing: missingPoint.value,
      content: detailLines.join('\n'),
      contact: contact.value.trim()
    })
    focusModule.value = ''
    goals.value = []
    missingPoint.value = ''
    featureSuggestion.value = ''
    extraRemark.value = ''
    contact.value = ''
    showToast({ type: 'success', message: '感谢参与平台共建 ❤️' })
  } catch (e) {
    showToast({ type: 'fail', message: e.message || '提交失败，请稍后重试' })
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.help-page {
  min-height: 100vh;
  background: #f8f9fb;
  padding-bottom: 24px;
}

.form-card {
  margin: 12px;
  border-radius: 16px;
  background: #fff;
  border: 1px solid #eff0f4;
  padding: 14px;
}

.subtitle {
  margin: 2px 0 12px;
  color: #666;
  font-size: 13px;
}

.question-block {
  margin-bottom: 14px;
}

.question-block h3 {
  margin: 0 0 8px;
  font-size: 15px;
  line-height: 1.4;
}

.contact-tip {
  margin: 6px 0 0;
  color: #8b8b8b;
  font-size: 12px;
}
</style>



