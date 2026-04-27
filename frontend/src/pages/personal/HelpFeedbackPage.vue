<template>
  <div class="help-page">
    <NavBar title="帮助与反" />

    <van-cell-group inset class="faq-card">
      <van-cell title="如何提高匹配成功率？" label="完善资料、上传清晰头像主动打招呼" />
      <van-cell title="为什么看不到更多用户？" label="可前往发现页搜索，或稍后刷新推荐池" />
      <van-cell title="如何保护隐私？" label="在隐私设置中可控制展示项和消息权限" />
    </van-cell-group>

    <div class="form-card">
      <h3>提交反馈</h3>
      <van-field
        v-model="content"
        rows="5"
        autosize
        type="textarea"
        maxlength="300"
        show-word-limit
        placeholder="请输入你遇到的问题或建议"
      />
      <van-field v-model="contact" label="联系方式" placeholder="手机号/微信（选填）" />
      <van-button block type="primary" round :loading="submitting" @click="submit">提交反馈</van-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { showToast } from 'vant'
import NavBar from '@/components/NavBar.vue'
import { submitFeedback } from '@/api/feedback.js'

const content = ref('')
const contact = ref('')
const submitting = ref(false)

async function submit() {
  const text = content.value.trim()
  if (!text) {
    showToast({ type: 'fail', message: '请先填写反馈内容' })
    return
  }

  submitting.value = true
  try {
    await submitFeedback({
      content: text,
      contact: contact.value.trim()
    })
    content.value = ''
    contact.value = ''
    showToast({ type: 'success', message: '反馈已提交，感谢支持' })
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

.faq-card {
  margin-top: 8px;
}

.form-card {
  margin: 12px;
  border-radius: 16px;
  background: #fff;
  border: 1px solid #eff0f4;
  padding: 14px;
}

.form-card h3 {
  margin: 0 0 10px;
  font-size: 16px;
}
</style>


