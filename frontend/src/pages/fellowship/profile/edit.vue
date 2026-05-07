<template>
  <div class="fellowship-profile-edit-page">
    <NavBar title="编辑联谊资料" />

    <button
      v-if="verifyBannerVisible"
      type="button"
      class="verify-banner"
      @click="router.push('/fellowship/verify')"
    >
      <van-icon name="passed" class="verify-banner-icon" />
      <span class="verify-banner-text">您已通过认证 · 个人资料展示认证标识</span>
      <van-icon name="arrow" class="verify-banner-arrow" />
    </button>

    <van-form class="form-wrap" @submit="handleSubmit">
      <van-cell-group inset>
        <van-field v-model="form.nickname" label="昵称" placeholder="请输入昵称（最多20字）" maxlength="20" />
        <van-field
          :model-value="genderLabel"
          label="性别"
          placeholder="请选择性别"
          readonly
          is-link
          @click="showGenderPicker = true"
        />
        <van-field v-model="form.age" label="年龄" type="number" placeholder="请输入年龄（必填）" />
        <van-field v-model="form.birthYear" label="出生年份" type="number" placeholder="例如 1998（选填）" />
        <van-field
          :model-value="maritalStatusLabel"
          label="婚姻状况"
          placeholder="请选择婚姻状况"
          readonly
          is-link
          @click="showMaritalPicker = true"
        />
        <van-field
          :model-value="cityLabel"
          label="城市"
          placeholder="请选择城市"
          readonly
          is-link
          @click="showCityPicker = true"
        />
        <van-field v-model="form.occupation" label="职业" placeholder="请输入职业" />
        <van-field
          :model-value="educationLabel"
          label="学历"
          placeholder="请选择学历"
          readonly
          is-link
          @click="showEducationPicker = true"
        />
        <van-field v-model="form.height" label="身高(cm)" type="number" placeholder="例如 170" />
        <van-field v-model="form.bio" label="自我介绍" type="textarea" rows="2" autosize />
        <van-field v-model="form.intention" label="交友意向" type="textarea" rows="2" autosize />
        <van-field v-model="form.tags" label="标签" placeholder="多个标签用逗号分隔" />
        <van-field label="头像" readonly>
          <template #input>
            <div class="avatar-upload">
              <img v-if="form.avatarUrl" :src="form.avatarUrl" alt="头像预览" class="avatar-preview" />
              <div v-else class="avatar-placeholder">未上传</div>
              <van-button
                type="primary"
                size="small"
                plain
                :loading="avatarUploading"
                :disabled="avatarUploading || store.saving"
                @click="triggerAvatarUpload"
              >
                {{ avatarUploading ? '上传中...' : '上传头像' }}
              </van-button>
              <input
                ref="avatarInputRef"
                class="avatar-file-input"
                type="file"
                accept="image/*"
                @change="handleAvatarSelected"
              />
            </div>
          </template>
        </van-field>
        <van-field label="生活照" readonly label-align="top">
          <template #input>
            <div class="life-photos">
              <p class="life-photos-tip">与头像不同，用于展示日常形象。开通联谊前须至少保存 1 张，最多 9 张。</p>
              <div class="life-photos-grid">
                <div v-for="(photo, index) in lifePhotoList" :key="photo.id || index" class="life-photo-item">
                  <img :src="photo.url" alt="生活照" />
                  <span v-if="index === 0" class="life-photo-main">主展示</span>
                  <button
                    type="button"
                    class="life-photo-remove"
                    :disabled="lifePhotoDeleting"
                    @click.stop="removeLifePhoto(photo, index)"
                  >
                    删除
                  </button>
                </div>
                <button
                  v-if="lifePhotoList.length < 9"
                  type="button"
                  class="life-photo-add"
                  :disabled="lifePhotoUploading || lifePhotoDeleting || store.saving"
                  @click="triggerLifePhotoUpload"
                >
                  <van-icon name="plus" />
                  <span>{{ lifePhotoUploading ? '上传中…' : '上传生活照' }}</span>
                  <small>({{ lifePhotoList.length }}/9)</small>
                </button>
              </div>
              <input
                ref="lifePhotoInputRef"
                class="avatar-file-input"
                type="file"
                accept="image/*"
                multiple
                @change="handleLifePhotosSelected"
              />
            </div>
          </template>
        </van-field>
      </van-cell-group>

      <div class="btn-wrap">
        <van-button round block type="primary" native-type="submit" :loading="store.saving" :disabled="store.saving">
          保存资料
        </van-button>
      </div>
    </van-form>

    <van-popup v-model:show="showGenderPicker" position="bottom" round>
      <van-picker :columns="genderOptions" @confirm="onGenderConfirm" @cancel="showGenderPicker = false" />
    </van-popup>

    <van-popup v-model:show="showEducationPicker" position="bottom" round>
      <van-picker :columns="educationOptions" @confirm="onEducationConfirm" @cancel="showEducationPicker = false" />
    </van-popup>

    <van-popup v-model:show="showMaritalPicker" position="bottom" round>
      <van-picker :columns="maritalOptions" @confirm="onMaritalConfirm" @cancel="showMaritalPicker = false" />
    </van-popup>

    <van-popup v-model:show="showCityPicker" position="bottom" round>
      <van-area
        v-model="cityCode"
        title="选择省市"
        :columns-num="2"
        :area-list="areaList"
        :columns-placeholder="['请选择', '请选择']"
        @confirm="onCityConfirm"
        @cancel="showCityPicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup>
import { areaList } from '@vant/area-data'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import NavBar from '@/components/NavBar.vue'
import {
  deleteFellowshipPhoto,
  getFellowshipPhotos,
  saveFellowshipPhotos,
  uploadFellowshipAvatar,
  uploadFellowshipPhoto
} from '@/api/fellowshipProfile.js'
import { useFellowshipProfileStore } from '@/stores/fellowshipProfile.js'
import { useUserStore } from '@/stores/user.js'
import { userHasVerificationBadge } from '@/utils/displayFields.js'

const router = useRouter()
const store = useFellowshipProfileStore()
const userStore = useUserStore()

const verifyBannerVisible = computed(() => userHasVerificationBadge(store.profile ?? {}))

const form = reactive({
  nickname: '',
  gender: '',
  age: '',
  birthYear: '',
  maritalStatus: '',
  city: '',
  occupation: '',
  education: '',
  height: '',
  bio: '',
  intention: '',
  tags: '',
  avatarUrl: ''
})

const showGenderPicker = ref(false)
const showEducationPicker = ref(false)
const showMaritalPicker = ref(false)
const showCityPicker = ref(false)
const cityCode = ref('')
const avatarUploading = ref(false)
const avatarInputRef = ref(null)
const lifePhotoList = ref([])
const lifePhotoUploading = ref(false)
const lifePhotoDeleting = ref(false)
const lifePhotoInputRef = ref(null)

const genderOptions = [
  { text: '男', value: 'male' },
  { text: '女', value: 'female' }
]

const educationOptions = [
  { text: '高中及以下', value: '高中及以下' },
  { text: '大专', value: '大专' },
  { text: '本科', value: '本科' },
  { text: '硕士', value: '硕士' },
  { text: '博士', value: '博士' }
]

const maritalOptions = [
  { text: '未填写', value: '' },
  { text: '单身', value: '单身' },
  { text: '已婚', value: '已婚' },
  { text: '离异', value: '离异' }
]

const genderLabelMap = {
  male: '男',
  female: '女',
  男: '男',
  女: '女'
}

const genderLabel = computed(() => genderLabelMap[form.gender] || form.gender || '')
const educationLabel = computed(() => form.education || '')
const cityLabel = computed(() => form.city || '')
const maritalStatusLabel = computed(() => form.maritalStatus || '未填写')

function normalizePhotosResponse(res) {
  const photos = Array.isArray(res?.photos) ? res.photos : Array.isArray(res) ? res : []
  return photos.map((url, idx) => ({ id: `${idx}-${url}`, url }))
}

onMounted(async () => {
  try {
    const [data, photosRes] = await Promise.allSettled([store.fetchProfile(), getFellowshipPhotos()])
    if (data.status === 'fulfilled') {
      const d = data.value
      form.nickname = d.nickname || ''
      form.gender = d.gender || ''
      form.age = d.age || ''
      form.birthYear = d.birthYear || ''
      form.maritalStatus = d.maritalStatus || ''
      form.city = d.city || ''
      form.occupation = d.occupation || ''
      form.education = d.education || ''
      form.height = d.height || ''
      form.bio = d.bio || ''
      form.intention = d.intention || ''
      form.tags = d.tags || ''
      form.avatarUrl = d.avatarUrl || ''
      cityCode.value = findCityCodeByName(form.city)
    }
    if (photosRes.status === 'fulfilled') {
      lifePhotoList.value = normalizePhotosResponse(photosRes.value)
    }
    if (data.status === 'rejected') {
      throw data.reason
    }
  } catch (e) {
    showToast({ message: e?.message || '资料加载失败，请稍后重试', type: 'fail' })
  }
})

function onGenderConfirm({ selectedValues }) {
  form.gender = selectedValues[0]
  showGenderPicker.value = false
}

function onEducationConfirm({ selectedValues }) {
  form.education = selectedValues[0]
  showEducationPicker.value = false
}

function onMaritalConfirm({ selectedValues }) {
  form.maritalStatus = selectedValues[0]
  showMaritalPicker.value = false
}

function onCityConfirm({ selectedOptions, selectedValues }) {
  const provinceName = selectedOptions?.[0]?.text || ''
  const cityName = selectedOptions?.[1]?.text || ''
  form.city = [provinceName, cityName].filter(Boolean).join(' ')
  cityCode.value = selectedValues?.[1] || selectedValues?.[0] || ''
  showCityPicker.value = false
}

function findCityCodeByName(cityText) {
  if (!cityText) return ''
  const normalized = String(cityText).replace(/\s+/g, ' ').trim()
  const cityList = areaList.city_list || {}
  const provinceList = areaList.province_list || {}

  for (const [code, name] of Object.entries(cityList)) {
    if (normalized === name || normalized.endsWith(` ${name}`)) {
      return code
    }
  }

  for (const [code, name] of Object.entries(provinceList)) {
    if (normalized === name) return code
  }

  return ''
}

async function handleSubmit() {
  const nickname = String(form.nickname || '').trim()
  const age = Number(form.age)
  if (nickname.length > 20) {
    showToast({ message: '昵称最多 20 个字符', type: 'fail' })
    return
  }
  if (!Number.isInteger(age) || age <= 0) {
    showToast({ message: '请先填写有效年龄', type: 'fail' })
    return
  }
  if (!String(form.avatarUrl || '').trim()) {
    showToast({ message: '请先上传头像', type: 'fail' })
    return
  }
  try {
    await store.saveProfile({
      nickname,
      gender: form.gender,
      age,
      birthYear: form.birthYear ? Number(form.birthYear) : null,
      maritalStatus: form.maritalStatus,
      city: form.city,
      occupation: form.occupation,
      education: form.education,
      height: form.height ? Number(form.height) : null,
      bio: form.bio,
      intention: form.intention,
      tags: form.tags,
      avatarUrl: form.avatarUrl
    })
    await store.fetchProfile(true)
    await store.fetchCompletion(true)
    await userStore.refreshCurrentUser().catch(() => null)
    showToast({ message: '保存成功', type: 'success' })
    router.replace('/fellowship/profile')
  } catch (e) {
    console.error('[profile/edit] save failed:', e)
    showToast({ message: e?.response?.data?.message || e?.message || '保存失败，请稍后重试', type: 'fail' })
  }
}

function triggerAvatarUpload() {
  if (avatarUploading.value || store.saving) return
  avatarInputRef.value?.click()
}

function parseUploadUrl(res) {
  return res?.url || res?.data?.url || res?.photoUrl || ''
}

function triggerLifePhotoUpload() {
  if (lifePhotoUploading.value || lifePhotoDeleting.value || store.saving) return
  lifePhotoInputRef.value?.click()
}

async function handleLifePhotosSelected(event) {
  const files = Array.from(event.target.files || [])
  event.target.value = ''
  if (!files.length || lifePhotoUploading.value) return
  lifePhotoUploading.value = true
  try {
    const uploadResults = await Promise.all(files.map((file) => uploadFellowshipPhoto(file)))
    const uploadedUrls = uploadResults.map(parseUploadUrl).filter(Boolean)
    if (!uploadedUrls.length) throw new Error('照片上传返回为空')
    const next = [...uploadedUrls, ...lifePhotoList.value.map((item) => item.url)].slice(0, 9)
    await saveFellowshipPhotos(next)
    lifePhotoList.value = next.map((item, idx) => ({ id: `${idx}-${item}`, url: item }))
    await userStore.refreshCurrentUser().catch(() => null)
    showToast({ type: 'success', message: `已保存 ${uploadedUrls.length} 张生活照` })
  } catch (e) {
    showToast({ type: 'fail', message: e?.message || '生活照上传失败，请稍后重试' })
  } finally {
    lifePhotoUploading.value = false
  }
}

async function removeLifePhoto(photo, index) {
  const targetUrl = photo?.url || ''
  if (!targetUrl || lifePhotoDeleting.value) return
  try {
    await showConfirmDialog({
      title: '删除生活照',
      message: '删除后不可恢复，确定继续吗？',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  lifePhotoDeleting.value = true
  try {
    await deleteFellowshipPhoto(targetUrl)
    lifePhotoList.value = lifePhotoList.value.filter((item, itemIndex) => !(item.url === targetUrl && itemIndex === index))
    await userStore.refreshCurrentUser().catch(() => null)
    showToast({ type: 'success', message: '已删除' })
  } catch (e) {
    showToast({ type: 'fail', message: e?.message || '删除失败' })
  } finally {
    lifePhotoDeleting.value = false
  }
}

async function handleAvatarSelected(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file || avatarUploading.value) return
  avatarUploading.value = true
  try {
    const uploadRes = await uploadFellowshipAvatar(file)
    const avatarUrl = parseUploadUrl(uploadRes)
    if (!avatarUrl) {
      throw new Error('头像上传失败，请重试')
    }
    form.avatarUrl = avatarUrl
    showToast({ message: '头像上传成功', type: 'success' })
  } catch (e) {
    showToast({ message: e?.message || '头像上传失败，请稍后重试', type: 'fail' })
  } finally {
    avatarUploading.value = false
  }
}
</script>

<style scoped>
.fellowship-profile-edit-page { min-height: 100vh; background: #f8f8f8; }

.verify-banner {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 10px 12px 0;
  padding: 10px 12px;
  border: none;
  border-radius: 12px;
  width: calc(100% - 24px);
  box-sizing: border-box;
  text-align: left;
  background: linear-gradient(135deg, #ecfdf5 0%, #d1fae5 45%, #e0f2fe 100%);
  color: #065f46;
  font-size: 13px;
  font-weight: 600;
  box-shadow: 0 4px 14px rgba(6, 95, 70, 0.12);
}

.verify-banner-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.verify-banner-text {
  flex: 1;
  min-width: 0;
  line-height: 1.35;
}

.verify-banner-arrow {
  font-size: 14px;
  color: #047857;
  flex-shrink: 0;
}

.form-wrap { padding: 12px 0 24px; }
.btn-wrap { margin: 20px 16px 0; }

.avatar-upload {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar-preview,
.avatar-placeholder {
  width: 44px;
  height: 44px;
  border-radius: 50%;
}

.avatar-preview {
  object-fit: cover;
}

.avatar-placeholder {
  display: grid;
  place-items: center;
  font-size: 11px;
  color: #999;
  background: #f2f3f5;
}

.avatar-file-input {
  display: none;
}

.life-photos {
  width: 100%;
  padding-bottom: 4px;
}

.life-photos-tip {
  margin: 0 0 10px;
  font-size: 12px;
  line-height: 1.5;
  color: #969799;
}

.life-photos-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.life-photo-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 10px;
  overflow: hidden;
  background: #f2f3f5;
}

.life-photo-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.life-photo-main {
  position: absolute;
  left: 4px;
  top: 4px;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 600;
  color: #fff;
  background: rgba(255, 95, 132, 0.92);
}

.life-photo-remove {
  position: absolute;
  right: 4px;
  bottom: 4px;
  border: none;
  border-radius: 999px;
  padding: 2px 8px;
  font-size: 10px;
  color: #fff;
  background: rgba(0, 0, 0, 0.55);
  cursor: pointer;
}

.life-photo-add {
  aspect-ratio: 1;
  border: 1px dashed #dcdee0;
  border-radius: 10px;
  background: #fafafa;
  color: #646566;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  cursor: pointer;
  font: inherit;
}

.life-photo-add:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.life-photo-add span {
  font-size: 12px;
  font-weight: 600;
}

.life-photo-add small {
  font-size: 10px;
  color: #969799;
}
</style>


