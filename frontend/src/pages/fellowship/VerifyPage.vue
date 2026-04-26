<template>
  <div class="verify-page">
    <NavBar title="认证中心" />

    <div v-if="loading" class="verify-loading">
      <van-loading type="spinner" color="#FF6B8A" />
    </div>

    <div v-else class="verify-content">
      <p class="verify-intro">通过认证可提升个人信任度，获得认证标识展示在资料页和推荐卡片上。</p>

      <!-- 真人头像认证 -->
      <div class="verify-card">
        <div class="verify-card-head">
          <span class="verify-icon">📷</span>
          <div class="verify-card-info">
            <p class="verify-name">真人头像认证</p>
            <p class="verify-desc">上传一张本人正脸照片，确认为真实用户</p>
          </div>
          <span class="verify-status-badge" :class="photoStatus">{{ statusLabel(photoStatus) }}</span>
        </div>

        <p v-if="photoStatus === 'approved'" class="verify-approved-msg">✅ 已认证</p>
        <p v-else-if="photoStatus === 'pending'" class="verify-pending-msg">🕐 审核中，请耐心等待</p>
        <template v-else>
          <p v-if="photoStatus === 'rejected'" class="verify-rejected-msg">
            ❌ 上次审核未通过：{{ photoRejectReason || '请重新提交' }}
          </p>
          <div class="verify-form">
            <van-uploader
              v-model="photoFiles"
              :max-count="1"
              :after-read="onPhotoRead"
              accept="image/*"
              class="verify-uploader"
            >
              <div class="upload-placeholder">
                <van-icon name="photograph" size="32" color="#ccc" />
                <p>点击上传本人照片</p>
              </div>
            </van-uploader>
            <van-button
              round block color="#FF6B8A"
              :loading="submitting === 'PHOTO'"
              :disabled="!photoFile"
              @click="submitPhoto"
            >提交真人认证</van-button>
          </div>
        </template>
      </div>

      <!-- 实名认证 -->
      <div class="verify-card">
        <div class="verify-card-head">
          <span class="verify-icon">🪪</span>
          <div class="verify-card-info">
            <p class="verify-name">实名认证</p>
            <p class="verify-desc">填写姓名与身份证后四位，平台严格保密</p>
          </div>
          <span class="verify-status-badge" :class="realnameStatus">{{ statusLabel(realnameStatus) }}</span>
        </div>

        <p v-if="realnameStatus === 'approved'" class="verify-approved-msg">✅ 已认证</p>
        <p v-else-if="realnameStatus === 'pending'" class="verify-pending-msg">🕐 审核中，请耐心等待</p>
        <template v-else>
          <p v-if="realnameStatus === 'rejected'" class="verify-rejected-msg">
            ❌ 上次审核未通过：{{ realnameRejectReason || '请重新提交' }}
          </p>
          <div class="verify-form">
            <van-field
              v-model="realName"
              label="真实姓名"
              placeholder="请输入真实姓名"
              class="verify-field"
            />
            <van-field
              v-model="idLast4"
              label="证件后四位"
              placeholder="身份证号后四位"
              maxlength="4"
              type="digit"
              class="verify-field"
            />
            <van-button
              round block color="#FF6B8A"
              :loading="submitting === 'REALNAME'"
              :disabled="!realName.trim() || idLast4.length < 4"
              @click="submitRealname"
            >提交实名认证</van-button>
          </div>
        </template>
      </div>

      <p class="verify-tip">认证信息仅用于审核，不会向其他用户展示原始数据</p>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { showToast } from 'vant'
import NavBar from '@/components/NavBar.vue'
import { getMyVerifications, submitVerification } from '@/api/verification.js'
import { uploadVerifyPhoto } from '@/api/upload.js'

const loading    = ref(true)
const submitting = ref('')

const photoStatus       = ref('none')
const photoRejectReason = ref('')
const photoFiles        = ref([])
const photoFile         = ref(null)

const realnameStatus       = ref('none')
const realnameRejectReason = ref('')
const realName = ref('')
const idLast4  = ref('')

function statusLabel(s) {
  return { approved: '已认证', pending: '审核中', rejected: '未通过', none: '未认证' }[s] || '未认证'
}

function latestByType(list, type) {
  return list
    .filter(v => (v.verifyType || '').toUpperCase() === type)
    .sort((a, b) => new Date(b.submittedAt) - new Date(a.submittedAt))[0] || null
}

async function load() {
  loading.value = true
  try {
    const list = await getMyVerifications()
    if (Array.isArray(list)) {
      const photo    = latestByType(list, 'PHOTO')
      const realname = latestByType(list, 'REALNAME')
      if (photo) {
        photoStatus.value       = photo.status || 'none'
        photoRejectReason.value = photo.rejectReason || ''
      }
      if (realname) {
        realnameStatus.value       = realname.status || 'none'
        realnameRejectReason.value = realname.rejectReason || ''
      }
    }
  } catch {
    // no verifications yet
  } finally {
    loading.value = false
  }
}

function onPhotoRead(file) {
  photoFile.value = file.file || null
}

async function submitPhoto() {
  if (!photoFile.value) return
  submitting.value = 'PHOTO'
  try {
    const res = await uploadVerifyPhoto(photoFile.value)
    const selfieUrl = res?.url || res?.data?.url || ''
    if (!selfieUrl) throw new Error('上传失败，未获得图片地址')
    await submitVerification('PHOTO', JSON.stringify({ selfieUrl }))
    photoStatus.value = 'pending'
    photoFiles.value  = []
    photoFile.value   = null
    showToast({ message: '已提交，等待审核', type: 'success' })
  } catch (e) {
    showToast({ message: e.response?.data?.message || e.message || '提交失败', type: 'fail' })
  } finally {
    submitting.value = ''
  }
}

async function submitRealname() {
  if (!realName.value.trim() || idLast4.value.length < 4) return
  submitting.value = 'REALNAME'
  try {
    const submitData = JSON.stringify({ realName: realName.value.trim(), idLast4: idLast4.value })
    await submitVerification('REALNAME', submitData)
    realnameStatus.value = 'pending'
    realName.value = ''
    idLast4.value  = ''
    showToast({ message: '已提交，等待审核', type: 'success' })
  } catch (e) {
    showToast({ message: e.response?.data?.message || e.message || '提交失败', type: 'fail' })
  } finally {
    submitting.value = ''
  }
}

onMounted(load)
</script>

<style scoped>
.verify-page { min-height: 100vh; background: #f8f8f8; padding-bottom: 40px; }
.verify-loading { display: flex; justify-content: center; padding-top: 120px; }
.verify-content { padding: 12px 14px; }
.verify-intro { font-size: 13px; color: #999; margin-bottom: 14px; line-height: 1.6; }

.verify-card {
  background: #fff; border-radius: 14px;
  padding: 16px; margin-bottom: 14px;
  box-shadow: 0 2px 10px rgba(0,0,0,.05);
}
.verify-card-head {
  display: flex; align-items: flex-start; gap: 12px; margin-bottom: 14px;
}
.verify-icon { font-size: 26px; flex-shrink: 0; margin-top: 2px; }
.verify-card-info { flex: 1; }
.verify-name { font-size: 15px; font-weight: 600; color: #333; margin-bottom: 2px; }
.verify-desc { font-size: 12px; color: #999; }

.verify-status-badge {
  flex-shrink: 0; padding: 3px 8px; border-radius: 20px;
  font-size: 11px; font-weight: 600; margin-top: 4px;
}
.verify-status-badge.approved { background: #e8f8f0; color: #07c160; }
.verify-status-badge.pending  { background: #fff8e1; color: #ff9800; }
.verify-status-badge.rejected { background: #ffeef0; color: #ee0a24; }
.verify-status-badge.none     { background: #f5f5f5; color: #aaa; }

.verify-approved-msg { text-align: center; color: #07c160; font-size: 14px; padding: 8px 0; }
.verify-pending-msg  { text-align: center; color: #ff9800; font-size: 14px; padding: 8px 0; }
.verify-rejected-msg {
  color: #ee0a24; font-size: 13px; margin-bottom: 12px;
  padding: 8px 12px; background: #fff5f5; border-radius: 8px;
}
.verify-form { display: flex; flex-direction: column; gap: 12px; }
.verify-uploader { width: 100%; }
.upload-placeholder {
  width: 100%; height: 120px; border: 2px dashed #eee;
  border-radius: 10px; display: flex; flex-direction: column;
  align-items: center; justify-content: center; color: #bbb; font-size: 13px; gap: 6px;
}
.verify-field { background: #f9f9f9; border-radius: 8px; overflow: hidden; }
.verify-tip { text-align: center; font-size: 12px; color: #bbb; margin-top: 8px; }
</style>
