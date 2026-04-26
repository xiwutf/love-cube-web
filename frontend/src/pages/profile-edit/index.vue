<template>
  <div class="profile-edit-page">
    <NavBar title="编辑资料">
      <template #right>
        <van-button size="small" type="primary" :loading="saving" @click="handleSave">保存</van-button>
      </template>
    </NavBar>

    <div class="form-body">
      <!-- 头像 -->
      <div class="avatar-section" @click="handlePickAvatar">
        <van-image round width="72" height="72" :src="form.avatar || DEFAULT_AVATAR" fit="cover">
          <template #error><div class="avatar-fallback">{{ (form.nickname || '?')[0] }}</div></template>
        </van-image>
        <p class="avatar-hint">点击更换头像 <van-loading v-if="uploading" size="14" /></p>
      </div>

      <!-- 基础信息 -->
      <van-cell-group inset title="基本信息">
        <van-field v-model="form.nickname"   label="昵称"   placeholder="请输入昵称"   maxlength="20" />
        <van-field v-model="form.signature"  label="签名"   placeholder="写点什么介绍自己"
                   type="textarea" rows="2" autosize maxlength="100" show-word-limit />
        <van-field
          v-model="form.gender"
          label="性别"
          placeholder="请选择性别"
          readonly
          is-link
          @click="showGenderPicker = true"
        />
        <van-field
          v-model="form.birthday"
          label="生日"
          placeholder="请选择生日"
          readonly
          is-link
          @click="showDatePicker = true"
        />
        <van-field v-model="form.location"   label="所在地" placeholder="如：上海市" />
        <van-field v-model="form.occupation" label="职业"   placeholder="如：设计师" maxlength="20" />
        <van-field
          v-model="heightLabel"
          label="身高"
          placeholder="请选择身高"
          readonly
          is-link
          @click="showHeightPicker = true"
        />
      </van-cell-group>

      <!-- 生活照 -->
      <van-cell-group inset title="生活照（可持续添加）" class="photos-group">
        <div class="photos-grid">
          <div v-for="(src, i) in form.photos" :key="i" class="photo-item">
            <van-image :src="toFullUrl(src)" width="100%" height="90" fit="cover" radius="6"
                       @click="previewPhoto(i)" />
            <van-icon name="cross" class="photo-delete" @click.stop="removePhoto(i)" />
          </div>
          <div class="photo-add" @click="handlePickPhotos">
            <van-loading v-if="uploading" size="20" />
            <van-icon v-else name="plus" size="28" color="#ccc" />
          </div>
        </div>
      </van-cell-group>
    </div>

    <!-- 性别选择 Popup -->
    <van-popup v-model:show="showGenderPicker" position="bottom" round>
      <van-picker
        :columns="genderOptions"
        @confirm="onGenderConfirm"
        @cancel="showGenderPicker = false"
      />
    </van-popup>

    <!-- 生日选择 Popup -->
    <van-popup v-model:show="showDatePicker" position="bottom" round>
      <van-date-picker
        v-model="currentDate"
        title="选择生日"
        :min-date="new Date(1950, 0, 1)"
        :max-date="new Date()"
        @confirm="onDateConfirm"
        @cancel="showDatePicker = false"
      />
    </van-popup>

    <!-- 身高选择 Popup -->
    <van-popup v-model:show="showHeightPicker" position="bottom" round>
      <van-picker
        :columns="heightOptions"
        @confirm="onHeightConfirm"
        @cancel="showHeightPicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog, showImagePreview } from 'vant'
import NavBar from '@/components/NavBar.vue'
import { getMe, updateProfile } from '@/api/user.js'
import { normalizeUser } from '@/utils/normalizeUser.js'
import { toFullUrl, DEFAULT_AVATAR } from '@/utils/image.js'
import { useImageUpload } from '@/composables/useImageUpload.js'
import { formatDate } from '@/utils/format.js'

const router  = useRouter()
const saving  = ref(false)
const isDirty = ref(false)

const { pickAndUpload, pickMultipleAndUpload, uploading } = useImageUpload()

// 表单数据
const form = reactive({
  avatar:     '',
  nickname:   '',
  gender:     '',
  birthday:   '',
  location:   '',
  occupation: '',
  height:     '',
  signature:  '',
  photos:     [],
})

// Picker 控制
const showGenderPicker = ref(false)
const showDatePicker   = ref(false)
const showHeightPicker = ref(false)
const currentDate      = ref([String(new Date().getFullYear() - 25), '01', '01'])

const genderOptions = ['男', '女']
const heightOptions = Array.from({ length: 81 }, (_, i) => `${140 + i}cm`)
const heightLabel   = computed(() => form.height ? `${form.height}cm` : '')

// 标记修改
function watchDirty() { isDirty.value = true }

// 加载初始数据
onMounted(async () => {
  try {
    const data = await getMe()
    const u    = normalizeUser(data)
    form.avatar     = u.avatar     || ''
    form.nickname   = u.nickname   || ''
    form.gender     = u.gender     || ''
    form.birthday   = u.birthday   || ''
    form.location   = u.location   || ''
    form.occupation = u.occupation || ''
    form.height     = u.height     || ''
    form.signature  = u.signature  || ''
    form.photos     = Array.isArray(data.photos) ? [...data.photos] : []
    // 初始化日期 picker 的默认值
    if (form.birthday) {
      const [y, m, d] = form.birthday.split('-')
      if (y && m && d) currentDate.value = [y, m, d]
    }
  } catch {
    showToast({ message: '加载失败', type: 'fail' })
  }
  // 初始化完成后再监听变化
  setTimeout(() => {
    Object.keys(form).forEach(key => {
      // 响应式监听由用户操作触发，依赖 isDirty
    })
  }, 100)
})

// 离开前提示
onBeforeUnmount(() => {
  // 由路由守卫处理（此处无法阻断路由跳转，提示依靠保存按钮）
})

async function handlePickAvatar() {
  try {
    const url = await pickAndUpload({ quality: 0.8 })
    if (url) { form.avatar = url; isDirty.value = true }
  } catch (e) {
    showToast({ message: e.message || '上传失败', type: 'fail' })
  }
}

async function handlePickPhotos() {
  try {
    const urls = await pickMultipleAndUpload()
    form.photos.push(...urls)
    isDirty.value = true
  } catch (e) {
    showToast({ message: e.message || '上传失败', type: 'fail' })
  }
}

function removePhoto(index) {
  form.photos.splice(index, 1)
  isDirty.value = true
}

function previewPhoto(startIndex) {
  showImagePreview({ images: form.photos.map(toFullUrl), startPosition: startIndex })
}

function onGenderConfirm({ selectedValues }) {
  form.gender        = selectedValues[0]
  showGenderPicker.value = false
  isDirty.value      = true
}

function onDateConfirm({ selectedValues }) {
  form.birthday    = selectedValues.join('-')
  showDatePicker.value = false
  isDirty.value    = true
}

function onHeightConfirm({ selectedValues }) {
  form.height        = parseInt(selectedValues[0])
  showHeightPicker.value = false
  isDirty.value      = true
}

async function handleSave() {
  if (!form.nickname?.trim()) {
    showToast({ message: '昵称不能为空', type: 'fail' })
    return
  }
  saving.value = true
  try {
    await updateProfile({
      nickname:   form.nickname,
      avatar:     form.avatar,
      gender:     form.gender,
      birthday:   form.birthday,
      location:   form.location,
      occupation: form.occupation,
      height:     form.height,
      signature:  form.signature,
      photos:     form.photos,
    })
    isDirty.value = false
    showToast({ message: '保存成功', type: 'success' })
    setTimeout(() => router.back(), 800)
  } catch (e) {
    showToast({ message: e.message || '保存失败', type: 'fail' })
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.profile-edit-page { min-height: 100vh; background: #f8f8f8; padding-bottom: 32px; }

.form-body { padding: 8px 0; }

.avatar-section {
  display: flex; flex-direction: column; align-items: center;
  padding: 20px 16px; background: #fff; margin-bottom: 8px; cursor: pointer;
}
.avatar-hint { font-size: 12px; color: #aaa; margin-top: 8px; display: flex; align-items: center; gap: 4px; }
.avatar-fallback {
  width: 72px; height: 72px; border-radius: 50%;
  background: linear-gradient(135deg, #FF6B8A, #FFB3C1);
  display: flex; align-items: center; justify-content: center;
  font-size: 24px; color: #fff; font-weight: 700;
}

.photos-group { margin-top: 8px; }
.photos-grid {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; padding: 0 16px 12px;
}
.photo-item { position: relative; }
.photo-delete {
  position: absolute; top: -6px; right: -6px; background: rgba(0,0,0,.5);
  color: #fff; border-radius: 50%; padding: 2px; font-size: 12px; cursor: pointer; z-index: 1;
}
.photo-add {
  height: 90px; border: 1.5px dashed #ddd; border-radius: 6px;
  display: flex; align-items: center; justify-content: center; cursor: pointer;
}
</style>
